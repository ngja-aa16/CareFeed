package com.carefeed.android.carefeed;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostViewHolder extends RecyclerView.ViewHolder{

    LinearLayout likeArea;
    View recyclerView;
    CircleImageView profileImage;
    TextView profileUsername, dateTime, postDescription, displayNoOfLikes;
    ImageView postImage, likePostButton;
    ProgressBar loadingBar;
    int countLikes;
    String currentUserId;
    DatabaseReference likeRef;

    public PostViewHolder(View itemView){
        super(itemView);
        recyclerView = itemView;
        likeArea = itemView.findViewById(R.id.post_layout_like_area);
        profileImage = itemView.findViewById(R.id.view_post_profile_image);
        profileUsername = itemView.findViewById(R.id.view_post_username);
        dateTime = itemView.findViewById(R.id.view_post_date_time);
        postDescription = itemView.findViewById(R.id.view_post_description);
        postImage = itemView.findViewById(R.id.view_post_image);
        loadingBar = itemView.findViewById(R.id.post_layout_progress_bar);
        displayNoOfLikes = itemView.findViewById(R.id.view_post_likes_no);
        likePostButton = itemView.findViewById(R.id.view_post_like_button);

        likeRef = FirebaseDatabase.getInstance().getReference().child("Post_Like");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void setLikeButtonStatus(final String postID){
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(postID).hasChild(currentUserId)){
                    countLikes = (int) dataSnapshot.child(postID).getChildrenCount();
                    likePostButton.setImageResource(R.drawable.like);
                    displayNoOfLikes.setText(Integer.toString(countLikes));
                } else {
                    countLikes = (int) dataSnapshot.child(postID).getChildrenCount();
                    likePostButton.setImageResource(R.drawable.dislike);
                    displayNoOfLikes.setText(Integer.toString((countLikes)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
