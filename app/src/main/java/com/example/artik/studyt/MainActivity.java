package com.example.artik.studyt;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth mAuth;
    private DatabaseReference mUserReference;
    private TextView mNameText;
    private CircleImageView mCircleView;
    private FirebaseUser user;
    private static final String TAG = "MainActivity";

    private static final int ERROR_DIALOG_REQUEST = 9001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerLayout = navigationView.getHeaderView(0);
        mNameText = (TextView)headerLayout.findViewById(R.id.name_nav);
        mCircleView = (CircleImageView)headerLayout.findViewById(R.id.circle_image_nav);
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        navigationView.setNavigationItemSelectedListener(this);
        if(user!=null) {
            Fragment fragment = new ProfileFragment();
            setTitle("Профиль");
            if (fragment != null) {
                FragmentTransaction fm = getSupportFragmentManager().beginTransaction();
                fm.replace(R.id.content_main, fragment);
                fm.commit();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(user == null) {
            start();
        }else {
            mUserReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
            mUserReference.child("online").setValue("true");
            mUserReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild("name")) {
                        String name = dataSnapshot.child("name").getValue().toString();
                        mNameText.setText(name);
                    }
                    if(dataSnapshot.hasChild("thumb_pic")){
                        final String image = dataSnapshot.child("thumb_pic").getValue().toString();
                        if(!image.equals("null")) {
                            Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).into(mCircleView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(image).into(mCircleView);
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

    @Override
    public void onStop() {
        super.onStop();
        if (user != null) {
            mUserReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
            mUserReference.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    private void start(){
        Intent startIntent = new Intent(MainActivity.this, Enter.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        } else if(id == R.id.log_out){
            mUserReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
            mUserReference.child("online").setValue(ServerValue.TIMESTAMP);
            FirebaseAuth.getInstance().signOut();
            start();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragment = null;
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            setTitle("Профиль");
            fragment = new ProfileFragment();
        } else if (id == R.id.nav_news) {
            setTitle("События");
            fragment = new NewsFragment();
        } else if (id == R.id.nav_map) {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_add) {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_users) {
            setTitle("Пользователи");
            fragment = new UsersFragment();
        } else if (id == R.id.nav_friends) {
            setTitle("Друзья");
            fragment = new FriendsFragment();
        } else if(id == R.id.nav_requests){
            setTitle("Запросы в друзья");
            fragment = new RequestFragment();
        } else if(id == R.id.nav_chat){
            setTitle("Чат");
            fragment = new ChatFragment();
        } else if(id == R.id.nav_my_tasks){
            setTitle("Мои события");
            fragment = new MyTasksFragment();
        } else if(id == R.id.nav_my_events){
            setTitle("Участие в событиях");
            fragment = new EventsFragment();
        }

        if (fragment != null) {
            FragmentTransaction fm = getSupportFragmentManager().beginTransaction();
            fm.replace(R.id.content_main, fragment);
            fm.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
