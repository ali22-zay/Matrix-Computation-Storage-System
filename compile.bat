@echo off
:: ============================================================
::  compile.bat  —  Compiles all Java source files
::
::  Prerequisites:
::    • JDK 17+ on PATH
::    • mysql-connector-j-*.jar placed in the lib\ folder
:: ============================================================

setlocal EnableDelayedExpansion

set "ROOT=%~dp0"
set "SRC=%ROOT%src"
set "OUT=%ROOT%out"
set "LIB=%ROOT%lib"

:: ── Find the MySQL connector jar ──────────────────────────────────────────────
set "CP=%OUT%"
for %%F in ("%LIB%\*.jar") do set "CP=!CP!;%%F"

:: ── Create output directory ───────────────────────────────────────────────────
if not exist "%OUT%" mkdir "%OUT%"

echo.
echo  [1/2] Compiling sources ...
echo  Source : %SRC%
echo  Output : %OUT%
echo  Libs   : %CP%
echo.

:: ── Collect all .java files ───────────────────────────────────────────────────
set "SOURCES="
for /R "%SRC%" %%F in (*.java) do set "SOURCES=!SOURCES! "%%F""

:: ── Run javac ─────────────────────────────────────────────────────────────────
javac -encoding UTF-8 -d "%OUT%" -cp "%CP%" !SOURCES!

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo  [ERROR] Compilation failed. Check the errors above.
    pause
    exit /b 1
)

echo.
echo  [2/2] Compilation successful!
echo  Run the application with:  run.bat
echo.
pause
