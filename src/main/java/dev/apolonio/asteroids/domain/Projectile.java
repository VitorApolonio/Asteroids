package dev.apolonio.asteroids.domain;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class Projectile extends Character {
    
    public Projectile(int x, int y) {
        super(new Polygon(12, -4, 12, 4, -12, 4, -12, -4), x, y);
        getCharacter().setFill(Color.GHOSTWHITE);
    }
    
    @Override
    public void move() {
        getCharacter().setTranslateX(getCharacter().getTranslateX() + getMovement().getX());
        getCharacter().setTranslateY(getCharacter().getTranslateY() + getMovement().getY());
    }
}
