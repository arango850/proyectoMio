package edu.sitm.common.dto;

/**
 * DTO usado localmente y para pruebas. Note: cuando `slice2java` genere
 * clases desde `sitm.ice`, esas clases del paquete generado serán las que
 * se usen para la comunicación ICE. Esta clase es útil para pruebas y para
 * construir CSVs de salida en las etapas tempranas.
 */
public class VelocityDatum {
    private final String arcId;
    private final double avgSpeed;
    private final long count;

    public VelocityDatum(String arcId, double avgSpeed, long count) {
        this.arcId = arcId;
        this.avgSpeed = avgSpeed;
        this.count = count;
    }

    public String getArcId() {
        return arcId;
    }

    public double getAvgSpeed() {
        return avgSpeed;
    }

    public long getCount() {
        return count;
    }

    @Override
    public String toString() {
        return arcId + "," + avgSpeed + "," + count;
    }
}
