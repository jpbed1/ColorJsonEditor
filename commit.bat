@echo off
echo Adding files to Git...
git add ColorJsonEditor.java README.md

echo Committing changes...
git commit -m "Add persistent directory and About dialog features

- Add persistent last directory between file operations
- Add About dialog with Git version integration
- Update README with comprehensive documentation
- Improve user experience with directory memory"

echo Pushing to remote...
git push origin main

echo Done!
pause

