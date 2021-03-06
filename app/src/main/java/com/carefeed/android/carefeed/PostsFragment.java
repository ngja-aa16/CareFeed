package com.carefeed.android.carefeed;


import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostsFragment extends Fragment {
    private RecyclerView userPostList;
    private DatabaseReference postRef, userRef, likeRef;
    private boolean likeCheck = false;
    private String currentLoginUserId;
    private Query query;
    LinearLayoutManager linearLayoutManager;

    private Parcelable listState;

    public PostsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts, container, false);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createPostIntent = new Intent(getContext(), CreatePostActivity.class);
                startActivity(createPostIntent);
            }
        });

        userPostList = (RecyclerView) view.findViewById(R.id.uesr_post_list);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        userPostList.setLayoutManager(linearLayoutManager);

        postRef = FirebaseDatabase.getInstance().getReference().child("Post_Info");
        query = postRef.orderByKey();
        userRef = FirebaseDatabase.getInstance().getReference().child("User_Info");
        likeRef = FirebaseDatabase.getInstance().getReference().child("Post_Like");

        currentLoginUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseRecyclerOptions<Post> firebaseRecyclerOptions =
                new FirebaseRecyclerOptions.Builder<Post>()
                        .setQuery(query, Post.class)
                        .build();

        FirebaseRecyclerAdapter<Post, PostViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final PostViewHolder holder, int position, @NonNull Post model) {
                holder.loadingBar.setVisibility(View.VISIBLE);
                final String postIDs = getRef(position).getKey();

                holder.setLikeButtonStatus(postIDs);

                Log.d("onBindViewHolder", "onBindViewHolder");
                postRef.child(postIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

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

                                    if (dataSnapshot.exists()) {
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
                                                Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                                profileIntent.putExtra("username", username);
                                                profileIntent.putExtra("age", age);
                                                profileIntent.putExtra("introduction", introduction);
                                                if (currentLoginUserId.equals(userID)) {
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
                                                Intent fullScreenIntent = new Intent(getContext(), FullScreenActivity.class);
                                                fullScreenIntent.putExtra("postImage", postImage);
                                                startActivity(fullScreenIntent);
                                            }
                                        });

                                        holder.recyclerView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent postDetailIntent = new Intent(getContext(), PostDetail.class);
                                                if (currentLoginUserId.equals(userID)) {
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
                                                        if (likeCheck) {
                                                            if (dataSnapshot.child(postIDs).hasChild(currentLoginUserId)) {
                                                                likeRef.child(postIDs).child(currentLoginUserId).removeValue();
                                                                likeCheck = false;
                                                            } else {
                                                                likeRef.child(postIDs).child(currentLoginUserId).setValue(true);
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
                                    Toast.makeText(getContext(), "something went wrong", Toast.LENGTH_SHORT).show();
                                    Log.d("userRefFail", "userRefFail");
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getContext(), "something went wrong", Toast.LENGTH_SHORT).show();
                        Log.d("postRefFail", "postRefFail");
                    }

                });
            }

            @NonNull
            @Override
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.users_post_layout, viewGroup, false);
                PostViewHolder viewHolder = new PostViewHolder(view);
                return viewHolder;
            }
        };
        userPostList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        listState = userPostList.getLayoutManager().onSaveInstanceState();
    }

    @Override
    public void onResume() {
        super.onResume();
        userPostList.getLayoutManager().onRestoreInstanceState(listState);
    }
}
