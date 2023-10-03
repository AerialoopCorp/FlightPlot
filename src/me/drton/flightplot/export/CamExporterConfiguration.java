package me.drton.flightplot.export;

import java.util.prefs.Preferences;

/**
 * Created by ada on 19.01.14.
 */
public class CamExporterConfiguration {
    private String imageName;
    private final static String IMAGE_NAME = "imageName";
    private long startingNumber;
    private final static String STARTING_NUMBER = "startingNumber";

    public void saveConfiguration(Preferences preferences) {
        if (imageName != null) {
            preferences.put(IMAGE_NAME, imageName);
        }
        preferences.putLong(STARTING_NUMBER, startingNumber);
    }

    public void loadConfiguration(Preferences preferences) {
        imageName = preferences.get(IMAGE_NAME, "DSC%05d.JPG");
        startingNumber = preferences.getLong(STARTING_NUMBER, 1);
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public long getStartingNumber() {
        return startingNumber;
    }

    public void setStartingNumber(long startingNumber) {
        this.startingNumber = startingNumber;
    }
}
