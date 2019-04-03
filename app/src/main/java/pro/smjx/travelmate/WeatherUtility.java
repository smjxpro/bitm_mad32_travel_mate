package pro.smjx.travelmate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WeatherUtility {
    public static String getTimeFromMS(long ms) {
        ms = verifyMS(ms);
        Date currentDate = new Date(ms);
        DateFormat df = new SimpleDateFormat("hh:mm:ss a");
        return df.format(currentDate);
    }

    public static String getTimeFromMSForecast(long ms) {
        ms = verifyMS(ms);
        Date currentDate = new Date(ms);
        DateFormat df = new SimpleDateFormat("hh:mm a");
        return df.format(currentDate);
    }

    private static long verifyMS(long ms) {
        String msString = String.valueOf(ms);
        int msLngth = msString.length();
        if (msLngth < 13) {
            for (int i = 13; i > msLngth; i--) {
                msString = msString + "0";
            }
        }
        return Long.parseLong(msString);
    }

    public static String getDateFromMS(long ms) {
        ms = verifyMS(ms);
        Date currentDate = new Date(ms);
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        return df.format(currentDate);
    }

    public static String getDateFromMSForecast(long ms) {
        ms = verifyMS(ms);
        Date currentDate = new Date(ms);
        DateFormat df = new SimpleDateFormat("EEEE: MMM dd");
        return df.format(currentDate);
    }

    public static String getDayFromMS(long ms) {
        ms = verifyMS(ms);
        Date currentDate = new Date(ms);
        DateFormat df = new SimpleDateFormat("EEEE");
        return df.format(currentDate);
    }
}
