package dev.apolonio.asteroids.domain;

/**
 * Represents a score value, which consists of a player name and a number of points.
 * <p>
 * Scores can be compared based on their numerical value.
 *
 * @param playerName  the player name.
 * @param playerPoints the number of points.
 */
public record Score(String playerName, int playerPoints) implements Comparable<Score> {

    @Override
    public String toString() {
        return playerName + ":" + playerPoints;
    }

    @Override
    public int compareTo(Score o) {
        return Integer.compare(o.playerPoints, playerPoints);
    }
}
