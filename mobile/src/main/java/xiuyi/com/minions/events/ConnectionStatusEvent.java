package xiuyi.com.minions.events;

public class ConnectionStatusEvent {
    boolean connected;

    public ConnectionStatusEvent(boolean c) {
        connected = c;
    }

    public boolean isConnected() {
        return connected;
    }
}
