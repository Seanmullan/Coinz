package mullan.sean.coinz;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.json.*;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseFirestore mFirestore;
    private DocumentSnapshot  mUserDoc;
    private FirebaseAuth      mAuth;
    private FirebaseUser      mUser;
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

        // Get authentication and user references
        mFirestore = FirebaseFirestore.getInstance();
        mAuth      = FirebaseAuth.getInstance();
        mUser      = mAuth.getCurrentUser();

        // Get user document reference
        getUserDocument(mUser.getUid());

        // Set up toolbar
        Toolbar mTopToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(mTopToolbar);

        // Set up bottom navigation
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Load map fragment by default
        loadFragment(new MapFragment());

        // Create firebase authentication listener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };
    }

    /*
     *  @brief  { Controls the basic navigation of the application. When a
     *            button is selected on the navigation bar, the corresponding
     *            fragment is loaded }
     */
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
        }
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

    private void getUserDocument(final String uid) {
        DocumentReference docRef = mFirestore.collection("users").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        // Initialise user document reference and retrieve map data
                        mUserDoc = document;
                        getMapData();
                    } else {
                        Log.d("MAIN", "Failed to retrieve user document with ID " + uid);
                    }
                } else {
                    Log.d("MAIN", "User document get failed with ", task.getException());
                }
            }
        });
    }

    public void getMapData() {
        String lastSavedDate = mUserDoc.getString("lastSavedDate");
        String currentDate   = getCurrentDate();
        if (lastSavedDate.equals(currentDate)) {
            //TODO: Retrieve map data from coins subcollection
        } else {
            retrieveNewMapData();
        }
    }

    private void retrieveNewMapData() {
        String mapUrl  = "http://homepages.inf.ed.ac.uk/stg/coinz/";
        String mapJson = "/coinzmap.geojson";
        String url = mapUrl + getCurrentDate() + mapJson;
        DownloadFileTask downloadTask = new DownloadFileTask(getApplicationContext(), new OnEventListener<String>() {

            @Override
            public void onSuccess(String result) {
                try {
                    Log.d("MAIN", "Successfully retrieved map data");
                    parseJsonString(result);
                    updateMapDataOnFirebase();
                    MapFragment.setMapData(mCoinData);
                } catch (JSONException e) {
                    Log.d("MAIN", "Failed to parse json data" + e.toString());
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.d("MAIN", "Download task failed: " + e.toString());
            }
        });
        downloadTask.execute(url);
    }

    private void retrieveExistingMapData() {
        // TODO: Locate coins subcolleciton of user document. Populate arraylist of coins with data
    }

    private String getCurrentDate() {
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.ENGLISH);
        return dateFormat.format(date);
    }

    private void parseJsonString(String json) throws JSONException {
        JSONObject mapData = new JSONObject(json);
        mExchangeRates = (JSONObject) mapData.get("rates");
        mCoinData = (JSONArray) mapData.get("features");
        // TODO: here, populate ArrayList of coins
    }

    private void updateMapDataOnFirebase() {
        //TODO: Take coin data json. For each JSON Object in mCoinData, populate ArrayList of coins

    }
}