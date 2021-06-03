package com.example.fui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

public class StepsActivity extends AppCompatActivity implements View.OnClickListener {

    ImageButton back_img, read_aloud_img, prevStep, nextStep;
    TextView stepNumber, stepText, stepCount;

    private List<String> recipe_text;
    private String RECIPE_TEXT="recipe_text";


    int noOfSteps;

    String[] temp, tempTitle;
    int initialPosition=1;

    private TextToSpeech mtts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps);

        init();
        Intent intent = getIntent();
        List<String> recipeSteps = intent.getStringArrayListExtra("recipe_text");
        noOfSteps = recipeSteps.size();
        temp = new String[noOfSteps];
        tempTitle = new String[noOfSteps];


        back_img.setOnClickListener(this);
        read_aloud_img.setOnClickListener(this);
        prevStep.setOnClickListener(this);
        nextStep.setOnClickListener(this);

        mtts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS)
                {
                    int result = mtts.setLanguage(Locale.ENGLISH);

                    if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                    {
                        Toast.makeText(StepsActivity.this, "Language not supported", Toast.LENGTH_SHORT).show();
                    }//                        tts.setEnabled(true);

                }else{
                    Toast.makeText(StepsActivity.this, "Initialization failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        for(int i=0;i<recipeSteps.size();i++)
        {
            int x = i+1;
            String text = recipeSteps.get(i);
            temp[i] = text;
            text = "STEP "+x;
            tempTitle[i] = text;
        }

        stepNumber.setText(tempTitle[initialPosition-1]);
        stepText.setText(temp[initialPosition-1]);
        String c = initialPosition+"/"+noOfSteps;
        stepCount.setText(c);


        setAlignment();

    }

    private void setAlignment() {
        stepNumber.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        stepText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
    }

    private void init() {
        back_img = findViewById(R.id.back_img);
        read_aloud_img = findViewById(R.id.read_aloud_img);
        prevStep = findViewById(R.id.prevStep);
        nextStep = findViewById(R.id.nextStep);
        stepNumber = findViewById(R.id.stepNumber);
        stepText = findViewById(R.id.stepText);
        stepCount = findViewById(R.id.stepCount);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.back_img:
                finish();
                break;

            case R.id.read_aloud_img:
                Toast.makeText(this, "Read aloud clicked", Toast.LENGTH_SHORT).show();
                speak();
                break;

            case R.id.prevStep:
                goBack();
                break;

            case R.id.nextStep:
                goForward();
                break;
        }
    }

    private void speak() {
        String text = stepText.getText().toString();
        mtts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        mtts.setSpeechRate(0.8f);

    }

    @Override
    protected void onDestroy() {
        if(mtts != null){
            mtts.stop();
            mtts.shutdown();
        }
        super.onDestroy();
    }

    private void goForward() {
        if(initialPosition == noOfSteps)
        {
            Toast.makeText(this, "Last step reached", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(this, "Next Step clicked", Toast.LENGTH_SHORT).show();
            initialPosition++;
            stepNumber.setText(tempTitle[initialPosition-1]);
            stepText.setText(temp[initialPosition-1]);
            String c = initialPosition+"/"+noOfSteps;
            stepCount.setText(c);
            setAlignment();
        }
    }

    private void goBack() {
        if(initialPosition == 1)
        {
            Toast.makeText(this, "First position achieved", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(this, "Previous step clicked", Toast.LENGTH_SHORT).show();
            initialPosition--;
            stepNumber.setText(tempTitle[initialPosition-1]);
            stepText.setText(temp[initialPosition-1]);
            String c = initialPosition+"/"+noOfSteps;
            stepCount.setText(c);
            setAlignment();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}