package com.example.openugame.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.openugame.R;
import com.google.android.gms.tasks.Continuation;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.example.openugame.listeners.MessageListener.VALUE_KEY;
import static com.example.openugame.listeners.MessageListener.token;

public class GameActivity extends AppCompatActivity {

    private ImageView circle_1, circle_2, circle_3, circle_4, circle_5, circle_c_1, circle_c_2, circle_c_3, circle_c_4, circle_c_5, circle_clicked_1, circle_clicked_2, circle_clicked_3, circle_clicked_4, circle_clicked_5;
    private TextView my_score_view, opo_score_view, round_value_view;
    private List<String> select_color = new ArrayList<>();
    private ArrayList<ImageView> clicked_images = new ArrayList<>();
    private ProgressDialog progress = null;
    private long startTime = 0;
    private int round_num = 1;
    private int my_score = 0;
    private int opponent_score = 0;
    private String gameID;

    private final List<String> colors = Arrays.asList("green", "gray", "blue", "red", "yellow");
    private final int MAX_TURNS = 5;
    private final int OPPONENT_TIMEOUT = 60 * 1000; // How long to wait for opponent result before considered as 'disconnected'

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("Gal", "Starting game activity");
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

        this.round_num++;
        this.round_value_view.setText(String.valueOf(GameActivity.this.round_num));
        this.progress.cancel();

        if (i_won) {
            this.my_score++;
        } else {
            this.opponent_score++;
        }
        this.my_score_view.setText(String.valueOf(GameActivity.this.my_score));
        this.opo_score_view.setText(String.valueOf(GameActivity.this.opponent_score));

        if(this.round_num > this.MAX_TURNS){
            String msg;
            if(my_score > opponent_score){
                msg = "What an amazing victory !";
            }else if(opponent_score > my_score){
                msg = "You should consider improving your skills\nSadly,you've lost the game";
            }else {
                msg = "Its a tie!";
            }
            stopGame(msg);
        }
    }

    @Override
    public void onBackPressed() {
        // disable back button press
    }

    private void initObjects() {
        Intent intent = getIntent();
        this.gameID = intent.getStringExtra(VALUE_KEY);
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

    private void stopGame(String msg){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setPositiveButton("Go back to main menu",
                (arg0, arg1) -> GameActivity.this.finish());
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        alertDialog.setCancelable(false);
    }

    private void showMySelected(String color, ImageView v1) {
        setCircleColor(v1, color);
        select_color.add(color);
        clicked_images.remove(0);
        if (select_color.size() == colors.size()) {
            finishRoundActions();
        }
    }

    private void finishRoundActions() {
        long difference = System.currentTimeMillis() - startTime;
        double difference_sec = difference / 1000.0;
        Collections.reverse(select_color);
        if (select_color.equals(colors)) {
            progress.setMessage("Perfect!!! \nSending result to server");
            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
            progress.show();

            Map<String, Object> data = new HashMap<>();
            data.put("gameID", this.gameID);
            data.put("turn", this.round_num);
            FirebaseFunctions.getInstance().getHttpsCallable("sendScore")
                    .call(data)
                    .continueWith(task -> "").addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    progress.setMessage("Waiting for the opponent result...");
                    this.checkScore(this.round_num);
                } else {
                    Log.e("Gal", "finishRoundActions: " ,task.getException());
                    this.stopGame("Failed to send score");
                }

            });
        } else {
            Toast.makeText(GameActivity.this, "Seems you've been wrong...", Toast.LENGTH_LONG).show();
            resetAllCircle();
        }
    }

    private void checkScore(int turn) {

        new CountDownTimer(this.OPPONENT_TIMEOUT, 2 * 1000) {

            public void onTick(long millisUntilFinished) {
                Log.i("Gal", "onTick: Checking score..");
                Map<String, Object> data = new HashMap<>();
                data.put("gameID", GameActivity.this.gameID);
                data.put("turn", GameActivity.this.round_num);
                FirebaseFunctions.getInstance().getHttpsCallable("checkScore")
                        .call(data).continueWith((Continuation<HttpsCallableResult, Object>) task -> {
                            try {
                                if (task.isSuccessful()) {
                                    HashMap res = (HashMap) Objects.requireNonNull(task.getResult()).getData();
                                    if(res != null) {
                                        String winner = (String) res.get("winner");
                                        int turn = (int) res.get("turn");

                                        // found a winner for current turn, set up a new turn
                                        if (winner.length() > 0 && turn != GameActivity.this.round_num) {
                                            GameActivity.this.newTurn(winner.equals(token));
                                            cancel();// stop timer
                                        }
                                    }

                                } else {
                                    throw Objects.requireNonNull(task.getException());
                                }
                            }catch (Exception e){
                                Log.e("Gal", "onTick: Failed to check score", e);
                            }
                    return null;
                });
            }

            public void onFinish() {
                if(turn == GameActivity.this.round_num){
                    Log.e("Gal", "onFinish: count time ended but turn didn't change, assume disconnection");
                    stopGame("Seems like your opponent left the game, disconnecting...");
                }
            }

        }.start();


    }



    private void randomCircles() {
        startTime = System.currentTimeMillis();
        Collections.shuffle(colors);
        setCircleColor(circle_c_1, colors.get(0));
        setCircleColor(circle_c_2, colors.get(1));
        setCircleColor(circle_c_3, colors.get(2));
        setCircleColor(circle_c_4, colors.get(3));
        setCircleColor(circle_c_5, colors.get(4));
    }

    private void setCircleColor(ImageView circle, String color) {
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

    private void resetAllCircle() {
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