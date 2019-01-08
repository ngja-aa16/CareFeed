package com.carefeed.android.carefeed;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private CircleImageView mImageView;
    private TextView mUsername, mIntro;
    private ProgressBar mProgressBar;
    private Button mFriendRequest, mDeclineRequest;
    private User currentLoginUser;
    private boolean isLoginUser;
    private static final int START_EDIT_PROFILE = 2;

    private String saveCurrentDate, receiverUID, senderUID, currentState;

    // --> Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference friendRequestRef, friendRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_profile);

        mToolbar = (Toolbar) findViewById(R.id.profile_page_toolbar);
        mUsername = (TextView) findViewById(R.id.txt_username);
        mIntro = (TextView) findViewById(R.id.txt_intro);
        mFriendRequest = (Button) findViewById(R.id.btn_friend_request);
        mDeclineRequest = (Button) findViewById(R.id.btn_decline_request);
        mImageView = (CircleImageView) findViewById(R.id.profile_picture);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);

        // --> get Firebase
        mAuth = FirebaseAuth.getInstance();
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        friendRef = FirebaseDatabase.getInstance().getReference().child("Friend");

        currentState = "not_friend";
        senderUID = mAuth.getCurrentUser().getUid();

        // set receiverUID to self which to solve home activity
        if(getIntent().hasExtra("visit_user_id")) {
            receiverUID = getIntent().getExtras().getString("visit_user_id");
        } else{
            receiverUID = senderUID;
        }
        ButtonMaintenance();
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
            case android.R.id.home:
                finish();
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

        if(isLoginUser){
            mFriendRequest.setVisibility(View.GONE);
        }

        ManageFriendRequest();

        mUsername.setText(currentLoginUser.getUsername());
        mIntro.setText(currentLoginUser.getIntroduction());
        if(!currentLoginUser.getProfile_image().equals("")){
            Picasso.get().load(currentLoginUser.getProfile_image()).into(mImageView);
        }
    }

    private void ManageFriendRequest() {
        mDeclineRequest.setVisibility(View.GONE);
        mDeclineRequest.setEnabled(false);

        if(!senderUID.equals(receiverUID)){
            mFriendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFriendRequest.setEnabled(false);

                    if(currentState.equals("not_friend")){
                        SendFriendRequest();
                    }
                    if(currentState.equals("request_sent")){
                        CancelFriendRequest();
                    }
                    if(currentState.equals("request_received")){
                        AcceptFriendRequest();
                    }
                    if(currentState.equals("friend")){
                        DeleteFriend();
                    }
                }
            });
        }
        else {
            mFriendRequest.setVisibility(View.GONE);
            mDeclineRequest.setVisibility(View.GONE);
        }
    }

    private void ButtonMaintenance() {
        friendRequestRef.child(senderUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(receiverUID)){
                    String request_type = dataSnapshot.child(receiverUID).child("request_type").getValue().toString();

                    if(request_type.equals("sent")){
                        currentState = "request_sent";
                        mFriendRequest.setText("Cancel Friend Request");
                        mDeclineRequest.setVisibility(View.GONE);
                    }
                    else if(request_type.equals("received")){
                        currentState = "request_received";
                        mFriendRequest.setText("Accept Friend Request");
                        mDeclineRequest.setVisibility(View.VISIBLE);
                        mDeclineRequest.setEnabled(true);

                        mDeclineRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelFriendRequest();
                            }
                        });
                    }
                }
                else{
                    friendRef.child(senderUID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(receiverUID)){
                                currentState = "friend";
                                mFriendRequest.setText("Delete friend");

                                mDeclineRequest.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendFriendRequest() {
        friendRequestRef.child(senderUID).child(receiverUID).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendRequestRef.child(receiverUID).child(senderUID).child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                mFriendRequest.setEnabled(true);
                                                currentState = "request_sent";
                                                mFriendRequest.setText("Cancel Friend Request");

                                                mDeclineRequest.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelFriendRequest() {
        friendRequestRef.child(senderUID).child(receiverUID).child("request_type").removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendRequestRef.child(receiverUID).child(senderUID).child("request_type").removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                mFriendRequest.setEnabled(true);
                                                currentState = "not_friend";
                                                mFriendRequest.setText("Send Friend Request");

                                                mDeclineRequest.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptFriendRequest() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMM-yyyy",Locale.ENGLISH);
        saveCurrentDate = currentDate.format(calForDate.getTime());
        Log.d("FriendRequest", "accept");

        friendRef.child(senderUID).child(receiverUID).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendRef.child(receiverUID).child(senderUID).child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                friendRequestRef.child(senderUID).child(receiverUID).child("request_type").removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    friendRequestRef.child(receiverUID).child(senderUID).child("request_type").removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        mFriendRequest.setEnabled(true);
                                                                                        currentState = "friend";
                                                                                        mFriendRequest.setText("Delete friend");

                                                                                        mDeclineRequest.setVisibility(View.GONE);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void DeleteFriend(){
        friendRef.child(senderUID).child(receiverUID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendRef.child(receiverUID).child(senderUID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                mFriendRequest.setEnabled(true);
                                                currentState = "not_friend";
                                                mFriendRequest.setText("Send Friend Request");

                                                mDeclineRequest.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
