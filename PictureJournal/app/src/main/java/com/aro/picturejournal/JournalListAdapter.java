package com.aro.picturejournal;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aro.picturejournal.model.Journal;
import com.aro.picturejournal.util.JournalApi;
import com.squareup.picasso.Picasso;

import java.util.List;

public class JournalListAdapter extends RecyclerView.Adapter<JournalListAdapter.ViewHolder> {
     /*
        this is the adapter used in the vertical list of entries displayed on the JournalListActivity screen.
         */

    private Context context;
    private final List<Journal> journalList;
    private final OnJournalClickListener journalClickListener;

    public JournalListAdapter(Context context, List<Journal> journalList, OnJournalClickListener onJournalClickListener) {
        this.context = context;
        this.journalList = journalList;
        this.journalClickListener = onJournalClickListener;
    }

    @NonNull
    @Override
    public JournalListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.journal_list_recyclerview_row, viewGroup, false);

        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalListAdapter.ViewHolder holder, int position) {

        Journal journal = journalList.get(position);

        //time ago source : https://medium.com/@shaktisinh/time-a-go-in-android-8bad8b171f87
        String timeAgo = (String) DateUtils.getRelativeTimeSpanString(journal.getTimeAdded().getSeconds() *1000);

        holder.titleText.setText(journal.getTitle());
        holder.entryText.setText(journal.getEntry());
        holder.dateAddedText.setText(timeAgo);

        if(journal.getImageUrl() != null){

            String imageUrl = journal.getImageUrl();

            //picasso image loader source: https://square.github.io/picasso/
            //this is where the image is loaded into the imageview
            Picasso.get().load(imageUrl).placeholder(R.drawable.image_one).into(holder.image);

        }
        else{
            holder.image.setVisibility(View.GONE);
        }



    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{



        public TextView titleText;
        public TextView entryText;
        public TextView dateAddedText;
        public ImageView image;
        public String userId;
        public String username;

        OnJournalClickListener onJournalClickListener;


        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);
            JournalApi journalApi = JournalApi.getInstance();
            context = ctx;

            titleText = itemView.findViewById(R.id.journal_list_title);
            entryText = itemView.findViewById(R.id.journal_list_entry);
            dateAddedText = itemView.findViewById(R.id.journal_list_time_stamp);
            image = itemView.findViewById(R.id.journal_list_image);

            this.onJournalClickListener = journalClickListener;
            itemView.setOnClickListener(this);

            userId = journalApi.getUserId();
            username = journalApi.getUsername();


        }

        @Override
        public void onClick(View view) {
            Journal currentJournal = journalList.get(getAdapterPosition());
            onJournalClickListener.onJournalClick(currentJournal);

        }
    }

    public interface OnJournalClickListener {
        void onJournalClick(Journal journal);
    }
}
