package me.drton.flightplot.processors;

import java.util.HashMap;
import java.util.Map;

/**
 * User: ton Date: 16.06.13 Time: 12:59
 */
public class MavlinkPX4CustomMode extends PlotProcessor {
    protected String param_Field;

    /*
     * PX4_CUSTOM_MAIN_MODE_MANUAL = 1,
	 * PX4_CUSTOM_MAIN_MODE_ALTCTL,
	 * PX4_CUSTOM_MAIN_MODE_POSCTL,
	 * PX4_CUSTOM_MAIN_MODE_AUTO,
	 * PX4_CUSTOM_MAIN_MODE_ACRO,
	 * PX4_CUSTOM_MAIN_MODE_OFFBOARD,
	 * PX4_CUSTOM_MAIN_MODE_STABILIZED
     */
    private static int[] mainModeMap = {-1, 0, 1, 2, -99, 10, 14, -1};

    /*
     * PX4_CUSTOM_SUB_MODE_AUTO_READY = 1,
	 * PX4_CUSTOM_SUB_MODE_AUTO_TAKEOFF,
	 * PX4_CUSTOM_SUB_MODE_AUTO_LOITER,
	 * PX4_CUSTOM_SUB_MODE_AUTO_MISSION,
	 * PX4_CUSTOM_SUB_MODE_AUTO_RTL,
	 * PX4_CUSTOM_SUB_MODE_AUTO_LAND,
	 * PX4_CUSTOM_SUB_MODE_AUTO_RTGS
     */
    private static int[] autoModeMap = {-1, -1, 17, 4, 3, 5, 11, 7};

    @Override
    public Map<String, Object> getDefaultParameters() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("Field", "M1:HEARTBEAT.custom_mode");
        return params;
    }

    @Override
    public void init() {
        param_Field = (String) parameters.get("Field");
        addSeries();
    }

    @Override
    public void process(double time, Map<String, Object> update) {
        int mode = -1;

        Object v = update.get(param_Field);

        if (v != null && v instanceof Number) {
            long l = ((Number) v).longValue();
            int mainMode = (int)((l >> 16) & 0xff);
            int subMode = (int)((l >> 24) & 0xff);

            if (mainMode < mainModeMap.length) {
                mode = mainModeMap[mainMode];
            }

            if (mode == -99 && subMode < autoModeMap.length) {
                mode = autoModeMap[subMode];
            }
        } else {
            return;
        }

        addPoint(0, time, mode);
    }
}
