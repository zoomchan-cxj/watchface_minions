package xiuyi.com.minions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import java.util.HashMap;
import java.util.Map;

public class BatteryMonitor {
    public static interface BatteryMonitorCallback {
        public void onChanged(BatteryStatus status);
    }

    private static Map<BatteryMonitorCallback, BatteryChangedReceiver> callbacks;
    private static BatteryMonitor monitor;

    public static BatteryMonitor getInstance() {
        if (monitor == null) {
            monitor = new BatteryMonitor();
            callbacks = new HashMap<BatteryMonitorCallback, BatteryChangedReceiver>();
        }
        return monitor;
    }

    public void register(Context context, BatteryMonitorCallback callback) {
        if (callbacks.get(callback) == null && callback != null) {
            BatteryChangedReceiver receiver = new BatteryChangedReceiver(callback);
            context.registerReceiver(receiver,  new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            callbacks.put(callback, receiver);
        }
    }

    public void unregister(Context context, BatteryMonitorCallback callback) {
        if (callbacks.get(callback) != null) {
            context.unregisterReceiver(callbacks.get(callback));
            callbacks.remove(callback);
        }
    }

    private class BatteryChangedReceiver extends BroadcastReceiver {
        private BatteryMonitorCallback callback;

        public BatteryChangedReceiver(BatteryMonitorCallback callback) {
            this.callback = callback;
        }

        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);
                BatteryStatus status = new BatteryStatus();
                status.volume = level * 1.0f / scale * 100;
                status.voltage = intent.getIntExtra("voltage", 0);
                status.temperature = intent.getIntExtra("temperature", 0);
                status.status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
                status.plugged = intent.getIntExtra("plugged", BatteryManager.BATTERY_PLUGGED_AC);
                status.charging = intent.getIntExtra("status",BatteryManager.BATTERY_STATUS_CHARGING);

                if (this.callback != null) {
                    this.callback.onChanged(status);
                }
            }
        }
    }
}
