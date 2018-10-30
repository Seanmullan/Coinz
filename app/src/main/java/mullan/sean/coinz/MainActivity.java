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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.json.*;

public class MainActivity extends AppCompatActivity {

    private static final String TAG         = "C_MAIN";
    private static final String UNCOLLECTED = "uncollected";
    private static final String COLLECTED   = "collected";
    private static final String RECEIVED    = "received";

    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseFirestore mFirestore;
    private DocumentSnapshot  mUserDoc;
    private FirebaseAuth      mAuth;
    private JSONObject        mExchangeRates;
    private JSONArray         mCoinData;

    /*
     *  @brief  { Set main activity view, load in map fragment as default
     *            and create a listener for the user authentication state }
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        // Load map fragment by default
        loadFragment(new MapFragment());

        // Create firebase authentication listener
        mAuthListener = firebaseAuth -> {
            FirebaseUser user1 = firebaseAuth.getCurrentUser();
            if (user1 == null) {
                // user auth state is changed - user is null
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        };
    }

    /*
     *  @brief  { Controls the basic navigation of the application. When a
     *            button is selected on the navigation bar, the corresponding
     *            fragment is loaded }
     */
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
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
     *  @brief  { Invoke superclass to resume application }
     */
    @Override
    protected void onResume() {
        super.onResume();
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

    /*
     *  @brief  { This method begins the process of data retrieval upon boot up.
     *            The first step is to retrieve the users document, then the Data
     *            class is populated given the users document. See the populateData()
     *            method for details. }
     */
    private void getUserDocument(final String uid) {
        DocumentReference docRef = mFirestore.collection("users").document(uid);
        Data.init(docRef);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null) {
                    // Initialise user document reference and retrieve map data
                    mUserDoc = document;
                    populateData();
                } else {
                    Log.d(TAG, "Failed to retrieve user document with ID " + uid);
                }
            } else {
                Log.d(TAG, "User document get failed with ", task.getException());
            }
        });
    }

    /*
     *  @brief  { This procedure will prompt the data class to pull in the appropriate
     *            data from firebase and populate the relevant fields in the Data class.
     *
     *            We want to retrieve existing data if a new day has not begun. This case
     *            arises if the user closes the app and reopens it on the same day, then
     *            we want to fetch the users uncollected, collected and received coins.
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
     *            retrieve the most up to date data from the Data class. }
     */
    @SuppressWarnings("unchecked")
    private void populateData() {
        String lastSavedDate = mUserDoc.getString("lastSavedDate");
        String currentDate   = getCurrentDate();

        if (lastSavedDate == null || currentDate == null) {
            Log.d(TAG, "Saved date or current date is null");
            return;
        }

        // If a new day has not yet begun, invoke the Data class to retrieve existing data
        if (lastSavedDate.equals(currentDate)) {
            // Retrieve all uncollected coins, then prompt Map to update its data
            Data.retrieveAllCoinsFromCollection(UNCOLLECTED, new OnEventListener<String>() {
                @Override
                public void onSuccess(String object) {
                    Log.d(TAG, "Successfully retrieved uncollected coins");
                    MapFragment.updateMapData(getApplicationContext());
                }
                @Override
                public void onFailure(Exception e) {
                    Log.d(TAG, "Failed to retrieve uncollected coins with exception ", e);
                }
            });
            // Retrieve all collected coins
            Data.retrieveAllCoinsFromCollection(COLLECTED, new OnEventListener<String>() {
                @Override
                public void onSuccess(String object) {
                    Log.d(TAG, "Successfully retrieved collected coins");
                }
                @Override
                public void onFailure(Exception e) {
                    Log.d(TAG, "Failed to retrieve collected coins with exception ", e);
                }
            });
            //Retrieve all received coins
            Data.retrieveAllCoinsFromCollection(RECEIVED, new OnEventListener<String>() {
                @Override
                public void onSuccess(String object) {
                    Log.d(TAG, "Successfully retrieved received coins");
                }
                @Override
                public void onFailure(Exception e) {
                    Log.d(TAG, "Failed to retrieve received coins with exception ", e);
                }
            });

        // If a new day has begun, clear the data from the previous day and fetch the new data
        } else {
            Data.updateDate(getCurrentDate());
            Data.clearAllCoinsFromCollection(UNCOLLECTED, new OnEventListener() {
                @Override
                public void onSuccess(Object object) {
                    Log.d(TAG, "Successfully cleared uncollected coins");
                }
                @Override
                public void onFailure(Exception e) {
                    Log.d(TAG, "Could not clear uncollected coins with exception: ", e);
                }
            });
            Data.clearAllCoinsFromCollection(COLLECTED, new OnEventListener() {
                @Override
                public void onSuccess(Object object) {
                    Log.d(TAG, "Successfully cleared collected coins");
                }
                @Override
                public void onFailure(Exception e) {
                    Log.d(TAG, "Could not clear collected coins with exception: ", e);
                }
            });
            Data.clearAllCoinsFromCollection(RECEIVED, new OnEventListener() {
                @Override
                public void onSuccess(Object object) {
                    Log.d(TAG, "Successfully cleared received coins");
                }
                @Override
                public void onFailure(Exception e) {
                    Log.d(TAG, "Could not clear received coins with exception: ", e);
                }
            });
            retrieveNewMapData();
        }
    }

    /*
     *  @brief  { Retrieves that json map data from Informatics server. Upon successful
     *            retrieval, processNewMapData is called to parse the data and store it
     *            in the Data class }
     */
    private void retrieveNewMapData() {
        String mapUrl  = "http://homepages.inf.ed.ac.uk/stg/coinz/";
        String mapJson = "/coinzmap.geojson";
        String url = mapUrl + getCurrentDate() + mapJson;
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
        mExchangeRates     = mapData.getJSONObject("rates");
        mCoinData          = mapData.getJSONArray("features");

        for (int i = 0; i < mCoinData.length(); i++) {
            // Extract json data
            final JSONObject coinJson = mCoinData.getJSONObject(i);
            JSONObject coinProperties = coinJson.getJSONObject("properties");
            JSONArray  coords = coinJson.getJSONObject("geometry").getJSONArray("coordinates");

            // Parse json data into coin attributes
            final String id = coinProperties.getString("id");
            Double value    = Double.parseDouble(coinProperties.getString("value"));
            String currency = coinProperties.getString("currency");
            String symbol   = coinProperties.getString("marker-symbol");
            String colour   = coinProperties.getString("marker-color");
            LatLng location = new LatLng(coords.getDouble(1), coords.getDouble(0));

            // Create coin from parsed data and add it to uncollected coins in Data class
            Coin coin = new Coin(id, value, currency, symbol, colour, location);
            Data.addCoinToCollection(coin, UNCOLLECTED, new OnEventListener() {
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
}