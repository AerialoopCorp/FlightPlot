package me.drton.flightplot.export;

import me.drton.jmavlib.log.FormatErrorException;
import me.drton.jmavlib.log.px4.PX4LogReader;

import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ada on 23.12.13.
 */
public class PX4TrackReader extends AbstractTrackReader {
    private static final String GPOS_LAT = "GPOS.Lat";
    private static final String GPOS_LON = "GPOS.Lon";
    private static final String GPOS_ALT = "GPOS.Alt";
    private static final String GPSP_LAT = "GPSP.Lat";
    private static final String GPSP_LON = "GPSP.Lon";
    private static final String GPSP_ALT = "GPSP.Alt";
    private static final String GPSP_TYPE = "GPSP.Type";
    private static final String ATT_PITCH = "ATT.Pitch";
    private static final String ATT_ROLL = "ATT.Roll";
    private static final String ATT_YAW = "ATT.Yaw";
    private static final String STAT_MAINSTATE = "STAT.MainState";

    private TrackPoint prev_setpoint = new TrackPoint(0, 0, 0, 0);

    private String flightMode = null;

    public PX4TrackReader(PX4LogReader reader, TrackReaderConfiguration config) throws IOException, FormatErrorException {
        super(reader, config);
    }

    @Override
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
            Number lat = (Number) data.get(GPOS_LAT);
            Number lon = (Number) data.get(GPOS_LON);
            Number alt = (Number) data.get(GPOS_ALT);
            Number spLat = (Number) data.get(GPSP_LAT);
            Number spLon = (Number) data.get(GPSP_LON);
            Number spAlt = (Number) data.get(GPSP_ALT);
            Number spType = (Number) data.get(GPSP_TYPE);
            Number pitch = (Number) data.get(ATT_PITCH);
            Number roll = (Number) data.get(ATT_ROLL);
            Number heading = (Number) data.get(ATT_YAW);

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

            if (lat != null && lon != null && alt != null) {
                TrackPoint point = new TrackPoint(lat.doubleValue(), lon.doubleValue(), alt.doubleValue() + config.getAltitudeOffset(),
                        t + reader.getUTCTimeReferenceMicroseconds(), flightMode);

                if (pitch != null && roll != null && heading != null) {
                    point.radPitch = pitch.doubleValue();
                    point.radRoll = roll.doubleValue();
                    point.heading = heading.doubleValue();
                }

                return point;
            }
        }
        return null;
    }

    private String getFlightMode(Map<String, Object> data) {
        Number flightMode = (Number) data.get(STAT_MAINSTATE);
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
                    return "AUTO_ACRO";
                case 7:
                    return "AUTO_OFFBOARD";
                case 8:
                    return "STABILIZED";
                default:
                    return String.format("UNKNOWN(%s)", flightMode.intValue());
            }
        }
        return null;    // Not supported
    }
}
