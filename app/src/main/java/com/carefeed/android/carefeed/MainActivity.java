package com.carefeed.android.carefeed;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class MainActivity extends AppCompatActivity {

    boolean doubleBackToExitPressedOnce = false;
    private Toolbar topToolbar;
    private BottomNavigationView bottomNav;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private ProgressDialog loadingBar;
    private String currentUserID;
    private User currentLoginUser = new User();
    private boolean firstTimeLogin = true;
    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            Fragment selectedFragment = null;

            switch (menuItem.getItemId()){
                case R.id.menu_bottom_home:
                    selectedFragment = new HomeFragment();
                    getSupportActionBar().setTitle("Carefeeds");
                    break;
                case R.id.menu_bottom_posts:
                    selectedFragment = new PostsFragment();
                    getSupportActionBar().setTitle("Posts");
                    break;
                case R.id.menu_bottom_chats:
                    selectedFragment = new ChatsFragment();
                    getSupportActionBar().setTitle("Chats");
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        loadingBar = new ProgressDialog(this);

        topToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(topToolbar);
        getSupportActionBar().setTitle("Carefeeds");

        bottomNav = (BottomNavigationView) findViewById(R.id.bottom_navi_bar);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater infalter = getMenuInflater();
        infalter.inflate(R.menu.options_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;
        switch(item.getItemId()){
            case android.R.id.home:
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.putExtra("age", currentLoginUser.getAge());
                intent.putExtra("introduction", currentLoginUser.getIntroduction());
                intent.putExtra("username", currentLoginUser.getUsername());
                intent.putExtra("profileImage", currentLoginUser.getProfile_image());
                intent.putExtra("isLoginUser", true);
                startActivity(intent);
                return true;
            case R.id.menu_search:
                intent = new Intent(MainActivity.this, SearchUserActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_setting:
                intent = new Intent(MainActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_logout:
                Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
                logOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        retrieveInfoFromDatabase();
    }

    private void logOut() {
        mAuth.signOut();
        finish();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    // ---------------------- Firebase -----------------------
    public void retrieveInfoFromDatabase() {

        if(firstTimeLogin) {
            loadingBar.setTitle("Fetching data...");
            loadingBar.setMessage("Please wait, while the server is fetching your information");
            loadingBar.show();
            loadingBar.setCancelable(false);
        }

        @SuppressLint("HandlerLeak") final Handler h = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if(loadingBar.isShowing()){
                    loadingBar.dismiss();
                    AlertDialog connectionFailDialog = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Connection to server failed!")
                            .setMessage("Please make sure the device is connected to internet.")
                            .setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    recreate();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        };
        h.sendMessageDelayed(new Message(), 10000);

        // Check if user is signed in (non-null) and update UI accordingly.

        if(mAuth.getCurrentUser() == null){
            loadingBar.dismiss();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else{
            currentUserID = mAuth.getCurrentUser().getUid();
            userRef = FirebaseDatabase.getInstance().getReference().child("User_Info").child(currentUserID);
            //check if user first time login
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {               //dataSnapshot for username
                    if(!dataSnapshot.hasChild("username")) {                                //If users do not has a username ((haven't create profile
                        Toast.makeText(MainActivity.this,"Please create profile to procced",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, CreateProfileActivity.class);
                        loadingBar.dismiss();
                        finish();
                        startActivity(intent);
                    } else {
                        if(dataSnapshot.hasChild("profile_image")){
                            ChangeUserProfileImage(true);
                        } else {
                            ChangeUserProfileImage(false);
                        }
                        currentLoginUser.setAge(dataSnapshot.child("age").getValue().toString());
                        currentLoginUser.setIntroduction(dataSnapshot.child("introduction").getValue().toString());
                        currentLoginUser.setUsername(dataSnapshot.child("username").getValue().toString());
                        if(firstTimeLogin){
                            Toast.makeText(MainActivity.this, "Welcome back, " + currentLoginUser.getUsername(), Toast.LENGTH_SHORT).show();
                            firstTimeLogin = false;
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(MainActivity.this, "Opps! Error Occurred.",
                            Toast.LENGTH_SHORT).show();
                    onRestart();
                }
            });
        }
    }

    private void ChangeUserProfileImage(boolean hasImage) {
        final Target homeIndicatorTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
                Log.d("DEBUG", "onBitmapLoaded");
                Drawable d = new BitmapDrawable(getResources(), bitmap);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeAsUpIndicator(d);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable drawable) {
                Log.d("DEBUG", "onBitmapFailed");
            }

            @Override
            public void onPrepareLoad(Drawable drawable) {
                Log.d("DEBUG", "onPrepareLoad");
            }
        };

        if(hasImage){
            DatabaseReference profileImageRef = userRef.child("profile_image");
            profileImageRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() != null){
                        String image = dataSnapshot.getValue().toString();

                        Picasso.get()
                                .load(image).transform(new CropCircleTransformation()).resize(100, 100).centerCrop()
                                .into(homeIndicatorTarget);
                        currentLoginUser.setProfile_image(image);
                        loadingBar.dismiss();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            currentLoginUser.setProfile_image("");
            Picasso.get().load(R.drawable.profile).transform(new CropCircleTransformation()).resize(100, 100).centerCrop().into(homeIndicatorTarget);
            loadingBar.dismiss();
        }
    }

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
}
