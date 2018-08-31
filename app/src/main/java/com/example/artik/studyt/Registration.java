package com.example.artik.studyt;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Registration extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText mEmailUser;
    private EditText mPasswordUser;
    private Button mRegistration;
    private TextView mLinkEnter;
    private ProgressDialog mDialog;
    private EditText mName;
    private DatabaseReference mDatabase, mFriendsDatabase;
    private Toolbar mRegistrationToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        mRegistrationToolBar = (Toolbar) findViewById(R.id.registration_app_bar);
        setSupportActionBar(mRegistrationToolBar);
        mName = (EditText) findViewById(R.id.input_name);
        mDialog = new ProgressDialog(this);
        mRegistration = (Button) findViewById(R.id.btn_signup);
        mAuth = FirebaseAuth.getInstance();
        mEmailUser = (EditText) findViewById(R.id.input_email_r);
        mPasswordUser = (EditText) findViewById(R.id.input_password_r);
        mLinkEnter = (TextView) findViewById(R.id.link_login);
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference("Friends");
        mLinkEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Registration.this, Enter.class);
                startActivity(intent);
                finish();
            }
        });

        mRegistration.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mDialog.setTitle("Регистрация");
                mDialog.setMessage("Пожалуйста, подождите");
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();
                register();
            }
        });
    }

    public void register() {
        String mEmail = mEmailUser.getText().toString().trim();
        String mPassword = mPasswordUser.getText().toString().trim();
        final String Name = mName.getText().toString();
        if (mEmail.isEmpty()) {
            mEmailUser.setError("Email is required");
            mEmailUser.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(mEmail).matches()) {
            mEmailUser.setError("Please enter a valid email");
            mEmailUser.requestFocus();
            return;
        }
        if (mPassword.isEmpty()) {
            mPasswordUser.setError("Password is required");
            mPasswordUser.requestFocus();
            return;
        }
        if (mPassword.length() < 6) {
            mPasswordUser.setError("Password should contain at less 6 simbols");
            mPasswordUser.requestFocus();
        }
        if (Name.isEmpty()) {
            mName.setError("Пожалуйста, введите ваше имя");
            mName.requestFocus();
        }

        mAuth.createUserWithEmailAndPassword(mEmail, mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    final String uid = currentUser.getUid();
                    mDatabase = FirebaseDatabase.getInstance().getReference("Users").child(uid);
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", Name);
                    map.put("uid", uid);
                    map.put("score", 0);
                    map.put("thumb_pic", "null");
                    map.put("picture", "null");
                    map.put("subject1", "пусто");
                    map.put("subject2", "пусто");
                    mDatabase.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mFriendsDatabase.child(uid).child("lbjov7MlVaM99fY9HwwbmZnxv183").child("uidd").setValue("lbjov7MlVaM99fY9HwwbmZnxv183").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mFriendsDatabase.child("lbjov7MlVaM99fY9HwwbmZnxv183").child(uid).child("uidd").setValue(uid).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mDialog.dismiss();
                                                Toast.makeText(Registration.this, "Registration succesfull", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(Registration.this, MainActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
                } else {
                    mDialog.hide();
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(getApplicationContext(), "You are already registered", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


    }
}
