package com.mio.graph.model;

public class Stop {
    private final long stopId;
    private final String shortName;
    private final String longName;
    private final String lat;
    private final String lon;

    public Stop(long stopId, String shortName, String longName, String lat, String lon) {
        this.stopId = stopId;
        this.shortName = shortName == null ? "" : shortName;
        this.longName = longName == null ? "" : longName;
        this.lat = lat;
        this.lon = lon;
    }

    public long getStopId() {
        return stopId;
    }

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }

    public String getLat() { return lat; }
    public String getLon() { return lon; }
}
