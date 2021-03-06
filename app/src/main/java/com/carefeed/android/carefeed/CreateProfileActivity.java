package com.carefeed.android.carefeed;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

    private Uri uploadedImage;

    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private DatabaseReference userDb;

    private String currentUserID;
    private boolean imageUploaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_create_profile);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);
        mUsername = (EditText) findViewById(R.id.cProfile_username);
        mAge = (EditText) findViewById(R.id.cProfile_age);
        mIntro = (EditText) findViewById(R.id.cProfile_intro);
        profileImage = (CircleImageView) findViewById(R.id.cProfile_picture);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        mStorage = FirebaseStorage.getInstance().getReference().child("profile_images");

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(CreateProfileActivity.this);
            }
        });

        userDb = FirebaseDatabase.getInstance().getReference().child("User_Info").child(currentUserID);

        mProgressBar.setVisibility(View.GONE);
        init();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*if(requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null){
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1).start(this);
        }*/

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){
                uploadedImage = result.getUri();
                Picasso.get().load(uploadedImage).into(profileImage); //profileImageView
            } else {
                Toast.makeText(CreateProfileActivity.this, "Image can't be crop", Toast.LENGTH_SHORT).show();
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
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    final Map newPost = new HashMap();
                    newPost.put("username", username);
                    newPost.put("age", age);
                    newPost.put("introduction", intro);

                    if(uploadedImage != null){
                        StorageReference filePath = mStorage.child(currentUserID + ".jpg");

                        //Add image to firebase storage
                        filePath.putFile(uploadedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if(task.isSuccessful()){

                                    Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();

                                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            final String downloadUrl = uri.toString();

                                            //Add image url to profile firebase
                                            userDb.child("profile_image").setValue(downloadUrl);
                                            //Update profile info to profile firebase

                                            userDb.updateChildren(newPost).addOnCompleteListener(new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task task) {

                                                    //On complete, send user to main activity
                                                    mProgressBar.setVisibility(View.GONE);
                                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                    Toast.makeText(CreateProfileActivity.this, "Create Profile Successful",
                                                            Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(CreateProfileActivity.this, MainActivity.class);
                                                    finish();
                                                    startActivity(intent);
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    Toast.makeText(CreateProfileActivity.this, "Error occured" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        //Update profile info to profile firebase
                        userDb.updateChildren(newPost).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {

                                //On complete, send user to main activity
                                mProgressBar.setVisibility(View.GONE);
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                Toast.makeText(CreateProfileActivity.this, "Create Profile Successful",
                                        Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CreateProfileActivity.this, MainActivity.class);
                                finish();
                                startActivity(intent);
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Please complete your profile", Toast.LENGTH_SHORT).show();
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
