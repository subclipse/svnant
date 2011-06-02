@echo off

set DEFAULT_SVNKIT_HOME=%~dp0

if "%SVNKIT_HOME%"=="" set SVNKIT_HOME=%DEFAULT_SVNKIT_HOME%

set SVNKIT_CLASSPATH= "%SVNKIT_HOME%svnkit.jar";"%SVNKIT_HOME%svnkit-cli.jar";"%SVNKIT_HOME%trilead.jar";"%SVNKIT_HOME%jna.jar";"%SVNKIT_HOME%sqljet.1.0.4.jar";"%SVNKIT_HOME%antlr-runtime-3.1.3.jar"
set SVNKIT_MAINCLASS=org.tmatesoft.svn.cli.svndumpfilter.SVNDumpFilter
set SVNKIT_OPTIONS=-Djava.util.logging.config.file="%SVNKIT_HOME%/logging.properties"

"%JAVA_HOME%\bin\java" %SVNKIT_OPTIONS% -cp %SVNKIT_CLASSPATH% %SVNKIT_MAINCLASS% %*