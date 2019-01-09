package com.carefeed.android.carefeed;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostDetail extends AppCompatActivity {

    private Toolbar topToolbar;
    private RelativeLayout postDetailsLayout;
    private CircleImageView profileImage;
    private TextView username, dateTime, postDescription, displayNoOfLikes, displayNoOfComments;
    private ImageView postImage, likePostButton, sendCommentButton;
    private EditText commentText;
    private ProgressBar loadingBar;
    private DatabaseReference currentPostRef, likeRef, userRef, commentRef;
    private RecyclerView commentList;
    private Query query;
    private boolean isLoginUser, likeCheck = false;
    private static final int MENU_SECOND = 2;
    private int countLikes, countComments;
    private String currentUserId, postId;
    private FirebaseRecyclerAdapter<Comment, CommentViewHolder> firebaseRecyclerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_post_detail);

        topToolbar = (Toolbar) findViewById(R.id.post_detail_toolbar);
        postDetailsLayout = (RelativeLayout) findViewById(R.id.post_detail_layout);
        profileImage = (CircleImageView) postDetailsLayout.findViewById(R.id.view_post_profile_image);
        username = (TextView) postDetailsLayout.findViewById(R.id.view_post_username);
        dateTime = (TextView) postDetailsLayout.findViewById(R.id.view_post_date_time);
        postDescription = (TextView) postDetailsLayout.findViewById(R.id.view_post_description);
        displayNoOfLikes = (TextView) postDetailsLayout.findViewById(R.id.view_post_likes_no);
        displayNoOfComments = (TextView) findViewById(R.id.post_detail_comment_no);
        postImage = (ImageView) postDetailsLayout.findViewById(R.id.view_post_image);
        likePostButton = (ImageView) postDetailsLayout.findViewById(R.id.view_post_like_button);
        loadingBar = (ProgressBar) postDetailsLayout.findViewById(R.id.post_layout_progress_bar);
        sendCommentButton = (ImageView) findViewById(R.id.post_detail_comment_button);
        commentText = (EditText) findViewById(R.id.post_detail_comment_text);
        commentList = (RecyclerView) findViewById(R.id.post_detail_comment_list);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        commentList.setLayoutManager(linearLayoutManager);
        commentList.setHasFixedSize(true);
        commentList.setNestedScrollingEnabled(false);

        isLoginUser = getIntent().getExtras().getBoolean("isLoginUser");
        postId = getIntent().getExtras().getString("postID");
        currentPostRef = FirebaseDatabase.getInstance().getReference().child("Post_Info").child(postId);
        likeRef = FirebaseDatabase.getInstance().getReference().child("Post_Like");
        userRef = FirebaseDatabase.getInstance().getReference().child("User_Info");
        commentRef = FirebaseDatabase.getInstance().getReference().child("Post_Comment");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        setLikeButtonStatus();

        likePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeCheck = true;
                likeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(likeCheck){
                            if(dataSnapshot.child(postId).hasChild(currentUserId)){
                                likeRef.child(postId).child(currentUserId).removeValue();
                                likeCheck = false;
                            } else {
                                likeRef.child(postId).child(currentUserId).setValue(true);
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

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = retrieveIntentExtraUser();
                Intent profileIntent = new Intent(PostDetail.this, ProfileActivity.class);
                profileIntent.putExtra("username", user.getUsername());
                profileIntent.putExtra("age", user.getAge());
                profileIntent.putExtra("introduction", user.getIntroduction());
                profileIntent.putExtra("isLoginUser", isLoginUser);
                profileIntent.putExtra("profileImage", user.getProfile_image());
                startActivity(profileIntent);
            }
        });

        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fullScreenIntent = new Intent(PostDetail.this, FullScreenActivity.class);
                fullScreenIntent.putExtra("postImage", getIntent().getExtras().getString("postImage"));
                startActivity(fullScreenIntent);
            }
        });

        sendCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            commentButtonPressed(dataSnapshot.child("username").getValue().toString());
                        }
                        commentText.setText("");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        setSupportActionBar(topToolbar);
        getSupportActionBar().setTitle("Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setAllView();

        query = commentRef.child(postId).orderByKey();

        FirebaseRecyclerOptions<Comment> firebaseRecyclerOptions =
                new FirebaseRecyclerOptions.Builder<Comment>()
                        .setQuery(query, Comment.class)
                        .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comment, CommentViewHolder>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final CommentViewHolder holder, int position, @NonNull Comment model) {
                final String commentIDs = getRef(position).getKey();

                commentRef.child(postId).child(commentIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()){
                            String date = dataSnapshot.child("date").getValue().toString();
                            String time = dataSnapshot.child("time").getValue().toString();
                            holder.commentDateTime.setText(date + " " + time);
                            holder.commentDescription.setText(dataSnapshot.child("comment").getValue().toString());

                            final String userId = dataSnapshot.child("userID").getValue().toString();
                            userRef.child(userId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        String profileImage = dataSnapshot.child("profile_image").getValue().toString();
                                        Picasso.get().load(profileImage).into(holder.commentProfileImage);
                                        holder.commentUsername.setText(dataSnapshot.child("username").getValue().toString());
                                    }

                                    holder.mView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            User user = retrieveIntentExtraUser();
                                            Intent profileIntent = new Intent(PostDetail.this, ProfileActivity.class);
                                            profileIntent.putExtra("username", dataSnapshot.child("username").getValue().toString());
                                            profileIntent.putExtra("age", dataSnapshot.child("age").getValue().toString());
                                            profileIntent.putExtra("introduction", dataSnapshot.child("introduction").getValue().toString());
                                            if (currentUserId.equals(userId)) {
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
                                            startActivity(profileIntent);
                                        }
                                    });
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
                holder.setIsRecyclable(false);
            }

            @NonNull
            @Override
            public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(PostDetail.this).inflate(R.layout.user_post_comment_layout, viewGroup, false);
                CommentViewHolder commentViewHolder = new CommentViewHolder(view);
                return commentViewHolder;
            }
        };

        commentList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("currentLoginUsername", "onCreateOptionMenu");
        if(isLoginUser){
            menu.add(0, Menu.FIRST, Menu.NONE, "Edit").setIcon(R.drawable.ic_edit_white).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add(0, MENU_SECOND, Menu.NONE, "Delete").setIcon(R.drawable.ic_delete_white_24dp).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch(item.getItemId()){
            case Menu.FIRST:
                editPost();
                return true;
            case MENU_SECOND:
                deletePost();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private User retrieveIntentExtraUser(){
        User user = new User();
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            user.setUsername(extras.getString("username"));
            user.setIntroduction(extras.getString("introduction"));
            user.setAge(extras.getString("age"));
            user.setProfile_image(extras.getString("profileImage"));
        }
        return user;
    }

    private Post retrieveIntentExtraPost(){
        Post post = new Post();
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            post.setDate(extras.getString("date"));
            post.setTime(extras.getString("time"));
            post.setDescription(extras.getString("description"));
            post.setPostImage(extras.getString("postImage"));
            post.setUsername(extras.getString("username"));
            post.setUserProfileImage(extras.getString("profileImage"));
        }
        return post;
    }

    private void setAllView(){
        Post post = retrieveIntentExtraPost();
        if(!post.getUserProfileImage().equals("")){
            Picasso.get().load(post.getUserProfileImage()).into(profileImage);
        }
        Picasso.get().load(post.getPostImage()).into(postImage, new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {
                loadingBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(Exception e) {
                loadingBar.setVisibility(View.GONE);
                postImage.setImageResource(R.drawable.failimage);
            }
        });
        username.setText(post.getUsername());
        String dateTimeString = post.getDate() + " " + post.getTime();
        dateTime.setText(dateTimeString);
        postDescription.setText(post.getDescription());

        commentRef.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    countComments = (int) dataSnapshot.getChildrenCount();
                    displayNoOfComments.setText("(" + Integer.toString(countComments) + ")");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void deletePost(){
        AlertDialog.Builder confirmationDialogBuilder = new AlertDialog.Builder(this, R.style.AlertDialog);

        confirmationDialogBuilder.setTitle("Confirmation");
        confirmationDialogBuilder.setMessage("Are you sure to delete this post");
        confirmationDialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                currentPostRef.removeValue();
                finish();
                Toast.makeText(PostDetail.this, "Post has been deleted", Toast.LENGTH_SHORT).show();
            }
        });
        confirmationDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(PostDetail.this, "Cancelled", Toast.LENGTH_SHORT).show();
            }
        });
        confirmationDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);

        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.90);

        Dialog confirmationDialog = confirmationDialogBuilder.create();
        confirmationDialog.show();
        confirmationDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void editPost(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
        builder.setTitle("Edit Post: ");

        final EditText descriptionField = new EditText(this);
        descriptionField.setText(postDescription.getText().toString());
        builder.setView(descriptionField);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                currentPostRef.child("description").setValue(descriptionField.getText().toString());
                postDescription.setText(descriptionField.getText().toString());
                Toast.makeText(PostDetail.this, "Post updated", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.90);

        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void setLikeButtonStatus(){
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(postId).hasChild(currentUserId)){
                    countLikes = (int) dataSnapshot.child(postId).getChildrenCount();
                    likePostButton.setImageResource(R.drawable.like);
                    displayNoOfLikes.setText(Integer.toString(countLikes));
                } else {
                    countLikes = (int) dataSnapshot.child(postId).getChildrenCount();
                    likePostButton.setImageResource(R.drawable.dislike);
                    displayNoOfLikes.setText(Integer.toString((countLikes)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void commentButtonPressed(String username){
        String comment = commentText.getText().toString();
        if(TextUtils.isEmpty(comment)){
            Toast.makeText(this, "Please input some comment...", Toast.LENGTH_SHORT).show();
        } else {
            Calendar calDate = Calendar.getInstance(Locale.ENGLISH);
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMM-yyyy");
            final String currentDateString = currentDate.format(calDate.getTime());

            Calendar calTime = Calendar.getInstance(Locale.ENGLISH);
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String currentTimeString = currentTime.format(calTime.getTime());

            HashMap commentsMap = new HashMap();
            commentsMap.put("userID", currentUserId);
            commentsMap.put("date", currentDateString);
            commentsMap.put("time", currentTimeString);
            commentsMap.put("comment", comment);

            commentRef.child(postId).push().updateChildren(commentsMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(PostDetail.this, "Comment inserted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PostDetail.this, "Error occured, please try again", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder{

        View mView;
        CircleImageView commentProfileImage;
        TextView commentUsername, commentDateTime, commentDescription;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
            commentProfileImage = itemView.findViewById(R.id.view_comment_profile_image);
            commentUsername = itemView.findViewById(R.id.view_comment_username);
            commentDateTime = itemView.findViewById(R.id.view_comment_dateTime);
            commentDescription = itemView.findViewById(R.id.view_comment_text);
        }
    }
}
