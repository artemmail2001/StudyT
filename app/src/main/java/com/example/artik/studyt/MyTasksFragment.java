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
import android.widget.Adapter;
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
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyTasksFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private FirebaseUser mCurrentUser;
    private RecyclerView.Adapter mAdapter;
    private DatabaseReference mUsersDatabase, mIssuesDatabase;
    private static final String TAG = "MyTasks";
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
        mIssuesDatabase = FirebaseDatabase.getInstance().getReference("Issues").child(uid);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference("Users");
        mUsersDatabase.keepSynced(true);
        mIssues = new ArrayList<>();
        return view;
    }
    @Override
    public void onStart(){
        super.onStart();
        mIssuesDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mIssues.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    issue = snapshot.getValue(Issue.class);
                    mIssues.add(issue);
                    mAdapter = new TaskAdapter(mIssues);
                    mRecyclerView.setAdapter(mAdapter);
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
            if(is.getNumber_people_left() == 0){
                mBlock.setVisibility(View.VISIBLE);
            }
            mName.setText(is.getTitle());
            String image = is.getThumb();
            if (!image.equals("null")) {
                Picasso.get().load(image).into(mImageCircle);
            }
            mNumber.setText("осталось " + is.getNumber_people_left() + " мест(-а)");
            mDate.setText(is.getDate());
            mScore.setText(is.getScore() + " points");

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
                        profileIntent.putExtra("key", is.getKey());
                        startActivity(profileIntent);

                    }

                    if(i == 1){

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
