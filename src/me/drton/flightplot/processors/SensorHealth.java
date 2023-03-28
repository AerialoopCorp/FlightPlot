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
        sensors.put(1048576l, "GEOFENCE (DEPRECATED)");
        sensors.put(2097152l, "AHRS");
        sensors.put(4194304l, "TERRAIN");
        sensors.put(8388608l, "REVERSE_MOTOR");
        sensors.put(16777216l, "LOGGING (DEPRECATED)");
        sensors.put(33554432l, "SENSOR_BATTERY");
        sensors.put(67108864l, "PROXIMITY");
        sensors.put(134217728l, "SATCOM");
        sensors.put(268435456l, "PREARM_CHECK");
        sensors.put(536870912l, "OBSTACLE_AVOIDANCE");
        //sensors.put(1073741824l, "not defined");
        sensors.put(2147483648l, "SAFETY (DEPRECATED)");
        sensors.put(4294967296l, "GYRO_CONSISTENT");
        sensors.put(8589934592l, "ACCEL_CONSISTENT");
        sensors.put(17179869184l, "MAG_CONSISTENT");
        sensors.put(34359738368l, "AVIONICS_POWER");
        sensors.put(68719476736l, "STORAGE");
        sensors.put(137438953472l, "BGPS");
        sensors.put(274877906944l, "COMPANION");
        sensors.put(549755813888l, "ADSB");
        sensors.put(1099511627776l, "GENERATOR");
        sensors.put(2199023255552l, "RC_CALIBRATION");
        sensors.put(4398046511104l, "PREC_LAND");
        sensors.put(8796093022208l, "NTRIP");
        sensors.put(17592186044416l, "BATTERY2");
        states.put("HEAL.Sens", sensors);
        states.put("vehicle_status_0.onboard_control_sensors_health", sensors);

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
        sensors.put(4096l, "VOTED_SENSOR");
        sensors.put(8192l, "MISSION_INVALID");
        sensors.put(16384l, "GEOFENCE");
        sensors.put(32768l, "BATTERY2_LOW");
        sensors.put(65536l, "BATTERY2_CRITICAL");
        sensors.put(131072l, "STATUS_CORRUPT");
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
        states.put("estimator_status_0.innovation_check_flags", sensors);

        sensors = new LinkedHashMap<Long, String>();
        sensors.put(1l, "TILT_ALIGN");
        sensors.put(2l, "YAW_ALIGN");
        sensors.put(4l, "GPS");
        sensors.put(8l, "OPT_FLOW");
        sensors.put(16l, "MAG_HDG");
        sensors.put(32l, "MAG_3D");
        sensors.put(64l, "MAG_DEC");
        sensors.put(128l, "IN_AIR");
        sensors.put(256l, "WIND");
        sensors.put(512l, "BARO_HGT");
        sensors.put(1024l, "RNG_HGT");
        sensors.put(2048l, "GPS_HGT");
        sensors.put(4096l, "EV_POS");
        sensors.put(8192l, "EV_YAW");
        sensors.put(16384l, "EV_HGT");
        sensors.put(32768l, "BETA");
        sensors.put(65536l, "MAG_FIELD");
        sensors.put(131072l, "FIXED_WING");
        sensors.put(262144l, "MAG_FAULT");
        sensors.put(524288l, "ASPD");
        sensors.put(1048576l, "GND_EFFECT");
        sensors.put(2097152l, "RNG_STUCK");
        sensors.put(4194304l, "GPS_YAW");
        sensors.put(8388608l, "MAG_ALIGN");
        states.put("EST2.CTRL", sensors);
        states.put("estimator_status_0.control_mode_flags", sensors);

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
        states.put("estimator_status_0.filter_fault_flags", sensors);

        sensors = new LinkedHashMap<Long, String>();
        sensors.put(1l, "OFF");
        sensors.put(2l, "READY");
        sensors.put(4l, "GENERATING");
        sensors.put(8l, "CHARGING");
        sensors.put(16l, "REDUCED_POWER");
        sensors.put(32l, "MAXPOWER");
        sensors.put(64l, "OVERTEMP_WARNING");
        sensors.put(128l, "OVERTEMP_FAULT");
        sensors.put(256l, "ELECTRONICS_OVERTEMP_WARNING");
        sensors.put(512l, "ELECTRONICS_OVERTEMP_FAULT");
        sensors.put(1024l, "ELECTRONICS_FAULT");
        sensors.put(2048l, "POWERSOURCE_FAULT");
        sensors.put(4096l, "COMMUNICATION_WARNING");
        sensors.put(8192l, "COOLING_WARNING");
        sensors.put(16384l, "POWER_RAIL_FAULT");
        sensors.put(32768l, "OVERCURRENT_FAULT");
        sensors.put(65536l, "BATTERY_OVERCHARGE_CURRENT_FAULT");
        sensors.put(131072l, "OVERVOLTAGE_FAULT");
        sensors.put(262144l, "BATTERY_UNDERVOLT_FAULT");
        sensors.put(524288l, "START_INHIBITED");
        sensors.put(1048576l, "MAINTENANCE_REQUIRED");
        sensors.put(2097152l, "WARMING_UP");
        sensors.put(4194304l, "IDLE");
        states.put("GEN.status", sensors);
        states.put("generator_status_0.status", sensors);

        sensors = new LinkedHashMap<Long, String>();
        sensors.put(1l, "TIMEOUT");
        sensors.put(2l, "MIN_ALT");
        sensors.put(4l, "AIRSPEED");
        sensors.put(8l, "none");
        sensors.put(16l, "Z_VEL");
        sensors.put(32l, "Z_ACC");
        sensors.put(64l, "ATT");
        sensors.put(128l, "PUSHER");
        sensors.put(256l, "YAW");
        sensors.put(512l, "MC_ATT");
        sensors.put(1024l, "MC_YAW");
        sensors.put(2048l, "MC_Z_VEL");
        sensors.put(4096l, "MC_ALT");
        states.put("VTOL.FsMask", sensors);
        states.put("vtol_vehicle_status_0.failsafe_mask", sensors);

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
            for (int a = 0; a < 64; a++) {
                long shifted = 1l << a;
                boolean isDifferent = (diff & shifted) > 0l;
                String sensor = sensors.get(shifted);
                if (sensor == null) {
                    sensor = String.valueOf(a);
                }

                if (isDifferent) {
                    boolean val = (temp & shifted) > 0;
                    marker.append(String.format("%s: %b | ", sensor, val));
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
