use std::path::Path;
use std::collections::HashMap;

use ed25519_dalek::{Signature,PublicKey,Keypair};
use rustc_serialize::hex::FromHex;
use sha2::Sha512;
use toml;

use util::{read_file_as_string,path_str};
use Result;


const DEFAULT_CONFIG_PATH: &str = "/usr/share/citadel/citadel-rootfs.conf";
fn default_citadel_updates() -> String { "/storage/citadel-updates".to_string() }
fn default_kernel_updates() -> String { "/storage/kernel-updates".to_string() }
fn default_image_builds() -> String { "/storage/image-builds".to_string() }

#[derive(Deserialize)]
pub struct Config {
    default_channel: Option<String>,

    #[serde (default= "default_citadel_updates")]
    citadel_updates: String,

    #[serde (default = "default_kernel_updates")]
    kernel_updates: String,

    #[serde (default = "default_image_builds")]
    image_builds: String,

    channel: HashMap<String, Channel>,
}

impl Config {

    pub fn load_default() -> Result<Config> {
        Config::load(DEFAULT_CONFIG_PATH)
    }

    pub fn load<P: AsRef<Path>>(path: P) -> Result<Config> {
        let config = match Config::from_path(path.as_ref()) {
            Ok(config) => config,
            Err(e) => bail!("Failed to load config file {}: {}", path_str(path.as_ref()), e),
        };
        Ok(config)
    }

    fn from_path(path: &Path) -> Result<Config> {
        let s = read_file_as_string(path.as_ref())?;
        let mut config = toml::from_str::<Config>(&s)?;
        for (k,v) in config.channel.iter_mut() {
            v.name = k.to_string();
        }
            
        Ok(config)
    }

    pub fn get_default_channel(&self) -> Option<Channel> {
        
        if let Some(ref name) = self.default_channel {
            if let Some(c) = self.channel(name) {
                return Some(c);
            }
        }
        
        if self.channel.len() == 1 {
            return self.channel.values().next().map(|c| c.clone());
        }
        None
    }

    pub fn channel(&self, name: &str) -> Option<Channel> {
        self.channel.get(name).map(|c| c.clone() )
    }

    pub fn citadel_updates_base(&self) -> &str {
        &self.citadel_updates
    }

    pub fn kernel_updates_base(&self) -> &str {
        &self.kernel_updates
    }

    pub fn image_builds_base(&self) -> &str {
        &self.image_builds
    }

    pub fn get_private_key(&self, channel: &str) -> Option<String> {
        if let Some(channel_config) = self.channel.get(channel) {
            if let Some(ref key) = channel_config.keypair {
                return Some(key.clone());
            }
        }
        None
    }

    pub fn get_public_key(&self, channel: &str) -> Option<String> {
        if let Some(channel_config) = self.channel.get(channel) {
            return Some(channel_config.pubkey.clone());
        }
        None
    }
}

#[derive(Deserialize,Clone)]
pub struct Channel {
    update_server: Option<String>,
    pubkey: String,
    keypair: Option<String>,

    #[serde(skip)]
    name: String,
}

impl Channel {
    pub fn name(&self) -> &str {
        &self.name
    }

    pub fn sign(&self, data: &[u8]) -> Result<Signature> {
        let keybytes = match self.keypair {
            Some(ref hex) => hex.from_hex()?,
            None => bail!("No private signing key available for channel {}", self.name),
        };
        let privkey = Keypair::from_bytes(&keybytes)?;
        let sig = privkey.sign::<Sha512>(data);
        Ok(sig)
    }

    pub fn verify(&self, data: &[u8], sigbytes: &[u8]) -> Result<bool> {
        let keybytes = self.pubkey.from_hex()?;
        let pubkey = PublicKey::from_bytes(&keybytes)?;
        let sig = Signature::from_bytes(sigbytes)?;
        Ok(pubkey.verify::<Sha512>(data, &sig))
    }

}

