use std::path::Path;
use std::fs::File;
use std::io::Read;
use toml;
use Result;

fn default_true() -> bool {
    true
}

fn default_zone() -> String {
    "clear".to_owned()
}

#[derive (Deserialize,Clone)]
pub struct RealmConfig {
    #[serde(default = "default_true", rename="add-shared-dir")]
    add_shared_dir: bool,

    #[serde(default, rename="use-ephemeral-home")]
    use_ephemeral_home: bool,

    #[serde(default = "default_true", rename="use-sound")]
    use_sound: bool,

    #[serde(default = "default_true", rename="use-x11")]
    use_x11: bool,

    #[serde(default = "default_true", rename="use-wayland")]
    use_wayland: bool,

    #[serde(default, rename="use-kvm")]
    use_kvm: bool,

    #[serde(default,rename="use-gpu")]
    use_gpu: bool,

    #[serde(default = "default_true", rename="use-network")]
    use_network: bool,

    #[serde(default = "default_zone", rename="network-zone")]
    network_zone: String,
}

impl RealmConfig {
    pub fn load_or_default(path: &Path) -> Result<RealmConfig> {
        if path.exists() {
            let s = load_as_string(&path)?;
            let config = toml::from_str::<RealmConfig>(&s)?;
            Ok(config)
        } else {
            Ok(RealmConfig::default())
        }
    }

    pub fn default() -> RealmConfig {
        RealmConfig {
            add_shared_dir: true,
            use_ephemeral_home: false,
            use_sound: true,
            use_x11: true,
            use_wayland: true,
            use_kvm: false,
            use_gpu: false,
            use_network: true,
            network_zone: default_zone(),
        }
    }

    pub fn kvm(&self) -> bool {
        self.use_kvm
    }

    pub fn gpu(&self) -> bool {
        self.use_gpu
    }

    pub fn shared_dir(&self) -> bool {
        self.add_shared_dir
    }

    pub fn emphemeral_home(&self) -> bool {
        self.use_ephemeral_home
    }

    pub fn sound(&self) -> bool {
        self.use_sound
    }

    pub fn x11(&self) -> bool {
        self.use_x11
    }

    pub fn wayland(&self) -> bool {
        self.use_wayland
    }

    pub fn network(&self) -> bool {
        self.use_network
    }

    pub fn network_zone(&self) -> &str {
        &self.network_zone
    }
}

fn load_as_string(path: &Path) -> Result<String> {
    let mut f = File::open(path)?;
    let mut buffer = String::new();
    f.read_to_string(&mut buffer)?;
    Ok(buffer)
}
