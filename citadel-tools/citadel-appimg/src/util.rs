use std::io::Write;
use std::fs::File;
use std::path::Path;

use Result;

pub fn path_filename(path: &Path) -> &str {
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

fn is_alphanum_or_dash(c: char) -> bool {
    is_ascii(c) && (c.is_alphanumeric() || c == '-')
}

fn is_ascii(c: char) -> bool {
    c as u32 <= 0x7F
}

fn is_first_char_alphabetic(s: &str) -> bool {
    if let Some(c) = s.chars().next() {
        return is_ascii(c) && c.is_alphabetic()
    }
    false
}

pub fn is_valid_name(name: &str) -> bool {
    is_first_char_alphabetic(name) && name.chars().all(is_alphanum_or_dash)
}
