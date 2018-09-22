package com.example.artik.studyt;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class EventsFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mKeysDatabase, mEventsDatabase;
    private RecyclerView.Adapter mAdapter;
    private Issue issue;
    private List<Issue> mIssues;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_view, container, false);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = mCurrentUser.getUid();
        mEventsDatabase = FirebaseDatabase.getInstance().getReference("Events").child(uid);
        mKeysDatabase = FirebaseDatabase.getInstance().getReference("Keys");
        mIssues = new ArrayList<>();
        return view;
    }
    @Override
    public void onStart(){
        super.onStart();
        mIssues.clear();
        mEventsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    final String key = snapshot.getKey();
                    mKeysDatabase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot1) {
                            if(dataSnapshot1.hasChild(key)) {
                                mKeysDatabase.child(key).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot2) {
                                        issue = dataSnapshot2.getValue(Issue.class);
                                        mIssues.add(issue);
                                        mAdapter = new TaskAdapter(mIssues);
                                        mRecyclerView.setAdapter(mAdapter);
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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private class TaskAdapter extends RecyclerView.Adapter<TasksHolder>{
        private LayoutInflater mLayoutInflater;
        private List<Issue> mI;
        public TaskAdapter(List<Issue> issues) {
            mI = issues;
        }
        @NonNull
        @Override
        public TasksHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            mLayoutInflater = LayoutInflater.from(getActivity());
            View view = mLayoutInflater.inflate(R.layout.fragment_news, parent, false);
            return new TasksHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TasksHolder holder, int position) {
            Issue iss = mI.get(position);
            holder.bind(iss);
        }

        @Override
        public int getItemCount() {
            return mIssues.size();
        }
    }
    private class TasksHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mName, mDate, mNumber, mScore;
        private CircleImageView mImageCircle;
        private Issue is;
        private ImageView mBlock;
        private int click = 0;
        public TasksHolder(View itemView) {
            super(itemView);
            mName = (TextView)itemView.findViewById(R.id.title_news);
            mImageCircle = (CircleImageView)itemView.findViewById(R.id.circle_news);
            mDate = (TextView)itemView.findViewById(R.id.date_news);
            mNumber = (TextView)itemView.findViewById(R.id.number_people_news);
            mScore = (TextView)itemView.findViewById(R.id.score_news);
            mBlock = (ImageView)itemView.findViewById(R.id.block_news);
            itemView.setOnClickListener(this);
        }
        public void bind(Issue i){
            is = i;
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);
            month = month + 1;

            String dd1 = is.getDate().substring(0, 2);
            int dd = Integer.parseInt(dd1);
            String mm1 = is.getDate().substring(3, 5);
            int mm = Integer.parseInt(mm1);
            String yy1 = is.getDate().substring(6);
            int yy = Integer.parseInt(yy1);
            if((month > mm && year >= yy) || (day>=dd && month==mm && year==yy)){
                click = 1;
            }

            if(is.getNumber_people_left() == 0){
                mBlock.setVisibility(View.VISIBLE);
            }
            mName.setText(is.getTitle());
            String image = is.getThumb();
            if (!image.equals("null")) {
                Picasso.get().load(image).into(mImageCircle);
            }
            mNumber.setText("осталось " + is.getNumber_people_left() + " мест(-а)");
            mDate.setText(is.getTime() + "  " + is.getDate());
            mScore.setText(is.getScore() + " points");

        }

        @Override
        public void onClick(View v) {
            if(click == 0) {
                CharSequence options[] = new CharSequence[]{"Посмотреть событие", "Посмотреть участников"};

                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (i == 0) {

                            Intent profileIntent = new Intent(getContext(), NewsActivity.class);
                            profileIntent.putExtra("key", is.getKey());
                            startActivity(profileIntent);

                        }

                        if (i == 1) {

                            Intent intent = new Intent(getContext(), ParticipantsActivity.class);
                            intent.putExtra("lol", is.getUid());
                            intent.putExtra("key", is.getKey());
                            startActivity(intent);

                        }

                    }
                });

                builder.show();
            }
        }
    }
}
