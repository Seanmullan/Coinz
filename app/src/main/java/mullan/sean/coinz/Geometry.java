package mullan.sean.coinz;

public class Geometry {

    private double latitude;
    private double longitude;

    public Geometry(double latitide, double longitude) {
        this.latitude  = latitide;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
