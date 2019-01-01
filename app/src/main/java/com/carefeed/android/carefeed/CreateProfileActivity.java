package com.carefeed.android.carefeed;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateProfileActivity extends AppCompatActivity {

    private ProgressBar mProgressBar;
    private EditText mUsername, mAge, mIntro;
    private CircleImageView profileImage;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private DatabaseReference userDb;

    private final static int GALLERY_PICK = 1;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_create_profile);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mUsername = (EditText) findViewById(R.id.cProfile_username);
        mAge = (EditText) findViewById(R.id.cProfile_age);
        mIntro = (EditText) findViewById(R.id.cProfile_intro);
        profileImage = (CircleImageView) findViewById(R.id.cProfile_picture);
        loadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        mStorage = FirebaseStorage.getInstance().getReference().child("profile_images");

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_PICK);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_PICK);
            }
        });

        userDb = FirebaseDatabase.getInstance().getReference().child("User_Info").child(currentUserID);
        userDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String image = dataSnapshot.child("profile_image").getValue().toString();
                    Picasso.get().load(image).placeholder(R.drawable.profile).into(profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mProgressBar.setVisibility(View.GONE);
        init();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null){
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1).start(this);
        }

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){
                Uri resultUri = result.getUri();

                StorageReference filePath = mStorage.child(currentUserID + ".jpg");

                loadingBar.setTitle("Uploading image");
                loadingBar.setMessage("Please wait, while the image is being uploading");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){

                            Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();

                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUrl = uri.toString();
                                    userDb.child("profile_image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            finish();
                                            Toast.makeText(CreateProfileActivity.this,"Image is succesfully uploaded", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();

                                            Intent selfIntent = new Intent (CreateProfileActivity.this, CreateProfileActivity.class);
                                            startActivity(selfIntent);
                                        }
                                    });
                                }
                            });
                        } else {
                            Toast.makeText(CreateProfileActivity.this, "Error occured" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            } else {
                Toast.makeText(CreateProfileActivity.this, "Image can't be crop", Toast.LENGTH_SHORT);
            }
        }
    }

    //check string if null
    private boolean isNull(String string){
        if(string.equals(""))
            return true;
        else
            return false;
    }

    // check input field if null
    private boolean checkNull(String username, String age, String intro) {
        if (isNull(username) || isNull(age) || isNull(intro)) {
            Toast.makeText(CreateProfileActivity.this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
            return true;
        } else
            return false;
    }

    // register button onClick event
    private void init(){
        Button btnConfirm = (Button) findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get input from user
                String username = mUsername.getText().toString();
                String age = mAge.getText().toString();
                String intro = mIntro.getText().toString();

                if(!checkNull(username, age, intro)) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    Map newPost = new HashMap();
                    newPost.put("username", username);
                    newPost.put("age", age);
                    newPost.put("introduction", intro);

                    userDb.updateChildren(newPost);
                    Toast.makeText(CreateProfileActivity.this, "Create Profile Successful",
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CreateProfileActivity.this, MainActivity.class);
                    finish();
                    startActivity(intent);
                }
            }
        });
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Snackbar.make(this.getWindow().getDecorView().findViewById(android.R.id.content), "Please click BACK again to exit", Snackbar.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    // ---------------------- Firebase -----------------------
    //setup firebase
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }
}
