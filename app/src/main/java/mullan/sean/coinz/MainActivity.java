package mullan.sean.coinz;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;

import org.json.*;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "C_MAIN";

    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseFirestore mFirestore;
    private DocumentSnapshot  mUserDoc;
    private FirebaseAuth      mAuth;
    private JSONArray         mCoinData;
    private String            mLastSavedDate;
    private String            mCurrentDate;

    // TESTING MODE FLAG
    private boolean testMode;

    /*
     *  @brief  { Set main activity view, load in map fragment as default
     *            and create a listener for the user authentication state }
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            testMode = extras.getBoolean("testMode");
        }
        Log.d(TAG, "TESTING MODE:" + testMode);

        // Get firestore and authentication references
        mFirestore = FirebaseFirestore.getInstance();
        mAuth      = FirebaseAuth.getInstance();

        // Get user document reference. Upon successful completion of this method,
        // populateData() will be called in order to retrieve new data if a new day
        // has begun, or existing data otherwise.
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            getUserDocument(user.getUid());
        }

        // Set up toolbar
        Toolbar mTopToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(mTopToolbar);

        // Set up bottom navigation
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Create firebase authentication listener
        mAuthListener = firebaseAuth -> {
            if (mAuth.getCurrentUser() == null) {
                // user auth state is changed - user is null
                Log.d(TAG, "[onCreate] User is null");
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        };
    }

    /*
     *  @brief  { Controls the basic navigation of the application. Navigation is
     *            disabled whilst the user is playing the daily bonus game. When a
     *            button is selected on the navigation bar, the corresponding
     *            fragment is loaded }
     */
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
                if (Data.getBonusActive()) {
                    return false;
                }
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.navigation_map:
                        fragment = new MapFragment();
                        loadFragment(fragment);
                        return true;
                    case R.id.navigation_friends:
                        fragment = new FriendsFragment();
                        loadFragment(fragment);
                        return true;
                    case R.id.navigation_bank:
                        fragment = new BankFragment();
                        loadFragment(fragment);
                        return true;
                    case R.id.navigation_wallet:
                        fragment = new WalletFragment();
                        loadFragment(fragment);
                        return true;
                    case R.id.navigation_leaderboard:
                        fragment = new LeaderBoardFragment();
                        loadFragment(fragment);
                        return true;
                }
                return false;
            };

    /*
     *  @brief { Load the appropriate fragment into the container }
     *
     *  @params  { The fragment to be loaded }
     */
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /*
     *  @brief  { Set up the toolbar overflow menu }
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    /*
     *  @brief  { Handles logic for overflow menu, including log out }
     *  TODO: Add a "help" option to menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                mAuth.signOut();
                return true;

            default:
                // Action was not recognized, invoke the superclass to handle it
                return super.onOptionsItemSelected(item);
        }
    }

    /*
     *  @brief  { This method begins the process of data retrieval upon boot up.
     *            The first step is to retrieve the users document, then the Data
     *            class is populated given the users document. See the populateData()
     *            method for details. }
     */
    private void getUserDocument(final String uid) {
        DocumentReference   docRef = mFirestore.collection("users").document(uid);
        CollectionReference collRef = mFirestore.collection("users");
        Data.init(docRef, collRef, testMode);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                // Initialise user document reference and retrieve map data
                mUserDoc = document;
                Data.setUserDocSnap(document);

                // If the app is in testing mode, set the last saved date to 0000/00/00 to
                // ensure that the last saved date and current date are not equal. This will
                // make the app run through the full procedure of a new day beginning
                if (!testMode) {
                    mLastSavedDate = mUserDoc.getString("lastSavedDate");
                } else {
                    mLastSavedDate = "0000/00/00";
                }
                mCurrentDate = getCurrentDate();

                if (mLastSavedDate == null || mCurrentDate == null) {
                    Log.d(TAG, "Saved date or current date is null");
                    return;
                }

                // Get amount of gold user has
                Double gold = document.getDouble("gold");
                if (gold != null) {
                    Data.setGoldAmount(gold);
                } else {
                    Log.d(TAG, "[getUserDocument] gold is null");
                }

                // Get amount of collected coins user has transferred into bank account today
                Double transferred = document.getDouble("collectedTransferred");
                if (transferred != null) {
                    Data.setCollectedTransferred(transferred.intValue());
                } else {
                    Log.d(TAG, "[getUserDocument] collected transferred is null");
                }

                // If a new day has begun, set bonusUsed to false and update the flag on firebase
                if (!mCurrentDate.equals(mLastSavedDate)) {
                    Data.setBonusUsed(false);
                } else {
                    // Otherwise, get bonusUsed flag and set it in the Data class
                    Boolean bonusUsed = document.getBoolean("bonusUsed");
                    if (bonusUsed != null) {
                        Data.setBonusUsed(bonusUsed);
                    } else {
                        Log.d(TAG, "[getUserDocument] bonus used is null");
                    }
                }

                // Load map fragment by default
                loadFragment(new MapFragment());

                // Invoke function to fetch data from users subcollection's on firebase
                populateData();
            } else {
                Log.d(TAG, "User document get failed with ", task.getException());
            }
        });
    }

    /*
     *  @brief  { This procedure will prompt the data class to pull in the appropriate
     *            data from firebase and populate the relevant fields in the Data class.
     *
     *            We want to retrieve existing data in all cases (that is, either it is the
     *            same day or a new day has begun). If it is the same day, then we use the
     *            retrieved existing data to populate the Data class. If it is a new day,
     *            then we use the existing data to identify what documents need to be deleted
     *            in firebase (i.e. for clearing the local wallet)
     *
     *            If a new day has begun, then we want to execute the following steps:
     *              1) Update the last saved date for the user on firebase
     *              2) Clear uncollected coins so that they can be replaced with new coins
     *              3) Clear 'spare change' i.e. collected coins and received coins
     *              4) Retrieve the new map data from the Informatics server
     *              5) Parse the received data
     *              6) Update the Data class fields and firebase with the new data
     *
     *            When these processes are completed, the Map Fragment is prompted to
     *            retrieve the most up to date data from the Data class.
     *
     *           Note: We have to retrieve the existing data regardless, as the ID's
     *           are required in order to identify which coins we want to remove }
     */
    private void populateData() {

        // If a new day has not begun, then retrieve the existing exchange rates. If a new
        // day has begun, then the new exchange rates will be retrieved when retrieveNewMapData()
        // is called
        if (mCurrentDate.equals(mLastSavedDate)) {
            Data.retrieveExchangeRates(new OnEventListener<String>() {
                @Override
                public void onSuccess(String object) {
                    WalletFragment.updateRates();
                }
                @Override
                public void onFailure(Exception e) {
                    Log.d(TAG, "Failed to retrieve exchange rates with exception ", e);
                }
            });
        }

        // Retrieve all uncollected coins, and use this data to remove these coins if
        // a new day has begun. Then prompt the application to retrieve new data if it
        // is a new day. Also, if a new day has begun, update the date and reset the
        // variable that stores the number of collected coins that the user has transferred
        // into their bank account for that day
        Data.retrieveAllCoinsFromCollection(Data.UNCOLLECTED, new OnEventListener<String>() {
            @Override
            public void onSuccess(String object) {
                Log.d(TAG, "Successfully retrieved uncollected coins");
                if (!mCurrentDate.equals(mLastSavedDate)) {
                    Data.updateDate(getCurrentDate());
                    Data.clearCollectedTransferred();
                    Data.clearAllCoinsFromCollection(Data.UNCOLLECTED);
                    retrieveNewMapData();
                }
                MapFragment.updateMapData(getApplicationContext());
            }
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "Failed to retrieve uncollected coins with exception ", e);
            }
        });

        // Retrieve all collected coins. Again, use this data to remove these coins if a
        // new day has begun
        Data.retrieveAllCoinsFromCollection(Data.COLLECTED, new OnEventListener<String>() {
            @Override
            public void onSuccess(String object) {
                Log.d(TAG, "Successfully retrieved collected coins");
                if (!mCurrentDate.equals(mLastSavedDate)) {
                    Data.clearAllCoinsFromCollection(Data.COLLECTED);
                }
            }
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "Failed to retrieve collected coins with exception ", e);
            }
        });

        //Retrieve all received coins, and remove them if a new day has begun
        Data.retrieveAllCoinsFromCollection(Data.RECEIVED, new OnEventListener<String>() {
            @Override
            public void onSuccess(String object) {
                Log.d(TAG, "Successfully retrieved received coins");
                if (!mCurrentDate.equals(mLastSavedDate)) {
                    Data.clearAllCoinsFromCollection(Data.RECEIVED);
                }
            }
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "Failed to retrieve received coins with exception ", e);
            }
        });

        // Retrieve the users friends and friend requests
        Data.retrieveAllFriends(new OnEventListener<String>() {
            @Override
            public void onSuccess(String object) {
                // Retrieve leader board once friends list is populated
                Data.retrieveLeaderBoard(new OnEventListener<String>() {
                    @Override
                    public void onSuccess(String object) {}
                    @Override
                    public void onFailure(Exception e) {
                        Log.d(TAG, "Failed to retrieve leader board with exception ", e);
                    }
                });
            }
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "Failed to retrieve friends with exception ", e);
            }
        });
        Data.retrieveAllRequests(new OnEventListener<String>() {
            @Override
            public void onSuccess(String object) {}
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "Failed to retrieve requests with exception ", e);
            }
        });

        // Retrieve all transactions
        Data.retrieveAllTransactions();
    }

    /*
     *  @brief  { Retrieves that json map data from Informatics server. Upon successful
     *            retrieval, processNewMapData is called to parse the data and store it
     *            in the Data class }
     */
    private void retrieveNewMapData() {
        String mapUrl  = "http://homepages.inf.ed.ac.uk/stg/coinz/";
        String mapJson = "/coinzmap.geojson";
        String url;
        if (testMode) {
            url = mapUrl + "2018/01/01" + mapJson;
        } else {
            url = mapUrl + mCurrentDate + mapJson;
        }
        DownloadFileTask downloadTask = new DownloadFileTask(new OnEventListener<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    Log.d(TAG, "Successfully retrieved map data from Informatics server");
                    processNewMapData(result);
                } catch (JSONException e) {
                    Log.d(TAG, "Failed to parse json data" + e.toString());
                }
            }
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "Download task failed: " + e.toString());
            }
        });
        downloadTask.execute(url);
    }

    /*
     *  @return  { Current date in format yyyy/mm/dd }
     */
    private String getCurrentDate() {
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.ENGLISH);
        return dateFormat.format(date);
    }

    /*
     *  @brief  { Extracts the json data into exchange rates and coin data.
     *            The coin data is parsed and a Coin object is created, which
     *            is then passed to the Data class for local storage and
     *            firebase storage }
     */
    @SuppressWarnings("unchecked")
    private void processNewMapData(String json) throws JSONException {
        JSONObject mapData = new JSONObject(json);
        JSONObject mExchangeRates = mapData.getJSONObject("rates");
        mCoinData          = mapData.getJSONArray("features");

        HashMap<String,Double> rates = new HashMap<>();
        rates.put("SHIL", mExchangeRates.getDouble("SHIL"));
        rates.put("DOLR", mExchangeRates.getDouble("DOLR"));
        rates.put("QUID", mExchangeRates.getDouble("QUID"));
        rates.put("PENY", mExchangeRates.getDouble("PENY"));

        Data.setRates(rates);

        for (int i = 0; i < mCoinData.length(); i++) {
            // Extract json data
            final JSONObject coinJson = mCoinData.getJSONObject(i);
            JSONObject coinProperties = coinJson.getJSONObject("properties");
            JSONArray  coords = coinJson.getJSONObject("geometry").getJSONArray("coordinates");

            // Parse json data into coin attributes
            final String id = coinProperties.getString("id");
            Double value    = Double.parseDouble(coinProperties.getString("value"));
            String currency = coinProperties.getString("currency");
            LatLng location = new LatLng(coords.getDouble(1), coords.getDouble(0));

            // Create coin from parsed data and add it to uncollected coins in Data class
            Coin coin = new Coin(id, value, currency, location);
            Data.addCoinToCollection(coin, Data.UNCOLLECTED, new OnEventListener() {
                @Override
                public void onSuccess(Object object) {
                    Log.d(TAG, "Coin successfully added with ID: " + id);
                    if ((int) object == mCoinData.length()) {
                        Log.d(TAG, "All coins successfully processed");
                    }
                }
                /*
                 * If data fails to upload to firebase, then prompt the user to check their internet
                 * connection and restart the application. We also reset the "lastSavedDate" so the
                 * application can restart the data retrieval process (otherwise it would try to
                 * retrieve existing data that wasn't correct
                 */
                @Override
                public void onFailure(Exception e) {
                    Log.d(TAG, "Failed to add coin with exception ", e);
                    Toast.makeText(MainActivity.this,
                            R.string.msgCoinAddFailure,
                            Toast.LENGTH_SHORT).show();
                    Data.updateDate("");
                }
            });
        }
        // Update the map after the coins have been locally added to the Data class
        MapFragment.updateMapData(getApplicationContext());
    }

    /*
     *  @brief  { Add authentication state listener to firebase authentication
     *            instance }
     */
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

    }

    /*
     *  @brief  { Remove authentication state listener if activity is stopped }
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected  void onPause() {
        super.onPause();
    }
}