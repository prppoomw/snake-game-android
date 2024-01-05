package com.example.testsnakenormal;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity implements BgMusicListener{

    private boolean isSoundOn;
    private MediaPlayer bgsong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve sound-related information from Intent
        Intent intent = getIntent();
        isSoundOn = intent.getBooleanExtra("isSoundOn", true);

        // Start or pause background music based on isSoundOn
        if (isSoundOn) {
            bgsong = MediaPlayer.create(this, R.raw.gamesong);
            bgsong.setLooping(true);
            bgsong.start();
        }


        SnakeGame snakeGame = findViewById(R.id.snakeGame);
        snakeGame.setBackgroundMusicListener(this);
        snakeGame.updateScoreTextView();
        ImageView btnPause = findViewById(R.id.pause_btn);
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ทำตามต้องการเมื่อปุ่ม Pause ถูกคลิก
                snakeGame.showPauseDialog();
                pauseBackgroundMusic();
            }
        });
    }

    public void pauseBackgroundMusic() {
        if (bgsong != null && bgsong.isPlaying()) {
            bgsong.pause();
        }
    }

    public void playAgainMusic(){
        if (isSoundOn) {
            bgsong = MediaPlayer.create(this,R.raw.gamesong);
            bgsong.setLooping(true);
            bgsong.start();
        }
    }

    public void gameOverMusic(){
        if (bgsong != null && bgsong.isPlaying()) {
            bgsong.pause();
            bgsong.release();
        }
    }

    public void resumeBackgroundMusic() {
        if (isSoundOn && bgsong != null && !bgsong.isPlaying()) {
            bgsong.start();
        }
    }

}
