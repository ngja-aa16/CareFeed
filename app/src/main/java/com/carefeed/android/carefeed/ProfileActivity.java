package com.carefeed.android.carefeed;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

    BitmapTransfer bitmapTransfer;
    private Toolbar mToolbar;
    private CircleImageView profilePicture;
    private TextView mUsername, mIntro;
    private ProgressBar mProgressBar;
    private String age;

    // --> Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

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

        // setting toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // --> get Firebase
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater infalter = getMenuInflater();
        infalter.inflate(R.menu.edit_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch(item.getItemId()){
            case R.id.edit:
                intent = new Intent(this, EditProfileActivity.class);

                intent.putExtra("username", mUsername.getText().toString());
                intent.putExtra("age", age);
                intent.putExtra("intro", mIntro.getText().toString());

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

    private void setUserProfileImage(){
        DatabaseReference profileImageRef = rootRef.child("User_Info").child(mAuth.getUid()).child("profile_image");
        final Target target = new Target(){
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Log.d("GetImage", "Loaded");
                bitmapTransfer.setBitmap(bitmap);
                Drawable d = new BitmapDrawable(getResources(), bitmap);
                profilePicture.setImageDrawable(d);
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                Log.d("GetImage", "Failed");
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                Log.d("GetImage", "PrepareLoad");
            }
        };

        if(profileImageRef != null){
            profileImageRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d("Get image", "" + 1);
                    if(dataSnapshot.getValue() != null){
                        String image = dataSnapshot.getValue().toString();

                        Picasso.get().load(image).into(target);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    private void setUserData() {
        DatabaseReference dbRef = rootRef.child("User_Info").child(mAuth.getUid());
        // Read from the database
        if (dbRef != null) {
            dbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    if(dataSnapshot != null) {
                        String username = dataSnapshot.child("username").getValue().toString();
                        age = dataSnapshot.child("age").getValue().toString();
                        String intro = dataSnapshot.child("introduction").getValue().toString();

                        mUsername.setText(username);
                        mIntro.setText(intro);
                        setUserProfileImage();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value

                }
            });
        }
    }
}
