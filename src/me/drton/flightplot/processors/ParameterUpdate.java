package me.drton.flightplot.processors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ada on 20.08.17.
 */
public class ParameterUpdate extends PlotProcessor {
    protected String paramField;
    protected String valueField;
    protected String typeField;

    @Override
    public Map<String, Object> getDefaultParameters() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("Field", "PARM.Name");
        params.put("Value Field", "PARM.Value");
        params.put("Type Field", "PARM.Type");
        return params;
    }

    @Override
    public void init() {
        paramField = (String) parameters.get("Field");
        valueField = (String) parameters.get("Value Field");
        typeField = (String) parameters.get("Type Field");
        addMarkersList();
    }

    @Override
    public void process(double time, Map<String, Object> update) {
        Object v = update.get("PARM");
        StringBuffer marker = new StringBuffer();

        if (v != null) {
            List<Map<String, Object>> store = (List<Map<String, Object>>)v;

            for (Map<String, Object>  params : store) {
                v = params.get(paramField);

                if (v != null && v instanceof String) {
                    boolean isFloat = true;
                    Object t = params.get(typeField);
                    if (t != null && t instanceof Number) {
                        isFloat = ((Number)t).equals(2);
                    }

                    Object n = params.get(valueField);
                    if (n != null && n instanceof Number) {
                        if (isFloat) {
                            marker.append(String.format("%s: %s | ", v, n.toString()));
                        } else {
                            if (n instanceof Integer) {
                                // ULG ints are ints
                                marker.append(String.format("%s: %d | ", v, ((Integer)n)));
                            } else {
                                // PX4LOG ints are floats
                                marker.append(String.format("%s: %d | ", v, ((Float)n).intValue()));
                            }

                        }
                    }
                }
            }
        }

        if (marker.length() >= 3) {
            addMarker(0, time, marker.substring(0, marker.length() - 3));
        }
    }
}
