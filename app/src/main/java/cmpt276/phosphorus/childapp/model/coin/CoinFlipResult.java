package cmpt276.phosphorus.childapp.model.coin;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

// ==============================================================================================
//
// Stored the result of a coin flip
//
// ==============================================================================================
public class CoinFlipResult {

    private final LocalDateTime time;
    private final CoinSide pickedSide;
    private final CoinSide flipResult;

    // Normal way to create results
    public CoinFlipResult(@NotNull CoinSide pickedSide, @NotNull CoinSide flipResult) {
        this(LocalDateTime.now(), pickedSide, flipResult);
    }

    // We might want this for saving/loading the result, not sure yet. Will leave for now
    public CoinFlipResult(@NotNull LocalDateTime time, @NotNull CoinSide pickedSide, @NotNull CoinSide flipResult) {
        this.time = Objects.requireNonNull(time, "Result cannot have a null time");
        this.pickedSide = Objects.requireNonNull(pickedSide, "Result cannot have null pickedSide");
        this.flipResult = Objects.requireNonNull(flipResult, "Result cannot have null flipSide");
    }

    public LocalDateTime getTime() {
        return this.time;
    }

    // Gets date/time in format MMMM dd HH:mm a (ex: July 14 1:35 PM)
    public String getFormattedTime() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("MMMM dd HH:mm a");
        return time.format(format);
    }

    public CoinSide getPickedSide() {
        return this.pickedSide;
    }

    public CoinSide getFlipResult() {
        return this.flipResult;
    }

    public boolean getDidWin() {
        return this.pickedSide == this.flipResult;
    }

    @NonNull
    @Override
    public String toString() {
        return "CoinFlipResult{" +
                "time=" + time +
                ", pickedSide=" + pickedSide +
                ", flipResult=" + flipResult +
                '}';
    }

}