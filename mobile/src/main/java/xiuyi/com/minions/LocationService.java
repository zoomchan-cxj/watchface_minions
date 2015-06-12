package xiuyi.com.minions;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;
import xiuyi.com.common.MessagePaths;
import xiuyi.com.common.WearableAPIHelper;
import xiuyi.com.minions.events.AddressChangeEvent;
import xiuyi.com.minions.events.LocationChangeEvent;





public class LocationService extends Service {
    public LocationService() {
    }

    private LocationManager mLocationManager;
    private Location mLastLocation;
    private String mLastAddress;
    private String mWOEID;
    private Context mContext;
    private final static String TAG = "LocationService";
    private long UPDATE_INTERVAL = 1000 * 60 * 30;
    private Timer mUpdateWeather;
    private WearableAPIHelper mWearableAPIHelper;
    private GoogleApiClient mApiClient;
    private boolean mRegistered;

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, provider + " enabled");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, provider + " disabled");
        }

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
//                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(LocationService.this);

                EventBus.getDefault().post(new LocationChangeEvent(location));

            }
        }

    };

    private TimerTask mUpdateWeatherTask = new TimerTask() {
        @Override
        public void run() {
            if (true) {
                if (mWOEID == null && !TextUtils.isEmpty(mLastAddress)) {
                    String[] values = mLastAddress.split(",");
                    if (values.length > 0) {
                        String woeid = null;
                        List<Utils.LocationItem> woeids = Utils.fetchWOEIDS(values[0]);
                        for (int i = 0; i < woeids.size(); i++) {
                            Utils.LocationItem item = woeids.get(i);
                            if (i == 0) {
                                woeid = item.woeid;
                            }
                            if (mLastAddress.equals(item.address)) {
                                woeid = item.woeid;
                                break;
                            }
                        }
                        if (woeid != null) {
                            EventBus.getDefault().post(new AddressChangeEvent(mLastAddress, woeid));
                        }
                    }
                } else if (mWOEID != null) {
                    new FetchWeather().execute(mWOEID);
                }
            }
        }
    };



    @Override
    public void onCreate() {
        super.onCreate();

        EventBus.getDefault().register(this);
        mContext = this;
        mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        mLastLocation = getLastLocation();
        if (mLastLocation != null) {
            Boolean use = sp.getBoolean(PreferenceKeys.PREF_USE_CUSTOM_LOCATION, false);
            if (!use)
                EventBus.getDefault().post(new LocationChangeEvent(mLastLocation));
        }
        mLastAddress = sp.getString(PreferenceKeys.PREF_KEY_CURRENT_ADDRESS, "");
        mWOEID = sp.getString(PreferenceKeys.PREF_KEY_WOEID, null);
        mUpdateWeather = new Timer();
        mUpdateWeather.scheduleAtFixedRate(mUpdateWeatherTask, 0, 1000 * 60 * 30);

        mWearableAPIHelper = new WearableAPIHelper(this, new WearableAPIHelper.WearableAPIHelperListener() {
            @Override
            public void onWearableAPIConnected(GoogleApiClient apiClient) {
                mApiClient = apiClient;
            }

            @Override
            public void onWearableAPIConnectionSuspended(int cause) {

            }

            @Override
            public void onWearableAPIConnectionFailed(ConnectionResult result) {

            }
        });


    }


    public void onEventBackgroundThread(LocationChangeEvent event) {
        if (event != null) {
            Location location = event.getLocation();
            mLastLocation = location;
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
            //经纬度写进preference
            editor.putString(PreferenceKeys.PREF_KEY_CURRENT_LOCATION, mLastLocation.getLongitude() + "," + mLastLocation.getLatitude());
            editor.commit();
            Geocoder coder = new Geocoder(LocationService.this);
            String ret = null;
            String woeid = null;
            try {
                List<Address> address = coder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (address.size() > 0) {
                    String admin = address.get(0).getAdminArea();
                    String locality = address.get(0).getLocality();
                    String countryCode = address.get(0).getCountryCode();
                    if (admin != null) {
                        ret = admin;
                    }
                    if (locality != null) {
                        ret += "," + locality;
                    }
                    if (countryCode != null) {
                        ret += "," + countryCode;
                    }
                    List<Utils.LocationItem> woeids = Utils.fetchWOEIDS(admin);
                    for (int i = 0; i < woeids.size(); i++) {
                        Utils.LocationItem item = woeids.get(i);
                        if (i == 0) {
                            woeid = item.woeid;
                        }
                        if (ret.equals(item.address)) {
                            woeid = item.woeid;
                            break;
                        }
                    }
                }
                if (ret != null) {
                    EventBus.getDefault().post(new AddressChangeEvent(ret, woeid));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onEventBackgroundThread(AddressChangeEvent event) {
        String address = event.getAddress();
        String woeid = event.getWOEID();
        if (!woeid.equals(mWOEID)) {
            mLastAddress = address;
            mWOEID = woeid;
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
            editor.putString(PreferenceKeys.PREF_KEY_CURRENT_ADDRESS, mLastAddress);
            editor.putString(PreferenceKeys.PREF_KEY_WOEID, mWOEID);
            editor.commit();
            if (mWOEID != null) {
                new FetchWeather().execute(mWOEID);
            }
        }
    }

    private Location getLastLocation() {
        if(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location != null){
                return location;
            }
        }
        if(mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(location != null){
                return location;
            }
        }

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (!mRegistered) {
            try{
                //Bug: 修复手机没有存储卡的适合导致的崩溃
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_INTERVAL, 1000, mLocationListener);
            }catch(RuntimeException e){
                e.printStackTrace();
            }

//            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_INTERVAL, 1000, mLocationListener);
            mRegistered = true;
        }
        return super.onStartCommand(intent, flags, startId);
    }



    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        mUpdateWeather.cancel();
        mWearableAPIHelper.onDestroy();
        super.onDestroy();
        mLocationManager.removeUpdates(mLocationListener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private class WeatherData {
        public String today;
        public String forecast;
    }

    private class FetchWeather extends AsyncTask<String, Long, WeatherData> {

        @Override
        protected WeatherData doInBackground(String... params) {
            return fetchYahooWeather(params[0]);
        }

        @Override
        protected void onPostExecute(WeatherData data) {
            super.onPostExecute(data);
            if (data.today != null) {
                try {
                    JSONObject json = new JSONObject(data.today);
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(LocationService.this).edit();
                    editor.putString(PreferenceKeys.PREF_KEY_WEATHER_DATA, data.today);
                    editor.commit();
                    mWearableAPIHelper.putMessage(MessagePaths.WEATHER_DATA, data.today.getBytes(), null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (data.forecast != null) {
                try {
                    JSONObject json = new JSONObject(data.forecast);
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(LocationService.this).edit();
                    editor.putString(PreferenceKeys.PREF_KEY_FORECAST_WEATHER_DATA, data.forecast);
                    editor.commit();
                    mWearableAPIHelper.putMessage(MessagePaths.FORECAST_WEATHER_DATA, data.forecast.getBytes(), null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        private WeatherData fetchYahooWeather(String woeid) {
            WeatherData data = new WeatherData();
            try {
                String url = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20%3D%20" + woeid + "&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                Response response = client.newCall(request).execute();
                if (response.code() == 200) {
                    data.today = response.body().string();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }

        private WeatherData fetchWeather(String address) {
            WeatherData data = new WeatherData();
            try {
                String url = "http://api.openweathermap.org/data/2.5/weather?q=" + URLEncoder.encode(address, "UTF-8");
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                Response response = client.newCall(request).execute();
                if (response.code() == 200) {
                    data.today = response.body().string();
                }

                url = "http://api.openweathermap.org/data/2.5/forecast/daily?cnt=4&q=" + URLEncoder.encode(address, "UTF-8");
                request = new Request.Builder()
                        .url(url)
                        .build();
                response = client.newCall(request).execute();
                if (response.code() == 200) {
                    data.forecast = response.body().string();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }
    }

}
