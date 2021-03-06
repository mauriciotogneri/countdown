package com.mauriciotogneri.countdown;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity
{
    private TextView upScoreTextView;
    private TextView downScoreTextView;

    private TextView upTimerTextView;
    private TextView downTimerTextView;

    // ==================================

    private boolean upPressed = false;
    private boolean downPressed = false;

    private boolean upBlocked = false;
    private boolean downBlocked = false;

    private int upTimer = 0;
    private int downTimer = 0;

    private int upScore = 0;
    private int downScore = 0;

    private boolean gameStarted = false;

    private Timer timer = new Timer();

    // ==================================

    private static final int TIMER_UP_LIMIT = 1000;
    private static final int TIMER_BOTTOM_LIMIT = -1000;

    private static final int MIN_COUNTDOWN_RATE = 8;
    private static final int MAX_COUNTDOWN_RATE = 12;

    private static final int COLOR_TIMER_NORMAL = Color.argb(255, 80, 80, 80);
    private static final int COLOR_TIMER_UNDER = Color.argb(255, 160, 0, 0);

    private static final String FIRST_LAUNCH_ATTRIBUTE = "first_launch";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initialize();

        if (firstLaunch())
        {
            Intent intent = new Intent(this, HowToPlay.class);
            startActivity(intent);
        }
    }

    private boolean firstLaunch()
    {
        SharedPreferences preferences = getSharedPreferences(MainActivity.class.toString(), Context.MODE_PRIVATE);
        boolean result = preferences.getBoolean(MainActivity.FIRST_LAUNCH_ATTRIBUTE, true);

        if (result)
        {
            SharedPreferences.Editor editor = getSharedPreferences(MainActivity.class.toString(), Context.MODE_PRIVATE).edit();
            editor.putBoolean(MainActivity.FIRST_LAUNCH_ATTRIBUTE, false);
            editor.commit();
        }

        return result;
    }

    private void initialize()
    {
        this.upScoreTextView = findViewById(R.id.score_up);
        this.upScoreTextView.setText("0");
        this.downScoreTextView = findViewById(R.id.score_down);
        this.downScoreTextView.setText("0");

        this.upTimerTextView = findViewById(R.id.timer_up);
        this.downTimerTextView = findViewById(R.id.timer_down);

        Button buttonUp = findViewById(R.id.button_up);
        buttonUp.setOnTouchListener((view, event) -> {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    buttonUpPressed();
                    break;

                case MotionEvent.ACTION_UP:
                    buttonUpReleased();
                    break;
            }

            return false;
        });

        Button buttonDown = findViewById(R.id.button_down);
        buttonDown.setOnTouchListener((view, event) -> {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    buttonDownPressed();
                    break;

                case MotionEvent.ACTION_UP:
                    buttonDownReleased();
                    break;
            }

            return false;
        });

        restartGame();
    }

    private void restartGame()
    {
        this.upTimer = MainActivity.TIMER_UP_LIMIT;
        this.downTimer = MainActivity.TIMER_UP_LIMIT;
        updateTimersTextView();

        this.upBlocked = false;
        this.downBlocked = false;

        this.gameStarted = false;

        this.timer = new Timer();
    }

    private void buttonUpPressed()
    {
        this.upPressed = true;

        checkStartGame();
    }

    private void buttonUpReleased()
    {
        this.upBlocked = true;
        this.upPressed = false;

        checkEndGame();
    }

    private void buttonDownPressed()
    {
        this.downPressed = true;

        checkStartGame();
    }

    private void buttonDownReleased()
    {
        this.downBlocked = true;
        this.downPressed = false;

        checkEndGame();
    }

    private void checkStartGame()
    {
        if ((!this.gameStarted) && (this.upPressed || this.downPressed))
        {
            restartGame();
        }

        if ((!this.gameStarted) && this.upPressed && this.downPressed)
        {
            this.gameStarted = true;

            Random random = new Random();
            final int frequency = random.nextInt((MainActivity.MAX_COUNTDOWN_RATE - MainActivity.MIN_COUNTDOWN_RATE) + 1) + MainActivity.MIN_COUNTDOWN_RATE;

            this.timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    runOnUiThread(new Runnable()
                    {
                        final int rate = (int) (frequency * 0.7f);

                        @Override
                        public void run()
                        {
                            updateTimer(this.rate);
                        }
                    });
                }
            }, 0, frequency);
        }
    }

    private void checkEndGame()
    {
        if (this.gameStarted && (!this.upPressed) && (!this.downPressed))
        {
            this.gameStarted = false;

            if ((this.upTimer >= 0) && ((this.upTimer < this.downTimer) || (this.downTimer < 0)))
            {
                this.upScore++;
            }

            if ((this.downTimer >= 0) && ((this.downTimer < this.upTimer) || (this.upTimer < 0)))
            {
                this.downScore++;
            }

            updateScoresTextView();

            cancelTimer();
        }
    }

    private void cancelTimer()
    {
        this.timer.cancel();
        this.timer.purge();
        this.timer = new Timer();
    }

    private void updateTimer(int value)
    {
        if ((this.upPressed) && (!this.upBlocked))
        {
            this.upTimer -= value;
        }

        if ((this.downPressed) && (!this.downBlocked))
        {
            this.downTimer -= value;
        }

        if ((this.upTimer < MainActivity.TIMER_BOTTOM_LIMIT) || (this.downTimer < MainActivity.TIMER_BOTTOM_LIMIT))
        {
            this.upTimer = MainActivity.TIMER_BOTTOM_LIMIT;
            this.downTimer = MainActivity.TIMER_BOTTOM_LIMIT;
            cancelTimer();
        }

        updateTimersTextView();
    }

    private void updateTimersTextView()
    {
        this.upTimerTextView.setText(String.valueOf(this.upTimer));

        if (this.upTimer < 0)
        {
            this.upTimerTextView.setTextColor(MainActivity.COLOR_TIMER_UNDER);
        }
        else
        {
            this.upTimerTextView.setTextColor(MainActivity.COLOR_TIMER_NORMAL);
        }

        this.downTimerTextView.setText(String.valueOf(this.downTimer));

        if (this.downTimer < 0)
        {
            this.downTimerTextView.setTextColor(MainActivity.COLOR_TIMER_UNDER);
        }
        else
        {
            this.downTimerTextView.setTextColor(MainActivity.COLOR_TIMER_NORMAL);
        }
    }

    private void updateScoresTextView()
    {
        this.upScoreTextView.setText(String.valueOf(this.upScore));
        this.downScoreTextView.setText(String.valueOf(this.downScore));
    }
}