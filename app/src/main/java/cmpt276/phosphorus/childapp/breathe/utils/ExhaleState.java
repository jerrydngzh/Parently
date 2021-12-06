package cmpt276.phosphorus.childapp.breathe.utils;

import android.animation.ObjectAnimator;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import cmpt276.phosphorus.childapp.R;
import cmpt276.phosphorus.childapp.breathe.BreatheActivity;

// ==============================================================================================
//
// Exhale state with timing button presses
//
// ==============================================================================================
public class ExhaleState extends BreatheState {

    private final android.os.Handler timerHandler = new Handler();
    private final Runnable timerRunnableThreeSeconds = this::updateBreathesLeft;
    private final Runnable timerRunnableTenSeconds = () -> {
        // TODO - stop sound
        stopAnimation();
    };

    public ExhaleState(BreatheActivity context) {
        super(context);
    }

    @Override
    public void handleEnter() {
        super.handleEnter();

        // TODO - play (different?) sound
        // TODO - update guide text

        // disable button to be touched
        Button btnBreatheState = context.findViewById(R.id.btnBreatheState);
        btnBreatheState.setEnabled(false);

        timerHandler.postDelayed(timerRunnableThreeSeconds, THREE_SECONDS);
        timerHandler.postDelayed(timerRunnableTenSeconds, TEN_SECONDS);

        stopAnimation();

        startExhaleAnimation();
    }

    @Override
    public void handleExit() {
        super.handleExit();
        timerHandler.removeCallbacks(timerRunnableThreeSeconds);
        timerHandler.removeCallbacks(timerRunnableTenSeconds);
    }

    private void updateBreathesLeft() {
        Button btnBreatheState = context.findViewById(R.id.btnBreatheState);
        btnBreatheState.setEnabled(true);
        btnBreatheState.performClick();

        context.setRemainingBreaths(context.getRemainingBreaths() - 1);
        // TODO - display updated remaining breathes

        if (context.getRemainingBreaths() > 0) {

            // TODO - update guide text
            btnBreatheState.setText(R.string.breathe_state_in);
            context.setState(context.getInhaleState());
        } else {
            // TODO - update guide text

            btnBreatheState.setText(R.string.breathe_state_finished);
            btnBreatheState.setOnClickListener(view -> {
                stopAnimation();
                context.finish();
            });
        }
    }

    private void startExhaleAnimation() {
        //https://stackoverflow.com/questions/33916287/android-scale-image-view-with-animation/33916973
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(context.getCircleAnimation(), ViewGroup.SCALE_X, 1f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(context.getCircleAnimation(), ViewGroup.SCALE_Y, 1f);

        final long ANIMATION_DURATION = TEN_SECONDS * (long) 2.5;
        scaleDownX.setDuration(ANIMATION_DURATION);
        scaleDownY.setDuration(ANIMATION_DURATION);

        animation.play(scaleDownX).with(scaleDownY);
        animation.setInterpolator(new LinearOutSlowInInterpolator());

        context.getCircleAnimation().setColorFilter(context.getColor(R.color.chalk_red_var));

        animation.start();
    }

    private void stopAnimation() {
        animation.cancel();
        animation.end();
    }
}
