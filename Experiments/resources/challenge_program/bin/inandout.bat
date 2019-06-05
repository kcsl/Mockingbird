@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  inandout startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Add default JVM options here. You can also use JAVA_OPTS and INANDOUT_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS="-Xms512m" "-Xmx12g" "-Dfile.encoding=utf-8" "-Djava.security.egd=file:///dev/urandom" "-Djdk.tls.ephemeralDHKeySize=1024" "-Dorg.slf4j.simpleLogger.defaultLogLevel=error"

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@rem Get command-line arguments, handling Windows variants

if not "%OS%" == "Windows_NT" goto win9xME_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\inandout_1.jar;%APP_HOME%\lib\commons-lang3-3.7.jar;%APP_HOME%\lib\commons-math3-3.6.1.jar;%APP_HOME%\lib\httpclient-4.5.5.jar;%APP_HOME%\lib\commons-cli-1.4.jar;%APP_HOME%\lib\commons-io-2.6.jar;%APP_HOME%\lib\commons-fileupload-1.3.3.jar;%APP_HOME%\lib\mapdb-2.0-beta13.jar;%APP_HOME%\lib\apfloat-1.8.3.jar;%APP_HOME%\lib\protobuf-java-3.5.1.jar;%APP_HOME%\lib\netty-all-4.1.21.Final.jar;%APP_HOME%\lib\jline-2.14.5.jar;%APP_HOME%\lib\log4j-1.2.17.jar;%APP_HOME%\lib\slf4j-api-1.7.25.jar;%APP_HOME%\lib\slf4j-log4j12-1.7.25.jar;%APP_HOME%\lib\httpcore-4.4.9.jar;%APP_HOME%\lib\commons-logging-1.2.jar;%APP_HOME%\lib\commons-codec-1.10.jar

@rem Execute inandout
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %INANDOUT_OPTS%  -classpath "%CLASSPATH%" com.cyberpointllc.stac.host.StacMain %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable INANDOUT_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%INANDOUT_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
