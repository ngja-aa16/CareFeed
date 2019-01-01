package com.carefeed.android.carefeed;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class MainActivity extends AppCompatActivity {

    boolean doubleBackToExitPressedOnce = false;
    private Toolbar topToolbar;
    private BottomNavigationView bottomNav;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private ProgressDialog loadingBar;
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

        topToolbar = (Toolbar) findViewById(R.id.main_app_bar);
        setSupportActionBar(topToolbar);
        getSupportActionBar().setTitle("Carefeeds");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(resizedProfileIcon());

        bottomNav = (BottomNavigationView) findViewById(R.id.bottom_navi_bar);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        loadingBar.setTitle("Fetching data...");
        loadingBar.setMessage("Please wait, while the server is fetching your information");
        loadingBar.show();
        loadingBar.setCancelable(false);

        if(mAuth.getCurrentUser() != null){
            ChangeUserProfileImage();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater infalter = getMenuInflater();
        infalter.inflate(R.menu.options_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_setting:
                Toast.makeText(this, "Setting", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_logout:
                Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                finish();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // ---------------------- Firebase -----------------------
    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.

        if(mAuth.getCurrentUser() == null){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else{

            //check if user first time login
            DatabaseReference userNameRef = rootRef.child("User_Info").child(mAuth.getCurrentUser().getUid()).child("username");
            userNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {               //dataSnapshot for username
                            if(!dataSnapshot.exists()) {                                //If users do not has a username ((haven't create profile
                                loadingBar.dismiss();
                                Toast.makeText(MainActivity.this,"Please create profile to procced",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this, CreateProfileActivity.class);
                                finish();
                                startActivity(intent);
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

    private void ChangeUserProfileImage() {
        DatabaseReference profileImageRef = rootRef.child("User_Info").child(mAuth.getCurrentUser().getUid()).child("profile_image");
        if(profileImageRef != null){
            profileImageRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() != null){
                        String image = dataSnapshot.getValue().toString();
                        Picasso.get()
                                .load(image).transform(new CropCircleTransformation()).resize(100, 100)
                                .into(new Target()
                                {
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from)
                                    {
                                        loadingBar.dismiss();
                                        Drawable d = new BitmapDrawable(getResources(), bitmap);
                                        getSupportActionBar().setHomeAsUpIndicator(d);
                                    }

                                    @Override
                                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                                    }

                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable)
                                    {
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
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

    private Drawable resizedProfileIcon(){
        Drawable drawable= getResources().getDrawable(R.drawable.profile_icon);
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        Drawable newdrawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 40, 40, true));
        return newdrawable;
    }
}
