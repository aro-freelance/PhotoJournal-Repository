package com.aro.picturejournal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.aro.picturejournal.util.JournalApi;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


public class MainActivity extends AppCompatActivity {


    /*
      This is an app that lets the user keep a journal with photos.
      A user can input image, title, message and tags. Only the title is mandatory.
      The journals are then displayed in a list form or alternately a grid form.
      Journals can be clicked in list/grid to update or delete.
      Uses firebase authentication to allow users to create an email login.
      Journals are stored in firestore and loaded from firestore based on user account.

      //todo add persistence to the current tag. so that onPause it will filter for that tag again

      //todo make alternate landscape layouts

      //todo fix issue with Auth. getting null values returned somehow from firebase


     */

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersReference = db.collection("Users");


    /*
    this activity handles authentication of the user.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button getStartedButton = findViewById(R.id.start_button);

        firebaseAuth = FirebaseAuth.getInstance();

        //check if user is logged in. If they are send them to the JournalListActivity.
        //App will remember the last logged in user even if the app has been closed and reopened.
        // to login as a new user they will now need to use the menu > sign out
        authStateListener = firebaseAuth -> {
            user = firebaseAuth.getCurrentUser();
            if(user != null) {
                String userId = user.getUid();
                //look in the user collection and find the document with the userId equal to current userId
                usersReference.whereEqualTo("userId", userId)
                        .addSnapshotListener((value, error) -> {
                            //if we found a user
                            if(value != null){
                                if(!value.isEmpty()){
                                    //loop through the users and get the info we need for JournalApi (user info)
                                    for(QueryDocumentSnapshot snapshot : value){
                                        JournalApi journalApi = JournalApi.getInstance();
                                        journalApi.setUsername(snapshot.getString("username"));
                                        journalApi.setUserId(userId);

                                        Intent intent = new Intent(this, JournalListActivity.class);
                                        startActivity(intent);
                                        finish(); // we don't want to come back here if we click back button
                                    }
                                }

                            }

                        });
            }
        };


        getStartedButton.setOnClickListener(this::startButtonMethod);
    }

    private void startButtonMethod(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish(); // we don't want to come back here if we click back button

    }

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(firebaseAuth != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}