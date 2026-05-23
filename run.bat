@echo off
:: ============================================================
::  run.bat  —  Launches the Matrix Computation application
::
::  Prerequisites:
::    • JDK 17+ on PATH
::    • compile.bat has been run successfully
::    • mysql-connector-j-*.jar in lib\
::    • MySQL server running with matrix_db created
:: ============================================================

setlocal EnableDelayedExpansion

set "ROOT=%~dp0"
set "OUT=%ROOT%out"
set "LIB=%ROOT%lib"

:: ── Build classpath ───────────────────────────────────────────────────────────
set "CP=%OUT%"
for %%F in ("%LIB%\*.jar") do set "CP=!CP!;%%F"

:: ── Launch ────────────────────────────────────────────────────────────────────
echo.
echo  Starting Matrix Computation ^& Storage System ...
echo  Classpath: %CP%
echo.

java -cp "%CP%" matrix.Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo  [ERROR] Application exited with errors.
    pause
)
