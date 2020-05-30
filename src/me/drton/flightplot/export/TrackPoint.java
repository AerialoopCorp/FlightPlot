package me.drton.flightplot.export;

/**
 * Created by ada on 23.12.13.
 */
public class TrackPoint {
    public final double lat;        /// latitude
    public final double lon;        /// longitude
    public final double alt;        /// altitude AMLS
    public double gpsLat;        /// latitude
    public double gpsLon;        /// longitude
    public double gpsAlt;        /// altitude AMLS
    public long time;         /// unix time in milliseconds
    public final String flightMode; /// flight mode
    public boolean setpoint;
    public boolean hasGps;
    public int spType;
    public double radRoll;
    public double radPitch;
    public double heading;
    public int sequence;

    public TrackPoint(double lat, double lon, double alt, long time, String flightMode) {
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.time = time;
        this.flightMode = flightMode;
    }

    public TrackPoint(double lat, double lon, double alt, long time) {
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.time = time;
        this.flightMode = null;
    }
}
