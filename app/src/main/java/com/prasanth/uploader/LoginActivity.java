package com.prasanth.uploader;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPassword;

    private final String[] user_type = new String[]{"Client Login", "Admin Login"};

    private String user_type_string = user_type[0];

    FirebaseAuth firebaseAuth;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        //initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //initialize EditText
        mEmail = findViewById(R.id.login_email);
        mPassword = findViewById(R.id.login_password);

        //initialize Spinner
        Spinner mUserType = findViewById(R.id.user_type);

        //initialize Button
        Button mLogin = findViewById(R.id.login_user);
        Button mForgetPassword = findViewById(R.id.forget_password);
        Button mRegister = findViewById(R.id.new_user);

        //initialize ProgressBar
        mProgressBar = findViewById(R.id.login_progressbar);

        //set adapter
        ArrayAdapter<String> userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, user_type);
        userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //add adapter to spinner
        mUserType.setAdapter(userAdapter);

        //set OnItemSelectListener to spinner
        mUserType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                //assign selected type to string
                user_type_string = user_type[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //start RegisterActivity
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));

                //finish current activity
                finish();
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check for null
                if (mEmail.getText().toString().equals("")) {

                    //set error
                    mEmail.setError("required field");

                    return;
                }

                //check for null
                if (mPassword.getText().toString().equals("")) {

                    //set error
                    mPassword.setError("required field");

                    return;
                }

                //show progress bar
                mProgressBar.setVisibility(View.VISIBLE);

                //method call
                login();
            }
        });

        mForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check for null
                if (mEmail.getText().toString().equals("")) {

                    //set error
                    mEmail.setError("required field");

                    return;
                }

                //show progress bar
                mProgressBar.setVisibility(View.VISIBLE);

                //method call
                forgetPassword();
            }
        });
    }

    private void forgetPassword() {

        firebaseAuth.sendPasswordResetEmail(mEmail.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            //hide progress bar
                            mProgressBar.setVisibility(View.GONE);

                            //method call
                            showMessage("Password reset email sent");
                        }
                        else {

                            //hide progress bar
                            mProgressBar.setVisibility(View.GONE);

                            //check for null
                            if (task.getException() != null)

                                //method call
                                showMessage(task.getException().getMessage());
                        }
                    }
                });
    }

    private void login() {

        firebaseAuth.signInWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            FirebaseUser user = firebaseAuth.getCurrentUser();

                            //user is client
                            if (user_type_string.equals(user_type[0])) {

                                if (user == null){

                                    //hide progress bar
                                    mProgressBar.setVisibility(View.GONE);

                                    //method call
                                    showMessage("no user found");

                                    return;
                                }

                                // root -> users -> clients -> 'user id'
                                DatabaseReference client_db_ref = FirebaseDatabase.getInstance().getReference().child("users").child("clients").child(user.getUid());

                                //add single value event listener
                                client_db_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()){

                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {

                                                    //create a intent object to ProfileActivity
                                                    Intent intent = new Intent(LoginActivity.this, ClientProfileActivity.class);

                                                    //hide progress bar
                                                    mProgressBar.setVisibility(View.GONE);

                                                    //method call
                                                    showMessage("Logging in as client");

                                                    //start activity
                                                    startActivity(intent);

                                                    //finish current activity
                                                    finish();
                                                }
                                            }, 2000);
                                        }
                                        else {

                                            //hide progress bar
                                            mProgressBar.setVisibility(View.GONE);

                                            //method call
                                            showMessage("This user is not a client");

                                            firebaseAuth.signOut();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });
                            }
                            //user is admin
                            else {

                                if (user == null){

                                    //hide progress bar
                                    mProgressBar.setVisibility(View.GONE);

                                    //method call
                                    showMessage("no user found");

                                    return;
                                }

                                // root -> users -> admins -> 'user id'
                                DatabaseReference admin_db_ref = FirebaseDatabase.getInstance().getReference().child("users").child("admins").child(user.getUid());

                                //add single value event listener
                                admin_db_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()){

                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {

                                                    //create a intent object to ProfileActivity
                                                    Intent intent = new Intent(LoginActivity.this, AdminProfileActivity.class);

                                                    //hide progress bar
                                                    mProgressBar.setVisibility(View.GONE);

                                                    //method call
                                                    showMessage("Logging in as admin");

                                                    //start activity
                                                    startActivity(intent);

                                                    //finish current activity
                                                    finish();
                                                }
                                            }, 2000);
                                        }
                                        else {

                                            //hide progress bar
                                            mProgressBar.setVisibility(View.GONE);

                                            //method call
                                            showMessage("This user is not a admin");

                                            firebaseAuth.signOut();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });
                            }
                        } else {

                            //hide progress bar
                            mProgressBar.setVisibility(View.GONE);

                            //check for null
                            if (task.getException() != null)

                                //method call
                                showMessage(task.getException().getMessage());
                        }
                    }
                });
    }

    private void showMessage(String message) {

        //show toast message
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}
