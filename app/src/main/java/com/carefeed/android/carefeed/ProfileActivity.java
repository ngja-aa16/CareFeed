package com.carefeed.android.carefeed;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
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
    private Button mFriendRequest, mDeclineRequest;
    private User currentLoginUser;
    private RecyclerView profilePostList;
    private ProgressBar mLoadingBar;
    private boolean isLoginUser, likeCheck = false;
    private static final int START_EDIT_PROFILE = 2;

    private String saveCurrentDate, receiverUID, senderUID, currentState;

    // --> Firebase
    private FirebaseAuth mAuth;
    private FirebaseRecyclerAdapter<Post, PostViewHolder> firebaseRecyclerAdapter;
    private DatabaseReference friendRequestRef, friendRef, postRef, userRef, likeRef;
    private Query query;

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
        profilePostList = (RecyclerView) findViewById(R.id.profile_post_list) ;
        mLoadingBar = (ProgressBar) findViewById(R.id.profile_progress_bar);
        mLoadingBar.setVisibility(View.VISIBLE);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        profilePostList.setLayoutManager(linearLayoutManager);
        profilePostList.setNestedScrollingEnabled(false);

        // --> get Firebase
        mAuth = FirebaseAuth.getInstance();
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        friendRef = FirebaseDatabase.getInstance().getReference().child("Friend");
        postRef = FirebaseDatabase.getInstance().getReference().child("Post_Info");
        userRef = FirebaseDatabase.getInstance().getReference().child("User_Info");
        likeRef = FirebaseDatabase.getInstance().getReference().child("Post_Like");

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

        query = postRef.orderByChild("userID").equalTo(receiverUID);

        FirebaseRecyclerOptions<Post> firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Post>().setQuery(query, Post.class).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final PostViewHolder holder, int position, @NonNull Post model) {
                holder.loadingBar.setVisibility(View.VISIBLE);
                final String postIDs = getRef(position).getKey();

                holder.setLikeButtonStatus(postIDs);

                Log.d("onBindViewHolder", "onBindViewHolder");
                postRef.child(postIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){

                            final String userID = dataSnapshot.child("userID").getValue().toString();
                            final String date = dataSnapshot.child("date").getValue().toString();
                            final String time = dataSnapshot.child("time").getValue().toString();
                            final String description = dataSnapshot.child("description").getValue().toString();
                            final String postImage = dataSnapshot.child("post_image").getValue().toString();

                            holder.dateTime.setText(date + " " + time);
                            holder.postDescription.setText(description);
                            Picasso.get().load(postImage).into(holder.postImage, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                    holder.loadingBar.setVisibility(View.GONE);
                                }

                                @Override
                                public void onError(Exception e) {
                                    holder.loadingBar.setVisibility(View.GONE);
                                    holder.postImage.setImageResource(R.drawable.failimage);
                                }
                            });

                            userRef.child(userID).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.exists()){
                                        final String username = dataSnapshot.child("username").getValue().toString();
                                        final String age = dataSnapshot.child("age").getValue().toString();
                                        final String introduction = dataSnapshot.child("introduction").getValue().toString();

                                        holder.profileUsername.setText(username);
                                        if (dataSnapshot.hasChild("profile_image")) {
                                            Picasso.get().load(dataSnapshot.child("profile_image").getValue().toString()).into(holder.profileImage);
                                        }

                                        holder.profileImage.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent profileIntent = new Intent(ProfileActivity.this, ProfileActivity.class);
                                                profileIntent.putExtra("username", username);
                                                profileIntent.putExtra("age", age);
                                                profileIntent.putExtra("introduction", introduction);
                                                if (senderUID.equals(userID)) {
                                                    Log.d("loginUser", "true");
                                                    profileIntent.putExtra("isLoginUser", true);
                                                } else {
                                                    Log.d("loginUser", "false");
                                                    profileIntent.putExtra("isLoginUser", false);
                                                }
                                                if (dataSnapshot.hasChild("profile_image")) {
                                                    profileIntent.putExtra("profileImage", dataSnapshot.child("profile_image").getValue().toString());
                                                } else {
                                                    profileIntent.putExtra("profileImage", "");
                                                }
                                                profileIntent.putExtra("visit_user_id", dataSnapshot.getKey().toString());
                                                startActivity(profileIntent);
                                            }
                                        });

                                        holder.postImage.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent fullScreenIntent = new Intent(ProfileActivity.this, FullScreenActivity.class);
                                                fullScreenIntent.putExtra("postImage", postImage);
                                                startActivity(fullScreenIntent);
                                            }
                                        });

                                        holder.recyclerView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent postDetailIntent = new Intent(ProfileActivity.this, PostDetail.class);
                                                if (senderUID.equals(userID)) {
                                                    Log.d("loginUser", "true");
                                                    postDetailIntent.putExtra("isLoginUser", true);
                                                } else {
                                                    Log.d("loginUser", "false");
                                                    postDetailIntent.putExtra("isLoginUser", false);
                                                }
                                                if (dataSnapshot.hasChild("profile_image")) {
                                                    postDetailIntent.putExtra("profileImage", dataSnapshot.child("profile_image").getValue().toString());
                                                } else {
                                                    postDetailIntent.putExtra("profileImage", "");
                                                }
                                                postDetailIntent.putExtra("postID", postIDs);
                                                postDetailIntent.putExtra("username", username);
                                                postDetailIntent.putExtra("date", date);
                                                postDetailIntent.putExtra("time", time);
                                                postDetailIntent.putExtra("description", description);
                                                postDetailIntent.putExtra("postImage", postImage);
                                                postDetailIntent.putExtra("age", age);
                                                postDetailIntent.putExtra("introduction", introduction);
                                                postDetailIntent.putExtra("visit_user_id", dataSnapshot.getKey().toString());
                                                startActivity(postDetailIntent);
                                            }
                                        });

                                        holder.likeArea.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                likeCheck = true;
                                                likeRef.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if(likeCheck){
                                                            if(dataSnapshot.child(postIDs).hasChild(senderUID)){
                                                                likeRef.child(postIDs).child(senderUID).removeValue();
                                                                likeCheck = false;
                                                            } else {
                                                                likeRef.child(postIDs).child(senderUID).setValue(true);
                                                                likeCheck = false;
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        });
                                    }


                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(ProfileActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
                                    Log.d("userRefFail", "userRefFail");
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError){
                        Toast.makeText(ProfileActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
                        Log.d("postRefFail", "postRefFail");
                    }

                });
            }

            @NonNull
            @Override
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(ProfileActivity.this).inflate(R.layout.users_post_layout, viewGroup,  false);
                PostViewHolder viewHolder = new PostViewHolder(view);
                return viewHolder;
            }
        };

        profilePostList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("currentLoginUsername", "onCreateOptionMenu");
        if(isLoginUser){
            menu.add(0, Menu.FIRST, Menu.NONE, "Edit").setIcon(R.drawable.ic_edit_white).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

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
            mLoadingBar.setVisibility(View.GONE);
            Picasso.get().load(currentLoginUser.getProfile_image()).into(mImageView);
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent fullScreenIntent = new Intent(ProfileActivity.this, FullScreenActivity.class);
                    fullScreenIntent.putExtra("postImage", currentLoginUser.getProfile_image());
                    startActivity(fullScreenIntent);
                }
            });
        } else {
            mLoadingBar.setVisibility(View.GONE);
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent fullScreenIntent = new Intent(ProfileActivity.this, FullScreenActivity.class);
                    fullScreenIntent.putExtra("postImage", R.drawable.profile);
                    startActivity(fullScreenIntent);
                }
            });
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
                Drawable green = getResources().getDrawable(R.drawable.green_rounded_button);
                Drawable red = getResources().getDrawable(R.drawable.red_rounded_button);

                if(dataSnapshot.hasChild(receiverUID)){
                    String request_type = dataSnapshot.child(receiverUID).child("request_type").getValue().toString();

                    if(request_type.equals("sent")){
                        currentState = "request_sent";
                        mFriendRequest.setText("Cancel Friend Request");
                        mFriendRequest.setTextColor(Color.WHITE);
                        mFriendRequest.setBackgroundDrawable(red);
                        mDeclineRequest.setVisibility(View.GONE);
                    }
                    else if(request_type.equals("received")){
                        currentState = "request_received";
                        mFriendRequest.setText("Accept Friend Request");
                        mFriendRequest.setBackgroundDrawable(green);
                        mFriendRequest.setTextColor(Color.WHITE);
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
                            Drawable red = getResources().getDrawable(R.drawable.red_rounded_button);
                            if(dataSnapshot.hasChild(receiverUID)){
                                currentState = "friend";
                                mFriendRequest.setText("Delete friend");
                                mFriendRequest.setBackgroundDrawable(red);
                                mFriendRequest.setTextColor(Color.WHITE);

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
                            Drawable red = getResources().getDrawable(R.drawable.red_rounded_button);
                            friendRequestRef.child(receiverUID).child(senderUID).child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Drawable red = getResources().getDrawable(R.drawable.red_rounded_button);
                                                mFriendRequest.setEnabled(true);
                                                currentState = "request_sent";
                                                mFriendRequest.setText("Cancel Friend Request");
                                                mFriendRequest.setTextColor(Color.WHITE);
                                                mFriendRequest.setBackgroundDrawable(red);

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
                            Drawable green = getResources().getDrawable(R.drawable.green_rounded_button);
                            friendRequestRef.child(receiverUID).child(senderUID).child("request_type").removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Drawable green = getResources().getDrawable(R.drawable.green_rounded_button);
                                                mFriendRequest.setEnabled(true);
                                                currentState = "not_friend";
                                                mFriendRequest.setText("Send Friend Request");
                                                mFriendRequest.setTextColor(Color.WHITE);
                                                mFriendRequest.setBackgroundDrawable(green);

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
                            Drawable red = getResources().getDrawable(R.drawable.red_rounded_button);
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
                                                                                    Drawable red = getResources().getDrawable(R.drawable.red_rounded_button);
                                                                                    if(task.isSuccessful()){
                                                                                        mFriendRequest.setEnabled(true);
                                                                                        currentState = "friend";
                                                                                        mFriendRequest.setText("Delete friend");
                                                                                        mFriendRequest.setBackgroundDrawable(red);
                                                                                        mFriendRequest.setTextColor(Color.WHITE);

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
                                            Drawable green = getResources().getDrawable(R.drawable.green_rounded_button);
                                            if(task.isSuccessful()){
                                                mFriendRequest.setEnabled(true);
                                                currentState = "not_friend";
                                                mFriendRequest.setText("Send Friend Request");
                                                mFriendRequest.setTextColor(Color.WHITE);
                                                mFriendRequest.setBackgroundDrawable(green);

                                                mDeclineRequest.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
