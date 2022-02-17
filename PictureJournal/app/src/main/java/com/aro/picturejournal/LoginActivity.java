package com.aro.picturejournal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.aro.picturejournal.util.JournalApi;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    /*
    this is the screen where users can login if they have an account.
    if they don't have an account they can click to go to the Create Account Activity
     */

    private AutoCompleteTextView emailText;
    private EditText passwordText;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersCollection = db.collection("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button loginButton = findViewById(R.id.login_button);
        Button goToCreateAccountButton = findViewById(R.id.create_account_button);
        emailText = findViewById(R.id.email_edittext);
        passwordText = findViewById(R.id.password_edittext);
        progressBar = findViewById(R.id.progress_bar_login);

        firebaseAuth = FirebaseAuth.getInstance();
        
        
        
        loginButton.setOnClickListener(this::loginButtonMethod);
        goToCreateAccountButton.setOnClickListener(this::goToCreateAccountButtonMethod);
        
    }

    private void goToCreateAccountButtonMethod(View view) {

        Intent intent = new Intent(this, CreateAccountActivity.class);
        startActivity(intent);

    }

    private void loginButtonMethod(View view) {

        String userInputEmail = emailText.getText().toString().trim();
        String userInputPassword = passwordText.getText().toString();

        loginEmailPasswordUser(userInputEmail, userInputPassword);

    }

    private void loginEmailPasswordUser(String userInputEmail, String userInputPassword) {
        progressBar.setVisibility(View.VISIBLE);

        //if we have the user input
        if(!TextUtils.isEmpty(userInputEmail) && !TextUtils.isEmpty(userInputPassword)){
            //log the user in with that input
            firebaseAuth.signInWithEmailAndPassword(userInputEmail, userInputPassword)
                    //if login is successful
                    .addOnSuccessListener(authResult -> {
                        user = firebaseAuth.getCurrentUser();

                        String currentUserId = Objects.requireNonNull(user).getUid();

                        //use the user id to loop through the collection of users and get the correct one so we can set username/id in journalapi
                        usersCollection
                                .whereEqualTo("userId", currentUserId)
                                .addSnapshotListener((value, error) -> {

                                    if(!Objects.requireNonNull(value).isEmpty()){
                                        //loop through user collection and get the correct user
                                        for(QueryDocumentSnapshot snapshot: value){
                                            //set username and userId to journal api global storage
                                            String snapshotUsername = snapshot.getString("username");
                                            JournalApi journalApi = JournalApi.getInstance();
                                            journalApi.setUsername(snapshotUsername);
                                            journalApi.setUserId(currentUserId);

                                            progressBar.setVisibility(View.INVISIBLE);

                                            //go to JournalList after logged in
                                            Intent intent = new Intent(LoginActivity.this, JournalListActivity.class);
                                            startActivity(intent);

                                        }
                                    }
                                });

                    })
                    //if login is not successful
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(LoginActivity.this,
                                "Failed to Login", Toast.LENGTH_LONG)
                                .show();
                    });
        }
        //if we don't have user input
        else{
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_LONG)
                    .show();
        }
    }
}