package cmpt276.phosphorus.childapp.coinflip;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;
import java.util.Random;

import cmpt276.phosphorus.childapp.R;
import cmpt276.phosphorus.childapp.coinflip.utils.CoinFlipAnimationDirection;
import cmpt276.phosphorus.childapp.model.child.Child;
import cmpt276.phosphorus.childapp.model.child.ChildManager;
import cmpt276.phosphorus.childapp.model.coin.CoinFlipResult;
import cmpt276.phosphorus.childapp.model.coin.CoinSide;
import cmpt276.phosphorus.childapp.model.data.DataManager;
import cmpt276.phosphorus.childapp.model.data.DataType;
import cmpt276.phosphorus.childapp.utils.Emoji;
import cmpt276.phosphorus.childapp.utils.Intents;
import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

// ==============================================================================================
//
// Allows the user to flip the coin with it deciding weither you won or lost (chosen in the ChooseSideActivity)
//
// ==============================================================================================
public class FlipCoinActivity extends AppCompatActivity {

    private static final String CHOSEN_COIN_SIDE = "CHOSEN_COIN_SIDE";

    private boolean hasFlipped = false;
    private Child child;
    private CoinSide winningSide;
    private CoinSide coinSide;
    private MediaPlayer resultSound;

    public static Intent makeIntent(Context context, Child child, CoinSide winningSide) {
        Intent intent = new Intent(context, FlipCoinActivity.class);
        intent.putExtra(CHOSEN_COIN_SIDE, winningSide.name());
        intent.putExtra(Intents.CHILD_UUID_TAG, (child != null ? child.getUUID().toString() : null));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flip_coin);

        this.setTitle(getString(R.string.flip_coin_flip_action_title));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        this.extractIntentData();
        this.coinSide = this.winningSide; // Set's the inital coin side to the one the person picked

        this.updateCoinDisplay();
        this.createFlipBtn();
    }

    // If user select the top left back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    private void randomlyChooseSide() {
        Button btnFlip = findViewById(R.id.btnFlip);
        btnFlip.setVisibility(View.INVISIBLE);

        final int MIN_FLIPS = 10;
        final int MAX_FLIPS = 15;

        Random random = new Random();
        CoinSide[] coinSide = CoinSide.values();

        CoinSide randomSide = coinSide[random.nextInt(coinSide.length)];
        int totalRandomFlips = random.nextInt((MAX_FLIPS - MIN_FLIPS) + 1) + MIN_FLIPS;

        int closetEvenNumber = Math.round(totalRandomFlips / 2) * 2;
        totalRandomFlips = (randomSide == this.coinSide) ? closetEvenNumber : closetEvenNumber + 1;

        for (int i = 1; i <= totalRandomFlips; i++) {
            int delay = getDelayBetween(i);
            // *2 the delay because flip coin rotates twice, and *i to queue them up so animations don't overlap
            (new Handler()).postDelayed(() -> flipCoin(delay), (delay * 2L) * i);
        }

        long afterAllAnimations = (getDelayBetween(totalRandomFlips) * 2L) * totalRandomFlips;
        (new Handler()).postDelayed(() -> {
            this.sideLanded();
            btnFlip.setVisibility(View.VISIBLE);
            btnFlip.setText(getString(R.string.flip_coin_finish_button_text));
        }, afterAllAnimations + 50); // Extra 50ms just in case
    }

    private int getDelayBetween(int x) {
        return (4 * x) + 25; // 4x + 25 | https://www.desmos.com/calculator/ya6hvzdgxj
    }

    private ObjectAnimator rotateCoin90Degree(CoinFlipAnimationDirection direction, int rotationDelay) {
        ImageView coinImg = findViewById(R.id.imgCoin);

        ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(FlipCoinActivity.this, direction.getAnimationId());
        anim.setTarget(coinImg);
        anim.setInterpolator(new LinearInterpolator());
        anim.setDuration(rotationDelay);
        anim.start();

        return anim;
    }

    private void flipCoin(int rotationDelay) {
        ObjectAnimator anim = this.rotateCoin90Degree(CoinFlipAnimationDirection.FORWARD, rotationDelay);

        anim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                updateCoinDisplay();
                flipCoinState();
                rotateCoin90Degree(CoinFlipAnimationDirection.BACKWARD, rotationDelay);
            }
        });
    }

    private void sideLanded() {
        // Means there aren't any avaliable children (i.e. empty)
        CoinFlipResult coinFlipResult = new CoinFlipResult(this.winningSide, this.coinSide);
        if (this.child != null) {
            this.child.addCoinFlipResult(coinFlipResult);

            ChildManager.getInstance().clearAllLastPicked(); // Just in case
            this.child.setLastPicked(true);

            DataManager.getInstance(this).saveData(DataType.CHILDREN);
        }

        boolean didWin = coinFlipResult.getDidWin();

        String toastMsg = getString((didWin ? R.string.flip_coin_win_toast : R.string.flip_coin_lose_toast))
                .replace("%happy%", Emoji.HAPPY.get())
                .replace("%sad%", Emoji.SAD.get());
        this.showLargeToast(toastMsg);

        this.resultSound = MediaPlayer.create(this, (didWin ? R.raw.victory : R.raw.defeat));
        this.resultSound.start();

        if (didWin) {
            // Ref https://github.com/DanielMartinus/Konfetti
            KonfettiView konfettiView = findViewById(R.id.viewKonfetti);
            konfettiView.build()
                    .addColors(Color.BLUE, Color.WHITE, Color.CYAN)
                    .setDirection(0.0, 359.0)
                    .setSpeed(1f, 5f)
                    .setFadeOutEnabled(true)
                    .setTimeToLive(2000L)
                    .addShapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                    .addSizes(new Size(12, 5))
                    .setPosition(-50f, konfettiView.getWidth() + 50f, -50f, -50f)
                    .streamFor(300, 5000L);
        }

    }

    private void updateCoinDisplay() {
        ImageView coinImg = findViewById(R.id.imgCoin);
        coinImg.setImageResource(this.coinSide.getImgId());

        TextView currentSide = findViewById(R.id.textCurrentSide);
        currentSide.setText(getString(this.coinSide.getTitleId()));
    }

    private void extractIntentData() {
        Intent intent = getIntent();
        String intentChildUUID = intent.getStringExtra(Intents.CHILD_UUID_TAG);

        this.winningSide = CoinSide.valueOf(intent.getStringExtra(CHOSEN_COIN_SIDE));
        this.child = ChildManager.getInstance().getChildByUUID(intentChildUUID);
    }

    private void flipCoinState() {
        this.coinSide = (this.coinSide == CoinSide.HEAD) ? CoinSide.TAILS : CoinSide.HEAD;
    }

    private void createFlipBtn() {
        Button btnFlip = findViewById(R.id.btnFlip);
        btnFlip.setOnClickListener(view -> {
            if (!this.hasFlipped) {
                this.hasFlipped = true; // Makes it so next time we press the btn we go back

                MediaPlayer mPlayer = MediaPlayer.create(this, R.raw.coin_flip);
                mPlayer.start();

                this.randomlyChooseSide();

            } else {
                this.resultSound.stop();
                finish();
            }
        });
    }

    private void showLargeToast(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        ViewGroup group = (ViewGroup) toast.getView();
        TextView messageTextView = (TextView) group.getChildAt(0);
        messageTextView.setTextSize(25);
        toast.show();
    }

}