@echo off
setlocal

set SRC_DIR=C:\Users\User\Desktop\LP2-Grupo1\LP2-Grupo1\src
set BIN_DIR=C:\Users\User\Desktop\LP2-Grupo1\LP2-Grupo1\bin
set LIB=C:\Users\User\Desktop\LP2-Grupo1\LP2-Grupo1\lib

set CP=%LIB%\angus-activation-2.1.0-M1.jar;%LIB%\angus-mail-2.1.0-M1.jar;%LIB%\jakarta.activation-api-2.2.0-M1.jar;%LIB%\jakarta.mail-api-2.2.0-M1.jar

if not exist "%BIN_DIR%" mkdir "%BIN_DIR%"

echo A compilar...

javac -cp "%CP%" -d "%BIN_DIR%" -sourcepath "%SRC_DIR%" "%SRC_DIR%\Main.java"

if %ERRORLEVEL% neq 0 (
    echo.
    echo ERRO na compilacao.
    pause
    exit /b 1
)

echo A executar...
echo.

java -cp "%BIN_DIR%;%CP%" Main

echo.
pause