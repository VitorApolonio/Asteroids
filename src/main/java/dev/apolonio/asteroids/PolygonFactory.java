package dev.apolonio.asteroids;

import javafx.scene.shape.Polygon;

import java.util.Random;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

/**
 * This factory generates {@link Polygon Polygons} to be used in other classes.
 */
public class PolygonFactory {

    /**
     * Creates a polygon to be used for asteroids of the specified level, using the {@link Polygon} class.
     * <p>
     * The asteroid level must be at least 1, and polygons are generated with the following logic:
     * for an asteroid of size n, a polygon of {@code 2 * level + 1} sides will be generated.
     * <p>
     * The size of a polygon will be determined according to a normal distribution where the mean is given by
     * {@code 25 * sqrt(level)} and the standard deviation by {@code level}.
     * @param level the level of the asteroid.
     * @return      the generated Polygon.
     */
    public Polygon createPolygon(int level) {
        if (level < 1) {
            throw new IllegalArgumentException("Asteroid level must be greater than 0.");
        }

        Random rand = new Random();
        // Please excuse all the magic numbers, I just plugged random functions into GeoGebra until things looked right.
        double size = rand.nextGaussian(25 * sqrt(level), level);
        int vertices = 2 * level + 1;
        Polygon polygon = new Polygon();

        // This here figures out the x and y coords for an n-sided regular polygon using trigonometry, isn't math fun?
        double angleStep = 2 * PI / vertices;
        for (int i = 0; i < vertices; i++) {
            polygon.getPoints().addAll(sin(i * angleStep) * size, -cos(i * angleStep) * size);
        }

        // Randomize slightly the position of each vertex
        polygon.getPoints().replaceAll(p -> p + rand.nextDouble(10 * sqrt(level)));
        
        return polygon;
    }

    /**
     * Creates a 4-point star to be used in the {@link dev.apolonio.asteroids.domain.Star Star} class.
     * <p>
     * The generated shape will be a star-shaped {@link Polygon}, with its size randomized according to a normal
     * distribution and a proportion of {@code 1:3} for its inner size.
     * @return the generated Polygon.
     */
    public Polygon createStar() {
        // Randomize size of the star
        Random rand = new Random();
        double size = rand.nextGaussian(5.0, 2.5);
        double innerSize = size / 3;

        Polygon polygon = new Polygon();

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
