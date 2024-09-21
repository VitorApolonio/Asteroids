package dev.apolonio.asteroids.domain;

import java.io.Serializable;

public record Score(String playerName, int playerScore) implements Comparable<Score>, Serializable {

    @Override
    public String toString() {
        return playerName + ": " + playerScore;
    }

    @Override
    public int compareTo(Score o) {
        return Integer.compare(o.playerScore, playerScore);
    }
}
