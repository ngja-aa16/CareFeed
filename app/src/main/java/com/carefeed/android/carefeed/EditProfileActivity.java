package com.carefeed.android.carefeed;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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

public class EditProfileActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private CircleImageView mImageView;
    private EditText mUsername, mAge, mIntro;
    private TextView mChange;
    private ProgressBar mProgressBar;
    private User oldUserData;
    private String image;
    private Uri uploadedImage;

    // --> Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private StorageReference mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_edit_profile);

        mToolbar = (Toolbar) findViewById(R.id.edit_profile_toolbar);
        mImageView = (CircleImageView) findViewById(R.id.eProfile_picture);
        mUsername = (EditText) findViewById(R.id.eProfile_username);
        mAge = (EditText) findViewById(R.id.eProfile_age);
        mIntro = (EditText) findViewById(R.id.eProfile_intro);
        mChange = (TextView) findViewById(R.id.txt_change);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        // setting toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Edit Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // --> get Firebase
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference().child("profile_images");

        init();

        mChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(EditProfileActivity.this);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, Menu.FIRST, Menu.NONE, "Done")
                .setIcon(R.drawable.ic_done_white)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            case Menu.FIRST:
                // update firebase
                updateUserInformation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                uploadedImage = result.getUri();
                Picasso.get().load(uploadedImage).into(mImageView);
            }
        }
    }

    public void init() {
        mProgressBar.setVisibility(View.VISIBLE);
        // get data from previous activity
        Bundle extras = getIntent().getExtras();

        if(extras != null) {
            oldUserData = new User(extras.getString("age")
                    , extras.getString("introduction")
                    , extras.getString("profileImage")
                    , extras.getString("username"));

            // set data to current activity field
            mUsername.setText(oldUserData.getUsername());
            mAge.setText(oldUserData.getAge());
            mIntro.setText(oldUserData.getIntroduction());

            if(!oldUserData.getProfile_image().equals("")){
                Picasso.get().load(oldUserData.getProfile_image()).into(mImageView);
            }
        }

        mProgressBar.setVisibility(View.GONE);
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
            Toast.makeText(EditProfileActivity.this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
            return true;
        } else
            return false;
    }

    private void updateUserInformation(){
        mProgressBar.setVisibility(View.VISIBLE);
        String username = mUsername.getText().toString()
                , age = mAge.getText().toString()
                , intro = mIntro.getText().toString()
                , currentUserID = mAuth.getCurrentUser().getUid();
        final DatabaseReference userDb = rootRef.child("User_Info").child(currentUserID);
        final Intent intent = new Intent();

        if(!checkNull(username, age, intro)) {
            mProgressBar.setVisibility(View.VISIBLE);
            Map newPost = new HashMap();
            newPost.put("username", username);
            newPost.put("age", age);
            newPost.put("introduction", intro);

            userDb.updateChildren(newPost).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()) {
                        Log.d("UpdateFirebase", "Success");
                    }
                    else{
                        Log.d("UpdateFirebase", "Failed");
                    }
                }
            });

            intent.putExtra("username", username);
            intent.putExtra("age", age);
            intent.putExtra("introduction", intro);

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
                                    intent.putExtra("profileImage", downloadUrl);
                                    Log.d("Upload_Image", "Success" + downloadUrl);
                                    setResult(RESULT_OK,intent);
                                    finish();
                                    mProgressBar.setVisibility(View.GONE);
                                }
                            });
                        } else {
                            Toast.makeText(EditProfileActivity.this, "Error occured" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                intent.putExtra("profileImage", oldUserData.getProfile_image());
                setResult(RESULT_OK, intent);
                finish();
            }
        }
        else{
            mProgressBar.setVisibility(View.GONE);
        }
    }
}
