package edu.sitm.master;

import SITM.VelocityDatum;
import SITM.MasterService;
import com.zeroc.Ice.Current;

import java.util.ArrayList;
import java.util.List;

/**
 * ICE servant implementing the generated `SITM.MasterService` interface.
 * Delegates to `MasterServiceImpl` for aggregation and persistence logic.
 */
public class MasterServiceI implements MasterService {

    private final MasterServiceImpl impl;

    public MasterServiceI(MasterServiceImpl impl) {
        this.impl = impl;
    }

    @Override
    public void reportResult(int partitionId, VelocityDatum[] results, Current __current) {
        // Convert generated VelocityDatum[] to local DTO list used by impl
        List<edu.sitm.common.dto.VelocityDatum> list = new ArrayList<>();
        if (results != null) {
            for (VelocityDatum v : results) {
                list.add(new edu.sitm.common.dto.VelocityDatum(v.arcId, v.avgSpeed, v.count));
            }
        }
        impl.reportResult(partitionId, list);
    }

    @Override
    public void reportStreamingResult(String workerId, VelocityDatum[] windowResults, Current __current) {
        List<edu.sitm.common.dto.VelocityDatum> list = new ArrayList<>();
        if (windowResults != null) {
            for (VelocityDatum v : windowResults) {
                list.add(new edu.sitm.common.dto.VelocityDatum(v.arcId, v.avgSpeed, v.count));
            }
        }
        impl.reportStreamingResult(workerId, list);
    }

    @Override
    public void ack(String messageId, Current __current) {
        // For now, simple log; in full implementation update ACK tracking
        System.out.println("Master received ack: " + messageId);
    }
}
