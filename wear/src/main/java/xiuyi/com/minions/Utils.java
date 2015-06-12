package xiuyi.com.minions;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Utils {




    public static Bitmap parseWeatherIcon(Context context, String icon, boolean black) {
        Bitmap bmp = null;
        int iconResId = -1;
        int intIcon = -1;
        try {
            intIcon = Integer.parseInt(icon);
        } catch (Exception ex) {

        }
        if (intIcon ==0 ) {
            iconResId = R.mipmap.ic_weather_1;
        } else if (intIcon ==1  || intIcon == 2) {
            iconResId = R.mipmap.ic_weather_3;
        } else if (intIcon == 23) {
            iconResId = R.mipmap.ic_weather_4;
        } else if (intIcon == 24) {
            iconResId = R.mipmap.ic_weather_5;
        } else if (intIcon >= 8 && intIcon <=10 ) {
            iconResId = R.mipmap.ic_weather_6;
        } else if (intIcon ==11 || intIcon ==12 || intIcon==40) {
            iconResId = R.mipmap.ic_weather_7;
        } else if (intIcon ==3 || intIcon ==37 || intIcon==38 || intIcon==39) {
            iconResId = R.mipmap.ic_weather_8;
        } else if (intIcon ==3) {
            iconResId = R.mipmap.ic_weather_9;
        } else if (intIcon ==5 || intIcon ==18 || intIcon==35) {
            iconResId = R.mipmap.ic_weather_10;
        } else if (intIcon == 13) {
            iconResId = R.mipmap.ic_weather_11;
        } else if (intIcon ==16) {
            iconResId = R.mipmap.ic_weather_12;
        } else if (intIcon ==14 || intIcon ==42 || intIcon==46) {
            iconResId = R.mipmap.ic_weather_13;
        } else if (intIcon ==15 || intIcon ==41 || intIcon==43) {
            iconResId = R.mipmap.ic_weather_14;
        } else if (intIcon ==6 || intIcon ==7 || intIcon==17) {
            iconResId = R.mipmap.ic_weather_15;
        } else if (intIcon ==45 || intIcon ==47) {
            iconResId = R.mipmap.ic_weather_16;
        } else if(intIcon ==29 || intIcon ==30 || intIcon==44){
            iconResId = R.mipmap.ic_weather_17;
        } else if(intIcon >=19 && intIcon<=22 || intIcon >=26 && intIcon<=28){
            iconResId = R.mipmap.ic_weather_18;
        }else if(intIcon >=31 && intIcon<=34 || intIcon ==36){
            iconResId = R.mipmap.ic_weather_19;
        }else if(intIcon ==25){
            iconResId = R.mipmap.ic_weather_20;
        }else{
            iconResId = -1;
        }

        if (iconResId > 0) {
            bmp = BitmapFactory.decodeResource(context.getResources(), iconResId);
        }
        return bmp;
    }

    public static String parseWeatherDirection(double deg) {
        if (deg >= 348.75 || deg <= 11.25) {
            return "N";
        } else if (deg >= 11.25 && deg <= 33.75) {
            return "NNE";
        } else if (deg >= 33.75 && deg <= 56.25) {
            return "NE";
        } else if (deg >= 56.25 && deg <= 78.75) {
            return "ENE";
        } else if (deg >= 78.75 && deg <= 101.25) {
            return "E";
        } else if (deg >= 101.25 && deg <= 123.75) {
            return "ESE";
        } else if (deg >= 123.75 && deg <= 146.25) {
            return "SE";
        } else if (deg >= 146.25 && deg <= 168.75) {
            return "SSE";
        } else if (deg >= 168.75 && deg <= 191.25) {
            return "S";
        } else if (deg >= 191.25 && deg <= 213.75) {
            return "SSW";
        } else if (deg >= 213.75 && deg <= 236.25) {
            return "SW";
        } else if (deg >= 236.25 && deg <= 258.75) {
            return "WSW";
        } else if (deg >= 258.75 && deg <= 281.25) {
            return "W";
        } else if (deg >= 281.25 && deg <= 303.75) {
            return "WNW";
        } else if (deg >= 303.75 && deg <= 326.25) {
            return "NW";
        } else if (deg >= 326.25 && deg <= 348.75) {
            return "NNW";
        } else {
            return "";
        }

//    N 348.75 - 11.25
//    NNE 11.25 - 33.75
//    NE 33.75 - 56.25
//    ENE 56.25 - 78.75
//    E 78.75 - 101.25
//    ESE 101.25 - 123.75
//    SE 123.75 - 146.25
//    SSE 146.25 - 168.75
//    S 168.75 - 191.25
//    SSW 191.25 - 213.75
//    SW 213.75 - 236.25
//    WSW 236.25 - 258.75
//    W 258.75 - 281.25
//    WNW 281.25 - 303.75
//    NW 303.75 - 326.25
//    NNW 326.25 - 348.75
    }

    /*
    // parse data from openweathermap.org
    public static ArrayList<WeatherData> parseForecastWeatherData(Context context, String weatherData, boolean black) {
        ArrayList<WeatherData> list = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(weatherData);
            JSONObject city = json.getJSONObject("city");
            JSONArray array = json.getJSONArray("list");
            for (int i = 0; i < array.length(); i++) {
                JSONObject main = array.getJSONObject(i);
                JSONObject temp = main.getJSONObject("temp");
                JSONArray weather = main.getJSONArray("weather");
                JSONObject w = weather.getJSONObject(0);
                WeatherData data = new WeatherData();
                data.cityName = city.getString("name");
                data.windStr = String.format("Wind: %s %.2fM/S", parseWeatherDirection(main.getDouble("deg")) ,main.getDouble("speed"));
                data.maxTemp = temp.getDouble("max");
                data.minTemp = temp.getDouble("min");
                data.description = w.getString("description");
                data.icon = w.getString("icon");
                data.main = w.getString("main");
                data.weatherIcon = parseWeatherIcon(context, data.icon, black);
                data.dt = main.getLong("dt");
                list.add(data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static WeatherData parseTemperature(Context context, String weatherData, boolean black) {
        WeatherData temp = null;
        try {
            JSONObject json = new JSONObject(weatherData);
            JSONObject main = json.getJSONObject("main");
            JSONArray weather = json.getJSONArray("weather");
            JSONObject wind = json.getJSONObject("wind");
            temp = new WeatherData();
            temp.temp = main.getDouble("temp");
            JSONObject w = weather.getJSONObject(0);
            temp.main = w.getString("main");
            temp.description = w.getString("description");
            temp.icon = w.getString("icon");
            temp.weatherIcon = parseWeatherIcon(context, temp.icon, black);
            temp.cityName = json.getString("name");
            temp.minTemp = main.getDouble("temp_min");
            temp.maxTemp = main.getDouble("temp_max");
            temp.windStr = String.format("Wind: %s %.2fM/S", parseWeatherDirection(wind.getDouble("deg")) ,wind.getDouble("speed"));
            temp.dt = json.getLong("dt");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return temp;
    }*/

    // parse data from yahoo weather
//    public static ArrayList<WeatherData> parseForecastWeatherData(Context context, String weatherData, boolean black) {
//        ArrayList<WeatherData> list = new ArrayList<>();
//        try {
//            JSONObject json = new JSONObject(weatherData);
//            JSONObject query = json.getJSONObject("query");
//            JSONObject results = query.getJSONObject("results");
//            JSONObject channel = results.getJSONObject("channel");
//            JSONObject location = channel.getJSONObject("location");
//            JSONObject item = channel.getJSONObject("item");
//            JSONArray forecast = item.getJSONArray("forecast");
//            for (int i = 0; i < forecast.length(); i++) {
//                JSONObject main = forecast.getJSONObject(i);
//                WeatherData data = new WeatherData();
//                data.cityName = location.getString("city");
//                data.windStr = "";
//                data.maxTemp = main.getDouble("high");
//                data.minTemp = main.getDouble("low");
//                data.description = "";
//                data.icon = main.getString("code");
//                data.main = main.getString("text");
//                data.weatherIcon = parseWeatherIcon(context, data.icon, black);
//                list.add(data);
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return list;
//    }

    public static WeatherData parseTemperature(Context context, String weatherData, boolean black) {
        WeatherData ret = null;
        try {
            JSONObject json = new JSONObject(weatherData);
            JSONObject query = json.getJSONObject("query");
            JSONObject results = query.getJSONObject("results");
            JSONObject channel = results.getJSONObject("channel");
            JSONObject location = channel.getJSONObject("location");
            JSONObject wind = channel.getJSONObject("wind");
            JSONObject item = channel.getJSONObject("item");
            JSONObject condition = item.getJSONObject("condition");
            WeatherData temp = new WeatherData();
            temp.temp = condition.getDouble("temp");
            temp.main = condition.getString("text");
            temp.description = item.getString("description");
            temp.icon = condition.getString("code");
            temp.weatherIcon = parseWeatherIcon(context, temp.icon, black);
            temp.cityName = location.getString("city");
            temp.minTemp = temp.temp;
            temp.maxTemp = temp.temp;
            temp.windStr = String.format("Wind: %s %.2fmph", parseWeatherDirection(wind.getDouble("direction")), wind.getDouble("speed"));
            ret = temp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static float dpFromPx(Context context, float px)
    {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float pxFromDp(Context context, float dp)
    {
        return dp * context.getResources().getDisplayMetrics().density;
    }



}
