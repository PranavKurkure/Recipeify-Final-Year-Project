package com.example.fui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import static java.lang.Thread.sleep;

public class SplashActivity extends AppCompatActivity {


    private ImageView logo;
    private TextView txt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);



        logo = findViewById(R.id.logo);
        txt  = findViewById(R.id.txt);
        logo.startAnimation(AnimationUtils.loadAnimation(
                getApplicationContext(),
                R.anim.zoom_in
        ));
        txt.startAnimation(AnimationUtils.loadAnimation(
                getApplicationContext(),
                R.anim.slide_up
        ));

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    sleep(4000);
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                }catch(InterruptedException ex)
                {
                    ex.printStackTrace();
                }
            }
        });
        thread.start();

    }
}
