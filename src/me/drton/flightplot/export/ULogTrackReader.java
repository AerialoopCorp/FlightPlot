package me.drton.flightplot.export;

import me.drton.jmavlib.geo.GlobalPositionProjector;
import me.drton.jmavlib.geo.LatLonAlt;
import me.drton.jmavlib.log.FormatErrorException;
import me.drton.jmavlib.log.ulog.ULogReader;

import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ada on 23.12.13.
 */
public class ULogTrackReader extends AbstractTrackReader {
    private static final String GPOS_VALID = "vehicle_local_position_0.global_position_valid";
    private static final String GPOS_LAT = "vehicle_local_position_0.lat";
    private static final String GPOS_LON = "vehicle_local_position_0.lon";
    private static final String GPOS_ALT = "vehicle_local_position_0.alt";
    private static final String GPS_LAT = "vehicle_gps_position_0.lat";
    private static final String GPS_LON = "vehicle_gps_position_0.lon";
    private static final String GPS_ALT = "vehicle_gps_position_0.alt";
    private static final String VISN_X = "vehicle_odometry.x";
    private static final String VISN_Y = "vehicle_odometry.y";
    private static final String VISN_Z = "vehicle_odometry.z";
    private static final String REF_LAT = "vehicle_local_position_0.ref_lat";
    private static final String REF_LON = "vehicle_local_position_0.ref_lon";
    private static final String REF_ALT = "vehicle_local_position_0.ref_alt";
    private static final String GPSP_LAT = "position_setpoint_triplet_0.current.lat";
    private static final String GPSP_LON = "position_setpoint_triplet_0.current.lon";
    private static final String GPSP_ALT = "position_setpoint_triplet_0.current.alt";
    private static final String GPSP_TYPE = "position_setpoint_triplet_0.current.type";
    private static final String ATT_PITCH = "ATT.Pitch";
    private static final String ATT_ROLL = "ATT.Roll";
    private static final String ATT_YAW = "ATT.Yaw";
    private static final String MODE = "vehicle_status_0.nav_state";

    private String flightMode = null;

    private TrackPoint prev_setpoint = new TrackPoint(0, 0, 0, 0);
    GlobalPositionProjector positionProjector = new GlobalPositionProjector();

    public ULogTrackReader(ULogReader reader, TrackReaderConfiguration config) throws IOException, FormatErrorException {
        super(reader, config);
    }

    public TrackPoint readNextPoint() throws IOException, FormatErrorException {
        Map<String, Object> data = new HashMap<String, Object>();
        while (true) {
            data.clear();
            long t;
            try {
                t = readUpdate(data);
            } catch (EOFException e) {
                break;  // End of file
            }
            String currentFlightMode = getFlightMode(data);
            if (currentFlightMode != null) {
                flightMode = currentFlightMode;
            }
            Number valid = (Number) data.get(GPOS_VALID);
            Number lat = (Number) data.get(GPOS_LAT);
            Number lon = (Number) data.get(GPOS_LON);
            Number alt = (Number) data.get(GPOS_ALT);
            Number gpsLat = (Number) data.get(GPS_LAT);
            Number gpsLon = (Number) data.get(GPS_LON);
            Number gpsAlt = (Number) data.get(GPS_ALT);
            Number spLat = (Number) data.get(GPSP_LAT);
            Number spLon = (Number) data.get(GPSP_LON);
            Number spAlt = (Number) data.get(GPSP_ALT);
            Number spType = (Number) data.get(GPSP_TYPE);
            Number pitch = (Number) data.get(ATT_PITCH);
            Number roll = (Number) data.get(ATT_ROLL);
            Number heading = (Number) data.get(ATT_YAW);
            Number x = (Number) data.get(VISN_X);
            Number y = (Number) data.get(VISN_Y);
            Number z = (Number) data.get(VISN_Z);

            Number rlat = (Number) data.get(REF_LAT);
            Number rlon = (Number) data.get(REF_LON);
            Number ralt = (Number) data.get(REF_ALT);

            if (!positionProjector.isInited() && rlat != null && rlon != null && ralt != null) {
                LatLonAlt latLonAlt = new LatLonAlt(rlat.doubleValue(), rlon.doubleValue(), ralt.doubleValue());
                positionProjector.init(latLonAlt);
            }

            if (spLat != null && spLon != null && spAlt != null) {
                if (prev_setpoint.lat != spLat.doubleValue() || prev_setpoint.lon != spLon.doubleValue()) {
                    /*
                     * Return setpoint with previous coordinates but with current SP altitude since target altitude
                     * is slew-rates (FOH). Unfortunately we will miss the last setpoint of a mission.
                     */
                    TrackPoint point = new TrackPoint(prev_setpoint.lat, prev_setpoint.lon, spAlt.doubleValue(),
                            t + reader.getUTCTimeReferenceMicroseconds());
                    point.setpoint = true;
                    point.spType = spType.intValue();

                    prev_setpoint = new TrackPoint(spLat.doubleValue(), spLon.doubleValue(), spAlt.doubleValue(),
                            t + reader.getUTCTimeReferenceMicroseconds());
                    return point;
                }
            }

            if (valid != null && valid.intValue() != 0 && lat != null && lon != null && alt != null) {
                TrackPoint point = new TrackPoint(lat.doubleValue(), lon.doubleValue(), alt.doubleValue() + config.getAltitudeOffset(),
                        t + reader.getUTCTimeReferenceMicroseconds(), flightMode);

                if (pitch != null && roll != null && heading != null) {
                    point.radPitch = pitch.doubleValue();
                    point.radRoll = roll.doubleValue();
                    point.heading = heading.doubleValue();
                }

                if (gpsAlt != null && gpsLat != null && gpsLon != null) {
                    point.gpsAlt = gpsAlt.doubleValue();
                    point.gpsLat = gpsLat.doubleValue();
                    point.gpsLon = gpsLon.doubleValue();
                    point.hasGps = true;
                }

                if (positionProjector.isInited() && x != null && y != null && z != null) {
                    LatLonAlt latLonAlt = positionProjector.reproject(new double[] {x.doubleValue(), y.doubleValue(), z.doubleValue()});

                    point.visionAlt = latLonAlt.alt;
                    point.visionLat = latLonAlt.lat;
                    point.visionLon = latLonAlt.lon;
                    point.hasVision = true;
                }

                return point;
            }
        }
        return null;
    }

    private String getFlightMode(Map<String, Object> data) {
        Number flightMode = (Number) data.get(MODE);
        if (flightMode != null) {
            switch (flightMode.intValue()) {
                case 0:
                    return "MANUAL";
                case 1:
                    return "ALTCTL";
                case 2:
                    return "POSCTL";
                case 3:
                    return "AUTO_MISSION";
                case 4:
                    return "AUTO_LOITER";
                case 5:
                    return "AUTO_RTL";
                case 6:
                    return "ACRO";
                case 7:
                    return "AUTO_OFFBOARD";
                case 8:
                    return "STABILIZED";
                case 9:
                    return "RATTITUDE";
                case 10:
                    return "AUTO_TAKEOFF";
                case 11:
                    return "AUTO_LAND";
                case 12:
                    return "AUTO_DESCEND";
                case 13:
                    return "TERMINATION";
                case 14:
                    return "OFFBOARD";
                case 15:
                    return "STABILIZED";
                case 16:
                    return "RATTITUDE";
                case 17:
                    return "AUTO_TAKEOFF";
                default:
                    return String.format("UNKNOWN(%s)", flightMode.intValue());
            }
        }
        return null;    // Not supported
    }
}
