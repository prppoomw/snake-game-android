package com.example.testsnakenormal;

import static java.security.AccessController.getContext;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


public class MainActivity extends AppCompatActivity {
    private boolean isSoundOn = true;
    private MediaPlayer bgsong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        ImageView detail = findViewById(R.id.about);
        detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDetailDialog();
            }
        });



        ImageView play = findViewById(R.id.normal);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,GameActivity.class);
                intent.putExtra("isSoundOn", isSoundOn);
                startActivity(intent);
            }
        });

        bgsong = MediaPlayer.create(this, R.raw.bgsong);
        bgsong.setLooping(true);
        bgsong.start();

        ImageView soundButton = findViewById(R.id.sound);
        soundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSound();
            }
        });
    }

    private void toggleSound() {
        if (isSoundOn) {
            // Turn off sound
            bgsong.pause();
            ((ImageView) findViewById(R.id.sound)).setImageResource(R.drawable.soff);
        } else {
            // Turn on sound
            bgsong.start();
            ((ImageView) findViewById(R.id.sound)).setImageResource(R.drawable.son);
        }
        isSoundOn = !isSoundOn;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bgsong != null) {
            bgsong.release();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (bgsong != null && bgsong.isPlaying()) {
            bgsong.pause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bgsong != null && bgsong.isPlaying()) {
            bgsong.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bgsong != null && !bgsong.isPlaying() && isSoundOn) {
            bgsong.start();
        }
    }

    public void showDetailDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Term Project CS361");
        builder.setMessage(R.string.detail);
        builder.setPositiveButton("close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.show();
    }


}


