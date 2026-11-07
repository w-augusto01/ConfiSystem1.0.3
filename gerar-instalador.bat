@echo off
setlocal
cd /d "%~dp0"

echo ==========================================
echo   ConfiSystem - Geracao do instalador
echo ==========================================
echo.

where java >nul 2>nul
if errorlevel 1 (
  echo ERRO: Java nao encontrado no PATH.
  echo Instale um JDK 21 de 64 bits e tente novamente.
  pause
  exit /b 1
)

set "TEM_MVN=1"
where mvn >nul 2>nul
if errorlevel 1 set "TEM_MVN=0"

java -version
echo.
echo Verificando jpackage...
where jpackage
if errorlevel 1 (
  echo ERRO: jpackage nao encontrado. Use um JDK 21, nao apenas JRE.
  pause
  exit /b 1
)

echo.
echo [1/4] Compilando o ConfiSystem e copiando dependencias...
if "%TEM_MVN%"=="1" (
  call mvn clean package -DskipTests
  if errorlevel 1 goto erro
) else (
  echo Maven nao encontrado no PATH. Usando o build ja feito pelo IntelliJ em target\.
  if not exist "target\ConfiSystem-1.0-SNAPSHOT.jar" (
    echo ERRO: target\ConfiSystem-1.0-SNAPSHOT.jar nao existe.
    echo No IntelliJ, abra a aba Maven, Lifecycle, e rode "package" primeiro.
    goto erro
  )
)

set "APP_INPUT=target\installer-input"
set "APP_DEST=target\instalador"

echo.
echo [2/4] Preparando arquivos do aplicativo...
if exist "%APP_INPUT%" rmdir /s /q "%APP_INPUT%"
if exist "%APP_DEST%" rmdir /s /q "%APP_DEST%"
mkdir "%APP_INPUT%"
mkdir "%APP_DEST%"

copy /y "target\ConfiSystem-1.0-SNAPSHOT.jar" "%APP_INPUT%\"
if errorlevel 1 goto erro

if not exist "target\libs" (
  echo ERRO: Pasta target\libs nao foi criada pelo Maven.
  goto erro
)
xcopy "target\libs\*" "%APP_INPUT%\" /E /I /Y
if errorlevel 1 goto erro

echo.
echo [3/4] Gerando instalador Windows x64 com runtime proprio...
echo IMPORTANTE: o JDK usado aqui deve ser 64 bits e compativel com Windows.
jpackage ^
  --type exe ^
  --name ConfiSystem ^
  --app-version 1.0.5 ^
  --input "%APP_INPUT%" ^
  --main-jar "ConfiSystem-1.0-SNAPSHOT.jar" ^
  --main-class "br.com.confisystem.Launcher" ^
  --dest "%APP_DEST%" ^
  --icon "icone\logo.ico" ^
  --win-menu ^
  --win-shortcut ^
  --win-dir-chooser ^
  --java-options "-Dspring.config.additional-location=optional:file:C:/ConfiSystem/config.properties" ^
  --add-modules java.base,java.desktop,java.scripting,java.compiler,jdk.jsobject,jdk.net,java.net.http,java.sql.rowset,java.logging,java.management,java.naming,java.sql,java.xml,java.instrument,java.prefs,java.rmi,java.security.jgss,java.transaction.xa,jdk.unsupported,jdk.xml.dom,jdk.crypto.ec,jdk.localedata
if errorlevel 1 goto erro

echo.
echo [4/4] Instalador gerado:
echo %CD%\%APP_DEST%\ConfiSystem-1.0.5.exe
echo.
echo Antes de distribuir, instale e teste em outro Windows 11 x64.
pause
exit /b 0

:erro
echo.
echo FALHA durante a compilacao ou empacotamento.
echo Confira as mensagens acima. Nenhum instalador novo foi validado.
pause
exit /b 1
