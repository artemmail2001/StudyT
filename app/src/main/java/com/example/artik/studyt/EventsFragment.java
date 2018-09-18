package com.example.artik.studyt;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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

public class EventsFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mKeysDatabase;
    private FirebaseRecyclerAdapter adapter;
    private Query query;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_view, container, false);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = mCurrentUser.getUid();
        mKeysDatabase = FirebaseDatabase.getInstance().getReference("Keys");
        mKeysDatabase.keepSynced(true);
        query = FirebaseDatabase.getInstance().getReference("Events").child(uid);
        FirebaseRecyclerOptions<Position> options =
                new FirebaseRecyclerOptions.Builder<Position>()
                        .setQuery(query, Position.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<Position, EventsHolder>(options) {

            @NonNull
            @Override
            public EventsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_news, parent, false);

                return new EventsHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final EventsHolder holder, int position, @NonNull Position pos) {
                final String key = getRef(position).getKey();
                mKeysDatabase.child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String date = dataSnapshot.child("date").getValue().toString();
                        String image = dataSnapshot.child("thumb").getValue().toString();
                        String name = dataSnapshot.child("title").getValue().toString();
                        String score = dataSnapshot.child("score").getValue().toString();
                        String number_people_left = dataSnapshot.child("number_people_left").getValue().toString();
                        String user_id = dataSnapshot.child("uid").getValue().toString();
                        holder.setDisplayTitle(name);
                        if(!image.equals("null")) {
                            holder.setUserImage(image);
                        }
                        int a = Integer.parseInt(number_people_left);
                        if(a == 0){
                            holder.setBlock();
                        }
                        holder.setDisplayScore(score);
                        holder.setDisplayDate(date);
                        holder.setDisplayNumber(number_people_left);
                        holder.key = key;
                        holder.user_id = user_id;
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
    private class EventsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mName, mDate, mNumber, mScore;
        private CircleImageView mImageCircle;
        private String key;
        private String user_id;
        private ImageView mBlock;
        public EventsHolder(View itemView) {
            super(itemView);
            mName = (TextView)itemView.findViewById(R.id.title_news);
            mImageCircle = (CircleImageView)itemView.findViewById(R.id.circle_news);
            mDate = (TextView)itemView.findViewById(R.id.date_news);
            mNumber = (TextView)itemView.findViewById(R.id.number_people_news);
            mScore = (TextView)itemView.findViewById(R.id.score_news);
            mBlock = (ImageView)itemView.findViewById(R.id.block_news);
            itemView.setOnClickListener(this);
        }
        public void setBlock(){
            mBlock.setVisibility(View.VISIBLE);
        }
        public void setDisplayTitle(String title){
            mName.setText(title);
        }
        public void setUserImage(String image){
            if (!image.equals("null")) {
                Picasso.get().load(image).into(mImageCircle);
            }
        }
        public void setDisplayNumber(String num_left){
            mNumber.setText("осталось " + num_left + " мест(-а)");
        }
        public void setDisplayDate(String date){
            mDate.setText(date);
        }
        public void setDisplayScore(String score){
            mScore.setText(score + " points");
        }

        @Override
        public void onClick(View v) {
            CharSequence options[] = new CharSequence[]{"Посмотреть событие", "Посмотреть участников"};

            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    if(i == 0){

                        Intent profileIntent = new Intent(getContext(), NewsActivity.class);
                        profileIntent.putExtra("key", key);
                        startActivity(profileIntent);

                    }

                    if(i == 1){

                        Intent intent = new Intent(getContext(), ParticipantsActivity.class);
                        intent.putExtra("lol", user_id);
                        intent.putExtra("key", key);
                        startActivity(intent);

                    }

                }
            });

            builder.show();
        }
    }
}
