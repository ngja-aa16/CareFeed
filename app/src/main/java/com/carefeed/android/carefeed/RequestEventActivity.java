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
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class RequestEventActivity extends AppCompatActivity {

    private Toolbar topToolbar;
    private ImageButton uploadImageButton;
    private EditText titleText, descriptionText;
    private ProgressBar loadingBar;

    private Uri imageUri;
    private String downloadUrl, currentUserId;
    private static final int GALLERY_PICK = 1;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef, eventRef;
    private StorageReference imageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_request_event);

        topToolbar = (Toolbar) findViewById(R.id.create_event_page_toolbar);
        uploadImageButton = (ImageButton) findViewById(R.id.create_event_upload_image_button);
        titleText = (EditText) findViewById(R.id.create_event_title_text);
        descriptionText = (EditText) findViewById(R.id.create_event_description_text);
        loadingBar = (ProgressBar) findViewById(R.id.create_event_progress_bar);

        setSupportActionBar(topToolbar);
        getSupportActionBar().setTitle("Request Event");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadingBar.setVisibility(View.GONE);

        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_PICK);
            }
        });

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        imageRef = FirebaseStorage.getInstance().getReference().child("event_images");
        userRef = FirebaseDatabase.getInstance().getReference().child("User_Info");
        eventRef = FirebaseDatabase.getInstance().getReference().child("Event_Info");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();
            Picasso.get().load(imageUri).into(uploadImageButton);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, Menu.FIRST , Menu.NONE, "Request").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            case Menu.FIRST:
                validateInputs();
                return true;
            default:
                Toast.makeText(this, "Error occrued, please try to upload again", Toast.LENGTH_SHORT).show();
                return false;
        }
    }

    private void validateInputs(){
        final String eventDescription = descriptionText.getText().toString();
        final String eventTitle = titleText.getText().toString();

        if(imageUri == null){
            Toast.makeText(this, "Please insert an image", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(titleText.getText().toString())){
            Toast.makeText(this, "Please insert a title for the event", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(eventDescription)){
            Toast.makeText(this, "Please insert a description for the event", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setVisibility(View.VISIBLE);

            Calendar calDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
            final String currentDateString = currentDate.format(calDate.getTime());

            Calendar calTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
            final String currentTimeString = currentTime.format(calTime.getTime());

            StorageReference filePath = imageRef.child(imageUri.getLastPathSegment() + currentDateString + currentTimeString + ".jpg");

            filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();

                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadUrl = uri.toString();

                                userRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()){

                                            HashMap eventsMap = new HashMap();
                                            eventsMap.put("userID", currentUserId);
                                            eventsMap.put("title", eventTitle);
                                            eventsMap.put("date", currentDateString);
                                            eventsMap.put("time", currentTimeString);
                                            eventsMap.put("description", eventDescription);
                                            eventsMap.put("post_image", downloadUrl);
                                            eventsMap.put("noOfPersonJoined", 0);
                                            eventsMap.put("Status", "requested");

                                            eventRef.push().updateChildren(eventsMap).addOnCompleteListener(new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task task) {
                                                    if(task.isSuccessful()){
                                                        finish();
                                                        Toast.makeText(RequestEventActivity.this, "The event request is successfully uploaded to server", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(RequestEventActivity.this, "Error occured while creating post", Toast.LENGTH_SHORT).show();
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
                        });
                    } else {
                        Toast.makeText(RequestEventActivity.this, "Error occurred: " + task.getException().toString() , Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
