package com.prasanth.uploader;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;


import static android.view.View.GONE;

public class ClientProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mProfilePic;

    private TextView mName;
    private TextView mEmail;
    private TextView mTel;
    private TextView mId;
    private TextView mUploaded;

    private ProgressBar mProgressBar;

    private BottomSheetDialog bottomSheetDialog;

    private FirebaseAuth firebaseAuth;

    private Uri file_uri;

    private String id;
    private String fileName;

    private final int PROFILE_PIC_PICK_CODE = 1;
    private final int PERMISSION_REQUEST_CODE = 2;
    private final int FILE_REQUEST_CODE = 2;

    private DatabaseReference user_db_ref;
    private DatabaseReference upload_id_db_ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_profile_layout);

        //initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //initialize image view
        mProfilePic = findViewById(R.id.profile_image);

        //initialize bottom sheet dialog
        bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetDialogView = getLayoutInflater().inflate(R.layout.dialog_layout, null);
        bottomSheetDialog.setContentView(bottomSheetDialogView);

        //initialize image button
        ImageButton mSettings = findViewById(R.id.user_settings);

        //initialize bottom sheet dialog elements
        //View mEditProfile = bottomSheetDialogView.findViewById(R.id.edit_profile);
        View mChangeMailId = bottomSheetDialogView.findViewById(R.id.change_mail_id);
        View mChangePassword = bottomSheetDialogView.findViewById(R.id.change_password);
        View mLogout = bottomSheetDialogView.findViewById(R.id.logout);

        //set on click listeners to image button
        mSettings.setOnClickListener(this);


        //set on click listeners to layout
        //mEditProfile.setOnClickListener(this);
        mChangeMailId.setOnClickListener(this);
        mChangePassword.setOnClickListener(this);
        mLogout.setOnClickListener(this);

        //initialize progress bar
        mProgressBar = findViewById(R.id.profile_progressbar);

        //initialize text view
        mName = findViewById(R.id.user_name);
        mEmail = findViewById(R.id.user_email);
        mTel = findViewById(R.id.user_tel);
        mId = findViewById(R.id.user_id);
        mUploaded = findViewById(R.id.files_uploaded);

        //initialize button
        Button mSelectFile = findViewById(R.id.select_file);
        Button mUploadFile = findViewById(R.id.upload_file);
        Button mGetUploadedFiles = findViewById(R.id.get_uploaded_files);

        //set on click listener to profile pic
        mProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //create intent to select image
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PROFILE_PIC_PICK_CODE);
            }
        });

        //set on click listener to buttons
        mSelectFile.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                //check for permissions
                if (ContextCompat.checkSelfPermission(ClientProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    //method call
                    selectFile();
                } else {

                    //request permission
                    ActivityCompat.requestPermissions(ClientProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                }
            }
        });

        //set on click listener to buttons
        mUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (file_uri != null)

                    //method call
                    uploadFile(file_uri);
                else

                    //method call
                    showMessage("You need to select a file before upload");
            }
        });

        //set on click listener to buttons
        mGetUploadedFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mUploaded.getText().toString().equals("0")) {

                    //method call
                    showMessage("No Files were Uploaded");

                    return;
                }

                //create a intent object to RecyclerViewActivity
                Intent intent = new Intent(ClientProfileActivity.this, RecyclerViewActivity.class);

                //put id to intent
                intent.putExtra("id", id);

                //start RecyclerViewActivity
                startActivity(intent);
            }
        });

        //method call
        getDataFromDatabase();
    }

    private void getDataFromDatabase() {

        if (firebaseAuth.getCurrentUser() == null) {

            //hide progress bar
            mProgressBar.setVisibility(GONE);

            //method call
            showMessage("user not found");

            return;
        }

        //get current user id
        String user_id = firebaseAuth.getCurrentUser().getUid();

        //root -> users -> clients -> 'user_id'
        user_db_ref = FirebaseDatabase.getInstance().getReference().child("users").child("clients").child(user_id);

        //add single value event listener to user_db_ref
        user_db_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {

                    //get snapshot values
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    //check for null
                    if (map == null)
                        return;

                    //get name
                    if (map.get("name") != null)
                        mName.setText(map.get("name").toString());

                    //get email
                    if (map.get("email") != null)
                        mEmail.setText(map.get("email").toString());

                    //get contact number
                    if (map.get("tel") != null)
                        mTel.setText(map.get("tel").toString());

                    if (map.get("profilePicUrl") != null) {

                        //get pic url from database
                        String profilePicUrl = map.get("profilePicUrl").toString();

                        //insert url into ImageView
                        Glide.with(getApplication()).load(profilePicUrl).into(mProfilePic);

                        //hide progress bar
                        mProgressBar.setVisibility(GONE);
                    }

                    //get id
                    if (map.get("id") != null) {

                        //assign id to class variable
                        id = (map.get("id").toString());

                        //root -> uploadId -> id
                        upload_id_db_ref = FirebaseDatabase.getInstance().getReference().child("uploadId").child(id);

                        //add event listener to db ref
                        upload_id_db_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists())
                                    mUploaded.setText(dataSnapshot.getChildrenCount() + "");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        mId.setText(id);

                        //hide progress bar
                        mProgressBar.setVisibility(GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    private void showMessage(String message) {

        //show toast message
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    //opens file manager to select file
    private void selectFile() {

        //create a intent object
        Intent intent = new Intent();

        //set type of intent
        intent.setType("*/*");

        //set action type of intent
        intent.setAction(Intent.ACTION_GET_CONTENT);

        //start  intent
        startActivityForResult(intent, FILE_REQUEST_CODE);

    }

    //upload file to Storage and Url to database
    private void uploadFile(final Uri file_uri) {

        //set progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(this);

        //set title
        progressDialog.setTitle("Uploading file..");

        //set style
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        //set icon
        progressDialog.setIcon(R.mipmap.select_file);

        //set progress
        progressDialog.setProgress(0);

        //show dialog
        progressDialog.show();

        Intent intent = new Intent(ClientProfileActivity.this, ClientProfileActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(ClientProfileActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //create a notification
        final NotificationCompat.Builder notification = new NotificationCompat.Builder(ClientProfileActivity.this)

        //set cancel to true
        .setAutoCancel(true)

        //set icon
        .setSmallIcon(R.mipmap.app_icon)

        //set time
        .setWhen(System.currentTimeMillis())

        //set sound
        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        //set content intent
        .setContentIntent(pendingIntent);

        //notify notification
        final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //show progress bar
        mProgressBar.setVisibility(View.VISIBLE);

        //get path of Storage database
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(id).child(fileName);

        //put file to database
        storageReference.putFile(file_uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        if (taskSnapshot.getMetadata() != null && taskSnapshot.getMetadata().getReference() != null) {

                            //get download url
                            taskSnapshot.getMetadata().getReference().getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {

                                            //create a realtime database reference
                                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();


                                            //get file name without extension
                                            StringTokenizer st = new StringTokenizer(fileName, ".");

                                            fileName = st.nextToken();

                                            //upload url to database
                                            databaseReference.child("uploadId").child(id).child(fileName).setValue(uri.toString())
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            //uploaded successfully
                                                            if (task.isSuccessful()) {

                                                                //set ticker
                                                                notification.setTicker(fileName + "Uploaded Successfully");

                                                                //set title
                                                                notification.setContentTitle(fileName);

                                                                //set text
                                                                notification.setContentText("Uploaded Successfully");

                                                                //notify notification
                                                                manager.notify(92, notification.build());

                                                                //hide progress bar
                                                                mProgressBar.setVisibility(GONE);

                                                                //method call
                                                                showMessage(fileName + " Uploaded Successfully");

                                                                //change files uploaded value
                                                                int uploaded = Integer.parseInt(mUploaded.getText().toString());
                                                                uploaded++;
                                                                mUploaded.setText(uploaded + "");
                                                            }
                                                            //not uploaded
                                                            else {

                                                                //set ticker
                                                                notification.setTicker(fileName + "Not Uploaded");

                                                                //set time
                                                                notification.setWhen(System.currentTimeMillis());

                                                                //set title
                                                                notification.setContentTitle(fileName);

                                                                //set text
                                                                notification.setContentText("Not Uploaded");

                                                                //hide progress bar
                                                                mProgressBar.setVisibility(GONE);

                                                                if (task.getException() != null)
                                                                    //log exception
                                                                    Log.d("Upload Failed ", task.getException().getLocalizedMessage());

                                                                //method call
                                                                showMessage(fileName + " not uploaded");
                                                            }
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            //set ticker
                                            notification.setTicker(fileName + "Not Uploaded");

                                            //set time
                                            notification.setWhen(System.currentTimeMillis());

                                            //set title
                                            notification.setContentTitle(fileName);

                                            //set text
                                            notification.setContentText("Not Uploaded");

                                            //hide progress bar
                                            mProgressBar.setVisibility(GONE);

                                            //log exception
                                            Log.d("Cannot get url", e.getCause().getMessage());

                                            //method call
                                            showMessage("Url fot found");
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        //set ticker
                        notification.setTicker(fileName + "Not Uploaded");

                        //set time
                        notification.setWhen(System.currentTimeMillis());

                        //set title
                        notification.setContentTitle(fileName);

                        //set text
                        notification.setContentText("Not Uploaded");

                        //hide progress bar
                        mProgressBar.setVisibility(GONE);

                        //log exception
                        Log.d("Upload Failed ", e.getCause().toString());

                        //method call
                        showMessage(fileName + " not uploaded");
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                        //get progress
                        int progress = (int) ((taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()) * 100);

                        //set progress
                        progressDialog.setProgress(progress);
                    }
                });
    }

    //this method will called when user select image or selects file
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {

        if (resultCode != RESULT_OK || data == null) {

            //method call
            showMessage("no file selected");

            return;
        }
        switch (requestCode) {
            case PROFILE_PIC_PICK_CODE:

                //show progress bar
                mProgressBar.setVisibility(View.VISIBLE);

                //get url
                Uri uri = data.getData();

                //check for null
                if (uri == null) {

                    //method call
                    showMessage("Unable to change profile pic");

                    return;
                }

                //get storage reference of profile pic
                StorageReference profile_pic_storage_ref = FirebaseStorage.getInstance().getReference().child("profilePic").child(id);

                profile_pic_storage_ref.putFile(uri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                if (taskSnapshot.getMetadata() != null && taskSnapshot.getMetadata().getReference() != null) {

                                    taskSnapshot.getMetadata().getReference().getDownloadUrl()
                                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(final Uri uri) {

                                                    //create a hash map object
                                                    HashMap<String, Object> hashMap = new HashMap<>();

                                                    //add elements to hash map
                                                    hashMap.put("profilePicUrl", uri.toString());

                                                    user_db_ref.updateChildren(hashMap)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()) {

                                                                        //hide progress bar
                                                                        mProgressBar.setVisibility(View.GONE);

                                                                        //method call
                                                                        showMessage("Profile pic updated successfully");

                                                                        //set image view
                                                                        mProfilePic.setImageURI(data.getData());
                                                                    } else {
                                                                        //hide progress bar
                                                                        mProgressBar.setVisibility(View.GONE);

                                                                        //method call
                                                                        showMessage("Unable to change profile pic");
                                                                    }
                                                                }
                                                            });


                                                }
                                            });

                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                //hide progress bar
                                mProgressBar.setVisibility(View.GONE);

                                //method call
                                showMessage("Unable to change profile pic");
                            }
                        });

                break;

            case FILE_REQUEST_CODE:
                //create a alert builder object
                AlertDialog.Builder builder = new AlertDialog.Builder(ClientProfileActivity.this);

                //set title
                builder.setTitle("Selected File");

                //set icon
                builder.setIcon(R.mipmap.select_file);

                //set message
                builder.setMessage(data.getData().getLastPathSegment());

                //create a EditText
                final EditText mFileName = new EditText(this);

                //set input type
                mFileName.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);

                //set hint
                mFileName.setHint("Enter filename.extension");

                //add edit text to builder
                builder.setView(mFileName);

                //set cancelable false
                builder.setCancelable(false);

                //set Button
                builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //get entered name
                        fileName = mFileName.getText().toString();

                        //check for null
                        if (fileName.equals("")) {

                            //method call
                            showMessage("YOU NEED TO ENTER FILE NAME");
                        } else if (fileName.contains("#") || fileName.contains("$") || fileName.contains("[") || fileName.contains("]")) {

                            //method call
                            showMessage("FILE NAME MUST NOT CONTAIN '#', '$', '[', ']' ");
                        } else if (!fileName.contains(".")) {

                            //method call
                            showMessage("Name must contain extension too");
                        } else {

                            //get file path
                            file_uri = data.getData();

                            //dismiss dialog
                            dialog.dismiss();

                            //method call
                            showMessage(fileName + " is ready to upload");
                        }
                    }
                });

                //build and show dialog
                AlertDialog dialog = builder.create();
                dialog.show();

                break;
        }
    }

    //this method will call after requesting permissions
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        //check requested code is 1
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            //method call
            selectFile();
        } else {

            //create a alert builder object
            AlertDialog.Builder builder = new AlertDialog.Builder(ClientProfileActivity.this);

            //set title
            builder.setTitle("Permission Not Granted");

            //set icon
            builder.setIcon(R.mipmap.danger);

            //set message
            builder.setMessage("You need to grant permission to upload files to our database.");

            //build and show dialog
            AlertDialog dialog = builder.create();
            dialog.show();

        }
    }

    @Override
    public void onClick(View v) {

        //get selected layout id
        int selected_id = v.getId();

        switch (selected_id) {

            case R.id.user_settings:

                bottomSheetDialog.show();

                break;

           /* case R.id.edit_profile:

                //method call
                showMessage("Not yet available");

                break;*/

            case R.id.change_mail_id:

                Intent i = new Intent(ClientProfileActivity.this, ChangeMailIdActivity.class);

                i.putExtra("user", "clients");

                i.putExtra("id", id);

                //start ChangeMailIdActivity
                startActivity(i);

                break;

            case R.id.change_password:

                //start ChangePasswordActivity
                startActivity(new Intent(ClientProfileActivity.this, ChangePasswordActivity.class));

                break;

            case R.id.logout:

                //sign out from firebase
                firebaseAuth.signOut();

                //method call
                showMessage("Logging out");

                //start LoginActivity
                startActivity(new Intent(ClientProfileActivity.this, LoginActivity.class));

                //finish current activity
                finish();
                break;
        }
    }
}
