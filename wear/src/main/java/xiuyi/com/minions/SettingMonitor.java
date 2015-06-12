package xiuyi.com.minions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import java.util.HashMap;
import java.util.Map;

public class SettingMonitor {
    public static interface SettingMonitorCallback {
        public void onChanged(SettingStatus status);
    }

    private static Map<SettingMonitorCallback, SettingChangedReceiver> callbacks;
    private static SettingMonitor monitor;

    public static SettingMonitor getInstance() {
        if (monitor == null) {
            monitor = new SettingMonitor();
            callbacks = new HashMap<SettingMonitorCallback, SettingChangedReceiver>();
        }
        return monitor;
    }

    public void register(Context context, SettingMonitorCallback callback) {
        if (callbacks.get(callback) == null && callback != null) {
            SettingChangedReceiver receiver = new SettingChangedReceiver(callback);
            context.registerReceiver(receiver,  new IntentFilter("android.intent.action.SETTING_CHANGED"));
            callbacks.put(callback, receiver);
        }
    }

    public void unregister(Context context, SettingMonitorCallback callback) {
        if (callbacks.get(callback) != null) {
            context.unregisterReceiver(callbacks.get(callback));
            callbacks.remove(callback);
        }
    }

    private class SettingChangedReceiver extends BroadcastReceiver {
        private SettingMonitorCallback callback;

        public SettingChangedReceiver(SettingMonitorCallback callback) {
            this.callback = callback;
        }

        public void onReceive(Context context, Intent intent) {
                if("android.intent.action.SETTING_CHANGED".equals(intent.getAction())){
                    SettingStatus status = new SettingStatus();
                    status.temperature_type = intent.getStringExtra("temperature_type");
                    if (this.callback != null) {
                        this.callback.onChanged(status);
                    }

                }


        }
    }
}
