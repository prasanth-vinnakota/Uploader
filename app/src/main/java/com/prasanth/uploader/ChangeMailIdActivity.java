package com.prasanth.uploader;

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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangeMailIdActivity extends AppCompatActivity {

    private EditText mCurrentEmail;
    private EditText mNewEmail;
    private EditText mPassword;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_mail_id_layout);

        //initialize Text View
        mCurrentEmail = findViewById(R.id.current_mail_id);
        mNewEmail = findViewById(R.id.new_mail_id);
        mPassword = findViewById(R.id.mail_password);

        //initialize button
        final Button mChangeEmail = findViewById(R.id.change_mail);

        //initialize progress bae
        mProgressBar = findViewById(R.id.change_maid_id_progressbar);

        mChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check for null
                if (mCurrentEmail.getText().toString().equals("")){

                    //set error
                    mCurrentEmail.setError("required field");

                    return;
                }

                //check for null
                if (mNewEmail.getText().toString().equals("")){

                    //set error
                    mNewEmail.setError("required field");

                    return;
                }

                //check for null
                if (mPassword.getText().toString().equals("")){

                    //set error
                    mPassword.setError("required field");

                    return;
                }

                //show progress bar
                mProgressBar.setVisibility(View.VISIBLE);

                //method call
                changeEmail();
            }
        });


    }

    private void changeEmail() {

        //get credentials of current user
        AuthCredential authCredential = EmailAuthProvider.getCredential(mCurrentEmail.getText().toString(), mPassword.getText().toString());

        //get current user instance
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        //user is not exists
        if (user == null){

            //hide progress bar
            mProgressBar.setVisibility(View.GONE);

            //method call
            showMessage("Unable to change email");

            return;
        }

        //re-authenticate user credentials
        user.reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                //re-authenticate success
                if (task.isSuccessful()){

                    //update email
                    user.updateEmail(mNewEmail.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            //email updated
                            if (task.isSuccessful()) {

                                //root -> users -> 'user' -> 'user id' -> email
                                DatabaseReference user_db_ref = FirebaseDatabase.getInstance().getReference().child("users").child(getIntent().getStringExtra("user")).child(user.getUid()).child("email");

                                //change email in database
                                user_db_ref.setValue(mNewEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()){

                                            //hide progress bar
                                            mProgressBar.setVisibility(View.GONE);

                                            //method call
                                            showMessage("Email Updated Successfully");

                                            //finish current activity
                                            finish();
                                        }
                                        else {

                                            //hide progress bar
                                            mProgressBar.setVisibility(View.GONE);

                                            //method call
                                            showMessage(task.getException().getMessage());
                                        }
                                    }
                                });
                            }
                            //password not updated
                            else {

                                //hide progress bar
                                mProgressBar.setVisibility(View.GONE);

                                //method call
                                showMessage(task.getException().getMessage());
                            }
                        }
                    });
                }
                //re-authenticate failed
                else {

                    //hide progress bar
                    mProgressBar.setVisibility(View.GONE);

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
