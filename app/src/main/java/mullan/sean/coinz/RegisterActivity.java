package mullan.sean.coinz;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseFirestore   mFirestore;
    private FirebaseAuth        mAuth;
    private ProgressBar         mProgressBar;
    private EditText            mFieldUsername;
    private EditText            mFieldEmail;
    private EditText            mFieldPassword;

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

        // Views
        mProgressBar   = findViewById(R.id.progressBar);
        mFieldUsername = findViewById(R.id.username);
        mFieldEmail    = findViewById(R.id.email);
        mFieldPassword = findViewById(R.id.password);

        // Buttons
        Button btnRegister = findViewById(R.id.btn_register);
        Button btnLogin    = findViewById(R.id.btn_already_member);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = mFieldUsername.getText().toString().trim();
                String email    = mFieldEmail.getText().toString().trim();
                String password = mFieldPassword.getText().toString().trim();

                // Check validity of user entered details. Create account if valid
                if (detailsValid(username, email, password)) {
                    createUserAccount(username, email, password);
                }
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proceedToActivity(LoginActivity.class);
            }
        });
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
     *  @brief  { Attempt to create a user account on firebase. If successful,
     *            create document for user in database and proceed to the main
     *            activity. If unsuccessful, print task exception.
     */
    private void createUserAccount(final String username, final String email, String password) {
        mProgressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            displayToast(getString(R.string.registration_success));
                            addUserToDatabase(username, email);
                            proceedToActivity(MainActivity.class);
                            finish();
                        } else {
                            displayToast(getString(R.string.registration_fail) + task.getException());
                        }
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
        userData.put("gold", 0);
        userData.put("lastSavedDate", LocalDate.now().toString());
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            mFirestore.collection("users").document(uid).set(userData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.w("REG", "Document successfully added");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("REG", "Error adding document", e);
                        }
                    });
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
