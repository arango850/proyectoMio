package edu.sitm.master;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import SITM.PartitionTask;
import SITM.WorkerServicePrx;

/**
 * Orchestrator: responsable de enviar particiones a los Workers.
 *
 * En esta etapa esqueleto, Orchestrator delega a un Ice client placeholder
 * para demostrar dónde se implementaría el envío real (Master -> Worker).
 *
 * Patrones aplicados:
 * - Master–Worker: el Orchestrator asigna particiones a Workers.
 * - Producer–Consumer: las tareas de partición se consideran mensajes que el
 *   Orchestrator produce hacia colas de envío (aquí simplificado).
 * - Reliable Messaging: el IceClient puede implementar reintentos/ACKs.
 */
public class Orchestrator {

    private final MasterServiceImpl masterService;
    private final Communicator communicator;
    private final String[] workerEndpoints;
    private final AtomicInteger rr = new AtomicInteger(0);
    private final MasterIceClient iceClient;

    public Orchestrator(MasterServiceImpl masterService, Communicator communicator, String[] workerEndpoints) {
        this.masterService = masterService;
        this.communicator = communicator;
        this.workerEndpoints = workerEndpoints;
        this.iceClient = new MasterIceClient(communicator);
        this.iceClient.start();
    }

    public void dispatchPartition(int partitionId, String csvPath, int startLine, int endLine) {
        // Round-robin assignment
        int idx = Math.abs(rr.getAndIncrement()) % workerEndpoints.length;
        String worker = workerEndpoints[idx];
        System.out.println("Dispatching partition " + partitionId + " [" + startLine + ":" + endLine + "] to " + worker);

        // Envío real vía ICE se implementará en MasterIceClient.sendPartition
        iceClient.sendPartition(partitionId, csvPath, startLine, endLine, worker);
    }
}

class MasterIceClient {
    private final Communicator communicator;

    public MasterIceClient(Communicator communicator) {
        this.communicator = communicator;
    }

    public void start() {
        System.out.println("[MasterIceClient] start placeholder. communicator=" + communicator);
        // Inicializar proxies si se dispone de las clases generadas
    }

    public void stop() {
        System.out.println("[MasterIceClient] stop placeholder");
    }

    public void sendPartition(int partitionId, String csvPath, int startLine, int endLine, String workerEndpoint) {
        System.out.println("[MasterIceClient] sending PartitionTask(partitionId=" + partitionId + ", start=" + startLine + ", end=" + endLine + ") to " + workerEndpoint);
        PartitionTask task = new PartitionTask();
        task.partitionId = partitionId;
        task.csvPath = csvPath;
        task.startLine = startLine;
        task.endLine = endLine;

        int attempts = 0;
        while (attempts < 3) {
            attempts++;
            try {
                ObjectPrx base = communicator.stringToProxy(workerEndpoint);
                WorkerServicePrx worker = WorkerServicePrx.checkedCast(base);
                if (worker == null) throw new IllegalStateException("Cannot create worker proxy for " + workerEndpoint);
                worker.processPartition(task);
                System.out.println("[MasterIceClient] successfully sent partition " + partitionId + " to " + workerEndpoint);
                return;
            } catch (Exception ex) {
                System.err.println("[MasterIceClient] attempt " + attempts + " failed to send partition to " + workerEndpoint + ": " + ex.getMessage());
                if (attempts >= 3) {
                    System.err.println("[MasterIceClient] giving up after " + attempts + " attempts");
                } else {
                    try { Thread.sleep(1000L * attempts); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        }
    }
}
