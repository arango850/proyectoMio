package com.mio.graph.model;

public class Line {
    private final int lineId;
    private final String shortName;
    private final String description;

    public Line(int lineId, String shortName, String description) {
        this.lineId = lineId;
        this.shortName = shortName == null ? "" : shortName;
        this.description = description == null ? "" : description;
    }

    public int getLineId() {
        return lineId;
    }

    public String getShortName() {
        return shortName;
    }

    public String getDescription() {
        return description;
    }
}
