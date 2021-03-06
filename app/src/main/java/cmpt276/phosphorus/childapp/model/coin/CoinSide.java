package cmpt276.phosphorus.childapp.model.coin;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import cmpt276.phosphorus.childapp.R;

// ==============================================================================================
//
// Enum to store the two possible sides of a coin, as well as keeping track of the image ID &
// title ID
//
// ==============================================================================================
public enum CoinSide {

    HEAD(R.drawable.coin_head, R.string.flip_coin_flip_head_title),
    TAILS(R.drawable.coin_tails, R.string.flip_coin_flip_tails_title);

    private @DrawableRes final int imgId;
    private @StringRes final int titleId;


    CoinSide(@DrawableRes int imgId, @StringRes int titleId) {
        this.imgId = imgId;
        this.titleId = titleId;
    }

    public @DrawableRes int getImgId() {
        return this.imgId;
    }

    public  @StringRes int getTitleId() {
        return titleId;
    }

}
