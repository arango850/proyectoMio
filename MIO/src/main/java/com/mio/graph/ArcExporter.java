package com.mio.graph;

import com.mio.graph.model.Arc;
import com.mio.graph.model.Stop;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class ArcExporter {

    public static void export(Map<Integer, Map<Integer, List<Arc>>> routeGraphs,
                              Map<Long, Stop> stops,
                              Path out) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(out)) {
            w.write("lineId,orientation,seq,fromStopId,toStopId,fromShortName,toShortName\n");
           
            TreeSet<Integer> lines = new TreeSet<>(routeGraphs.keySet());
            for (Integer lineId : lines) {
                Map<Integer, List<Arc>> byOrient = routeGraphs.get(lineId);
                TreeSet<Integer> orients = new TreeSet<>(byOrient.keySet());
                for (Integer orient : orients) {
                    List<Arc> arcs = byOrient.get(orient);
                    for (int i = 0; i < arcs.size(); i++) {
                        Arc a = arcs.get(i);
                        String fromName = stops.containsKey(a.getFromStopId()) ? escapeCsv(stops.get(a.getFromStopId()).getShortName()) : "";
                        String toName = stops.containsKey(a.getToStopId()) ? escapeCsv(stops.get(a.getToStopId()).getShortName()) : "";
                        w.write(String.format("%d,%d,%d,%d,%d,%s,%s\n",
                                lineId, orient, i + 1, a.getFromStopId(), a.getToStopId(), fromName, toName));
                    }
                }
            }
        }
    }

    private static String escapeCsv(String s) {
        if (s == null) return "";
        String out = s.replaceAll("\"", "\"\"");
        if (out.contains(",") || out.contains("\n") || out.contains("\r")) {
            return "\"" + out + "\"";
        }
        return out;
    }
}
