package edu.sitm.master;

import edu.sitm.common.dto.VelocityDatum;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MasterServiceImpl: Receptor de resultados parciales desde Workers.
 *
 * Implementa la agregación ponderada por conteo: cuando recibe un conjunto
 * de `VelocityDatum` con `avgSpeed` y `count`, convierte a suma parcial
 * (avg*count) y acumula para calcular el promedio final más tarde.
 *
 * Patrones aplicados: Reliable Messaging (comentarios donde implementar ACKs y reintentos).
 */
public class MasterServiceImpl {

    private final String outputPath;
    // arcId -> [sumOfSpeeds, totalCount]
    private final Map<String, double[]> aggregates = new ConcurrentHashMap<>();

    public MasterServiceImpl(String outputPath) {
        this.outputPath = outputPath;
    }

    // Método que workers llamarían (placeholder) para reportar resultados de partición
    public void reportResult(int partitionId, List<VelocityDatum> results) {
        System.out.println("[MasterService] Received partition result " + partitionId + " items=" + results.size());
        for (VelocityDatum d : results) {
            double partialSum = d.getAvgSpeed() * (double) d.getCount();
            aggregates.compute(d.getArcId(), (k, cur) -> {
                if (cur == null) {
                    return new double[]{partialSum, (double) d.getCount()};
                }
                cur[0] += partialSum;
                cur[1] += (double) d.getCount();
                return cur;
            });
        }
        // En un diseño real, enviar ACK al Worker y actualizar métricas internas
    }

    // Método que workers usarían para reportes de streaming
    public void reportStreamingResult(String workerId, List<VelocityDatum> windowResults) {
        System.out.println("[MasterService] Received streaming window from " + workerId + " items=" + windowResults.size());
        // integrar igual que en reportResult (se pueden mantener estructuras separadas si se requiere)
        for (VelocityDatum d : windowResults) {
            double partialSum = d.getAvgSpeed() * (double) d.getCount();
            aggregates.compute(d.getArcId(), (k, cur) -> {
                if (cur == null) {
                    return new double[]{partialSum, (double) d.getCount()};
                }
                cur[0] += partialSum;
                cur[1] += (double) d.getCount();
                return cur;
            });
        }
    }

    // Persistir resultados finales en CSV y guardar métricas de ejecución
    public void writeFinalOutputs() throws IOException {
        String ts = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());

        String outCsv = outputPath + "/velocidades_historicas.csv";
        String metrics = outputPath + "/metrics_historico_" + ts + ".txt";

        try (FileWriter fw = new FileWriter(outCsv)) {
            fw.write("arcId,avgSpeed,count\n");
            for (Map.Entry<String, double[]> e : aggregates.entrySet()) {
                String arc = e.getKey();
                double sum = e.getValue()[0];
                long count = (long) e.getValue()[1];
                double avg = count > 0 ? (sum / (double) count) : 0.0;
                fw.write(arc + "," + avg + "," + count + "\n");
            }
        }

        try (FileWriter fm = new FileWriter(metrics)) {
            fm.write("timestamp=" + ts + "\n");
            fm.write("distinct_arcs=" + aggregates.size() + "\n");
            long totalSamples = aggregates.values().stream().mapToLong(a -> (long) a[1]).sum();
            fm.write("total_samples=" + totalSamples + "\n");
            fm.write("note=This is a skeleton metrics file. Add timings and worker stats in full implementation.\n");
        }

        System.out.println("Wrote outputs: " + outCsv + " and metrics: " + metrics);
    }
}
