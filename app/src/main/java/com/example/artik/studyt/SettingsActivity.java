package com.example.artik.studyt;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText mName, mS1, mS2;
    private DatabaseReference mUserDatabase;
    private CircleImageView mCircle;
    private Button mChangeAva, mChangeName, mS1Change, mS2Change;
    private static final int Pick_image = 1;
    private StorageReference mImageStor;
    private ProgressDialog mProgressDialog;
    private FirebaseUser mUser;
    private Toolbar mSettingsToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mSettingsToolbar = (Toolbar) findViewById(R.id.settings_app_bar);
        setSupportActionBar(mSettingsToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        mProgressDialog = new ProgressDialog(this);
        mName = (EditText)findViewById(R.id.name_ch);
        mS1 = (EditText)findViewById(R.id.s1_ch);
        mS2 = (EditText)findViewById(R.id.s2_ch);
        mCircle = (CircleImageView)findViewById(R.id.circle_ch);
        mChangeAva = (Button)findViewById(R.id.change_circle);
        mChangeName = (Button)findViewById(R.id.change_name);
        mS1Change = (Button)findViewById(R.id.change_s1);
        mS2Change = (Button)findViewById(R.id.change_s2);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mImageStor = FirebaseStorage.getInstance().getReference();
        final String uid = mUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        mUserDatabase.keepSynced(true);
        mChangeAva.setOnClickListener(this);
        mChangeName.setOnClickListener(this);
        mS1Change.setOnClickListener(this);
        mS2Change.setOnClickListener(this);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String subject1 = dataSnapshot.child("subject1").getValue().toString();
                String subject2 = dataSnapshot.child("subject2").getValue().toString();
                if(!subject1.equals("пусто")) {
                    mS1.setText(subject1);
                }
                if(!subject2.equals("пусто")) {
                    mS2.setText(subject2);
                }
                if(dataSnapshot.hasChild("name")) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    mName.setText(name);
                }
                if(dataSnapshot.hasChild("thumb_pic")){
                    final String image = dataSnapshot.child("thumb_pic").getValue().toString();
                    if(!image.equals("null")) {
                        Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).into(mCircle, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(image).into(mCircle);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.change_circle:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "SELECT IMAGE"), Pick_image);
                break;
            case R.id.change_name:
                String name = mName.getText().toString();
                mUserDatabase.child("name").setValue(name);
                break;
            case R.id.change_s1:
                String s1 = mS1.getText().toString();
                mUserDatabase.child("subject1").setValue(s1);
                break;
            case R.id.change_s2:
                String s2 = mS2.getText().toString();
                mUserDatabase.child("subject2").setValue(s2);
                break;
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Pick_image && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri).setAspectRatio(2, 2).start(this);
        }
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK) {
                mProgressDialog.setTitle("Сохранение изображения");
                mProgressDialog.setMessage("Пожалуйста, подождите");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();
                Uri uri_uri = result.getUri();
                File thumb_pic_path = new File(uri_uri.getPath());
                String id = mUser.getUid();
                StorageReference file = mImageStor.child("images/" + id + ".jpg");
                try {
                    Bitmap bitmap = new Compressor(this).setMaxHeight(100).setMaxWidth(100).setQuality(75).compressToBitmap(thumb_pic_path);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    final byte[] bytes = baos.toByteArray();
                    final StorageReference refer = mImageStor.child("thumb_images/" + id + ".jpg");
                    file.putFile(uri_uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()) {
                                final String download = task.getResult().getDownloadUrl().toString();
                                UploadTask uploadTask = refer.putBytes(bytes);
                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task_t) {
                                        if(task_t.isSuccessful()){
                                            String download1 = task_t.getResult().getDownloadUrl().toString();
                                            Map map = new HashMap();
                                            map.put("picture", download);
                                            map.put("thumb_pic", download1);
                                            mUserDatabase.updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task t) {
                                                    if(t.isSuccessful()) {
                                                        mProgressDialog.dismiss();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
