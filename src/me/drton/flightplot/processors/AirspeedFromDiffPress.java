package me.drton.flightplot.processors;

import me.drton.jmavlib.conversion.RotationConversion;

import javax.vecmath.Matrix3d;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ton on 05.01.15.
 */
public class AirspeedFromDiffPress extends PlotProcessor {
    private String param_diff_press;
    private String param_baro_press;
    private String param_temp;
    private double param_dp_offset;
    private double param_dp_scale;
    private double baro_press;
    private double temp;

    @Override
    public Map<String, Object> getDefaultParameters() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("Diff Press", "DPRS.DPRES");
        params.put("DP Offset", 0.0);
        params.put("DP Scale", 2.0);
        params.put("Baro Press", "SENS.BaroPres");
        params.put("Temp", "DPRS.Temp");
        return params;
    }

    @Override
    public void init() {
        param_diff_press = ((String) parameters.get("Diff Press"));
        param_dp_offset = (Double) parameters.get("DP Offset");
        param_dp_scale = (Double) parameters.get("DP Scale");
        param_baro_press = ((String) parameters.get("Baro Press"));
        param_temp = ((String) parameters.get("Temp"));
        addSeries("IAS");
        addSeries("TAS");
    }

    @Override
    public void process(double time, Map<String, Object> update) {
        double press = 0;
        Number v = (Number) update.get(param_diff_press);
        if (v == null) {
            return;
        }

        press = Math.max(0.0, v.doubleValue() + param_dp_offset);

        v = (Number) update.get(param_baro_press);
        if (v != null) {
            baro_press = v.doubleValue() * 1e2;
        }

        v = (Number) update.get(param_temp);
        if (v != null) {
            temp = v.doubleValue();
        }

        double ias = Math.sqrt((param_dp_scale * press) / 1.225);
        addPoint(0, time, ias);

        double density = baro_press / (287.1 * (temp - -273.15));
        double tas = Math.sqrt((param_dp_scale * press) / density);
        addPoint(1, time, tas);
    }
}
