package dev.apolonio.asteroids.domain;

import java.io.Serializable;

/**
 * Represents a score value, which consists of a player name and a number of points.
 * <p>
 * Scores can be compared based on their numerical value, and serialized to be stored in a file.
 *
 * @param playerName the player name.
 * @param playerScore the number of points.
 */
public record Score(String playerName, int playerScore) implements Comparable<Score>, Serializable {

    /**
     * Returns a string representation of this score.
     *
     * @return a string representation of the score.
     */
    @Override
    public String toString() {
        return playerName + ": " + playerScore;
    }

    /**
     * Compares this score with the specified based on their point values.
     *
     * @param o the Score to be compared.
     * @return an integer representing whether this score is larger, smaller or equal to the compared.
     */
    @Override
    public int compareTo(Score o) {
        return Integer.compare(o.playerScore, playerScore);
    }
}
