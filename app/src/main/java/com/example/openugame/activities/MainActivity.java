package com.example.openugame.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.google.firebase.functions.FirebaseFunctions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.example.openugame.listeners.MessageListener.VALUE_KEY;
import static com.example.openugame.listeners.MessageListener.START_GAME_ACTION;
import static com.example.openugame.listeners.MessageListener.getToken;

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
                stopApp("Error in broadcast receiver : "+e.toString());
            }
        }
    };
    private ProgressDialog progress = null;
    private Button connectButton;
    private TextInputEditText playerName;
    private Player player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver),
                new IntentFilter(START_GAME_ACTION)
        );
        setContentView(R.layout.activity_main);
        initAndShowDialog();
        initFireBase();
        setListeners();
    }

    private void initAndShowDialog(){
        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Firebase authentication...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
    }

    private void initFireBase(){
        FirebaseApp.initializeApp(/*context=*/ this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance());
        //Authenticate
        FirebaseAuth.getInstance().signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    progress.dismiss();
                    if (task.isSuccessful()) {
                        getToken().addOnCompleteListener(task2 -> {
                            if (task2.isSuccessful()) {
                                progress.cancel();
                            }else {
                                progress.setMessage("Failed to get firebase messaging token");
                            }
                        });

                    } else {
                        progress.setMessage("Failed to do firebase authentication...");
                    }
                });
    }

    private void setListeners(){
        playerName = findViewById(R.id.playerName);
        connectButton = findViewById(R.id.button);

        playerName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                MainActivity.this.checkValidPlayerName();
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
                stopApp("Failed to initialize player name");
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
                    Log.e("Gal", "Message failed");
                    stopApp("Failed to do be added to waiting list ...");
                }

            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mMessageReceiver, new IntentFilter(START_GAME_ACTION));
        progress.cancel();
        checkValidPlayerName();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mMessageReceiver);
    }

    private void stopApp(String msg){
        AlertDialog alertDialog = null;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setPositiveButton("Ok",
                (arg0, arg1) -> MainActivity.this.finish());
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        alertDialog.setCancelable(false);
    }

    private void checkValidPlayerName() {
        if(Player.isValidName(this.playerName.getText().toString())){
            connectButton.setEnabled(true);
            ((TextInputLayout) findViewById(R.id.textInputLayout)).setError("");
        }else {
            connectButton.setEnabled(false);

            if(this.playerName.getText().toString().length() > 0) {
                ((TextInputLayout) findViewById(R.id.textInputLayout)).setError("Invalid name");
            }
        }
    }
}