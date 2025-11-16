package com.mio.graph.model;

public class LineStop {
    private final int lineId;
    private final long stopId;
    private final int stopSequence;
    private final int orientation;

    public LineStop(int lineId, long stopId, int stopSequence, int orientation) {
        this.lineId = lineId;
        this.stopId = stopId;
        this.stopSequence = stopSequence;
        this.orientation = orientation;
    }

    public int getLineId() {
        return lineId;
    }

    public long getStopId() {
        return stopId;
    }

    public int getStopSequence() {
        return stopSequence;
    }

    public int getOrientation() {
        return orientation;
    }
}
