package com.example.artik.studyt;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;


public class NewsFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private Query query;
    private FirebaseUser mCurrentUser;
    private FirebaseRecyclerAdapter adapter;
    private DatabaseReference mUsersDatabase, mKeysDatabase;
    private ProgressBar mProgressBar;
    private static final String TAG = "NewsFragment";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_view, container, false);
        mProgressBar = (ProgressBar)view.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = mCurrentUser.getUid();
        query = FirebaseDatabase.getInstance().getReference("Keys");
        mKeysDatabase = FirebaseDatabase.getInstance().getReference("Keys");
        mUsersDatabase = FirebaseDatabase.getInstance().getReference("Users");
        mUsersDatabase.keepSynced(true);
        FirebaseRecyclerOptions<Issue> options =
                new FirebaseRecyclerOptions.Builder<Issue>()
                        .setQuery(query, Issue.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<Issue, NewsHolder>(options) {

            @NonNull
            @Override
            public NewsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_news, parent, false);

                return new NewsHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final NewsHolder holder, int position, @NonNull Issue issue) {
                final String key = getRef(position).getKey();
                mKeysDatabase.child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String date = dataSnapshot.child("date").getValue().toString();
                        String image = dataSnapshot.child("thumb").getValue().toString();
                        String name = dataSnapshot.child("title").getValue().toString();
                        String score = dataSnapshot.child("score").getValue().toString();
                        String number_people_left = dataSnapshot.child("number_people_left").getValue().toString();
                        String time = dataSnapshot.child("time").getValue().toString();
                        holder.setDisplayName(name);
                        int a = Integer.parseInt(number_people_left);
                        if(!image.equals("null")) {
                            holder.setUserImage(image);
                        }
                        if(a == 0){
                            holder.setBlocked();
                        }
                        holder.setDisplayScore(score);
                        holder.setDisplayDate(time, date);
                        holder.setDisplayNumber(number_people_left);
                        holder.key = key;
                        mProgressBar.setVisibility(View.GONE);
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
    private class NewsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mName, mDate, mNumber, mScore;
        private CircleImageView mImageCircle;
        private String key;
        private ImageView mBlock;
        public NewsHolder(View itemView) {
            super(itemView);
            mName = (TextView)itemView.findViewById(R.id.title_news);
            mImageCircle = (CircleImageView)itemView.findViewById(R.id.circle_news);
            mDate = (TextView)itemView.findViewById(R.id.date_news);
            mNumber = (TextView)itemView.findViewById(R.id.number_people_news);
            mScore = (TextView)itemView.findViewById(R.id.score_news);
            mBlock = (ImageView)itemView.findViewById(R.id.block_news);
            itemView.setOnClickListener(this);
        }
        public void setBlocked(){
            mBlock.setVisibility(View.VISIBLE);
        }
        public void setDisplayName(String name){
            mName.setText(name);
        }
        public void setUserImage(String image){
            if (!image.equals("null")) {
                Picasso.get().load(image).into(mImageCircle);
            }
        }
        public void setDisplayNumber(String num_left){
            mNumber.setText("осталось " + num_left + " мест(-а)");
        }
        public void setDisplayDate(String time, String date){
            mDate.setText(time + "  " + date);
        }
        public void setDisplayScore(String score){
            mScore.setText(score + " points");
        }

        @Override
        public void onClick(View v) {
            Intent profileIntent = new Intent(getActivity(), NewsActivity.class);
            profileIntent.putExtra("key", key);
            startActivity(profileIntent);
        }
    }
}
