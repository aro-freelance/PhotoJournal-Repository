package com.aro.picturejournal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.aro.picturejournal.util.JournalApi;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateAccountActivity extends AppCompatActivity {

    /*
    this activity handles making new email authentication accounts to use the app. Using firebase.
     */

    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private ProgressBar progressBar;


    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        usernameEditText = findViewById(R.id.user_name_create_acc);
        emailEditText = findViewById(R.id.email_edittext_create_acc);
        passwordEditText = findViewById(R.id.password_edittext_create_acc);
        progressBar = findViewById(R.id.progress_bar_create_acc);
        Button createAccountButton = findViewById(R.id.create_account_button_create_acc);


        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = firebaseAuth -> currentUser = firebaseAuth.getCurrentUser();


        createAccountButton.setOnClickListener(this::createAccountButtonMethod);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(firebaseAuth != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //get current user
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    private void createAccountButtonMethod(View view) {

        if(!TextUtils.isEmpty(emailEditText.getText().toString())
                && !TextUtils.isEmpty(passwordEditText.getText().toString())
                && !TextUtils.isEmpty(usernameEditText.getText().toString())) {

            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String username = usernameEditText.getText().toString().trim();

            //make the account
            createUserEmailAccount(email, password, username);
        }
        else{
            Toast.makeText(this, "Cannot Create Account without all three fields completed", Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void createUserEmailAccount(String email, String password, String username){

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username)){
            progressBar.setVisibility(View.VISIBLE);

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            //get the username
                            currentUser = firebaseAuth.getCurrentUser();
                            assert currentUser != null;
                            String currentUserId = currentUser.getUid();

                            //add user to the Firestore collection of users
                            Map<String, String> userObj = new HashMap<>();
                            userObj.put("userId", currentUserId);
                            userObj.put("username", username);
                            db.collection("Users").add(userObj)
                                    .addOnSuccessListener(documentReference -> documentReference.get()
                                            .addOnCompleteListener(task1 -> {
                                                progressBar.setVisibility(View.GONE);

                                                //use our journal api class to save the user info globally
                                                JournalApi journalApi = JournalApi.getInstance();
                                                journalApi.setUserId(currentUserId);
                                                journalApi.setUsername(username);

                                                //take user to add journal activity
                                                Intent intent = new Intent(this, WriteJournalActivity.class);
                                                startActivity(intent);
                                            }))
                                    .addOnFailureListener(e -> progressBar.setVisibility(View.GONE));
                        }
                        else{
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(CreateAccountActivity.this,
                                    "Account Creation Unsuccessful. Check that email is correct and password is strong.", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    })
                    .addOnFailureListener(e -> progressBar.setVisibility(View.GONE));
        }else{
            progressBar.setVisibility(View.GONE);
        }
    }
}