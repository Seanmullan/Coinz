package mullan.sean.coinz;

import android.content.Context;
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
    private static int               mCount;

    public static void init(DocumentReference docRef) {
        Data.mUserDocRef   = docRef;
        mUncollectedCoins  = new ArrayList<>();
        mCollectedCoins    = new ArrayList<>();
        mReceivedCoins     = new ArrayList<>();
        mCount             = 0;
    }

    public static void retrieveExistingData(final OnEventListener<String> event) {
        // Retrieve uncollected coins
        mUserDocRef.collection("uncollected").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                mUncollectedCoins.add(documentToCoin(document));
                            }
                        } else {
                            event.onFailure(task.getException());
                        }
                    }
                });

        // Retrieve collected coins
        mUserDocRef.collection("collected").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                mCollectedCoins.add(documentToCoin(document));
                            }
                        } else {
                            event.onFailure(task.getException());
                        }
                    }
                });

        // Retrieve received coins
        mUserDocRef.collection("received").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                mReceivedCoins.add(documentToCoin(document));
                            }
                        } else {
                            event.onFailure(task.getException());
                        }
                    }
                });

        // Let MainActivity know that all data has been retrieved
        event.onSuccess("success");
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

    public static ArrayList<Coin> getUncollectedCoins() {
        return mUncollectedCoins;
    }

    public static void addUncollectedCoin(final Coin coin, final OnEventListener<Integer> event) {
        mUncollectedCoins.add(coin);
        String coinId = coin.getId();
        HashMap<String,Object> coinData = coin.getCoinMap();
        mUserDocRef.collection("uncollected").document(coinId).set(coinData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    event.onSuccess(++mCount);
                }
                }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    event.onFailure(e);
                }
        });
    }

    public static void removeUncollectedCoin(Coin coin, OnEventListener event) {
        // TODO: Remove coin from uncollected subcollection and from mUncollectedCoins
    }

    public static void clearUncollectedCoins(Context con, final OnEventListener event) {

    }

    public static ArrayList<Coin> getCollectedCoins() {
        return mCollectedCoins;
    }

    public static void addCollectedCoin(Coin coin, OnEventListener event) {
        // TODO: Add coin to collected coins subcollection and update mCollectedCoins
    }

    public static void removeCollectedCoin(Coin coin, OnEventListener event) {
        // TODO: Remove coin from uncollected subcollection and from mCollectedCoins
    }

    public static void clearCollectedCoins(OnEventListener event) {
        // TODO: Retrieve all coins in subcollection and then delete them using ID's
        event.onSuccess("success");
    }
}
