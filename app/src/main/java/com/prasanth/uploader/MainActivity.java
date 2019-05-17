package com.prasanth.uploader;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private ProgressBar mProgressBar;

    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    private FirebaseAuth firebaseAuth;

    private TextView mLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize progress bar
        mProgressBar = findViewById(R.id.main_progressbar);

        //init text view
        mLoading = findViewById(R.id.loading);

        //getting Firebase instance
        firebaseAuth = FirebaseAuth.getInstance();

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                //get current user instance
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if ( user == null){

                    //hide progress bar
                    mProgressBar.setVisibility(View.GONE);

                    //start login activity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));

                    //finish current activity
                    finish();

                    return;
                }

                //get current user id
                final String user_id = user.getUid();

                //create a client database reference
                DatabaseReference client_db_ref = FirebaseDatabase.getInstance().getReference().child("users").child("clients");

                //add single value event listener to database reference
                client_db_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){

                            for (DataSnapshot child : dataSnapshot.getChildren()){

                                if (child.getKey()!=null && child.getKey().equals(user_id)){

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {

                                            //hide progress bar
                                            mProgressBar.setVisibility(View.GONE);

                                            //method call
                                            showMessage("Logging in as client");

                                            //start ClientProfileActivity
                                            startActivity(new Intent(MainActivity.this, ClientProfileActivity.class));

                                            //finish current activity
                                            finish();
                                        }
                                    },2000);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

                //create a client database reference
                DatabaseReference admin_db_ref = FirebaseDatabase.getInstance().getReference().child("users").child("admins");

                //add single value event listener to database reference
                admin_db_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){

                            for (DataSnapshot child : dataSnapshot.getChildren()){

                                if (child.getKey()!=null && child.getKey().equals(user_id)){

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {

                                            //hide progress bar
                                            mProgressBar.setVisibility(View.GONE);

                                            //method call
                                            showMessage("Logging in as admin");

                                            //start ClientProfileActivity
                                            startActivity(new Intent(MainActivity.this, AdminProfileActivity.class));

                                            //finish current activity
                                            finish();
                                        }
                                    },2000);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        };
    }

    public boolean checkInternetConnection() {

        //initialize connectivityManager to get the statuses of connectivity services
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);


        NetworkInfo mobile_data = null;
        NetworkInfo wifi = null;

        //connectivityManager have statuses of connection services
        if (connectivityManager != null) {

            //get the status of mobile data
            mobile_data = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            //get status of wifi
            wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        }

        //mobile data or wifi is connected
        if ((mobile_data != null && mobile_data.isConnected()) || (wifi != null && wifi.isConnected())) {

            //exit
            return true;
        }

        //hide progress bar
        mProgressBar.setVisibility(View.GONE);

        //set text
        mLoading.setText(R.string.nic);

        //method call
        showMessage("No internet connection");

        return false;
    }

    private void showMessage(String message) {

        //show toast message
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {

        super.onStart();

        if (checkInternetConnection()) {
            mLoading.setText(R.string.loading);
            firebaseAuth.addAuthStateListener(firebaseAuthListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        firebaseAuth.removeAuthStateListener(firebaseAuthListener);
    }
}
