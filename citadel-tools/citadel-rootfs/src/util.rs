use std::path::{Path,PathBuf};
use std::fs::{self,File};
use std::process::Command;
use std::io::{Read,Write,BufReader,BufRead};

use Result;

pub struct Workdir(PathBuf); 

impl Workdir {
    pub fn new(base: &str, channel: &str) -> Workdir {
        let mut pb = PathBuf::from(base);
        pb.push(channel);
        Workdir(pb)
    }

    pub fn find_next_version(&mut self) -> Result<usize> {
        let mut version = 1;
        loop {
            let path = self.0.join(version.to_string());
            if !path.exists() {
                self.set_version(version)?;
                return Ok(version);
            }
            version += 1;
        }
    }

    pub fn set_version(&mut self, version: usize) -> Result<()> {
        self.0.push(version.to_string());
        fs::create_dir_all(&self.0)?;
        Ok(())
    }

    pub fn filepath(&self, name: &str) -> PathBuf {
        self.0.join(name)
    }

}


///
/// Returns `true` if `path` matches the source field (first field) 
/// of any of the mount lines listed in /proc/mounts
///
pub fn is_path_mounted(path: &Path) -> Result<bool> {
    let path_str = path.to_str().unwrap();
    let f = File::open("/proc/mounts")?;
    let reader = BufReader::new(f);
    for line in reader.lines() {
        if let Some(s) = line?.split_whitespace().next() {
            if s == path_str {
                return Ok(true);
            }
        }
    }
    Ok(false)
}

///
/// Converts a `Path` into `&str` representation, assuming
/// that it contains valid utf-8
///
pub fn path_str(path: &Path) -> &str {
    path.to_str().unwrap()
}

pub fn rootfs_partition_paths() -> Result<Vec<PathBuf>> {
    let mut rootfs_paths = Vec::new();
    for dent in fs::read_dir("/dev/mapper")? {
        let path = dent?.path();
        if is_path_rootfs(&path) {
            rootfs_paths.push(path);
        }
    }
    Ok(rootfs_paths)
}
pub fn is_path_rootfs(path: &Path) -> bool {
    path_filename(path).starts_with("citadel-rootfs")
}

fn path_filename(path: &Path) -> &str {
    if let Some(osstr) = path.file_name() {
        if let Some(name) = osstr.to_str() {
            return name;
        }
    }
    ""
}

pub fn write_string_to_file(path: &Path, s: &str) -> Result<()> {
    let mut f = File::create(path)?;
    f.write_all(s.as_bytes())?;
    Ok(())
}

pub fn read_file_as_string(path: &Path) -> Result<String> {
    let mut f = File::open(path)?;
    let mut buffer = String::new();
    f.read_to_string(&mut buffer)?;
    Ok(buffer)
}

pub fn run_file_command(path: &Path) -> Result<String> {
    let path = path_str(path);
    let output = try_run_command("/usr/bin/file", &["-b", path])?;
    Ok(output)
}

pub fn run_xz_command(path: &Path, decompress: bool) -> Result<()> {
    let path = path_str(path);
    if decompress {
        let _ = try_run_command("/usr/bin/xz", &["-d", path])?;
    } else {
        let _ = try_run_command("/usr/bin/xz", &["-T0", path])?;
    }
    Ok(())
}

pub fn run_sha256_command(path: &Path) -> Result<String> {
    let path = path_str(path);
    let output = try_run_command("/usr/bin/sha256sum", &[path])?;

    let v: Vec<&str> = output.split_whitespace().collect();
    Ok(v[0].trim().to_owned())
}

pub fn run_verityformat_command(srcfile: &Path, hashfile: &Path) -> Result<String> {
    let srcfile = path_str(srcfile);
    let hashfile = path_str(hashfile);
    let output = try_run_command("/usr/sbin/veritysetup", 
                                 &["format", srcfile, hashfile])?;
    Ok(output)
}

pub fn run_verityinstall_command(block_device: &Path, salt: &str, data_blocks: usize, hash_offset: usize) -> Result<String> {
    let data_device = path_str(block_device).to_owned();
    let hash_device = path_str(block_device).to_owned();
    let arg1 = format!("--data-blocks={}", data_blocks);
    let arg2 = format!("--hash-offset={}", hash_offset);
    let arg3 = format!("--salt={}", salt);
    let output = try_run_command("/usr/sbin/veritysetup", &[&arg1, &arg2, &arg3, "format", &data_device, &hash_device])?;
    Ok(output)
}

pub fn run_write_image_dd(image_src: &Path, block_device: &Path) -> Result<()> {
    let src = format!("if={}", path_str(image_src));
    let dst = format!("of={}", path_str(block_device));
    let _ = try_run_command("/bin/dd", &[&src, &dst, "bs=4M"])?;
    Ok(())
}

fn try_run_command(cmd_path: &str, args: &[&str]) -> Result<String> {
    let mut cmd = Command::new(cmd_path);
    for arg in args {
        cmd.arg(arg);
    }

    let result = cmd.output()?;

    if !result.status.success() {
        let err = String::from_utf8(result.stderr)?;
        let argstr = args.join(" ");
        bail!("{} {} command failed: {}", cmd_path, argstr, err);
    }
    let output = String::from_utf8(result.stdout)?;
    Ok(output)
}
