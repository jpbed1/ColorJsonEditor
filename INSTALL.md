# Installation Guide

## Quick Start

### Prerequisites
- **Java 8 or higher** installed on your system
- **Git** (optional, for version information)

### Download & Run

1. **Download** `ColorJsonEditor.jar` from the latest release
2. **Open terminal/command prompt** in the download directory
3. **Run the application**:
   ```bash
   java -jar ColorJsonEditor.jar
   ```

### Alternative: Open with File
- **Double-click** `ColorJsonEditor.jar` (if Java is properly configured)
- **Right-click** → "Open with" → Java

## System Requirements

### Windows
- Java 8+ installed
- Java added to PATH (recommended)

### macOS
- Java 8+ installed
- Can be installed via Homebrew: `brew install openjdk@11`

### Linux
- Java 8+ installed
- Can be installed via package manager:
  - Ubuntu/Debian: `sudo apt install openjdk-11-jre`
  - Fedora: `sudo dnf install java-11-openjdk`

## Verification

After running the application:
1. **Check the About dialog**: Help → About…
2. **Verify version**: Should show v1.0.3e05951
3. **Test file operations**: Try opening a JSON file

## Troubleshooting

### "java not found"
- Install Java from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://adoptium.net/)
- Ensure Java is added to your system PATH

### "Permission denied"
- Make sure the JAR file is executable
- On Linux/macOS: `chmod +x ColorJsonEditor.jar`

### Application won't start
- Check Java version: `java -version`
- Ensure you have Java 8 or higher
- Try running from command line to see error messages

## Support

- **Issues**: Create an issue on GitHub
- **Repository**: https://github.com/jpbed/ColorJsonEditor
- **License**: MIT License
