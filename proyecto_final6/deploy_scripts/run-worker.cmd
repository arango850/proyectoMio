@echo off
setlocal














endlocaljava -jar ..\worker\target\worker-0.1.0-SNAPSHOT-shaded.jar "%WORKER_ENDPOINT%" "%CSV_PATH%" "%OUTBOX_DIR%" "%MASTER_ENDPOINT%"echo Master endpoint: %MASTER_ENDPOINT%echo Worker endpoint: %WORKER_ENDPOINT%echo Starting Worker on %WORKER_HOST%
:infoset MASTER_ENDPOINT=MasterService:tcp -h %MASTER_HOST% -p 20000set WORKER_ENDPOINT=WorkerService:tcp -h %WORKER_HOST% -p 20001
:: Master endpoint
:: Worker endpoint uses port 20001 by conventionif "%4"=="" (set MASTER_HOST=x104m01) else set MASTER_HOST=%4if "%3"=="" (set OUTBOX_DIR=outbox) else set OUTBOX_DIR=%3if "%2"=="" (set CSV_PATH=datagrams4history.csv) else set CSV_PATH=%2if "%1"=="" (set WORKER_HOST=x104m02) else set WORKER_HOST=%1:: run-worker.cmd [WORKER_HOST] [CSV_PATH] [OUTBOX_DIR] [MASTER_HOST]