package mullan.sean.coinz;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;

    /*
     *  @brief  { Set main activity view, load in map fragment as default
     *            and create a listener for the user authentication state }
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get Firebase instance
        mAuth = FirebaseAuth.getInstance();

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
}