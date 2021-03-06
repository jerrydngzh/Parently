package cmpt276.phosphorus.childapp.timeout;

import static cmpt276.phosphorus.childapp.timeout.utils.TimeConversionUtils.COUNT_DOWN_INTERVAL;
import static cmpt276.phosphorus.childapp.timeout.utils.TimeConversionUtils.NUM_TO_MULTI_TO_CONVERT_MIN_TO_MILLISECONDS;
import static cmpt276.phosphorus.childapp.timeout.utils.TimeConversionUtils.timeLeftFormatter;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.Objects;

import cmpt276.phosphorus.childapp.R;
import cmpt276.phosphorus.childapp.timeout.utils.TimeoutNotificationService;
import cmpt276.phosphorus.childapp.timeout.utils.TimeoutPrefConst;

// Code assisted by https://www.youtube.com/playlist?list=PLrnPJCHvNZuB8wxqXCwKw2_NkyEmFwcSd

// ==============================================================================================
//
// Represents a count down timer for the parent to use for
// a child's timeout
//
// ==============================================================================================
public class TimeoutActivity extends AppCompatActivity {

    private final float defaultTimerSpeed = 1;
    // Time is in milliseconds, 1000ms = 1s
    private long startTime = 60000;
    private long timeLeft;
    private long endTime;
    private long totalTime = 60000;
    private float newTimerSpeed = 1;
    private TextView tvCountDown;
    private RadioGroup timeGroup;
    private Button btnStartAndPause;
    private Button btnReset;

    private CountDownTimer cdTimer;
    private EditText customTimeInput;
    private boolean isTimerRunning;

    public static Intent makeIntent(Context context) {
        return new Intent(context, TimeoutActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeout);

        tvCountDown = findViewById(R.id.tvCountDown);
        customTimeInput = findViewById(R.id.inputCustomNumber);

        this.setTitle(getString(R.string.timeout_title));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        this.setUpStartAndPauseBtn();
        this.setUpResetBtn();
        this.createTimeOptions();
        this.setUpCustomInput();
        this.updateProgressBar(); // For if they come back and re-open while ticking
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_timeout, menu);
        return true;
    }

    //https://stackoverflow.com/questions/5440601/android-how-to-enable-disable-option-menu-item-on-button-click
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.timerSpeedRate).setEnabled(isTimerRunning);
        return true;
    }

    // If user select the top left back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Refactored to if statements instead of switch cases to be compatible with API 30
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        if (item.getItemId() == R.id.percent_25) {
            newTimerSpeed = 0.25f;
        } else if (item.getItemId() == R.id.percent_50) {
            newTimerSpeed = 0.50f;
        } else if (item.getItemId() == R.id.percent_75) {
            newTimerSpeed = 0.75f;
        } else if (item.getItemId() == R.id.percent_100) {
            newTimerSpeed = 1f;
        } else if (item.getItemId() == R.id.percent_200) {
            newTimerSpeed = 2f;
        } else if (item.getItemId() == R.id.percent_300) {
            newTimerSpeed = 3f;
        } else if (item.getItemId() == R.id.percent_400) {
            newTimerSpeed = 4f;
        }

        // Reset timer and start it with new speed rate
        pauseTimer();
        startTimer();

        showTimerSpeed();

        return true;
    }

    private void setUpStartAndPauseBtn() {
        btnStartAndPause = findViewById(R.id.btnStartAndPause);
        btnStartAndPause.setOnClickListener(v -> {
            if (!isTimerRunning) {
                startTimer();
            } else {
                pauseTimer();
            }
        });
    }

    private void setUpResetBtn() {
        btnReset = findViewById(R.id.btnReset);
        btnReset.setOnClickListener(v -> resetTimer());
    }

    private void startTimer() {
        // Helper variable for saving data regarding timer
        endTime = System.currentTimeMillis() + timeLeft;

        cdTimer = new CountDownTimer((long) (timeLeft / newTimerSpeed), (long) (COUNT_DOWN_INTERVAL / newTimerSpeed)) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = (long) (millisUntilFinished * newTimerSpeed);
                updateCountDownText();
                updateProgressBar();

                // For bug where notification is still showing if in activity
                stopTimeoutNotificationService();
                stopNotification(1);
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                btnStartAndPause.setText(getString(R.string.start));
                btnStartAndPause.setVisibility(View.INVISIBLE);
                setVisibilities();
                showTimeoutAlertDialog();
                updateProgressBar();
            }
        }.start();

        isTimerRunning = true;
        btnStartAndPause.setText(getString(R.string.pause));
        setVisibilities();
    }

    private void pauseTimer() {
        cdTimer.cancel();
        isTimerRunning = false;
        btnStartAndPause.setText(getString(R.string.start));
        setVisibilities();
    }

    private void resetTimer() {
        timeLeft = startTime;
        newTimerSpeed = defaultTimerSpeed;
        updateCountDownText();
        updateProgressBar();
        btnReset.setVisibility(View.INVISIBLE);
        btnStartAndPause.setVisibility(View.VISIBLE);
        btnStartAndPause.setText(getString(R.string.start));
    }

    private void setVisibilities() {
        // Either enable or disable the speed button if timer is running
        invalidateOptionsMenu();

        int currentView = isTimerRunning ? View.INVISIBLE : View.VISIBLE;
        btnReset.setVisibility(currentView);
        timeGroup.setVisibility(currentView);
        customTimeInput.setVisibility(currentView);
        showTimerSpeed();

        if (timeLeft == startTime) {
            btnReset.setVisibility(View.INVISIBLE);
        }

        if (timeLeft == 0) {
            btnStartAndPause.setVisibility(View.INVISIBLE);
        }

        TextView tvCountDown = findViewById(R.id.tvCountDown);
        int cdTextColour = isTimerRunning ? R.color.white : R.color.black;
        tvCountDown.setTextColor(ContextCompat.getColor(this, cdTextColour));

        int background = isTimerRunning ? R.drawable.relaxing_background : R.drawable.lavender_min_1;
        ConstraintLayout constraintLayout = findViewById(R.id.timeoutLayout);
        constraintLayout.setBackgroundResource(background);

        if (isTimerRunning) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void showTimerSpeed() {
        TextView tvTimerSpeed = findViewById(R.id.tvTimerSpeed);
        int timerSpeed = (int) (newTimerSpeed * 100);
        tvTimerSpeed.setText(getString(R.string.timer_speed, timerSpeed));

        int visibility = isTimerRunning ? View.VISIBLE : View.INVISIBLE;
        tvTimerSpeed.setVisibility(visibility);
    }

    private void createTimeOptions() {
        timeGroup = findViewById(R.id.radioGroupTimeOptions);
        int[] timeOptions = getResources().getIntArray(R.array.time_options);

        SharedPreferences prefs = getSharedPreferences(TimeoutPrefConst.PREFERENCE_PREF, MODE_PRIVATE);

        for (int options : timeOptions) {
            RadioButton button = new RadioButton(this);
            button.setText(getString(R.string.time_selected, options));
            button.setOnClickListener(v -> {
                startTime = options * NUM_TO_MULTI_TO_CONVERT_MIN_TO_MILLISECONDS;
                timeLeft = startTime;
                totalTime = startTime;
                newTimerSpeed = defaultTimerSpeed;
                updateCountDownText();
                setVisibilities();
                btnStartAndPause.setVisibility(View.VISIBLE);
                customTimeInput.setText("");
                button.setChecked(true);
            });
            timeGroup.addView((button));

            if (options * NUM_TO_MULTI_TO_CONVERT_MIN_TO_MILLISECONDS == prefs.getLong(TimeoutPrefConst.START_TIME, startTime)) {
                button.setChecked(true);
            }
        }
    }

    private void setUpCustomInput() {
        customTimeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String customInput = customTimeInput.getText().toString();
                timeGroup.clearCheck();
                if (customInput.isEmpty()) {
                    return;
                }

                long input = Long.parseLong(customInput);
                btnReset.setVisibility(View.INVISIBLE);
                if (input != 0) {
                    startTime = input * NUM_TO_MULTI_TO_CONVERT_MIN_TO_MILLISECONDS;
                    timeLeft = startTime;
                    totalTime = startTime;
                    newTimerSpeed = defaultTimerSpeed;
                    updateCountDownText();
                    btnStartAndPause.setVisibility(View.VISIBLE);
                } else {
                    // Unique case so not using setVisibilities()
                    timeLeft = 0;
                    updateCountDownText();
                    btnStartAndPause.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    // Turns milliseconds it is given to minutes and seconds for timer
    private void updateCountDownText() {
        String timeLeftFormatted = timeLeftFormatter(timeLeft);
        tvCountDown.setText(timeLeftFormatted);
    }

    private void updateProgressBar() {
        final double TO_PERCENT = 100.0;
        float curProgress = (float) (((double) timeLeft / (double) totalTime) * TO_PERCENT);

        final int SECOND_IN_MS = 1000;
        if (timeLeft <= SECOND_IN_MS) {
            curProgress = 0;
        }

        final long ANIMATION_DURATION = 500L;
        CircularProgressBar circularProgressBar = findViewById(R.id.timeoutCircularBtn);
        circularProgressBar.setProgressWithAnimation(curProgress, ANIMATION_DURATION);
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences prefs = getSharedPreferences(TimeoutPrefConst.PREFERENCE_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong(TimeoutPrefConst.START_TIME, startTime);
        editor.putLong(TimeoutPrefConst.TIME_LEFT, timeLeft);
        editor.putLong(TimeoutPrefConst.END_TIME, endTime);
        editor.putBoolean(TimeoutPrefConst.IS_TIMER_RUNNING, isTimerRunning);
        editor.putFloat(TimeoutPrefConst.TIMER_SPEED_RATE, newTimerSpeed);

        editor.apply();

        if (isTimerRunning) {
            startTimeoutNotificationService();
        }

        if (cdTimer != null) {
            cdTimer.cancel();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        stopTimeoutNotificationService();
        stopNotification(1);

        SharedPreferences prefs = getSharedPreferences(TimeoutPrefConst.PREFERENCE_PREF, MODE_PRIVATE);

        startTime = prefs.getLong(TimeoutPrefConst.START_TIME, NUM_TO_MULTI_TO_CONVERT_MIN_TO_MILLISECONDS);
        timeLeft = prefs.getLong(TimeoutPrefConst.TIME_LEFT, startTime);
        isTimerRunning = prefs.getBoolean(TimeoutPrefConst.IS_TIMER_RUNNING, false);
        newTimerSpeed = prefs.getFloat(TimeoutPrefConst.TIMER_SPEED_RATE, defaultTimerSpeed);
        totalTime = startTime;

        updateCountDownText();
        updateProgressBar();
        setVisibilities();

        if (isTimerRunning) {
            endTime = prefs.getLong(TimeoutPrefConst.END_TIME, 0);
            //timeLeft = endTime - System.currentTimeMillis();

            if (timeLeft < 0) {
                timeLeft = 0;
                isTimerRunning = false;
                updateCountDownText();
                setVisibilities();
                showTimeoutAlertDialog();
            } else {
                startTimer();
            }
        }
    }

    private void showTimeoutAlertDialog() {
        // For Alertdialog Vibration
        final int VIBRATION_LENGTH = 1000;
        final int NO_VIBRATION = 500;
        final int VIBRATION_AMPLITUDE = 150;
        final int NO_AMPLITUDE = 0;

        View view = LayoutInflater.from(this)
                .inflate(R.layout.timeout_finished_notification_layout, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(dialog -> {
            // Code from https://stackoverflow.com/questions/27473245/how-to-play-ringtone-sound-in-android-with-infinite-loop/27473353
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            MediaPlayer player = MediaPlayer.create(getApplicationContext(), notification);
            player.setLooping(true);
            player.start();

            // Code from https://developer.android.com/reference/android/os/Vibrator
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            long[] timings = {VIBRATION_LENGTH, NO_VIBRATION};
            int[] amplitudes = {VIBRATION_AMPLITUDE, NO_AMPLITUDE};
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, 0));

            Button btnStopTimeout = view.findViewById(R.id.btnStopTimeout);
            btnStopTimeout.setOnClickListener(v -> {
                player.stop();
                vibrator.cancel();
                dialog.dismiss();
            });
        });

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    public void startTimeoutNotificationService() {
        Intent serviceIntent = new Intent(this, TimeoutNotificationService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void stopTimeoutNotificationService() {
        Intent serviceIntent = new Intent(this, TimeoutNotificationService.class);
        stopService(serviceIntent);
    }

    public void stopNotification(int id) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }
}