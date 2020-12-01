package com.jiashuai.videoviewlayoutproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.jiashuai.videoviewlayoutproject.videoview.VideoViewLayout;

public class MainActivity extends AppCompatActivity {
    VideoViewLayout viewLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewLayout = findViewById(R.id.video_layout);
        viewLayout.setVideoPath("https://files.raykite.com/raydata/test/20200818/775642A4-7031-41D2-B8D7-8B28C2F378EAVideo_1597721632464.mp4");
    }
}