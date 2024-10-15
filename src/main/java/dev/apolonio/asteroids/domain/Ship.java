package dev.apolonio.asteroids.domain;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

/**
 * Represents the spaceship controlled by the player.
 * <p>
 * The spaceship can move in the screen, and has a safe zone around it where asteroids can't spawn.
 */
public class Ship extends Entity {

    // This circle moves with the ship and is used to define the area in which asteroids can't spawn.
    private final Circle safeZone;

    /**
     * Creates a new ship at given coordinates.
     *
     * @param x the X coordinate for the ship
     * @param y the Y coordinate for the ship
     */
    public Ship(double x, double y) {
        // Creates a triangle to represent the ship
        super(new Polygon(-10, -11,
                -5, 0,
                -10, 11,
                20, 0), x, y);

        // Creates the circle for the safe zone
        safeZone = new Circle(200);
        safeZone.setCenterX(getCharacter().getTranslateX());
        safeZone.setCenterY(getCharacter().getTranslateY());
        safeZone.setFill(Color.MAGENTA);
        safeZone.setVisible(false); // Safe zone is invisible during normal gameplay

        // Set style for ship polygon
        getCharacter().setFill(Color.WHITE);
        getCharacter().setStroke(Color.LIGHTGRAY);
        getCharacter().setStrokeWidth(2);
    }

    /**
     * Rotates the ship counterclockwise by 4.5 degrees
     */
    @Override
    public void turnLeft() {
        getCharacter().setRotate(getCharacter().getRotate() - 4.5);
    }

    /**
     * Rotates the ship clockwise by 4.5 degrees
     */
    @Override
    public void turnRight() {
        getCharacter().setRotate(getCharacter().getRotate() + 4.5);
    }

    /**
     * Accelerates the ship in the direction it is facing.
     */
    @Override
    public void accelerate() {

        double changeX = Math.cos(Math.toRadians(getCharacter().getRotate()));
        double changeY = Math.sin(Math.toRadians(getCharacter().getRotate()));

        changeX *= 0.07;
        changeY *= 0.07;

        setMovement(getMovement().add(changeX, changeY));
    }

    /**
     * Moves the ship and its safe zone, teleporting both to the other side of the screen if they move outside it.
     *
     * @param scrWidth the width of the screen in pixels
     * @param scrHeight the height of the screen in pixels
     */
    @Override
    public void move(double scrWidth, double scrHeight) {
        super.move(scrWidth, scrHeight);

        safeZone.setCenterX(getCharacter().getTranslateX() + getMovement().getX());
        safeZone.setCenterY(getCharacter().getTranslateY() + getMovement().getY());
    }

    /**
     * Returns a {@link Circle} representing the ship's safe zone.
     *
     * @return the Circle representing the ship's safe zone.
     */
    public Circle getSafeZone() {
        return safeZone;
    }

    /**
     * Returns whether the specified {@link Entity} is inside the ship's safe zone.
     *
     * @param other the Entity to check for an intersection with the ship's safe zone.
     * @return      {@code true} if the entity and the safe zone intersect, {@code false} otherwise.
     */
    public boolean inSafeZone(Entity other) {
        Shape collisionArea = Shape.intersect(safeZone, other.getCharacter());
        return collisionArea.getBoundsInLocal().getWidth() != -1;
    }
}
