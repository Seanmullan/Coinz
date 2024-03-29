package mullan.sean.coinz;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 *  This class serves as the applications global data structure that stores the relevant data
 *  accessible from all fragments so that no two fragments have to communicate between each
 *  other. These methods handle all uploading to and downloading from firebase.
 */
public final class Data {

    /*  Most of the variables are hopefully self explanatory, however I've explained the bottom
     *  two which are more ambiguous:
     *
     *  mCollectedTransferred:  The amount of collected coins (only collected, not received) the
     *                          user has transferred into their bank account on the current day
     *
     *  mUncollectedCoinCount:  The amount of coins that have currently been transferred into the
     *                          "uncollected" subcollection in firebase
     */

    public static final String UNCOLLECTED  = "uncollected";
    public static final String COLLECTED    = "collected";
    public static final String RECEIVED     = "received";
    public static final String FRIENDS      = "friends";
    public static final String REQUESTS     = "requests";
    public static final String TRANSACTIONS = "transactions";
    public static final String DOLR         = "DOLR";
    public static final String QUID         = "QUID";
    public static final String SHIL         = "SHIL";
    public static final String PENY         = "PENY";
    private static final String TAG         = "C_DATA";

    private static CollectionReference    mUsersRef;
    private static DocumentReference      mUserDocRef;
    private static DocumentSnapshot       mUserDocSnap;
    private static HashMap<String,Double> mExchangeRates;
    private static ArrayList<Coin>        mUncollectedCoins;
    private static ArrayList<Coin>        mCollectedCoins;
    private static ArrayList<Coin>        mReceivedCoins;
    private static ArrayList<User>        mFriends;
    private static ArrayList<User>        mRequests;
    private static ArrayList<Transaction> mTransactions;
    private static ArrayList<User>        mFriendLeaderBoard;
    private static ArrayList<User>        mGlobalLeaderBoard;
    private static double                 mGoldAmount;
    private static boolean                mBonusUsed;
    private static boolean                mBonusActive;
    private static int                    mCollectedTransferred;
    private static int                    mUncollectedCoinCount;

    // TESTING MODE FLAG
    private static boolean testMode;

    /**
     *  Initialise the document reference that will be used to identify
     *  the users document within firebase, and initialise local variables
     */
    public static void init(DocumentReference docRef, CollectionReference collRef, boolean test) {
        mUsersRef             = collRef;
        mUserDocRef           = docRef;
        mExchangeRates        = new HashMap<>();
        mUncollectedCoins     = new ArrayList<>();
        mCollectedCoins       = new ArrayList<>();
        mReceivedCoins        = new ArrayList<>();
        mFriends              = new ArrayList<>();
        mRequests             = new ArrayList<>();
        mTransactions         = new ArrayList<>();
        mFriendLeaderBoard    = new ArrayList<>();
        mGlobalLeaderBoard    = new ArrayList<>();
        mBonusUsed            = false;
        mBonusActive          = false;
        mGoldAmount           = 0;
        mCollectedTransferred = 0;
        mUncollectedCoinCount = 0;
        testMode              = test;
    }

    public static void setUserDocSnap(DocumentSnapshot docSnap) {
        mUserDocSnap = docSnap;
    }

    public static HashMap<String,Double> getRates() {
        return mExchangeRates;
    }

    public static double getGoldAmount() {
        return mGoldAmount;
    }

    public static void setGoldAmount(double gold) {
        mGoldAmount = gold;
    }

    public static boolean getBonusUsed() {
        return mBonusUsed;
    }

    public static boolean getBonusActive() {
        return mBonusActive;
    }

    public static void setBonusActive(boolean active) {
        mBonusActive = active;
    }

    public static void setCollectedTransferred(int transferred) {
        mCollectedTransferred = transferred;
    }

    public static int getCollectedTransferred() {
        return mCollectedTransferred;
    }

    public static ArrayList<Coin> getUncollectedCoins() {
        return mUncollectedCoins;
    }

    public static ArrayList<Coin> getCollectedCoins() {
        return mCollectedCoins;
    }

    public static ArrayList<Coin> getReceivedCoins() {
        return mReceivedCoins;
    }

    public static ArrayList<User> getFriends() {
        return mFriends;
    }

    public static ArrayList<User> getRequests() {
        return mRequests;
    }

    public static ArrayList<Transaction> getTransactions() {
        return mTransactions;
    }

    public static ArrayList<User> getFriendLeaderBoard() {
        return mFriendLeaderBoard;
    }

    public static ArrayList<User> getGlobalLeaderBoard() {
        return mGlobalLeaderBoard;
    }

    public static String getUsersEmail() {
        if (mUserDocSnap != null) {
            return mUserDocSnap.getString("email");
        } else {
            return "";
        }
    }

    /**
     *  Sets exchange rates HashMap and uploads rates data to firebase
     */
    public static void setRates(HashMap<String,Double> rates) {
        mExchangeRates = rates;
        if (!testMode) {
            mUsersRef.document("rates").set(rates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "[setRates] successful");
                        } else {
                            Log.d(TAG, "[setRates] failed");
                        }
                    });
        }
    }

    /**
     *  Sets the flag that indicates whether or not the user has used their
     *  daily bonus
     */
    public static void setBonusUsed(boolean bonusUsed) {
        mBonusUsed = bonusUsed;
        mUserDocRef.update("bonusUsed", bonusUsed)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "[setBonusUsed] success");
                    } else {
                        Log.d(TAG, "[setBonusUsed] failed" + task.getException());
                    }
                });
    }

    /**
     *  Resets the count of the number of collected coins the user has transferred
     *  into their bank account for the present day
     */
    public static void clearCollectedTransferred() {
        mCollectedTransferred = 0;
        mUserDocRef.update("collectedTransferred", 0)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "[clearCollectedTransferred] success");
                    } else {
                        Log.d(TAG, "[clearCollectedTransferred] failed" + task.getException());
                    }
                });
    }

    /**
     *  Retrieves rates data from firebase and sets the exchange rates
     *  HashMap, and informs caller of success or failure
     */
    public static void retrieveExchangeRates(OnEventListener<String> event) {
        mUsersRef.document("rates").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Log.d(TAG, "[retrieveExchangeRates] success");
                        DocumentSnapshot doc = task.getResult();
                        mExchangeRates.put("SHIL", doc.getDouble("SHIL"));
                        mExchangeRates.put("DOLR", doc.getDouble("DOLR"));
                        mExchangeRates.put("QUID", doc.getDouble("QUID"));
                        mExchangeRates.put("PENY", doc.getDouble("PENY"));
                        event.onSuccess("success");
                    } else {
                        Log.d(TAG, "[retrieveExchangeRates] failed");
                        event.onFailure(task.getException());
                    }
                });
    }


    /**
     *  This procedure fetches all documents within the specified collection
     *  argument. It identifies the collection and stores the retrieved coins
     *  in their respective ArrayLists. The caller is notified if the procedure
     *  was a success or failure. In the case of received coins, this list will
     *  be updated every time the user goes into their wallet (so it is up to date
     *  if they have received coins from someone), so we add the coin to the received
     *  list if it does not already exist there
     */
    public static void retrieveAllCoinsFromCollection(final String collection,
                                                      final OnEventListener<String> event) {
        Log.d(TAG, "[retrieveAllCoinsFromCollection] retrieving coins from " + collection);
        mUserDocRef.collection(collection).get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Coin coin = documentToCoin(document);
                            switch (collection) {
                                case UNCOLLECTED:
                                    mUncollectedCoins.add(coin);
                                    break;
                                case COLLECTED:
                                    mCollectedCoins.add(coin);
                                    break;
                                case RECEIVED:
                                    if (!mReceivedCoins.contains(coin)) {
                                        mReceivedCoins.add(coin);
                                    }
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

    /**
     *  This procedure removes documents within the specified collection
     *  argument, and removes the coin from the corresponding ArrayList.
     *  In the case of received coins, the coin should is kept if it was
     *  received on the present day, and deleted otherwise.
     */
    public static void clearOldCoinData(String collection) {
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
            // Do not delete the coin if it has been received today
            if (collection.equals(RECEIVED)) {
                if (c.getDate().equals(getCurrentDate())) {
                    continue;
                }
            }
            mUserDocRef.collection(collection).document(c.getId()).delete();
            i.remove();
        }
    }

    /**
     *  The specified coin is added to the specified collection and corresponding
     *  ArrayList. If the specified collection is "uncollected", then the method
     *  informs the caller of the number of uncollected coins it has currently
     *  added. This is so the caller can identify when all of it's uncollected
     *  coins have successfully been added to firebase. The method also informs
     *  the caller if the document fails to upload
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
        Map<String,Object> coinData = coin.getCoinMap();
        mUserDocRef.collection(collection).document(coinId).set(coinData)
                .addOnSuccessListener(aVoid -> {
                    if (collection.equals(UNCOLLECTED)) {
                        event.onSuccess(++mUncollectedCoinCount);
                    } else {
                        event.onSuccess(1);
                    }
                }).addOnFailureListener(event::onFailure);
    }

    /**
     *  Removes the document corresponding to the coin argument in the specified
     *  collection, and the coin is also removed from the corresponding ArrayList.
     *  The caller is notified when the procedure has finished. This method returns
     *  the number of coins that have been removed from a collection for a bank
     *  transfer, however only the Wallet fragment will use this number
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
                        Log.d(TAG, "[removeCoinFromCollection] removed coin " + coin.getId());
                        event.onSuccess("");
                    } else {
                        event.onFailure(task.getException());
                    }
        });
    }

    /**
     *  Retrieves all documents in the users friends subcollection, then creates a
     *  User object for each document and stores the object in an ArrayList if the
     *  friend does not already exist in the list (this case arises when friends
     *  fragment calls this method to retrieve the most up to date friends list).
     *  Caller is notified when procedure is complete
     */
    public static void retrieveAllFriends(OnEventListener<String> event) {
        Log.d(TAG, "[retrieveAllFriends] retrieving friends");
        mUserDocRef.collection(FRIENDS).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = documentToUser(document);
                            if (!mFriends.contains(user)) {
                                mFriends.add(user);
                            }
                        }
                        Log.d(TAG, "[retrieveAllFriends] success");
                        event.onSuccess("Success");
                    } else {
                        Log.d(TAG, "[retrieveAllFriends] failed to retrieve friends");
                        event.onFailure(task.getException());
                    }
                });
    }

    /**
     *  Retrieves all documents in the users requests subcollection, then creates a
     *  User object for each document and stores the object in an ArrayList if the
     *  request does not already exist in the list (this case arises when friends
     *  fragment calls this method to retrieve the most up to date requests list).
     *  Caller is notified when procedure is complete
     */
    public static void retrieveAllRequests(OnEventListener<String> event) {
        Log.d(TAG, "[retrieveAllRequests] retrieving friend requests");
        mUserDocRef.collection(REQUESTS).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = documentToUser(document);
                            if (!mRequests.contains(user)) {
                                mRequests.add(user);
                            }
                        }
                        Log.d(TAG, "[retrieveAllRequests] success");
                        event.onSuccess("Success");
                    } else {
                        Log.d(TAG, "[retrieveAllRequests] failed to retrieve requests");
                        event.onFailure(task.getException());
                    }
                });
    }

    /**
     *  Retrieves all documents in the users transactions subcollection,
     *  then creates a Transaction object for each document and stores
     *  the objects in an ArrayList
     */
    public static void retrieveAllTransactions() {
        Log.d(TAG, "[retrieveAllTransactions] retrieving transactions");
        mUserDocRef.collection(TRANSACTIONS).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            mTransactions.add(documentToTransaction(document));
                        }
                        Log.d(TAG, "[retrieveAllTransactions] success");
                    } else {
                        Log.d(TAG, "[retrieveAllTransaction] failed to retrieve transactions");
                    }
                });
    }

    /**
     *  Performs three steps to accept a friend request:
     *   1) Remove friend from users requests subcollection
     *   2) Add friend to users friends subcollection
     *   3) Add user to the requester's friends subcollection
     */
    public static void acceptFriendRequest(User friend) {
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
        friendMap.put("email", friend.getEmail());
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
        userMap.put("email", mUserDocSnap.getString("email"));
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

    /**
     *   Removes friend requester from users requests subcollection
     */
    public static void declineFriendRequest(User friend) {
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

    /**
     *  Performs query to locate user with the specified email address. When
     *  located, the requester (this user) will be placed in the located users
     *  "requests" subcollection
     */
    public static void sendFriendRequest(String email, OnEventListener<String> event) {
        Log.d(TAG, "[sendFriendRequest] sending friend request...");
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
                            String friendId  = result.getDocuments().get(0).getId();
                            String userId    = mUserDocSnap.getId();
                            String username  = mUserDocSnap.getString("username");
                            String userEmail = mUserDocSnap.getString("email");
                            HashMap<String, Object> userMap = new HashMap<>();
                            userMap.put("username", username);
                            userMap.put("email", userEmail);
                            mUsersRef.document(friendId).collection(REQUESTS).document(userId)
                                    .set(userMap).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "[sendFriendRequest] success");
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

    /**
     *  Removes coin from the collection it currently exists in, then places coin
     *  in the received subcollection of the specified friend. A random ID will be
     *  generated for the coin document. This is to avoid duplicate coin ID's in the
     *  case that two players send the same coin to a third player. The date that the
     *  coin is sent is added to the document.
     */
    public static void sendCoinToFriend(User friend, Coin coin, String collection,
                                        OnEventListener<String> event) {

        removeCoinFromCollection(coin, collection, new OnEventListener<String>() {
            @Override
            public void onSuccess(String str) {}
            @Override
            public void onFailure(Exception e) {}
        });

        // Add coin to friends received collection
        Map<String,Object> coinData = coin.getCoinMap();
        coinData.put("date", getCurrentDate());
        mUsersRef.document(friend.getUserID()).collection(RECEIVED).add(coinData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        event.onSuccess("");
                    } else {
                        event.onFailure(task.getException());
                    }
        });
    }

    /**
     *  Updates the number of collected coins that the user has transferred into their
     *  bank account for the current day, adds the value of gold from the transaction
     *  to the current value of gold, then updates this value on the users document. The
     *  transaction object is then added to the users transactions subcollection
     */
    public static void addTransaction(Transaction transaction, int numberProcessed,
                                      String collection) {
        mGoldAmount += transaction.getGoldAdded();
        if (collection.equals(COLLECTED)) {
            mCollectedTransferred += numberProcessed;
        }
        mTransactions.add(transaction);

        mUserDocRef.update("collectedTransferred", mCollectedTransferred)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "[addTransaction] collected transferred updated");
                    } else {
                        Log.d(TAG, "[addTransaction] failed to collected transferred");
                    }
                });

        mUserDocRef.update("gold", mGoldAmount)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "[addTransaction] gold updated");
                    } else {
                        Log.d(TAG, "[addTransaction] failed to update gold");
                    }
                });
        mUserDocRef.collection(TRANSACTIONS).add(transaction.getTransactionMap())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "[addTransaction] transaction added");
                    } else {
                        Log.d(TAG, "[addTransaction] failed to add transaction");
                    }
                });
    }

    /**
     *  Finds all users from firebase, and an object is then created for each user
     *  so they can be placed on the global leader board. If the user is also in the
     *  friends list, then they are added to the friends leader board
     */
    public static void retrieveLeaderBoard(OnEventListener<String> event) {
        Log.d(TAG, "[retrieveLeaderBoard] retrieving leader board");
        mUsersRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Ignore the rates document within users
                            if (document.getId().equals("rates")) {
                                continue;
                            }
                            User user = documentToUser(document);
                            if (!mGlobalLeaderBoard.contains(user)) {
                                mGlobalLeaderBoard.add(user);
                            }
                            if (mFriends.contains(user) && !mFriendLeaderBoard.contains(user)) {
                                mFriendLeaderBoard.add(user);
                            }
                        }
                        Log.d(TAG, "[retrieveLeaderBoard] success");
                        event.onSuccess("Success");
                    } else {
                        event.onFailure(task.getException());
                    }
                });
    }

    /**
     *  Creates a coin object from the document data
     *
     *  @return Coin object
     */
    private static Coin documentToCoin(QueryDocumentSnapshot doc) {
        Map<String, Object> coinData = doc.getData();
        String id         = doc.getId();
        double value      = (double) coinData.get("value");
        String currency   = (String) coinData.get("currency");
        double latitude   = (double) coinData.get("latitude");
        double longitude  = (double) coinData.get("longitude");
        LatLng location   = new LatLng(latitude, longitude);
        String date       = (String) coinData.get("date");
        return new Coin(id, value, currency, location, date);
    }

    /**
     *  Creates a user object from the document data. Adds check for null value of
     *  gold - this is because the friends and requests subcollection's only contain
     *  username and email, so we ignore the value of gold by setting it to 0
     *
     *  @return User object
     */
    private static User documentToUser(QueryDocumentSnapshot doc) {
        Map<String, Object> userData = doc.getData();
        String uid      = doc.getId();
        String username = (String) userData.get("username");
        String email    = (String) userData.get("email");
        double gold = 0;
        if (!(userData.get("gold") == null)) {
            gold = (double) userData.get("gold");
        }
        return new User(uid, username, email, gold);
    }

    /**
     *  Creates a transaction object from the document data
     *
     *  @return Transaction object
     */
    private static Transaction documentToTransaction(QueryDocumentSnapshot doc) {
        Map<String, Object> transData = doc.getData();
        String date = (String) transData.get("date");
        double gold = (double) transData.get("gold");
        return new Transaction(gold, date);
    }

    /**
     *  Updates the "lastSavedDate" field on firebase
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

    /**
     *  @return  Current date in format yyyy/mm/dd
     */
    private static String getCurrentDate() {
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.ENGLISH);
        return dateFormat.format(date);
    }
}
