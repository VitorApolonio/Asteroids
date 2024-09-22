package dev.apolonio.asteroids;

import java.util.Random;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class PolygonFactory {
    
    public Polygon createPolygon() {
        Random rand = new Random();
        
        double size = 30 + rand.nextInt(15);
        
        Polygon polygon = new Polygon();
        polygon.setFill(Color.GRAY);
        polygon.setStroke(Color.DARKGRAY);
        polygon.setStrokeWidth(3);

        double c1 = Math.cos(Math.PI * 2 / 5);
        double c2 = Math.cos(Math.PI / 5);
        double s1 = Math.sin(Math.PI * 2 / 5);
        double s2 = Math.sin(Math.PI * 4 / 5);
        
        polygon.getPoints().addAll(0.0, size,
                s1 * size, c1 * size,
                s2 * size, -c2 * size,
                -s2 * size, -c2 * size,
                -s1 * size, c1 * size);

        polygon.getPoints().replaceAll(aDouble -> aDouble + rand.nextDouble(15) - 7.5);
        
        return polygon;
    }
}
