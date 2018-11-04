package mullan.sean.coinz;

import java.util.HashMap;
import java.util.Map;

public class Transaction {

    private double goldAdded;
    private String dateOfTransaction;

    public Transaction(double goldAdded, String date) {
        this.goldAdded = goldAdded;
        this.dateOfTransaction = date;
    }

    public double getGoldAdded() {
        return goldAdded;
    }

    public String getDate() {
        return dateOfTransaction;
    }

    /*
     *  @return  { returns Map of transaction data }
     */
    public Map<String,Object> getTransactionMap() {
        Map<String,Object> transactionMap = new HashMap<>();
        transactionMap.put("gold", goldAdded);
        transactionMap.put("date", dateOfTransaction);
        return transactionMap;
    }
}
