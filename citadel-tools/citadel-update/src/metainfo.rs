#[derive(Deserialize,Serialize,Clone)]
pub struct Metainfo {
    channel: String,
    version: u32,
    base_version: u32,
    date: String,
    gitrev: String,
    nsectors: u32,
    shasum: String,
    verity_salt: String,
    verity_root: String,
}


impl Metainfo {

    pub fn channel(&self) -> &str {
        &self.channel
    }

    pub fn version(&self) -> u32 {
        self.version
    }

    pub fn date(&self) -> &str {
        &self.date
    }

    pub fn gitrev(&self) -> &str {
        &self.gitrev
    }

    pub fn nsectors(&self) -> usize {
        self.nsectors as usize
    }

    pub fn shasum(&self) -> &str {
        &self.shasum
    }

    pub fn verity_root(&self) -> &str {
        &self.verity_root
    }

    pub fn verity_salt(&self) -> &str {
        &self.verity_salt
    }
}
