package com.example.openugame;

import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class gameActivity extends AppCompatActivity {

    public ImageView circle_1, circle_2, circle_3, circle_4, circle_5, circle_c_1, circle_c_2, circle_c_3, circle_c_4, circle_c_5, finishBtn;
    public TextView my_score, apo_score;
    public List<String> colors = Arrays.asList("green", "gray", "blue", "red", "yellow");

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
        my_score = (TextView) findViewById(R.id.my_score);
        apo_score = (TextView) findViewById(R.id.apo_score);
        finishBtn = (ImageView) findViewById(R.id.finishBtn);
        finishBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                randomCircles();
            }
        });
    }


    public void randomCircles(){
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
        circle_1.setImageResource(R.drawable.black_circle);
        circle_2.setImageResource(R.drawable.black_circle);
        circle_3.setImageResource(R.drawable.black_circle);
        circle_4.setImageResource(R.drawable.black_circle);
        circle_5.setImageResource(R.drawable.black_circle);
        circle_c_1.setImageResource(R.drawable.black_circle);
        circle_c_2.setImageResource(R.drawable.black_circle);
        circle_c_3.setImageResource(R.drawable.black_circle);
        circle_c_4.setImageResource(R.drawable.black_circle);
        circle_c_5.setImageResource(R.drawable.black_circle);
    }
}