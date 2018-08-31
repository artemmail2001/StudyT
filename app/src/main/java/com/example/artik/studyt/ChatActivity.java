package com.example.artik.studyt;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String mAnotherUser;
    private Toolbar mChatToolbar;
    private DatabaseReference mRoot;
    private TextView mName;
    private TextView mLastSeenView;
    private CircleImageView mProfileImage;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private ImageButton mChatSendBtn;
    private EditText mChatMessageView;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mRefreshLayout;
    private final List<Messages> messagesList = new ArrayList<>();
    private MessageAdapter mAdapter;
    private LinearLayoutManager LinearLayout;
    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;
    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mChatToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        mRoot = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mAnotherUser = getIntent().getStringExtra("lol");
        String userName = getIntent().getStringExtra("name");
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View bar = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(bar);
        mName = (TextView) findViewById(R.id.bar_title);
        mLastSeenView = (TextView) findViewById(R.id.bar_seen);
        mProfileImage = (CircleImageView) findViewById(R.id.bar_image);
        mChatSendBtn = (ImageButton) findViewById(R.id.chat_send);
        mChatMessageView = (EditText) findViewById(R.id.chat_message);
        mAdapter = new MessageAdapter(messagesList);
        mRecyclerView = (RecyclerView) findViewById(R.id.messages_list);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.message_swipe_layout);
        mRecyclerView.setHasFixedSize(true);
        LinearLayout = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(LinearLayout);
        mRecyclerView.setAdapter(mAdapter);
        mRoot.child("Chat").child(mCurrentUserId).child(mAnotherUser).child("seen").setValue(true);
        loadMessages();
        mName.setText(userName);
        mRoot.child("Users").child(mAnotherUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("thumb_pic").getValue().toString();
                if(!image.equals("null")){
                    Picasso.get().load(image).into(mProfileImage);
                }
                if(online.equals("true")) {
                    mLastSeenView.setText("Online");
                } else {
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(online);
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                    mLastSeenView.setText(lastSeenTime);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mRoot.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(mAnotherUser)){
                    Map ChatAdd = new HashMap();
                    ChatAdd.put("seen", false);
                    ChatAdd.put("timestamp", ServerValue.TIMESTAMP);
                    Map ChatUser = new HashMap();
                    ChatUser.put("Chat/" + mCurrentUserId + "/" + mAnotherUser, ChatAdd);
                    ChatUser.put("Chat/" + mAnotherUser + "/" + mCurrentUserId, ChatAdd);

                    mRoot.updateChildren(ChatUser, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                itemPos = 0;
                loadMoreMessages();
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
    private void loadMoreMessages() {
        DatabaseReference messageRef = mRoot.child("messages").child(mCurrentUserId).child(mAnotherUser);
        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();
                if(!mPrevKey.equals(messageKey)){
                    messagesList.add(itemPos++, message);
                } else {
                    mPrevKey = mLastKey;
                }
                if(itemPos == 1) {
                    mLastKey = messageKey;
                }
                mAdapter.notifyDataSetChanged();
                mRefreshLayout.setRefreshing(false);
                LinearLayout.scrollToPositionWithOffset(10, 0);
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

    }

    private void loadMessages() {
        DatabaseReference messageRef = mRoot.child("messages").child(mCurrentUserId).child(mAnotherUser);
        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                itemPos++;
                if(itemPos == 1){
                    String messageKey = dataSnapshot.getKey();
                    mLastKey = messageKey;
                    mPrevKey = messageKey;
                }
                messagesList.add(message);
                mAdapter.notifyDataSetChanged();
                mRecyclerView.scrollToPosition(messagesList.size() - 1);
                mRefreshLayout.setRefreshing(false);
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
    }
    private void sendMessage() {
        String message = mChatMessageView.getText().toString();
        if(!TextUtils.isEmpty(message)){
            String current_user_ref = "messages/" + mCurrentUserId + "/" + mAnotherUser;
            String chat_user_ref = "messages/" + mAnotherUser + "/" + mCurrentUserId;
            DatabaseReference user_message_push = mRoot.child("messages")
                    .child(mCurrentUserId).child(mAnotherUser).push();
            String push_id = user_message_push.getKey();
            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);
            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);
            mChatMessageView.setText("");
            mRoot.child("Chat").child(mCurrentUserId).child(mAnotherUser).child("seen").setValue(true);
            mRoot.child("Chat").child(mCurrentUserId).child(mAnotherUser).child("timestamp").setValue(ServerValue.TIMESTAMP);
            mRoot.child("Chat").child(mAnotherUser).child(mCurrentUserId).child("seen").setValue(false);
            mRoot.child("Chat").child(mAnotherUser).child(mCurrentUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);
            mRoot.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                }
            });
        }
    }
}
