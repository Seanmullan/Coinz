package mullan.sean.coinz;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.HashMap;
import java.util.Map;

public class Coin {

    private String  id;
    private double  value;
    private String  currency;
    private LatLng  location;
    private boolean isSelected;

    public Coin(String id, double value, String currency, LatLng location) {
        this.id         = id;
        this.value      = value;
        this.currency   = currency;
        this.location   = location;
        this.isSelected = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LatLng getLocation() {
        return location;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    /*
     *  @return { Returns Map of coin data }
     */
    public Map<String,Object> getCoinMap() {
        Map<String,Object> coinData = new HashMap<>();
        coinData.put("value", this.value);
        coinData.put("currency", this.currency);
        coinData.put("longitude", this.location.getLongitude());
        coinData.put("latitude", this.location.getLatitude());
        return coinData;
    }
}
