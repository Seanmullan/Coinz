package mullan.sean.coinz;

import java.util.ArrayList;
import java.util.HashMap;

public class Data {

    /*
     *  This class serves as a global data structure available to all classes
     */

    private static ArrayList<Coin> availableCoins;
    private static HashMap<String, Double> exchangeRates;

    public static void setAvailableCoins(ArrayList<Coin> coins) {
        Data.availableCoins = coins;
    }

    public static ArrayList<Coin> getAvailableCoins() {
        return availableCoins;
    }

    public static void setExchangeRates(HashMap<String, Double> exchangeRates) {
        Data.exchangeRates = exchangeRates;
    }

    public static HashMap<String, Double> getExchangeRates() {
        return exchangeRates;
    }
}
