package xiuyi.com.minions.events;

public class AddressChangeEvent {
    private String mAddress;
    private String woeid;

    public String getAddress() {return mAddress;}
    public String getWOEID() {return woeid;}
    public AddressChangeEvent(String address, String woeid) {
        mAddress = address;
        this.woeid = woeid;
    }
}
