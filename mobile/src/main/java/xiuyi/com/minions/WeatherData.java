package xiuyi.com.minions;

import android.graphics.Bitmap;

public class WeatherData {
    public double temp; //Temperature, Kelvin (subtract 273.15 to convert to Celsius)
    public String main; // clouds, clear, etc
    public String description; //
    public String icon; //weather icon
    public Bitmap weatherIcon;
    public String cityName;
    public double minTemp;
    public double maxTemp;
    public String windStr;
}