package me.drton.flightplot.processors;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by ada on 01.06.18.
 */
public class SensorHealth extends PlotProcessor {
    protected String paramField;
    HashMap<Long, String> sensors;
    private long sens = 0;

    @Override
    public Map<String, Object> getDefaultParameters() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("Field", "HEAL.Sens");
        return params;
    }

    @Override
    public void init() {
        paramField = (String) parameters.get("Field");
        addMarkersList();
        sensors = new LinkedHashMap<Long, String>();
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
        sensors.put(536870912l, "undefined");
        sensors.put(1073741824l, "undefined");
        sensors.put(2147483648l, "SAFETY");
    }

    @Override
    public void process(double time, Map<String, Object> update) {
        Object v = update.get(paramField);
        if (v != null && ((Number)v).longValue() != sens) {
            long temp = ((Number)v).longValue();
            long diff = sens ^ temp;
            if (sens == 0) {
                diff = Long.MAX_VALUE;
            }
            for (int a = 0; a < 32; a++) {
                boolean val = (diff & 1 << a) > 0;
                if (val) {
                    if (sens == 0) {
                        System.out.println(String.format("%s: %b", sensors.get(1l << a), (temp & 1 << a) > 0));

                    } else {
                        // only mark changes
                        addMarker(0, time, String.format("%s: %b", sensors.get(1l << a), (temp & 1 << a) > 0));
                    }
                }
            }
            sens = temp;
        }
    }
}
