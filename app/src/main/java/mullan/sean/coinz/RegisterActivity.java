package mullan.sean.coinz;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ProgressBar  mProgressBar;
    private EditText     mFieldUsername;
    private EditText     mFieldEmail;
    private EditText     mFieldPassword;

    /*
     *  @brief  { Display registration view, set listeners for buttons and
     *            parse user entered data }
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Get Firebase authentication instance
        mAuth = FirebaseAuth.getInstance();

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
            Toast.makeText(getApplicationContext(),
                    R.string.required_field_email,
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(),
                    R.string.required_field_password,
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(getApplicationContext(),
                    R.string.error_password_short,
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /*
     *  @brief  { Attempt to create a user account on firebase. If successful,
     *            create document for user in database and proceed to the main
     *            activity. If unsuccessful, print task exception.
     */
    private void createUserAccount(String username, String email, String password) {
        mProgressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this,
                                    R.string.registration_success,
                                    Toast.LENGTH_SHORT).show();
                            // TODO(Sean): Create Document with User ID and add username as field
                            proceedToActivity(MainActivity.class);
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    getString(R.string.registration_fail) + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        }
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
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
}
