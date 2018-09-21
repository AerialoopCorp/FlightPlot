package me.drton.flightplot.processors;

import java.util.HashMap;
import java.util.Map;

/**
 * User: ton Date: 16.06.13 Time: 12:59
 */
public class DistanceToWaypoint extends PlotProcessor {
    protected String param_PosLat;
    protected String param_PosLon;
    protected String param_TargetLat;
    protected String param_TargetLon;
    protected String param_LandState;
    protected String[] param_Groundspeed;
    protected double param_Scale;
    private double targetLat = 0;
    private double targetLon = 0;
    private boolean flaring = false;
    private double distanceAtFlaring = -1.0;
    private double groundspeed = 0;
    private int distanceSeriesIndex = 0;
    private int markerListIndex = 0;

    @Override
    public Map<String, Object> getDefaultParameters() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("Pos Lat", "GPOS.Lat");
        params.put("Pos Lon", "GPOS.Lon");
        params.put("Target Lat", "GPSP.Lat");
        params.put("Target Lon", "GPSP.Lon");
        params.put("Land State", "NACA.State");
        params.put("Groundspeed", "LPOS.VX LPOS.VY");
        params.put("Scale", 1.0);
        return params;
    }

    @Override
    public void init() {
        param_PosLat = (String) parameters.get("Pos Lat");
        param_PosLon = (String) parameters.get("Pos Lon");
        param_TargetLat = (String) parameters.get("Target Lat");
        param_TargetLon = (String) parameters.get("Target Lon");
        param_LandState = (String) parameters.get("Land State");
        param_Groundspeed = ((String) parameters.get("Groundspeed")).split((WHITESPACE_RE));
        param_Scale = (Double) parameters.get("Scale");
        distanceSeriesIndex = addSeries("Distance");
        markerListIndex = addMarkersList("Markers");
    }

    @Override
    public void process(double time, Map<String, Object> update) {
        double lat_now = 0;
        double lon_now = 0;

        Object v = update.get(param_PosLat);
        if (v != null && v instanceof Number) {
            lat_now = ((Number) v).doubleValue();
        }

        v = update.get(param_PosLon);
        if (v != null && v instanceof Number) {
            lon_now = ((Number) v).doubleValue();
        }

        v = update.get(param_TargetLat);
        if (v != null && v instanceof Number) {
            targetLat = ((Number) v).doubleValue();
        }

        v = update.get(param_TargetLon);
        if (v != null && v instanceof Number) {
            targetLon = ((Number) v).doubleValue();
        }

        boolean haveGroundSpeed = false;
        for (String field : param_Groundspeed) {
            v = update.get(field);
            if (v != null && v instanceof Number) {
                double d = ((Number) v).doubleValue();
                groundspeed += d * d;
                haveGroundSpeed = true;
            }
        }

        if (haveGroundSpeed) {
            groundspeed = Math.sqrt(groundspeed);
        }

        v = update.get(param_LandState);
        if (v != null && v instanceof Number) {
            if (((Number) v).intValue() == 6) {
                if (!flaring) {
                    flaring = true;
                    addMarker(markerListIndex, time, "Flaring");
                }

            } else {
                flaring = false;
            }
        }

        if (targetLat == 0 || targetLon == 0 || lat_now == 0 || lon_now == 0) {
            return;
        }

        double lat_now_rad = lat_now / 180.0 * Math.PI;
        double lon_now_rad = lon_now / 180.0 * Math.PI;
        double lat_next_rad = targetLat / 180.0 * Math.PI;
        double lon_next_rad = targetLon / 180.0 * Math.PI;

        double d_lat = lat_next_rad - lat_now_rad;
        double d_lon = lon_next_rad - lon_now_rad;

        double a = Math.sin(d_lat / 2.0) * Math.sin(d_lat / 2.0) + Math.sin(d_lon / 2.0) * Math.sin(d_lon /
                2.0) * Math.cos(lat_now_rad) * Math.cos(lat_next_rad);
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

        // 6371000 = radius of earth in m
        double distance = 6371000 * c;

        if (flaring && distanceAtFlaring < 0 && groundspeed > 2.0) {
            distanceAtFlaring = distance;

        } else if (flaring && groundspeed < 2.0 && distanceAtFlaring > 0) {
            // we assume we flared and are on the ground now
            addMarker(markerListIndex, time, String.format("Landed, flare dist %.2f", distanceAtFlaring - distance));
            distanceAtFlaring = -1.0;
        }

        addPoint(distanceSeriesIndex, time, distance * param_Scale);
    }
}
