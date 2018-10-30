package mullan.sean.coinz;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class Data {

    /*
     *  This class serves as a global data structure available to all classes
     */

    private static DocumentReference mUserDocRef;
    private static ArrayList<Coin>   mUncollectedCoins;
    private static ArrayList<Coin>   mCollectedCoins;
    private static ArrayList<Coin>   mReceivedCoins;
    private static int               mUncollectedCoinCount;

    public static void init(DocumentReference docRef) {
        Data.mUserDocRef      = docRef;
        mUncollectedCoins     = new ArrayList<>();
        mCollectedCoins       = new ArrayList<>();
        mReceivedCoins        = new ArrayList<>();
        mUncollectedCoinCount = 0;
    }

    public static ArrayList<Coin> getUncollectedCoins() {
        return mUncollectedCoins;
    }

    public static ArrayList<Coin> getCollectedCoins() {
        return mCollectedCoins;
    }

    public static ArrayList<Coin> getmReceivedCoins() {
        return mReceivedCoins;
    }

    public static void retrieveAllCoinsFromCollection(final String collection, final OnEventListener<String> event) {
        mUserDocRef.collection(collection).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                switch (collection) {
                                    case "uncollected":
                                        mUncollectedCoins.add(documentToCoin(document));
                                        break;
                                    case "collected":
                                        mCollectedCoins.add(documentToCoin(document));
                                        break;
                                    case "received":
                                        mReceivedCoins.add(documentToCoin(document));
                                        break;
                                    default:
                                        Log.d("FBDATA", "Invalid collection argument");
                                }
                            }
                            event.onSuccess("Success");
                        } else {
                            event.onFailure(task.getException());
                        }
                    }
                });
    }

    public static void clearAllCoinsFromCollection(final String collection, final OnEventListener<String> event) {
        // TODO: Clear all documents in specified collection
    }

    public static void addCoinToCollection(final Coin coin, final String collection, final OnEventListener<Integer> event) {
        // Add coin to appropriate ArrayList
        switch (collection) {
            case "uncollected":
                mUncollectedCoins.add(coin);
                break;
            case "collected":
                mCollectedCoins.add(coin);
                break;
            case "received":
                mReceivedCoins.add(coin);
                break;
            default:
                Log.d("FBDATA", "Invalid collection argument");
        }
        // Upload coin to specified collection on firebase
        String coinId = coin.getId();
        HashMap<String,Object> coinData = coin.getCoinMap();
        mUserDocRef.collection(collection).document(coinId).set(coinData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // If we are uploading uncollected coins, then return the count of
                        // the number of uncollected coins that have been uploaded so far
                        if (collection == "uncollected") {
                            event.onSuccess(++mUncollectedCoinCount);
                        } else {
                            event.onSuccess(1);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                event.onFailure(e);
            }
        });
    }

    public static void removeCoinFromCollection(Coin coin, final String collection, OnEventListener event) {
        // TODO: Remove coin fromm specified collection
    }

    private static Coin documentToCoin(QueryDocumentSnapshot doc) {
        Map<String, Object> coinData = doc.getData();
        String id         = doc.getId();
        double value      = (double) coinData.get("value");
        String currency   = (String) coinData.get("currency");
        String symbol     = (String) coinData.get("symbol");
        String colour     = (String) coinData.get("colour");
        double longitude  = (double) coinData.get("longitude");
        double latitude   = (double) coinData.get("latitude");
        Location location = new Location(longitude, latitude);
        return new Coin(id, value, currency, symbol, colour, location);
    }

    public static void updateDate(String date) {
        mUserDocRef.update("lastSavedDate", date)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("FBDATA", "Date successfully updated");
                }
                }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("FBDATA", "Date failed to update with exception ", e);
                }
        });
    }
}
