@echo off
REM Run Master via mvn exec (Windows cmd helper)
setlocal enableextensions enabledelayedexpansion
set "MASTER_ENDPOINT=MasterService:tcp -h 127.0.0.1 -p 10000"
set "CSV_FULL=C:\Andrés\Sistemas 11vo semestre\Ingeniería de software IV\proyecto_final6\lines-241.csv"
set "OUT_FULL=C:\Andrés\Sistemas 11vo semestre\Ingeniería de software IV\proyecto_final6\out_master"
set "WORKERS=Worker02:tcp -h 127.0.0.1 -p 10001"
rem compute short (8.3) paths to avoid spaces in args
for %%I in ("%CSV_FULL%") do set "CSV=%%~sI"
for %%I in ("%OUT_FULL%") do set "OUT=%%~sI"
set "ARGS=%MASTER_ENDPOINT% %CSV% %OUT% 4 %WORKERS%"
echo Running: mvn -pl master -am exec:java -Dexec.mainClass=edu.sitm.master.MasterApp -Dexec.args="%ARGS%"
mvn -pl master -am exec:java -Dexec.mainClass=edu.sitm.master.MasterApp -Dexec.args="%ARGS%"
endlocal
pause
