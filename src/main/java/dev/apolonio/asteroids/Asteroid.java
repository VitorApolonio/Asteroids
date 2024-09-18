package dev.apolonio.asteroids;

import java.util.Random;

public class Asteroid extends Character {
    
    private double rotationalMovement;
    
    public Asteroid(int x, int y) {
        super(new PolygonFactory().createPolygon(), x, y);
        
        Random rand = new Random();
        
        super.getCharacter().setRotate(rand.nextInt(360));
        
        int acceleration = 4 + rand.nextInt(10);
        
        for (int i = 0; i < acceleration; i++) {
            accelerate();
        }
        
        rotationalMovement = 0.5 - rand.nextDouble();
    }
    
    @Override
    public void move() {
        super.move();
        super.getCharacter().setRotate(super.getCharacter().getRotate() + rotationalMovement);
    }
}
