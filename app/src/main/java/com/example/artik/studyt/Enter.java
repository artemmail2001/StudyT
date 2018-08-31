package com.example.artik.studyt;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Enter extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button mLoginButton;
    private EditText mEmailText;
    private EditText mPasswordText;
    private TextView mLinkSignUp;
    private ProgressDialog mDialog;
    private Toolbar mEnterToolBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);
        mEnterToolBar = (Toolbar) findViewById(R.id.enter_app_bar);
        setSupportActionBar(mEnterToolBar);
        mDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        mEmailText = (EditText)findViewById(R.id.input_email);
        mPasswordText = (EditText)findViewById(R.id.input_password);
        mLoginButton = (Button)findViewById(R.id.btn_login);
        mLinkSignUp = (TextView)findViewById(R.id.link_signup);
        mLinkSignUp.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Enter.this, Registration.class);
                startActivity(intent);
                finish();
            }
        });
        mLoginButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                mDialog.setTitle("Вход");
                mDialog.setMessage("Пожалуйста, подождите");
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();
                LoginUser();
            }
        });
    }
    public void LoginUser() {
        final String mEmailUser = mEmailText.getText().toString().trim();
        String mPasswordUser = mPasswordText.getText().toString().trim();
        if(mEmailUser.isEmpty()) {
            mEmailText.setError("Email is required");
            mEmailText.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(mEmailUser).matches()) {
            mEmailText.setError("Please enter a valid email");
            mEmailText.requestFocus();
            return;
        }
        if(mPasswordUser.isEmpty()){
            mPasswordText.setError("Password is required");
            mPasswordText.requestFocus();
            return;
        }
        if(mPasswordUser.length()<6) {
            mPasswordText.setError("Password should contain at less 6 simbols");
            mPasswordText.requestFocus();
        }

        mAuth.signInWithEmailAndPassword(mEmailUser, mPasswordUser).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    mDialog.dismiss();
                    Intent intent = new Intent(Enter.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
                else {
                    mDialog.hide();
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
