package com.aai.camera2demo;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;

import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_expert_help:
                    mTextMessage.setText(R.string.title_expert_help);
                    return true;
                case R.id.navigation_recognize_pests_and_diseases:
                    Intent intent = new Intent(MainActivity.this, Camera2Activity.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_trends_pests_and_diseases:
                    mTextMessage.setText(R.string.title_trends_pests_and_diseases);
                    return true;
                case R.id.navigation_recording_farming_events:
                    mTextMessage.setText(R.string.title_record_farming_events);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}
