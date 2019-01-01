package com.carefeed.android.carefeed;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private ProgressBar mProgressBar;
    private EditText mEmail, mPassword, mConfirmPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_register);

        mProgressBar = (ProgressBar) findViewById(R.id.register_progress_bar);
        mEmail = (EditText) findViewById(R.id.register_email);
        mPassword = (EditText) findViewById(R.id.register_password);
        mConfirmPassword = (EditText) findViewById(R.id.register_confirm_password);
        mAuth = FirebaseAuth.getInstance();

        mProgressBar.setVisibility(View.GONE);
        init();
    }

    //check string if null
    private boolean isNull(String string){
        if(string.equals(""))
            return true;
        else
            return false;
    }

    // check input field if null
    private boolean checkValid(String email, String password, String confirmPassword){
        if(isNull(email) ||  isNull(password) || isNull(confirmPassword)) {
            Toast.makeText(RegisterActivity.this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!isEmailValid(email)){
            return false;
        }
        else if(!checkPasswordMatch(password, confirmPassword)){
            return false;
        }
        else
            return true;
    }

    //check valid email format
    private boolean isEmailValid(CharSequence email) {
        boolean valid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();

        if(!valid)
            Toast.makeText(RegisterActivity.this, "Incorrect email format", Toast.LENGTH_SHORT).show();

        return valid;
    }

    // check password match
    private boolean checkPasswordMatch(String password, String confirmPassword){
        if(!password.equals(confirmPassword)) {
            Toast.makeText(RegisterActivity.this, "Password does not match", Toast.LENGTH_SHORT).show();
            return false;
        }
        else
            return true;
    }

    // register button onClick event
    private void init(){
        Button btnRegister = (Button) findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get input from user
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                String confirmPassword = mConfirmPassword.getText().toString();

                if(checkValid(email, password, confirmPassword)) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Register success, update UI with the signed-in user's information
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        Toast.makeText(RegisterActivity.this, "Register successful.",
                                                Toast.LENGTH_SHORT).show();

                                        mProgressBar.setVisibility(View.GONE);
                                        Intent intent = new Intent(RegisterActivity.this, CreateProfileActivity.class);
                                        finish();
                                        startActivity(intent);
                                    } else {
                                        // If register fails, display a message to the user.
                                        Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                        mProgressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                }
            }
        });
    }

    // ---------------------- Firebase -----------------------
    //setup firebase
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }
}
