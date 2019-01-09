package com.carefeed.android.carefeed;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class FriendFragment extends Fragment {

    private ProgressBar progressBar;
    private FloatingActionButton fab;
    private RecyclerView friendList, friendRequestList;
    private TextView mNoFriend;
    private String currentLoginUserId;

    private DatabaseReference friendRef, userRef, friendRequestRef;
    private FirebaseRecyclerAdapter<Friends, FriendViewHolder> friendAdapter;
    private FirebaseRecyclerAdapter<FriendRequest, FriendRequestViewHolder> friendRequestAdapter;
    private FirebaseAuth mAuth;



    public FriendFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend, container, false);

        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),SearchUserActivity.class);
                startActivity(intent);
            }
        });
        progressBar = (ProgressBar) view.findViewById(R.id.fragment_friend_progress_bar);
        mNoFriend = (TextView) view.findViewById(R.id.txt_no_friend);

        friendList = (RecyclerView) view.findViewById(R.id.fragment_friend_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        friendList.setLayoutManager(linearLayoutManager);

        friendRequestList = (RecyclerView) view.findViewById(R.id.fragment_friend_request);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(getContext());
        linearLayoutManager2.setReverseLayout(true);
        linearLayoutManager2.setStackFromEnd(true);
        friendRequestList.setLayoutManager(linearLayoutManager2);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        currentLoginUserId = mAuth.getCurrentUser().getUid();
        friendRef = FirebaseDatabase.getInstance().getReference().child("Friend").child(currentLoginUserId);
        userRef = FirebaseDatabase.getInstance().getReference().child("User_Info");
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend_Requests").child(currentLoginUserId);

        DisplayFriendRequests();
        DisplayAllFriends();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void DisplayFriendRequests() {
        Log.d("DisplayFriendRequests", "display");
        Query query2 = friendRequestRef.orderByKey();
        FirebaseRecyclerOptions<FriendRequest> options2
                = new FirebaseRecyclerOptions.Builder<FriendRequest>()
                .setQuery(query2, FriendRequest.class)
                .build();

        friendRequestAdapter = new FirebaseRecyclerAdapter<FriendRequest, FriendRequestViewHolder>(options2) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendRequestViewHolder holder, int position, @NonNull final FriendRequest model) {
                final String userID = getRef(position).getKey();
                Log.d("DisplayFriendRequests", "BindViewHolder");

                DatabaseReference requestTypeRef = getRef(position).child("request_type").getRef();
                requestTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if(dataSnapshot.getValue().toString().equals("received")) {
                                userRef.child(userID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                                        Log.d("DisplayFriendRequests", "Snapshot Exist");
                                        final String username = dataSnapshot.child("username").getValue().toString();
                                        final String profileImage = dataSnapshot.child("profile_image").getValue().toString();

                                        holder.request_username.setText(username);
                                        if (dataSnapshot.hasChild("profile_image"))
                                            Picasso.get().load(profileImage).into(holder.request_profileImage);

                                        holder.mView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(getContext(), ProfileActivity.class);
                                                if (currentLoginUserId.equals(userID)) {
                                                    intent.putExtra("isLoginUser", true);
                                                } else {
                                                    intent.putExtra("isLoginUser", false);
                                                }
                                                intent.putExtra("visit_user_id", dataSnapshot.getKey().toString());
                                                intent.putExtra("username", username);
                                                intent.putExtra("age", dataSnapshot.child("age").getValue().toString());
                                                intent.putExtra("introduction", dataSnapshot.child("introduction").getValue().toString());
                                                if (profileImage != null)
                                                    intent.putExtra("profileImage", profileImage);
                                                else
                                                    intent.putExtra("profileImage", "");

                                                startActivity(intent);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                            else{
                                holder.mView.setVisibility(View.GONE);
                            }
                        } else {
                            Log.d("DisplayFriends", "Snapshot Not Exist");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("DisplayFriendRequests", "Cancelled");
                    }

                });
            }

            @NonNull
            @Override
            public FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_user_list, viewGroup, false);
                FriendRequestViewHolder viewHolder = new FriendRequestViewHolder(view);
                Log.d("DisplayFriendRequests", "OnCreate");
                return viewHolder;
            }
        };
        friendRequestList.setAdapter(friendRequestAdapter);
        friendRequestAdapter.startListening();
        progressBar.setVisibility(View.GONE);
    }

    private void DisplayAllFriends() {
        Query query = friendRef.orderByKey();
        FirebaseRecyclerOptions<Friends> options
                = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(query, Friends.class)
                .build();

        friendAdapter = new FirebaseRecyclerAdapter<Friends, FriendViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final FriendViewHolder holder, int position, @NonNull final Friends model) {
                final String userID = getRef(position).getKey();

                Log.d("DisplayFriends", "BindViewHolder");
                userRef.child(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            Log.d("DisplayFriends", "Snapshot Exist");
                            final String username = dataSnapshot.child("username").getValue().toString();
                            final String profileImage = dataSnapshot.child("profile_image").getValue().toString();
                            final String uid = dataSnapshot.getKey();



                            mNoFriend.setVisibility(View.GONE);
                            holder.mChat.setVisibility(View.VISIBLE);
                            holder.mChat.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("target_user_id", uid);
                                    chatIntent.putExtra("username", username);
                                    if(profileImage != null)
                                        chatIntent.putExtra("profileImage", profileImage);
                                    else
                                        chatIntent.putExtra("profileImage", "");
                                    Log.d("ChatUser", "" + uid + username + profileImage);
                                    startActivity(chatIntent);
                                }
                            });

                            holder.date.setVisibility(View.VISIBLE);
                            holder.date.setText("Friends Since : "+ model.getDate());
                            holder.username.setText(username);
                            if(dataSnapshot.hasChild("profile_image"))
                                Picasso.get().load(profileImage).into(holder.profileImage);

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getContext(), ProfileActivity.class);
                                    intent.putExtra("isLoginUser", false);
                                    intent.putExtra("visit_user_id", dataSnapshot.getKey().toString());
                                    intent.putExtra("username", username);
                                    intent.putExtra("age", dataSnapshot.child("age").getValue().toString());
                                    intent.putExtra("introduction", dataSnapshot.child("introduction").getValue().toString());
                                    if(profileImage != null)
                                        intent.putExtra("profileImage", profileImage);
                                    else
                                        intent.putExtra("profileImage", "");

                                    startActivity(intent);
                                }
                            });
                        }else{
                            Log.d("DisplayFriends", "Snapshot Not Exist");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getContext(), "something went wrong", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @NonNull
            @Override
            public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_user_list, viewGroup, false);
                FriendViewHolder viewHolder = new FriendViewHolder(view);
                Log.d("DisplayFriends", "OnCreate");
                return viewHolder;
            }
        };
        friendList.setAdapter(friendAdapter);
        friendAdapter.startListening();
        progressBar.setVisibility(View.GONE);
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder{

        CircleImageView profileImage;
        TextView username, date;
        ImageView mChat;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);

            mChat = (ImageView) itemView.findViewById(R.id.chat_button);
            profileImage = itemView.findViewById(R.id.profile_picture);
            username = itemView.findViewById(R.id.txt_username);
            date = itemView.findViewById(R.id.txt_date);
        }
    }

    public static class FriendRequestViewHolder extends RecyclerView.ViewHolder{

        LinearLayout mView;
        CircleImageView request_profileImage;
        TextView request_username;

        public FriendRequestViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView.findViewById(R.id.user_linear_layout);
            request_profileImage = itemView.findViewById(R.id.profile_picture);
            request_username = itemView.findViewById(R.id.txt_username);
        }
    }
}
