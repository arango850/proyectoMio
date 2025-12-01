@echo off
@echo off
setlocal

:: run-streaming-javafx.cmd [STREAM_HOST] [WORKER_ENDPOINTS_COMMA_SEP]
if "%1"=="" (set STREAM_HOST=streamer01) else set STREAM_HOST=%1
if "%2"=="" (
	set WORKER_ENDPOINTS=WorkerService:tcp -h x104m02 -p 20001,WorkerService:tcp -h x104m03 -p 20001,WorkerService:tcp -h x104m04 -p 20001,WorkerService:tcp -h x104m05 -p 20001,WorkerService:tcp -h x104m06 -p 20001,WorkerService:tcp -h x104m07 -p 20001,WorkerService:tcp -h x104m08 -p 20001,WorkerService:tcp -h x104m09 -p 20001,WorkerService:tcp -h x104m10 -p 20001,WorkerService:tcp -h x104m11 -p 20001,WorkerService:tcp -h x104m12 -p 20001,WorkerService:tcp -h x104m13 -p 20001,WorkerService:tcp -h x104m14 -p 20001,WorkerService:tcp -h x104m15 -p 20001,WorkerService:tcp -h x104m16 -p 20001,WorkerService:tcp -h x104m17 -p 20001,WorkerService:tcp -h x104m18 -p 20001,WorkerService:tcp -h x104m19 -p 20001,WorkerService:tcp -h x104m20 -p 20001,WorkerService:tcp -h x104m21 -p 20001,WorkerService:tcp -h x104m22 -p 20001,WorkerService:tcp -h x104m23 -p 20001,WorkerService:tcp -h x104m24 -p 20001,WorkerService:tcp -h x104m25 -p 20001,WorkerService:tcp -h x104m26 -p 20001,WorkerService:tcp -h x104m27 -p 20001,WorkerService:tcp -h x104m28 -p 20001,WorkerService:tcp -h x104m29 -p 20001,WorkerService:tcp -h x104m30 -p 20001
) else set WORKER_ENDPOINTS=%2

:: Default JavaFX lib path (relative to script)
if "%JAVAFX_LIB%"=="" set "JAVAFX_LIB=%~dp0..\javafx-sdk-25.0.1\lib"

:: Streaming client endpoint (unused by client but kept for parity)
set STREAMING_ENDPOINT=StreamingClient:tcp -h %STREAM_HOST% -p 30000

echo Starting Streaming client on %STREAM_HOST%
echo Worker endpoints: %WORKER_ENDPOINTS%
java --module-path "%JAVAFX_LIB%" --add-modules javafx.controls,javafx.fxml -jar "..\streaming\target\streaming-0.1.0-SNAPSHOT-shaded.jar" "%STREAMING_ENDPOINT%" "%WORKER_ENDPOINTS%"
endlocal