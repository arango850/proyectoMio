package edu.sitm.worker;

import SITM.Datagram;
import SITM.PartitionTask;
import SITM.WorkerService;
import com.zeroc.Ice.Current;

/**
 * WorkerServiceI: ICE servant implementing SITM.WorkerService.
 * Delegates processing to local processors.
 */
public class WorkerServiceI implements WorkerService {

    private final PartitionProcessor partitionProcessor;
    private final StreamingProcessor streamingProcessor;

    public WorkerServiceI(PartitionProcessor partitionProcessor, StreamingProcessor streamingProcessor) {
        this.partitionProcessor = partitionProcessor;
        this.streamingProcessor = streamingProcessor;
    }

    @Override
    public void processPartition(PartitionTask task, Current __current) {
        System.out.println("WorkerService.processPartition received: partitionId=" + task.partitionId + " start=" + task.startLine + " end=" + task.endLine + " csvPath=" + task.csvPath);
        partitionProcessor.submitPartitionTask(task.partitionId, task.csvPath, task.startLine, task.endLine);
    }

    @Override
    public void processDatagram(Datagram d, Current __current) {
        System.out.println("WorkerService.processDatagram id=" + d.id + " lat=" + d.lat + " lon=" + d.lon + " ts=" + d.timestamp + " speed=" + d.speed);
        streamingProcessor.processDatagram(d.id, d.lat, d.lon, d.timestamp, d.speed);
    }

    @Override
    public void ping(Current __current) {
        System.out.println("WorkerService.ping received");
    }
}
