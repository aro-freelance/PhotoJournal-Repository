package com.aro.picturejournal;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aro.picturejournal.model.Journal;
import com.squareup.picasso.Picasso;


import java.util.List;

public class JournalGridViewAdapter extends ArrayAdapter<Journal> {


    public JournalGridViewAdapter(@NonNull Context context, List<Journal> journalList){
        super(context, 0, journalList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View listItemView = convertView;

        if(listItemView == null){
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.gridview_journal_item, parent, false);
        }

        Journal journal = getItem(position);
        //get views

        ImageView journalImage = listItemView.findViewById(R.id.journal_grid_image);
        TextView journalTitle = listItemView.findViewById(R.id.journal_grid_title);

        //set content from journal to views

        journalTitle.setText(journal.getTitle());

        if(journal.getImageUrl() != null){

            String imageUrl = journal.getImageUrl();

            //picasso image loader source: https://square.github.io/picasso/
            //this is where the image is loaded into the imageview
            Picasso.get().load(imageUrl).placeholder(R.drawable.image_one)
                    .fit()
                    .into(journalImage);
        }

        listItemView.setOnClickListener(view -> {
            String uniqueReferenceString;

            if(journal.getUniqueRefName() != null){
                uniqueReferenceString = journal.getUniqueRefName();
            }
            else{
                uniqueReferenceString = "";
            }



            Intent intent = new Intent(getContext(), WriteJournalActivity.class);
            intent.putExtra("unique_ref", uniqueReferenceString);
            getContext().startActivity(intent);
        });



        return listItemView;
    }


}
