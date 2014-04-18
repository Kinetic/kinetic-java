@echo off

SET BASE_DIR=%~dp0..
SET CLASSPATH=.

for %%j in ("%BASE_DIR%\kinetic-simulator\target\*.jar") do (call :append_classpath "%%j")
goto :run

:append_classpath
set CLASSPATH=%CLASSPATH%;%1
goto :eof

:run
java -cp %CLASSPATH% com.seagate.kinetic.simulator.internal.SimulatorRunner %*
