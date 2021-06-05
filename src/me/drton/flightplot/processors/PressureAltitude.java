package me.drton.flightplot.processors;

import me.drton.flightplot.processors.tools.LowPassFilter;

import java.util.HashMap;
import java.util.Map;

/**
 * User: ton Date: 15.06.13 Time: 12:04
 */
public class PressureAltitude extends PlotProcessor {
    protected String param_Pressure;
    protected String param_TempField;
    protected double param_QNH;
    protected double param_Scale;
    protected double param_Offset;
    protected double param_Delay;

    private double temp = 15.0;

    private static float CONSTANTS_ABSOLUTE_NULL_CELSIUS = -273.15f;
    private static float CONSTANTS_AIR_GAS_CONST = 287.1f;
    private static float CONSTANTS_ONE_G = 9.80665f;

    @Override
    public Map<String, Object> getDefaultParameters() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("Pressure", "ADA.pres");
        params.put("Temp", 15.0);
        params.put("Temp Field", "ignore");
        params.put("QNH", 1013.25);
        params.put("Delay", 0.0);
        params.put("LPF", 0.0);
        params.put("Scale", 1.0);
        params.put("Offset", 0.0);
        return params;
    }

    @Override
    public void init() {
        param_Pressure = (String) parameters.get("Pressure");
        param_TempField = (String) parameters.get("Temp Field");
        param_QNH = (Double) parameters.get("QNH");
        param_Scale = (Double) parameters.get("Scale");
        param_Offset = (Double) parameters.get("Offset");
        param_Delay = (Double) parameters.get("Delay");
        temp = (Double) parameters.get("Temp");

        addSeries("Altitude");
    }

    protected double preProcessValue(int idx, double time, double in) {
        return in;
    }

    protected double postProcessValue(int idx, double time, double in) {
        return in;
    }

    @Override
    public void process(double time, Map<String, Object> update) {
        Object v = update.get(param_TempField);
        if (v != null && v instanceof Number) {
            temp = ((Number) v).doubleValue();
        }

        v = update.get(param_Pressure);
        if (v != null && v instanceof Number) {
            double pressure = ((Number) v).doubleValue();

            double T1 = temp - CONSTANTS_ABSOLUTE_NULL_CELSIUS;	/* temperature at base height in Kelvin */
            double a  = -6.5f / 1000.0f;	/* temperature gradient in degrees per metre */

			/* current pressure at MSL in kPa (QNH in hPa)*/
            double p1 = (float)param_QNH * 0.1f;

			/* measured pressure in kPa */
            double p = (float)pressure * 0.001f;

			/*
			 * Solve:
			 *
			 *     /        -(aR / g)     \
			 *    | (p / p1)          . T1 | - T1
			 *     \                      /
			 * h = -------------------------------  + h1
			 *                   a
			 */
            double altitude = (((Math.pow((p / p1), (-(a * CONSTANTS_AIR_GAS_CONST) / CONSTANTS_ONE_G))) * T1) - T1) / a;



            addPoint(0, time + param_Delay, altitude * param_Scale + param_Offset);
        }


    }
}
