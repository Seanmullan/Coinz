package mullan.sean.coinz;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.HashMap;

public class Coin {

    private String  id;
    private double  value;
    private String  currency;
    private String  symbol;
    private String  colour;
    private LatLng  location;
    private boolean isSelected;

    public Coin(String id, double value, String currency, String symbol, String colour, LatLng location) {
        this.id         = id;
        this.value      = value;
        this.currency   = currency;
        this.symbol     = symbol;
        this.colour     = colour;
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

    public String getSymbol() { return symbol; }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public boolean isSelected() { return isSelected; }

    public void setSelected(boolean selected) { isSelected = selected; }

    public HashMap<String,Object> getCoinMap() {
        HashMap<String,Object> coinData = new HashMap<>();
        coinData.put("value", this.value);
        coinData.put("currency", this.currency);
        coinData.put("symbol", this.symbol);
        coinData.put("colour", this.colour);
        coinData.put("longitude", this.location.getLongitude());
        coinData.put("latitude", this.location.getLatitude());
        return coinData;
    }
}
