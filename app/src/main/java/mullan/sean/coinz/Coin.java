package mullan.sean.coinz;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.HashMap;
import java.util.Map;

/**
 *   A Coin object stores the id, value, currency and location of the coin. It also stores
 *   a flag which indicates whether or not the coin has been selected in the users local
 *   wallet.
 */
public class Coin {

    private String  id;
    private double  value;
    private String  currency;
    private LatLng  location;
    private boolean isSelected;
    private String  date;

    Coin(String id, double value, String currency, LatLng location, String date) {
        this.id         = id;
        this.value      = value;
        this.currency   = currency;
        this.location   = location;
        this.isSelected = false;
        this.date       = date;
    }

    public String getId() {
        return id;
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

    LatLng getLocation() {
        return location;
    }

    boolean isSelected() {
        return isSelected;
    }

    void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getDate() {
        return date;
    }

    /**
     *  @return True if and only if ID's are equal
     */
    @Override
    public boolean equals(Object coin) {
        if (coin.getClass().equals(Coin.class)) {
            return this.id.equals(((Coin) coin).id);
        } else {
            return false;
        }
    }

    /**
     *  @return Returns Map of Coin data
     */
    Map<String,Object> getCoinMap() {
        Map<String,Object> coinData = new HashMap<>();
        coinData.put("value", this.value);
        coinData.put("currency", this.currency);
        coinData.put("longitude", this.location.getLongitude());
        coinData.put("latitude", this.location.getLatitude());
        coinData.put("date", this.date);
        return coinData;
    }
}
