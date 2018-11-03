package mullan.sean.coinz;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class Data {

    /*
     *  This class serves as a global data structure that uploads and downloads from
     *  the firebase database
     */

    public static final String UNCOLLECTED = "uncollected";
    public static final String COLLECTED   = "collected";
    public static final String RECEIVED    = "received";
    public static final String FRIENDS     = "friends";
    public static final String REQUESTS    = "requests";
    public static final String DOLR        = "DOLR";
    public static final String QUID        = "QUID";
    public static final String SHIL        = "SHIL";
    public static final String PENY        = "PENY";
    private static final String TAG        = "C_DATA";

    private static CollectionReference mUsersRef;
    private static DocumentReference   mUserDocRef;
    private static DocumentSnapshot    mUserDocSnap;
    private static ArrayList<Coin>     mUncollectedCoins;
    private static ArrayList<Coin>     mCollectedCoins;
    private static ArrayList<Coin>     mReceivedCoins;
    private static ArrayList<Friend>   mFriends;
    private static ArrayList<Friend>   mRequests;
    private static int                 mUncollectedCoinCount;
    private static int                 mFriendTransferCount;
    private static int                 mBankTransferCount;

    /*
     *  @brief  { Initialise the document reference that will be used to identify
     *            the users document within firebase, and initialise local variables }
     */
    public static void init(DocumentReference docRef, CollectionReference collRef) {
        mUsersRef             = collRef;
        mUserDocRef           = docRef;
        mUncollectedCoins     = new ArrayList<>();
        mCollectedCoins       = new ArrayList<>();
        mReceivedCoins        = new ArrayList<>();
        mFriends              = new ArrayList<>();
        mRequests             = new ArrayList<>();
        mUncollectedCoinCount = 0;
        mFriendTransferCount  = 0;
        mBankTransferCount    = 0;
    }

    public static void setUserDocSnap(DocumentSnapshot docSnap) {
        mUserDocSnap = docSnap;
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
    public static ArrayList<Coin> getCollectedCoins() {
        return mCollectedCoins;
    }

    /*
     *  @return  { ArrayList of received coins }
     */
    public static ArrayList<Coin> getReceivedCoins() {
        return mReceivedCoins;
    }

    /*
     *  @return  { ArrayList of users friends }
     */
    public static ArrayList<Friend> getFriends() {
        return mFriends;
    }

    /*
     *  @return  { ArrayList of users friend requests }
     */
    public static ArrayList<Friend> getRequests() {
        return mRequests;
    }

    /*
     *  @brief  { This procedure fetches all documents within the specified collection
     *            argument. It identifies the collection and stores the retrieved coins
     *            in their respective ArrayLists. The caller is notified if the procedure
     *            was a success or failure }
     */
    public static void retrieveAllCoinsFromCollection(final String collection,
                                                      final OnEventListener<String> event) {
        Log.d(TAG, "[retrieveAllCoinsFromCollection] retrieving coins from " + collection);
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
                        Log.d(TAG, "[retrieveAllCoinsFromCollection] success");
                        event.onSuccess("Success");
                    } else {
                        event.onFailure(task.getException());
                    }
                });
    }

    /*
     *  @brief  { This procedure removes all documents within the specified collection
     *            argument, and removes the coin from the corresponding ArrayList }
     */
    @SuppressWarnings("unchecked")
    public static void clearAllCoinsFromCollection(String collection) {
        Log.d(TAG, "[clearAllCoinsFromCollection] clearing coins from " + collection);
        Iterator<Coin> i;
        switch (collection) {
            case UNCOLLECTED:
                i = mUncollectedCoins.iterator();
                break;
            case COLLECTED:
                i = mCollectedCoins.iterator();
                break;
            case RECEIVED:
                i = mReceivedCoins.iterator();
                break;
            default:
                i = Collections.emptyIterator();
                Log.d(TAG, "[clearAllCoinsFromCollection] invalid collection argument");
        }
        while (i.hasNext()) {
            Coin c = i.next();
            mUserDocRef.collection(collection).document(c.getId()).delete();
            i.remove();
        }
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
                Log.d(TAG, "[addCoinToCollection] invalid collection argument");
        }

        // Upload coin to specified collection on firebase
        String coinId = coin.getId();
        HashMap<String,Object> coinData = coin.getCoinMap();
        mUserDocRef.collection(collection).document(coinId).set(coinData)
                .addOnSuccessListener(aVoid -> {
                    if (collection.equals(UNCOLLECTED)) {
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
                                                OnEventListener<String> event) {
        Log.d(TAG,  "[removeCoinFromCollection] removing coin from " + collection);
        // Remove coin from appropriate ArrayList
        switch (collection) {
            case UNCOLLECTED:
                mUncollectedCoins.remove(coin);
                break;
            case COLLECTED:
                mCollectedCoins.remove(coin);
                break;
            case RECEIVED:
                mReceivedCoins.remove(coin);
                break;
            default:
                Log.d(TAG, "[removeCoinFromCollection] invalid collection argument");
        }

        mUserDocRef.collection(collection).document(coin.getId()).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "COIN REMOVED");
                        event.onSuccess("success");
                    } else {
                        event.onFailure(task.getException());
                    }
        });
        event.onSuccess("success");
    }

    /*
     *  @brief  { Retrieves all documents in the users friends subcollection,
     *            then creates a Friend object for each document and stores
     *            the objects in an ArrayList }
     */
    public static void retrieveAllFriends() {
        Log.d(TAG, "[retrieveAllFriends] retrieving friends");
        mUserDocRef.collection(FRIENDS).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            mFriends.add(documentToFriend(document));
                        }
                        Log.d(TAG, "[retrieveAllFriends] success");
                    } else {
                        Log.d(TAG, "[retrieveAllFriends] failed to retrieve friends");
                    }
                    });
    }

    /*
     *  @brief  { Retrieves all documents in the users request's subcollection,
     *            then creates a Friend object for each document and stores
     *            the objects in an ArrayList }
     */
    public static void retrieveAllRequests() {
        Log.d(TAG, "[retrieveAllRequests] retrieving friend requests");
        mUserDocRef.collection(REQUESTS).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            mRequests.add(documentToFriend(document));
                        }
                        Log.d(TAG, "[retrieveAllRequests] success");
                    } else {
                        Log.d(TAG, "[retrieveAllRequests] failed to retrieve friends");
                    }
                });
    }

    /*
     *  @brief  { Performs three steps to accept a friend request:
     *             1) Remove friend from users requests subcollection
     *             2) Add friend to users friends subcollection
     *             3) Add user to the requester's friends subcollection
     */
    public static void acceptFriendRequest(Friend friend) {
        // Remove friend from users requests subcollection
        mRequests.remove(friend);
        String friendId = friend.getUserID();
        mUserDocRef.collection(REQUESTS).document(friendId).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG,
                        "[acceptFriendRequest] successfully removed from requests: " + friendId);
                    } else {
                        Log.d(TAG,
                        "[acceptFriendRequest] failed to remove from requests: " + friendId);
                    }
                });

        // Add friend to users friends subcollection
        mFriends.add(friend);
        HashMap<String,String> friendMap = new HashMap<>();
        friendMap.put("username", friend.getUsername());
        mUserDocRef.collection(FRIENDS).document(friendId).set(friendMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG,
                                "[acceptFriendRequest] successfully added friend: " + friendId);
                    } else {
                        Log.d(TAG,
                                "[acceptFriendRequest] failed to add friend: " + friendId);
                    }
                });

        // Add user to the requester's friends subcollection
        HashMap<String,String> userMap = new HashMap<>();
        userMap.put("username", mUserDocSnap.getString("username"));
        mUsersRef.document(friendId).collection(FRIENDS).document(mUserDocRef.getId()).set(userMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG,
                        "[acceptFriendRequest] successfully added user to requester's friends");
                    } else {
                        Log.d(TAG,
                        "[acceptFriendRequest] failed to add user to requester's friends");
                    }
                });
    }

    /*
     *  @brief  {  Removes friend requester from users requests subcollection
     */
    public static void declineFriendRequest(Friend friend) {
        mRequests.remove(friend);
        String friendId = friend.getUserID();
        mUserDocRef.collection(REQUESTS).document(friendId).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG,
                        "[declineFriendRequest] successfully removed from requests: " + friendId);
                    } else {
                        Log.d(TAG,
                        "[declineFriendRequest] failed to remove from requests: " + friendId);
                    }
                });
    }

    /*
     *  @brief  { Performs query to locate user with the specified email address. When
     *            located, the requester (this user) will be placed in the located users
     *            "requests" subcollection }
     */
    public static void sendFriendRequest(String email, OnEventListener<String> event) {
        // Perform query to find user with specified email address
        mUsersRef.whereEqualTo("email", email).get()
                .addOnCompleteListener(queryTask -> {

                    if (queryTask.isSuccessful() && queryTask.getResult() != null) {
                        QuerySnapshot result = queryTask.getResult();

                        // If no documents are found, then inform user that the friend
                        // request failed
                        if (result.isEmpty()) {
                            Log.d(TAG, "No users found");
                            Exception e = new Exception("No users found");
                            event.onFailure(e);

                        // Otherwise, extract the details from the document (there will
                        // always only be one document as user's emails are unique) and
                        // place the current user in the located user's requests subcollection
                        } else {
                            String friendId = result.getDocuments().get(0).getId();
                            String userId = mUserDocSnap.getId();
                            String username = mUserDocSnap.getString("username");
                            String userEmail = mUserDocSnap.getString("email");
                            HashMap<String, Object> userMap = new HashMap<>();
                            userMap.put("username", username);
                            userMap.put("email", userEmail);
                            mUsersRef.document(friendId).collection(REQUESTS).document(userId)
                                    .set(userMap).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    event.onSuccess("success");
                                } else {
                                    event.onFailure(task.getException());
                                }

                            });
                        }
                    } else {
                        event.onFailure(queryTask.getException());
                    }
                });
    }

    /*
     *  @brief  { Removes coin from the collection it currently exists in, then places coin
     *            in the received subcollection of the specified friend. A random ID will be
     *            generated for the coin document. This is to avoid duplicate coin ID's in the
     *            case that two players send the same coin to a third player. Upon successful
     *            completion of sending the coin, the method caller is provided with the number
     *            of coins that have currently been transferred - this is so the caller can
     *            identify when all coins have been transferred }
     */
    public static void sendCoinToFriend(Friend friend, Coin coin, String collection,
                                        OnEventListener<Integer> event) {
        // Remove coin from ArrayList
        switch (collection) {
            case COLLECTED:
                mCollectedCoins.remove(coin);
                break;
            case RECEIVED:
                mReceivedCoins.remove(coin);
                break;
            default:
                Log.d(TAG, "[sendCoinToFriend] collection not recognised");
                break;
        }

        // Remove coin from collection
        mUserDocRef.collection(collection).document(coin.getId()).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "[sendCoinToFriend] successfully removed coin");
                    } else {
                        Log.d(TAG, "[sendCoinToFriend] failed to remove coin");
                    }
                });

        // Add coin to friends received collection
        mUsersRef.document(friend.getUserID()).collection(RECEIVED).add(coin.getCoinMap())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        event.onSuccess(++mFriendTransferCount);
                    } else {
                        event.onFailure(task.getException());
                    }
        });
    }

    public static void clearFriendTransferCount() { mFriendTransferCount = 0; }

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
     *  @brief  { Creates a friend object from the document data }
     *
     *  @return { Friend object }
     */
    private static Friend documentToFriend(QueryDocumentSnapshot doc) {
        Map<String, Object> friendData = doc.getData();
        String uid      = doc.getId();
        String username = (String) friendData.get("username");
        String email    = (String) friendData.get("email");
        return new Friend(uid, username, email);
    }

    /*
     *  @brief  { Updates the "lastSavedDate" field on firebase }
     */
    public static void updateDate(String date) {
        Log.d(TAG, "[updateDate] updating date");
        mUserDocRef.update("lastSavedDate", date).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "[updateDate] successful");
            } else {
                Log.d(TAG, "[updateDate] failed " + task.getException());
            }
        });
    }
}
