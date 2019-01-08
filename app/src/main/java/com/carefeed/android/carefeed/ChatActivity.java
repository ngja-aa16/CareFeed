package com.carefeed.android.carefeed;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private Toolbar chatToolbar;
    private ImageButton mSendImage, mSendText;
    private EditText mMessage;
    private RecyclerView mRecyclerView;

    private String username, profile_image, receiverUID, senderUID, currentDateString, currentTimeString;

    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_chat);

        username = getIntent().getStringExtra("username");
        profile_image = getIntent().getStringExtra("profileImage");
        receiverUID = getIntent().getStringExtra("visit_user_id");

        mSendImage = (ImageButton) findViewById(R.id.chat_upload_image);
        mSendText = (ImageButton) findViewById(R.id.chat_sent_button);
        mMessage = (EditText)  findViewById(R.id.chat_edit_text);
        mRecyclerView = (RecyclerView) findViewById(R.id.chat_recycler_view);

        // setting toolbar
        setSupportActionBar(chatToolbar);
        getSupportActionBar().setTitle(username);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // firebase
        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        senderUID = mAuth.getCurrentUser().getUid();

        mSendText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
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

                    }
                    else{
                        Toast.makeText(ChatActivity.this, "Error + "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    mMessage.setText("");
                }
            });
        }
    }
}
