package com.carefeed.android.carefeed;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;


import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private CircleImageView profilePicture;
    private TextView mUsername, mIntro;
    private ProgressBar mProgressBar;;
    private User currentLoginUser;
    private boolean isLoginUser;

    // --> Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_profile);

        mToolbar = (Toolbar) findViewById(R.id.main_app_bar);
        mUsername = (TextView) findViewById(R.id.txt_username);
        mIntro = (TextView) findViewById(R.id.txt_intro);
        profilePicture = (CircleImageView) findViewById(R.id.profile_picture);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            currentLoginUser = new User(extras.getString("age"), extras.getString("introduction"), extras.getString("profileImage"), extras.getString("username"));
            isLoginUser = extras.getBoolean("isLoginUser");
            Log.d("Intentextras", currentLoginUser.getUsername());
        }

        // --> get Firebase
        mAuth = FirebaseAuth.getInstance();

        // setting toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("currentLoginUsername", "onCreateOptionMenu");
        if(isLoginUser)
            menu.add(0, Menu.FIRST, Menu.NONE, "Edit").setIcon(R.drawable.ic_edit_white).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        mProgressBar.setVisibility(View.GONE);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch(item.getItemId()){
            case Menu.FIRST:
                intent = new Intent(this, EditProfileActivity.class);

                intent.putExtra("username", currentLoginUser.getUsername());
                intent.putExtra("age", currentLoginUser.getAge());
                intent.putExtra("introduction", currentLoginUser.getIntroduction());
                intent.putExtra("profileImage", currentLoginUser.getProfileImage());

                startActivity(intent);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // display user information
        setUserData();
    }

    private void setUserData() {
        mUsername.setText(currentLoginUser.getUsername());
        mIntro.setText(currentLoginUser.getIntroduction());
        if(!currentLoginUser.getProfileImage().equals("")){
            Picasso.get().load(currentLoginUser.getProfileImage()).into(profilePicture);
        }
    }
}
