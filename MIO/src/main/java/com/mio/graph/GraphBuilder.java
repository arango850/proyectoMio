package com.mio.graph;

import com.mio.graph.model.Arc;
import com.mio.graph.model.LineStop;
import com.mio.graph.model.Stop;
import com.mio.graph.model.Line;

import java.util.*;
import java.util.stream.Collectors;

public class GraphBuilder {
    private final Map<Integer, Line> lines;
    private final Map<Long, Stop> stops;
    private final List<LineStop> lineStops;

    public GraphBuilder(Map<Integer, Line> lines, Map<Long, Stop> stops, List<LineStop> lineStops) {
        this.lines = lines;
        this.stops = stops;
        this.lineStops = lineStops;
    }

    public Map<Integer, Map<Integer, List<Arc>>> buildRouteArcs() {
        
        Map<Integer, Map<Integer, List<LineStop>>> grouped = new HashMap<>();

        for (LineStop ls : lineStops) {
            grouped.computeIfAbsent(ls.getLineId(), k -> new HashMap<>())
                    .computeIfAbsent(ls.getOrientation(), k -> new ArrayList<>())
                    .add(ls);
        }

        Map<Integer, Map<Integer, List<Arc>>> result = new HashMap<>();

        for (Map.Entry<Integer, Map<Integer, List<LineStop>>> e : grouped.entrySet()) {
            int lineId = e.getKey();
            Map<Integer, List<LineStop>> byOrient = e.getValue();
            Map<Integer, List<Arc>> arcsByOrient = new HashMap<>();
            for (Map.Entry<Integer, List<LineStop>> o : byOrient.entrySet()) {
                int orient = o.getKey();
                List<LineStop> stopsList = o.getValue();
                stopsList.sort(Comparator.comparingInt(LineStop::getStopSequence));
                List<Arc> arcs = new ArrayList<>();
                for (int i = 0; i < stopsList.size() - 1; i++) {
                    long from = stopsList.get(i).getStopId();
                    long to = stopsList.get(i + 1).getStopId();
                    arcs.add(new Arc(lineId, orient, from, to));
                }
                arcsByOrient.put(orient, arcs);
            }
            result.put(lineId, arcsByOrient);
        }

        return result;
    }
}
