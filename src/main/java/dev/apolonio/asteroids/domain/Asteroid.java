package dev.apolonio.asteroids.domain;

import dev.apolonio.asteroids.PolygonFactory;

import java.util.Random;

/**
 * Represents an asteroid in the game.
 * <p>
 * Asteroids move around randomly in the screen, slowly rotating. They can collide with the player ship but not with
 * one another.
 */
public class Asteroid extends Entity {
    
    private double rotationalMovement;

    /**
     * Creates a new asteroid at position X, Y.
     *
     * @param x the X coordinate for the asteroid position
     * @param y the Y coordinate for the asteroid position
     */
    public Asteroid(int x, int y) {
        // Creates a new Entity using a polygon from PolygonFactory
        super(new PolygonFactory().createPolygon(), x, y);

        // Randomize inicial rotation
        Random rand = new Random();
        super.getCharacter().setRotate(rand.nextInt(360));

        // Randomize acceleration
        int acceleration = 4 + rand.nextInt(10);
        
        for (int i = 0; i < acceleration; i++) {
            accelerate();
        }

        // Randomize rotational velocity
        rotationalMovement = 0.5 - rand.nextDouble();
    }

    /**
     * Moves the asteroid in the direction it's facing and rotates it.
     */
    @Override
    public void move() {
        super.move();
        super.getCharacter().setRotate(super.getCharacter().getRotate() + rotationalMovement);
    }
}
