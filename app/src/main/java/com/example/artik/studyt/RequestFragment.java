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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestHandler;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class RequestFragment extends Fragment {
    private DatabaseReference mDatabase, mData, mFriendsDatabase, mD;
    FirebaseUser mCurrentUser;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private List<User> mRequests;
    private User mUser;
    public String uuiidd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.recycler_view, container, false);
        mRecyclerView = (RecyclerView)v.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        uuiidd = mCurrentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference("Requests").child(uuiidd);
        mDatabase.keepSynced(true);
        mD = FirebaseDatabase.getInstance().getReference("Requests");
        mData = FirebaseDatabase.getInstance().getReference("Users");
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference("Friends");
        mRequests = new ArrayList<>();
        mUser = new User();
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mRequests.clear();
                if(dataSnapshot.hasChildren()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Request request = snapshot.getValue(Request.class);
                        if(request.getRequest_type().toString().equals("received")) {
                            String req = snapshot.getKey();
                            mData.child(req).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    mUser = dataSnapshot.getValue(User.class);
                                    mRequests.add(mUser);
                                    mAdapter = new RequestsAdapter(mRequests);
                                    mRecyclerView.setAdapter(mAdapter);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private class RequestsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private User mUser;
        private TextView mName;
        private CircleImageView mImageCircle;
        private Button mAccept, mDecline;
        private String UID;
        public RequestsViewHolder(View itemView) {
            super(itemView);
            mName = (TextView)itemView.findViewById(R.id.name_req);
            mImageCircle = (CircleImageView)itemView.findViewById(R.id.circle_req);
            mAccept = (Button)itemView.findViewById(R.id.accept_req);
            mDecline = (Button)itemView.findViewById(R.id.decline_req);
            mAccept.setOnClickListener(this);
            mDecline.setOnClickListener(this);
        }

        public void bind(User user) {
            mUser = user;
            UID = mUser.getUid().toString();
            mName.setText(mUser.getName().toString());
            if(mUser.getThumb_pic() != "null") {
                String image = mUser.getThumb_pic().toString();
                if (!image.equals("null")) {
                    Picasso.get().load(image).into(mImageCircle);
                }
            }
        }

        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.accept_req:
                    mFriendsDatabase.child(uuiidd).child(UID).child("uidd").setValue(UID).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendsDatabase.child(UID).child(uuiidd).child("uidd").setValue(uuiidd).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mD.child(uuiidd).child(UID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mD.child(UID).child(uuiidd).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                    break;
                case R.id.decline_req:
                    mD.child(uuiidd).child(UID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mD.child(UID).child(uuiidd).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                }
                            });
                        }
                    });
                    break;
            }
        }
    }
    private class RequestsAdapter extends RecyclerView.Adapter<RequestsViewHolder>{
        private List<User> mUsers;
        private LayoutInflater mLayoutInflater;
        public RequestsAdapter(List<User> mU){
            mUsers = mU;
        }

        @NonNull
        @Override
        public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            mLayoutInflater = LayoutInflater.from(getActivity());
            View view = mLayoutInflater.inflate(R.layout.fragment_request, parent, false);
            return new RequestsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RequestsViewHolder holder, int position) {
            User user = mUsers.get(position);
            holder.bind(user);
        }

        @Override
        public int getItemCount() {
            return mUsers.size();
        }
    }
}
