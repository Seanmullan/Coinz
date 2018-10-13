package mullan.sean.coinz;

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
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText     mInputEmail;
    private ProgressBar  mProgressBar;

    /*
     *  @brief  { Set reset password view, create listeners for buttons and parse
     *            entered user data }
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Get Firebase instance
        mAuth = FirebaseAuth.getInstance();

        // Views
        mInputEmail  = findViewById(R.id.email);
        mProgressBar = findViewById(R.id.progressBar);

        // Buttons
        Button btnReset = findViewById(R.id.btn_reset_password);
        Button btnBack  = findViewById(R.id.btn_back);

        // Reset password if email field is filled
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mInputEmail.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    displayToast(getString(R.string.reset_email));
                } else {
                    resetPassword(email);
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /*
     *  @brief  { Send email to reset users password }
     *
     *  @params { User entered email }
     */
    private void resetPassword(String email) {
        mProgressBar.setVisibility(View.VISIBLE);
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            displayToast(getString(R.string.password_reset_msg));
                        } else {
                            displayToast(getString(R.string.password_reset_failed));
                        }
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
    }

    /*
     *  @brief  { Display message on device }
     *
     *  @params { Message to be displayed }
     */
    private void displayToast(String message) {
        Toast.makeText(ResetPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
