package com.mio.graph.model;

public class Arc {
    private final int lineId;
    private final int orientation;
    private final long fromStopId;
    private final long toStopId;

    public Arc(int lineId, int orientation, long fromStopId, long toStopId) {
        this.lineId = lineId;
        this.orientation = orientation;
        this.fromStopId = fromStopId;
        this.toStopId = toStopId;
    }

    public int getLineId() {
        return lineId;
    }

    public int getOrientation() {
        return orientation;
    }

    public long getFromStopId() {
        return fromStopId;
    }

    public long getToStopId() {
        return toStopId;
    }
}
