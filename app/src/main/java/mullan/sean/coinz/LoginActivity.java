package mullan.sean.coinz;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ProgressBar  mProgressBar;
    private EditText     mFieldEmail;
    private EditText     mFieldPassword;

    /*
     *  @brief  { Set log in view, create listeners for buttons and parse
     *            entered user data }
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get Firebase authentication instance
        mAuth = FirebaseAuth.getInstance();

        // Check if user is already logged in
        if (mAuth.getCurrentUser() != null) {
            proceedToActivity(MainActivity.class);
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

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email    = mFieldEmail.getText().toString().trim();
                String password = mFieldPassword.getText().toString().trim();

                if (detailsEntered(email, password)) {
                    authenticateUser(email, password);
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proceedToActivity(RegisterActivity.class);
            }
        });

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proceedToActivity(ResetPasswordActivity.class);
            }
        });
    }

    /*
     *  @brief  { Check that fields are not empty }
     *
     *  @params { User entered email and password }
     *
     *  @return { True if fields are not empty, false otherwise }
     */
    private boolean detailsEntered(String email, String password) {
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
        return true;
    }

    /*
     *  @brief  { Authenticate email and password with firebase, proceed
     *            to main activity if details are correct }
     *
     *  @params { User entered email and password }
     */
    private void authenticateUser(String email, String password) {
        mProgressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this,
                        new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        mProgressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            proceedToActivity(MainActivity.class);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
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