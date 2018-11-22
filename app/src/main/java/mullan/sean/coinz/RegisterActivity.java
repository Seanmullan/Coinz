package mullan.sean.coinz;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "C_REG";

    private FirebaseFirestore   mFirestore;
    private FirebaseAuth        mAuth;
    private CollectionReference mUsersRef;
    private ProgressBar         mProgressBar;
    private EditText            mFieldUsername;
    private EditText            mFieldEmail;
    private EditText            mFieldPassword;
    private boolean             mUsernameAvailable;

    /*
     *  @brief  { Display registration view, set listeners for buttons and
     *            parse user entered data }
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Get Firebase firestore and authentication instances
        mFirestore = FirebaseFirestore.getInstance();
        mAuth      = FirebaseAuth.getInstance();
        mUsersRef  = mFirestore.collection("users");

        // Views
        mProgressBar   = findViewById(R.id.progressBar);
        mFieldUsername = findViewById(R.id.username);
        mFieldEmail    = findViewById(R.id.email);
        mFieldPassword = findViewById(R.id.password);

        mUsernameAvailable = true;

        // Buttons
        Button btnRegister = findViewById(R.id.btn_register);
        Button btnLogin    = findViewById(R.id.btn_already_member);

        btnRegister.setOnClickListener(v -> {

            String username = mFieldUsername.getText().toString().trim();
            String email    = mFieldEmail.getText().toString().trim();
            String password = mFieldPassword.getText().toString().trim();

            // Check validity of user entered details. If valid, check that the entered
            // username is not being used by another user. If not, create user account
            if (detailsValid(username, email, password)) {
                mProgressBar.setVisibility(View.VISIBLE);
                checkUsernameAvailable(username, new OnEventListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean usernameAvailable) {
                        if (usernameAvailable) {
                            createUserAccount(username, email, password);
                        } else {
                            mProgressBar.setVisibility(View.GONE);
                            displayToast(getString(R.string.msg_username_already_exists));
                        }
                    }
                    @Override
                    public void onFailure(Exception e) {
                        mProgressBar.setVisibility(View.GONE);
                        displayToast(getString(R.string.msg_failed_to_create_account));
                        Log.d(TAG, "[onCreate] failed to fetch user data", e);
                    }
                });
            }
        });

        btnLogin.setOnClickListener(v -> proceedToActivity(LoginActivity.class));
    }

    /*
     *  @brief   { Checks that fields have been filled and that password is at least
     *             6 characters in length }
     *
     *  @params  { String username, String email, String password }
     *
     *  @return  { True if details are valid, false otherwise }
     */
    private boolean detailsValid(String username, String email, String password) {
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(getApplicationContext(),
                    R.string.required_field_username,
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            displayToast(getString(R.string.required_field_email));
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            displayToast(getString(R.string.required_field_password));
            return false;
        }

        if (password.length() < 6) {
            displayToast(getString(R.string.error_password_short));
            return false;
        }

        return true;
    }

    /*
     *  @brief  { Fetches all users in firebase and checks if the username entered has
     *            already been taken by another user. Callback will return true if
     *            username is available, and false otherwise }
     */
    private void checkUsernameAvailable(String username, OnEventListener<Boolean> event) {
        mUsersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Log.d(TAG, "[checkUsernameAvailable] successfully retrieved user data");
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Map<String, Object> userData = document.getData();
                    String name = (String) userData.get("username");

                    // If username is taken, return false
                    if (username.equals(name)) {
                        Log.d(TAG, "[checkUsernameAvailable] username already exists");
                        event.onSuccess(false);
                        mUsernameAvailable = false;
                        break;
                    }
                }
                // If username is available, return true
                if (mUsernameAvailable) {
                    event.onSuccess(true);
                }

                // Reset username available flag
                mUsernameAvailable = true;
            } else {
                event.onFailure(task.getException());
            }
        });
    }

    /*
     *  @brief  { Attempt to create a user account on firebase. If successful,
     *            create document for user in database and proceed to the main
     *            activity. If unsuccessful, print task exception.
     */
    private void createUserAccount(final String username, final String email, String password) {
        Log.d(TAG, "[createUserAccount] creating user account...");
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "[createUserAccount] user account created");
                        displayToast(getString(R.string.registration_success));
                        addUserToDatabase(username, email);
                        proceedToActivity(MainActivity.class);
                        finish();
                    } else {
                        Log.d(TAG, "[createUserAccount] failed: " + task.getException());
                        displayToast(getString(R.string.registration_fail));
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
    }

    /*
     *  @brief  { Create a document for user in the database using the users unique ID
     *            and add the required fields to the document }
     *
     *  @params { Users username and email }
     */
    private void addUserToDatabase(String username, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", email);
        userData.put("gold", 0.0);
        userData.put("lastSavedDate", "");
        userData.put("collectedTransferred", 0);
        userData.put("bonusUsed", false);
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            mFirestore.collection("users").document(uid).set(userData)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User doc successfully added"))
                    .addOnFailureListener(e -> Log.d(TAG, "Error adding document", e));
        } else {
            displayToast(getString(R.string.user_null_pointer));
        }
    }

    /*
     *  @brief { Start new activity }
     *
     *  @params { Class of intended activity }
     */
    private void proceedToActivity(Class activity) {
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }

    /*
     *  @brief  { Display message on device }
     *
     *  @params { Message to be displayed }
     */
    private void displayToast(String message) {
        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
