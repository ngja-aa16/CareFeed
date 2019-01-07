package com.carefeed.android.carefeed;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostDetail extends AppCompatActivity {

    private Toolbar topToolbar;
    private RelativeLayout postDetailsLayout;
    private CircleImageView profileImage;
    private TextView username, dateTime, postDescription;
    private ImageView postImage;
    private ProgressBar loadingBar;
    private DatabaseReference currentPostRef;
    private boolean isLoginUser;
    private static final int MENU_SECOND = 2;

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
        postImage = (ImageView) postDetailsLayout.findViewById(R.id.view_post_image);
        loadingBar = (ProgressBar) postDetailsLayout.findViewById(R.id.post_layout_progress_bar);

        isLoginUser = getIntent().getExtras().getBoolean("isLoginUser");
        String postID = getIntent().getExtras().getString("postID");

        currentPostRef = FirebaseDatabase.getInstance().getReference().child("Post_Info").child(postID);

        setSupportActionBar(topToolbar);
        getSupportActionBar().setTitle("Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setAllView();
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

    private Post retrieveIntentExtraValue(){
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
        Post post = retrieveIntentExtraValue();
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
    }

    private void deletePost(){
        currentPostRef.removeValue();
        final Handler handler = new Handler();
        finish();
        Toast.makeText(this, "Post has been deleted", Toast.LENGTH_SHORT).show();
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
}
