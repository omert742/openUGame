package com.example.openugame.activities;

import android.app.Activity;
import android.app.ProgressDialog;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.openugame.R;
import com.example.openugame.utils.Player;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.example.openugame.listeners.MessageListener.VALUE_KEY;
import static com.example.openugame.listeners.MessageListener.START_GAME_ACTION;

public class MainActivity extends AppCompatActivity {
    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                intent.getExtras().get(VALUE_KEY);
                Intent myIntent = new Intent(MainActivity.this, GameActivity.class);
                myIntent.putExtra(VALUE_KEY, intent.getExtras().get(VALUE_KEY).toString());
                MainActivity.this.startActivity(myIntent);
            } catch (Exception e) {
                //TODO : error message
            }
        }
    };
    public ProgressDialog progress = null;
    private Player player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver),
                new IntentFilter(START_GAME_ACTION)
        );
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(/*context=*/ this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance());

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(false);
        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Firebase authentication...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
        //Authenticate
        FirebaseAuth.getInstance().signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        progress.dismiss();
                        Log.d("Gal", "signInAnonymously:success");
                    } else {
                        progress.setMessage("Failed to do firebase authentication...");
                        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                        progress.show();
                    }
                });

        TextInputEditText playerName = findViewById(R.id.playerName);
        Button connectButton = findViewById(R.id.button);

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

        connectButton.setOnClickListener(view -> {
            connectButton.setEnabled(false);
            progress.setMessage("Sending player data to server...");
            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
            progress.show();

            try {
                player = new Player(Objects.requireNonNull(playerName.getText()).toString());
            } catch (Exception e) {
                ((TextInputLayout) findViewById(R.id.textInputLayout)).setError("Invalid name");
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("name", player.getName());
            FirebaseFunctions.getInstance().getHttpsCallable("addPlayerToWaitingList")
                    .call(data)
                    .continueWith(task -> {
                        Log.i("Gal", " Continue with then async");

                        progress.setMessage("Waiting for opponent...");
                        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                        progress.show();
                        return "";
                    }).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.i("Gal", "Message sent successfully");
                        } else {
                            Log.i("Gal", "Message failed");
                            progress.setMessage("Failed to do be added to waiting list ...");
                            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                            progress.show();
                        }

                    });


        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mMessageReceiver, new IntentFilter(START_GAME_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mMessageReceiver);
        // TODO: remove player from waiting list
    }
}