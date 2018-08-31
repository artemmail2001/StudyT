package com.example.artik.studyt;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatFragment extends Fragment {
    private DatabaseReference mUsersDatabase, mMessageDatabase, mChatDatabase;
    private RecyclerView mRecyclerView;
    private String mCurrentUserId;
    private FirebaseRecyclerAdapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_view, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        mCurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mChatDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrentUserId);
        mChatDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrentUserId);
        mUsersDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        Query query = FirebaseDatabase.getInstance().getReference("Chat").child(mCurrentUserId).orderByChild("timestamp");
        FirebaseRecyclerOptions<Chats> options =
                new FirebaseRecyclerOptions.Builder<Chats>()
                        .setQuery(query, Chats.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<Chats, ChatHolder>(options) {

            @NonNull
            @Override
            public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_chat, parent, false);

                return new ChatHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final ChatHolder holder, int position, @NonNull final Chats chats) {
                final String user_id = getRef(position).getKey();
                Query query1 = mMessageDatabase.child(user_id).limitToLast(1);
                query1.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        String message = dataSnapshot.child("message").getValue().toString();
                        holder.setMessage(message, chats.isSeen());

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                mUsersDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String uidd = dataSnapshot.child("uid").getValue().toString();
                        String image = dataSnapshot.child("thumb_pic").getValue().toString();
                        String name = dataSnapshot.child("name").getValue().toString();
                        String online = dataSnapshot.child("online").getValue().toString();
                        holder.setDisplayName(name);
                        if(!image.equals("null")) {
                            holder.setUserImage(image);
                        }
                        if(online.equals("true")){
                            holder.setTrueGreenDot();
                        } else {
                            holder.setFalseGreenDot();
                        }
                        holder.user_id = uidd;
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };
        mRecyclerView.setAdapter(adapter);
        return view;
    }
    @Override
    public void onStart(){
        super.onStart();
        adapter.startListening();
    }
    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
    private class ChatHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mName, mMessage;
        private ImageView mGreenDot;
        private CircleImageView mCircleImage;
        private String user_id;
        private String Name;
        public ChatHolder(View itemView) {
            super(itemView);
            mName = (TextView)itemView.findViewById(R.id.name_chat);
            mMessage = (TextView)itemView.findViewById(R.id.message_chat);
            mCircleImage = (CircleImageView)itemView.findViewById(R.id.circle_chat);
            mGreenDot = (ImageView)itemView.findViewById(R.id.green_chat);
            itemView.setOnClickListener(this);
        }
        public void setDisplayName(String name){
            mName.setText(name);
            Name = name;
        }
        public void setUserImage(String image){
            if (!image.equals("null")) {
                Picasso.get().load(image).into(mCircleImage);
            }
        }
        public void setTrueGreenDot(){
            mGreenDot.setVisibility(View.VISIBLE);
        }
        public void setFalseGreenDot(){
            mGreenDot.setVisibility(View.INVISIBLE);
        }
        public void setMessage(String message, boolean isSeen){
            mMessage.setText(message);

            if(!isSeen){
                mMessage.setTypeface(mMessage.getTypeface(), Typeface.BOLD);
            } else {
                mMessage.setTypeface(mMessage.getTypeface(), Typeface.NORMAL);
            }

        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra("lol", user_id);
            intent.putExtra("name", Name);
            startActivity(intent);
        }
    }


}
