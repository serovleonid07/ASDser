@echo off
rem Builds FoxAndGeese.jar from the sources (needs JDK 17 or newer: javac and jar in PATH).
cd /d "%~dp0"
where javac >nul 2>nul || (echo javac not found. Install JDK 17+ and add it to PATH. & pause & exit /b 1)
if exist build rmdir /s /q build
mkdir build\classes
javac --release 17 -encoding UTF-8 -d build\classes src\backend\*.java src\frontend\*.java || (echo Compile error. & pause & exit /b 1)
xcopy /E /I /Q /Y src\frontend\images build\classes\images >nul
jar --create --file FoxAndGeese.jar --main-class frontend.Main -C build\classes . || (echo jar error. & pause & exit /b 1)
rmdir /s /q build
echo Done: FoxAndGeese.jar
