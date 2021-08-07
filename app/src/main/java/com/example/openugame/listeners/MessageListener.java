package com.example.openugame.listeners;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

public class MessageListener extends FirebaseMessagingService {



    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            switch (remoteMessage.getNotification().getTitle()) {
                case "START_GAME":
                    //TODO start game activity
                    Log.i("Gal", "Should start game");
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
    }

}
