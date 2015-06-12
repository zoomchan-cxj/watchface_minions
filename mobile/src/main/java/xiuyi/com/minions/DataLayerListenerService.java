/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xiuyi.com.minions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import de.greenrobot.event.EventBus;
import xiuyi.com.common.*;
import xiuyi.com.minions.events.ConnectionStatusEvent;


/**
 * Listens to DataItems and Messages from the local node.
 */
public class DataLayerListenerService extends WearableListenerService {

    private static final String TAG = "AppDataLayer";
    private static boolean connected;

    private GoogleApiClient mGoogleApiClient;
    private WearableAPIHelper mWearApiHelper;
    private Tracker mTracker;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Intent intent = new Intent(DataLayerListenerService.this, LocationService.class);
            startService(intent);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("mobile", "DataLayerListenerService onCreate");
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        mTracker = analytics.newTracker(R.xml.global_tracker);
        mWearApiHelper = new WearableAPIHelper(this, new WearableAPIHelper.WearableAPIHelperListener() {
            @Override
            public void onWearableAPIConnected(GoogleApiClient apiClient) {
                mGoogleApiClient = apiClient;
                LOGD(TAG, "onWearableAPIConnected");
            }

            @Override
            public void onWearableAPIConnectionSuspended(int cause) {

            }

            @Override
            public void onWearableAPIConnectionFailed(ConnectionResult result) {

            }
        });
    }

    @Override
    public void onDestroy() {
        Log.d("mobile", "DataLayerListenerService onDestroy");
        super.onDestroy();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        connected = true;
        LOGD(TAG, "onDataChanged: " + dataEvents);

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        LOGD(TAG, "onMessageReceived: " + messageEvent);
        connected = true;
        mHandler.sendEmptyMessage(1);

        String path = messageEvent.getPath();
        if (path.equals(MessagePaths.SEND_EVENT)) {
            boolean enable = true;
            if (enable) {
                String jsonStr = new String(messageEvent.getData());
                try {
                    JSONObject json = new JSONObject(jsonStr);
                    String category = json.getString("c");
                    String action = json.getString("a");
                    String label = json.getString("l");
                    mTracker.send(new HitBuilders.EventBuilder()
                            .setCategory(category)
                            .setAction(action)
                            .setLabel(label)
                            .build());
                } catch (JSONException e) {
                }
            }
        } else if (path.equals(MessagePaths.SEND_SCREEN_VIEW)) {

                mTracker.setScreenName(new String(messageEvent.getData()));
                mTracker.send(new HitBuilders.AppViewBuilder().build());

        }
    }

    @Override
    public void onPeerConnected(Node peer) {
        connected = true;
        connected = true;
        EventBus.getDefault().post(new ConnectionStatusEvent(true));
        LOGD(TAG, "onPeerConnected: " + peer);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String weather = sp.getString(PreferenceKeys.PREF_KEY_WEATHER_DATA, null);
        if (weather != null) {
            mWearApiHelper.putMessage(MessagePaths.WEATHER_DATA, weather.getBytes(), null);
        }
        weather = sp.getString(PreferenceKeys.PREF_KEY_FORECAST_WEATHER_DATA, null);
        if (weather != null) {
            mWearApiHelper.putMessage(MessagePaths.FORECAST_WEATHER_DATA, weather.getBytes(), null);
        }

    }

    @Override
    public void onPeerDisconnected(Node peer) {
        connected = false;
        EventBus.getDefault().post(new ConnectionStatusEvent(false));
        LOGD(TAG, "onPeerDisconnected: " + peer);

    }

    public static boolean isConnectedWithWear() {
        return connected;
    }

    public static void LOGD(final String tag, String message) {
        if (Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, message);
        }
    }
}
