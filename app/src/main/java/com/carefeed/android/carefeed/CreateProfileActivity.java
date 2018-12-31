package com.carefeed.android.carefeed;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class CreateProfileActivity extends AppCompatActivity {

    private ProgressBar mProgressBar;
    private EditText mUsername, mAge, mIntro;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_create_profile);

        mProgressBar = findViewById(R.id.progress_bar);
        mUsername = (EditText) findViewById(R.id.cProfile_username);
        mAge = (EditText) findViewById(R.id.cProfile_age);
        mIntro = (EditText) findViewById(R.id.cProfile_intro);
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
    private boolean checkNull(String username, String age, String intro) {
        if (isNull(username) || isNull(age) || isNull(intro)) {
            Toast.makeText(CreateProfileActivity.this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
            return true;
        } else
            return false;
    }

    // register button onClick event
    private void init(){
        Button btnConfirm = (Button) findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get input from user
                String username = mUsername.getText().toString();
                String age = mAge.getText().toString();
                String intro = mIntro.getText().toString();

                if(!checkNull(username, age, intro)) {
                    mProgressBar.setVisibility(View.VISIBLE);

                    DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("User_Info").child(mAuth.getCurrentUser().getUid());
                    Map newPost = new HashMap();
                    newPost.put("username", username);
                    newPost.put("age", age);
                    newPost.put("introduction", intro);

                    userDb.setValue(newPost);
                    Toast.makeText(CreateProfileActivity.this, "Create Profile Successful",
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CreateProfileActivity.this, MainActivity.class);
                    finish();
                    startActivity(intent);
                }
            }
        });
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Snackbar.make(this.getWindow().getDecorView().findViewById(android.R.id.content), "Please click BACK again to exit", Snackbar.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
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
