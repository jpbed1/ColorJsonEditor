# JSON Color Palette Editor

A simple UI application for editing color palettes in JSON files with an intuitive visual interface.

This was developed mainly as a local editor alternative for the JSON files created by Berikai's awesome Bitwig Theme Editor:
https://github.com/Berikai/bitwig-theme-editor


The official online color editor (which I suggest you visit and use first) is out at: 
https://bitwig.berikai.dev/


![Version](https://img.shields.io/badge/version-v1.0.daf7e7d-blue)
![Java](https://img.shields.io/badge/Java-8+-orange)
![License](https://img.shields.io/badge/license-MIT-green)

## ✨ Features

- **Visual Color Editing**: Edit colors with a color picker or hex input
- **Real-time Preview**: See color changes instantly in the preview panel
- **Smart File Management**: Persistent directory memory across file operations
- **Drag & Drop**: Drag favorite colors onto parameters for quick updates
- **User Palette System**: Save and load custom color palettes
- **Search & Filter**: Quickly find colors by name or hex value
- **Professional About Dialog**: Version information with Git integration
- **Cross-platform**: Works on Windows, macOS, and Linux
- **Java 8 Compatible**: Optimized for broad compatibility

## 🚀 Quick Start

### Prerequisites
- Java 8 or higher
- Git (for version information)

### Download & Run
1. **Download** the latest `ColorJsonEditor.jar` from [Releases](https://github.com/jpbed/ColorJsonEditor/releases)
2. **Run** the application:
   ```bash
   java -jar ColorJsonEditor.jar
   ```
3. **Optional**: Open a JSON file directly:
   ```bash
   java -jar ColorJsonEditor.jar path/to/your/colors.json
   ```

## 📖 Usage Guide

### Opening Files
- Use **File → Open…** or the **Open** button
- Supports JSON files with color entries in format: `"name": "#RRGGBB"` or `"name": "#RRGGBBAA"`
- **Persistent Directory**: File choosers remember your last used directory

### Editing Colors
1. **Select** a color from the left panel
2. **Edit** using:
   - **Color Picker**: Click "Edit Color…" for visual selection
   - **Hex Input**: Type hex values directly (e.g., `#FF5733`)
   - **Apply Hex**: Click to apply typed hex values

### User Palette (Favorites)
- **Add Colors**: Select a parameter and click "Add Selected" in the Favorites panel
- **Drag & Drop**: Drag favorites onto parameters for instant updates
- **Save/Load**: Use **User Palette → Save…** or **Load…** to manage your palettes
- **Auto-save**: Favorites are automatically saved on exit and loaded on startup

### Advanced Features
- **Search**: Use the search field to filter colors by name or hex
- **Revert**: Undo all changes with **File → Revert**
- **About Dialog**: Access via **Help → About…** for version information

## 🎨 Supported Color Formats

The editor recognizes these JSON color patterns:
```json
{
  "primary": "#FF5733",
  "secondary": "#33FF57",
  "accent": "#3357FF",
  "transparent": "#FF573380"
}
```

- **6-digit hex**: `#RRGGBB` (RGB colors)
- **8-digit hex**: `#RRGGBBAA` (RGBA colors with alpha)

## 🔧 Advanced Features

### Persistent Directory
- File choosers automatically remember the last directory used
- Works across all file operations (Open, Save As, User Palette)
- Improves workflow efficiency for repeated operations

### Version Information
- **Git Integration**: Version numbers are automatically generated from Git commit hashes
- **About Dialog**: Shows current version, author, and GitHub repository link
- **Fallback**: Gracefully handles environments without Git

### User Palette Auto-save
- **Automatic**: Favorites are saved automatically when the application closes
- **Smart Loading**: Favorites are restored on startup
- **Cross-session**: Your palette persists between application sessions

## 🛠 Technical Details

### System Requirements
- **Java**: Version 8 or higher
- **Memory**: Minimum 512MB RAM
- **Storage**: ~20KB for the application

### File Locations
- **Application**: `ColorJsonEditor.jar`
- **User Palette**: 
  - Windows: `%APPDATA%\ColorJsonEditor\user-palette.palette`
  - Other: `~/.colorjsoneditor/user-palette.palette`

### Build Instructions
```bash
# Compile (requires JDK 8+)
javac --release 8 -encoding UTF-8 ColorJsonEditor.java

# Create JAR
jar cfe ColorJsonEditor.jar ColorJsonEditor ColorJsonEditor*.class
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 👨‍💻 Author

**jpbed** - [GitHub Profile](https://github.com/jpbed)

**Repository**: [https://github.com/jpbed/ColorJsonEditor](https://github.com/jpbed/ColorJsonEditor)

---

**Latest Version**: v1.0.daf7e7d  
**Last Updated**: August 2025

