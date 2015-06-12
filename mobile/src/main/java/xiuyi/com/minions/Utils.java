package xiuyi.com.minions;

import android.content.Context;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static boolean isWifiEnabled(Context context) {
        WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        return wm.isWifiEnabled();
    }

    public static boolean enableWifi(Context context, boolean enable) {
        WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        return wm.setWifiEnabled(enable);
    }

    public static int getPhoneRingVolume(Context context) {
        AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        return am.getStreamVolume(AudioManager.STREAM_RING);
    }

    public static int getPhoneRingMaxVolume(Context context) {
        AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        return am.getStreamMaxVolume(AudioManager.STREAM_RING);
    }

    public static void setPhoneRingVolume(Context context, int v) {
        AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_RING, v, AudioManager.FLAG_SHOW_UI);
    }

    public static int getRingerMode(Context context) {
        AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        return am.getRingerMode();
    }

    public static void setRingerMode(Context context, int mode) {
        AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        am.setRingerMode(mode);
    }

    public static int getMusicVolume(Context context) {
        AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        return am.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    public static int getMusicMaxVolume(Context context) {
        AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        return am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public static void setMusicVolume(Context context, int v) {
        AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, v, AudioManager.FLAG_SHOW_UI);
    }

    public static class LocationItem {
        public String address;
        public String woeid;
    }

    public static List<LocationItem> fetchWOEIDS(String address) {
        List<LocationItem> ret = new ArrayList<>();
        try {
            String url = "http://search.yahoo.com/sugg/gossip/gossip-gl-location/?appid=weather&output=sd1&command=" + URLEncoder.encode(address, "UTF-8");
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            if (response.code() != 200) {
                return ret;
            }
            String str = response.body().string();
            JSONObject json = new JSONObject(str);
            JSONArray arr = json.getJSONArray("r");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    String d = obj.getString("d");
                    String k = obj.getString("k");
                    String iso = splitKV(d, "iso");
                    String woeid = splitKV(d, "woeid");
                    String s = splitKV(d, "s");
                    String sc = splitKV(d, "sc");
                    StringBuilder builder = new StringBuilder();
                    if (k != null && !TextUtils.isEmpty(k)) {
                        builder.append(k + ",");
                    }
                    if (sc != null && !TextUtils.isEmpty(sc)) {
                        builder.append(sc + ",");
                    } else if (s != null && !TextUtils.isEmpty(s)) {
                        builder.append(s + ",");
                    }
                    if (iso != null) {
                        builder.append(iso);
                    }
                    LocationItem locationItem = new LocationItem();
                    locationItem.address = builder.toString();
                    locationItem.woeid = woeid;
                    boolean find = false;
                    for (int ii = 0; ii < ret.size(); ii++) {
                        if (ret.get(ii).address.equals(locationItem.address)) {
                            find = true;
                        }
                    }
                    if (!find) {
                        ret.add(locationItem);
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static String splitKV(String source, String key) {
        return splitKV(source, key, "&");
    }

    public static String splitKV(String source, String key, String delimiter) {
        int start = source.indexOf(key + "=");
        if (start != -1) {
            int end = source.indexOf(delimiter, start);
            if (end != -1) {
                String value = source.substring(start + key.length() + delimiter.length(), end);
                return value;
            }
        }
        return null;
    }
}
