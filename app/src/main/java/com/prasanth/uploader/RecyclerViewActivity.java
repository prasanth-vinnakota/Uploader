package com.prasanth.uploader;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.prasanth.uploader.recyclerview.FileAdapter;

import java.util.ArrayList;

public class RecyclerViewActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view);

        //initialize recycler view
        recyclerView = findViewById(R.id.recyclerView);

        //initialize progress bar
        mProgressBar = findViewById(R.id.recycler_progressbar);

        //set layout params to recycler view
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //create a object fr=or FileAdapter
        FileAdapter fileAdapter = new FileAdapter();

        //set adapter
        recyclerView.setAdapter(fileAdapter);

        DatabaseReference file_db_ref = FirebaseDatabase.getInstance().getReference().child("uploadId").child(getIntent().getStringExtra("id"));

        file_db_ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                //get name
                String name = dataSnapshot.getKey();

                //get url
                String url = dataSnapshot.getValue(String.class);

                if (recyclerView.getAdapter() != null) {
                    ((FileAdapter) recyclerView.getAdapter()).update(name, url);
                    mProgressBar.setVisibility(View.GONE);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
