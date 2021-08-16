package com.example.openugame.listeners;

import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.openugame.activities.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MessageListener extends FirebaseMessagingService {

    public static final String START_GAME_ACTION = "START_GAME";
    public static final String GAMEID_DATA = "value";


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
                Map<String, String> data = remoteMessage.getData();
                switch (data.get("action")) {
                    case START_GAME_ACTION:
                        Intent intent = new Intent(START_GAME_ACTION);
                        intent.putExtra(GAMEID_DATA, data.get(GAMEID_DATA));
                        broadcaster.sendBroadcast(intent);

                        break;

                    case "OPPONENT_DONE":
                        //TODO
                        // OPPONENT SENT score
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
