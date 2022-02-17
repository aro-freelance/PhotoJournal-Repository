package com.aro.picturejournal;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aro.picturejournal.model.Journal;
import com.aro.picturejournal.util.JournalApi;
import com.aro.picturejournal.util.Utils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class WriteJournalActivity extends AppCompatActivity {
    /*
    this is where the user writes a new journal entry.
    It will also be the screen where they view single entries and edit them (after clicking them in the list)
     */

    private static final int GALLERY_CODE = 1;
    private EditText titleText;
    private EditText journalEntryText;
    private ImageView photo;
    private ProgressBar progressBar;
    private Button saveButton;
    private Button deleteButton;

    private TextView tagsListText;
    private Button tagsButton;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private final CollectionReference journalCollection = db.collection("Journal");



    private StringBuilder tagString = new StringBuilder("Tags : ");
    private List<String> listOfTagStrings;

    private Uri imageUri;

    private boolean isEdit = false;
    private String clickedJournalUniqueReference = "";
    private Journal clickedJournal;
    private boolean updatePhoto = false;

    String username;
    String userId;
    JournalApi journalApi = JournalApi.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_journal);

        titleText = findViewById(R.id.title_edit_text);
        journalEntryText = findViewById(R.id.journal_entry_edit_text);
        ImageView addPhotoButton = findViewById(R.id.camera_icon);
        photo = findViewById(R.id.image_journal);
        progressBar = findViewById(R.id.progress_bar_write_journal);
        saveButton = findViewById(R.id.save_button_write_journal);
        deleteButton = findViewById(R.id.delete_button_write_journal);

        tagsListText = findViewById(R.id.tags_textview_write_journal);
        tagsButton = findViewById(R.id.enter_tag_button_write_journal);

        progressBar.setVisibility(View.INVISIBLE);
        deleteButton.setVisibility(View.GONE);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        listOfTagStrings = new ArrayList<>();

        Bundle bundle = getIntent().getExtras();

        if(bundle != null){
            if(bundle.getString("unique_ref") != null){
                clickedJournalUniqueReference = bundle.getString("unique_ref");
                isEdit = true;

            }
        }

        if(journalApi != null){
            username = JournalApi.getInstance().getUsername();
            userId = JournalApi.getInstance().getUserId();


        }

        authStateListener = firebaseAuth -> user = firebaseAuth.getCurrentUser();


        titleText.setOnKeyListener((view, keyCode, keyEvent) -> {
            if(keyEvent.getAction() == KeyEvent.ACTION_DOWN){
                switch(keyCode){
                    //if the enter key is pressed when the title text is being edited close the soft keyboard
                    case KeyEvent.KEYCODE_ENTER:
                        Utils.hideKeyboard(titleText);
                        //and remove the extra space... one line max
                        titleText.setText(titleText.getText().toString().trim());
                        //and open the entry text
                        journalEntryText.requestFocus();
                }
            }

            return false;
        });

        journalEntryText.setOnKeyListener((view, keyCode, keyEvent) -> {

            if(keyEvent.getAction() == KeyEvent.ACTION_DOWN){
                switch(keyCode){
                    //if the enter key is pressed close the soft keyboard
                    case KeyEvent.KEYCODE_ENTER:
                        Utils.hideKeyboard(titleText);

                }
            }

            return false;
        });



        saveButton.setOnClickListener(this::saveButtonMethod);
        addPhotoButton.setOnClickListener(this::addPhotoButtonMethod);
        tagsButton.setOnClickListener(this::tagButtonMethod);
        deleteButton.setOnClickListener(this::deleteButtonMethod);
    }





    private void tagButtonMethod(View view) {

        //open tags dialog

        BottomSheetDialog tagDialog = new BottomSheetDialog(this);
        tagDialog.setContentView(R.layout.tags_dialog);

        EditText tagEditText = tagDialog.findViewById(R.id.tags_edit_text);
        Button tagButton = tagDialog.findViewById(R.id.tag_dialog_button);


        //populate the edit text with the list of tags separated by commas
        if (listOfTagStrings.size() > 0) {
            //this should clear the string builder each time so there are no duplicates.
            tagString = new StringBuilder();
            for (int i = 0; i < listOfTagStrings.size(); i++) {

                //build the tag string from the tag list
                tagString.append(listOfTagStrings.get(i)).append(", ");
                //and set it to the edit text in the dialog

                Objects.requireNonNull(tagEditText).setText(tagString);
            }
        }


        tagDialog.show();

        Objects.requireNonNull(tagEditText).setOnKeyListener((view12, keyCode, keyEvent) -> {

            if(keyEvent.getAction() == KeyEvent.ACTION_DOWN){
                switch(keyCode){
                    //if the enter key is pressed close the soft keyboard
                    case KeyEvent.KEYCODE_ENTER:
                        Utils.hideKeyboard(tagEditText);
                        tagEditText.setText(tagEditText.getText().toString().trim());

                }
            }
            return false;
        });


        //update the tags in the list and in the textview when the dialog is closed
/////////////////////
        //close dialog by button

        Objects.requireNonNull(tagButton).setOnClickListener(view1 -> {

            if (!TextUtils.isEmpty(tagEditText.getText().toString().trim())) {

                listOfTagStrings = separateStringByCommas(tagEditText.getText().toString().trim().toUpperCase());

                tagString = new StringBuilder("Tags : ");
                for (int i = 0; i < listOfTagStrings.size(); i++) {

                    tagString.append(listOfTagStrings.get(i)).append(", ");
                }
                //remove the comma and space at the end
                tagString.deleteCharAt(tagString.length()-1);
                tagString.deleteCharAt(tagString.length()-1);
                tagsListText.setText(tagString);

            }
            else {
                listOfTagStrings.clear();
                tagsListText.setText("");
            }

            tagDialog.dismiss();

        });

        //close by cancel
        tagDialog.setOnCancelListener(dialogInterface -> {
            if (!TextUtils.isEmpty(tagEditText.getText().toString().trim())) {

                listOfTagStrings = separateStringByCommas(tagEditText.getText().toString().trim().toUpperCase());

                tagString = new StringBuilder("Tags : ");
                for (int i = 0; i < listOfTagStrings.size(); i++) {

                    tagString.append(listOfTagStrings.get(i)).append(", ");

                }
                //remove the comma and space at the end
                tagString.deleteCharAt(tagString.length()-1);
                tagString.deleteCharAt(tagString.length()-1);
                tagsListText.setText(tagString);

            }
            else {
                listOfTagStrings.clear();
                tagsListText.setText("");
            }
        });

        //close by dismiss
        tagDialog.setOnDismissListener(dialogInterface -> {
            if (!TextUtils.isEmpty(tagEditText.getText().toString().trim())) {

                listOfTagStrings = separateStringByCommas(tagEditText.getText().toString().trim().toUpperCase());

                tagString = new StringBuilder("Tags : ");
                for (int i = 0; i < listOfTagStrings.size(); i++) {

                    tagString.append(listOfTagStrings.get(i)).append(", ");

                }
                //remove the comma and space at the end
                tagString.deleteCharAt(tagString.length()-1);
                tagString.deleteCharAt(tagString.length()-1);
                tagsListText.setText(tagString);

            }
            else {
                listOfTagStrings.clear();
                tagsListText.setText("");
            }

        });
////////////////////////

    }

    private List<String> separateStringByCommas(String string) {

        String[] strings = string.trim().split(",");

        List<String> list = new ArrayList<>(Arrays.asList(strings));

        //trim all the extra spaces and remove empty/null strings
        for (int i = 0; i < list.size(); i++) {
            String trimmedString = list.get(i).trim();
            list.set(i, trimmedString);
            list.removeAll(Arrays.asList("", null));
        }


        return list;
    }

    @Override
    protected void onStart() {
        super.onStart();

        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);

        Log.d("edit", "is edit is " + isEdit);

        if(isEdit && !updatePhoto){
            saveButton.setText(R.string.update_button_string);
            deleteButton.setVisibility(View.VISIBLE);

            journalCollection.whereEqualTo("userId", JournalApi.getInstance().getUserId())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {

                        if(!queryDocumentSnapshots.isEmpty()) {
                            //loop through the journal entries
                            for (QueryDocumentSnapshot journals : queryDocumentSnapshots) {
                                Journal journal = journals.toObject(Journal.class);

                                if(journal.getUniqueRefName() != null){
                                    //if the journal matches the one clicked title and timestamp it is the clicked journal
                                    if(journal.getUniqueRefName().equals(clickedJournalUniqueReference)){
                                        //set the clicked journal
                                        clickedJournal = journal;

                                        //set the views
                                        titleText.setText(clickedJournal.getTitle());
                                        journalEntryText.setText(clickedJournal.getEntry());
                                        if (clickedJournal.getTags() != null) {
                                            tagsButton.setText("Edit Tags");
                                            //this should clear the string builder each time so there are no duplicates.
                                            tagString = new StringBuilder("Tags : ");
                                            for (int i = 0; i < clickedJournal.getTags().size(); i++) {

                                                //build the tag string from the tag list
                                                tagString.append(clickedJournal.getTags().get(i)).append(", ");
                                                //and set it to the text view
                                                tagsListText.setText(tagString);
                                            }

                                            listOfTagStrings = clickedJournal.getTags();
                                        }
                                        if(clickedJournal.getImageUrl() != null){
                                            String imageUrl = clickedJournal.getImageUrl();

                                            //picasso image loader source: https://square.github.io/picasso/
                                            //this is where the image is loaded into the imageview
                                            Picasso.get().load(imageUrl).placeholder(R.drawable.image_one).into(photo);

                                        }
                                    }
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(WriteJournalActivity.this,
                            "Failed to load the journal that was clicked", Toast.LENGTH_LONG)
                            .show());

        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(firebaseAuth != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    private void addPhotoButtonMethod(View view) {

        if(isEdit){
            updatePhoto = true;
        }

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_CODE && resultCode == RESULT_OK){
            if(data != null){
                imageUri = data.getData(); // this is the path where the image is
                photo.setImageURI(imageUri);
            }
        }
    }


    private void deleteButtonMethod(View view) {
        deleteJournal();
    }

    private void deleteJournal() {
        journalCollection.document(clickedJournal.getUniqueRefName()).delete()
                .addOnSuccessListener(unused -> {
                    Intent intent = new Intent(WriteJournalActivity.this, JournalListActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(WriteJournalActivity.this,
                "Journal could not be safely deleted from Firestore",
                Toast.LENGTH_LONG)
                .show());

    }

    private void saveButtonMethod(View view) {
        if(isEdit){
            updateJournal();
        }
        else{
            saveJournal();
        }
    }

    private void updateJournal() {

        progressBar.setVisibility(View.VISIBLE);
        String title = titleText.getText().toString().trim();
        String journalEntry = journalEntryText.getText().toString().trim();

        if(TextUtils.isEmpty(journalEntry)){
            journalEntry = "";
        }

        //if we have user input
        if(!TextUtils.isEmpty(title)) {

            //this is to prevent an error (variable used in lamba expression should be final)
            String finalJournalEntry = journalEntry;

            //if there is a new image
            if(updatePhoto){
                //create an image file path
                String imageName = title + JournalApi.getInstance().getUserId() + Timestamp.now().getSeconds();
                StorageReference filepath = storageReference //... / journal_images/our_image.jpg
                        .child("journal_images")
                        .child(imageName);
                //save the image to the file path
                filepath.putFile(imageUri)
                        //if the image saves to firebase
                        .addOnSuccessListener(taskSnapshot -> {
                            progressBar.setVisibility(View.INVISIBLE);

                            //get the url of the image after it saves so that we can store that to the journal class
                            filepath.getDownloadUrl()
                                    .addOnSuccessListener(uri -> {
                                        String imageUrl = uri.toString();
                                        //make journal object
                                        Journal journal = new Journal();
                                        journal.setTitle(title);
                                        journal.setEntry(finalJournalEntry);
                                        journal.setImageUrl(imageUrl);
                                        journal.setTimeAdded(clickedJournal.getTimeAdded());
                                        journal.setUsername(clickedJournal.getUsername());
                                        journal.setUserId(clickedJournal.getUserId());
                                        journal.setUniqueRefName(clickedJournal.getUniqueRefName());
                                        if(listOfTagStrings.size() > 0){
                                            journal.setTags(listOfTagStrings);
                                        }

                                        //update the journal object in the document for the journal clicked
                                        journalCollection.document(clickedJournal.getUniqueRefName()).set(journal)
                                                //if it is successfully added, take user to the journal list activity
                                                .addOnSuccessListener(documentReference -> {
                                                    Intent intent = new Intent(WriteJournalActivity.this, JournalListActivity.class);
                                                    startActivity(intent);
                                                    finish();

                                                })
                                                .addOnFailureListener(e -> Toast.makeText(WriteJournalActivity.this,
                                                        "Journal could not be saved to Firestore",
                                                        Toast.LENGTH_LONG)
                                                        .show());

                                    })
                                    //if we can't get the image url
                                    .addOnFailureListener(e -> Toast.makeText(WriteJournalActivity.this,
                                            "Failed to retrieve file location", Toast.LENGTH_LONG)
                                            .show());


                        })
                        //if the image doesn't save to firebase
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(WriteJournalActivity.this, "Failed to upload", Toast.LENGTH_LONG)
                                    .show();
                        });

            }

            //no new image
            else{

                journalCollection.document(clickedJournal.getUniqueRefName()).update("title", title);
                journalCollection.document(clickedJournal.getUniqueRefName()).update("entry", finalJournalEntry);
                if(listOfTagStrings.size() > 0){
                    journalCollection.document(clickedJournal.getUniqueRefName()).update("tags", listOfTagStrings);
                }
                Intent intent = new Intent(WriteJournalActivity.this, JournalListActivity.class);
                startActivity(intent);
                finish();

            }
        }
        //user input is missing
        else{
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_LONG)
                    .show();
        }

    }




    private void saveJournal() {
        progressBar.setVisibility(View.VISIBLE);
        String title = titleText.getText().toString().trim();
        String journalEntry = journalEntryText.getText().toString().trim();

        if(TextUtils.isEmpty(journalEntry)){
            journalEntry = "";
        }

        //if we have user input
        if(!TextUtils.isEmpty(title)){

            String uniqueReferenceName = title + JournalApi.getInstance().getUserId() + Timestamp.now().getSeconds();

            //this is to prevent an error (variable used in lamba expression should be final)
            String finalJournalEntry = journalEntry;

            //if there is an image
            if(imageUri != null){
                //create an image file path

                StorageReference filepath = storageReference //... / journal_images/our_image.jpg
                        .child("journal_images")
                        .child(uniqueReferenceName);
                //save the image to the file path
                filepath.putFile(imageUri)
                        //if the image saves to firebase
                        .addOnSuccessListener(taskSnapshot -> {
                            progressBar.setVisibility(View.INVISIBLE);

                            //get the url of the image after it saves so that we can store that to the journal class
                            filepath.getDownloadUrl()
                                    .addOnSuccessListener(uri -> {
                                        String imageUrl = uri.toString();
                                        //make journal object
                                        Journal journal = new Journal();
                                        journal.setTitle(title);
                                        journal.setEntry(finalJournalEntry);
                                        journal.setImageUrl(imageUrl);
                                        journal.setTimeAdded(new Timestamp(new Date()));
                                        journal.setUsername(username);
                                        journal.setUserId(userId);
                                        journal.setUniqueRefName(uniqueReferenceName);
                                        if(listOfTagStrings.size() > 0){
                                            journal.setTags(listOfTagStrings);
                                        }

                                        //add journal object to firestore collection
                                        journalCollection.document(uniqueReferenceName).set(journal)
                                                //if it is successfully added, take user to the journal list activity
                                                .addOnSuccessListener(documentReference -> {
                                                    Intent intent = new Intent(WriteJournalActivity.this, JournalListActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> Toast.makeText(WriteJournalActivity.this,
                                                        "Journal could not be saved to Firestore",
                                                        Toast.LENGTH_LONG)
                                                        .show());

                                    })
                                    //if we can't get the image url
                                    .addOnFailureListener(e -> Toast.makeText(WriteJournalActivity.this,
                                            "Failed to retrieve file location", Toast.LENGTH_LONG)
                                            .show());


                        })
                        //if the image doesn't save to firebase
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(WriteJournalActivity.this, "Failed to upload", Toast.LENGTH_LONG)
                                    .show();
                        });


            }
            //if imageUri is null (there is no image)
            else{

                Journal journal = new Journal();
                journal.setTitle(title);
                journal.setEntry(finalJournalEntry);
                journal.setTimeAdded(new Timestamp(new Date()));
                journal.setUsername(username);
                journal.setUserId(userId);
                journal.setUniqueRefName(uniqueReferenceName);
                if(listOfTagStrings.size() > 0){
                    journal.setTags(listOfTagStrings);
                }

                //add journal object to firestore collection
                journalCollection.document(uniqueReferenceName).set(journal)
                        //if it is successfully added, take user to the journal list activity
                        .addOnSuccessListener(documentReference -> {
                            Intent intent = new Intent(WriteJournalActivity.this, JournalListActivity.class);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(WriteJournalActivity.this,
                                "Journal could not be saved to Firestore",
                                Toast.LENGTH_LONG)
                                .show());
            }
        }
        //user input is missing
        else{
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_LONG)
                    .show();
        }
    }

}