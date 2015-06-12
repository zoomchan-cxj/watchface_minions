package xiuyi.com.minions.events;

import android.location.Location;

public class LocationChangeEvent {
    private Location mLocation;

    public Location getLocation() {return mLocation;}
    public LocationChangeEvent(Location location) {
        mLocation = location;
    }
}
