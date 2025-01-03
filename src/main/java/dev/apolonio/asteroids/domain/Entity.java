package dev.apolonio.asteroids.domain;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

/**
 * This is the base class for all entities in the game, containing methods for moving around
 * the screen and detecting collisions with other entities.
 */
public abstract class Entity {
    private final Polygon CHARACTER;
    private Point2D movement;
    private DoubleProperty velScale;

    /**
     * Creates a new Entity at the specified X and Y coordinates.
     *
     * @param polygon the {@link Polygon} that will visually represent the class.
     * @param x       the X coordinate for the Entity's initial position.
     * @param y       the Y coordinate for the Entity's initial position.
     */
    public Entity(Polygon polygon, double x, double y) {
        CHARACTER = polygon;
        CHARACTER.setTranslateX(x);
        CHARACTER.setTranslateY(y);
        
        movement = new Point2D(0, 0);

        velScale = new SimpleDoubleProperty(1.0);
    }

    /**
     * Returns a visual representation of the Entity that can be shown on the screen.
     *
     * @return a {@link Polygon} representing the Entity visually.
     */
    public Polygon getCharacter() {
        return CHARACTER;
    }

    /**
     * Rotates the Entity by 5 degrees counterclockwise.
     */
    public void turnLeft() {
        CHARACTER.setRotate(CHARACTER.getRotate() - 5);
    }

    /**
     * Rotates the Entity by 5 degrees clockwise.
     */
    public void turnRight() {
        CHARACTER.setRotate(CHARACTER.getRotate() + 5);
    }

    /**
     * Moves the Entity forwards.
     * <p>
     * In case moving the Entity places it outside the screen bounds, it will be teleported to the
     * other side of the screen, maintaining its current direction and velocity.
     *
     * @param scrWidth the width of the screen in pixels
     * @param scrHeight the height of the screen in pixels
     */
    public void move(double scrWidth, double scrHeight) {
        CHARACTER.setTranslateX(CHARACTER.getTranslateX() + movement.getX());
        CHARACTER.setTranslateY(CHARACTER.getTranslateY() + movement.getY());

        /* This code checks if the entity is outside the screen bounds. If it is, the entity is moved into the screen
           on the opposite side. However, the entity is only moved if it is moving in the direction of the bound it has
           left (i.e. if it's outside the left side of the screen, the entity must also be moving towards the left to be
           teleported). This is done because when the entity is moved, it is actually slightly outside the screen, and so
           would be teleported again and again, from one side to the other, if there wasn't also a check for direction. */
        if (CHARACTER.getBoundsInParent().getMaxX() < 0 && movement.getX() < 0) {
            CHARACTER.setTranslateX(CHARACTER.getLayoutBounds().getMaxX() + scrWidth);
        }
        
        if (CHARACTER.getBoundsInParent().getMinX() > scrWidth && movement.getX() > 0) {
            CHARACTER.setTranslateX(CHARACTER.getLayoutBounds().getMinX() % scrWidth);
        }

        if (CHARACTER.getBoundsInParent().getMaxY() < 0 && movement.getY() < 0) {
            CHARACTER.setTranslateY(CHARACTER.getLayoutBounds().getMaxY() + scrHeight);
        }

        if (CHARACTER.getBoundsInParent().getMinY() > scrHeight && movement.getY() > 0) {
            CHARACTER.setTranslateY(CHARACTER.getLayoutBounds().getMinY() % scrHeight);
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
        double angleInRadians = Math.toRadians(CHARACTER.getRotate());

        // Calculate the change in X and Y velocity using the cosine and sine of the angles
        double dx = Math.cos(angleInRadians);
        double dy = Math.sin(angleInRadians);

        // Lower dx and dy to 6% of the original value
        dx *= 0.06 * velScale.get();
        dy *= 0.06 * velScale.get();

        // Apply change in velocity
        movement = movement.add(dx, dy);
    }

    /**
     * Returns this entity's current velocity multiplier.
     *
     * @return a {@code double} representing the current velocity scale.
     */
    public DoubleProperty getVelocityScale() {
        return velScale;
    }

    /**
     * Sets this entity's velocity multiplier.
     *
     * @param scale a {@link DoubleProperty} value for the multiplier.
     */
    public void setVelScale(DoubleProperty scale) {
        this.velScale = scale;
    }

    /**
     * Returns whether this Entity has collided with the specified Entity.
     *
     * @param other the Entity to check for a collision with.
     * @return      {@code true} if the entities have collided, {@code false} otherwise.
     */
    public boolean collide(Entity other) {
        Shape collisionArea = Shape.intersect(CHARACTER, other.getCharacter());
        return collisionArea.getBoundsInLocal().getWidth() != -1;
    }
}
