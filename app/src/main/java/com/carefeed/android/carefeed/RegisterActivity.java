package com.carefeed.android.carefeed;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private ProgressBar mProgressBar;
    private EditText mEmail, mPassword, mConfirmPassword;
    private TextView mValidPassword, mPasswordMatch;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_register);

        mProgressBar = (ProgressBar) findViewById(R.id.register_progress_bar);
        mEmail = (EditText) findViewById(R.id.register_email);
        mPassword = (EditText) findViewById(R.id.register_password);
        mConfirmPassword = (EditText) findViewById(R.id.register_confirm_password);
        mValidPassword = (TextView) findViewById(R.id.password_hint);
        mPasswordMatch = (TextView) findViewById(R.id.password_match_hint);
        mAuth = FirebaseAuth.getInstance();

        mProgressBar.setVisibility(View.GONE);
        registerOnClick();
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
            Toast.makeText(RegisterActivity.this, "Password does not match", Toast.LENGTH_SHORT).show();
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
            return false;
        }
        else
            return true;
    }

    private boolean passwordPattern(String password){
        Pattern pattern;
        Matcher matcher;
        final String pw_pattern = getString(R.string.password_pattern);
        pattern = Pattern.compile(pw_pattern);
        matcher = pattern.matcher(password);

        return matcher.matches();
    }

    // register button onClick event
    private void registerOnClick(){
        mPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(!passwordPattern(mPassword.getText().toString())){
                    mValidPassword.setVisibility(View.VISIBLE);
                    mValidPassword.setText("Invalid password.");
                    mValidPassword.setTextColor(Color.RED);
                } else{
                    mValidPassword.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(checkPasswordMatch(mPassword.getText().toString(), mConfirmPassword.getText().toString())){
                    mPasswordMatch.setVisibility(View.GONE);
                } else{
                    mPasswordMatch.setVisibility(View.VISIBLE);
                }
            }
        });

        mConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(checkPasswordMatch(mPassword.getText().toString(), mConfirmPassword.getText().toString())){
                    mPasswordMatch.setVisibility(View.GONE);
                } else{
                    mPasswordMatch.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

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
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Register success, update UI with the signed-in user's information
                                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        Toast.makeText(RegisterActivity.this, "Register successful.",
                                                Toast.LENGTH_SHORT).show();

                                        mProgressBar.setVisibility(View.GONE);
                                        Intent intent = new Intent(RegisterActivity.this, CreateProfileActivity.class);
                                        finish();
                                        startActivity(intent);
                                    } else {
                                        // If register fails, display a message to the user.
                                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                        Toast.makeText(RegisterActivity.this, "Email has been registered.",
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
