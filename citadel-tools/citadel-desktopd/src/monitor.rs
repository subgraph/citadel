use std::collections::HashMap;
use std::path::{Path,PathBuf};
use std::sync::{Arc,Mutex,Once,ONCE_INIT};
use std::sync::atomic::{AtomicBool,Ordering};
use std::thread::{self,JoinHandle};
use std::os::unix::thread::JoinHandleExt;
use std::io::ErrorKind;

use nix::libc;
use nix::sys::signal;

use inotify::{Events,Inotify,EventMask,WatchMask,Event,WatchDescriptor};

use Result;

pub trait MonitorEventHandler: Send+Sync {
    fn file_added(&self, path: &Path) -> Result<()> { let _ = path; Ok(()) }
    fn file_removed(&self, path: &Path) -> Result<()> { let _ = path; Ok(()) }
    fn directory_added(&self, path: &Path) -> Result<()> { let _ = path; Ok(()) }
    fn directory_removed(&self, path: &Path) -> Result<()> { let _ = path; Ok(()) }
}

pub struct DirectoryMonitor {
    event_handler: Arc<Mutex<MonitorEventHandler>>,
    worker_handle: Option<WorkerHandle>,
}

impl DirectoryMonitor {
    pub fn new(handler: Arc<Mutex<MonitorEventHandler>>) -> DirectoryMonitor {
        initialize();
        DirectoryMonitor {
            event_handler: handler,
            worker_handle: None,
        }
    }

    pub fn set_monitor_sources(&mut self, sources: &[PathBuf]) {
        if let Some(handle) = self.worker_handle.take() {
            handle.stop();
            handle.wait();
        }
        let sources = Vec::from(sources);
        let h = MonitorWorker::start_worker(sources, self.event_handler.clone());
        self.worker_handle = Some(h);
    }
}

struct MonitorWorker {
    descriptors: HashMap<WatchDescriptor,PathBuf>,
    inotify: Inotify,
    exit_flag: Arc<AtomicBool>,
    watch_paths: Vec<PathBuf>,
    handler: Arc<Mutex<MonitorEventHandler>>,
}


impl MonitorWorker {
    fn start_worker(watch_paths: Vec<PathBuf>, handler: Arc<Mutex<MonitorEventHandler>>) -> WorkerHandle {
        let exit_flag = Arc::new(AtomicBool::new(false));
        let flag_clone = exit_flag.clone();
        let jhandle = thread::spawn(move || {

            let mut worker = match MonitorWorker::new(watch_paths, flag_clone, handler) {
                Ok(worker) => worker,
                Err(e) => {
                    info!("failed to initialize inotify handle: {}", e);
                    return;
                }
            };
            if let Err(e) = worker.run() {
                info!("error returned from worker thread: {}", e);
            }
        });
        WorkerHandle::new(jhandle, exit_flag)
    }

    fn new(watch_paths: Vec<PathBuf>, exit_flag: Arc<AtomicBool>, handler: Arc<Mutex<MonitorEventHandler>>) -> Result<MonitorWorker> {
        Ok(MonitorWorker {
            descriptors: HashMap::new(),
            inotify: Inotify::init()?,
            exit_flag,
            watch_paths,
            handler,
        })
    }

    fn add_watches(&mut self) -> Result<()> {
        let watch_flags = WatchMask::CREATE | WatchMask::DELETE | WatchMask::MOVED_TO |
            WatchMask::DONT_FOLLOW | WatchMask::ONLYDIR;
        for p in &self.watch_paths {
            let wd = self.inotify.add_watch(p, watch_flags)?;
            self.descriptors.insert(wd, p.clone());
        }
        Ok(())
    }

    fn read_events<'a>(&mut self, buffer: &'a mut [u8]) -> Result<Option<Events<'a>>> {
        if self.exit_flag.load(Ordering::Relaxed) {
            return Ok(None);
        }

        match self.inotify.read_events_blocking(buffer) {
            Ok(events) => Ok(Some(events)),
            Err(e) => {
                if e.kind() == ErrorKind::Interrupted {
                    Ok(None)
                } else {
                    Err(e.into())
                }
            }
        }
    }

    fn process_events(&self, events: Events) {
        for ev in events {
            if let Err(e) = self.handle_event(&ev) {
                info!("error handling inotify event: {}", e);
            }
        }
    }

    fn run(&mut self) -> Result<()> {
        info!("running monitor event loop");
        self.add_watches()?;
        let mut buffer = [0u8; 4096];
        loop {
            match self.read_events(&mut buffer)? {
                Some(events) => self.process_events(events),
                None => break,
            }
        }
        Ok(())
    }

    fn full_event_path(&self, ev: &Event) -> Result<PathBuf> {
        let filename = ev.name
            .ok_or(format_err!("inotify event received without a filename"))?;
        let path = self.descriptors.get(&ev.wd)
            .ok_or(format_err!("Failed to find descriptor for received inotify event"))?;
        Ok(path.join(filename))
    }

    fn handle_event(&self, ev: &Event) -> Result<()> {
        let handler = self.handler.lock().unwrap();
        let pb = self.full_event_path(ev)?;
        let path = pb.as_path();
        let is_create = ev.mask.intersects(EventMask::CREATE|EventMask::MOVED_TO);
        if !is_create && !ev.mask.contains(EventMask::DELETE) {
            return Err(format_err!("Unexpected mask value for inotify event: {:?}", ev.mask));
        }

        if ev.mask.contains(EventMask::ISDIR) {
            if is_create {
                handler.directory_added(path)?;
            } else {
                handler.directory_removed(path)?;
            }

        } else {
            if is_create {
                handler.file_added(path)?;
            } else {
                handler.file_removed(path)?;
            }
        }
        Ok(())
    }
}

pub struct WorkerHandle {
    join_handle: JoinHandle<()>,
    exit_flag: Arc<AtomicBool>,
}

impl WorkerHandle {
    fn new(join_handle: JoinHandle<()>, exit_flag: Arc<AtomicBool>) -> WorkerHandle {
        WorkerHandle { join_handle, exit_flag }
    }

    pub fn stop(&self) {
        info!("calling stop on monitor");
        let tid = self.join_handle.as_pthread_t();
        self.exit_flag.store(true, Ordering::Relaxed);
        unsafe {
            libc::pthread_kill(tid, signal::SIGUSR1 as libc::c_int);
        }
    }

    pub fn wait(self) {
        if let Err(e) = self.join_handle.join() {
            warn!("monitor thread panic with '{:?}'", e);
        }
    }
}

static INITIALIZE_ONCE: Once = ONCE_INIT;

fn initialize() {
    INITIALIZE_ONCE.call_once(|| {
        let h = signal::SigHandler::Handler(sighandler);
        let sa = signal::SigAction::new(h, signal::SaFlags::empty(), signal::SigSet::empty());
        if let Err(e) = unsafe { signal::sigaction(signal::SIGUSR1, &sa) } {
            warn!("Error setting signal handler: {}", e);
        }
    });
}

extern fn sighandler(_: libc::c_int) {
    // do nothing, signal is only used to EINTR blocking inotify call
}