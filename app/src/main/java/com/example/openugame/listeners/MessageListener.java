package com.example.openugame.listeners;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

    public class MessageListener extends FirebaseMessagingService {

    public static String token = null;

    public static final String START_GAME_ACTION = "START_GAME";
    public static final String NEXT_TURN_ACTION = "NEXT_TURN";
    public static final String VALUE_KEY = "value";
    private static final String TAG = "Gal";




    private LocalBroadcastManager broadcaster;

    @Override
    public void onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i(TAG, "New message received");
        // Check if message contains a data payload.
        try {
            Intent intent;
            Map<String, String> data = remoteMessage.getData();
            String action = Objects.requireNonNull(data.get("action"));
            switch (action) {
                case START_GAME_ACTION:
                    Log.i(TAG, "Starting game...");
                    intent = new Intent(START_GAME_ACTION);
                    intent.putExtra(VALUE_KEY, data.get(VALUE_KEY));
                    broadcaster.sendBroadcast(intent);

                    break;

                case NEXT_TURN_ACTION:
                    Log.i(TAG, "Next turn..");
                    intent = new Intent(NEXT_TURN_ACTION);
                    intent.putExtra(VALUE_KEY, data.get(VALUE_KEY));
                    broadcaster.sendBroadcast(intent);
                    break;

                case "ERROR":
                    //TODO : Tell the user an error occurred and send back to waiting
                    break;

                default:
                    throw new Exception("Unknown action received: '" + action + "'");
            }
        } catch (Exception e) {
            Log.e(TAG, "onMessageReceived: " + e.getMessage(), e);
        }
    }

        @Override
        public void onDeletedMessages() {
            super.onDeletedMessages();
            Log.i(TAG, "onDeletedMessages: Message was deleted");
        }

        public static Task<String> getToken(){
        return FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task2 -> {
            token = task2.getResult();
            Log.i("Gal", "My token is " + token);
        });
    }

    @Override
    public void onNewToken(@NonNull @NotNull String s) {
        super.onNewToken(s);
        Log.i(TAG, "onNewToken: received new token");
        token = s;
    }
}
