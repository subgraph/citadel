use std::path::Path;
use std::fs;
use std::os::unix::ffi::OsStrExt;
use std::os::unix::fs::MetadataExt;
use std::ffi::CString;
use std::io::{self,Write};

use libc;
use walkdir::WalkDir;

use Result;


pub fn path_filename(path: &Path) -> &str {
    if let Some(osstr) = path.file_name() {
        if let Some(name) = osstr.to_str() {
            return name;
        }
    }
    ""
}

fn is_alphanum_or_dash(c: char) -> bool {
    is_ascii(c) && (c.is_alphanumeric() || c == '-')
}

fn is_ascii(c: char) -> bool {
    c as u32 <= 0x7F
}

pub fn is_first_char_alphabetic(s: &str) -> bool {
    if let Some(c) = s.chars().next() {
        return is_ascii(c) && c.is_alphabetic()
    }
    false
}

const MAX_REALM_NAME_LEN:usize = 128;

/// Valid realm names:
///   * must start with an alphabetic ascii letter character
///   * may only contain ascii characters which are letters, numbers, or the dash '-' symbol 
///   * must not be empty or have a length exceeding 128 characters
pub fn is_valid_realm_name(name: &str) -> bool {
    name.len() <= MAX_REALM_NAME_LEN && 
        // Also false on empty string
        is_first_char_alphabetic(name) && 
        name.chars().all(is_alphanum_or_dash)
}

pub fn mkdir_chown(path: &Path, uid: u32, gid: u32) -> io::Result<()> {
    if !path.exists() {
        fs::create_dir(path)?;
    }
    let cstr = CString::new(path.as_os_str().as_bytes())?;
    unsafe {
        if libc::chown(cstr.as_ptr(), uid, gid) == -1 {
            return Err(io::Error::last_os_error());
        }
    }
    Ok(())
}

pub fn copy_tree(from_base: &Path, to_base: &Path) -> Result<()> {
    for entry in WalkDir::new(from_base) {
        let path = entry?.path().to_owned();
        let to = to_base.join(path.strip_prefix(from_base)?);
        if path.is_dir() {
            let meta = path.metadata()?;
            mkdir_chown(&to, meta.uid(), meta.gid())?;
        } else {
            fs::copy(&path, &to)
                .map_err(|e| format_err!("failed to copy {} to {}: {}", path.display(), to.display(), e))?;
        }
    }
    Ok(())
}

use termcolor::{ColorChoice,Color,ColorSpec,WriteColor,StandardStream};

pub struct ColoredOutput {
    color_bright: ColorSpec,
    color_bold: ColorSpec,
    color_dim: ColorSpec,
    stream: StandardStream,
}


impl ColoredOutput {
    pub fn new() -> ColoredOutput {
        ColoredOutput::new_with_colors(Color::Rgb(0, 110, 180), Color::Rgb(100, 100, 80))
    }

    pub fn new_with_colors(bright: Color, dim: Color) -> ColoredOutput {
        let mut out = ColoredOutput {
            color_bright: ColorSpec::new(),
            color_bold: ColorSpec::new(),
            color_dim: ColorSpec::new(),
            stream: StandardStream::stdout(ColorChoice::AlwaysAnsi),
        };
        out.color_bright.set_fg(Some(bright.clone()));
        out.color_bold.set_fg(Some(bright)).set_bold(true);
        out.color_dim.set_fg(Some(dim));

        out
    }

    pub fn write(&mut self, s: &str) -> &mut Self {
        write!(&mut self.stream, "{}", s).unwrap();
        self.stream.reset().unwrap();
        self
    }
    pub fn bright(&mut self, s: &str) -> &mut Self {
        self.stream.set_color(&self.color_bright).unwrap();
        self.write(s)
    }
    pub fn bold(&mut self, s: &str) -> &mut Self {
        self.stream.set_color(&self.color_bold).unwrap();
        self.write(s)
    }
    pub fn dim(&mut self, s: &str) -> &mut Self {
        self.stream.set_color(&self.color_dim).unwrap();
        self.write(s)
    }

}
