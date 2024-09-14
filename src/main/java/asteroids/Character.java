package asteroids;

import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

public abstract class Character {
    private Polygon character;
    private Point2D movement;
    
    public Character(Polygon polygon, int x, int y) {
        character = polygon;
        character.setTranslateX(x);
        character.setTranslateY(y);
        
        movement = new Point2D(0, 0);
    }
    
    public Polygon getCharacter() {
        return character;
    }
    
    public void turnLeft() {
        character.setRotate(character.getRotate() - 5);
    }
    
    public void turnRight() {
        character.setRotate(character.getRotate() + 5);
    }
    
    public void move() {
        character.setTranslateX(character.getTranslateX() + movement.getX());
        character.setTranslateY(character.getTranslateY() + movement.getY());
        
        if (character.getBoundsInParent().getMaxX() < 0) {
            character.setTranslateX(character.getBoundsInParent().getMaxX() + AsteroidsApplication.WIDTH);
        }
        
        if (character.getBoundsInParent().getMinX() > AsteroidsApplication.WIDTH) {
            character.setTranslateX(character.getBoundsInParent().getMinX() % AsteroidsApplication.WIDTH);
        }
        
        if (character.getBoundsInParent().getMaxY() < 0) {
            character.setTranslateY(character.getBoundsInParent().getMaxY() + AsteroidsApplication.HEIGHT);
        }
        
        if (character.getBoundsInParent().getMinY() > AsteroidsApplication.HEIGHT) {
            character.setTranslateY(character.getBoundsInParent().getMinY() % AsteroidsApplication.HEIGHT);
        }
    }

    public Point2D getMovement() {
        return movement;
    }

    public void setMovement(Point2D movement) {
        this.movement = movement;
    }
    
    public void accelerate() {
        double changeX = Math.cos(Math.toRadians(character.getRotate()));
        double changeY = Math.sin(Math.toRadians(character.getRotate()));
        
        changeX *= 0.06;
        changeY *= 0.06;
        
        movement = movement.add(changeX, changeY);
    }
    
    public boolean collide(Character other) {
        Shape collisionArea = Shape.intersect(character, other.getCharacter());
        return collisionArea.getBoundsInLocal().getWidth() != -1;
    }
}
