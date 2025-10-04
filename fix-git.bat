@echo off
echo === FIXING GIT REPOSITORY ===

cd /d "F:\Programming\Java Projects\hotel-management-system"

echo 1. Creating backup...
xcopy "." "..\hotel-backup" /E /I /H >nul

echo 2. Removing old git...
rmdir /s /q ".git" 2>nul

echo 3. Initializing new repository...
git init

echo 4. Creating .gitignore...
(
echo # IntelliJ IDEA
echo .idea/
echo *.iml
echo # Compiled files
echo *.class
echo out/
echo target/
echo # Database
echo *.db
echo # Libraries
echo lib/
echo *.jar
) > .gitignore

echo 5. Adding files...
git add .

echo 6. Creating commit...
git commit -m "Initial commit: Hotel management system"

echo 7. Connecting to GitHub...
git remote add origin https://github.com/ваш-логин/hotel-management-system.git

echo 8. Pushing to GitHub...
git push -f origin main

echo === DONE! ===
pause