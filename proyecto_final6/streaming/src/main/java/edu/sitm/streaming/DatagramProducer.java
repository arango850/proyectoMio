package edu.sitm.streaming;

import edu.sitm.common.util.MapMatching;

import java.util.Random;
import java.util.UUID;

/**
 * DatagramProducer: genera datagramas simulados y los envía al IceClientStreaming.
 * Este productor es muy simple y sirve como esqueleto para integrar un flujo
 * real de datagramas.
 */
public class DatagramProducer {

    private final IceClientStreaming iceClient;
    private final Random rnd = new Random();

    public DatagramProducer(IceClientStreaming iceClient) {
        this.iceClient = iceClient;
    }

    public void produceOnce() {
        // generar datagrama aleatorio cercano a coordenadas de ejemplo
        double lat = 3.45 + rnd.nextDouble() * 0.01; // ej. Medellín/Bogotá small offset
        double lon = -76.54 + rnd.nextDouble() * 0.01;
        long ts = System.currentTimeMillis();
        double speed = 10.0 + rnd.nextDouble() * 15.0; // entre 10 y 25
        String id = UUID.randomUUID().toString();

        // Enviar mediante IceClientStreaming (esqueleto)
        try {
            iceClient.sendDatagram(id, lat, lon, ts, speed);
        } catch (Exception e) {
            System.err.println("Failed to send datagram: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
