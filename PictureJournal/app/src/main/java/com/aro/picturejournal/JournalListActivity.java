package com.aro.picturejournal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.aro.picturejournal.model.Journal;
import com.aro.picturejournal.util.JournalApi;
import com.aro.picturejournal.util.Utils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class JournalListActivity extends AppCompatActivity implements JournalListAdapter.OnJournalClickListener {


    /*
    this activity shows the journals to the user in either list or grid.
     */

    private EditText searchBarEditText;
    private TextView currentTagText;
    private ImageButton searchButton;
    private CardView searchCardView;
    private CardView currentTagCardView;
    private ImageView currentTagCancelImage;


    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private List<Journal> journalList;

    private RecyclerView recyclerView;
    private JournalListAdapter journalListAdapter;

    private GridView gridView;

    private final CollectionReference journalCollection = db.collection("Journal");
    private TextView noJournalEntry;

    private boolean isGallery = false;

    private AdView mAdviewList;
    private AdView mAdviewGallery;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_list);

        searchBarEditText = findViewById(R.id.search_bar_edit_text);
        currentTagText = findViewById(R.id.current_tag_text);
        searchButton = findViewById(R.id.search_image_button);
        searchCardView = findViewById(R.id.search_bar_cardview);
        currentTagCardView = findViewById(R.id.current_tag_cardview);
        currentTagCancelImage = findViewById(R.id.exit_x_image_tag_cardview);

        noJournalEntry = findViewById(R.id.no_journals_textview);
        recyclerView = findViewById(R.id.recyclerview_journal_list);

        mAdviewList = findViewById(R.id.adView_list);

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdviewList.loadAd(adRequest);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        journalList = new ArrayList<>();
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();



        searchButton.setOnClickListener(this::searchButtonMethod);
        searchCardView.setOnClickListener(this::searchButtonMethod);
        currentTagText.setOnClickListener(this::currentTagRemoveMethod);
        currentTagCardView.setOnClickListener(this::currentTagRemoveMethod);



    }

    private void sortByDate(List<Journal> list ){
        list.sort(Comparator.comparing(Journal::getTimeAdded).reversed());
    }

    private void currentTagRemoveMethod(View view) {
        removeFilters();
    }

    private void removeFilters() {
        currentTagText.setText("");
        currentTagText.setVisibility(View.INVISIBLE);
        currentTagCardView.setVisibility(View.INVISIBLE);
        currentTagCancelImage.setVisibility(View.INVISIBLE);
        noJournalEntry.setVisibility(View.INVISIBLE);

        if(journalList.size() > 0){

            sortByDate(journalList);
            if(isGallery){
                //gridview adapter
                JournalGridViewAdapter gridAdapter = new JournalGridViewAdapter(this, journalList);

                gridView.setAdapter(gridAdapter);
                gridView.setNumColumns(3);


            }
            else{
                //set up the recyclerview with the list
                journalListAdapter = new JournalListAdapter(JournalListActivity.this, journalList, this);
                recyclerView.setAdapter(journalListAdapter);
            }

        }
        //if the data is empty
        else{
            if(isGallery){
                //gridview adapter
                JournalGridViewAdapter gridAdapter = new JournalGridViewAdapter(this, journalList);

                gridView.setAdapter(gridAdapter);
                gridView.setNumColumns(3);


            }
            else{
                //set up the recyclerview with the list
                journalListAdapter = new JournalListAdapter(JournalListActivity.this, journalList, this);
                recyclerView.setAdapter(journalListAdapter);
            }
            //there is no data so prompt user with how to add a journal
            noJournalEntry.setText(R.string.no_journal_entries_prompt);
            noJournalEntry.setVisibility(View.VISIBLE);
        }

    }

    private void searchButtonMethod(View view) {
        filterForTag();
    }

    //update the list to a list of only the journals with the tag user entered. and then update the screen using the adapter
    private void filterForTag() {

        List<Journal> filteredList = new ArrayList<>();
        noJournalEntry.setVisibility(View.INVISIBLE);

        //if the user input a tag
        if(!TextUtils.isEmpty( searchBarEditText.getText().toString().trim() )){
            //get user input
            String userInputTag = searchBarEditText.getText().toString().trim();

            currentTagText.setText(userInputTag.toUpperCase());
            currentTagText.setVisibility(View.VISIBLE);
            currentTagCardView.setVisibility(View.VISIBLE);
            currentTagCancelImage.setVisibility(View.VISIBLE);


            //for each journal
            for (int i = 0; i < journalList.size(); i++) {
                Journal journal = journalList.get(i);

                //if the journal has tags
                if (journal.getTags() != null) {
                    List<String> tags = journal.getTags();

                    //if the tags match the user input add them to the filtered list
                    if (tags.contains(userInputTag.toUpperCase())) {
                        filteredList.add(journal);
                    }
                }
            }

            //after the filtered list is populated

            //if filtered list is not empty display it to the screen
            if(filteredList.size() > 0){
                //sort the list by time added
                sortByDate(filteredList);

                if(isGallery){
                    //gridview adapter
                    JournalGridViewAdapter gridAdapter = new JournalGridViewAdapter(this, filteredList);

                    gridView.setAdapter(gridAdapter);
                    gridView.setNumColumns(3);


                }
                else{
                    //set up the recyclerview with the list
                    journalListAdapter = new JournalListAdapter(JournalListActivity.this, filteredList, this);
                    recyclerView.setAdapter(journalListAdapter);
                }



            }
            //if filtered list is empty display the textview to let the user know there are no results for that tag
            else{

                noJournalEntry.setText(String.format("%s%s", getString(R.string.no_results_tag_search), userInputTag));
                noJournalEntry.setVisibility(View.VISIBLE);

                if(isGallery){
                    //gridview adapter
                    JournalGridViewAdapter gridAdapter = new JournalGridViewAdapter(this, filteredList);

                    gridView.setAdapter(gridAdapter);
                    gridView.setNumColumns(3);


                }
                else{
                    //set up the recyclerview with the list
                    journalListAdapter = new JournalListAdapter(JournalListActivity.this, filteredList, this);
                    recyclerView.setAdapter(journalListAdapter);
                }
            }




        }
        //if user searches for empty string remove the search filter
        else{
            removeFilters();
        }


        //reset the screen
        searchBarEditText.setText("");
        Utils.hideKeyboard(searchBarEditText);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(JournalApi.getInstance().getUsername() == null || JournalApi.getInstance().getUserId() == null){

            //when the user is signed out take them back to the "get started" screen
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();

        }
        currentTagText.setText("");
        currentTagText.setVisibility(View.INVISIBLE);
        currentTagCardView.setVisibility(View.INVISIBLE);
        currentTagCancelImage.setVisibility(View.INVISIBLE);

        if(journalList.size() > 0){
            //sort the list by time added
            sortByDate(journalList);
            //set up the recyclerview with the list
            journalListAdapter = new JournalListAdapter(JournalListActivity.this, journalList, this);
            recyclerView.setAdapter(journalListAdapter);
        }
        //if the data is empty
        else{
            //there is no data so prompt user with how to add a journal
            noJournalEntry.setText(R.string.no_journal_entries_prompt);
            noJournalEntry.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        journalList.clear();

        if(JournalApi.getInstance().getUsername() == null || JournalApi.getInstance().getUserId() == null){

            //when the user is signed out take them back to the "get started" screen
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();

        }

        currentTagText.setText("");
        currentTagText.setVisibility(View.INVISIBLE);
        currentTagCardView.setVisibility(View.INVISIBLE);
        currentTagCancelImage.setVisibility(View.INVISIBLE);


        //get the data saved by the current user by checking userId
        journalCollection.whereEqualTo("userId", JournalApi.getInstance().getUserId())
                .get()
                //if data is obtained successfully
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    //if the data is not empty
                    if(!queryDocumentSnapshots.isEmpty()){
                        //loop through the journal entries and add them to the journal list
                        for(QueryDocumentSnapshot journals: queryDocumentSnapshots){
                            Journal journal = journals.toObject(Journal.class);

                            journalList.add(journal);

                        }
                        //sort the list by time added
                        sortByDate(journalList);
                        //set up the recyclerview with the list
                        journalListAdapter = new JournalListAdapter(JournalListActivity.this, journalList, this);
                        recyclerView.setAdapter(journalListAdapter);
                    }
                    //if the data is empty
                    else{
                        //there is no data so prompt user with how to add a journal
                        noJournalEntry.setText(R.string.no_journal_entries_prompt);
                        noJournalEntry.setVisibility(View.VISIBLE);
                    }
                })
                //if we fail to get data
                .addOnFailureListener(e -> Toast.makeText(JournalListActivity.this, "Failed to get Journal entries", Toast.LENGTH_LONG)
                        .show());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topbar_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_add:
            case R.id.action_add_icon:

                //take user to add journal
                if(user != null && firebaseAuth != null){
                    Intent intent = new Intent(this, WriteJournalActivity.class);
                    startActivity(intent);
                    finish();
                }
                break;

            case R.id.action_logout:
                //log user out

                if(user != null && firebaseAuth != null){
                    firebaseAuth.signOut();
                    /*
                    if we have issues with user login check the journal api. might need to set fields to null.
                     */

                    //when the user is signed out take them back to the "get started" screen
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                break;
            case R.id.action_gallery:
            case R.id.action_gallery_icon:
                //toggle the layout

                if(!isGallery){
                    isGallery = true;
                    contentViewGalleryLayout();
                    setGalleryView();
                }
                else{
                    isGallery = false;
                    contentViewListLayout();
                    setListLayout();
                }

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //this functions like onCreate for the layout
    private void contentViewListLayout(){
        setContentView(R.layout.activity_journal_list);
        searchBarEditText = findViewById(R.id.search_bar_edit_text);
        currentTagText = findViewById(R.id.current_tag_text);
        searchButton = findViewById(R.id.search_image_button);
        searchCardView = findViewById(R.id.search_bar_cardview);
        currentTagCardView = findViewById(R.id.current_tag_cardview);
        currentTagCancelImage = findViewById(R.id.exit_x_image_tag_cardview);

        noJournalEntry = findViewById(R.id.no_journals_textview);
        recyclerView = findViewById(R.id.recyclerview_journal_list);

        mAdviewList = findViewById(R.id.adView_list);

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdviewList.loadAd(adRequest);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchButton.setOnClickListener(this::searchButtonMethod);
        searchCardView.setOnClickListener(this::searchButtonMethod);
        currentTagText.setOnClickListener(this::currentTagRemoveMethod);
        currentTagCardView.setOnClickListener(this::currentTagRemoveMethod);
    }

    private void setListLayout(){

        currentTagText.setText("");
        currentTagText.setVisibility(View.INVISIBLE);
        currentTagCardView.setVisibility(View.INVISIBLE);
        currentTagCancelImage.setVisibility(View.INVISIBLE);

        if(journalList.size() > 0){
            //sort the list by time added
            sortByDate(journalList);
            //set up the recyclerview with the list
            journalListAdapter = new JournalListAdapter(JournalListActivity.this, journalList, this);
            recyclerView.setAdapter(journalListAdapter);
        }
        else{

            //there is no data so prompt user with how to add a journal
            noJournalEntry.setText(R.string.no_journal_entries_prompt);
            noJournalEntry.setVisibility(View.VISIBLE);

        }
    }

    //this functions like onCreate for the layout
    private void contentViewGalleryLayout(){

        setContentView(R.layout.gallery_journal_list);

        searchBarEditText = findViewById(R.id.search_bar_edit_text_gallery);
        currentTagText = findViewById(R.id.current_tag_text_gallery);
        searchButton = findViewById(R.id.search_image_button_gallery);
        searchCardView = findViewById(R.id.search_bar_cardview_gallery);
        currentTagCardView = findViewById(R.id.current_tag_cardview_gallery);
        currentTagCancelImage = findViewById(R.id.exit_x_image_tag_cardview_gallery);

        noJournalEntry = findViewById(R.id.no_journals_textview_gallery);
        gridView = findViewById(R.id.gridview_journal_gallery_list);

        mAdviewGallery = findViewById(R.id.adView_gallery);

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdviewGallery.loadAd(adRequest);

        searchButton.setOnClickListener(this::searchButtonMethod);
        searchCardView.setOnClickListener(this::searchButtonMethod);
        currentTagText.setOnClickListener(this::currentTagRemoveMethod);
        currentTagCardView.setOnClickListener(this::currentTagRemoveMethod);
    }

    private void setGalleryView() {

        currentTagText.setText("");
        currentTagText.setVisibility(View.INVISIBLE);
        currentTagCardView.setVisibility(View.INVISIBLE);
        currentTagCancelImage.setVisibility(View.INVISIBLE);

        if(journalList.size() > 0){
            //sort the list by time added
            sortByDate(journalList);

            //gridview adapter
            JournalGridViewAdapter gridAdapter = new JournalGridViewAdapter(this, journalList);

            gridView.setAdapter(gridAdapter);
            gridView.setNumColumns(3);


        }
        else{

            //there is no data so prompt user with how to add a journal
            noJournalEntry.setText(R.string.no_journal_entries_prompt);
            noJournalEntry.setVisibility(View.VISIBLE);

        }

    }


    @Override
    protected void onStop() {
        super.onStop();
        journalList.clear();
    }

    @Override
    public void onJournalClick(Journal journal) {
        String uniqueReferenceString;

        if(journal.getUniqueRefName() != null){
            uniqueReferenceString = journal.getUniqueRefName();
        }
        else{
            uniqueReferenceString = "";
        }

        Intent intent = new Intent(JournalListActivity.this, WriteJournalActivity.class);
        intent.putExtra("unique_ref", uniqueReferenceString);
        startActivity(intent);


    }
}