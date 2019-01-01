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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private ProgressBar mProgressBar;
    private EditText mEmail, mPassword;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);

        mProgressBar = (ProgressBar) findViewById(R.id.login_progress_bar);
        mEmail = (EditText) findViewById(R.id.login_email);
        mPassword = (EditText) findViewById(R.id.login_password);
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

    //check valid email format
    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // login button onclick event
    private void init(){
        Button btnLogin = (Button) findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get input from user
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                if(isNull(email) || isNull(password))
                    Toast.makeText(LoginActivity.this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
                else if(!isEmailValid(email)){
                    Toast.makeText(LoginActivity.this, "Incorrect email format", Toast.LENGTH_SHORT).show();
                }
                else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();

                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(LoginActivity.this, "Invalid email or password",
                                                Toast.LENGTH_SHORT).show();
                                        mProgressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                }
            }
        });
    }

    public void register_onClick(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        onPause();
        startActivity(intent);
    }


    public void recovery_onClick(View view){
        Intent intent = new Intent(this, PasswordRecoveryActivity.class);
        onPause();
        startActivity(intent);
    }
}
