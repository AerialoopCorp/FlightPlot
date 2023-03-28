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
    private String param_baro_press_pa;
    private String param_temp;
    private double param_dp_offset;
    private double param_dp_scale;
    private double baro_press;
    private double temp;
    private int model;
    private double tube_dia_mm;
    private double tube_len;
    private double diff_press;

    @Override
    public Map<String, Object> getDefaultParameters() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("Diff Press", "DPRS.DPRES");
        params.put("DP Offset", 0.0);
        params.put("DP Scale", 2.0);
        params.put("Baro Press mbar", "SENS.BaroPres");
        params.put("Baro Press pa", "ADA.pres");
        params.put("Temp", "DPRS.Temp");
        params.put("Model", 3);
        params.put("Tube Dia mm", 1.5);
        params.put("Tube Len", 0.3);
        return params;
    }

    @Override
    public void init() {
        param_diff_press = ((String) parameters.get("Diff Press"));
        param_dp_offset = (Double) parameters.get("DP Offset");
        param_dp_scale = (Double) parameters.get("DP Scale");
        param_baro_press = ((String) parameters.get("Baro Press mbar"));
        param_baro_press_pa = ((String) parameters.get("Baro Press pa"));
        param_temp = ((String) parameters.get("Temp"));
        model = ((Integer) parameters.get("Model"));
        tube_dia_mm = (Double) parameters.get("Tube Dia mm");
        tube_len = (Double) parameters.get("Tube Len");
        addSeries("IAS");
        addSeries("TAS");
    }

    @Override
    public void process(double time, Map<String, Object> update) {
        double press = 0;

        Number v = (Number) update.get(param_diff_press);
        if (v != null) {
            diff_press = Math.max(0.0, v.doubleValue() + param_dp_offset);
        }

        press = diff_press;

        v = (Number) update.get(param_baro_press);
        if (v != null) {
            baro_press = v.doubleValue() * 1e2;
        }

        v = (Number) update.get(param_baro_press_pa);
        if (v != null) {
            baro_press = v.doubleValue();
        }

        v = (Number) update.get(param_temp);
        if (v != null) {
            temp = v.doubleValue();
        }

        double density = baro_press / (287.1 * (temp - -273.15));
        double airspeed_correction = 0.0;

        if (density < Float.MIN_NORMAL) {
            return;
        }

        if (model == 0 || model == 1) {
            double dp_corr = press * 96600.0 / baro_press;
            // flow through sensor
            double flow_SDP33 = (300.805 - 300.878 / (0.00344205 * Math.pow(dp_corr, 0.68698) + 1.0)) * 1.29 / density;

            // for too small readings the compensation might result in a negative flow which causes numerical issues
            if (flow_SDP33 < 0.0) {
                flow_SDP33 = 0.0;
            }

            double dp_pitot = 0.0;

            if (model == 0) {
                    dp_pitot = (0.0032 * flow_SDP33 * flow_SDP33 + 0.0123 * flow_SDP33 + 1.0) * 1.29 / density;
            }

            // pressure drop through tube
            // convert tube length to mm
            double dp_tube = (flow_SDP33 * 0.674) / 450.0 * tube_len * 1e3 * density / 1.29;

            // speed at pitot-tube tip due to flow through sensor
            airspeed_correction = 0.125 * flow_SDP33;

            // sum of all pressure drops
            press = dp_corr + dp_tube + dp_pitot;
        }

        if (model == 2) {
            double d_tubePow4 = Math.pow(tube_dia_mm * 1e-3, 4);
            double denominator = Math.PI * d_tubePow4 * density * press;

            // avoid division by 0
            double eps = 0.0;

            if (Math.abs(denominator) > 1e-32) {
                double viscosity = (18.205 + 0.0484 * (temp - 20.0)) * 1e-6;

                // 4.79 * 1e-7 -> mass flow through sensor
                // 59.5 -> dp sensor constant where linear and quadratic contribution to dp vs flow is equal
                eps = -64.0 * tube_len * viscosity * 4.79 * 1e-7 * (Math.sqrt(1.0 + 8.0 * press / 59.3319) - 1.0) / denominator;
            }

            // range check on eps
            if (Math.abs(eps) >= 1.0) {
                eps = 0.0;
            }

            // pressure correction
            press = press / (1.0 + eps);
        }

        double ias = Math.sqrt((param_dp_scale * press) / 1.225) + airspeed_correction;
        addPoint(0, time, ias);

        double tas = ias * Math.sqrt(1.225 / density);
        addPoint(1, time, tas);
    }
}
