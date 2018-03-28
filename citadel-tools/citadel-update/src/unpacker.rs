
use std::path::{Path,PathBuf};
use std::fs::{self,File};
use std::io::{self,Read};

use toml;
use ed25519_dalek::SIGNATURE_LENGTH;

use Result;
use Config;
use Metainfo;
use Partition;
use util::*;
use MAX_METAINFO_LEN;

pub struct UpdateImageUnpacker {
    path: PathBuf,
    workdir: Workdir,
    metainfo: Metainfo,
    metainfo_bytes: Vec<u8>,
    signature_bytes: Vec<u8>,
    header_len: usize,
}

impl UpdateImageUnpacker {
    pub fn open<P: AsRef<Path>>(path: P, config: &Config) -> Result<UpdateImageUnpacker> {
        let mut f = File::open(path.as_ref())?;

        UpdateImageUnpacker::read_magic(&mut f)?;
        let metainfo_bytes = UpdateImageUnpacker::read_metainfo(&mut f)?;
        let metainfo = toml::from_slice::<Metainfo>(&metainfo_bytes)?;

        let mut signature_bytes = vec![0; SIGNATURE_LENGTH];
        f.read_exact(&mut signature_bytes)?;

        let channel = match config.channel(metainfo.channel()) {
            Some(ch) => ch,
            None => bail!("Channel '{}' not found in configuration", metainfo.channel()),
        };

        if !channel.verify(metainfo_bytes.as_slice(), &signature_bytes)? {
            bail!("Signature verification failed");
        }

        
        let mut workdir = Workdir::new(config.citadel_updates_base(), metainfo.channel());
        workdir.set_version(metainfo.version() as usize)?;

        let mlen = metainfo_bytes.len();

        Ok(UpdateImageUnpacker {
            path: PathBuf::from(path.as_ref()),
            workdir, metainfo, metainfo_bytes,
            signature_bytes,

            header_len: 6 + SIGNATURE_LENGTH + mlen,
        })
    }

    fn read_magic(r: &mut File) -> Result<()> {
        let mut buf = [0u8; 4];
        r.read_exact(&mut buf)?;

        if &buf != b"UPDT" {
            bail!("not an update image, bad magic value");
        }
        Ok(())
    }
    fn read_metainfo(r: &mut File) -> Result<Vec<u8>> {
        let mut lenbuf = [0u8; 2];
        r.read_exact(&mut lenbuf)?;
        let len = (lenbuf[0] as usize) << 8 | (lenbuf[1] as usize);
        if len == 0 || len > MAX_METAINFO_LEN {
            bail!("metainfo length field has invalid value: {}", len);
        }
        let mut bytes = vec![0u8; len];
        r.read_exact(bytes.as_mut_slice())?;
        Ok(bytes)
    }

    pub fn metainfo(&self) -> &Metainfo {
        &self.metainfo
    }

    pub fn unpack_disk_image(&self) -> Result<()> {
        let mut from = File::open(&self.path)?;
        info!("{} -> {}", path_str(&self.path), path_str(&self.workdir.filepath("citadel-image.ext2.xz")));
        let mut to = File::create(&self.workdir.filepath("citadel-image.ext2.xz"))?;
        let mut discard = vec![0u8; self.header_len];
        from.read_exact(&mut discard)?;
        io::copy(&mut from, &mut to)?;
        Ok(())
    }

    pub fn decompress_disk_image(&self) -> Result<()> {
        let output = self.workdir.filepath("citadel-image.ext2");
        if output.exists() {
            fs::remove_file(&output)?;
        }
        run_xz_command(&self.workdir.filepath("citadel-image.ext2.xz"), true)?;

        let file_sz = fs::metadata(&output)?.len() as usize;
        let meta_sz = self.metainfo.nsectors() * 512;
        if file_sz != meta_sz {
            bail!("Uncompressed images size {} does not match size declared in metainfo {}", file_sz, meta_sz);
        }
        Ok(())
    }

    pub fn verify_shasum(&self) -> Result<()> {
        let path = self.workdir.filepath("citadel-image.ext2");
        let shasum = run_sha256_command(&path)?;
        if shasum != self.metainfo.shasum() {
            let mut bad = path.clone();
            bad.pop(); bad.push("citadel-image.ext2.badsum");
            fs::rename(&path, &bad)?;
            bail!("Failed sha256 sum of {}: {}", path_str(&path), shasum);
        }
        info!("GOOD: {}", shasum);
        Ok(())
    }

    pub fn write_partition(&self, part: &Partition) -> Result<()> {
        let path = self.workdir.filepath("citadel-image.ext2");
        part.write_image(&path, &self.metainfo_bytes, &self.signature_bytes)?;
        Ok(())
    }

}
