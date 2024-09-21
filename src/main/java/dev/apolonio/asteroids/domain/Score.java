package dev.apolonio.asteroids.domain;

public class Score implements Comparable<Score> {
    private final String playerName;
    private final int score;

    public Score(String playerName, int score) {
        this.playerName = playerName;
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public String getPlayerName() {
        return playerName;
    }

    @Override
    public int compareTo(Score o) {
        return Integer.compare(o.score, score);
    }
}
