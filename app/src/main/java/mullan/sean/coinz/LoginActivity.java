package mullan.sean.coinz;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ProgressBar  mProgressBar;
    private EditText     mFieldEmail;
    private EditText     mFieldPassword;

    // TESTING MODE FLAG
    private boolean testMode;

    /**
     *   Set log in view, create listeners for buttons and parse entered user data
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            testMode = extras.getBoolean("testMode");
        } else {
            testMode = false;
        }

        // Get Firebase authentication instance
        mAuth = FirebaseAuth.getInstance();

        // Check if user is already logged in
        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(this, MainActivity.class);
            if (testMode) {
                intent.putExtra("testMode", true);
            } else {
                intent.putExtra("testMode", false);
            }
            startActivity(intent);
            finish();
        }

        // Views
        setContentView(R.layout.activity_login);
        mProgressBar   = findViewById(R.id.progressBar);
        mFieldEmail    = findViewById(R.id.email);
        mFieldPassword = findViewById(R.id.password);

        // Buttons
        Button btnLogin         = findViewById(R.id.btn_login);
        Button btnRegister      = findViewById(R.id.btn_not_a_member);
        Button btnResetPassword = findViewById(R.id.btn_reset_password);

        btnLogin.setOnClickListener(v -> {

            String email    = mFieldEmail.getText().toString().trim();
            String password = mFieldPassword.getText().toString().trim();

            if (detailsEntered(email, password)) {
                authenticateUser(email, password);
            }
        });

        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        btnResetPassword.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    /**
     *  Check that fields are not empty
     *
     *  @param email user entered email
     *  @param password user entered password
     *
     *  @return True if fields are not empty, false otherwise
     */
    private boolean detailsEntered(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            displayToast(getString(R.string.required_field_email));
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            displayToast(getString(R.string.required_field_password));
            return false;
        }
        return true;
    }

    /**
     *  Authenticate email and password with firebase, proceed
     *  to main activity if details are correct
     *
     *  @param email user entered email
     *  @param password user entered password
     */
    private void authenticateUser(String email, String password) {
        mProgressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this,
                        task -> {
                            mProgressBar.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                //proceedToActivity(MainActivity.class);
                                Intent intent = new Intent(this, MainActivity.class);
                                if (testMode) {
                                    intent.putExtra("testMode", true);
                                } else {
                                    intent.putExtra("testMode", false);
                                }
                                startActivity(intent);
                                finish();
                            } else {
                                displayToast(getString(R.string.auth_failed));
                            }
                        });
    }

    /**
     *  Display message on device
     *
     *  @param message Message to be displayed
     */
    private void displayToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}