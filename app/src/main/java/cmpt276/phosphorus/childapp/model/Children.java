package cmpt276.phosphorus.childapp.model;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class Children {

    private final UUID uuid;
    private final List<CoinFlipResult> coinFlipResults;
    private String name;

    // Normal way to create users
    public Children(@NotNull String name) {
        this(UUID.randomUUID(), new ArrayList<>(), name);
    }

    // We might want this for saving/loading the children, not sure yet. Will leave for now
    public Children(@NotNull UUID uuid, @NotNull List<CoinFlipResult> coinFlipResults, @NotNull String name) {
        this.uuid = Objects.requireNonNull(uuid, "Children UUID cannot be null");
        this.coinFlipResults = Objects.requireNonNull(coinFlipResults, "Children flips results cannot be null");
        this.setName(name); // setName already makes sure name isn't null || empty
    }

    public int getTotalLosses() {
        // Ex 10 flips:
        // 10 flips - 7 wins = 3 losses
        return this.coinFlipResults.size() - this.getTotalWins();
    }

    public int getTotalWins() {
        return (int) this.coinFlipResults.stream().filter(CoinFlipResult::getDidWin).count();
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public void setName(@NotNull String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }

        this.name = name;
    }

    // Returns sorted coin resaults from Oldest -> Newest
    public List<CoinFlipResult> getCoinFlipResults() {
        return this.coinFlipResults.stream().sorted(Comparator.comparing(CoinFlipResult::getTime)).collect(Collectors.toList());
    }

    public void addCoinFlipResault(@NotNull CoinFlipResult coinFlipResult) {
        this.coinFlipResults.add(Objects.requireNonNull(coinFlipResult, "Cannot add a null coin resualt"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Children children = (Children) o;
        return Objects.equals(this.uuid, children.getUUID()) && Objects.equals(this.name, children.getName());
    }

    @Override
    public String toString() {
        return "Children{" +
                "uuid=" + this.uuid +
                ", name='" + this.name + '\'' +
                ", coinFlipResults=" + this.coinFlipResults +
                '}';
    }

}
