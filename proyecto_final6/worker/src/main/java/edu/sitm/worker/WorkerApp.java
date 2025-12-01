package edu.sitm.worker;

import edu.sitm.common.util.MapMatching;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Util;
import com.zeroc.Ice.ObjectAdapter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WorkerApp: esqueleto de la aplicación Worker.
 * Uso esperado: java -jar worker.jar {{WORKER_ENDPOINT}} {{MASTER_ENDPOINT}}
 *
 * Comentarios de patrón:
 * - Master-Worker: este proceso implementa el lado Worker.
 * - Producer-Consumer: el Worker consumirá particiones/datagramas en colas internas.
 * - Concurrency: usa thread pool para procesamiento concurrente.
 * - Reliable Messaging: el cliente ICE (IceClient) implementará reintentos y ACKs.
 */
public class WorkerApp {

    private static final int DEFAULT_THREADS = 4;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java -jar worker.jar {{WORKER_ENDPOINT}} {{MASTER_ENDPOINT}}");
            System.exit(1);
        }
        final String workerEndpoint = args[0];
        final String masterEndpoint = args[1];

        System.out.println("Starting Worker with endpoint: " + workerEndpoint);
        System.out.println("Master endpoint: " + masterEndpoint);

        // Thread pool used to process partitions and datagrams concurrently
        ExecutorService workerPool = Executors.newFixedThreadPool(DEFAULT_THREADS);

        // Inicializar ICE communicator y cliente
        Communicator communicator = Util.initialize();

        IceClient iceClient = new IceClient(communicator, masterEndpoint);
        iceClient.start();

        // Crear processors
        PartitionProcessor partitionProcessor = new PartitionProcessor(iceClient, workerPool);
        StreamingProcessor streamingProcessor = new StreamingProcessor(iceClient, workerPool);

        // Programar envío periódico de snapshots de streaming al Master
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        final String workerId = parseIdentity(workerEndpoint, "WorkerService");
        scheduler.scheduleAtFixedRate(() -> {
            try {
                java.util.List<edu.sitm.common.dto.VelocityDatum> snapshot = streamingProcessor.snapshotAndClear();
                if (snapshot != null && !snapshot.isEmpty()) {
                    iceClient.reportStreamingResult(workerId, snapshot);
                }
            } catch (Exception ex) {
                System.err.println("Failed to report streaming snapshot: " + ex.getMessage());
            }
        }, 60, 60, TimeUnit.SECONDS);

        // Crear y registrar servant WorkerServiceI
        ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("WorkerAdapter", parseEndpoints(workerEndpoint));
        WorkerServiceI servant = new WorkerServiceI(partitionProcessor, streamingProcessor);
        adapter.add(servant, Util.stringToIdentity(parseIdentity(workerEndpoint, "WorkerService")));
        adapter.activate();

        System.out.println("Worker ready. Press Ctrl+C to exit.");

        // mantener la app viva
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down worker...");
            try {
                iceClient.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            workerPool.shutdown();
            try {
                if (!workerPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    workerPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                workerPool.shutdownNow();
            }
            try {
                scheduler.shutdownNow();
            } catch (Exception ex) { }
            try {
                communicator.destroy();
            } catch (Exception ex) {
                // ignore
            }
        }));

        // bloque principal sencillamente duerme
        Thread.currentThread().join();
    }

    private static String parseEndpoints(String proxy) {
        if (proxy == null) return "";
        int idx = proxy.indexOf(':');
        if (idx < 0) return proxy;
        return proxy.substring(idx + 1);
    }

    private static String parseIdentity(String proxy, String defaultIdentity) {
        if (proxy == null) return defaultIdentity;
        int idx = proxy.indexOf(':');
        if (idx < 0) return defaultIdentity;
        return proxy.substring(0, idx);
    }
}
