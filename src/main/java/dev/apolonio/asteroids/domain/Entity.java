package dev.apolonio.asteroids.domain;

import dev.apolonio.asteroids.AsteroidsApplication;
import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

/**
 * This is the base class for all entities in the game, containing methods for moving around
 * the screen and detecting collisions with other entities.
 */
public abstract class Entity {
    private final Polygon character;
    private Point2D movement;

    /**
     * Creates a new Entity at the specified X and Y coordinates.
     *
     * @param polygon the {@link Polygon} that will visually represent the class.
     * @param x       the X coordinate for the Entity's inicial position.
     * @param y       the Y coordinate for the Entity's inicial position.
     */
    public Entity(Polygon polygon, int x, int y) {
        character = polygon;
        character.setTranslateX(x);
        character.setTranslateY(y);
        
        movement = new Point2D(0, 0);
    }

    /**
     * Returns a visual representation of the Entity that can be shown on the screen.
     *
     * @return a {@link Polygon} representing the Entity visually.
     */
    public Polygon getCharacter() {
        return character;
    }

    /**
     * Rotates the Entity by 5 degrees counterclockwise.
     */
    public void turnLeft() {
        character.setRotate(character.getRotate() - 5);
    }

    /**
     * Rotates the Entity by 5 degrees clockwise.
     */
    public void turnRight() {
        character.setRotate(character.getRotate() + 5);
    }

    /**
     * Moves the Entity forwards.
     * <p>
     * In case moving the Entity places it outside the screen bounds, it will be teleported to the
     * other side of the screen, maintaining its current direction and velocity.
     */
    public void move() {
        character.setTranslateX(character.getTranslateX() + movement.getX());
        character.setTranslateY(character.getTranslateY() + movement.getY());

        /* This code checks if the entity is outside the screen bounds. If it is, the entity is moved into the screen
           on the opposite side. However, the entity is only moved if it is moving in the direction of the bound it has
           left (i.e. if it's outside the left side of the screen, the entity must also be moving towards the left to be
           teleported). This is done because when the entity is moved, it is actually slightly outside the screen, and so
           would be teleported again and again, from one side to the other, if there wasn't also a check for direction. */
        if (character.getBoundsInParent().getMaxX() < 0 && movement.getX() < 0) {
            character.setTranslateX(character.getLayoutBounds().getMaxX() + AsteroidsApplication.WIDTH);
        }
        
        if (character.getBoundsInParent().getMinX() > AsteroidsApplication.WIDTH && movement.getX() > 0) {
            character.setTranslateX(character.getLayoutBounds().getMinX() % AsteroidsApplication.WIDTH);
        }

        if (character.getBoundsInParent().getMaxY() < 0 && movement.getY() < 0) {
            character.setTranslateY(character.getLayoutBounds().getMaxY() + AsteroidsApplication.HEIGHT);
        }

        if (character.getBoundsInParent().getMinY() > AsteroidsApplication.HEIGHT && movement.getY() > 0) {
            character.setTranslateY(character.getLayoutBounds().getMinY() % AsteroidsApplication.HEIGHT);
        }
    }

    /**
     * Returns the current X and Y velocity of the Entity.
     *
     * @return the Entity's X and Y velocity as a {@link Point2D} .
     */
    public Point2D getMovement() {
        return movement;
    }

    /**
     * Sets the X and Y velocity of the Entity.
     *
     * @param movement a {@link Point2D} representing the X and Y velocity.
     */
    public void setMovement(Point2D movement) {
        this.movement = movement;
    }

    /**
     * Accelerates the Entity in the direction it is facing.
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
     * Returns whether this Entity has collided with the specified Entity.
     *
     * @param other the Entity to check for a collision with.
     * @return      {@code true} if the entities have collided, {@code false} otherwise.
     */
    public boolean collide(Entity other) {
        Shape collisionArea = Shape.intersect(character, other.getCharacter());
        return collisionArea.getBoundsInLocal().getWidth() != -1;
    }
}
