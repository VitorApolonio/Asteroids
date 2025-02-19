package dev.apolonio.asteroids.domain;

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
    private final Circle SAFE_ZONE;

    /**
     * Creates a new ship at given coordinates.
     *
     * @param x the X coordinate for the ship
     * @param y the Y coordinate for the ship
     * @param zoneRadius the radius of the ship's safe zone
     */
    public Ship(double x, double y, double zoneRadius) {
        // Creates a triangle to represent the ship
        super(new Polygon(-10, -11,
                -5, 0,
                -10, 11,
                20, 0), x, y);

        // Creates the circle for the safe zone
        SAFE_ZONE = new Circle(zoneRadius);
        SAFE_ZONE.setCenterX(getCharacter().getTranslateX());
        SAFE_ZONE.setCenterY(getCharacter().getTranslateY());
        SAFE_ZONE.setVisible(false); // Safe zone is invisible during normal gameplay

        SAFE_ZONE.getStyleClass().add("ship-zone");
        getCharacter().getStyleClass().add("ship");
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

        changeX *= 0.07 * getVelocityScale().get();
        changeY *= 0.07 * getVelocityScale().get();

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

        SAFE_ZONE.setCenterX(getCharacter().getTranslateX() + getMovement().getX());
        SAFE_ZONE.setCenterY(getCharacter().getTranslateY() + getMovement().getY());
    }

    /**
     * Returns a {@link Circle} representing the ship's safe zone.
     *
     * @return the Circle representing the ship's safe zone.
     */
    public Circle getSafeZone() {
        return SAFE_ZONE;
    }

    /**
     * Returns whether the specified {@link Entity} is inside the ship's safe zone.
     *
     * @param other the Entity to check for an intersection with the ship's safe zone.
     * @return      {@code true} if the entity and the safe zone intersect, {@code false} otherwise.
     */
    public boolean inSafeZone(Entity other) {
        Shape collisionArea = Shape.intersect(SAFE_ZONE, other.getCharacter());
        return collisionArea.getBoundsInLocal().getWidth() != -1;
    }
}
