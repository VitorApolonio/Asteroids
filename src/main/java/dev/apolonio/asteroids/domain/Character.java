package dev.apolonio.asteroids.domain;

import dev.apolonio.asteroids.AsteroidsApplication;
import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

/**
 * This is the base class for all entities in the game, containing methods for moving around
 * the screen and detecting collisions with other entities.
 */
public abstract class Character {
    private Polygon character;
    private Point2D movement;

    /**
     * Creates a new Character at the specified X and Y coordinates.
     *
     * @param polygon the Polygon that will visually represent the class.
     * @param x       the X coordinate for the Character's inicial position.
     * @param y       the Y coordinate for the Character's inicial position.
     */
    public Character(Polygon polygon, int x, int y) {
        character = polygon;
        character.setTranslateX(x);
        character.setTranslateY(y);
        
        movement = new Point2D(0, 0);
    }

    /**
     * Returns a visual representation of the character that can be shown on the screen.
     *
     * @return the character's Polygon.
     */
    public Polygon getCharacter() {
        return character;
    }

    /**
     * Rotates the character by 5 degrees counterclockwise.
     */
    public void turnLeft() {
        character.setRotate(character.getRotate() - 5);
    }

    /**
     * Rotates the character by 5 degrees clockwise.
     */
    public void turnRight() {
        character.setRotate(character.getRotate() + 5);
    }

    /**
     * Moves the character forwards.
     * <p>
     * In case moving the character places it outside the screen bounds, it will be teleported to the
     * other side of the screen, maintaining its current direction and velocity.
     */
    public void move() {
        character.setTranslateX(character.getTranslateX() + movement.getX());
        character.setTranslateY(character.getTranslateY() + movement.getY());
        
        if (character.getBoundsInParent().getMaxX() < 0) {
            character.setTranslateX(character.getBoundsInParent().getMaxX() + AsteroidsApplication.WIDTH);
        }
        
        if (character.getBoundsInParent().getMinX() > AsteroidsApplication.WIDTH) {
            character.setTranslateX(character.getBoundsInParent().getMinX() % AsteroidsApplication.WIDTH);
        }
        
        if (character.getBoundsInParent().getMaxY() < 0) {
            character.setTranslateY(character.getBoundsInParent().getMaxY() + AsteroidsApplication.HEIGHT);
        }
        
        if (character.getBoundsInParent().getMinY() > AsteroidsApplication.HEIGHT) {
            character.setTranslateY(character.getBoundsInParent().getMinY() % AsteroidsApplication.HEIGHT);
        }
    }

    /**
     * Returns the current X and Y velocity of the character.
     *
     * @return a Point2D object.
     */
    public Point2D getMovement() {
        return movement;
    }

    /**
     * Sets the X and Y velocity of the character.
     *
     * @param movement a Point2D object representing the X and Y velocity.
     */
    public void setMovement(Point2D movement) {
        this.movement = movement;
    }

    /**
     * Accelerates the character in the direction it is facing.
     */
    public void accelerate() {
        // Convert angle from degree to radians, to be used with Math.sin() and Math.cos()
        double angleInRadians = Math.toRadians(character.getRotate());

        // Calculate the change in X and Y velocity using the cosine and sine of the angles
        double dx = Math.cos(angleInRadians);
        double dy = Math.sin(angleInRadians);

        // Lower dx and dy to 6% of the original value
        dx *= 0.06;
        dy *= 0.06;

        // Apply change in velocity
        movement = movement.add(dx, dy);
    }

    /**
     * Returns whether this character has collided with the specified character.
     *
     * @param other the Character to check for a collision with.
     * @return whether the characters have collided.
     */
    public boolean collide(Character other) {
        Shape collisionArea = Shape.intersect(character, other.getCharacter());
        return collisionArea.getBoundsInLocal().getWidth() != -1;
    }
}
