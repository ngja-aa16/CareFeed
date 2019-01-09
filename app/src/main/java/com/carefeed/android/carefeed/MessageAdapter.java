package com.carefeed.android.carefeed;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private DatabaseReference dbRef;
    private List<Message> messageList;
    private FirebaseAuth mAuth;

    public MessageAdapter(List<Message> messageList){
        this.messageList = messageList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView senderMessage, receiverMessage;
        public CircleImageView mImageView;
        public ImageView mSenderPhoto, mReceiverPhoto;
        public ProgressBar mSenderProgress, mReceiverProgress;
        public RelativeLayout mSenderRelative, mReceiverRelative;

        public MessageViewHolder(View itemView) {
            super(itemView);

            mSenderRelative = itemView.findViewById(R.id.message_sender_relative);
            mReceiverRelative = itemView.findViewById(R.id.message_receiver_relative);
            senderMessage = (TextView) itemView.findViewById(R.id.message_sender_message);
            receiverMessage = (TextView) itemView.findViewById(R.id.message_receiver_message);
            mImageView = (CircleImageView) itemView.findViewById(R.id.message_receiver_profile);
            mSenderPhoto = (ImageView) itemView.findViewById(R.id.message_sender_image);
            mReceiverPhoto = (ImageView) itemView.findViewById(R.id.message_receiver_image);
            mSenderProgress = (ProgressBar) itemView.findViewById(R.id.chat_sender_progress_bar);
            mReceiverProgress = (ProgressBar) itemView.findViewById(R.id.chat_receiver_progress_bar);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_message, viewGroup, false);
        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int i) {
        String messageSenderID = mAuth.getCurrentUser().getUid();
        final Message message = messageList.get(i);

        String fromUserID = message.getFrom();
        String fromMessageType = message.getType();

        dbRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Picasso.get().load(dataSnapshot.child("profile_image").getValue().toString()).into(holder.mImageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // image
        holder.mSenderPhoto.setVisibility(View.GONE);
        holder.mReceiverPhoto.setVisibility(View.GONE);
        holder.mSenderRelative.setVisibility(View.GONE);
        holder.mReceiverRelative.setVisibility(View.GONE);

        //profile picture
        holder.mImageView.setVisibility(View.GONE);
        // text
        holder.receiverMessage.setVisibility(View.GONE);
        holder.senderMessage.setVisibility(View.GONE);

        if(fromMessageType.equals("text")) {
            if(fromUserID.equals(messageSenderID)){
                holder.senderMessage.setVisibility(View.VISIBLE);
                holder.senderMessage.setBackgroundResource(R.drawable.sender_message);
                holder.senderMessage.setTextColor(Color.WHITE);
                holder.senderMessage.setGravity(Gravity.LEFT);
                holder.senderMessage.setText(message.getMessage());
            }
            else{
                holder.receiverMessage.setVisibility(View.VISIBLE);
                holder.mImageView.setVisibility(View.VISIBLE);

                holder.receiverMessage.setBackgroundResource(R.drawable.receiver_message);
                holder.receiverMessage.setTextColor(Color.BLACK);
                holder.receiverMessage.setGravity(Gravity.LEFT);
                holder.receiverMessage.setText(message.getMessage());
            }
        } else if(fromMessageType.equals("image")){
            if(fromUserID.equals(messageSenderID)){
                holder.mSenderRelative.setVisibility(View.VISIBLE);
                holder.mSenderProgress.setVisibility(View.VISIBLE);
                holder.mSenderPhoto.setVisibility(View.VISIBLE);
                Picasso.get().load(message.getMessage()).into(holder.mSenderPhoto, new com.squareup.picasso.Callback() {

                    @Override
                    public void onSuccess() {
                        holder.mSenderProgress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        holder.mSenderProgress.setVisibility(View.GONE);
                        holder.mSenderPhoto.setImageResource(R.drawable.failimage);
                    }
                });
            }
            else{
                holder.mImageView.setVisibility(View.VISIBLE);
                holder.mReceiverPhoto.setVisibility(View.VISIBLE);
                holder.mReceiverProgress.setVisibility(View.VISIBLE);
                holder.mReceiverRelative.setVisibility(View.VISIBLE);

                Picasso.get().load(message.getMessage()).into(holder.mReceiverPhoto, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        holder.mReceiverProgress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        holder.mReceiverProgress.setVisibility(View.GONE);
                        holder.mReceiverPhoto.setImageResource(R.drawable.failimage);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount()
    {
        return messageList.size();
    }
}
