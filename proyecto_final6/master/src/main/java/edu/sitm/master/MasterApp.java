package edu.sitm.master;

import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Util;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Identity;

/**
 * MasterApp: esqueleto de la orquestación.
 * Uso: java -jar master.jar {{MASTER_ENDPOINT}} {{CSV_PATH}} {{OUTPUT_PATH}} {{NUM_PARTITIONS}} {{WORKER_ENDPOINT_1}},{{WORKER_ENDPOINT_2}}
 *
 * Responsabilidades (esqueleto):
 * - Contar líneas del CSV sin cargar todo en memoria
 * - Particionar en {{NUM_PARTITIONS}} rangos de líneas
 * - Enviar particiones a Workers (Orchestrator) usando endpoints proporcionados
 * - Recibir reportes parciales y unificarlos (MasterServiceImpl)
 */
public class MasterApp {
    public static void main(String[] args) throws Exception {
        if (args.length < 5) {
            System.err.println("Usage: java -jar master.jar {{MASTER_ENDPOINT}} {{CSV_PATH}} {{OUTPUT_PATH}} {{NUM_PARTITIONS}} {{WORKER_ENDPOINTS(comma)}}");
            System.exit(1);
        }

        final String masterEndpoint = args[0];
        final String csvPath = args[1];
        final String outputPath = args[2];
        final int numPartitions = Integer.parseInt(args[3]);
        final String workerEndpointsArg = args[4];

        String[] workerEndpoints = workerEndpointsArg.split(",");

        System.out.println("Master endpoint: " + masterEndpoint);
        System.out.println("CSV path: " + csvPath);
        System.out.println("Output path: " + outputPath);
        System.out.println("Num partitions: " + numPartitions);

        Partitioner partitioner = new Partitioner();
        List<int[]> ranges = partitioner.partitionRanges(csvPath, numPartitions);
        System.out.println("Computed " + ranges.size() + " partitions.");

        MasterServiceImpl masterService = new MasterServiceImpl(outputPath);

        // Initialize ICE communicator and expose MasterService servant
        Communicator communicator = Util.initialize();
        ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("MasterAdapter", parseEndpoints(masterEndpoint));
        MasterServiceI servant = new MasterServiceI(masterService);
        Identity id = Util.stringToIdentity(parseIdentity(masterEndpoint, "MasterService"));
        adapter.add(servant, id);
        adapter.activate();

        Orchestrator orchestrator = new Orchestrator(masterService, communicator, workerEndpoints);

        // Dispatch partitions round-robin
        int i = 0;
        for (int[] r : ranges) {
            int partitionId = i;
            String csvLocalPath = csvPath; // assume workers will have local copy in same path
            orchestrator.dispatchPartition(partitionId, csvLocalPath, r[0], r[1]);
            i++;
        }

        // Nota: en una implementación completa esperaríamos hasta recibir todos los resultados.
        // Aquí, como esqueleto, imprimimos instrucciones y terminamos.
        String ts = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        System.out.println("Master started at " + ts + ". Results will be saved to " + Paths.get(outputPath).toAbsolutePath());
        System.out.println("When all workers report, MasterServiceImpl.writeFinalOutputs() should be invoked to persist results.");

        System.out.println("Master ICE adapter active and ready to receive reports.");
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
