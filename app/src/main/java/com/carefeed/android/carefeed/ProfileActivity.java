package com.carefeed.android.carefeed;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private CircleImageView mImageView;
    private TextView mUsername, mIntro;
    private ProgressBar mProgressBar;
    private User currentLoginUser;
    private boolean isLoginUser;
    private static final int START_EDIT_PROFILE = 2;

    // --> Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_profile);

        mToolbar = (Toolbar) findViewById(R.id.profile_page_toolbar);
        mUsername = (TextView) findViewById(R.id.txt_username);
        mIntro = (TextView) findViewById(R.id.txt_intro);
        mImageView = (CircleImageView) findViewById(R.id.profile_picture);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);

        // --> get Firebase
        mAuth = FirebaseAuth.getInstance();

        setUserData();

        // setting toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("currentLoginUsername", "onCreateOptionMenu");
        if(isLoginUser){
            menu.add(0, Menu.FIRST, Menu.NONE, "Edit").setIcon(R.drawable.ic_edit_white).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
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
                intent.putExtra("profileImage", currentLoginUser.getProfile_image());

                startActivityForResult(intent, START_EDIT_PROFILE);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == START_EDIT_PROFILE){
            if(resultCode == RESULT_OK){
                Log.d("getExtras","Success");
                Bundle extras = data.getExtras();
                currentLoginUser.setUsername(extras.getString("username"));
                currentLoginUser.setAge(extras.getString("age"));
                currentLoginUser.setIntroduction(extras.getString("introduction"));
                currentLoginUser.setProfile_image(extras.getString("profileImage"));

                Log.d("getExtras",currentLoginUser.getProfile_image());
                if(!currentLoginUser.getProfile_image().equals("")){
                    Picasso.get().load(currentLoginUser.getProfile_image()).into(mImageView);
                }
                mUsername.setText(currentLoginUser.getUsername());
                mIntro.setText(currentLoginUser.getIntroduction());
                Toast.makeText(this, "Update Successful", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setUserData() {
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            currentLoginUser = new User(extras.getString("age"), extras.getString("introduction"), extras.getString("profileImage"), extras.getString("username"));
            isLoginUser = extras.getBoolean("isLoginUser");
            Log.d("Intentextras", currentLoginUser.getUsername());
        }

        mUsername.setText(currentLoginUser.getUsername());
        mIntro.setText(currentLoginUser.getIntroduction());
        if(!currentLoginUser.getProfile_image().equals("")){
            Picasso.get().load(currentLoginUser.getProfile_image()).into(mImageView);
        }
    }
}
