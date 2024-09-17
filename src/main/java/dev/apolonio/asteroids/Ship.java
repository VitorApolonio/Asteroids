package dev.apolonio.asteroids;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class Ship extends Character {
    
    public Ship(int x, int y) {
        super(new Polygon(-10, -10, 20, 0, -10, 10), x, y);
        getCharacter().setFill(Color.WHITE);
    }
}
