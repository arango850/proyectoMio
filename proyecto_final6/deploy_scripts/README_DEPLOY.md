Despliegue rápido — Master / Workers / Streaming (Windows)

Resumen
- Generé tres scripts Windows en `deploy_scripts/` para facilitar ejecutar Master, Worker y Streaming en las máquinas destino.
- Los scripts asumen que las JARs som colocadas en los subdirectorios `master\target`, `worker\target`, `streaming\target` relativos al directorio conteniendo los scripts (normalmente el repositorio raíz).

Archivos generados
- `deploy_scripts\run-master.cmd` — inicia Master en el host dado (por defecto `x104m01`).
- `deploy_scripts\run-worker.cmd` — inicia un Worker en el host dado (por defecto `x104m02`).
- `deploy_scripts\run-streaming.cmd` — inicia la aplicación de streaming que envía datagramas a los workers.

Uso (pasos de despliegue)
1) Copiar las JARs a cada host y el script correspondiente. Por ejemplo en `C:\sitm` en cada máquina:
   - `C:\sitm\master\target\master-0.1.0-SNAPSHOT-shaded.jar`
   - `C:\sitm\worker\target\worker-0.1.0-SNAPSHOT-shaded.jar`
   - `C:\sitm\streaming\target\streaming-0.1.0-SNAPSHOT-shaded.jar`
   - `C:\sitm\deploy_scripts\run-*.cmd`

2) En cada worker (x104m02..x104m30), ejecutar (ejemplo en CMD):
```
cd C:\sitm\deploy_scripts
run-worker.cmd x104m02 C:\sitm\datagrams4history.csv C:\sitm\outbox x104m01
```
- Parámetros: `run-worker.cmd [WORKER_HOST] [CSV_PATH] [OUTBOX_DIR] [MASTER_HOST]`.
- Si no indica `CSV_PATH`, el script usa `datagrams4history.csv` en el directorio donde se ejecute.

3) Iniciar Master en `x104m01`:
```
cd C:\sitm\deploy_scripts
run-master.cmd x104m01 C:\sitm\datagrams4history.csv C:\sitm\master_output.csv 30
```
- El script usa por defecto los workers `x104m02`..`x104m30` en el puerto `20001`.
- Parámetros: `run-master.cmd [MASTER_HOST] [CSV_PATH] [OUTPUT_PATH] [NUM_PARTITIONS] [WORKER_ENDPOINTS_COMMA_SEP]`.

4) Iniciar el cliente de streaming (puede ejecutarse desde cualquier host con conectividad a los workers):
```
cd C:\sitm\deploy_scripts
run-streaming.cmd streamer01
```
- Parámetros: `run-streaming.cmd [STREAM_HOST] [WORKER_ENDPOINTS_COMMA_SEP]`.

Notas operativas
- Puertos por convención: Master `20000`, Worker `20001`, Streaming `30000`.
- Los endpoints se pasan en formato Slice proxy textual, por ejemplo: `WorkerService:tcp -h x104m02 -p 20001`.

FALLBACK: Si ejecutar las JARs en una máquina produce un error sobre JavaFX
(en mi entorno local obtuve "faltan los componentes de JavaFX runtime"), use la alternativa siguiente instalando OpenJFX.

Instalar OpenJFX (Java 11)
1) Descargar OpenJFX 11 (o la versión coincidente con su JDK) desde Gluon:
   https://gluonhq.com/products/javafx/
2) Extraer, por ejemplo a `C:\javafx-sdk-11`.
3) Ejecutar la JAR con `--module-path` y `--add-modules`. Ejemplo:
```
java --module-path "C:\javafx-sdk-11\lib" --add-modules javafx.controls,javafx.fxml -jar master\target\master-0.1.0-SNAPSHOT-shaded.jar "MasterService:tcp -h 127.0.0.1 -p 20000" "datagrams4history.csv" "master_output.csv" 4 "WorkerService:tcp -h 127.0.0.1 -p 20001"
```
- Ajuste la ruta a `javafx-sdk-11` según donde la haya descomprimido.

Automatización de despliegue (PowerShell)

Incluí dos scripts para automatizar copia y despliegue desde tu máquina:

- `deploy_scripts\\deploy_psremoting.ps1` (recomendado): usa PowerShell Remoting (WinRM) y `Copy-Item -ToSession`. Pide credenciales y realiza:
   - copia `deploy_package.zip` a `C:\\sitm` (por defecto) en cada host
   - descomprime el ZIP en `C:\\sitm`
   - opcionalmente arranca el script `deploy_scripts\\run-worker-javafx-fixed.cmd` en cada host en background (usa `Start-Process` remoto)

- `deploy_scripts\\deploy_pscp_plink.ps1` (alternativa): usa `pscp`/`plink` (PuTTY). Útil si no tienes PowerShell Remoting habilitado. Requiere `pscp.exe` y `plink.exe` en `PATH`.

Ejemplo rápido (PowerShell Remoting, desde el repo root):
```powershell
$hosts = @('x104m02','x104m03','x104m04','x104m05','x104m06','x104m07','x104m08','x104m09','x104m10','x104m11','x104m12','x104m13','x104m14','x104m15','x104m16','x104m17','x104m18','x104m19','x104m20','x104m21','x104m22','x104m23','x104m24','x104m25','x104m26','x104m27','x104m28','x104m29','x104m30')


```

Si prefieres `pscp`/`plink` (con user/password o con clave):
```powershell
$hosts = @('x104m02','x104m03',...)
#.\\deploy_scripts\\deploy_pscp_plink.ps1 -Hosts $hosts -User myuser -ZipPath ..\\deploy_package.zip -RemotePath C:\\sitm -StartWorkers
```

Notas de seguridad y pre-requisitos
- PowerShell Remoting debe estar habilitado en los hosts (`Enable-PSRemoting -Force`) y la cuenta usada debe tener permisos remotos.
- `pscp/plink` requiere llave SSH o contraseña; `-batch` evita prompts interactivos si dispone de llave.
- Los scripts intentar iniciar los workers en background y dejar un `worker_start.log` en `C:\\sitm` por host.

Si quieres que ejecute el deploy localmente desde aquí necesito credenciales (no es posible por seguridad). En lugar de eso puedo:
- Generar un único PowerShell que llame a `deploy_psremoting.ps1` con la lista fija de hosts `x104m02`..`x104m30` para que lo pegues y lances en tu máquina, o
- Generar comandos `pscp`/`plink` en un `.bat` para que los ejecutes desde tu equipo (te solicitarán key/password cuando deban).

