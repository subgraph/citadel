
use std::path::Path;
use std::io::{self,Write};
use std::fs::{File,OpenOptions};
use std::collections::HashMap;

use config::{Config,Channel};
use util::*;

use Result;

const IMAGE_FILENAME: &str = "citadel-image.ext2";

///
/// 
pub struct UpdateImagePacker {
    workdir: Workdir,
    version: usize,
    channel: Channel,
    nsectors: usize,
    verity_salt: String,
    verity_root: String,
    shasum: String,
    header: Vec<u8>,
}

impl UpdateImagePacker {
    pub fn new(config: &Config, channel: Channel, image_path: &str) -> Result<UpdateImagePacker> {
        let mut workdir = Workdir::new(config.image_builds_base(), channel.name());
        let version = workdir.find_next_version()?;

        sanity_check_source(image_path)?;
        let mut from = File::open(image_path)?;
        let mut to = File::create(workdir.filepath(IMAGE_FILENAME))?;
        io::copy(&mut from, &mut to)?;

        Ok(UpdateImagePacker {
            workdir, version,
            channel: channel.to_owned(),
            nsectors: 0,
            verity_salt: String::new(),
            verity_root: String::new(),
            shasum: String::new(),
            header: Vec::new(),
        })
    }

    pub fn build(&mut self) -> Result<()> {
        self.pad_image(4096)?;
        let meta = self.workdir.filepath(IMAGE_FILENAME).metadata()?;
        self.nsectors = (meta.len() / 512) as usize;
        self.build_verity()?;
        self.calculate_image_shasum()?;
        self.build_update_header()?;
        self.compress_image()?;
        self.write_update_image()?;
        
        Ok(())
    }

    fn pad_image(&self, size: usize) -> Result<()> {
        let path = self.workdir.filepath(IMAGE_FILENAME);
        let meta = path.metadata()?;
        let rem = (meta.len() as usize) % size;
        if rem == 0 {
            return Ok(());
        }
        let padlen = size - rem;
        info!("padding image with {} bytes", padlen);
        let zeros = vec![0u8; padlen];

        let mut file = OpenOptions::new().append(true).open(&path)?;
        file.write_all(&zeros)?;
        Ok(())
    }

    fn build_verity(&mut self) -> Result<()> {
        info!("Building dm-verity hash tree");
        let verity_output = run_verityformat_command(&self.workdir.filepath(IMAGE_FILENAME), &self.workdir.filepath("verifyhash.out"))?;
        write_string_to_file(&self.workdir.filepath("verityinfo"), &verity_output)?;

        let map = UpdateImagePacker::parse_verity_output(&verity_output);

        self.verity_root = match map.get("Root hash") {
            Some(v) => v.to_owned(),
            None => bail!("No root hash found in veritysetup output"),
        };

        self.verity_salt = match map.get("Salt") {
            Some(v) => v.to_owned(),
            None => bail!("No Salt found in veritysetup output"),
        };

        info!("Verity root: {}", self.verity_root);
        Ok(())
    }

    fn calculate_image_shasum(&mut self) -> Result<()> {
        info!("Calculating sha256 digest over image file");
        self.shasum = run_sha256_command(&self.workdir.filepath(IMAGE_FILENAME))?;
        Ok(())
    }

    fn parse_verity_output(output: &str) -> HashMap<String,String> {
        let mut map = HashMap::new();
        for line in output.lines() {
            if let Some((k,v)) = UpdateImagePacker::parse_verity_line(line) {
                map.insert(k, v);
            }
        }
        map
    }

    fn parse_verity_line(line: &str) -> Option<(String,String)> {
        let v = line.split(':').map(|s| s.trim()) 
            .collect::<Vec<&str>>();

        if v.len() == 2 {
            Some((v[0].to_string(), v[1].to_string()))
        } else {
            None
        }
    }


    fn build_update_header(&mut self) -> Result<()> {
        info!("Creating update image header");
        let metainfo = self.generate_metainfo()?;

        let mut f = File::create(self.workdir.filepath("metainfo"))?;
        f.write_all(&metainfo)?;

        let sig = self.channel.sign(&metainfo)?;

        let mut szbuf = [0u8; 2];
        szbuf[0] = (metainfo.len() >> 8) as u8;
        szbuf[1] = metainfo.len() as u8;

        self.header.write_all(b"UPDT")?;
        self.header.write_all(&szbuf)?;
        self.header.write_all(&metainfo)?;
        self.header.write_all(&sig.to_bytes())?;
        Ok(())
    }

    fn generate_metainfo(&self) -> Result<Vec<u8>> {
        let mut v = Vec::new();
        writeln!(v, "channel = \"{}\"", self.channel.name())?;
        writeln!(v, "version = {}", self.version)?;
        writeln!(v, "base_version = {}", self.version)?;
        writeln!(v, "date = \"\"")?;
        writeln!(v, "gitrev = \"\"")?;
        writeln!(v, "nsectors = {}", self.nsectors)?;

        writeln!(v, "shasum = \"{}\"", self.shasum)?;
        writeln!(v, "verity_salt = \"{}\"", self.verity_salt)?;
        writeln!(v, "verity_root = \"{}\"", self.verity_root)?;
        Ok(v)
    }

    fn compress_image(&self) -> Result<()> {
        info!("Compressing image file");
        run_xz_command(&self.workdir.filepath(IMAGE_FILENAME), false)?;
        Ok(())
    }

    fn write_update_image(&self) -> Result<()> {
        let img_path = self.workdir.filepath("citadel-update.img");
        info!("writing update image to {}", path_str(&img_path));
        let mut out = File::create(&img_path)?;

        out.write_all(self.header.as_slice())?;

        let mut image = File::open(self.workdir.filepath("citadel-image.ext2.xz"))?;
        io::copy(&mut image, &mut out)?;

        Ok(())
    }
}


fn sanity_check_source<P: AsRef<Path>>(src: P) -> Result<()> {
    let src: &Path = src.as_ref();
    let meta = match src.metadata() {
        Ok(md) => md,
        Err(e) => bail!("Could not load image file {}: {}", path_str(src), e),
    };

    if !meta.file_type().is_file() {
        bail!("Image file {} exists but is not a regular file");
    }

    let filetype = match run_file_command(&src) {
        Ok(s) => s,
        Err(e) => bail!("{}", e),
    };

    if filetype.starts_with("XZ") {
        bail!("Image file is compressed, decompress first");
    } else if !filetype.starts_with("Linux rev 1.0 ext2 filesystem data") {
        bail!("Image file is not an ext2 filesystem as expected:\n    {}", 
              filetype.trim_right());
    }

    if meta.len() % 512 != 0 {
        bail!("Image file size is not a multiple of sector size (512 bytes)");
    }
    Ok(())
}
