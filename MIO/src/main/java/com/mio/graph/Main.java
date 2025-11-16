package com.mio.graph;

import com.mio.graph.model.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws Exception {
        Path projectRoot = Path.of(System.getProperty("user.dir"));

        String linesFile = args.length > 0 ? args[0] : "lines-241.csv";
        String stopsFile = args.length > 1 ? args[1] : "stops-241.csv";
        String lineStopsFile = args.length > 2 ? args[2] : "linestops-241.csv";

        System.out.println("Reading CSVs from: " + projectRoot.toAbsolutePath());

        Map<Integer, Line> lines = parseLines(projectRoot.resolve(linesFile));
        Map<Long, Stop> stops = parseStops(projectRoot.resolve(stopsFile));
        List<LineStop> lineStops = parseLineStops(projectRoot.resolve(lineStopsFile));

        GraphBuilder builder = new GraphBuilder(lines, stops, lineStops);
        Map<Integer, Map<Integer, List<Arc>>> routeGraphs = builder.buildRouteArcs();

        // Export arcs.csv
        Path arcsCsv = projectRoot.resolve("arcs.csv");
        ArcExporter.export(routeGraphs, stops, arcsCsv);
        System.out.println("Wrote arcs to: " + arcsCsv.toAbsolutePath());

        // Create output directory for images
        Path graphsDir = projectRoot.resolve("graphs");
        if (!graphsDir.toFile().exists()) graphsDir.toFile().mkdirs();

        // Render JPG per route+orientation (bonus)
        for (Integer routeId : routeGraphs.keySet()) {
            Map<Integer, List<Arc>> byOrient = routeGraphs.get(routeId);
            for (Map.Entry<Integer, List<Arc>> eo : byOrient.entrySet()) {
                int orient = eo.getKey();
                List<Arc> arcs = eo.getValue();
                Path out = graphsDir.resolve(String.format("route_%d_orient_%d.jpg", routeId, orient));
                try {
                    GraphVisualizer.renderRoute(routeId, orient, arcs, stops, out);
                } catch (Exception ex) {
                    System.err.println("Failed to render " + out + ": " + ex.getMessage());
                }
            }
        }

        // Print ordered arcs by route and orientation
        List<Integer> sortedRoutes = new ArrayList<>(routeGraphs.keySet());
        Collections.sort(sortedRoutes);

        for (Integer routeId : sortedRoutes) {
            Line line = lines.get(routeId);
            String shortName = line != null ? line.getShortName() : "(unknown)";
            Map<Integer, List<Arc>> byOrient = routeGraphs.get(routeId);
            List<Integer> orients = new ArrayList<>(byOrient.keySet());
            Collections.sort(orients);
            for (Integer orient : orients) {
                System.out.printf("Route %d (%s) - Orientation %d\n", routeId, shortName, orient);
                List<Arc> arcs = byOrient.get(orient);
                for (int i = 0; i < arcs.size(); i++) {
                    Arc a = arcs.get(i);
                    String fromName = stops.containsKey(a.getFromStopId()) ? stops.get(a.getFromStopId()).getShortName() : "?";
                    String toName = stops.containsKey(a.getToStopId()) ? stops.get(a.getToStopId()).getShortName() : "?";
                    System.out.printf("  %3d: %d (%s) -> %d (%s)\n", i + 1, a.getFromStopId(), fromName, a.getToStopId(), toName);
                }
                System.out.println();
            }
        }
    }

    private static Map<Integer, Line> parseLines(Path p) throws IOException {
        Map<Integer, Line> map = new HashMap<>();
        try (Reader r = Files.newBufferedReader(p);
             CSVParser parser = CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withTrim()
                     .parse(r)) {
            for (CSVRecord rec : parser) {
                String idS = rec.get("LINEID");
                if (idS == null || idS.isBlank()) continue;
                int id = Integer.parseInt(idS.trim());
                String shortname = safeGet(rec, "SHORTNAME", "SHORTNAME\n", "SHORTNAME ");
                String desc = safeGet(rec, "DESCRIPTION", "DESCRIPTION\n");
                map.put(id, new Line(id, shortname, desc));
            }
        }
        return map;
    }

    private static Map<Long, Stop> parseStops(Path p) throws IOException {
        Map<Long, Stop> map = new HashMap<>();
        try (Reader r = Files.newBufferedReader(p);
             CSVParser parser = CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withTrim()
                     .parse(r)) {
            for (CSVRecord rec : parser) {
                String idS = safeGet(rec, "STOPID", "STOPID\n");
                if (idS == null || idS.isBlank()) continue;
                long id = Long.parseLong(idS.trim());
                String shortName = safeGet(rec, "SHORTNAME", "SHORTNAME\n", "SHORTNAME ");
                String longName = safeGet(rec, "LONGNAME", "LONGNAME\n", "LONGNAME ");
                String lon = safeGet(rec, "DECIMALLONG", "DECIMALLONG\n", "DECIMALLONG ");
                String lat = safeGet(rec, "DECIMALLATIT", "DECIMALLATIT\n", "DECIMALLATIT ");
                map.put(id, new Stop(id, shortName, longName, lat, lon));
            }
        }
        return map;
    }

    private static List<LineStop> parseLineStops(Path p) throws IOException {
        List<LineStop> list = new ArrayList<>();
        try (Reader r = Files.newBufferedReader(p);
             CSVParser parser = CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withTrim()
                     .parse(r)) {
            for (CSVRecord rec : parser) {
                String lineIdS = safeGet(rec, "LINEID", "LINEID\n");
                String stopSeqS = safeGet(rec, "STOPSEQUENCE", "STOPSEQUENCE\n", "STOPSEQUENCE ");
                String orientationS = safeGet(rec, "ORIENTATION", "ORIENTATION\n");
                String stopIdS = safeGet(rec, "STOPID", "STOPID\n");
                if (lineIdS == null || lineIdS.isBlank() || stopIdS == null || stopIdS.isBlank()) continue;
                int lineId = Integer.parseInt(lineIdS.trim());
                int stopSeq = stopSeqS != null && !stopSeqS.isBlank() ? Integer.parseInt(stopSeqS.trim()) : -1;
                int orientation = orientationS != null && !orientationS.isBlank() ? Integer.parseInt(orientationS.trim()) : 0;
                long stopId = Long.parseLong(stopIdS.trim());
                list.add(new LineStop(lineId, stopId, stopSeq, orientation));
            }
        }
        return list;
    }

    private static String safeGet(CSVRecord rec, String... keys) {
        for (String k : keys) {
            try {
                if (rec.isMapped(k)) {
                    String v = rec.get(k);
                    if (v != null) return v;
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        // fallback: try first column that looks like the key ignoring extra chars
        for (String header : rec.getParser().getHeaderMap().keySet()) {
            String norm = header.replaceAll("\\n|\\s", "").toUpperCase();
            for (String k : keys) {
                if (norm.equalsIgnoreCase(k.replaceAll("\\s", ""))) {
                    try { return rec.get(header); } catch (Exception ignored) {}
                }
            }
        }
        return null;
    }
}
