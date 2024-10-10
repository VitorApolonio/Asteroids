package dev.apolonio.asteroids.domain;

import java.io.Serializable;

/**
 * Represents a score value, which consists of a player name and a number of points.
 * <p>
 * Scores can be compared based on their numerical value, and serialized to be stored in a file.
 *
 * @param playerName  the player name.
 * @param playerScore the number of points.
 */
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
