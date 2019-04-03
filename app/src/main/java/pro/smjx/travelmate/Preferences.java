package pro.smjx.travelmate;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public static final String OWM_API_KEY = "6ca1b9187423f3972ecfaf7a41f01722";
    public static final String GOOGLE_API_KEY = "AIzaSyCIr178oStq2Qq5wuwPcFfEm-raMVJovxQ";
    public static final String GOOGLE_BASE_URL = "https://maps.googleapis.com/maps/api/geocode/";
    public static final String BASE_URL = "http://api.openweathermap.org/data/2.5/";
    public static final int NUMBER_OF_FORECAST_DATA = 100;

    private String DIR_NAME = "settings";
    private String TEMP_UNIT = "temp_unit";
    private String LATITUDE = "latitude";
    private String LONGITUDE = "longitude";
    private String TAB_POSITION = "tab_position";


    private final static String IMPERIAL = "imperial";
    private final static String METRIC = "metric";

    public int getTabPosition() {
        return Integer.parseInt(preferences.getString(TAB_POSITION, "0"));
    }

    public void setTabPosition(int position) {
        editor.putString(TAB_POSITION, String.valueOf(position));
        editor.commit();
    }

    public Preferences(Context context) {
        preferences = context.getSharedPreferences(DIR_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public void setTempUnit(String tempUnit) {
        String saveString;
        switch (tempUnit) {
            case "f":
                saveString = IMPERIAL;
                break;
            default:
                saveString = METRIC;
                break;
        }
        editor.putString(TEMP_UNIT, saveString);
        editor.commit();
    }

    public void setLatitude(double lattitude) {
        editor.putString(LATITUDE, String.valueOf(lattitude));
        editor.commit();
    }

    public void setLongitude(double longitude) {
        editor.putString(LONGITUDE, String.valueOf(longitude));
        editor.commit();
    }

    public double getLatitude() {
        return Double.parseDouble(preferences.getString(LATITUDE, "23.72"));
    }

    public double getLongitude() {
        return Double.parseDouble(preferences.getString(LONGITUDE, "90.4"));
    }

    public String getTempUnit() {
        String getFomPreference = preferences.getString(TEMP_UNIT, METRIC);
        if (getFomPreference.equals(METRIC) || getFomPreference.equals(IMPERIAL)) {
            return getFomPreference;
        }
        return METRIC;
    }

    public String getTempUnitSign() {
        String getFomPreference = preferences.getString(TEMP_UNIT, METRIC);
        if (getFomPreference.equals(IMPERIAL)) {
            return "F";
        }
        return "C";
    }
}
