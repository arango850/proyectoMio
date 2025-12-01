streaming module

Este módulo simula un productor de datagramas GPS en tiempo real y proporciona
un esqueleto para enviar datagramas a los Workers usando ZeroC ICE.

Uso esperado (final):

java -jar streaming.jar {{STREAM_ENDPOINT}} {{MASTER_ENDPOINT}}

Contenido actual:
- `StreamingApp.java` - app principal que arranca el productor periódico.
- `DatagramProducer.java` - genera datagramas simulados y los envía al cliente ICE.
- `IceClientStreaming.java` - cliente ICE esqueleto; aquí se implementarán proxies y reintentos.

Notas:
- Para que la comunicación ICE funcione, ejecuta `mvn -Dslice2java.exec=slice2java package` desde la raíz para generar los stubs desde `common/src/main/slice/sitm.ice`.
- Implementar selección de Workers (por arcId hashing o round-robin) y hacer la llamada `processDatagram` hacia `WorkerService`.

Siguiente paso sugerido:
- Preparar la integración con los stubs Slice generados y añadir lógica de ventanas temporales en Workers para cálculos de promedios móviles (5 minutos).
