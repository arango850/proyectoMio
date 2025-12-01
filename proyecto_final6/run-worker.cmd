@echo off
REM Run Worker via mvn exec (Windows cmd helper)
setlocal enableextensions enabledelayedexpansion
set "WORKER_ENDPOINT=Worker02:tcp -h 127.0.0.1 -p 10001"
set "MASTER_ENDPOINT=MasterService:tcp -h 127.0.0.1 -p 10000"
set "ARGS=%WORKER_ENDPOINT% %MASTER_ENDPOINT%"
echo Running: mvn -pl worker -am exec:java -Dexec.mainClass=edu.sitm.worker.WorkerApp -Dexec.args="%ARGS%"
mvn -pl worker -am exec:java -Dexec.mainClass=edu.sitm.worker.WorkerApp -Dexec.args="%ARGS%"
endlocal
pause
