package edu.sitm.worker;

import edu.sitm.common.util.MapMatching;
import edu.sitm.common.dto.VelocityDatum;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * StreamingProcessor: mantiene ventanas por arcId y actualiza agregados incrementales.
 * Ventanas: sliding windows de 5 minutos (configurable). Este esqueleto muestra la
 * estructura de datos y el punto donde se emitirían resultados parciales al Master.
 */
public class StreamingProcessor {

    private final IceClient iceClient;
    private final ExecutorService pool;

    // Estructura por arc: arcId -> (sum, count, lastTimestamp)
    private final Map<String, double[]> state = new ConcurrentHashMap<>();

    public StreamingProcessor(IceClient iceClient, ExecutorService pool) {
        this.iceClient = iceClient;
        this.pool = pool;
    }

    public void processDatagram(String id, double lat, double lon, long timestamp, double speed) {
        // Debe ejecutarse rápidamente; delegar en thread-pool si es CPU-intensivo
        String arcId = MapMatching.arcId(lat, lon);
        state.compute(arcId, (k, cur) -> {
            if (cur == null) return new double[]{speed, 1.0, (double) timestamp};
            cur[0] += speed;
            cur[1] += 1.0;
            cur[2] = Math.max(cur[2], (double) timestamp);
            return cur;
        });

        // En un diseño real, aquí manejamos ventanas temporales: purgar muestras fuera de la ventana
        // y agregar un mecanismo para emitir resultados por ventana de 5 minutos.
    }

    // Método de ejemplo que genera un snapshot para enviar al Master
    public java.util.List<VelocityDatum> snapshotAndClear() {
        java.util.List<VelocityDatum> out = new java.util.ArrayList<>();
        for (Map.Entry<String, double[]> e : state.entrySet()) {
            String arc = e.getKey();
            double sum = e.getValue()[0];
            long count = (long) e.getValue()[1];
            double avg = count > 0 ? (sum / count) : 0.0;
            out.add(new VelocityDatum(arc, avg, count));
        }
        // En un comportamiento real no debemos limpiar todo; usamos ventanas deslizantes.
        state.clear();
        return out;
    }
}
