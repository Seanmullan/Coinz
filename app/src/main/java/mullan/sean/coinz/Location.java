package mullan.sean.coinz;

public class Location {

    private double latitude;
    private double longitude;

    public Location(double longitude, double latitide) {
        this.longitude = longitude;
        this.latitude  = latitide;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
