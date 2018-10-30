package mullan.sean.coinz;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class Data {

    /*
     *  This class serves as a global data structure that uploads and downloads from
     *  the firebase database
     */

    private static final String TAG         = "C_DATA";
    private static final String UNCOLLECTED = "uncollected";
    private static final String COLLECTED   = "collected";
    private static final String RECEIVED    = "received";

    private static DocumentReference mUserDocRef;
    private static ArrayList<Coin>   mUncollectedCoins;
    private static ArrayList<Coin>   mCollectedCoins;
    private static ArrayList<Coin>   mReceivedCoins;
    private static int               mUncollectedCoinCount;

    /*
     *  @brief  { Initialise the document reference that will be used to identify
     *            the users document within firebase, and initialise local variables }
     */
    public static void init(DocumentReference docRef) {
        Data.mUserDocRef      = docRef;
        mUncollectedCoins     = new ArrayList<>();
        mCollectedCoins       = new ArrayList<>();
        mReceivedCoins        = new ArrayList<>();
        mUncollectedCoinCount = 0;
    }

    /*
     *  @return  { ArrayList of uncollected coins }
     */
    public static ArrayList<Coin> getUncollectedCoins() {
        return mUncollectedCoins;
    }

    /*
     *  @return  { ArrayList of collected coins }
     */
    public static ArrayList<Coin> getCollectedCoins() { return mCollectedCoins; }

    /*
     *  @return  { ArrayList of received coins }
     */
    public static ArrayList<Coin> getReceivedCoins() { return mReceivedCoins; }


    /*
     *  @brief  { This procedure fetches all documents within the specified collection
     *            argument. It identifies the collection and stores the retrieved coins
     *            in their respective ArrayLists. The caller is notified if the procedure
     *            was a success or failure }
     */
    public static void retrieveAllCoinsFromCollection(final String collection,
                                                      final OnEventListener<String> event) {
        mUserDocRef.collection(collection).get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            switch (collection) {
                                case UNCOLLECTED:
                                    mUncollectedCoins.add(documentToCoin(document));
                                    break;
                                case COLLECTED:
                                    mCollectedCoins.add(documentToCoin(document));
                                    break;
                                case RECEIVED:
                                    mReceivedCoins.add(documentToCoin(document));
                                    break;
                                default:
                                    Log.d(TAG, "Invalid collection argument");
                            }
                        }
                        event.onSuccess("Success");
                    } else {
                        event.onFailure(task.getException());
                    }
                });
    }

    /*
     *  @brief  { This procedure removes all documents within the specified collection
     *            argument. The caller is notified if the procedure was a success or failure }
     */
    public static void clearAllCoinsFromCollection(final String collection,
                                                   final OnEventListener<String> event) {
        // TODO: Clear all documents in specified collection
    }

    /*
     *  @brief  { The specified coin is added to the specified collection and corresponding
     *            ArrayList. If the specified collection is "uncollected", then the method
     *            informs the caller of the number of uncollected coins it has currently
     *            added. This is so the caller can identify when all of it's uncollected
     *            coins have successfully been added to firebase. The method also informs
     *            the caller if the document fails to upload }
     */
    public static void addCoinToCollection(final Coin coin,
                                           final String collection,
                                           final OnEventListener<Integer> event) {
        // Add coin to appropriate ArrayList
        switch (collection) {
            case UNCOLLECTED:
                mUncollectedCoins.add(coin);
                break;
            case COLLECTED:
                mCollectedCoins.add(coin);
                break;
            case RECEIVED:
                mReceivedCoins.add(coin);
                break;
            default:
                Log.d(TAG, "Invalid collection argument");
        }

        // Upload coin to specified collection on firebase
        String coinId = coin.getId();
        HashMap<String,Object> coinData = coin.getCoinMap();
        mUserDocRef.collection(collection).document(coinId).set(coinData)
                .addOnSuccessListener(aVoid -> {
                    if (collection == UNCOLLECTED) {
                        event.onSuccess(++mUncollectedCoinCount);
                    } else {
                        event.onSuccess(1);
                    }
                }).addOnFailureListener(event::onFailure);
    }

    /*
     *  @brief  { Removes the document corresponding to the coin argument in the specified
     *            collection, and the coin is also removed from the corresponding ArrayList.
     *            The caller is notified if the procedure was a success or failure }
     */
    public static void removeCoinFromCollection(Coin coin,
                                                final String collection,
                                                OnEventListener event) {
        // TODO: Remove coin fromm specified collection
    }

    /*
     *  @brief  { Creates a coin object from the document data }
     *
     *  @return { Coin object }
     */
    private static Coin documentToCoin(QueryDocumentSnapshot doc) {
        Map<String, Object> coinData = doc.getData();
        String id         = doc.getId();
        double value      = (double) coinData.get("value");
        String currency   = (String) coinData.get("currency");
        String symbol     = (String) coinData.get("symbol");
        String colour     = (String) coinData.get("colour");
        double latitude   = (double) coinData.get("latitude");
        double longitude  = (double) coinData.get("longitude");
        LatLng location   = new LatLng(latitude, longitude);
        return new Coin(id, value, currency, symbol, colour, location);
    }

    /*
     *  @brief  { Updates the "lastSavedDate" field on firebase }
     */
    public static void updateDate(String date) {
        mUserDocRef.update("lastSavedDate", date)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Date successfully updated"))
                .addOnFailureListener(e -> Log.d(TAG, "Date failed to update with exception ", e));
    }
}
