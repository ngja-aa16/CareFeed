package com.carefeed.android.carefeed;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private CircleImageView mImageView;
    private EditText mUsername, mAge, mIntro;
    private TextView mChange;
    private ProgressBar mProgressBar;
    private User currentLoginUser;

    // --> Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_edit_profile);

        mToolbar = (Toolbar) findViewById(R.id.main_app_bar);
        mImageView = (CircleImageView) findViewById(R.id.eProfile_picture);
        mUsername = (EditText) findViewById(R.id.eProfile_username);
        mAge = (EditText) findViewById(R.id.eProfile_age);
        mIntro = (EditText) findViewById(R.id.eProfile_intro);
        mChange = (TextView) findViewById(R.id.txt_change);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        // setting toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Edit Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // --> get Firebase
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, Menu.FIRST, Menu.NONE, "Done").setIcon(R.drawable.ic_done_white).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            case Menu.FIRST:
                // update firebase
                updateUserInformation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        mProgressBar.setVisibility(View.VISIBLE);
        // get data from previous activity
        Bundle extras = getIntent().getExtras();

        if(extras != null) {
            currentLoginUser = new User(extras.getString("age"),extras.getString("introduction"), extras.getString("profileImage"), extras.getString("username"));


            // set data to current activity field
            mUsername.setText(currentLoginUser.getUsername());
            mAge.setText(currentLoginUser.getAge());
            mIntro.setText(currentLoginUser.getIntroduction());

            if(!currentLoginUser.getProfileImage().equals("")){
                Picasso.get().load(currentLoginUser.getProfileImage()).into(mImageView);
            }
        }

        mProgressBar.setVisibility(View.GONE);
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
            Toast.makeText(EditProfileActivity.this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
            return true;
        } else
            return false;
    }

    private void updateUserInformation(){
        mProgressBar.setVisibility(View.VISIBLE);
        String username = mUsername.getText().toString()
                , age = mAge.getText().toString()
                , intro = mIntro.getText().toString();

        mChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1).start(EditProfileActivity.this);
            }
        });

        String currentUserID = mAuth.getCurrentUser().getUid();
        DatabaseReference userDb = rootRef.child("User_Info").child(currentUserID);

        if(!checkNull(username, age, intro)) {
            mProgressBar.setVisibility(View.VISIBLE);
            Map newPost = new HashMap();
            newPost.put("username", username);
            newPost.put("age", age);
            newPost.put("introduction", intro);

            userDb.updateChildren(newPost).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(EditProfileActivity.this, "Update successful.", Toast.LENGTH_SHORT).show();
                        finish();
                    }else{
                        Toast.makeText(EditProfileActivity.this, "Update failed. Please try again", Toast.LENGTH_SHORT).show();
                    }
                    mProgressBar.setVisibility(View.GONE);
                }
            });

        }
        else{
            mProgressBar.setVisibility(View.GONE);
        }
    }
}
