package me.drton.flightplot.processors;

import me.drton.jmavlib.conversion.RotationConversion;

import javax.vecmath.Matrix3d;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ton on 05.01.15.
 */
public class TecsAnalysis extends PlotProcessor {
    private double param_Scale;
    private double param_thrTimeConst;
    private double param_timeConst;
    private double param_thrDamp;
    private double param_pitchDamp;
    private double param_thrCruise;
    private double param_thrMax;
    private double param_thrMin;
    private double param_climbMax;
    private double param_sinkMax;
    private double param_rollComp;
    private double roll;
    private static double CONSTANTS_ONE_G = 9.80665;

    @Override
    public Map<String, Object> getDefaultParameters() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("Show", "_SPE_dem _SKE_dem _SPEdot_dem _SKEdot_dem _SPE_est _SKE_est _SPEdot _SKEdot ff_throttle pd_throttle dem_throttle");
        params.put("Scale", 1.0);
        params.put("Thr Damp", 0.5);
        params.put("Thr Time Const", 8.0);
        params.put("Time Const", 5.0);
        params.put("Pitch Damp", 0.0);
        params.put("Thr Max", 1.0);
        params.put("Thr Min", 0.0);
        params.put("Thr Cruise", 0.5);
        params.put("Climb Max", 3.0);
        params.put("Sink Max", 3.0);
        params.put("Roll Comp", 15.0);
        return params;
    }

    @Override
    public void init() {
        param_Scale = (Double) parameters.get("Scale");
        param_thrDamp = (Double) parameters.get("Thr Damp");
        param_thrTimeConst = (Double) parameters.get("Thr Time Const");
        param_pitchDamp = (Double) parameters.get("Pitch Damp");
        param_timeConst = (Double) parameters.get("Time Const");
        param_thrMax = (Double) parameters.get("Thr Max");
        param_thrMin = (Double) parameters.get("Thr Min");
        param_thrCruise = (Double) parameters.get("Thr Cruise");
        param_climbMax = (Double) parameters.get("Climb Max");
        param_sinkMax = (Double) parameters.get("Sink Max");
        param_rollComp = (Double) parameters.get("Roll Comp");
        String[] showStr = ((String) parameters.get("Show")).split(WHITESPACE_RE);
        for (int axis = 0; axis < showStr.length; axis++) {
            String axisName = showStr[axis];
            addSeries(axisName);
        }
    }

    @Override
    public void process(double time, Map<String, Object> update) {
        double _hgt_dem_adj = 0;
        double _TAS_dem_adj = 0;
        double _hgt_rate_dem = 0;
        double _est_airspeed = 0;
        double _TAS_rate_dem = 0;
        double _est_height = 0;
        double _est_height_rate = 0;
        double _vel_dot = 0;
        double throttle_int = 0;
        double energy_error = 0;
        double energy_rate_error = 0;

        Object temp = update.get("ATT.Roll");
        if (temp != null && temp instanceof Number) {
            roll = ((Number)temp).doubleValue();
        }

        temp = update.get("TECS.ASP");
        if (temp != null && temp instanceof Number) {
            _hgt_dem_adj = ((Number)temp).doubleValue();
        } else {
            return;
        }
        temp = update.get("TECS.AsSP");
        if (temp != null && temp instanceof Number) {
            _TAS_dem_adj = ((Number)temp).doubleValue();
        } else {
            return;
        }
        temp = update.get("TECS.FSP");
        if (temp != null && temp instanceof Number) {
            _hgt_rate_dem = ((Number)temp).doubleValue();
        } else {
            return;
        }
        temp = update.get("TECS.AsF");
        if (temp != null && temp instanceof Number) {
            _est_airspeed = ((Number)temp).doubleValue();
        } else {
            return;
        }
        temp = update.get("TECS.AsDSP");
        if (temp != null && temp instanceof Number) {
            _TAS_rate_dem = ((Number)temp).doubleValue();
        } else {
            return;
        }
        temp = update.get("TECS.AF");
        if (temp != null && temp instanceof Number) {
            _est_height = ((Number)temp).doubleValue();
        } else {
            return;
        }
        temp = update.get("TECS.F");
        if (temp != null && temp instanceof Number) {
            _est_height_rate = ((Number)temp).doubleValue();
        } else {
            return;
        }
        temp = update.get("TECS.AsD");
        if (temp != null && temp instanceof Number) {
            _vel_dot = ((Number)temp).doubleValue();
        } else {
            return;
        }
        temp = update.get("TECS.TI");
        if (temp != null && temp instanceof Number) {
            throttle_int = ((Number)temp).doubleValue();
        } else {
            return;
        }
        temp = update.get("TECS.EE");
        if (temp != null && temp instanceof Number) {
            energy_error = ((Number)temp).doubleValue();
        } else {
            return;
        }
        temp = update.get("TECS.ERE");
        if (temp != null && temp instanceof Number) {
            energy_rate_error = ((Number)temp).doubleValue();
        } else {
            return;
        }

        double _SPE_dem = _hgt_dem_adj * CONSTANTS_ONE_G;
        double _SKE_dem = 0.5 * _TAS_dem_adj * _TAS_dem_adj;
        double _SPEdot_dem = _hgt_rate_dem * CONSTANTS_ONE_G;
        double _SKEdot_dem = _est_airspeed * _TAS_rate_dem;
        double _SPE_est = _est_height * CONSTANTS_ONE_G;
        double _SKE_est = 0.5 * _est_airspeed * _est_airspeed;
        double _SPEdot = _est_height_rate * CONSTANTS_ONE_G;
        double _SKEdot = _est_airspeed * _vel_dot;

        double _STEdot_max = param_climbMax * CONSTANTS_ONE_G;
        double _STEdot_min = -param_sinkMax * CONSTANTS_ONE_G;
        double STEdot_dem = Math.min(Math.max(_SPEdot_dem + _SKEdot_dem, _STEdot_min), _STEdot_max);
        double _rollComp = param_rollComp;
        double _STEdot_error = STEdot_dem - _SPEdot - _SKEdot;
        // FIXME: _STEdot_error needs to be low-passed

        double ff_throttle = 0;
        double cosPhi = Math.cos(roll);
        STEdot_dem = STEdot_dem + _rollComp * (1.0 / Math.min(Math.max(cosPhi , 0.1), 1.0) - 1.0);
        if (STEdot_dem >= 0) {
            ff_throttle = param_thrCruise + STEdot_dem / _STEdot_max * (param_thrMax - param_thrCruise);

        } else {
            ff_throttle = param_thrCruise - STEdot_dem / _STEdot_min * (param_thrCruise - param_thrMin);
        }

        double K_STE2Thr = 1.0 / (param_thrTimeConst * (_STEdot_max - _STEdot_min));
        double pd_throttle = (energy_error + energy_rate_error * param_thrDamp) * K_STE2Thr;

        String[] showStr = ((String) parameters.get("Show")).split(" ");
        int plot_idx = 0;
        for (int axis = 0; axis < showStr.length; axis++) {
            String axisName = showStr[axis];
            double value = 0;

            if ("roll".equals(axisName)) {
                value = roll;
            }

            if ("_SPE_err".equals(axisName)) {
                value = _SPE_dem - _SPE_est;
            }

            if ("_SKE_err".equals(axisName)) {
                value = _SKE_dem - _SKE_est;
            }

            if ("_STE_err".equals(axisName)) {
                value = _SPE_dem - _SPE_est + _SKE_dem - _SKE_est;
            }

            if ("_STEdot_dem".equals(axisName)) {
                value = STEdot_dem;
            }

            if ("_STEdot_error".equals(axisName)) {
                value = _STEdot_error;
            }

            if ("_SPE_dem".equals(axisName)) {
                value = _SPE_dem;
            }
            if ("_SKE_dem".equals(axisName)) {
                value = _SKE_dem;
            }
            if ("_SPEdot_dem".equals(axisName)) {
                value = _SPEdot_dem;
            }
            if ("_SKEdot_dem".equals(axisName)) {
                value = _SKEdot_dem;
            }
            if ("_SPE_est".equals(axisName)) {
                value = _SPE_est;
            }
            if ("_SKE_est".equals(axisName)) {
                value = _SKE_est;
            }
            if ("_SPEdot".equals(axisName)) {
                value = _SPEdot;
            }
            if ("_SKEdot".equals(axisName)) {
                value = _SKEdot;
            }
            if ("ff_throttle".equals(axisName)) {
                value = ff_throttle;
            }
            if ("pd_throttle".equals(axisName)) {
                value = pd_throttle;
            }
            if ("dem_throttle".equals(axisName)) {
                value = ff_throttle + pd_throttle + throttle_int;
            }

            addPoint(plot_idx++, time, value * param_Scale);
        }
    }
}
