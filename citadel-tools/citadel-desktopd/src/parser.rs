
use std::io::Read;
use std::fs::File;
use std::path::Path;
use std::collections::HashSet;
use desktop::{DesktopFile,Line};
use Result;

lazy_static! {
    // These are the keys which are copied into the translated .desktop files
    static ref KEY_WHITELIST: HashSet<&'static str> = [
    "Type", "Version", "Name", "GenericName", "NoDisplay", "Comment", "Icon", "Hidden",
    "OnlyShowIn", "NotShowIn", "Path", "Terminal", "Actions", "MimeType",
    "Categories", "Keywords", "StartupNotify", "StartupWMClass", "URL", "DocPath",
    "X-GNOME-FullName", "X-GNOME-Provides", "X-Desktop-File-Install-Version", "X-GNOME-UsesNotifications",
    "X-GNOME-DocPath", "X-Geoclue-Reason", "X-GNOME-SingleWindow", "X-GNOME-Gettext-Domain",
    "X-MultipleArgs",
    ].iter().cloned().collect();

    // These are keys which are recognized but deliberately ignored.
    static ref KEY_IGNORELIST: HashSet<&'static str> = [
    "DBusActivatable", "Implements", "TryExec", "InitialPreference", "Encoding", "X-KDE-Protocols", "X-GIO-NoFuse", "X-Gnome-Vfs-System",
    "X-GNOME-Autostart-Phase", "X-GNOME-Autostart-Notify", "X-GNOME-AutoRestart",
    "X-GNOME-Bugzilla-Bugzilla", "X-GNOME-Bugzilla-Product", "X-GNOME-Bugzilla-Component", "X-GNOME-Bugzilla-Version",
    "X-GNOME-Bugzilla-ExtraInfoScript", "X-GNOME-Bugzilla-OtherBinaries", "X-GNOME-Autostart-enabled",
    "X-AppInstall-Package", "X-KDE-SubstituteUID", "X-Ubuntu-Gettext-Domain", "X-AppInstall-Keywords",
    "X-Ayatana-Desktop-Shortcuts", "X-GNOME-Settings-Panel", "X-GNOME-WMSettingsModule", "X-GNOME-WMName",
    "X-GnomeWMSettingsLibrary",
    ].iter().cloned().collect();
}

fn is_whitelisted_key(key: &str) -> bool {
    KEY_WHITELIST.contains(key)
}

fn filename_from_path(path: &Path) -> Result<&str> {
    let filename = match path.file_name() {
        Some(name) => name,
        None => return Err(format_err!("Path {:?} has no filename component", path)),
    };
    match filename.to_str() {
        Some(s) => Ok(s),
        None => Err(format_err!("Filename has invalid utf8 encoding")),
    }
}
pub struct DesktopFileParser {
    desktop_file: DesktopFile,
    exec_prefix: String,
    seen_header: bool,
    current_action: Option<String>,
    in_ignored_group: bool,
    known_actions: HashSet<String>,
}


impl DesktopFileParser {
    fn new(filename: &str, exec_prefix: &str) -> DesktopFileParser {
        DesktopFileParser {
            desktop_file: DesktopFile::new(filename),
            exec_prefix: exec_prefix.to_string(),
            seen_header: false,
            current_action: None,
            in_ignored_group: false,
            known_actions: HashSet::new(),
        }
    }

    pub fn parse_from_path<P: AsRef<Path>>(path: P, exec_prefix: &str) -> Result<DesktopFile> {
        let filename = filename_from_path(path.as_ref())?;
        let f = File::open(path.as_ref())?;
        DesktopFileParser::parse_from_reader(f, filename, exec_prefix)
    }

    fn parse_from_reader<T: Read>(mut r: T, filename: &str, exec_prefix: &str) -> Result<DesktopFile> {
        let mut buffer = String::new();
        r.read_to_string(&mut buffer)?;
        DesktopFileParser::parse_from_string(&buffer, filename, exec_prefix)
    }

    fn parse_from_string(body: &str, filename: &str, exec_prefix: &str) -> Result<DesktopFile> {
        let mut parser = DesktopFileParser::new(filename, exec_prefix);
        for s in body.lines() {
            match LineParser::parse(s) {
                Some(line) => parser.process_line(line)?,
                None => return Err(format_err!("Failed to parse line: '{}'", s))
            }
        }
        Ok(parser.desktop_file)
    }

    fn process_initial(&mut self, line: Line) -> Result<()> {
        match line {
            Line::Comment(_) | Line::Empty => {},
            Line::DesktopHeader => self.seen_header = true,
            _ => return Err(format_err!("Missing Desktop Entry header"))
        }
        self.desktop_file.add_line(line);
        Ok(())
    }

    fn process_line(&mut self, mut line: Line) -> Result<()> {
        if self.in_ignored_group && !line.is_action_header() {
            return Ok(())
        }
        if !self.seen_header {
            return self.process_initial(line)
        }

        if let Line::KeyValue(ref k, ref value) = line {
            if k == "Actions" {
                for s in value.split_terminator(";") {
                    self.known_actions.insert(s.trim().to_string());
                }
            }

        }

        match line {
            Line::ExecLine(ref mut s) => {
                s.insert_str(0,self.exec_prefix.as_str())
            },
            Line::DesktopHeader => return Err(format_err!("Duplicate Desktop Entry header")),
            Line::ActionHeader(ref action) => {
                if self.known_actions.contains(action) {
                    self.current_action = Some(action.to_string());
                    self.in_ignored_group = false;
                } else {
                    return Err(format_err!("Desktop Action header with undecleared action: {}", action))
                }
            },
            Line::GroupHeader(_) => {
                self.in_ignored_group = true;
                return Ok(())
            },
            Line::KeyLocaleValue(ref k,_,_) | Line::KeyValue(ref k,_) => {

                if !is_whitelisted_key(k) {
                    if !KEY_IGNORELIST.contains(k.as_str()) {
                        info!("Unknown key in {}: {}", self.desktop_file.filename(), k);
                    }
                    return Ok(())
                }
            }
            _ => {},
        }
        if let Some(ref action) = self.current_action {
            self.desktop_file.add_action_line(action, line)
        } else {
            self.desktop_file.add_line(line);
        }
        Ok(())
    }
}

const DESKTOP_ACTION: &'static str = "Desktop Action ";

struct LineParser<'a> {
    s: &'a str,
}

impl <'a> LineParser<'a> {
    fn new(s: &'a str) -> LineParser<'a> {
        LineParser {
            s,
        }
    }

    fn parse(s: &'a str) -> Option<Line> {
        if let Some(line) = LineParser::new(s)._parse() {
            if validate_line(&line) {
                return Some(line)
            }
        }
        None
    }

    fn first(&self) -> Option<char> {
        self.s.chars().next()
    }

    fn last(&self) -> Option<char> {
        self.s.chars().next_back()
    }

    fn _parse(&mut self) -> Option<Line> {
        match self.first() {
            None => Some(Line::Empty),
            Some('#') => Some(Line::Comment(self.s[1..].to_string())),
            Some('[') => self.parse_header(),
            Some(_) => self.parse_keyval(),
        }
    }

    fn parse_header(&mut self) -> Option<Line> {
        if self.last().unwrap() != ']' {
            return None
        }
        let content = &self.s[1..self.s.len() - 1];
        if content.starts_with(DESKTOP_ACTION) {
            let action = &content[DESKTOP_ACTION.len()..];
            return Some(Line::ActionHeader(action.to_string()))
        } else if content == "Desktop Entry" {
            return Some(Line::DesktopHeader)
        }
        return Some(Line::GroupHeader(content.to_string()))
    }

    fn parse_keyval(&self) -> Option<Line> {
        let parts: Vec<&str> = self.s.splitn(2, "=").collect();
        if parts.len() != 2 {
            return None
        }
        let key = parts[0].trim();
        let val = parts[1].trim();
        if !key.contains("[") {
            if key == "Exec" {
                return Some(Line::ExecLine(val.to_string()))
            }
            return Some(Line::KeyValue(key.to_string(), val.to_string()))
        }
        self.parse_locale(key).map(|(key,locale)| Line::KeyLocaleValue(key, locale, val.to_string()))
    }

    fn parse_locale(&self, key: &str) -> Option<(String,String)> {
        let idx = key.find("[").unwrap();
        let (k,loc) = key.split_at(idx);
        let mut chars = loc.chars();
        if let Some(']') = chars.next_back() {
            chars.next();
            if k.trim() == "Exec" {
                // Exec key with locale not allowed
                return None;
            }
            return Some((k.trim().to_string(), chars.as_str().to_string()))
        }
        None
    }
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

fn is_valid_key(key: &str) -> bool {
    if !is_first_char_alphabetic(key) {
        return false
    }
    key.chars().all(is_alphanum_or_dash)
}

fn is_valid_locale(locale: &str) -> bool {
    !locale.is_empty() && locale.chars().all(|c| {
        is_alphanum_or_dash(c) || c == '_' || c == '.' || c == '@'
    })
}

fn is_valid_value(value: &str) -> bool {
    value.chars().all(|c| {
        !(c.is_control() || c as u32 == 0 )
    })
}

fn is_valid_action(action: &str) -> bool {
    is_first_char_alphabetic(action) && action.chars().all(is_alphanum_or_dash)
}

fn is_valid_group(group: &str) -> bool {
    is_first_char_alphabetic(group) && group.chars().all(|c| {
        is_ascii(c) && !c.is_control()
    })
}

fn is_valid_exec(val: &str) -> bool {
    val.chars().all(|c| {
        is_ascii(c) && !(c.is_control() || c as u32 == 0)
    })
}

pub fn validate_line(line: &Line) -> bool {
    match *line {
        Line::ExecLine(ref s) => is_valid_exec(s),
        Line::KeyValue(ref k, ref v) => is_valid_key(k) && is_valid_value(v),
        Line::KeyLocaleValue(ref k, ref l, ref v) => is_valid_key(k) && is_valid_locale(l) && is_valid_value(v),
        Line::ActionHeader(ref action) => is_valid_action(action),
        Line::GroupHeader(ref group) => is_valid_group(group),
        _ => true,
    }
}

#[test]
fn test_parser() {
    let tests = vec!["###", "", "# hello", "[Desktop Entry]", "[Desktop Action foo]", "Foo=Bar", "Foo[hehe]=Lol"];
    for t in tests {
        println!("{:?}", LineParser::parse(t));
    }
}
