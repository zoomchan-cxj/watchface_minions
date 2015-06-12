package xiuyi.com.minions;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import xiuyi.com.common.*;

public class GlobalWearApiHelper {
    private Context mContext;
    private WearableAPIHelper mWearApiHelper;
    private GoogleApiClient mGoogleApiClient;
    private static GlobalWearApiHelper _static;

    public static GlobalWearApiHelper getInstance(Context context) {
        if (_static == null) {
            _static = new GlobalWearApiHelper();
            _static.init(context);
        }
        return _static;
    }

    private void init(Context context) {
        mContext = context;

        mWearApiHelper = new WearableAPIHelper(mContext, new WearableAPIHelper.WearableAPIHelperListener() {
            @Override
            public void onWearableAPIConnected(GoogleApiClient apiClient) {
                mGoogleApiClient = apiClient;
            }

            @Override
            public void onWearableAPIConnectionSuspended(int cause) {

            }

            @Override
            public void onWearableAPIConnectionFailed(ConnectionResult result) {

            }
        });
    }

    public void sendScreenView(String name) {
        if (mWearApiHelper != null) {
            mWearApiHelper.putMessage(MessagePaths.SEND_SCREEN_VIEW, name.getBytes(), null);
        }
    }

    public void sendEvent(String category, String action, String label) {
        if (mWearApiHelper != null) {
            JSONObject json = new JSONObject();
            try {
                json.put("c", category);
                json.put("a", action);
                json.put("l", label);
            } catch (JSONException ex) {
            }
            mWearApiHelper.putMessage(MessagePaths.SEND_EVENT, json.toString().getBytes(), null);
        }
    }

    public void sendMessage(String path, String data) {
        if (mWearApiHelper != null) {
            mWearApiHelper.putMessage(path, data.getBytes(), null);
        }
    }
}
