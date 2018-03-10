use std::path::{Path,PathBuf};
use std::str;
use std::cell::{RefCell,Ref};

use toml;
use ed25519_dalek::SIGNATURE_LENGTH;

use Result;
use Metainfo;
use BlockDev;
use Config;
use blockdev::AlignedBuffer;
use util::*;

const MAGIC: &[u8] = b"CTDL";

/// Size in bytes of the Partition Info Block
const BLOCK_SIZE: usize = 4096;

/// Size of the PIB header up to the metainfo data
const HEADER_SIZE: usize = 8;

/// The maximum length of metainfo data in a Partition Info Block.
/// This size is the entire block minus the header and signature.
pub const MAX_METAINFO_LEN: usize = BLOCK_SIZE - (HEADER_SIZE + SIGNATURE_LENGTH);

///
/// Flag to override the algorithm which selects a partition to mount during boot
///
const FLAG_PREFER: u8 = 1;

///
/// The last 4096 bytes of a rootfs block device stores a structure
/// called the Partition Info Block.
///
/// The layout of this structure is the following:
///
///     field     size (bytes)        offset
///     -----     ------------        ------
///
///     magic        4                  0
///     status       1                  4
///     flags        1                  5
///     length       2                  6
///
///     metainfo  <length>              8
///
///     signature    64              8 + length
///
/// magic     : Must match ascii bytes 'CTDL' for the block to be considered valid
///
/// status    : See `PartitionStatus` for description of defined valid values
///
/// flags     : Only one flag is defined, `FLAG_PREFER`
///
/// length    : Big endian 16 bit size in bytes of metainfo field
///
/// metainfo  : A utf-8 TOML document with various fields describing the rootfs image
///             on this partition.
///
/// signature : ed25519 signature of the content of metainfo field
///
struct Infoblock(RefCell<Vec<u8>>);

impl Infoblock {
    fn new() -> Infoblock {
        let v = vec![0; BLOCK_SIZE];
        Infoblock(RefCell::new(v))
    }

    fn reset(&self) {
        for b in &mut self.0.borrow_mut()[..] {
            *b = 0;
        }
        self.write_bytes(0, MAGIC);
    }

    fn w8(&self, idx: usize, val: u8) {
        self.0.borrow_mut()[idx] = val;
    }

    fn r8(&self, idx: usize) -> u8 {
        self.0.borrow()[idx]
    }

    fn write_bytes(&self, offset: usize, data: &[u8]) {
        self.0.borrow_mut()[offset..offset+data.len()].copy_from_slice(data)
    }

    fn read_bytes(&self, offset: usize, len: usize) -> Vec<u8> {
        Vec::from(&self.0.borrow()[offset..offset+len])
    }

    fn status(&self) -> u8 {
        self.r8(4)
    }

    fn set_status(&self, status: u8) {
        self.w8(4, status);
    }

    fn has_status(&self, status: u8) -> bool {
        self.is_valid() && self.status() == status
    }

    fn flags(&self) -> u8 {
        self.r8(5)
    }

    fn has_flag(&self, flag: u8) -> bool {
        self.is_valid() && (self.flags() & flag) == flag
    }

    /// Returns `true` if flag value changed
    fn set_flag(&self, flag: u8, value: bool) -> bool {
        let old = self.flags();

        if value {
            self.w8(5, old | flag);
        } else {
            self.w8(5, old & !flag);
        }
        self.flags() == old
    }

    fn metainfo_len(&self) -> usize {
        let high = self.r8(6) as usize;
        let low = self.r8(7) as usize;
        (high << 8) | low
    }

    fn set_metainfo_len(&self, mlen: usize) {
        assert!(mlen <= MAX_METAINFO_LEN);
        let high = (mlen >> 8) as u8;
        let low = mlen as u8;
        self.w8(6, high);
        self.w8(7, low);
    }

    fn write_metainfo(&self, metainfo: &[u8]) {
        self.set_metainfo_len(metainfo.len());
        self.write_bytes(8, metainfo);
    }

    fn write_signature(&self, signature: &[u8]) {
        assert_eq!(signature.len(), SIGNATURE_LENGTH);
        self.write_bytes(8 + self.metainfo_len(), signature);
    }

    fn read_metainfo(&self) -> Vec<u8> {
        assert!(self.is_valid());
        self.read_bytes(8, self.metainfo_len())
    }

    fn read_signature(&self) -> Vec<u8> {
        assert!(self.is_valid());
        self.read_bytes(8 + self.metainfo_len(), SIGNATURE_LENGTH)
    }

    fn is_valid(&self) -> bool {
        &self.0.borrow()[0..4] == MAGIC &&
            is_valid_status_code(self.status()) &&
            self.flags() & !FLAG_PREFER == 0 &&
            self.metainfo_len() > 0 && self.metainfo_len() <= MAX_METAINFO_LEN
    }

    fn from_slice(&self, bytes: &[u8]) {
        self.0.borrow_mut().copy_from_slice(bytes);
    }

    fn as_ref(&self) -> Ref<Vec<u8>> {
        self.0.borrow()
    }
}

pub struct Partition {
    path: PathBuf,
    is_mounted: bool,
    infoblock: Infoblock,
}

impl Partition {
    ///
    /// Return a `Vec` of all rootfs partitions on the system.  Usually
    /// there are two (rootfsA and rootfsB).
    ///
    pub fn rootfs_partitions() -> Result<Vec<Partition>> {
        let mut v = Vec::new();
        for path in rootfs_partition_paths()? {
            v.push(Partition::load(&path)?);
        }
        Ok(v)
    }

    /// Construct a new `Partition` object for the device `dev` and load 
    /// the Partition Info Block structure from the block device.
    fn load(dev: &Path) -> Result<Partition> {
        let is_mounted = is_path_mounted(dev)?;
        let part = Partition::new(dev, is_mounted);
        part.read_infoblock()?;
        Ok(part)
    }

    fn new(path: &Path, is_mounted: bool) -> Partition {
        Partition {
            path: path.to_path_buf(), 
            is_mounted,
            infoblock: Infoblock::new(),
        }
    }

    ///
    /// For the passed in `BlockDev` instance calculate and return 
    /// the sector offset of the Partition Info Block, which is 
    /// located 8 sectors (4096 bytes) from the end of the partition.  
    ///
    fn infoblock_offset(&self, bdev: &BlockDev) -> Result<usize> {
        let nsectors = bdev.nsectors()?;
        if nsectors < 8 {
            bail!("{} is a block device but it's very short, {} sectors", 
                  self.path_str(), nsectors);
        }
        Ok(nsectors - 8)
    }

    ///
    /// Open the block device for this partition and load the
    /// Partition Info Block into the internal buffer `self.infoblock`.
    ///
    fn read_infoblock(&self) -> Result<()> {
        let mut dev = BlockDev::open_ro(&self.path)?;
        let off = self.infoblock_offset(&dev)?;
        let mut buffer = AlignedBuffer::new(BLOCK_SIZE);
        dev.read_sectors(off, buffer.as_mut())?;
        self.infoblock.from_slice(buffer.as_ref());
        Ok(())
    }

    ///
    /// Open the block device for this partition and write the
    /// internal buffer `self.infoblock` into the Partition Info
    /// Block.
    ///
    fn write_infoblock(&self) -> Result<()> {
        let mut dev = BlockDev::open_rw(&self.path)?;
        let off = self.infoblock_offset(&dev)?;
        let buffer = AlignedBuffer::from_slice(self.infoblock.as_ref().as_slice());
        dev.write_sectors(off, buffer.as_ref())?;
        Ok(())
    }

    pub fn path(&self) -> &Path {
        &self.path
    }

    pub fn path_str(&self) -> &str {
        self.path.to_str().unwrap()
    }

    ///
    /// Returns true if this partition is currently mounted and
    /// cannot be written to.
    ///
    pub fn is_mounted(&self) -> bool {
        self.is_mounted
    }

    ///
    /// Update the Partition Info Block for this partition with a new
    /// status field.
    ///
    pub fn write_status(&self, status: u8) -> Result<()> {
        self.infoblock.set_status(status);
        self.write_infoblock()?;
        Ok(())
    }

    pub fn set_prefer_flag(&self, value: bool) -> Result<()> {
        if self.infoblock.set_flag(FLAG_PREFER, value) {
            self.write_infoblock()?;
        }
        Ok(())
    }

    ///
    /// Write metainfo and signature to the Partition Info Block of this partition.  
    ///
    /// This also sets the internal buffer `self.infoblock` to the block contents
    /// written to disk.
    ///
    /// Writing new partition info also set status to `STATUS_INVALID` as this function
    /// is meant to be called in preparation for writing a raw disk image to partition.
    ///
    /// Caller should write status as `STATUS_NEW` after raw image has been successfully
    /// written to partition.
    ///
    pub fn write_partition_info(&self, metainfo: &[u8], signature: &[u8]) -> Result<()> {
        let mlen = metainfo.len();

        if mlen > MAX_METAINFO_LEN {
            bail!("cannot write partition because metainfo field is too long ({} bytes)", metainfo.len());
        }

        if mlen == 0 {
            bail!("cannot write partition because metainfo is empty");
        }

        if signature.len() != SIGNATURE_LENGTH {
            bail!("cannot write partition info because signature has wrong length {} != {}",
                  signature.len(), SIGNATURE_LENGTH);
        }

        self.infoblock.reset();
        self.infoblock.write_metainfo(metainfo);
        self.infoblock.write_signature(signature);
        
        self.write_infoblock()?;

        Ok(())
    }

    ///
    /// Returns true only if this partition has a valid Partition Information
    /// Block and the signature on the metainfo field can be verified with
    /// the provided `PublicKey`.
    ///
    pub fn verify_signature(&self, config: &Config) -> Result<bool> {
        if !self.infoblock.is_valid() {
            bail!("Cannot verify signature because partition is invalid");
        }
        let metainfo = self.metainfo()?;
        let channel = match config.channel(metainfo.channel()) {
            Some(ch) => ch,
            None => bail!("No public key configured for channel '{}'", metainfo.channel()),
        };

        let data = self.infoblock.read_metainfo();
        let signature = self.infoblock.read_signature();
        Ok(channel.verify(&data, &signature)?)
    }

    ///
    /// Parse the bytes from the metainfo section of the Partition Information
    /// Block and return a `Metainfo` structure.
    ///
    pub fn metainfo(&self) -> Result<Metainfo> {
        if !self.infoblock.is_valid() {
            bail!("partition is invalid");
        }
        let bytes = self.infoblock.read_metainfo();
        let metainfo = toml::from_slice::<Metainfo>(&bytes)?;
        Ok(metainfo)
    }

    pub fn is_new(&self) -> bool {
        self.infoblock.has_status(STATUS_NEW)
    }

    pub fn is_good(&self) -> bool {
        self.infoblock.has_status(STATUS_GOOD)
    }

    pub fn is_preferred(&self) -> bool {
        self.infoblock.has_flag(FLAG_PREFER)
    }

    pub fn status_label(&self) -> String {
        status_code_label(self.infoblock.status())
    }

    /// `true` if the Partition Info Block fields
    /// contain legal values. `false` indicates that
    /// the data is corrupted or was never written to
    /// this partition.
    pub fn is_initialized(&self) -> bool {
        self.infoblock.is_valid()
    }

    pub fn write_image(&self, image_path: &Path, metainfo: &[u8], signature: &[u8]) -> Result<()> {
        if self.is_mounted {
            bail!("Cannot write to mounted device {}", self.path_str());
        }
        info!("Writing raw rootfs disk image to {}", self.path_str());
        self.write_partition_info(metainfo, signature)?;
        run_write_image_dd(image_path, &self.path)?;

        let meta = image_path.metadata()?;
        let len = meta.len() as usize;
        let nblocks = len / 4096;

        info!("Generating dm-verity hash tree");
        let mut verityinfo = image_path.to_path_buf();
        verityinfo.pop();
        verityinfo.push("verity-format.out");
        let out = run_verityinstall_command(self.path(), self.metainfo()?.verity_salt(), nblocks, len)?;
        write_string_to_file(&verityinfo, &out)?;

        info!("Setting parition status field to STATUS_NEW");
        self.write_status(STATUS_NEW)?;

        Ok(())
    }

    /// Called at boot to perform various checks and possibly
    /// update the status field to an error state.
    ///
    /// Mark `STATUS_TRY_BOOT` partition as `STATUS_FAILED`.
    ///
    /// If metainfo cannot be parsed, mark as `STATUS_BAD_META`.
    ///
    /// Verify metainfo signature and mark `STATUS_BAD_SIG` if
    /// signature verification fails.
    ///
    pub fn boot_scan(&self, config: &Config) -> Result<()> {
        if !self.is_initialized() {
            return Ok(());
        }
        if self.infoblock.status() == STATUS_TRY_BOOT {
            warn!("Partition {} has STATUS_TRY_BOOT, assuming it failed boot attempt and marking STATUS_FAILED", self.path_str());
            self.write_status(STATUS_FAILED)?;
        }

        if let Err(_) = self.metainfo() {
            warn!("Partition {} has invalid metainfo, setting STATUS_BAD_META", self.path_str());
            self.write_status(STATUS_BAD_META)?;
            return Ok(());
        }

        match self.verify_signature(config) {
            Err(e) => {
                warn!("Error verifying parition signature on {}: {}", self.path_str(), e);
                warn!("Partition {} has bad signature, marking STATUS_BAD_SIG", self.path_str());
                self.write_status(STATUS_BAD_SIG)?;
            },
            Ok(false) => {
                warn!("Partition {} has bad signature, marking STATUS_BAD_SIG", self.path_str());
                self.write_status(STATUS_BAD_SIG)?;
            },
            Ok(true) => { /* signature good */ },
        };
        Ok(())
    }
}



/// Set on partition before writing a new rootfs disk image
const STATUS_INVALID : u8 = 0;

/// Set on partition after write of new rootfs disk image completes successfully
const STATUS_NEW     : u8 = 1;

/// Set on boot selected partition if in `STATUS_NEW` state.
const STATUS_TRY_BOOT: u8 = 2;

/// Set on boot when a `STATUS_TRY_BOOT` partition successfully launches desktop
const STATUS_GOOD    : u8 = 3;

/// Set on boot for any partition in state `STATUS_TRY_BOOT`
const STATUS_FAILED  : u8 = 4;

/// Set on boot selected partition when signature fails to verify
const STATUS_BAD_SIG : u8 = 5;

/// Set on boot selected partition when Metainfo cannot be parsed from Partition Info Block
const STATUS_BAD_META: u8 = 6;

const CODE_TO_LABEL: [&str; 7] = ["Invalid", "New", "Try Boot", "Good", "Failed Boot", "Bad Signature", "Bad Metainfo"];

fn is_valid_status_code(code: u8) -> bool {
    code <= STATUS_BAD_META 
}

fn status_code_label(code: u8) -> String {
    if is_valid_status_code(code) {
        CODE_TO_LABEL[code as usize].to_string()
    } else {
        format!("Invalid status code: {}", code)
    }
}

