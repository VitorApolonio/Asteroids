package dev.apolonio.asteroids.domain;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class Ship extends Character {
    
    public Ship(int x, int y) {
        super(new Polygon(-10, -10, 20, 0, -10, 10), x, y);
        getCharacter().setFill(Color.WHITE);
        getCharacter().setStroke(Color.LIGHTGRAY);
        getCharacter().setStrokeWidth(2);
    }

    @Override
    public void turnLeft() {
        getCharacter().setRotate(getCharacter().getRotate() - 3.75);
    }

    @Override
    public void turnRight() {
        getCharacter().setRotate(getCharacter().getRotate() + 3.75);
    }
}
