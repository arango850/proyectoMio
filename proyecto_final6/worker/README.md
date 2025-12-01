worker module

Este módulo contiene el esqueleto del Worker (procesamiento batch y streaming).

Uso esperado (al final):

java -jar worker.jar {{WORKER_ENDPOINT}} {{MASTER_ENDPOINT}}

Contenido actual:
- `src/main/java/edu/sitm/worker/WorkerApp.java` - app principal esqueleto.
- `PartitionProcessor.java` - procesa rangos de CSV localmente y genera `VelocityDatum`.
- `StreamingProcessor.java` - estructura inicial para ventanas de streaming.
- `IceClient.java` - cliente ICE esqueleto (donde se integrará la comunicación con Master).

Notas:
- Este esqueleto usa `common` (módulo local) para utilidades (map-matching) y DTOs de prueba.
- La integración con ZeroC ICE será añadida cuando `slice2java` haya generado los stubs Java.
- El `pom.xml` ya produce un "uber-jar" usando `maven-shade-plugin` para facilitar despliegue.

Próximos pasos:
- Implementar las clases generadas por Slice (`sitm.*`) en los puntos marcados.
- Agregar health-checks, reintentos y manejo de ACKs en `IceClient`.
- Implementar persistencia temporal si el envío al Master falla.
