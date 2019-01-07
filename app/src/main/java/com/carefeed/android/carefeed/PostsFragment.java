package com.carefeed.android.carefeed;


import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
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

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class PostsFragment extends Fragment {
    private RecyclerView userPostList;
    private DatabaseReference postRef, userRef;
    private String currentLoginUserId;
    private FirebaseRecyclerAdapter<Post, PostViewHolder> firebaseRecyclerAdapter;
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
                Intent createPostIntent = new Intent(getActivity(), CreatePostActivity.class);
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

        currentLoginUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseRecyclerOptions<Post> firebaseRecyclerOptions =
                new FirebaseRecyclerOptions.Builder<Post>()
                        .setQuery(query, Post.class)
                        .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final PostViewHolder holder, int position, @NonNull Post model) {
                holder.loadingBar.setVisibility(View.VISIBLE);
                String postIDs = getRef(position).getKey();

                Log.d("onBindViewHolder", "onBindViewHolder");
                postRef.child(postIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String userID = dataSnapshot.child("userID").getValue().toString();
                        String date = dataSnapshot.child("date").getValue().toString();
                        String time = dataSnapshot.child("time").getValue().toString();
                        String description = dataSnapshot.child("description").getValue().toString();
                        final String postImage = dataSnapshot.child("post_image").getValue().toString();

                        holder.dateTime.setText(date + " " + time);
                        holder.postDescription.setText(description);
                        Picasso.get().load(postImage).into(holder.postImage, new com.squareup.picasso.Callback(){

                            @Override
                            public void onSuccess() {
                                holder.loadingBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError(Exception e) {
                                holder.postImage.setImageResource(R.drawable.failimage);
                            }
                        });

                        userRef.child(userID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                                final String username = dataSnapshot.child("username").getValue().toString();
                                holder.profileUsername.setText(username);

                                Log.d("userRef","userRef" + username);

                                holder.profileImage.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                        profileIntent.putExtra("username", username);
                                        profileIntent.putExtra("age", dataSnapshot.child("age").getValue().toString());
                                        profileIntent.putExtra("introduction", dataSnapshot.child("introduction").getValue().toString());
                                        if(currentLoginUserId.equals(userID)){
                                            Log.d("loginUser", "true");
                                            profileIntent.putExtra("isLoginUser", true);
                                        } else {
                                            Log.d("loginUser", "false");
                                            profileIntent.putExtra("isLoginUser", false);
                                        }
                                        if(dataSnapshot.hasChild("profile_image")){
                                            String profileImage = dataSnapshot.child("profile_image").getValue().toString();
                                            Picasso.get().load(profileImage).into(holder.profileImage);
                                            profileIntent.putExtra("profileImage",profileImage);
                                        } else {
                                            profileIntent.putExtra("profileImage", "");
                                        }
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
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(getContext(), "something went wrong", Toast.LENGTH_SHORT).show();
                                Log.d("userRefFail", "userRefFail");
                            }
                        });
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
                Log.d("onCreateViewHolder", "onCreateViewHolder");
                View view = LayoutInflater.from(getContext()).inflate(R.layout.users_post_layout, viewGroup,  false);
                PostViewHolder viewHolder = new PostViewHolder(view);
                userPostList.getLayoutManager().onRestoreInstanceState(listState);
                return viewHolder;
            }
        };
        userPostList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

        return view;
    }

    public class PostViewHolder extends RecyclerView.ViewHolder{

        CircleImageView profileImage;
        TextView profileUsername, dateTime, postDescription;
        ImageView postImage;
        ProgressBar loadingBar;

        public PostViewHolder(View itemView){
            super(itemView);
            profileImage = itemView.findViewById(R.id.view_post_profile_image);
            profileUsername = itemView.findViewById(R.id.view_post_username);
            dateTime = itemView.findViewById(R.id.view_post_date_time);
            postDescription = itemView.findViewById(R.id.view_post_description);
            postImage = itemView.findViewById(R.id.view_post_image);
            loadingBar = itemView.findViewById(R.id.post_layout_progress_bar);
        }
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
