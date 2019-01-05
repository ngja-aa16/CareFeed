package com.carefeed.android.carefeed;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class CreatePostActivity extends AppCompatActivity {

    private ImageButton uploadImageButton;
    private ProgressBar progressBar;
    private EditText postTitleText, postDescriptionText;
    private Toolbar topToolbar;
    private Uri imageUri;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference userReference, postReference;
    private StorageReference postImagesReference;

    private static final int GALLERY_PICK = 1;
    private String downloadUrl, currentUserId, currentDateString, currentTimeString, postTitle, postDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_create_post);

        topToolbar = findViewById(R.id.create_post_page_toolbar);
        setSupportActionBar(topToolbar);
        getSupportActionBar();
        getSupportActionBar().setTitle("Create Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        uploadImageButton = (ImageButton) findViewById(R.id.upload_image_button);
        postTitleText = (EditText) findViewById(R.id.post_title_text);
        postDescriptionText = (EditText) findViewById(R.id.post_description_text);
        progressBar = (ProgressBar) findViewById(R.id.post_progress_bar);
        progressBar.setVisibility(View.GONE);

        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_PICK);
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        userReference = FirebaseDatabase.getInstance().getReference().child("User_Info");
        postReference = FirebaseDatabase.getInstance().getReference().child("Post_Info");
        postImagesReference = FirebaseStorage.getInstance().getReference().child("post_images");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();
            uploadImageButton.setImageURI(imageUri);
        }
    }

    private void validateInputs(){
        postTitle = postTitleText.getText().toString();
        postDescription = postDescriptionText.getText().toString();
        if(imageUri == null){
            Toast.makeText(this, "Please insert an image", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(postTitle)){
            Toast.makeText(this, "Please insert a title for the post", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(postDescription)){
            Toast.makeText(this, "Please insert a description for the post", Toast.LENGTH_SHORT).show();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            StoreImageToFirebase();
        }
    }

    private void StoreImageToFirebase() {
        Calendar calDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMM-yyyy");
        currentDateString = currentDate.format(calDate.getTime());

        Calendar calTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        currentTimeString = currentTime.format(calTime.getTime());

        StorageReference filePath = postImagesReference.child(imageUri.getLastPathSegment() + currentDateString + currentTimeString + ".jpg");

        filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();

                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            downloadUrl = uri.toString();
                            SavingPostInformationToFirebase();
                        }
                    });
                } else {
                    Toast.makeText(CreatePostActivity.this, "Error occurred: " + task.getException().toString() , Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SavingPostInformationToFirebase() {
        userReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    HashMap postsMap = new HashMap();
                    postsMap.put("userID", currentUserId);
                    postsMap.put("date", currentDateString);
                    postsMap.put("time", currentTimeString);
                    postsMap.put("title", postTitle);
                    postsMap.put("description", postDescription);
                    postsMap.put("post_image", downloadUrl);

                    postReference.push().updateChildren(postsMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){
                                finish();
                                Toast.makeText(CreatePostActivity.this, "The post is successfully created", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(CreatePostActivity.this, "Error occured while creating post", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, Menu.FIRST , Menu.NONE, "Post").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case Menu.FIRST:
                validateInputs();
                return true;
            default:
                Toast.makeText(this, "Error occrued, please try to upload again", Toast.LENGTH_SHORT).show();
                return false;
        }
    }
}
