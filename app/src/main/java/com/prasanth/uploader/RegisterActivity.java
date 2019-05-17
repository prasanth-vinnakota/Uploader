package com.prasanth.uploader;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText mName;
    private EditText mEmail;
    private EditText mTel;
    private EditText mPassword;
    private EditText mReEnterPassword;

    private ProgressBar mProgressBar;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);

        //initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //initialize edit text
        mName = findViewById(R.id.registered_name);
        mEmail = findViewById(R.id.registered_email);
        mTel = findViewById(R.id.registered_contact_no);
        mPassword = findViewById(R.id.registered_password);
        mReEnterPassword = findViewById(R.id.registered_re_enter_password);

        //initialize progress bar
        mProgressBar = findViewById(R.id.register_progressbar);

        //initialize button
        Button mLogin = findViewById(R.id.login);
        Button mRegister = findViewById(R.id.add_user);

        //set on click listener to button
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //start LoginActivity
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));

                //finish current activity
                finish();
            }
        });

        //set on click listener to button
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check for null
                if (mName.getText().toString().equals("")) {

                    //set error
                    mName.setError("required field");

                    return;
                }
                //check for null
                if (mEmail.getText().toString().equals("")) {

                    //set error
                    mEmail.setError("required field");

                    return;
                }
                //check for null
                if (mTel.getText().toString().equals("")) {

                    //set error
                    mTel.setError("required field");

                    return;
                }
                //check for null
                if (mPassword.getText().toString().equals("")) {

                    //set error
                    mPassword.setError("required field");

                    return;
                }
                //check for null
                if (mReEnterPassword.getText().toString().equals("")) {

                    //set error
                    mReEnterPassword.setError("required field");

                    return;
                }

                if (!(mReEnterPassword.getText().toString().equals(mPassword.getText().toString()))) {

                    //set error
                    mPassword.setError("password mismatch");

                    //set error
                    mReEnterPassword.setError("password mismatch");

                    return;
                }

                //show progress bar
                mProgressBar.setVisibility(View.VISIBLE);

                //method call
                register();
            }
        });
    }

    private void register() {

        final int[] id = {0};

        // root -> id
        final DatabaseReference id_db_ref = FirebaseDatabase.getInstance().getReference().child("id");

        id_db_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists())
                    id[0] = (int) dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        firebaseAuth.createUserWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            if (firebaseAuth.getCurrentUser() == null) {

                                //method call
                                showMessage("User not found");

                                return;
                            }

                            //get current user id
                            String user_id = firebaseAuth.getCurrentUser().getUid();

                            //add id to database
                            id_db_ref.child((id[0]+1)+"").setValue(mName.getText().toString());

                            // root -> uploadId -> 'id'
                            DatabaseReference upload_db_ref = FirebaseDatabase.getInstance().getReference().child("uploadId").child(id[0]+"");

                            //add value to db ref
                            upload_db_ref.setValue(true);

                            //root -> users -> clients -> 'user_id'
                            DatabaseReference user_db_ref = FirebaseDatabase.getInstance().getReference().child("users").child("clients").child(user_id);

                            //create a hash map object
                            HashMap<String, Object> hashMap = new HashMap<>();

                            //add elements to hash map
                            hashMap.put("name", mName.getText().toString());
                            hashMap.put("email", mEmail.getText().toString());
                            hashMap.put("id", (id[0]+1));
                            hashMap.put("tel", mTel.getText().toString());

                            //update db ref children
                            user_db_ref.updateChildren(hashMap);

                            //create a intent object to ProfileActivity
                            Intent intent = new Intent(RegisterActivity.this, ClientProfileActivity.class);

                            //hide progress bar
                            mProgressBar.setVisibility(View.GONE);

                            //method call
                            showMessage("User registered id is " + (id[0]+1));

                            //start activity
                            startActivity(intent);

                            //finish current activity
                            finish();
                        }
                    }
                });
    }

    private void showMessage(String message) {

        //show toast message
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
