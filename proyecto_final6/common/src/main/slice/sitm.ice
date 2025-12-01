// SITM Slice definitions for Velocity calculation
// Placeholders for ZeroTier endpoints and paths will be used in runtime configuration.
module SITM
{
    // Datagram GPS record used for streaming and for line-level processing
    struct Datagram
    {
        string id;       // UUID or message id
        double lat;
        double lon;
        long timestamp;  // epoch ms
        double speed;    // speed reported in datagram (unit documented in README)
        string raw;      // optional: raw CSV line
    };

    // Velocity result element per arc
    struct VelocityDatum
    {
        string arcId;    // map-matching id: floor(lat*1000) + '_' + floor(lon*1000)
        double avgSpeed; // average speed for the arc
        long count;      // number of samples aggregated
    };

    // Task for batch partition processing
    struct PartitionTask
    {
        int startLine;
        int endLine;
        string csvPath;  // path to CSV (local on the worker)
        int partitionId;
    };

    sequence<VelocityDatum> VelocityList;

    // Worker service: operations exposed on each worker node
    interface WorkerService
    {
        // Process a batch partition (Master -> Worker)
        void processPartition(PartitionTask task);

        // Process a single datagram (Streaming -> Worker)
        void processDatagram(Datagram d);

        // Health-check/ping
        void ping();
    };

    // Master service: operations exposed on Master for workers to report results
    interface MasterService
    {
        // Report partial results for a partition (Worker -> Master)
        void reportResult(int partitionId, VelocityList results);

        // Report streaming window results from a worker (Worker -> Master)
        void reportStreamingResult(string workerId, VelocityList windowResults);

        // Acknowledge message receipt (optional)
        void ack(string messageId);
    };
};
