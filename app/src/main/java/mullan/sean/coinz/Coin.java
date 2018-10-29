package mullan.sean.coinz;

import java.util.HashMap;

public class Coin {

    private String   id;
    private double   value;
    private String   currency;
    private String   symbol;
    private String   colour;
    private Location location;

    public Coin(String id, double value, String currency, String symbol, String colour, Location location) {
        this.id = id;
        this.value = value;
        this.currency = currency;
        this.symbol = symbol;
        this.colour = colour;
        this.location = location;
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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

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
