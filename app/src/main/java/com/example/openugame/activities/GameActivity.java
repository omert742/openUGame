package com.example.openugame.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.openugame.R;
import com.example.openugame.listeners.MessageListener;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.example.openugame.listeners.MessageListener.NEXT_TURN_ACTION;
import static com.example.openugame.listeners.MessageListener.VALUE_KEY;

public class GameActivity extends AppCompatActivity {

    public ImageView circle_1, circle_2, circle_3, circle_4, circle_5, circle_c_1, circle_c_2, circle_c_3, circle_c_4, circle_c_5, circle_clicked_1, circle_clicked_2, circle_clicked_3, circle_clicked_4, circle_clicked_5;
    public TextView my_score_view, opo_score_view, round_value_view;
    public List<String> colors = Arrays.asList("green", "gray", "blue", "red", "yellow");
    public List<String> select_color = new ArrayList<>();
    public ArrayList<ImageView> clicked_images = new ArrayList<>();
    public ProgressDialog progress = null;
    public long startTime = 0;
    public int round_num = 1;
    public int my_score = 0;
    public int opponent_score = 0;
    private String gameID;


    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                // check if winning token equals to current player
                String winner_token = intent.getExtras().get(VALUE_KEY).toString();
                GameActivity.this.newTurn(winner_token.equals(MessageListener.token));

            } catch (Exception e) {
                //TODO : error message
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("Gal", "Starting game activity");
        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver),
                new IntentFilter(NEXT_TURN_ACTION)
        );

        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_game);
        initObjects();
        randomCircles();

        Intent intent = getIntent();
        this.gameID = intent.getStringExtra(VALUE_KEY);
    }

    /**
     * Set up a new turn and update UI parameters
     * @param i_won True if current user won
     */
    private void newTurn(Boolean i_won) {
        resetAllCircle();
        randomCircles();

        round_num += 1;
        round_value_view.setText(String.valueOf(GameActivity.this.round_num));
        progress.cancel();

        if (i_won) {
            my_score++;
        } else {
            opponent_score++;
        }
        my_score_view.setText(String.valueOf(GameActivity.this.my_score));
        opo_score_view.setText(String.valueOf(GameActivity.this.opponent_score));
    }

    @Override
    public void onBackPressed() {
        // disable back button press
    }

    public void initObjects() {
        circle_1 = findViewById(R.id.circle_1);
        circle_2 = findViewById(R.id.circle_2);
        circle_3 = findViewById(R.id.circle_3);
        circle_4 = findViewById(R.id.circle_4);
        circle_5 = findViewById(R.id.circle_5);
        circle_c_1 = findViewById(R.id.circle_c_1);
        circle_c_2 = findViewById(R.id.circle_c_2);
        circle_c_3 = findViewById(R.id.circle_c_3);
        circle_c_4 = findViewById(R.id.circle_c_4);
        circle_c_5 = findViewById(R.id.circle_c_5);
        circle_clicked_1 = findViewById(R.id.circle_clicked_1);
        circle_clicked_2 = findViewById(R.id.circle_clicked_2);
        circle_clicked_3 = findViewById(R.id.circle_clicked_3);
        circle_clicked_4 = findViewById(R.id.circle_clicked_4);
        circle_clicked_5 = findViewById(R.id.circle_clicked_5);
        clicked_images.add(circle_clicked_5);
        clicked_images.add(circle_clicked_4);
        clicked_images.add(circle_clicked_3);
        clicked_images.add(circle_clicked_2);
        clicked_images.add(circle_clicked_1);
        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        round_value_view = findViewById(R.id.round_value);
        my_score_view = findViewById(R.id.my_score);
        opo_score_view = findViewById(R.id.opo_score);

        String yellow_color = "yellow";
        String red_color = "red";
        String blue_color = "blue";
        String gray_color = "gray";
        String green_color = "green";
        circle_5.setOnClickListener(v -> {
            circle_5.setEnabled(false);
            showMySelected(red_color, clicked_images.get(0));
        });
        circle_4.setOnClickListener(v -> {
            circle_4.setEnabled(false);
            showMySelected(yellow_color, clicked_images.get(0));
        });
        circle_3.setOnClickListener(v -> {
            circle_3.setEnabled(false);
            showMySelected(blue_color, clicked_images.get(0));
        });
        circle_2.setOnClickListener(v -> {
            circle_2.setEnabled(false);
            showMySelected(gray_color, clicked_images.get(0));
        });
        circle_1.setOnClickListener(v -> {
            circle_1.setEnabled(false);
            showMySelected(green_color, clicked_images.get(0));
        });
    }

    public void showMySelected(String color, ImageView v1) {
        setCircleColor(v1, color);
        select_color.add(color);
        clicked_images.remove(0);
        if (select_color.size() == colors.size()) {
            finishRoundActions();
        }
    }

    public void finishRoundActions() {
        long difference = System.currentTimeMillis() - startTime;
        double difference_sec = difference / 1000.0;
        Collections.reverse(select_color);
        if (select_color.equals(colors)) {
            progress.setMessage("Perfect!!! it took you \n" + difference_sec + " sec...\nWaiting for the opponent result...");
            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
            progress.show();

            Map<String, Object> data = new HashMap<>();
            data.put("gameID", this.gameID);
            data.put("turn", this.round_num);
            FirebaseFunctions.getInstance().getHttpsCallable("sendScore")
                    .call(data)
                    .continueWith(task -> "").addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // TODO: sent score successfully
                        } else {
                            //TODO: failed to send score
                        }

                    });
        } else {
            Toast.makeText(GameActivity.this, "Seems you've been wrong...", Toast.LENGTH_LONG).show();
            resetAllCircle();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mMessageReceiver, new IntentFilter(NEXT_TURN_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mMessageReceiver);
    }

    public void randomCircles() {
        startTime = System.currentTimeMillis();
        Collections.shuffle(colors);
        setCircleColor(circle_c_1, colors.get(0));
        setCircleColor(circle_c_2, colors.get(1));
        setCircleColor(circle_c_3, colors.get(2));
        setCircleColor(circle_c_4, colors.get(3));
        setCircleColor(circle_c_5, colors.get(4));
    }

    public void setCircleColor(ImageView circle, String color) {
        switch (color) {
            case "green":
                circle.setImageResource(R.drawable.green);
                break;
            case "gray":
                circle.setImageResource(R.drawable.gray_circle);
                break;
            case "blue":
                circle.setImageResource(R.drawable.blue);
                break;
            case "red":
                circle.setImageResource(R.drawable.red_circle);
                break;
            case "yellow":
                circle.setImageResource(R.drawable.yellow_cricle);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + color);
        }
    }

    public void resetAllCircle() {
        circle_clicked_1.setImageResource(R.drawable.black_circle);
        circle_clicked_2.setImageResource(R.drawable.black_circle);
        circle_clicked_3.setImageResource(R.drawable.black_circle);
        circle_clicked_4.setImageResource(R.drawable.black_circle);
        circle_clicked_5.setImageResource(R.drawable.black_circle);
        select_color = new ArrayList<>();
        circle_1.setEnabled(true);
        circle_2.setEnabled(true);
        circle_3.setEnabled(true);
        circle_4.setEnabled(true);
        circle_5.setEnabled(true);
        clicked_images.add(circle_clicked_5);
        clicked_images.add(circle_clicked_4);
        clicked_images.add(circle_clicked_3);
        clicked_images.add(circle_clicked_2);
        clicked_images.add(circle_clicked_1);
    }
}