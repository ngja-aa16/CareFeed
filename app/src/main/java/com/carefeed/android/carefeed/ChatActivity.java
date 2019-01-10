package com.carefeed.android.carefeed;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private Toolbar chatToolbar;
    private ImageButton mSendText, mCamera;
    private EditText mMessage;
    private RecyclerView mRecyclerView;

    // parse from last intent
    private String username, receiverUID;

    private String senderUID, currentDateString, currentTimeString;
    private final List<Message> messageList = new ArrayList<>();
    private static final int GALLERY_PICK = 1;
    private Uri imageUri;

    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    private StorageReference postImagesReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_chat);

        username = getIntent().getStringExtra("username");
        receiverUID = getIntent().getStringExtra("target_user_id");

        mCamera = (ImageButton) findViewById(R.id.chat_camera);
        mSendText = (ImageButton) findViewById(R.id.chat_sent_button);
        mMessage = (EditText)  findViewById(R.id.chat_edit_text);
        mRecyclerView = (RecyclerView) findViewById(R.id.chat_recycler_view);
        chatToolbar = (Toolbar) findViewById(R.id.chat_page_toolbar);

        // setting toolbar
        setSupportActionBar(chatToolbar);
        getSupportActionBar().setTitle(username);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // firebase
        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        senderUID = mAuth.getCurrentUser().getUid();
        postImagesReference = FirebaseStorage.getInstance().getReference().child("chat_images");

        mRecyclerView = (RecyclerView) findViewById(R.id.chat_recycler_view);
        messageAdapter = new MessageAdapter(messageList);
        linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(messageAdapter);

        mSendText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });
        mCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_PICK);
            }
        });

        FetchMessage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();
            Toast.makeText(ChatActivity.this, "Uploading image...", Toast.LENGTH_SHORT).show();
            uploadImage(imageUri);
        }
    }

    private void uploadImage(Uri imageUri){
        Log.d("uploadImage", "imageUri=" + imageUri.toString());
        DatabaseReference user_message_key = rootRef.child("Messages").child(senderUID).child(receiverUID).push();
        final String message_push_id = user_message_key.getKey();
        final String message_sender_ref = "Messages/" + senderUID + "/" + receiverUID;
        final String message_receiver_ref = "Messages/" + receiverUID + "/" + senderUID;

        Calendar calDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
        currentDateString = currentDate.format(calDate.getTime());

        Calendar calTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa", Locale.ENGLISH);
        currentTimeString = currentTime.format(calTime.getTime());

        StorageReference filePath = postImagesReference.child(imageUri.getLastPathSegment() + currentDateString + currentTimeString + ".jpg");
        final Map messageTextBody = new HashMap();

        filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();

                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUrl;
                            downloadUrl = uri.toString();

                            messageTextBody.put("message", downloadUrl);
                            messageTextBody.put("time", currentTimeString);
                            messageTextBody.put("date", currentDateString);
                            messageTextBody.put("type", "image");
                            messageTextBody.put("from", senderUID);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(message_sender_ref + "/" + message_push_id, messageTextBody);
                            messageBodyDetails.put(message_receiver_ref + "/" + message_push_id, messageTextBody);

                            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(ChatActivity.this, "Image has been sent", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Toast.makeText(ChatActivity.this, "Error + "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        mMessage.setText("");
                                    }
                                }
                            });
                        }
                    });
                } else {
                    Toast.makeText(ChatActivity.this, "Error occurred: " + task.getException().toString() , Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void FetchMessage() {
        rootRef.child("Messages").child(senderUID).child(receiverUID).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("FetchMessage", "datasnapshot");
                if(dataSnapshot.exists()){
                    Message message = dataSnapshot.getValue(Message.class);

                    messageList.add(message);
                    messageAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        messageAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                mRecyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                mRecyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });
    }

    private void SendMessage() {
        String message_text = mMessage.getText().toString();

        if(TextUtils.isEmpty(message_text)){
            Toast.makeText(this, "Please enter something...", Toast.LENGTH_SHORT).show();
        }
        else{
            DatabaseReference user_message_key = rootRef.child("Messages").child(senderUID).child(receiverUID).push();
            String message_push_id = user_message_key.getKey();
            String message_sender_ref = "Messages/" + senderUID + "/" + receiverUID;
            String message_receiver_ref = "Messages/" + receiverUID + "/" + senderUID;

            Calendar calDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
            currentDateString = currentDate.format(calDate.getTime());

            Calendar calTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa", Locale.ENGLISH);
            currentTimeString = currentTime.format(calTime.getTime());

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", message_text);
            messageTextBody.put("time", currentTimeString);
            messageTextBody.put("date", currentDateString);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", senderUID);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(message_sender_ref + "/" + message_push_id, messageTextBody);
            messageBodyDetails.put(message_receiver_ref + "/" + message_push_id, messageTextBody);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        mMessage.setText("");
                    }
                    else{
                        Toast.makeText(ChatActivity.this, "Error + "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        mMessage.setText("");
                    }
                }
            });
        }
    }
}
