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

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText mCurrentPassword;
    private EditText mNewPassword;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password_layout);

        //initialize Text View
        mCurrentPassword = findViewById(R.id.current_password);
        mNewPassword = findViewById(R.id.new_password);

        //initialize button
        final Button mChangePassword = findViewById(R.id.change_password);

        //initialize progress bae
        mProgressBar = findViewById(R.id.change_password_progressbar);

        mChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                //check for null
                if (mNewPassword.getText().toString().equals("")){
                    
                    //set error
                    mNewPassword.setError("required field");
                    
                    return;
                }

                //check for null
                if (mCurrentPassword.getText().toString().equals("")){

                    //set error
                    mCurrentPassword.setError("required field");

                    return;
                }

                //show progress bar
                mProgressBar.setVisibility(View.VISIBLE);

                //method call
                changePassword();
            }
        });
            

    }

    private void changePassword() {

        //get credentials of current user
        AuthCredential authCredential = EmailAuthProvider.getCredential(FirebaseAuth.getInstance().getCurrentUser().getEmail().toString(), mCurrentPassword.getText().toString());

        //get current user instance
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        //user is not exists
        if (user == null){

            //hide progress bar
            mProgressBar.setVisibility(View.GONE);

            //method call
            showMessage("Unable to change password");

            return;
        }

        //re-authenticate user credentials
            user.reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    //re-authenticate success
                    if (task.isSuccessful()){

                        //update password
                        user.updatePassword(mNewPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                //password updated
                                if (task.isSuccessful()) {

                                    //hide progress bar
                                    mProgressBar.setVisibility(View.GONE);

                                    //method call
                                    showMessage("Password Updated Successfully");

                                    //finish current activity
                                    finish();
                                }
                                //password not updated
                                else {

                                    //hide progress bar
                                    mProgressBar.setVisibility(View.GONE);

                                    //method call
                                    showMessage("Unable to change password");
                                }
                            }
                        });
                    }
                    //re-authenticate failed
                    else {

                        //hide progress bar
                        mProgressBar.setVisibility(View.GONE);

                        //method call
                        showMessage("Unable to change password");
                    }
                }
            });
    }

    private void showMessage(String message) {

        //show toast message
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
