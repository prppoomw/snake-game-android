package com.example.testsnakenormal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class SnakeGame extends SurfaceView implements Runnable, SurfaceHolder.Callback {
    private final Paint paint;
    private Thread thread;
    private boolean running, gameOver;
    private final LinkedList<PointF> snakeBody;
    private final float padding;
    private float foodX;
    private float foodY;
    private float initialTouchX;
    private float initialTouchY;
    private float gridSize;
    private float textSize;
    private long lastPressTime;
    private int maxX, maxY;
    private Direction direction;
    private Random random;
    private boolean paused = false;
    private float pauseButtonSize;
    private Direction savedSnakeDirection;

    private LinkedList<PointF> savedSnakePosition;
    private float savedFoodX;
    private float savedFoodY;
    private int score = 0;
    private Bitmap backgroundBitmap;
    private static final String PREF_HIGH_SCORE = "highScore";
    private int highScore = 0;
    private MediaPlayer eatingSound;
    private MediaPlayer dieSound;

    private BgMusicListener backgroundMusicListener;



    public SnakeGame(Context context, AttributeSet at) {
        super(context, at);
        paint = new Paint();
        running = false;
        gameOver = false;
        snakeBody = new LinkedList<>();
        snakeBody.add(new PointF(10f, 10f));
        padding = 2f;
        direction = Direction.RIGHT;
        //backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
        SharedPreferences preferences = context.getSharedPreferences("SnakeGamePrefs", Context.MODE_PRIVATE);
        int savedHighScore = preferences.getInt(PREF_HIGH_SCORE, 0);
        highScore = savedHighScore;
        eatingSound = MediaPlayer.create(context, R.raw.eat);
        dieSound = MediaPlayer.create(context, R.raw.die);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        gridSize = Math.min(getWidth(), getHeight()) / 26f;
        textSize = Math.min(getWidth(), getHeight()) / 20f;
        maxX = (int) (getWidth() / gridSize) - 1;
        maxY = (int) (getHeight() / gridSize) - 1;
        random = new Random();
        randomizeFood();
        resume();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // No-op
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        pause();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (!paused) {
                    if (isTouchInsidePauseButton(event.getX(), event.getY())) {
                        showPauseDialog();
                    }
                    else {
                        initialTouchX = event.getX();
                        initialTouchY = event.getY();
                        lastPressTime = System.currentTimeMillis();
                    }
                }
//                initialTouchX = event.getX();
//                initialTouchY = event.getY();
//                lastPressTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_UP:
                if (System.currentTimeMillis() - lastPressTime >= 1000L) {
//                    gameOver = false;
//                    snakeBody.clear();
//                    snakeBody.add(new PointF(10f, 10f));
//                    direction = Direction.RIGHT;
//                    randomizeFood();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = event.getX() - initialTouchX;
                float deltaY = event.getY() - initialTouchY;
                direction = Math.abs(deltaX) > Math.abs(deltaY)
                        ? (deltaX > 0 ? Direction.RIGHT : Direction.LEFT)
                        : (deltaY > 0 ? Direction.DOWN : Direction.UP);
                break;
        }
        return true;
    }

    @Override
    public void run() {
        while (running) {
            if (!gameOver) {
                update();
                draw();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void update() {
        PointF head = snakeBody.getFirst();
        PointF newHead = new PointF(head.x, head.y);

        switch (direction) {
            case RIGHT:
                newHead.x += gridSize;
                break;
            case LEFT:
                newHead.x -= gridSize;
                break;
            case UP:
                newHead.y -= gridSize;
                break;
            case DOWN:
                newHead.y += gridSize;
                break;
        }

        if (newHead.x < 0 || newHead.y < 0 || newHead.x >= getWidth() || newHead.y >= getHeight()) {
            if (backgroundMusicListener != null) {
                backgroundMusicListener.gameOverMusic();
            }
            if (dieSound != null) {
                dieSound.start();
            }
            gameOver = true;
            ((Activity) getContext()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showGameOverDialog(); // Display the game over dialog
                }
            });
            return;
        }

        // ตรวจสอบการชนกับตัวเอง
        for (int i = 1; i < snakeBody.size(); i++) {
            PointF bodyPart = snakeBody.get(i);
            if (newHead.x == bodyPart.x && newHead.y == bodyPart.y) {
                // ถ้าตำแหน่งของหัวตรงกับตำแหน่งของส่วนอื่น ๆ ของงู
                if (backgroundMusicListener != null) {
                    backgroundMusicListener.gameOverMusic();
                }
                if (dieSound != null) {
                    dieSound.start();
                }
                gameOver = true;
                ((Activity) getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showGameOverDialog(); // Display the game over dialog
                    }
                });
                return;
            }
        }


        snakeBody.addFirst(newHead);

        if (Math.abs(newHead.x - foodX) < gridSize && Math.abs(newHead.y - foodY) < gridSize ) {
            if (eatingSound != null) {
                eatingSound.start();
            }
            score++;
            if (score > highScore) {
                highScore = score;
                saveHighScore(); // Save the new high score to SharedPreferences
            }

            updateScoreTextView();
            randomizeFood();
        }
        else {
            snakeBody.removeLast();
        }
    }


    private void draw() {
        if (getHolder().getSurface().isValid()) {
            Canvas canvas = getHolder().lockCanvas();
            canvas.drawColor(Color.BLACK);

            paint.setColor(Color.GREEN);
            for (PointF point : snakeBody) {
                RectF rect = new RectF(point.x + padding, point.y + padding, point.x + gridSize - padding, point.y + gridSize - padding);
                canvas.drawRect(rect, paint);
            }

            paint.setColor(Color.RED);
            RectF foodRect = new RectF(foodX, foodY, foodX + gridSize, foodY + gridSize);
            canvas.drawRect(foodRect, paint);



            if (gameOver) {
                paint.setColor(Color.WHITE);
                paint.setTextSize(textSize);
                String text = "Game Over"; // replace with context.getString(R.string.game_over_text) if you are using string resources
                float textWidth = paint.measureText(text);
                canvas.drawText(text, (getWidth() - textWidth) / 2, getHeight() / 2, paint);

            }

            getHolder().unlockCanvasAndPost(canvas);
        }
    }


    private void randomizeFood() {
        foodX = random.nextInt(maxX) * gridSize;
        foodY = random.nextInt(maxY) * gridSize;

    }

    public void pause() {
        running = false;

        savedSnakeDirection = direction;
        savedSnakePosition = new LinkedList<>(snakeBody);
        savedFoodX = foodX;
        savedFoodY = foodY;
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        thread = null;
    }

    public void resume() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public boolean isTouchInsidePauseButton(float x, float y) {
        // ตรวจสอบว่าการแตะอยู่ภายในปุ่ม pause หรือไม่
        return x >= padding && x <= (padding + pauseButtonSize)
                && y >= padding && y <= (padding + pauseButtonSize);
    }

    public void setBackgroundMusicListener(BgMusicListener listener) {
        this.backgroundMusicListener = listener;
    }

    public void showPauseDialog() {
        pause(); // หยุดเกมชั่วคราว

        // TODO: สร้างหน้าต่าง pause ที่ให้เลือกว่าจะเล่นต่อหรือออก
        // ในหน้าต่าง pause ให้เพิ่มปุ่ม "Resume" และ "Quit"
        // ตัวอย่าง:
         AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
         builder.setMessage("Game Paused");
         builder.setPositiveButton("Resume", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 if (backgroundMusicListener != null) {
                     backgroundMusicListener.resumeBackgroundMusic();
                 }

                 direction = savedSnakeDirection;
                 snakeBody.clear();
                 snakeBody.addAll(savedSnakePosition);
                 foodX = savedFoodX;
                 foodY = savedFoodY;


                 resume();
                 dialog.dismiss();
             }
         });
         builder.setNegativeButton("Home", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 ((Activity) getContext()).finish();
             }
         });
         AlertDialog dialog = builder.create();
         dialog.show();
    }


    public void updateScoreTextView() {
        // Update the score TextView in the layout
        ((Activity) getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView scoreTextView = ((Activity) getContext()).findViewById(R.id.scoreTextView);
                TextView highScoreTextView = ((Activity) getContext()).findViewById(R.id.highscoreTextView);
                if (scoreTextView != null && highScoreTextView != null) {
                    scoreTextView.setText("Score: " + score);
                    highScoreTextView.setText("High Score: " + highScore);
                }
            }
        });
    }

    public void showGameOverDialog() {
        pause(); // Pause the game

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Game Over");
        builder.setPositiveButton("Play Again", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (backgroundMusicListener != null) {
                    backgroundMusicListener.playAgainMusic();
                }
                // Reset the game state and resume
                resetGame();
                resume();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Home", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ((Activity) getContext()).finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void resetGame() {
        direction = Direction.RIGHT;
        snakeBody.clear();
        snakeBody.add(new PointF(10f, 10f));
        score = 0;
        updateScoreTextView();
        randomizeFood();
        gameOver = false;
    }

    private void saveHighScore() {
        SharedPreferences preferences = getContext().getSharedPreferences("SnakeGamePrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREF_HIGH_SCORE, highScore);
        editor.apply();
    }




    private enum Direction {RIGHT, LEFT, DOWN, UP}
}

