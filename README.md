# JSON Color Palette Editor

A powerful and user-friendly editor for modifying color palettes in JSON files, originally designed for Bitwig Studio themes but useful for any JSON-based color configuration.

## ✨ Features

### 🎨 Core Functionality
- **Visual Color Editing**: Edit colors with a built-in color picker
- **Real-time Preview**: See color changes instantly in the preview panel
- **Hex Input Support**: Direct hex code input with validation
- **Search & Filter**: Quickly find specific color entries by name or hex value

### 💾 File Management
- **Persistent Directory**: Remembers the last directory used for file operations
- **Multiple File Formats**: Support for JSON theme files and custom palette files
- **Auto-save Favorites**: User palette automatically saves on exit and loads on startup

### 🎯 User Palette System
- **Drag & Drop**: Drag colors from favorites directly onto parameters
- **Custom Palettes**: Save and load your own color palettes
- **Alpha Channel Support**: Handles both RGB (#RRGGBB) and RGBA (#RRGGBBAA) formats
- **Smart Alpha Merging**: Preserves alpha channels when applying colors

### 🔧 Professional Features
- **About Dialog**: Version information pulled from Git commits
- **Error Handling**: Comprehensive error messages and validation
- **Cross-platform**: Works on Windows, macOS, and Linux

## 🚀 Quick Start

### Prerequisites
- Java 8 or higher
- Git (for version information)

### Building from Source
```bash
# Compile the application
javac --release 8 -encoding UTF-8 ColorJsonEditor.java

# Create the JAR file
jar cfe ColorJsonEditor.jar ColorJsonEditor ColorJsonEditor*.class
```

### Running the Application
```bash
# Run the application
java -jar ColorJsonEditor.jar

# Or open a specific JSON file
java -jar ColorJsonEditor.jar path/to/your/theme.json
```

## 📖 Usage Guide

### Opening Files
1. Click **File → Open…** or use the toolbar button
2. Select your JSON file containing color definitions
3. The application will parse and display all color entries

### Editing Colors
1. **Select a color entry** from the left panel
2. **Choose your method**:
   - Click **"Edit Color…"** to use the color picker
   - Type a hex value and click **"Apply Hex"**
   - Drag a color from the favorites panel

### Managing Favorites
- **Add to Favorites**: Select a color and click **"Add Selected"**
- **Remove from Favorites**: Select a favorite and click **"Remove"**
- **Save/Load Palettes**: Use **User Palette → Save…** or **Load…**
- **Drag & Drop**: Drag favorites onto color parameters to apply them

### File Operations
- **Save**: **File → Save** (saves to current file)
- **Save As**: **File → Save As…** (save to new location)
- **Revert**: **File → Revert** (restore original file)

## 🎨 Supported Color Formats

The editor recognizes color entries in JSON files with this pattern:
```json
{
  "colorName": "#RRGGBB",
  "colorWithAlpha": "#RRGGBBAA"
}
```

### Color Format Rules
- **RGB**: 6-digit hex codes (#RRGGBB)
- **RGBA**: 8-digit hex codes (#RRGGBBAA)
- **Alpha Preservation**: When applying colors, alpha channels are preserved based on the target format

## 🔧 Advanced Features

### Persistent Directory
- The application remembers the last directory used for file operations
- All file dialogs (Open, Save, Save As, User Palette) use this persistent directory
- Automatically initialized to your home directory on first run

### Version Information
- **About Dialog**: Access via **Help → About…**
- **Git Integration**: Version automatically pulled from current Git commit
- **Fallback**: Uses "1.0.0" if Git is not available

### User Palette Auto-save
- Favorites are automatically saved to:
  - **Windows**: `%APPDATA%\ColorJsonEditor\user-palette.palette`
  - **Other OS**: `~/.colorjsoneditor/user-palette.palette`
- Automatically loads on application startup

## 🛠️ Technical Details

### Build Requirements
- **JDK**: 9+ (compiles with `--release 8` for compatibility)
- **Encoding**: UTF-8
- **Dependencies**: Pure Java (no external libraries)

### File Formats
- **Input**: JSON files with color definitions
- **Output**: Modified JSON files
- **User Palettes**: Custom `.palette` files (JSON array format)

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Feel free to submit issues, feature requests, or pull requests to improve the application.

## 👨‍💻 Author

**jpbed** - [GitHub Profile](https://github.com/jpbed)

---

*Originally designed for Bitwig Studio themes but useful for any JSON-based color configuration.*

