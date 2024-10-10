package dev.apolonio.asteroids;

import java.util.Random;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

/**
 * This factory generates {@link Polygon Polygons} to be used in other classes.
 */
public class PolygonFactory {

    /**
     * Creates a pentagon to be used for asteroids, using the {@link Polygon} class.
     *
     * @return the generated Polygon.
     */
    public Polygon createPentagon() {
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

    /**
     * Creates a 4-point star to be used for stars using the {@link Polygon}.
     *
     * @return the generated Polygon.
     */
    public Polygon createStar() {
        // Randomize size of the star
        Random rand = new Random();
        double size = 5 + rand.nextInt(7);
        double innerSize = size / 3;

        // Create a polygon and set its style
        Polygon polygon = new Polygon();
        polygon.setFill(Color.LIGHTGRAY);

        // Calculate positions of star points
        double c1 = Math.cos(Math.PI / 4);
        double s1 = Math.sin(Math.PI / 4);

        // Add star vertices to polygon
        polygon.getPoints().addAll(0.0, -size,
                s1 * innerSize, -c1 * innerSize,
                size, 0.0,
                s1 * innerSize, c1 * innerSize,
                0.0, size,
                -s1 * innerSize, c1 * innerSize,
                -size, 0.0,
                -s1 * innerSize, -c1 * innerSize);

        return polygon;
    }
}
