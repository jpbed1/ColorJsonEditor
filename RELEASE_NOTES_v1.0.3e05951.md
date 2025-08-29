# Release v1.0.3e05951 - Persistent Directory & About Dialog

## 🎉 What's New

This release introduces significant improvements to the user experience and adds professional features to make ColorJsonEditor more user-friendly and maintainable.

## ✨ New Features

### 🔄 Persistent Directory
- **File choosers now remember the last directory used** across all file operations
- **Improved workflow** - no more navigating to the same directory repeatedly
- **Works with all file operations**: Open, Save, Save As, and User Palette operations
- **Automatic initialization** to user's home directory on first run

### ℹ️ About Dialog
- **Professional About dialog** accessible via Help → About…
- **Git integration** - version automatically pulled from current commit hash
- **Author information** and GitHub repository link
- **Fallback version** (1.0.0) if Git is not available

### 📚 Enhanced Documentation
- **Comprehensive README** with professional formatting
- **Feature highlights** and usage guides
- **Technical documentation** and build instructions
- **Cross-platform compatibility** information

## 🔧 Technical Improvements

### Version Management
- **Git-based versioning** using commit hashes
- **Automatic version detection** at runtime
- **Consistent version format**: 1.0.[commit-hash]

### User Experience
- **Better error handling** and validation
- **Improved file operation workflow**
- **Professional application appearance**

## 📦 Files Changed

- `ColorJsonEditor.java` - Added persistent directory and About dialog functionality
- `README.md` - Complete rewrite with professional documentation
- Various `.class` files - Compiled application components

## 🚀 Installation

### Prerequisites
- Java 8 or higher
- Git (for version information, optional)

### Quick Start
```bash
# Download the JAR file from releases
java -jar ColorJsonEditor.jar

# Or build from source
javac --release 8 -encoding UTF-8 ColorJsonEditor.java
jar cfe ColorJsonEditor.jar ColorJsonEditor ColorJsonEditor*.class
```

## 🎯 Usage Highlights

### Persistent Directory
- Open any file → directory is remembered
- Save/Save As → uses the remembered directory
- User Palette operations → also use the persistent directory

### About Dialog
- Access via **Help → About…** in the menu bar
- Shows current version with Git commit hash
- Displays author and repository information

## 🔄 Migration from Previous Versions

This release is **fully backward compatible**. Existing user palettes and JSON files will work without any changes.

## 🐛 Bug Fixes

- Improved file chooser behavior
- Better error handling for file operations
- Enhanced user feedback for operations

## 📈 Performance

- No performance impact from new features
- Efficient directory persistence implementation
- Fast version detection with Git integration

## 🤝 Contributing

Feel free to submit issues, feature requests, or pull requests to improve the application.

## 📝 License

This project is licensed under the MIT License.

---

**Author**: jpbed  
**Repository**: https://github.com/jpbed/ColorJsonEditor  
**Version**: v1.0.3e05951  
**Release Date**: August 29, 2025
