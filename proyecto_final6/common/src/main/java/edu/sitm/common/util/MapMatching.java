package edu.sitm.common.util;

/**
 * Utilidad de map-matching simplificado.
 *
 * Patrón aplicado: esta clase encapsula la función de map-matching usada
 * por los Workers para convertir lat/lon en `arcId`.
 * Implementación sencilla: floor(lat*1000) + '_' + floor(lon*1000)
 */
public final class MapMatching {

    private MapMatching() {
        // util class
    }

    /**
     * Calcula el arcId para una coordenada.
     * @param lat latitud
     * @param lon longitud
     * @return arcId en formato "<floor(lat*1000)>_<floor(lon*1000)>"
     */
    public static String arcId(double lat, double lon) {
        long la = (long) Math.floor(lat * 1000.0);
        long lo = (long) Math.floor(lon * 1000.0);
        return la + "_" + lo;
    }
}
