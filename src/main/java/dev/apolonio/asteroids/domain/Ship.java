package dev.apolonio.asteroids.domain;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class Ship extends Character {
    
    public Ship(int x, int y) {
        super(new Polygon(-10, -11,
                -5, 0,
                -10, 11,
                20, 0), x, y);
        getCharacter().setFill(Color.WHITE);
        getCharacter().setStroke(Color.LIGHTGRAY);
        getCharacter().setStrokeWidth(2);
    }

    @Override
    public void turnLeft() {
        getCharacter().setRotate(getCharacter().getRotate() - 4.25);
    }

    @Override
    public void turnRight() {
        getCharacter().setRotate(getCharacter().getRotate() + 4.25);
    }

    @Override
    public void accelerate() {

        double changeX = Math.cos(Math.toRadians(getCharacter().getRotate()));
        double changeY = Math.sin(Math.toRadians(getCharacter().getRotate()));

        changeX *= 0.08;
        changeY *= 0.08;

        setMovement(getMovement().add(changeX, changeY));
    }
}
