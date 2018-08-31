package com.example.artik.studyt;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendsDatabase;
    private TextView mName, mScore, mFriends, mSubject1, mSubject2;
    private List<String> fr;
    private CircleImageView mCircleImage;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mName = (TextView)view.findViewById(R.id.name);
        mScore = (TextView)view.findViewById(R.id.score_number);
        mSubject1 = (TextView)view.findViewById(R.id.subject1);
        mSubject2 = (TextView)view.findViewById(R.id.subject2);
        mCircleImage = (CircleImageView)view.findViewById(R.id.circle);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        return view;
    }
    @Override
    public void onStart(){
        super.onStart();
        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String score = dataSnapshot.child("score").getValue().toString();
                String subject1 = dataSnapshot.child("subject1").getValue().toString();
                String subject2 = dataSnapshot.child("subject2").getValue().toString();
                mScore.setText(score);
                if(!subject1.equals("пусто")) {
                    mSubject1.setText(subject1);
                }
                if(!subject2.equals("пусто")) {
                    mSubject2.setText(subject2);
                }
                if(dataSnapshot.hasChild("name")) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    mName.setText(name);
                }
                if(dataSnapshot.hasChild("thumb_pic")){
                    final String image = dataSnapshot.child("thumb_pic").getValue().toString();
                    if(!image.equals("null")) {
                        Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).into(mCircleImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(image).into(mCircleImage);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
