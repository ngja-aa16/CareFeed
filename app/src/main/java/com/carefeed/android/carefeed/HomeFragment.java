package com.carefeed.android.carefeed;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
public class HomeFragment extends Fragment {

    private RecyclerView eventList;
    private DatabaseReference eventRef, userRef, joinRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private FirebaseRecyclerAdapter<Event, PostViewHolder> firebaseRecyclerAdapter;
    private Query query;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.event_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent requestEventIntent = new Intent(getContext(), RequestEventActivity.class);
                startActivity(requestEventIntent);
            }
        });

        eventList = (RecyclerView) view.findViewById(R.id.event_post_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        eventList.setLayoutManager(linearLayoutManager);

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
            eventRef = FirebaseDatabase.getInstance().getReference().child("Event_Info");
            query = eventRef.orderByChild("Status").equalTo("approved");
            joinRef = FirebaseDatabase.getInstance().getReference().child("Event_Join");

            FirebaseRecyclerOptions<Event> firebaseRecyclerOptions =
                    new FirebaseRecyclerOptions.Builder<Event>()
                            .setQuery(query, Event.class)
                            .build();

            FirebaseRecyclerAdapter<Event, EventViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Event, EventViewHolder>(firebaseRecyclerOptions) {
                @Override
                protected void onBindViewHolder(@NonNull final EventViewHolder holder, int position, @NonNull Event model) {
                    holder.loadingBar.setVisibility(View.VISIBLE);
                    final String eventIDs = getRef(position).getKey();
                    holder.setJoinButtonStatus(eventIDs);
                    eventRef.child(eventIDs).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                final String eventTitle = dataSnapshot.child("title").getValue().toString();
                                final String eventDescription = dataSnapshot.child("description").getValue().toString();
                                final String date = dataSnapshot.child("date").getValue().toString();
                                final String time = dataSnapshot.child("time").getValue().toString();
                                final String eventImage = dataSnapshot.child("post_image").getValue().toString();

                                holder.titleText.setText(eventTitle);
                                holder.dateTimeText.setText(date + " " + time);
                                holder.descriptionText.setText(eventDescription);
                                Picasso.get().load(eventImage).into(holder.eventImage, new com.squareup.picasso.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        holder.loadingBar.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        holder.loadingBar.setVisibility(View.GONE);
                                        holder.eventImage.setImageResource(R.drawable.failimage);
                                    }
                                });

                                holder.eventImage.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent fullScreenIntent = new Intent(getContext(), FullScreenActivity.class);
                                        fullScreenIntent.putExtra("postImage", eventImage);
                                        startActivity(fullScreenIntent);
                                    }
                                });

                                holder.joinButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        joinRef.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                joinRef.child(eventIDs).child(currentUserId).setValue("true");
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

                        }
                    });
                }

                @NonNull
                @Override
                public EventViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                    View view = LayoutInflater.from(getContext()).inflate(R.layout.event_layout, viewGroup, false);
                    EventViewHolder eventHolder = new EventViewHolder(view);
                    return eventHolder;
                }
            };

            eventList.setAdapter(firebaseRecyclerAdapter);
            firebaseRecyclerAdapter.startListening();
        }
        return view;
    }

    public class EventViewHolder extends RecyclerView.ViewHolder{

        TextView titleText, descriptionText, dateTimeText, joinedCounts;
        ImageView eventImage;
        ProgressBar loadingBar;
        Button joinButton;
        int joinCount;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);

            titleText = itemView.findViewById(R.id.view_event_title);
            descriptionText = itemView.findViewById(R.id.view_event_description);
            dateTimeText = itemView.findViewById(R.id.view_event_dateTime);
            joinedCounts = itemView.findViewById(R.id.view_event_joined_counts);
            eventImage = itemView.findViewById(R.id.view_event_image);
            loadingBar = itemView.findViewById(R.id.event_layout_progress_bar);
            joinButton = itemView.findViewById(R.id.event_join_button);
        }

        public void setJoinButtonStatus(final String eventId){
            joinRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(eventId).hasChild(currentUserId)){
                        joinCount = (int) dataSnapshot.child(eventId).getChildrenCount();
                        joinButton.setText("Joined");
                        joinButton.setClickable(false);
                        joinedCounts.setText(Integer.toString(joinCount));
                    } else {
                        joinCount = (int) dataSnapshot.child(eventId).getChildrenCount();
                        joinButton.setText("Join");
                        joinButton.setClickable(true);
                        joinedCounts.setText(Integer.toString(joinCount));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(firebaseRecyclerAdapter != null)
        firebaseRecyclerAdapter.stopListening();
    }
}
