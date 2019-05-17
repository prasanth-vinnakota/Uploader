package com.prasanth.uploader.recyclerview;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.prasanth.uploader.R;

public class FileViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener {

    TextView mFileName;
    TextView mFileUrl;

    FileViewHolder(@NonNull View itemView) {
        super(itemView);

        //initialize TextView
        mFileName = itemView.findViewById(R.id.file_name);
        mFileUrl = itemView.findViewById(R.id.file_url);

        //set on click listener to view
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {

        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        Log.d("Context", v.getContext().toString());

        builder.setTitle("Select one");

        builder.setPositiveButton("copy url", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //copy url to clip board
                ClipboardManager clipboardManager = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("url", mFileUrl.getText());
                clipboardManager.setPrimaryClip(clipData);

                Toast.makeText(v.getContext(), "url copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("open url", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //open url
                Intent intent = new Intent();
                intent.setDataAndType(Uri.parse(mFileUrl.getText().toString()), Intent.ACTION_VIEW);
                v.getContext().startActivity(intent);

            }
        });

        //build and show dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
