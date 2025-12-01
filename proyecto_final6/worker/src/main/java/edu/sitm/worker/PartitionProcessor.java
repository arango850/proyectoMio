package edu.sitm.worker;

import edu.sitm.common.util.MapMatching;
import edu.sitm.common.dto.VelocityDatum;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * PartitionProcessor procesa un rango de líneas de CSV (startLine..endLine).
 * Este esqueleto implementa la lógica local de agregación por arco y luego
 * delega el envío del resultado al Master mediante `IceClient`.
 *
 * Nota: asume que el CSV está disponible localmente en el path proporcionado
 * por `PartitionTask.csvPath`. No maneja la copia desde el Master; eso queda
 * como paso manual de despliegue.
 */
public class PartitionProcessor {

    private final IceClient iceClient;
    private final ExecutorService pool;

    public PartitionProcessor(IceClient iceClient, ExecutorService pool) {
        this.iceClient = iceClient;
        this.pool = pool;
    }

    public void submitPartitionTask(int partitionId, String csvPath, int startLine, int endLine) {
        pool.submit(() -> processRange(partitionId, csvPath, startLine, endLine));
    }

    private void processRange(int partitionId, String csvPath, int startLine, int endLine) {
        // Map arcId -> [sumSpeed, count]
        Map<String, double[]> agg = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String line;
            int idx = 0;
            while ((line = br.readLine()) != null) {
                idx++;
                if (idx < startLine) continue;
                if (idx > endLine) break;

                // Presumimos CSV: id,lat,lon,timestamp,speed,...
                String[] cols = line.split(",");
                if (cols.length < 5) continue;
                try {
                    double lat = Double.parseDouble(cols[1]);
                    double lon = Double.parseDouble(cols[2]);
                    double speed = Double.parseDouble(cols[4]);

                    String arcId = MapMatching.arcId(lat, lon);
                    double[] cur = agg.get(arcId);
                    if (cur == null) {
                        cur = new double[]{0.0, 0.0}; // sum, count
                        agg.put(arcId, cur);
                    }
                    cur[0] += speed;
                    cur[1] += 1.0;
                } catch (NumberFormatException ex) {
                    // ignorar línea mal formada
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read CSV partition: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Construir lista de VelocityDatum
        java.util.List<VelocityDatum> results = new java.util.ArrayList<>();
        for (Map.Entry<String, double[]> e : agg.entrySet()) {
            String arc = e.getKey();
            double sum = e.getValue()[0];
            long count = (long) e.getValue()[1];
            double avg = count > 0 ? (sum / count) : 0.0;
            results.add(new VelocityDatum(arc, avg, count));
        }

        // Enviar resultados al Master mediante ICE (esqueleto)
        try {
            iceClient.reportPartitionResult(partitionId, results);
        } catch (Exception ex) {
            System.err.println("Failed to report partition result: " + ex.getMessage());
            ex.printStackTrace();
            // aquí implementaremos reintentos y colas de reenvío para reliable messaging
        }
    }
}
