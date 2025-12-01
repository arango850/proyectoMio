package edu.sitm.common;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import edu.sitm.common.dto.VelocityDatum;
import edu.sitm.common.util.MapMatching;

/**
 * Demo muy pequeño para validar las utilidades de `common` sin depender de ICE.
 * Muestra cómo obtener `arcId` y cómo escribir un CSV de `VelocityDatum`.
 */
public class MainDemo {
    public static void main(String[] args) {
        System.out.println("SITM common module demo");

        // ejemplo de coordenadas
        double lat = 3.451234;
        double lon = -76.54321;
        String arc = MapMatching.arcId(lat, lon);
        System.out.println("arcId(" + lat + ", " + lon + ") = " + arc);

        // crear algunos VelocityDatum de prueba
        List<VelocityDatum> list = new ArrayList<>();
        list.add(new VelocityDatum(arc, 12.5, 10));
        list.add(new VelocityDatum(MapMatching.arcId(3.451, -76.544), 10.0, 5));

        // escribir CSV de salida en ./target/ (no usar OUTPUT_PATH aquí; demo local)
        String ts = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
        String out = "target/velocidades_demo_" + ts + ".csv";
        try {
            java.io.File parent = new java.io.File(out).getAbsoluteFile().getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (FileWriter fw = new FileWriter(out)) {
            fw.write("arcId,avgSpeed,count\n");
            for (VelocityDatum v : list) {
                fw.write(v.toString() + "\n");
            }
                System.out.println("Wrote demo CSV to: " + out);
            }
        } catch (IOException e) {
            System.err.println("Failed to write demo CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
