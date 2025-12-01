@echo off
REM Run Streaming via mvn exec (Windows cmd helper)
setlocal enableextensions enabledelayedexpansion
set "STREAM_ENDPOINT=Stream:tcp -h 127.0.0.1 -p 10002"
set "MASTER_ENDPOINT=MasterService:tcp -h 127.0.0.1 -p 10000"
set "WORKERS=Worker02:tcp -h 127.0.0.1 -p 10001"
set "ARGS=%STREAM_ENDPOINT% %MASTER_ENDPOINT% %WORKERS%"
echo Running: mvn -pl streaming -am exec:java -Dexec.mainClass=edu.sitm.streaming.StreamingApp -Dexec.args="%ARGS%"
mvn -pl streaming -am exec:java -Dexec.mainClass=edu.sitm.streaming.StreamingApp -Dexec.args="%ARGS%"
endlocal
pause
