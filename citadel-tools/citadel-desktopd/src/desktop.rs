use std::io::Write;
use std::fs::File;
use std::path::Path;
use std::collections::HashMap;
use Result;


pub struct DesktopFile {
    filename: String,
    lines: Vec<Line>,
    // map from key of key/value pair to index of line in lines vector
    main_map: HashMap<String, usize>,
    // map from group name to map of key/value -> index
    groups: HashMap<String,HashMap<String,usize>>,
}


impl DesktopFile {

    pub fn write_to_dir<P: AsRef<Path>>(&self, directory: P) -> Result<()> {
        let mut path = directory.as_ref().to_path_buf();
        path.push(self.filename.as_str());
        let f = File::create(&path)?;
        self.write_to(f)?;
        Ok(())
    }

    pub fn write_to<W: Write>(&self, mut w: W) -> Result<()> {
        for line in &self.lines {
            line.write_to(&mut w)?;
        }
        Ok(())
    }

    pub fn filename(&self) -> &str {
        self.filename.as_ref()
    }

    //
    // Conditions for translating a .desktop entry
    //
    //     Type=Application // Mandatory 'Type' key must be Application
    //     NotShowIn=       // If 'NotShowIn' key is present, must not contain GNOME
    //     OnlyShowIn=      // If 'OnlyShowIn' key is present, must contain 'GNOME'
    //     Terminal=false   // If 'Terminal' key is present, must be false
    //     Hidden=false     // If 'Hidden' key is present, must be false
    //
    pub fn is_showable(&self) -> bool {
        self.is_application_type() && self.show_in_gnome() &&
            !(self.needs_terminal() || self.is_hidden())
    }

    fn needs_terminal(&self) -> bool {
        self.key_exists_and_not_false("Terminal")
    }

    fn is_application_type(&self) -> bool {
        if let Some(t) = self.get_key_val("Type") {
            return t == "Application"
        }
        false
    }

    fn show_in_gnome(&self) -> bool {
        if self.key_exists("NotShowIn") && self.key_value_contains("NotShowIn", "GNOME") {
            return false;
        }
        if self.key_exists("OnlyShowIn") && !self.key_value_contains("OnlyShowIn", "GNOME") {
            return false;
        }
        true
    }

    fn key_value_contains(&self, key: &str, s: &str) -> bool {
        match self.get_key_val(key) {
            Some(val) => val.contains(s),
            None => false,
        }
    }

    fn key_exists(&self, key: &str) -> bool {
        self.main_map.contains_key(key)
    }

    fn is_hidden(&self) -> bool {
        self.key_exists_and_not_false("Hidden")
    }

    fn key_exists_and_not_false(&self, key: &str) -> bool {
        if let Some(s) = self.get_key_val(key) {
            if s != "false" {
                return true;
            }
        }
        false
    }

    fn get_key_val(&self, key: &str) -> Option<&str> {
        if let Some(idx) = self.main_map.get(key) {
            match self.lines[*idx] {
                Line::KeyValue(_, ref v) => return Some(v),
                ref line => panic!("Key lookup on '{}' returned wrong line type: {:?}", key, line),
            }
        }
        None
    }


    pub fn new(filename: &str) -> DesktopFile {
        DesktopFile {
            filename: filename.to_string(),
            lines: Vec::new(),
            main_map: HashMap::new(),
            groups: HashMap::new(),
        }
    }

    pub fn add_line(&mut self, line: Line) {
        if line.is_key_value_type() {
            let idx = self.lines.len();
            self.main_map.insert(line.get_key_string(), idx);
        }
        self.lines.push(line);
    }

    pub fn add_action_line(&mut self, action: &str, line: Line) {
        if line.is_key_value_type() {
            let idx = self.lines.len();
            let map = self.groups.entry(action.to_string()).or_insert(HashMap::new());
            map.insert(line.get_key_string(), idx);
        }
        self.lines.push(line);
    }


}


#[derive(Debug)]
pub enum Line {
    Empty,
    Comment(String),
    ExecLine(String),
    KeyValue(String,String),
    KeyLocaleValue(String,String,String),
    DesktopHeader,
    ActionHeader(String),
    GroupHeader(String)
}


impl Line {
    pub fn is_action_header(&self) -> bool {
        match *self {
            Line::ActionHeader(..) => true,
            _ => false,
        }
    }

    fn is_key_value_type(&self) -> bool {
        match *self {
            Line::KeyValue(..) | Line::KeyLocaleValue(..) => true,
            _ => false,
        }
    }

    fn get_key_string(&self) -> String {
        match *self {
            Line::KeyValue(ref k, ..) => k.to_string(),
            Line::KeyLocaleValue(ref k, ref loc, ..) => format!("{}[{}]", k, loc),
            _ => panic!("get_key_string() called on Line item which is not a key/value type"),
        }
    }

    fn write_to<W: Write>(&self, mut w: W) -> Result<()> {
        match *self {
            Line::Empty => writeln!(w)?,
            Line::Comment(ref s) => writeln!(w, "#{}", s)?,
            Line::ExecLine(ref s) => writeln!(w, "Exec={}", s)?,
            Line::KeyValue(ref k, ref v) => writeln!(w, "{}={}", k, v)?,
            Line::KeyLocaleValue(ref k, ref loc, ref v) => writeln!(w, "{}[{}]={}", k, loc, v)?,
            Line::DesktopHeader => writeln!(w, "[Desktop Entry]")?,
            Line::ActionHeader(ref action) => writeln!(w, "[Desktop Action {}]", action)?,
            Line::GroupHeader(..) => {},
        }
        Ok(())
    }
}
