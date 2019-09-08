package me.drton.flightplot.export;

import javax.sound.midi.Track;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by ada on 23.12.13.
 */
public class KMLTrackExporter extends AbstractTrackExporter {
    private static final String LINE_STYLE_RED = "red";
    private static final String LINE_STYLE_GREEN = "green";
    private static final String LINE_STYLE_BLUE = "blue";
    private static final String LINE_STYLE_CYAN = "cyan";
    private static final String LINE_STYLE_MAGENTA = "magenta";
    private static final String LINE_STYLE_YELLOW = "yellow";
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    protected String getStyleForFlightMode(String flightMode) {
        if (flightMode == null) {
            return LINE_STYLE_YELLOW;
        }
        if ("MANUAL".equals(flightMode)) {
            return LINE_STYLE_RED;
        } else if ("STABILIZED".equals(flightMode)) {
            return LINE_STYLE_RED;
        } else if ("ACRO".equals(flightMode)) {
            return LINE_STYLE_RED;
        } else if ("ALTCTL".equals(flightMode)) {
            return LINE_STYLE_YELLOW;
        } else if ("POSCTL".equals(flightMode)) {
            return LINE_STYLE_GREEN;
        } else if ("AUTO_MISSION".equals(flightMode)) {
            return LINE_STYLE_BLUE;
        } else if ("AUTO_LOITER".equals(flightMode)) {
            return LINE_STYLE_CYAN;
        } else if ("AUTO_RTL".equals(flightMode)) {
            return LINE_STYLE_MAGENTA;
        } else if ("AUTO_LAND".equals(flightMode)) {
            return LINE_STYLE_BLUE;
        } else if ("AUTO_TAKEOFF".equals(flightMode)) {
            return LINE_STYLE_BLUE;
        } else if ("AUTO_OFFBOARD".equals(flightMode)) {
            return LINE_STYLE_BLUE;
        } else {
            return LINE_STYLE_RED;
        }
    }

    protected void writeStart() throws IOException {
        // TODO: maybe make some settings configurable
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        writer.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\">\n");
        writer.write("<Document>\n");
        writer.write("<name>" + this.title + "</name>\n");
        writer.write("<description></description>\n");
        writer.write("<Style id=\"" + LINE_STYLE_YELLOW + "\">\n");
        writer.write("<LineStyle>\n");
        writer.write("<color>7f00ffff</color>\n");
        writer.write("<width>4</width>\n");
        writer.write("</LineStyle>\n");
        writer.write("</Style>\n");
        writer.write("<Style id=\"" + LINE_STYLE_BLUE + "\">\n");
        writer.write("<LineStyle>\n");
        writer.write("<color>7fff0000</color>\n");
        writer.write("<width>4</width>\n");
        writer.write("</LineStyle>\n");
        writer.write("</Style>\n");
        writer.write("<Style id=\"" + LINE_STYLE_YELLOW + "\">\n");
        writer.write("<LineStyle>\n");
        writer.write("<color>7fffff00</color>\n");
        writer.write("<width>4</width>\n");
        writer.write("</LineStyle>\n");
        writer.write("</Style>\n");
        writer.write("<Style id=\"" + LINE_STYLE_GREEN + "\">\n");
        writer.write("<LineStyle>\n");
        writer.write("<color>7f00ff00</color>\n");
        writer.write("<width>4</width>\n");
        writer.write("</LineStyle>\n");
        writer.write("</Style>\n");
        writer.write("<Style id=\"" + LINE_STYLE_CYAN + "\">\n");
        writer.write("<LineStyle>\n");
        writer.write("<color>7f00ffff</color>\n");
        writer.write("<width>4</width>\n");
        writer.write("</LineStyle>\n");
        writer.write("</Style>\n");
        writer.write("<Style id=\"" + LINE_STYLE_MAGENTA + "\">\n");
        writer.write("<LineStyle>\n");
        writer.write("<color>7fff00ff</color>\n");
        writer.write("<width>4</width>\n");
        writer.write("</LineStyle>\n");
        writer.write("</Style>\n");
        writer.write("<Style id=\"" + LINE_STYLE_RED + "\">\n");
        writer.write("<LineStyle>\n");
        writer.write("<color>7f0000ff</color>\n");
        writer.write("<width>4</width>\n");
        writer.write("</LineStyle>\n");
        writer.write("</Style>\n");
        writer.write("<Style id=\"setpoint\">\n");
        writer.write("<LineStyle>\n");
        writer.write("<color>7fffffff</color>\n");
        writer.write("<width>6</width>\n");
        writer.write("</LineStyle>\n");
        writer.write("</Style>\n");
    }

    @Override
    protected void writeTrackPartStart(String trackPartName) throws IOException {
        String styleId = getStyleForFlightMode(flightMode);
        writer.write("<Placemark>\n");
        writer.write("<name>" + trackPartName + "</name>\n");
        writer.write("<description></description>\n");
        writer.write("<styleUrl>#" + styleId + "</styleUrl>\n");

        /*writer.write("<MultiGeometry>\n");*/

        writer.write("<gx:Track id=\"" + trackPartName + "\">\n");
        writer.write("<altitudeMode>absolute</altitudeMode>\n");
        writer.write("<gx:interpolate>0</gx:interpolate>\n");
    }

    protected void writePoint(TrackPoint point) throws IOException {
        /*double z1 = 5 * Math.sin(point.radRoll);
        double z2 = -z1;
        double y1 = -5 * Math.sin(point.heading + 90);
        double y2 = -y1;
        double x1 = -5 * Math.cos(point.heading + 90);
        double x2 = -x1;

        double p1[] = reproject(point, x2, y2, z2);
        double p2[] = reproject(point, x1, y1, z1);

        writer.write("<LineString>\n");
        //writer.write("<extrude>1</extrude>\n");
        writer.write("<altitudeMode>absolute</altitudeMode>\n");
        //writer.write("<outerBoundaryIs>\n");
        //writer.write("<LinearRing>\n");
        writer.write("<coordinates>\n");
        writer.write(String.format(Locale.ROOT, "%.10f,%.10f,%.2f\n", p1[0], p1[1], p1[2]));
        writer.write(String.format(Locale.ROOT, "%.10f,%.10f,%.2f\n", p2[0], p2[1], p2[2]));
        writer.write("</coordinates>\n");
        //writer.write("</LinearRing>\n");
        //writer.write("</outerBoundaryIs>\n");
        writer.write("</LineString>\n");*/

        writer.write(String.format("<when>%s</when>\n", dateFormatter.format(point.time / 1000)));
        writer.write(String.format(Locale.ROOT, "<gx:coord>%.10f %.10f %.2f</gx:coord>\n", point.lon, point.lat, point.alt));
    }

    private double[] reproject(TrackPoint ref, double x, double y, double z) {
        double res[] = new double[3];
        double x_rad = x / 6371000;
        double y_rad = y / 6371000;
        double c = Math.sqrt(x_rad * x_rad + y_rad * y_rad);
        double sin_c = Math.sin(c);
        double cos_c = Math.cos(c);

        double lat_rad = Math.asin(cos_c * Math.sin(Math.toRadians(ref.lat)) + (x_rad * sin_c * Math.cos(Math.toRadians(ref.lat))) / c);
        double lon_rad = (Math.toRadians(ref.lon) + Math.atan2(y_rad * sin_c, c * Math.cos(Math.toRadians(ref.lat)) * cos_c - x_rad * Math.sin(Math.toRadians(ref.lat)) * sin_c));

        res[0] = Math.toDegrees(lon_rad);
        res[1] = Math.toDegrees(lat_rad);

        res[2] = ref.alt + z;

        return res;
    }

    protected void writeTrackPartEnd() throws IOException {
        writer.write("</gx:Track>\n");

        /*writer.write("</MultiGeometry>\n");*/

        writer.write("</Placemark>\n");
    }

    protected void writeEnd() throws IOException {
        writer.write("</Document>\n");
        writer.write("</kml>");
    }

    @Override
    public String getName() {
        return "KML";
    }

    @Override
    public String getDescription() {
        return "Google Earth Track (KML)";
    }

    @Override
    public String getFileExtension() {
        return "kml";
    }

    protected void writeSetpoints(List<TrackPoint> setpoints) throws IOException {
        Iterator<TrackPoint> it = setpoints.iterator();
        int index = 0;

        writer.write("<Placemark>\n");
        writer.write("<name>mission</name>\n");
        writer.write("<description></description>\n");
        writer.write("<styleUrl>#setpoint</styleUrl>\n");
        writer.write("<gx:Track id=\"mission\">\n");
        writer.write("<altitudeMode>absolute</altitudeMode>\n");
        writer.write("<gx:interpolate>0</gx:interpolate>\n");

        while(it.hasNext()) {
            TrackPoint point = it.next();

            String name = "SP " + index;
            if (point.spType == 2) {
                name += ": Loiter";
            }
            if (point.spType == 3) {
                name += ": Takeoff";
            }
            if (point.spType == 4) {
                name += ": Land";
            }
            if (point.spType == 5) {
                name += ": Idle";
            }

            /*writer.write("<Placemark>\n");
            writer.write("<name>" + name + "</name>\n");
            writer.write("<description></description>\n");
            writer.write("<styleUrl>#setpoint</styleUrl>\n");
            writer.write("<Point>\n");
            writer.write("<altitudeMode>absolute</altitudeMode>\n");*/
            writer.write(String.format("<when>%s</when>\n", dateFormatter.format(point.time / 1000)));
            writer.write(String.format(Locale.ROOT, "<gx:coord>%.10f %.10f %.2f</gx:coord>\n", point.lon, point.lat, point.alt));
            //writer.write(String.format(Locale.ROOT, "<coordinates>%.10f %.10f %.2f</coordinates>\n", point.lon, point.lat, point.alt));
            /*writer.write("</Point>\n");
            writer.write("</Placemark>\n");*/

            index++;
        }

        writer.write("</gx:Track>\n");
        writer.write("</Placemark>\n");
    }

    protected void writeSinglePoint(TrackPoint point, String name) throws IOException {
        if (Math.abs(point.lon) < 0.0001 && Math.abs(point.lat) < 0.0001) {
            return;
        }

        writer.write("<Placemark>\n");
        writer.write("<name>" + name + "</name>\n");
        writer.write("<description></description>\n");
        writer.write("<styleUrl>#setpoint</styleUrl>\n");
        writer.write("<Point>\n");
        writer.write("<altitudeMode>absolute</altitudeMode>\n");
        writer.write(String.format(Locale.ROOT, "<coordinates>%.10f %.10f %.2f</coordinates>\n", point.lon, point.lat, point.alt));
        writer.write("</Point>\n");
        writer.write("</Placemark>\n");
    }
}
