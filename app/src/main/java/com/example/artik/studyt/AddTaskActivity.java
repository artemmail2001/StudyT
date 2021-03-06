package com.example.artik.studyt;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddTaskActivity extends AppCompatActivity implements OnMapReadyCallback{
    private static final String TAG = "MapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private boolean flag = false;
    private double C1 = 0;
    private double C2 = 0;
    private Toolbar mMapToolbar;
    private Map<Double, Double> map = new HashMap();
    private ImageView mGps;
    private Marker markerName;

    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private EditText mTitle, mText;
    private Spinner mNumberPeopleAdd, mScore;
    private Button mDate, mSave, mTime;
    private DatabaseReference mUsersDatabase, mRoot;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private String user_id;
    private String thumb, n;
    private ProgressDialog mDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_adding_task);
        mMapToolbar = (Toolbar) findViewById(R.id.add_app_bar);
        mDialog = new ProgressDialog(this);
        setSupportActionBar(mMapToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        mGps = (ImageView) findViewById(R.id.ic_gps);
        mTitle = (EditText) findViewById(R.id.title_add);
        mText = (EditText) findViewById(R.id.text_add);
        mNumberPeopleAdd = (Spinner) findViewById(R.id.number_people_add);
        mScore = (Spinner)findViewById(R.id.score_add) ;
        mDate = (Button)findViewById(R.id.button_date);
        user_id = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference("Users").child(user_id);
        mUsersDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String th = dataSnapshot.child("thumb_pic").getValue().toString();
                String N = dataSnapshot.child("name").getValue().toString();
                n = N;
                thumb = th;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mRoot = FirebaseDatabase.getInstance().getReference();

        mDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        AddTaskActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                String date = day + "." + month + "." + year;
                if(month<10 && day<10) {
                    date = "0" + day + ".0" + month + "." + year;
                }else if(month<10 && day>=10){
                    date = day + ".0" + month + "." + year;
                } else if(month>=10 && day<10){
                    date = "0" + day + "." + month + "." + year;
                } else if(month>10 && day>10){
                    date = day + "." + month + "." + year;
                }

                mDate.setText(date);
            }
        };

        mTime = (Button)findViewById(R.id.time_button);
        mTime.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int min = cal.get(Calendar.MINUTE);
                int hour = cal.get(Calendar.HOUR_OF_DAY);

                TimePickerDialog dialog = new TimePickerDialog(
                        AddTaskActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int i, int m) {
                                String t = i + ":" + m;
                                if(i<10 && m<10) {
                                    t = "0" + i + ":0" + m;
                                }else if(i<10 && m>=10){
                                    t = "0" + i + ":" + m;
                                } else if(i>=10 && m<10){
                                    t = i + ":0" + m;
                                } else if(i>10 && m>10){
                                    t = i + ":" + m;
                                }
                                mTime.setText(t);
                            }
                        },
                        hour, min, true);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mSave = (Button)findViewById(R.id.button_save);
        mSave.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 mDialog.setTitle("Сохранение");
                 mDialog.setCanceledOnTouchOutside(false);
                 mDialog.show();
                 int a = 0;
                 mSave.setEnabled(false);
                 Calendar cal = Calendar.getInstance();
                 int year = cal.get(Calendar.YEAR);
                 int month = cal.get(Calendar.MONTH);
                 int day = cal.get(Calendar.DAY_OF_MONTH);
                 month = month + 1;
                 final String name = mTitle.getText().toString();
                 final String text = mText.getText().toString();
                 final String date = mDate.getText().toString();
                 final String score = mScore.getSelectedItem().toString();
                 final String time = mTime.getText().toString();
                 final String number_people = mNumberPeopleAdd.getSelectedItem().toString();
                 if(!date.equals("Выберите дату")) {
                     String dd1 = date.substring(0, 2);
                     int dd = Integer.parseInt(dd1);
                     String mm1 = date.substring(3, 5);
                     int mm = Integer.parseInt(mm1);
                     String yy1 = date.substring(6);
                     int yy = Integer.parseInt(yy1);
                     if ((month > mm && year >= yy) || (day >= dd && month == mm && year == yy)) {
                         a = 1;
                     }
                 }
                 if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(text) && !date.equals("Выберите дату") && !time.equals("Выберите время") && a == 0
                         && !TextUtils.isEmpty(score) && !TextUtils.isEmpty(number_people) && (C1 != 0) && (C2 != 0)) {
                     final int sc = Integer.parseInt(score);
                     final int np = Integer.parseInt(number_people);
                     String current_user_ref = "Issues/" + user_id;
                     DatabaseReference user_issue_push = mRoot.child("Issues")
                             .child(user_id).push();
                     final String push_id = user_issue_push.getKey();
                     final Map issueMap = new HashMap();
                     issueMap.put("title", name);
                     issueMap.put("text", text);
                     issueMap.put("date", date);
                     issueMap.put("time", time);
                     issueMap.put("score", sc);
                     issueMap.put("number_people", np);
                     issueMap.put("number_people_left", np);
                     issueMap.put("latitude", C1);
                     issueMap.put("longitude", C2);
                     issueMap.put("thumb", thumb);
                     issueMap.put("name", n);
                     issueMap.put("uid", user_id);
                     issueMap.put("key", push_id);
                     Map isssueUserMap = new HashMap();
                     isssueUserMap.put(current_user_ref + "/" + push_id, issueMap);
                     mRoot.updateChildren(isssueUserMap, new DatabaseReference.CompletionListener() {
                         @Override
                         public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                             String key_ref = "Keys/";
                             Map keysMap = new HashMap();
                             keysMap.put(key_ref + "/" + push_id, issueMap);
                             mRoot.updateChildren(keysMap, new DatabaseReference.CompletionListener() {
                                 @Override
                                 public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                     for(int i = 1; i<np + 1; i++){
                                         mRoot.child("Participants").child(user_id).child(push_id).child("uid_" + i).setValue("null");
                                     }
                                     mSave.setVisibility(View.INVISIBLE);
                                     mDialog.dismiss();
                                     Intent intent = new Intent(AddTaskActivity.this, MainActivity.class);
                                     startActivity(intent);
                                 }
                             });
                         }
                     });
                 }
                 else{
                     mSave.setEnabled(true);
                     mDialog.dismiss();
                     Toast.makeText(AddTaskActivity.this, "Проверьте правильность введённых данных", Toast.LENGTH_SHORT).show();
                 }
             }
        });
        getLocationPermission();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            init();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(AddTaskActivity.this, MainActivity.class);
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

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(flag){
                    markerName.remove();
                }
                markerName = mMap.addMarker(new MarkerOptions().position(latLng));
                flag = true;
                C1 = latLng.latitude;
                C2 = latLng.longitude;
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

        mapFragment.getMapAsync(AddTaskActivity.this);
    }

    private void getLocationPermission(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
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
