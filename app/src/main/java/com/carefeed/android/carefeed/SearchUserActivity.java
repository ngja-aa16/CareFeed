package com.carefeed.android.carefeed;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchUserActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView friendList;
    private EditText mSearch;
    private TextView mResult;

    private DatabaseReference userRef;
    private FirebaseRecyclerAdapter<User, UserViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_search_user);

        friendList = (RecyclerView) findViewById(R.id.recycler_friend);
        friendList.setLayoutManager(new LinearLayoutManager(this));
        mToolbar = (Toolbar) findViewById(R.id.main_app_bar);
        mSearch = (EditText) findViewById(R.id.txt_search_user);
        mResult = (TextView) findViewById(R.id.result_not_found);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Edit Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Search User");

        userRef = FirebaseDatabase.getInstance().getReference().child("User_Info");
    }

    @Override
    public void onStart(){
        super.onStart();

        mSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                final String searchItem = mSearch.getText().toString();
                Query query = userRef.orderByChild("username").startAt(searchItem).endAt(searchItem + "\uf8ff");
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(!dataSnapshot.exists()){
                            mResult.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                if(!searchItem.matches("")) {
                    friendList.setVisibility(View.VISIBLE);
                    FirebaseRecyclerOptions<User> options
                            = new FirebaseRecyclerOptions.Builder<User>()
                            .setQuery(query, User.class)
                            .build();

                    adapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull User model) {
                            mResult.setVisibility(View.GONE);

                            holder.username.setText(model.getUsername());
                            String profile_image = model.getProfile_image();
                            if (profile_image != null)
                                Picasso.get().load(model.getProfile_image()).into(holder.profileImage);
                        }

                        @NonNull
                        @Override
                        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_user_list, viewGroup, false);
                            UserViewHolder viewHolder = new UserViewHolder(view);
                            return viewHolder;
                        }
                    };
                    friendList.setAdapter(adapter);
                    adapter.startListening();
                }else{
                    friendList.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{

        CircleImageView profileImage;
        TextView username;
        LinearLayout linearLayout;

        public UserViewHolder(View itemView){
            super(itemView);
            linearLayout = itemView.findViewById(R.id.linear_layout);
            profileImage = itemView.findViewById(R.id.profile_picture);
            username = itemView.findViewById(R.id.txt_username);
        }
    }
}
