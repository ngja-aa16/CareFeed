package com.carefeed.android.carefeed;

import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
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

public class MainActivity extends AppCompatActivity {

    private Toolbar topToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef, userNameRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        topToolbar = findViewById(R.id.main_app_bar);
        setSupportActionBar(topToolbar);
        getSupportActionBar().setTitle("Carefeeds");

        myViewPager = findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);

        myTabLayout = findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
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
            case R.id.menu_profile:
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
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else{
            userNameRef = rootRef.child("User_Info").child(mAuth.getCurrentUser().getUid());
            //check if user first time login

            ValueEventListener eventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.exists()) {
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
            };
            userNameRef.addListenerForSingleValueEvent(eventListener);
        }
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
}
