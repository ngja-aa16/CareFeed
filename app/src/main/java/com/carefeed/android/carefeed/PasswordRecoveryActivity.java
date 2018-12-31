package com.carefeed.android.carefeed;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class PasswordRecoveryActivity extends AppCompatActivity {

    private ProgressBar mProgressBar;
    private EditText mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_password_recovery);

        mProgressBar = findViewById(R.id.recovery_progress_bar);
        mEmail = (EditText) findViewById(R.id.recovery_email);

        mProgressBar.setVisibility(View.GONE);
        init();
    }

    //check string if null
    private boolean isNull(String string){
        if(string.equals("")) {
            Toast.makeText(PasswordRecoveryActivity.this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
            return true;
        }
        else
            return false;
    }

    //check valid email format
    private boolean isEmailValid(CharSequence email) {
        boolean valid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();

        if(!valid)
            Toast.makeText(PasswordRecoveryActivity.this, "Incorrect email format", Toast.LENGTH_SHORT).show();
        return valid;
    }

    // check validation
    private boolean checkValid(String email){
        if(isNull(email) || !isEmailValid(email))
            return false;
        else
            return true;
    }

    // login button onclick event
    private void init(){
        Button btnConfirm = (Button) findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get input from user
                String email = mEmail.getText().toString();

                if(checkValid(email)) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    /*mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "signInWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        Toast.makeText(LoginActivity.this, "Login Successful.",
                                                Toast.LENGTH_SHORT).show();
                                        mProgressBar.setVisibility(View.GONE);
                                        updateUI(user);
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                        mProgressBar.setVisibility(View.GONE);
                                        updateUI(null);
                                    }

                                    // ...
                                }
                            });*/
                }
            }
        });
    }
}
