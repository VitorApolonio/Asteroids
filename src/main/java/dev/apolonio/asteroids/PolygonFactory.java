package dev.apolonio.asteroids;

import java.util.Random;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

/**
 * This factory generates a {@link Polygon} to be used for the {@link dev.apolonio.asteroids.domain.Asteroid Asteroid} class.
 * <p>
 * Each polygon has 5 sides (a pentagon), with the positions of each side being slightly randomized
 * for more varied asteroid shapes.
 */
public class PolygonFactory {

    /**
     * Creates a {@link Polygon} to be used for asteroids.
     *
     * @return the generated Polygon.
     */
    public Polygon createPolygon() {
        Random rand = new Random();
        // Randomize the size of the polygon. Here, the size is the distance between the center and a vertex, in pixels.
        double size = 30 + rand.nextInt(15);

        // Create a polygon and set its style
        Polygon polygon = new Polygon();
        polygon.setFill(Color.GRAY);
        polygon.setStroke(Color.DARKGRAY);
        polygon.setStrokeWidth(3);

        // This is the formula for calculating the positions of the vertices of a regular pentagon
        double c1 = Math.cos(Math.PI * 2 / 5);
        double c2 = Math.cos(Math.PI / 5);
        double s1 = Math.sin(Math.PI * 2 / 5);
        double s2 = Math.sin(Math.PI * 4 / 5);

        // Add each vertex to the polygon
        polygon.getPoints().addAll(0.0, size,
                s1 * size, c1 * size,
                s2 * size, -c2 * size,
                -s2 * size, -c2 * size,
                -s1 * size, c1 * size);

        // Randomize slightly the position of each point
        polygon.getPoints().replaceAll(aDouble -> aDouble + rand.nextDouble(15) - 7.5);
        
        return polygon;
    }
}
