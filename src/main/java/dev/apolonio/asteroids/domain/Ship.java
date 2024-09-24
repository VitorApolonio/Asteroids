package dev.apolonio.asteroids.domain;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

public class Ship extends Character {

    // This circle moves with the ship and is used to define the area in which asteroids can't spawn.
    private Circle safeZone;
    
    public Ship(int x, int y) {
        super(new Polygon(-10, -11,
                -5, 0,
                -10, 11,
                20, 0), x, y);

        safeZone = new Circle(200);
        safeZone.setCenterX(getCharacter().getTranslateX());
        safeZone.setCenterY(getCharacter().getTranslateY());
        safeZone.setFill(Color.MAGENTA);
        safeZone.setVisible(false); // Safe zone is invisible during normal gameplay

        getCharacter().setFill(Color.WHITE);
        getCharacter().setStroke(Color.LIGHTGRAY);
        getCharacter().setStrokeWidth(2);
    }

    @Override
    public void turnLeft() {
        getCharacter().setRotate(getCharacter().getRotate() - 4.5);
    }

    @Override
    public void turnRight() {
        getCharacter().setRotate(getCharacter().getRotate() + 4.5);
    }

    @Override
    public void accelerate() {

        double changeX = Math.cos(Math.toRadians(getCharacter().getRotate()));
        double changeY = Math.sin(Math.toRadians(getCharacter().getRotate()));

        changeX *= 0.07;
        changeY *= 0.07;

        setMovement(getMovement().add(changeX, changeY));
    }

    @Override
    public void move() {
        super.move();

        safeZone.setCenterX(getCharacter().getTranslateX() + getMovement().getX());
        safeZone.setCenterY(getCharacter().getTranslateY() + getMovement().getY());
    }

    public Circle getSafeZone() {
        return safeZone;
    }

    public boolean inSafeZone(Character other) {
        Shape collisionArea = Shape.intersect(safeZone, other.getCharacter());
        return collisionArea.getBoundsInLocal().getWidth() != -1;
    }
}
