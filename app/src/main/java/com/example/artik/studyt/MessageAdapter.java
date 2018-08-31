package com.example.artik.studyt;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{


    private List<Messages> mMessageList;
    private DatabaseReference mUserDatabase;

    public MessageAdapter(List<Messages> mMessageList) {

        this.mMessageList = mMessageList;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_message ,parent, false);

        return new MessageViewHolder(v);

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView mMessageText, mText;
        public CircleImageView mCircleImage, mCircle;

        public MessageViewHolder(View itemView) {
            super(itemView);

            mMessageText = (TextView) itemView.findViewById(R.id.message_text);
            mCircleImage = (CircleImageView) itemView.findViewById(R.id.circle_message);
            mText = (TextView) itemView.findViewById(R.id.message_text_view);
            mCircle = (CircleImageView) itemView.findViewById(R.id.circle_message_view);

        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {

        Messages c = mMessageList.get(i);

        final String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String from_user = c.getFrom();
        String message_type = c.getType();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

        if(!from_user.equals(user_id)){
            viewHolder.mText.setVisibility(View.INVISIBLE);
            viewHolder.mCircle.setVisibility(View.INVISIBLE);
            viewHolder.mMessageText.setText(c.getMessage());
        }else{
            viewHolder.mMessageText.setVisibility(View.INVISIBLE);
            viewHolder.mCircleImage.setVisibility(View.INVISIBLE);
            viewHolder.mText.setVisibility(View.VISIBLE);
            viewHolder.mCircle.setVisibility(View.VISIBLE);
            viewHolder.mText.setText(c.getMessage());
        }

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String image = dataSnapshot.child("thumb_pic").getValue().toString();
                if(!image.equals("null")) {
                    if(!from_user.equals(user_id)) {
                        Picasso.get().load(image).into(viewHolder.mCircleImage);
                    }
                    else{
                        Picasso.get().load(image).into(viewHolder.mCircle);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

}
