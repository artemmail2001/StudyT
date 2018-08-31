package com.example.artik.studyt;

import android.content.DialogInterface;
import android.content.Intent;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ParticipantsActivity extends AppCompatActivity {
    private DatabaseReference mParticipantsDatabase, mKeysDatabase, mRoot, mUsersDatabase;
    private Query query;
    private FirebaseRecyclerAdapter adapter;
    private List<User> mUsers;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view);
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        String uid = getIntent().getStringExtra("lol");
        String key = getIntent().getStringExtra("key");
        mKeysDatabase = FirebaseDatabase.getInstance().getReference("Keys").child(key);
        mParticipantsDatabase = FirebaseDatabase.getInstance().getReference("Participants").child(uid).child(key);
        query = FirebaseDatabase.getInstance().getReference("Participants").child(uid).child(key);
        mRoot = FirebaseDatabase.getInstance().getReference();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference("Users");
        query = FirebaseDatabase.getInstance().getReference("Participants").child(uid).child(key);
        FirebaseRecyclerOptions<String> options =
                new FirebaseRecyclerOptions.Builder<String>()
                        .setQuery(query, String.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<String, ParticipantsHolder>(options) {

            @NonNull
            @Override
            public ParticipantsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_list, parent, false);

                return new ParticipantsHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final ParticipantsHolder holder, int position, @NonNull String s) {
                final String user_id = getRef(position).getKey();
                mParticipantsDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String ui = dataSnapshot.child(user_id).getValue().toString();
                        if(!ui.equals("null")) {
                            mUsersDatabase.child(ui).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String name = dataSnapshot.child("name").getValue().toString();
                                    String thumb = dataSnapshot.child("thumb_pic").getValue().toString();
                                    String u = dataSnapshot.child("uid").getValue().toString();
                                    holder.setDisplayName(name);
                                    if(!thumb.equals("null")) {
                                        holder.setUserImage(thumb);
                                    }
                                    holder.user_id = u;
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        } else{
                            holder.setDisplayName("место не занято");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        mRecyclerView.setAdapter(adapter);
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
    private class ParticipantsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mName;
        private CircleImageView mImageCircle;
        private String user_id;
        private String Name;
        private String Image;
        public ParticipantsHolder(View itemView) {
            super(itemView);
            mName = (TextView)itemView.findViewById(R.id.name_user);
            mImageCircle = (CircleImageView)itemView.findViewById(R.id.circle_image);
            itemView.setOnClickListener(this);
        }
        public void setDisplayName(String name){
            Name = name;
            mName.setText(name);
        }
        public void setUserImage(String image){
            Image = image;
            if (!image.equals("null")) {
                Picasso.get().load(image).into(mImageCircle);
            }
        }

        @Override
        public void onClick(View v) {
            if(!Name.equals("место не занято")) {
                CharSequence options[] = new CharSequence[]{"Посмотреть страничку", "Отправить сообщение"};

                final AlertDialog.Builder builder = new AlertDialog.Builder(ParticipantsActivity.this);

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (i == 0) {

                            Intent profileIntent = new Intent(ParticipantsActivity.this, UserProfileActivity.class);
                            profileIntent.putExtra("lol", user_id);
                            startActivity(profileIntent);

                        }

                        if (i == 1) {

                            Intent chatIntent = new Intent(ParticipantsActivity.this, ChatActivity.class);
                            chatIntent.putExtra("lol", user_id);
                            chatIntent.putExtra("name", Name);
                            chatIntent.putExtra("image", Image);
                            startActivity(chatIntent);

                        }

                    }
                });

                builder.show();
            }
        }
    }
}
