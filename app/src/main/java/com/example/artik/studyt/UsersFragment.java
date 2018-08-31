package com.example.artik.studyt;

import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private Query query;
    private FirebaseUser mCurrentUser;
    private FirebaseRecyclerAdapter adapter;
    private DatabaseReference mUsersDatabase;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_view, container, false);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = mCurrentUser.getUid();
        query = FirebaseDatabase.getInstance().getReference("Users");
        mUsersDatabase = FirebaseDatabase.getInstance().getReference("Users");
        mUsersDatabase.keepSynced(true);
        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(query, User.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<User, UsersHolder>(options) {

            @NonNull
            @Override
            public UsersHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_list, parent, false);

                return new UsersHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final UsersHolder holder, int position, @NonNull User user) {
                final String user_id = getRef(position).getKey();
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
    private class UsersHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mName;
        private CircleImageView mImageCircle;
        private ImageView mGreenDot;
        private String user_id;
        public UsersHolder(View itemView) {
            super(itemView);
            mName = (TextView)itemView.findViewById(R.id.name_user);
            mImageCircle = (CircleImageView)itemView.findViewById(R.id.circle_image);
            mGreenDot = (ImageView)itemView.findViewById(R.id.green);
            itemView.setOnClickListener(this);
        }
        public void setDisplayName(String name){
            mName.setText(name);
        }
        public void setUserImage(String image){
            if (!image.equals("null")) {
                Picasso.get().load(image).into(mImageCircle);
            }
        }
        public void setTrueGreenDot(){
            mGreenDot.setVisibility(View.VISIBLE);
        }
        public void setFalseGreenDot(){
            mGreenDot.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onClick(View v) {
            Intent profileIntent = new Intent(getActivity(), UserProfileActivity.class);
            profileIntent.putExtra("lol", user_id);
            startActivity(profileIntent);
        }
    }
}
