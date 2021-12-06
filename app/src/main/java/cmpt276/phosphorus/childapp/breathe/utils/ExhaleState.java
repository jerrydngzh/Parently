package cmpt276.phosphorus.childapp.breathe.utils;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import cmpt276.phosphorus.childapp.R;
import cmpt276.phosphorus.childapp.breathe.BreatheActivity;

public class ExhaleState extends BreatheState {
//    private final Runnable timerRunnableThreeSeconds = this::updateBreathesLeft;
//    private final Runnable timerRunnableTenSeconds = () -> {
//        // TODO (jack) - stop sound
//        stopAnimation();
//
//        // black = exhale ten seconds runnable stops
//        context.getCircleAnimationView().setColorFilter(context.getColor(R.color.black));
//    };

    public ExhaleState(BreatheActivity context) {
        super(context);
    }

    @Override
    public void handleEnter() {
        super.handleEnter();

        // TODO (jack) - play mc sound {0:10-0:20}
        // TODO (jack) - update guide text

        // disable button to be touched
        Button btnBreatheState = context.findViewById(R.id.btnBreatheState);
        btnBreatheState.setEnabled(false);

//        timerHandler.postDelayed(timerRunnableThreeSeconds, THREE_SECONDS);
//        timerHandler.postDelayed(timerRunnableTenSeconds, TEN_SECONDS);

        initializeExhaleCountDownTimer();

        stopAnimation();
        startExhaleAnimation();
    }

    private void initializeExhaleCountDownTimer() {
        timer = new CountDownTimer(TEN_SECONDS, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(millisUntilFinished == TEN_SECONDS - THREE_SECONDS){
                    updateBreathesLeft();
                }
            }

            @Override
            public void onFinish() {
                stopAnimation();

                // black = exhale ten seconds runnable stops
                context.getCircleAnimationView().setColorFilter(context.getColor(R.color.black));
            }
        };
    }

    @Override
    public void handleExit() {
        super.handleExit();
//        timerHandler.removeCallbacks(timerRunnableThreeSeconds);

    }

    private void updateBreathesLeft() {
        Button btnBreatheState = context.findViewById(R.id.btnBreatheState);
        btnBreatheState.setEnabled(true);

        context.setRemainingBreaths(context.getRemainingBreaths() - 1);
        context.getRemainBreathsView().setText(
                context.getString(R.string.remaining_breaths_text, context.getRemainingBreaths()));

        if (context.getRemainingBreaths() > 0) {

            // TODO (jack) - update guide text
            btnBreatheState.setText(R.string.breathe_state_in);
            context.setState(context.getInhaleState());
        } else {
            // TODO (jack) - update guide text

            btnBreatheState.setText(R.string.breathe_state_finished);
            btnBreatheState.setOnClickListener(view -> {
//                stopAnimation();
                context.finish();
            });
        }
    }

    private void startExhaleAnimation() {
        //https://stackoverflow.com/questions/33916287/android-scale-image-view-with-animation/33916973
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(context.getCircleAnimationView(), ViewGroup.SCALE_X, 1f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(context.getCircleAnimationView(), ViewGroup.SCALE_Y, 1f);

        final long animationDuration = TEN_SECONDS * (long) 2.5;
        scaleDownX.setDuration(animationDuration);
        scaleDownY.setDuration(animationDuration);

        animation.play(scaleDownX).with(scaleDownY);
        animation.setInterpolator(new LinearOutSlowInInterpolator());
        context.getCircleAnimationView().setColorFilter(context.getColor(R.color.chalk_red_var));

        animation.start();
    }

    private void stopAnimation() {
        animation.cancel();
        animation.end();
    }
}
