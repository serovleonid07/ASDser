@echo off
rem Starts the game. Double click this file.
cd /d "%~dp0"
where javaw >nul 2>nul || (echo Java is not installed. Install JDK or JRE 17+ from adoptium.net and try again. & pause & exit /b 1)
start "" javaw -jar FoxAndGeese.jar
