package dev.apolonio.asteroids.domain;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

/**
 * Represents a projectile that can move in a straight line.
 * <p>
 * Projectiles are fired by the player ship, and unlike other characters, don't teleport to the other side of the screen
 * after leaving it.
 */
public class Projectile extends Entity {

    /**
     * Creates a new projectile at coordinates X, Y.
     *
     * @param x the X coordinate for the projectile's inicial position.
     * @param y the Y coordinate for the projectile's inicial position.
     */
    public Projectile(int x, int y) {
        // Creates a rectangle to represent the class
        super(new Polygon(10, -4,
                10, 4,
                -10, 4,
                -10, -4), x, y);
        getCharacter().setFill(Color.GHOSTWHITE);
    }

    /**
     * Moves the projectile in a straight line.
     */
    @Override
    public void move() {
        getCharacter().setTranslateX(getCharacter().getTranslateX() + getMovement().getX());
        getCharacter().setTranslateY(getCharacter().getTranslateY() + getMovement().getY());
    }
}
