package cmpt276.phosphorus.childapp.help;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import cmpt276.phosphorus.childapp.R;

// ==============================================================================================
//
// Display information about the application
//
// ==============================================================================================
public class HelpActivity extends AppCompatActivity {

    public static Intent makeIntent(Context context) {
        return new Intent(context, HelpActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        this.setTitle(getString(R.string.help_activity_title));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        this.setLinkFunctionality();
    }

    // If user select the top left back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    private void setLinkFunctionality() {
        // https://stackoverflow.com/questions/2734270/how-to-make-links-in-a-textview-clickable
        TextView coinImgLink = findViewById(R.id.coinImgLink);
        TextView timerImgLink = findViewById(R.id.timerRelaxBgLink);
        TextView appIconLink = findViewById(R.id.appIconLink);
        TextView menuIconLink = findViewById(R.id.menuIconLink);
        TextView appBackgroundLink = findViewById(R.id.appBgLink);
        TextView victorySoundLink = findViewById(R.id.victorySoundLink);
        TextView defeatSoundLink = findViewById(R.id.defeatSoundLink);
        TextView inhaleExhaleSoundLink = findViewById(R.id.inhaleExhaleSoundLink);

        // Sets the hyperlink functionality to be clickable
        coinImgLink.setMovementMethod(LinkMovementMethod.getInstance());
        timerImgLink.setMovementMethod(LinkMovementMethod.getInstance());
        appIconLink.setMovementMethod(LinkMovementMethod.getInstance());
        menuIconLink.setMovementMethod(LinkMovementMethod.getInstance());
        appBackgroundLink.setMovementMethod(LinkMovementMethod.getInstance());
        victorySoundLink.setMovementMethod(LinkMovementMethod.getInstance());
        defeatSoundLink.setMovementMethod(LinkMovementMethod.getInstance());
        inhaleExhaleSoundLink.setMovementMethod(LinkMovementMethod.getInstance());
    }
}