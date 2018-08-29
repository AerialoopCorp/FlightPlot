package me.drton.flightplot.processors;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by ada on 01.06.18.
 */
public class SensorHealth extends PlotProcessor {
    protected String paramField;
    private long sens = 0;
    private boolean init = false;
    private long defaultValue = 1;
    static private HashMap<String, HashMap<Long, String>> states = new LinkedHashMap<String, HashMap<Long, String>>();

    static {
        HashMap<Long, String> sensors = new LinkedHashMap<Long, String>();
        sensors.put(1l, "GYRO");
        sensors.put(2l, "ACCEL");
        sensors.put(4l, "MAG");
        sensors.put(8l, "ABSPRESSURE");
        sensors.put(16l, "DIFFPRESSURE");
        sensors.put(32l, "GPS");
        sensors.put(64l, "OPTICALFLOW");
        sensors.put(128l, "CVPOSITION");
        sensors.put(256l, "RANGEFINDER");
        sensors.put(512l, "EXTERNALGROUNDTRUTH");
        sensors.put(1024l, "ANGULARRATECONTROL");
        sensors.put(2048l, "ATTITUDESTABILIZATION");
        sensors.put(4096l, "YAWPOSITION");
        sensors.put(8192l, "ALTITUDECONTROL");
        sensors.put(16384l, "POSITIONCONTROL");
        sensors.put(32768l, "MOTOR_OUTPUTS");
        sensors.put(65536l, "RC_RECEIVER");
        sensors.put(131072l, "GYRO2");
        sensors.put(262144l, "ACCEL2");
        sensors.put(524288l, "MAG2");
        sensors.put(1048576l, "GEOFENCE");
        sensors.put(2097152l, "AHRS");
        sensors.put(4194304l, "TERRAIN");
        sensors.put(8388608l, "REVERSE_MOTOR");
        sensors.put(16777216l, "LOGGING");
        sensors.put(33554432l, "SENSOR_BATTERY");
        sensors.put(67108864l, "STORAGE");
        sensors.put(134217728l, "GYRO_CONSISTENT");
        sensors.put(268435456l, "ACCEL_CONSISTENT");
        //sensors.put(536870912l, "undefined");
        //sensors.put(1073741824l, "undefined");
        sensors.put(2147483648l, "SAFETY");
        states.put("HEAL.Sens", sensors);

        sensors = new LinkedHashMap<Long, String>();
        sensors.put(1l, "FW_ENGINE");
        sensors.put(2l, "VTOL_TRANSITION");
        sensors.put(4l, "MISSION");
        sensors.put(8l, "GPOS");
        sensors.put(16l, "ALTITUDE");
        sensors.put(32l, "MIN_ALTITUDE");
        sensors.put(64l, "AIRSPEED");
        sensors.put(128l, "BATTERY_LOW");
        sensors.put(256l, "BATTERY_CRITICAL");
        sensors.put(512l, "TERRAIN");
        sensors.put(1024l, "MANUAL_CONTROL");
        sensors.put(2048l, "DATA_LINK");
        states.put("HEAL.Fail", sensors);

        sensors = new LinkedHashMap<Long, String>();
        sensors.put(1l, "vel");
        sensors.put(2l, "hor pos");
        sensors.put(4l, "vert pos");
        sensors.put(8l, "x mag");
        sensors.put(16l, "y mag");
        sensors.put(32l, "z mag");
        sensors.put(64l, "yaw");
        sensors.put(128l, "airspeed");
        sensors.put(256l, "sideslip");
        sensors.put(512l, "hag");
        sensors.put(1024l, "x flow");
        sensors.put(2048l, "y flow");
        states.put("EST2.IC", sensors);

        sensors = new LinkedHashMap<Long, String>();
        sensors.put(1l, "CS_TILT_ALIGN");
        sensors.put(2l, "CS_YAW_ALIGN");
        sensors.put(4l, "CS_GPS");
        sensors.put(8l, "CS_OPT_FLOW");
        sensors.put(16l, "CS_MAG_HDG");
        sensors.put(32l, "CS_MAG_3D");
        sensors.put(64l, "CS_MAG_DEC");
        sensors.put(128l, "CS_IN_AIR");
        sensors.put(256l, "CS_WIND");
        sensors.put(512l, "CS_BARO_HGT");
        sensors.put(1024l, "CS_RNG_HGT");
        sensors.put(2048l, "CS_GPS_HGT");
        sensors.put(4096l, "CS_EV_POS");
        sensors.put(8192l, "CS_EV_YAW");
        sensors.put(16384l, "CS_EV_HGT");
        sensors.put(32768l, "CS_BETA");
        sensors.put(65536l, "CS_MAG_FIELD");
        sensors.put(131072l, "CS_FIXED_WING");
        sensors.put(262144l, "CS_MAG_FAULT");
        sensors.put(524288l, "CS_ASPD");
        sensors.put(1048576l, "CS_GND_EFFECT");
        sensors.put(2097152l, "CS_RNG_STUCK");
        states.put("EST2.CTRL", sensors);

        sensors = new LinkedHashMap<Long, String>();
        sensors.put(1l, "mag X-axis");
        sensors.put(2l, "mag Y-axis");
        sensors.put(4l, "mag Z-axis");
        sensors.put(8l, "mag head");
        sensors.put(16l, "mag decl");
        sensors.put(32l, "airspeed");
        sensors.put(64l, "sideslip");
        sensors.put(128l, "flow x");
        sensors.put(256l, "flow y");
        sensors.put(512l, "N vel");
        sensors.put(1024l, "E vel");
        sensors.put(2048l, "D vel");
        sensors.put(4096l, "N pos");
        sensors.put(8192l, "E pos");
        sensors.put(16384l, "D pos");
        sensors.put(32768l, "d vel bias");
        states.put("EST0.fFault", sensors);
    };

    @Override
    public Map<String, Object> getDefaultParameters() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("Field", "HEAL.Sens");
        params.put("Default", "1");
        return params;
    }

    @Override
    public void init() {
        paramField = (String) parameters.get("Field");
        defaultValue = Long.parseLong((String)parameters.get("Default"));
        addMarkersList();
    }

    @Override
    public void process(double time, Map<String, Object> update) {
        Object v = update.get(paramField);
        HashMap<Long, String> sensors = states.get(paramField);

        if (!init) {
            if (defaultValue == 1) {
                sens = Long.MAX_VALUE;
            } else {
                sens = 0;
            }
        }

        if (v != null && ((Number)v).longValue() != sens && sensors != null) {
            long temp = ((Number)v).longValue();
            long diff = sens ^ temp;

            StringBuffer marker = new StringBuffer();
            for (int a = 0; a < 32; a++) {
                boolean isDifferent = (diff & 1 << a) > 0;
                String sensor = sensors.get(1l << a);
                if (sensor == null) {
                    sensor = "unknown";
                }

                if (isDifferent) {
                    boolean val = (temp & 1 << a) > 0;
                    if (!init) {
                        // only add non-default flags on the first change
                        if (!val) {
                            marker.append(String.format("%s: %b | ", sensor, val));
                        }

                    } else {
                        // only mark changes
                        marker.append(String.format("%s: %b | ", sensor, val));
                    }
                }
            }

            if (marker.length() >= 3) {
                addMarker(0, time, marker.substring(0, marker.length() - 3));
            }
            sens = temp;
            init = true;
        }
    }
}
