package edu.sitm.worker;

import SITM.MasterServicePrx;
import SITM.VelocityDatum;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;
/**
 * IceClient: esqueleto de cliente ICE encargado de enviar resultados al Master
 * y de exponer llamadas hacia/desde otros nodos.
 *
 * Aquí no usamos directamente las clases generadas por `slice2java` porque
 * esas clases se generan en la fase build; cuando estén disponibles, este
 * cliente usará los proxies generados para invocar `MasterService.reportResult`.
 *
 * Este esqueleto ilustra dónde se implementarán reintentos, timeouts y ACKs
 * para Reliable Messaging.
 */
public class IceClient {

    private final Communicator communicator;
    private final String masterEndpoint;
    private final Path outboxDir;

    public IceClient(Communicator communicator, String masterEndpoint) {
        this.communicator = communicator;
        this.masterEndpoint = masterEndpoint;
        this.outboxDir = Paths.get("outbox");
    }

    public void start() {
        System.out.println("[IceClient] start. masterEndpoint=" + masterEndpoint + " communicator=" + communicator);
        // Ensure outbox directory exists and resend pending messages
        try {
            if (!Files.exists(outboxDir)) Files.createDirectories(outboxDir);
            resendPending();
        } catch (IOException ex) {
            System.err.println("[IceClient] failed to prepare outbox: " + ex.getMessage());
        }
    }

    public void stop() {
        System.out.println("[IceClient] stop placeholder");
    }

    public void reportPartitionResult(int partitionId, List<edu.sitm.common.dto.VelocityDatum> results) throws Exception {
        System.out.println("[IceClient] Reporting partition result (partitionId=" + partitionId + ") with " + results.size() + " items to " + masterEndpoint);

        // Build SITM.VelocityDatum[]
        java.util.List<VelocityDatum> arr = new ArrayList<>();
        for (edu.sitm.common.dto.VelocityDatum l : results) {
            VelocityDatum v = new VelocityDatum();
            v.arcId = l.getArcId();
            v.avgSpeed = l.getAvgSpeed();
            v.count = l.getCount();
            arr.add(v);
        }

        VelocityDatum[] array = arr.toArray(new VelocityDatum[0]);

        // Attempt send with retries and persist on failure
        String messageId = UUID.randomUUID().toString();
        boolean ok = sendWithRetries(partitionId, array, messageId);
        if (!ok) {
            // persist to outbox for later retry
            persistOutboxMessage(messageId, partitionId, arr);
        }
    }

    public void reportStreamingResult(String workerId, List<edu.sitm.common.dto.VelocityDatum> windowResults) throws Exception {
        System.out.println("[IceClient] Reporting streaming result from " + workerId + " items=" + windowResults.size());
        java.util.List<VelocityDatum> arr = new ArrayList<>();
        for (edu.sitm.common.dto.VelocityDatum l : windowResults) {
            VelocityDatum v = new VelocityDatum();
            v.arcId = l.getArcId();
            v.avgSpeed = l.getAvgSpeed();
            v.count = l.getCount();
            arr.add(v);
        }
        VelocityDatum[] array = arr.toArray(new VelocityDatum[0]);

        String messageId = UUID.randomUUID().toString();
        boolean ok = sendStreamingWithRetries(workerId, array, messageId);
        if (!ok) {
            persistOutboxMessage(messageId, -1, arr); // -1 indicates streaming
        }
    }

    private boolean sendWithRetries(int partitionId, VelocityDatum[] array, String messageId) {
        int attempts = 0;
        while (attempts < 5) {
            attempts++;
            try {
                ObjectPrx base = communicator.stringToProxy(masterEndpoint);
                MasterServicePrx master = MasterServicePrx.checkedCast(base);
                if (master == null) throw new IllegalStateException("Cannot create master proxy for endpoint=" + masterEndpoint);
                master.reportResult(partitionId, array);
                System.out.println("[IceClient] reportResult succeeded messageId=" + messageId + " attempts=" + attempts);
                return true;
            } catch (Exception ex) {
                System.err.println("[IceClient] attempt " + attempts + " failed to report partition result: " + ex.getMessage());
                try { Thread.sleep(500L * attempts); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }
        return false;
    }

    private boolean sendStreamingWithRetries(String workerId, VelocityDatum[] array, String messageId) {
        int attempts = 0;
        while (attempts < 5) {
            attempts++;
            try {
                ObjectPrx base = communicator.stringToProxy(masterEndpoint);
                MasterServicePrx master = MasterServicePrx.checkedCast(base);
                if (master == null) throw new IllegalStateException("Cannot create master proxy for endpoint=" + masterEndpoint);
                master.reportStreamingResult(workerId, array);
                System.out.println("[IceClient] reportStreamingResult succeeded messageId=" + messageId + " attempts=" + attempts);
                return true;
            } catch (Exception ex) {
                System.err.println("[IceClient] attempt " + attempts + " failed to report streaming result: " + ex.getMessage());
                try { Thread.sleep(500L * attempts); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }
        return false;
    }

    private void persistOutboxMessage(String messageId, int partitionId, List<VelocityDatum> arr) {
        Path file = outboxDir.resolve("msg_" + messageId + ".txt");
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            w.write(Integer.toString(partitionId));
            w.newLine();
            for (VelocityDatum v : arr) {
                w.write(v.arcId + "," + v.avgSpeed + "," + v.count);
                w.newLine();
            }
            System.out.println("[IceClient] persisted failed message to " + file.toAbsolutePath());
        } catch (IOException ex) {
            System.err.println("[IceClient] failed to persist outbox message: " + ex.getMessage());
        }
    }

    private void resendPending() {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(outboxDir, "msg_*.txt")) {
            for (Path p : ds) {
                try (BufferedReader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
                    String first = r.readLine();
                    int partitionId = Integer.parseInt(first.trim());
                    List<VelocityDatum> list = new ArrayList<>();
                    String line;
                    while ((line = r.readLine()) != null) {
                        String[] cols = line.split(",");
                        if (cols.length < 3) continue;
                        VelocityDatum v = new VelocityDatum();
                        v.arcId = cols[0];
                        try { v.avgSpeed = Double.parseDouble(cols[1]); } catch (NumberFormatException ne) { v.avgSpeed = 0.0; }
                        try { v.count = Long.parseLong(cols[2]); } catch (NumberFormatException ne) { v.count = 0L; }
                        list.add(v);
                    }
                    VelocityDatum[] array = list.toArray(new VelocityDatum[0]);
                    boolean ok;
                    if (partitionId >= 0) ok = sendWithRetries(partitionId, array, p.getFileName().toString());
                    else ok = sendStreamingWithRetries("worker", array, p.getFileName().toString());
                    if (ok) {
                        Files.deleteIfExists(p);
                        System.out.println("[IceClient] resent and deleted pending message " + p.getFileName());
                    }
                } catch (Exception ex) {
                    System.err.println("[IceClient] failed to resend pending " + p.getFileName() + ": " + ex.getMessage());
                }
            }
        } catch (IOException ex) {
            System.err.println("[IceClient] error scanning outbox: " + ex.getMessage());
        }
    }
}
