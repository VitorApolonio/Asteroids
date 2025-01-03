package dev.apolonio.asteroids.domain;

import dev.apolonio.asteroids.PolygonFactory;

import java.util.Random;

/**
 * Represents an asteroid in the game.
 * <p>
 * Asteroids move around randomly in the screen, slowly rotating. They can collide with the player ship but not with
 * one another. Their shape and size is defined by their level.
 */
public class Asteroid extends Entity {
    
    private final double ROTATIONAL_MVT;
    private final int LEVEL;

    /**
     * Creates a new asteroid at position X, Y.
     *
     * @param x     the X coordinate for the asteroid position
     * @param y     the Y coordinate for the asteroid position
     * @param level the level of the asteroid
     */
    public Asteroid(double x, double y, int level) {
        // Creates a new Entity using a polygon from PolygonFactory
        super(new PolygonFactory().createPolygon(level), x, y);
        LEVEL = level;

        // Randomize initial rotation
        Random rand = new Random();
        super.getCharacter().setRotate(rand.nextInt(360));

        // Randomize acceleration
        int acceleration = 4 + rand.nextInt(10);
        
        for (int i = 0; i < acceleration; i++) {
            accelerate();
        }

        ROTATIONAL_MVT = 0.5 - rand.nextDouble();

        getCharacter().getStyleClass().add("asteroid");
    }

    /**
     * Moves the asteroid in the direction it's facing and rotates it, teleporting it if it goes past the screen bounds.
     *
     * @param scrWidth the width of the screen in pixels
     * @param scrHeight the height of the screen in pixels
     */
    @Override
    public void move(double scrWidth, double scrHeight) {
        super.move(scrWidth, scrHeight);
        super.getCharacter().setRotate(super.getCharacter().getRotate() + ROTATIONAL_MVT);
    }

    /**
     * Gets the level of this asteroid.
     * 
     * @return an {@code int} representing the asteroid level.
     */
    public int getLevel() {
        return LEVEL;
    }
}
