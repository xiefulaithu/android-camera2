package com.aai.camera2demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.aai.camera2demo.ui.recognize.RecognizeFragment;

public class RecognizeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recognize_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, RecognizeFragment.newInstance())
                    .commitNow();
        }
    }
}
