package com.prasanth.uploader.recyclerview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.prasanth.uploader.R;

import java.util.ArrayList;

public class FileAdapter extends RecyclerView.Adapter<FileViewHolder> {

    private ArrayList<String> fileArrayList = new ArrayList<>();
    private ArrayList<String> fileUrlList = new ArrayList<>();

    public void update(String name, String url){

        fileArrayList.add(name);
        fileUrlList.add(url);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        //create a view for items
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_view_items, viewGroup, false);

        //return view
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder fileViewHolder, int i) {

        //set text for text view
        fileViewHolder.mFileName.setText(fileArrayList.get(i));

        //set text for button
        fileViewHolder.mFileUrl.setText(fileUrlList.get(i));
    }

    @Override
    public int getItemCount() {
        return fileArrayList.size();
    }
}
