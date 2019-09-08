package me.drton.flightplot.processors;

import java.util.HashMap;
import java.util.Map;

/**
 * User: ton Date: 16.06.13 Time: 12:59
 */
public class DistanceToSetpoint extends PlotProcessor {
    protected String[] param_Pos;
    protected String[] param_Target;
    protected double param_Scale;
    private double[] vecPos = new double[3];
    private double[] vecTarget = new double[3];
    private int distanceSeriesIndex = 0;

    @Override
    public Map<String, Object> getDefaultParameters() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("Pos", "LPOS.X LPOS.Y LPOS.Z");
        params.put("Target", "LPSP.X LPSP.Y LPSP.Z");
        params.put("Scale", 1.0);
        return params;
    }

    @Override
    public void init() {
        param_Pos = ((String) parameters.get("Pos")).split((WHITESPACE_RE));
        param_Target = ((String) parameters.get("Target")).split((WHITESPACE_RE));
        param_Scale = (Double) parameters.get("Scale");
        distanceSeriesIndex = addSeries("Distance");
    }

    @Override
    public void process(double time, Map<String, Object> update) {
        double distance = 0;

        if (param_Pos.length < 2 || param_Pos.length != param_Target.length) {
            return;
        }

        Object v = null;

        for (int i = 0; i < param_Pos.length; i++) {
            v = update.get(param_Pos[i]);
            if (v != null && v instanceof Number) {
                vecPos[i] = ((Number) v).doubleValue();
            }
        }

        for (int i = 0; i < param_Target.length; i++) {
            v = update.get(param_Target[i]);
            if (v != null && v instanceof Number) {
                vecTarget[i] = ((Number) v).doubleValue();
            }
        }

        distance = Math.pow(vecTarget[0] - vecPos[0], 2) + Math.pow(vecTarget[1] - vecPos[1], 2);

        if (param_Pos.length == 3) {
            distance += Math.pow(vecTarget[2] - vecPos[2], 2);
        }

        distance = Math.sqrt((distance));

        addPoint(distanceSeriesIndex, time, distance * param_Scale);
    }
}
