@echo off
setlocal
















endlocaljava -jar ..\master\target\master-0.1.0-SNAPSHOT-shaded.jar "%MASTER_ENDPOINT%" "%CSV_PATH%" "%OUTPUT_PATH%" %NUM_PARTS% "%WORKER_ENDPOINTS%"echo Workers: %WORKER_ENDPOINTS%echo Master endpoint: %MASTER_ENDPOINT%echo Starting Master on %MASTER_HOST% with %NUM_PARTS% partitions
:: Run the master jar (assume jars are colocated with this script in repo root)set MASTER_ENDPOINT=MasterService:tcp -h %MASTER_HOST% -p 20000
:: Master endpoint uses port 20000 by convention) else set WORKER_ENDPOINTS=%5  set WORKER_ENDPOINTS=WorkerService:tcp -h x104m02 -p 20001,WorkerService:tcp -h x104m03 -p 20001,WorkerService:tcp -h x104m04 -p 20001,WorkerService:tcp -h x104m05 -p 20001,WorkerService:tcp -h x104m06 -p 20001,WorkerService:tcp -h x104m07 -p 20001,WorkerService:tcp -h x104m08 -p 20001,WorkerService:tcp -h x104m09 -p 20001,WorkerService:tcp -h x104m10 -p 20001,WorkerService:tcp -h x104m11 -p 20001,WorkerService:tcp -h x104m12 -p 20001,WorkerService:tcp -h x104m13 -p 20001,WorkerService:tcp -h x104m14 -p 20001,WorkerService:tcp -h x104m15 -p 20001,WorkerService:tcp -h x104m16 -p 20001,WorkerService:tcp -h x104m17 -p 20001,WorkerService:tcp -h x104m18 -p 20001,WorkerService:tcp -h x104m19 -p 20001,WorkerService:tcp -h x104m20 -p 20001,WorkerService:tcp -h x104m21 -p 20001,WorkerService:tcp -h x104m22 -p 20001,WorkerService:tcp -h x104m23 -p 20001,WorkerService:tcp -h x104m24 -p 20001,WorkerService:tcp -h x104m25 -p 20001,WorkerService:tcp -h x104m26 -p 20001,WorkerService:tcp -h x104m27 -p 20001,WorkerService:tcp -h x104m28 -p 20001,WorkerService:tcp -h x104m29 -p 20001,WorkerService:tcp -h x104m30 -p 20001if "%5"=="" (if "%4"=="" (set NUM_PARTS=30) else set NUM_PARTS=%4if "%3"=="" (set OUTPUT_PATH=master_output.csv) else set OUTPUT_PATH=%3if "%2"=="" (set CSV_PATH=datagrams4history.csv) else set CSV_PATH=%2if "%1"=="" (set MASTER_HOST=x104m01) else set MASTER_HOST=%1:: run-master.cmd [MASTER_HOST] [CSV_PATH] [OUTPUT_PATH] [NUM_PARTITIONS] [WORKER_ENDPOINTS_COMMA_SEP]