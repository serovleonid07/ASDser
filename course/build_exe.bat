@echo off
rem Builds a real Windows program: exe\FoxAndGeese\FoxAndGeese.exe (with the fox icon, Java is bundled inside).
rem Needs JDK 17+ (jpackage must be in PATH). Run this file by double click, it takes about a minute.
cd /d "%~dp0"
where jpackage >nul 2>nul || (echo jpackage not found. Install JDK 17+ and add its bin folder to PATH. & pause & exit /b 1)
call build.bat || exit /b 1
if exist dist rmdir /s /q dist
if exist exe rmdir /s /q exe
mkdir dist
copy /Y FoxAndGeese.jar dist\ >nul
jpackage --type app-image --name FoxAndGeese --input dist --main-jar FoxAndGeese.jar --main-class Main --icon icon.ico --dest exe --app-version 1.0 || (echo jpackage error. & pause & exit /b 1)
rmdir /s /q dist
echo.
echo Done. The game is in the folder exe\FoxAndGeese. Run FoxAndGeese.exe or make a desktop shortcut to it.
pause
