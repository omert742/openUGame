package com.example.openugame.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.openugame.R;
import com.example.openugame.utils.Player;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Player player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver),
                new IntentFilter("MyData")
        );
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(/*context=*/ this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance());

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(false);

        //Authenticate
        FirebaseAuth.getInstance().signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //TODO : need to add loading screen until authenticated otherwise will get an error when trying to call firebase function
                        if (task.isSuccessful()) {

                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Gal", "signInAnonymously:success");
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Gal", "signInAnonymously:failure", task.getException());
                        }
                    }
                });


        TextInputEditText playerName = findViewById(R.id.playerName);
        Button connectButton = findViewById(R.id.button);
        Button goToGame = findViewById(R.id.gotogame);
        goToGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent( MainActivity.this, GameActivity.class);
                myIntent.putExtra("name", "omer"); //Optional parameters
                MainActivity.this.startActivity(myIntent);
            }
        });
        playerName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                connectButton.setEnabled(true);
                ((TextInputLayout) findViewById(R.id.textInputLayout)).setError("");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    player = new Player(playerName.getText().toString());
                } catch (Exception e) {
                    connectButton.setEnabled(false);
                    ((TextInputLayout) findViewById(R.id.textInputLayout)).setError("Invalid name");
                }

                Map<String, Object> data = new HashMap<>();
                data.put("name", player.getName());
                FirebaseFunctions.getInstance().getHttpsCallable("addPlayerToWaitingList")
                        .call(data)
                        .continueWith(new Continuation<HttpsCallableResult, String>() {
                            @Override
                            public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                                //TODO: waiting screen (waiting for message to be sent)
                                //HashMap result = (HashMap) task.getResult().getData();
                                return "";
                            }
                        }).addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<String> task) {
                        Log.i("Gal", "Sent message");
                        if (task.isSuccessful()) {
                            //TODO: waiting screen (message sent successfully, waiting for another player)
                            Log.i("Gal", "Successfully");
                        } else {
                            //TODO: error screen
                            Log.i("Gal", "Unsuccessfully");
                            Exception e = task.getException();
                            if (e instanceof FirebaseFunctionsException) {
                                FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                                FirebaseFunctionsException.Code code = ffe.getCode();
                                Object details = ffe.getDetails();
                            }
                        }

                    }
                });


            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (this.player != null) {
            // TODO: remove player from waiting list
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Gal", "Message received");
        }
    };
}