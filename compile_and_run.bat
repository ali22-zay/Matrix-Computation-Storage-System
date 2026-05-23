@echo off
:: ============================================================
::  compile_and_run.bat  —  One-click compile + launch
:: ============================================================

setlocal
set "ROOT=%~dp0"

echo.
echo  ══════════════════════════════════════════════
echo    Matrix Computation ^& Storage System
echo    Compile + Run
echo  ══════════════════════════════════════════════
echo.

call "%ROOT%compile.bat"
if %ERRORLEVEL% NEQ 0 (
    echo  Compilation failed. Aborting.
    pause
    exit /b 1
)

call "%ROOT%run.bat"
