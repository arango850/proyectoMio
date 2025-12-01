package edu.sitm.streaming;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * StreamingApp: productor de datagramas en tiempo real (simulado).
 * Uso: java -jar streaming.jar {{STREAM_ENDPOINT}} {{MASTER_ENDPOINT}}
 *
 * Comportamiento:
 * - Crea un `DatagramProducer` que genera datagramas a una tasa configurable
 *   y los envía a Workers mediante `IceClientStreaming`.
 * - Patrones aplicados: Producer–Consumer (producer genera datagramas que son
 *   enviados/consumidos por Workers), Asynchronous Queuing (envío desacoplado)
 *   y Reliable Messaging (lugar para implementar reintentos/ACKs en `IceClientStreaming`).
 */
public class StreamingApp {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java -jar streaming.jar {{STREAM_ENDPOINT}} {{MASTER_ENDPOINT}}");
            System.exit(1);
        }
        final String streamEndpoint = args[0];
        final String masterEndpoint = args[1];
        final String workerEndpointsArg = args.length >= 3 ? args[2] : "";

        System.out.println("Starting Streaming node at " + streamEndpoint);
        System.out.println("Master endpoint: " + masterEndpoint);

        com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize();
        String[] workerEndpoints = workerEndpointsArg.isEmpty() ? new String[0] : workerEndpointsArg.split(",");
        IceClientStreaming iceClient = new IceClientStreaming(communicator, streamEndpoint, masterEndpoint, workerEndpoints);
        iceClient.start();

        DatagramProducer producer = new DatagramProducer(iceClient);

        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        // Ejecutar el productor a 10 mensajes por segundo (ejemplo)
        ses.scheduleAtFixedRate(producer::produceOnce, 0, 100, TimeUnit.MILLISECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down streaming node...");
            ses.shutdownNow();
            iceClient.stop();
            try { communicator.destroy(); } catch (Exception ex) { }
        }));

        Thread.currentThread().join();
    }
}
