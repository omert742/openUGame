package com.example.openugame;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class gameActivity extends AppCompatActivity {

    public ImageView circle_1, circle_2, circle_3, circle_4, circle_5, circle_c_1, circle_c_2, circle_c_3, circle_c_4, circle_c_5, circle_clicked_1, circle_clicked_2, circle_clicked_3, circle_clicked_4, circle_clicked_5, finishBtn;
    public TextView my_score, apo_score,round;
    public List<String> colors = Arrays.asList("green", "gray", "blue", "red", "yellow");
    public List<String> select_color = new ArrayList<String>();
    public ArrayList<ImageView> clicked_images = new ArrayList<ImageView>();
    public ProgressDialog progress = null;
    public long startTime = 0;
    public int round_num = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_game);
        initObjects();
        randomCircles();
    }

    public void initObjects(){
        circle_1 = (ImageView) findViewById(R.id.circle_1);
        circle_2 = (ImageView) findViewById(R.id.circle_2);
        circle_3 = (ImageView) findViewById(R.id.circle_3);
        circle_4 = (ImageView) findViewById(R.id.circle_4);
        circle_5 = (ImageView) findViewById(R.id.circle_5);
        circle_c_1 = (ImageView) findViewById(R.id.circle_c_1);
        circle_c_2 = (ImageView) findViewById(R.id.circle_c_2);
        circle_c_3 = (ImageView) findViewById(R.id.circle_c_3);
        circle_c_4 = (ImageView) findViewById(R.id.circle_c_4);
        circle_c_5 = (ImageView) findViewById(R.id.circle_c_5);
        circle_clicked_1 = (ImageView) findViewById(R.id.circle_clicked_1);
        circle_clicked_2 = (ImageView) findViewById(R.id.circle_clicked_2);
        circle_clicked_3 = (ImageView) findViewById(R.id.circle_clicked_3);
        circle_clicked_4 = (ImageView) findViewById(R.id.circle_clicked_4);
        circle_clicked_5 = (ImageView) findViewById(R.id.circle_clicked_5);
        clicked_images.add(circle_clicked_5);
        clicked_images.add(circle_clicked_4);
        clicked_images.add(circle_clicked_3);
        clicked_images.add(circle_clicked_2);
        clicked_images.add(circle_clicked_1);
        my_score = (TextView) findViewById(R.id.my_score);
        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        round = (TextView) findViewById(R.id.round);
        apo_score = (TextView) findViewById(R.id.apo_score);
        finishBtn = (ImageView) findViewById(R.id.finishBtn);
        finishBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                resetAllCircle();
                randomCircles();
            }
        });
        String yellow_color = "yellow";
        String red_color = "red";
        String blue_color = "blue";
        String gray_color = "gray";
        String green_color = "green";
        circle_5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                circle_5.setEnabled(false);
                showMySelected(red_color, clicked_images.get(0));
            }
        });
        circle_4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                circle_4.setEnabled(false);
                showMySelected(yellow_color, clicked_images.get(0));
            }
        });
        circle_3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                circle_3.setEnabled(false);
                showMySelected(blue_color, clicked_images.get(0));
            }
        });
        circle_2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                circle_2.setEnabled(false);
                showMySelected(gray_color, clicked_images.get(0));
            }
        });
        circle_1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                circle_1.setEnabled(false);
                showMySelected(green_color, clicked_images.get(0));
            }
        });
    }

    public void showMySelected(String color, ImageView v1){
        setCircleColor(v1, color);
        select_color.add(color);
        clicked_images.remove(0);
        if (select_color.size() == colors.size()){
            finishRoundActions();
        }
    }

    public void finishRoundActions(){
        long difference = System.currentTimeMillis() - startTime;
        double difference_sec = difference / 1000;
        Collections.reverse(select_color);
        if (select_color.equals(colors)){
            progress.setMessage("Perfect!!! its took you "+difference_sec+" sec...\nWaiting for the opponent result...");
            round_num += 1;
            round.setText("Round num : "+round_num);
            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
            progress.show();
        }else{
            Toast.makeText(gameActivity.this, "Seems you've been wrong..." , Toast.LENGTH_LONG).show();
            resetAllCircle();
        }
//        progress.dismiss();
        //Toast.makeText(gameActivity.this, "opponent won the point" , Toast.LENGTH_LONG).show();
    }

    public void randomCircles(){
        startTime = System.currentTimeMillis();
        Collections.shuffle(colors);
        setCircleColor(circle_c_1, colors.get(0));
        setCircleColor(circle_c_2, colors.get(1));
        setCircleColor(circle_c_3, colors.get(2));
        setCircleColor(circle_c_4, colors.get(3));
        setCircleColor(circle_c_5, colors.get(4));
    }

    public void setCircleColor(ImageView circle, String color){
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

    public void resetAllCircle(){
        circle_clicked_1.setImageResource(R.drawable.black_circle);
        circle_clicked_2.setImageResource(R.drawable.black_circle);
        circle_clicked_3.setImageResource(R.drawable.black_circle);
        circle_clicked_4.setImageResource(R.drawable.black_circle);
        circle_clicked_5.setImageResource(R.drawable.black_circle);
        select_color = new ArrayList<String>();
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