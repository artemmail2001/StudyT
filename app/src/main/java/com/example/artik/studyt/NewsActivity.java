package com.example.artik.studyt;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewsActivity extends AppCompatActivity implements OnMapReadyCallback{
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mLocationPermissionsGranted) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            init();
        }
    }

    private static final String TAG = "NewsActivity";

    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private Toolbar mMapToolbar;
    private ImageView mGps;

    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private DatabaseReference mKeysDatabase;
    private TextView mName, mDate, mScore, mPeople, mTitle, mText;
    private CircleImageView mCircle;
    private Button mJoin;
    private DatabaseReference mRoot;
    private String uid;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        final String key = getIntent().getStringExtra("key");
        mGps = (ImageView) findViewById(R.id.ic_gps);
        mMapToolbar = (Toolbar) findViewById(R.id.news_act_app_bar);
        mDialog = new ProgressDialog(this);
        setSupportActionBar(mMapToolbar);
        mName = (TextView)findViewById(R.id.name_news_act);
        mDate = (TextView)findViewById(R.id.date_news_act);
        mScore = (TextView)findViewById(R.id.score_news_act);
        mPeople = (TextView)findViewById(R.id.number_people_news_act);
        mTitle = (TextView)findViewById(R.id.title_news_act);
        mText = (TextView)findViewById(R.id.text_news_act);
        mCircle = (CircleImageView)findViewById(R.id.circle_news_act);
        mJoin = (Button)findViewById(R.id.button_join);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mKeysDatabase = FirebaseDatabase.getInstance().getReference("Keys").child(key);
        mRoot = FirebaseDatabase.getInstance().getReference();
        mRoot.child("Events").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    if(snapshot.getKey().equals(key)){
                        mJoin.setText("Выйти из события");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mKeysDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String date = dataSnapshot.child("date").getValue().toString();
                String image = dataSnapshot.child("thumb").getValue().toString();
                String name = dataSnapshot.child("name").getValue().toString();
                String score = dataSnapshot.child("score").getValue().toString();
                String number_people_left = dataSnapshot.child("number_people_left").getValue().toString();
                String title = dataSnapshot.child("title").getValue().toString();
                String text = dataSnapshot.child("text").getValue().toString();
                String time = dataSnapshot.child("time").getValue().toString();
                final String key_uid = dataSnapshot.child("uid").getValue().toString();
                if(key_uid.equals(uid)) {
                    mJoin.setEnabled(false);
                    mJoin.setVisibility(View.INVISIBLE);
                }
                if(number_people_left.equals("0") && mJoin.getText().equals("Присоединиться")){
                    mJoin.setEnabled(false);
                }
                mDate.setText(time + "  " + date);
                if(!image.equals("null")){
                    Picasso.get().load(image).into(mCircle);
                }
                mName.setText(name);
                mScore.setText(score);
                mText.setText(text);
                mTitle.setText(title);
                mPeople.setText("осталось " + number_people_left + " мест(-а)");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mJoin.setEnabled(false);
                mDialog.setTitle("Сохранение");
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();
                if(mJoin.getText().equals("Присоединиться")) {
                    mKeysDatabase.runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(final MutableData mutableData) {
                            final Issue issue = mutableData.getValue(Issue.class);
                            if (issue == null) {
                                return Transaction.success(mutableData);
                            }
                            final int a = issue.number_people;
                            final int sc = issue.getScore();
                            final int num = issue.number_people_left-1;
                            mRoot.child("Participants").child(issue.getUid()).child(issue.getKey()).runTransaction(new Transaction.Handler() {
                                @Override
                                public Transaction.Result doTransaction(MutableData mutableData1) {
                                    for(int i = 1; i<=a; i++) {
                                        Log.d("lolita", "Началось");
                                        if (mutableData1.child("uid_" + i).getValue().toString().equals("null")){
                                            mutableData1.child("uid_" + i).setValue(uid);
                                            mRoot.child("Events").child(uid).child(issue.getKey()).child("position").setValue("uid_" + i).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(NewsActivity.this, "Сохранено", Toast.LENGTH_SHORT).show();
                                                    mKeysDatabase.child("number_people_left").setValue(num);
                                                    mRoot.child("Issues").child(issue.getUid()).child(issue.getKey()).setValue(issue);
                                                    mRoot.child("Users").child(uid).runTransaction(new Transaction.Handler() {
                                                        @Override
                                                        public Transaction.Result doTransaction(MutableData mutableData2) {
                                                            User user = mutableData2.getValue(User.class);
                                                            if (user == null) {
                                                                return Transaction.success(mutableData2);
                                                            }
                                                            int a = user.getScore() + sc;
                                                            mRoot.child("Users").child(uid).child("score").setValue(a);
                                                            return Transaction.success(mutableData2);
                                                        }

                                                        @Override
                                                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                                        }
                                                    });
                                                }
                                            });
                                            return Transaction.success(mutableData1);
                                        }
                                        else{
                                            continue;
                                        }
                                    }
                                    return Transaction.abort();
                                }

                                @Override
                                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                }
                            });
                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                            mJoin.setText("Выйти из события");
                            mJoin.setEnabled(true);
                            mDialog.dismiss();
                        }
                    });
                }
                else if(mJoin.getText().toString().equals("Выйти из события")){
                    mKeysDatabase.runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(final MutableData mutableData) {
                            final Issue issue = mutableData.getValue(Issue.class);
                            if (issue == null) {
                                return Transaction.success(mutableData);
                            }
                            final int a = issue.number_people;
                            final int sc = issue.getScore();
                            final int num = issue.number_people_left + 1;
                            mRoot.child("Participants").child(issue.getUid()).child(issue.getKey()).runTransaction(new Transaction.Handler() {
                                @Override
                                public Transaction.Result doTransaction(MutableData mutableData1) {
                                    for(int i = 1; i<=a; i++) {
                                        if (mutableData1.child("uid_" + i).getValue().toString().equals(uid)){
                                            mutableData1.child("uid_" + i).setValue("null");
                                            mRoot.child("Events").child(uid).child(issue.getKey()).child("position").removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(NewsActivity.this, "Сохранено", Toast.LENGTH_SHORT).show();
                                                    issue.number_people_left = issue.number_people_left + 1;
                                                    mKeysDatabase.child("number_people_left").setValue(num);
                                                    mRoot.child("Issues").child(issue.getUid()).child(issue.getKey()).setValue(issue);
                                                    mRoot.child("Users").child(uid).runTransaction(new Transaction.Handler() {
                                                        @Override
                                                        public Transaction.Result doTransaction(MutableData mutableData2) {
                                                            User user = mutableData2.getValue(User.class);
                                                            if (user == null) {
                                                                return Transaction.success(mutableData2);
                                                            }
                                                            int a = user.getScore() - sc;
                                                            mRoot.child("Users").child(uid).child("score").setValue(a);
                                                            return Transaction.success(mutableData2);
                                                        }

                                                        @Override
                                                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                                        }
                                                    });
                                                }
                                            });
                                            return Transaction.success(mutableData1);
                                        }
                                        else{
                                            continue;
                                        }
                                    }
                                    return Transaction.abort();
                                }

                                @Override
                                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                }
                            });
                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                            mJoin.setText("Присоединиться");
                            mJoin.setEnabled(true);
                            mDialog.dismiss();
                        }
                    });
                }

            }
        });
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        getLocationPermission();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(NewsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void init(){
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeviceLocation();
            }
        });
        mKeysDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String C1 = dataSnapshot.child("latitude").getValue().toString();
                String C2 = dataSnapshot.child("longitude").getValue().toString();
                double L1 = Double.parseDouble(C1);
                double L2 = Double.parseDouble(C2);
                LatLng latLng = new LatLng(L1, L2);
                moveCamera(latLng, 15f, "Title");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        hideSoftKeyboard();
    }

    private void getDeviceLocation(){
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted){

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Location currentLocation = (Location) task.getResult();

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM,
                                    "My Location");

                        }else{
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }
    private void moveCamera(LatLng latLng, float zoom, String title){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if(!title.equals("My Location")){
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }

        hideSoftKeyboard();
    }

    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(NewsActivity.this);
    }

    private void getLocationPermission(){
        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionsGranted = true;
                    initMap();
                }
            }
        }
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}
