package mullan.sean.coinz;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;

public final class Data {

    /*
     *  This class serves as a global data structure available to all classes
     */

    private static DocumentReference mUserDocRef;
    private static ArrayList<Coin>   mUncollectedCoins;
    private static ArrayList<Coin>   mCollectedCoins;

    public static void setUserDocument(DocumentReference docRef) {
        Data.mUserDocRef = docRef;
    }

    public static void populateData(OnEventListener event) {
        // TODO: Retrieve data from all subcollections and populate fields
    }

    public static ArrayList<Coin> getUncollectedCoins() {
        return mUncollectedCoins;
    }

    public static void addUncollectedCoin(Coin coin, OnEventListener event) {
        // TODO: Add coin to uncollected coins subcollection and update mUncollectedCoins
    }

    public static void removeUncollectedCoin(Coin coin, OnEventListener event) {
        // TODO: Remove coin from uncollected subcollection and from mUncollectedCoins
    }

    public static void clearUncollectedCoins(OnEventListener event) {
        // TODO: Retrieve all coins in subcollection and then delete them using ID's
    }

    public static ArrayList<Coin> getCollectedCoins() {
        return mCollectedCoins;
    }

    public static void addCollectedCoin(Coin coin, OnEventListener event) {
        // TODO: Add coin to xollected coins subcollection and update mCollectedCoins
    }

    public static void removeCollectedCoin(Coin coin, OnEventListener event) {
        // TODO: Remove coin from uncollected subcollection and from mCollectedCoins
    }

    public static void clearCollectedCoins(OnEventListener event) {
        // TODO: Retrieve all coins in subcollection and then delete them using ID's
    }
}
