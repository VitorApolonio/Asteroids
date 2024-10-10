package dev.apolonio.asteroids.domain;

import dev.apolonio.asteroids.PolygonFactory;

import java.util.Random;

/**
 * Represents a star in the game.
 * <p>
 * Stars don't normally move around, only serving as background decoration.
 */
public class Star extends Entity {

    /**
     * Creates a new Star at position X, Y.
     *
     * @param x the X coordinate for the Star.
     * @param y the Y coordinate for the Star.
     */
    public Star(int x, int y) {
        super(new PolygonFactory().createStar(), x, y);

        Random rand = new Random();

        getCharacter().setRotate(rand.nextInt(360));
    }
}
