

use {Result,Partition,Config};

pub struct BootSelection {
    partitions: Vec<Partition>,
}

impl BootSelection {
    pub fn load_partitions() -> Result<BootSelection> {
        let partitions = Partition::rootfs_partitions()
            .map_err(|e| format_err!("Could not load rootfs partition info: {}", e))?;

        Ok(BootSelection {
            partitions
        })
    }

    pub fn choose_install_partition(&self) -> Option<&Partition> {
        self.choose(|p| {
            // first pass, if there is a partition which is not mounted and 
            // not initialized use that one
            !p.is_mounted() && !p.is_initialized()
        }).or_else(|| self.choose(|p| {
            // second pass, just find one that's not mounted
            !p.is_mounted()
        }))
    }

    fn choose<F>(&self, pred: F) -> Option<&Partition> 
        where F: Sized + Fn(&&Partition) -> bool
    {
        self.partitions.iter().find(pred)
    }

    /// Find the best rootfs partition to boot from
    pub fn choose_boot_partition(&self) -> Option<&Partition> {
        let mut best: Option<&Partition> = None;

        for p in &self.partitions {
            if is_better(&best, p) {
                best = Some(p);
            }
        }
        best
    }


    /// Perform checks for error states at boot time.
    pub fn scan_boot_partitions(&self, config: &Config) -> Result<()> {
        for p in &self.partitions {
            if let Err(e) = p.boot_scan(config) {
                warn!("error in bootscan of partition {}: {}", p.path_str(), e);
            }
        }
        Ok(())
    }
}

fn is_better<'a>(current_best: &Option<&'a Partition>, other: &'a Partition) -> bool {

    // Only consider partitions in state NEW or state GOOD
    if !other.is_good() && !other.is_new() {
        return false;
    }
    // If metainfo is broken, then no, it's not better
    if !other.metainfo().is_ok() {
        return false;
    }

    let best = match *current_best {
        Some(p) => p,
        // No current 'best', so 'other' is better, whatever it is.
        None => return true,
    };

    // First parition with PREFER flag trumps everything else
    if best.is_preferred() {
        return false;
    }

    // These are guaranteed to unwrap()
    let best_version = best.metainfo().unwrap().version();
    let other_version = other.metainfo().unwrap().version();

    if best_version > other_version {
        return false;
    }

    if other_version > best_version {
        return true;
    }

    // choose NEW over GOOD if versions are the same 
    if other.is_new() && best.is_good() {
        return true;
    }
    // ... but if all things otherwise match, return first match
    false
}
