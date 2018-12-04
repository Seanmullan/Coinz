package mullan.sean.coinz;

import java.util.HashMap;
import java.util.Map;

/**
 *  A Transaction object stores the amount of gold that was added in a single transaction and the
 *  date of the transaction.
 */
public class Transaction {

    private double goldAdded;
    private String dateOfTransaction;

    /**
     * @param goldAdded amount of gold that was added in transaction
     * @param date date of the transaction
     */
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
     *  @return  Map of transaction data
     */
    public Map<String,Object> getTransactionMap() {
        Map<String,Object> transactionMap = new HashMap<>();
        transactionMap.put("gold", goldAdded);
        transactionMap.put("date", dateOfTransaction);
        return transactionMap;
    }
}
