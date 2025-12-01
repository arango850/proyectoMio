package edu.sitm.streaming;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import SITM.Datagram;
import SITM.WorkerServicePrx;
import edu.sitm.common.util.MapMatching;

import java.util.List;
import java.util.Arrays;
/**
 * IceClientStreaming: esqueleto del cliente ICE usado por el nodo streaming
 * para enviar datagramas a Workers y enviar reportes al Master cuando sea
 * necesario.
 *
 * Implementación esperada:
 * - Usar stubs generados por `slice2java` para crear proxies a `WorkerService`
 *   y llamar `processDatagram`.
 * - Manejar reintentos y ACKs para Reliable Messaging.
 */
public class IceClientStreaming {

    private final Communicator communicator;
    private final String streamEndpoint;
    private final String masterEndpoint;
    private final String[] workerEndpoints;

    public IceClientStreaming(Communicator communicator, String streamEndpoint, String masterEndpoint, String[] workerEndpoints) {
        this.communicator = communicator;
        this.streamEndpoint = streamEndpoint;
        this.masterEndpoint = masterEndpoint;
        this.workerEndpoints = workerEndpoints == null ? new String[0] : workerEndpoints;
    }

    public void start() {
        System.out.println("[IceClientStreaming] start. StreamEndpoint=" + streamEndpoint + ", Master=" + masterEndpoint + ", workers=" + Arrays.toString(workerEndpoints));
    }

    public void stop() {
        System.out.println("[IceClientStreaming] stop");
    }

    public void sendDatagram(String id, double lat, double lon, long timestamp, double speed) throws Exception {
        System.out.println(String.format("[IceClientStreaming] sendDatagram id=%s lat=%.6f lon=%.6f ts=%d speed=%.2f", id, lat, lon, timestamp, speed));

        String arcId = MapMatching.arcId(lat, lon);
        System.out.println("  arcId=" + arcId);

        if (workerEndpoints.length == 0) {
            // No workers known: nothing to do yet
            return;
        }

        // Simple consistent selection: hash of arcId
        int idx = Math.abs(arcId.hashCode()) % workerEndpoints.length;
        String workerEndpoint = workerEndpoints[idx];

        // Create SITM.Datagram and send
        Datagram d = new Datagram();
        d.id = id;
        d.lat = lat;
        d.lon = lon;
        d.timestamp = timestamp;
        d.speed = speed;
        d.raw = "";

        int attempts = 0;
        while (attempts < 3) {
            attempts++;
            try {
                ObjectPrx base = communicator.stringToProxy(workerEndpoint);
                WorkerServicePrx worker = WorkerServicePrx.checkedCast(base);
                if (worker == null) throw new IllegalStateException("Cannot create worker proxy for " + workerEndpoint);
                worker.processDatagram(d);
                System.out.println("[IceClientStreaming] sent datagram to " + workerEndpoint + " (attempt=" + attempts + ")");
                return;
            } catch (Exception ex) {
                System.err.println("[IceClientStreaming] attempt " + attempts + " failed to send datagram to " + workerEndpoint + ": " + ex.getMessage());
                if (attempts >= 3) {
                    System.err.println("[IceClientStreaming] giving up after " + attempts + " attempts for datagram " + id);
                } else {
                    try { Thread.sleep(200L * attempts); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        }
    }

    // Método para enviar lotes/resultados al Master (cuando sea necesario)
    public void reportToMaster(String workerId, List<?> windowResults) {
        System.out.println("[IceClientStreaming] reportToMaster from " + workerId + " items=" + (windowResults == null ? 0 : windowResults.size()));
    }
}
