package com.example.openugame.listeners;

import android.app.ProgressDialog;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MessageListener extends FirebaseMessagingService {

    public static final String START_GAME_ACTION = "START_GAME";
    public static final String OPPONENT_DONE_ACTION = "OPPONENT_DONE";
    public static final String MESSAGE_KEY = "value";


    private LocalBroadcastManager broadcaster;

    @Override
    public void onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Check if message contains a data payload.
        if (remoteMessage.getData() != null) {
            try {
                Intent intent;
                Map<String, String> data = remoteMessage.getData();
                switch (data.get("action")) {
                    case START_GAME_ACTION:
                        intent = new Intent(START_GAME_ACTION);
                        intent.putExtra(MESSAGE_KEY, data.get(MESSAGE_KEY));
                        broadcaster.sendBroadcast(intent);

                        break;

                    case OPPONENT_DONE_ACTION:
                        intent = new Intent(OPPONENT_DONE_ACTION);
                        intent.putExtra(MESSAGE_KEY, data.get(MESSAGE_KEY));
                        broadcaster.sendBroadcast(intent);
                        break;

                    case "ERROR":
                        //TODO : Tell the user an error occurred and send back to waiting
                        break;
                }
            }
            catch (Exception e){

            }
        }
    }

}
