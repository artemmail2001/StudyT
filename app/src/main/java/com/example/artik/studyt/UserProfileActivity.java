package com.example.artik.studyt;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {
    private TextView mName, mScore, mSubject2, mSubject1;
    private CircleImageView mCircleImage;
    private Button mSend;
    private Button mDecline;
    private Toolbar mUserToolbar;
    private DatabaseReference mDatabase;
    private DatabaseReference mData;
    private DatabaseReference mFriendsDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private String status;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        mUserToolbar = (Toolbar) findViewById(R.id.user_app_bar);
        setSupportActionBar(mUserToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        status = "не друзья";
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        final String uId = currentUser.getUid();
        final String uidd = getIntent().getStringExtra("lol");
        mDatabase = FirebaseDatabase.getInstance().getReference("Requests");
        mData = FirebaseDatabase.getInstance().getReference().child("Users").child(uidd);
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference("Friends");
        mName = (TextView)findViewById(R.id.name);
        mScore = (TextView)findViewById(R.id.score_number);
        mSubject1 = (TextView)findViewById(R.id.subject1);
        mSubject2 = (TextView)findViewById(R.id.subject2);
        mSend = (Button)findViewById(R.id.send_button);
        mDecline = (Button)findViewById(R.id.decline_button);
        mCircleImage = (CircleImageView)findViewById(R.id.circle);
        mDecline.setVisibility(View.INVISIBLE);
        if(!uidd.equals(uId)) {
            mSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!uId.equals(uidd)) {
                        if (status == "не друзья") {
                            mSend.setEnabled(false);
                            mDatabase.child(uId).child(uidd).child("request_type").setValue("sent").addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mDatabase.child(uidd).child(uId).child("request_type").setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mSend.setEnabled(true);
                                            status = "запрос отправлен";
                                            mSend.setText("убрать запрос");
                                            mSend.setTextColor(getResources().getColor(R.color.red));
                                        }
                                    });
                                }
                            });
                        } else if (status == "запрос отправлен") {
                            mSend.setEnabled(false);
                            mDatabase.child(uId).child(uidd).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mDatabase.child(uidd).child(uId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mSend.setEnabled(true);
                                            status = "не друзья";
                                            mSend.setText("добавить в друзья");
                                            mSend.setTextColor(getResources().getColor(R.color.black));
                                        }
                                    });
                                }
                            });
                        } else if (status == "запрос получен") {
                            mSend.setEnabled(false);
                            mDecline.setEnabled(false);
                            mFriendsDatabase.child(uId).child(uidd).child("uidd").setValue(uidd).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendsDatabase.child(uidd).child(uId).child("uidd").setValue(uId).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mDatabase.child(uId).child(uidd).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mDatabase.child(uidd).child(uId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            status = "друзья";
                                                            mSend.setText("убрать из друзей");
                                                            mSend.setTextColor(getResources().getColor(R.color.red));
                                                            mSend.setEnabled(true);
                                                            mDecline.setEnabled(true);
                                                            mDecline.setVisibility(View.INVISIBLE);
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        } else if (status == "друзья") {
                            mSend.setEnabled(false);
                            mFriendsDatabase.child(uId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendsDatabase.child(uidd).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            status = "не друзья";
                                            mSend.setText("добавить в друзья");
                                            mSend.setTextColor(getResources().getColor(R.color.black));
                                            mSend.setEnabled(true);
                                        }
                                    });
                                }
                            });
                        }
                    }
                }
            });
            mDecline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (status == "запрос получен") {
                        mDecline.setEnabled(false);
                        mDatabase.child(uId).child(uidd).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mDatabase.child(uidd).child(uId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mSend.setText("добавить в друзья");
                                        mSend.setTextColor(getResources().getColor(R.color.black));
                                        status = "не друзья";
                                        mDecline.setEnabled(true);
                                        mDecline.setVisibility(View.INVISIBLE);
                                    }
                                });
                            }
                        });
                    }
                }
            });
        }
        mData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("name") && dataSnapshot.hasChild("thumb_pic")) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    if(name.equals("Helper")){
                        mSend.setEnabled(false);
                        mSend.setVisibility(View.INVISIBLE);
                    }
                    mName.setText(name);
                    String score = dataSnapshot.child("score").getValue().toString();
                    mScore.setText(score);
                    String subject1 = dataSnapshot.child("subject1").getValue().toString();
                    String subject2 = dataSnapshot.child("subject2").getValue().toString();
                    if(!subject1.equals("пусто")) {
                        mSubject1.setText(subject1);
                    }
                    if(!subject2.equals("пусто")) {
                        mSubject2.setText(subject2);
                    }
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
                if(uidd.equals(uId)){
                    mSend.setVisibility(View.INVISIBLE);
                }
                if(!uidd.equals(uId)) {
                    mFriendsDatabase.child(uId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(uidd)) {
                                status = "друзья";
                                mSend.setText("убрать из друзей");
                                mSend.setTextColor(getResources().getColor(R.color.red));
                            } else {
                                mSend.setText("Добавить в друзья");
                                mSend.setTextColor(getResources().getColor(R.color.black));
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    mDatabase.child(uId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(uidd)) {
                                String value = dataSnapshot.child(uidd).child("request_type").getValue().toString();
                                if (value.equals("sent")) {
                                    status = "запрос отправлен";
                                    mSend.setText("Убрать запрос");
                                    mSend.setTextColor(getResources().getColor(R.color.red));
                                } else if (value.equals("received")) {
                                    status = "запрос получен";
                                    mSend.setText("Подтвердить запрос");
                                    mSend.setTextColor(getResources().getColor(R.color.black));
                                    mDecline.setVisibility(View.VISIBLE);
                                } else {
                                    mSend.setText("Добавить в друзья");
                                    mSend.setTextColor(getResources().getColor(R.color.black));
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
