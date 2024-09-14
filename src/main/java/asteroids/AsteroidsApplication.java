package asteroids;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;

public class AsteroidsApplication extends Application {
    
    public static final int WIDTH = 900;
    public static final int HEIGHT = 600;
    public boolean isPaused = false;

    @Override
    public void start(Stage window) {
        
        // Create layout
        Pane mainLayout = new Pane();
        mainLayout.setPrefSize(WIDTH, HEIGHT);
        mainLayout.setStyle("-fx-background-color: black");
        
        // Create player ship
        Ship ship = new Ship(WIDTH / 2, HEIGHT / 2);
        ship.getCharacter().setFill(Color.WHITE);
        mainLayout.getChildren().add(ship.getCharacter());
        
        // Create list of asteroids and projectiles (empty for now)
        List<Asteroid> asteroids = new ArrayList<>();
        List<Projectile> projectiles = new ArrayList<>();
        
        // Create 5 asteroids at random positions
        for (int i = 0; i < 5; i++) {
            Random rand = new Random();
            Asteroid asteroid = new Asteroid(rand.nextInt(WIDTH / 3), rand.nextInt(HEIGHT));
            asteroid.getCharacter().setFill(Color.GRAY);
            asteroids.add(asteroid);
        }
        asteroids.forEach(asteroid -> mainLayout.getChildren().addFirst(asteroid.getCharacter()));
        
        // Create scene with layout
        Scene mainView = new Scene(mainLayout);
        
        /* This is a system for handling key presses. When the user presses a key, it gets set to true in the map.
           When the user releases a key, it gets set to false. This is done because the default way of handling input
           has a slight delay between detecting the first press and subsequent ones, which would make for laggy controls.
           The event listener for this is created after the animation timer. */
        Map<KeyCode, Boolean> pressedKeys = new HashMap<>();

        mainView.setOnKeyPressed(event -> pressedKeys.put(event.getCode(), Boolean.TRUE));

        mainView.setOnKeyReleased(event -> pressedKeys.put(event.getCode(), Boolean.FALSE));

        // Create user score text
        Text scoreText = new Text(10, 50, "SCORE: 0");
        scoreText.setFont(Font.font("Courier New", FontWeight.BOLD, 50));
        scoreText.setFill(Color.WHITE);
        scoreText.setStroke(Color.BLACK);
        scoreText.setStrokeWidth(3);
        mainLayout.getChildren().add(scoreText);
        AtomicInteger points = new AtomicInteger();
        
        // Create title screen layout
        VBox startPane = new VBox(HEIGHT / 6);
        startPane.setPrefSize(WIDTH, HEIGHT);
        startPane.setStyle("-fx-background-color: black");
        startPane.setAlignment(Pos.CENTER);
        
        // Create title text
        Text titleText = new Text("ASTEROIDS");
        titleText.setFont(Font.font("Verdana", FontWeight.BOLD, 90));
        titleText.setFill(Color.WHITE);
        titleText.setStroke(Color.BLACK);
        titleText.setStrokeWidth(3);
        startPane.getChildren().add(titleText);
        
        Text instruction = new Text("PRESS SPACEBAR TO START");
        instruction.setFont(Font.font("Courier New", FontWeight.BOLD, 40));
        instruction.setFill(Color.WHITE);
        instruction.setStroke(Color.BLACK);
        instruction.setStrokeWidth(1);
        startPane.getChildren().add(instruction);
        
        Scene startView = new Scene(startPane);
        
        // Create game over screen layout
        VBox endScreenPane = new VBox(HEIGHT / 6);
        endScreenPane.setPrefSize(WIDTH, HEIGHT);
        endScreenPane.setStyle("-fx-background-color: black");
        endScreenPane.setAlignment(Pos.CENTER);
        
        // Create game over text
        Text gameOverText = new Text("GAME OVER!");
        gameOverText.setFont(Font.font("Verdana", FontWeight.BOLD, 90));
        gameOverText.setFill(Color.WHITE);
        gameOverText.setStroke(Color.BLACK);
        gameOverText.setStrokeWidth(3);
        endScreenPane.getChildren().add(gameOverText);
        
        Text finalScoreText = new Text("FINAL SCORE: 0");
        finalScoreText.setFont(Font.font("Courier New", FontWeight.BOLD, 60));
        finalScoreText.setFill(Color.WHITE);
        finalScoreText.setStroke(Color.BLACK);
        finalScoreText.setStrokeWidth(3);
        endScreenPane.getChildren().add(finalScoreText);
        
        Scene endView = new Scene(endScreenPane);
        
        
        /* This timer handles all real time events, like movement of the asteroids and the ship. All code here
           gets executed about 60 times a second. */
        AnimationTimer mainTimer = new AnimationTimer() {
            
            /* This variable handles the cooldown for shots, since otherwise the player could fire
               multiple bullets at the same time. */
            private int cooldown = 0;
            
            @Override
            public void handle(long now) {
                
                // Ship movement
                if (pressedKeys.getOrDefault(KeyCode.LEFT, false) ||
                        pressedKeys.getOrDefault(KeyCode.A, false)) {
                    ship.turnLeft();
                }
                if (pressedKeys.getOrDefault(KeyCode.RIGHT, false) ||
                        pressedKeys.getOrDefault(KeyCode.D, false)) {
                    ship.turnRight();
                }
                if (pressedKeys.getOrDefault(KeyCode.UP, false) ||
                        pressedKeys.getOrDefault(KeyCode.W, false)) {
                    ship.accelerate();
                }
                
                // Projectile
                if (pressedKeys.getOrDefault(KeyCode.SPACE, false) && projectiles.size() < 3 && cooldown <= 0) {
                    Projectile proj = new Projectile(((int) ship.getCharacter().getTranslateX()), (int) ship.getCharacter().getTranslateY());
                    proj.getCharacter().setRotate(ship.getCharacter().getRotate());
                    proj.getCharacter().setFill(Color.WHITE);
                    projectiles.add(proj);
                    
                    proj.accelerate();
                    proj.setMovement(proj.getMovement().normalize().multiply(4).add(ship.getMovement().multiply(0.5)));
                    
                    mainLayout.getChildren().addFirst(proj.getCharacter());
                    
                    cooldown += 30;
                }
                
                if (cooldown > 0) {
                    cooldown -= 1;
                }
                
                // Continuously spawn asteroids starting with a 0.5% chance, raising by 0.5% more every 2000 points
                if (Math.random() < 0.005 * (1 + points.get() / 2000)) {
                    Asteroid asteroid = new Asteroid(WIDTH, HEIGHT);
                    asteroid.getCharacter().setFill(Color.GRAY);
                    if (!asteroid.collide(ship)) {
                        asteroids.add(asteroid);
                        mainLayout.getChildren().addFirst(asteroid.getCharacter());
                    }
                }
                
                // Ship and asteroid movement
                ship.move();
                asteroids.forEach(Asteroid::move);
                projectiles.forEach(Projectile::move);

                // Handle collisions between shots and asteroids
                projectiles.forEach(proj -> {
                    List<Asteroid> collisions = asteroids.stream()
                            .filter(asteroid -> asteroid.collide(proj))
                            .toList();
                    
                    // Increase score with hits
                    collisions.forEach(collided -> {
                        asteroids.remove(collided);
                        mainLayout.getChildren().remove(collided.getCharacter());
                        scoreText.setText("SCORE: " + points.addAndGet(100));
                    });
                });
                
                // Remove off-screen projectiles
                Iterator<Projectile> projIt = projectiles.iterator();
                while (projIt.hasNext()) {
                    Projectile proj = projIt.next();
                    
                    if (proj.getCharacter().getTranslateX() < 0
                            || proj.getCharacter().getTranslateX() > WIDTH
                            || proj.getCharacter().getTranslateY() < 0
                            || proj.getCharacter().getTranslateY() > HEIGHT) {
                        projIt.remove();
                        mainLayout.getChildren().remove(proj.getCharacter());
                    }
                }
                
                // End game if ship hits an asteroid
                for (Asteroid asteroid : asteroids) {
                    if (ship.collide(asteroid)) {
                        stop();
                        PauseTransition pause = new PauseTransition(Duration.seconds(1));
                        pause.setOnFinished(event -> {
                            finalScoreText.setText("FINAL SCORE: " + points.get());
                            window.setScene(endView);
                        });
                        pause.play();
                        break;
                    }
                }
            }
        };
        
        // Start game with spacebar
        startView.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                window.setScene(mainView);
                mainTimer.start();
            }
        });

        // Pause label
        Text pauseText = new Text("PAUSED");
        pauseText.setFont(Font.font("Verdana", FontWeight.BOLD, 70));
        pauseText.setFill(Color.WHITE);
        pauseText.setStroke(Color.BLACK);
        pauseText.setStrokeWidth(3);
        pauseText.setVisible(false);
        pauseText.setTranslateX(WIDTH / 2 - pauseText.getBoundsInParent().getMaxX() / 2);
        pauseText.setTranslateY(HEIGHT / 2 + pauseText.getBoundsInParent().getMaxY());
        mainLayout.getChildren().add(pauseText);

        // Pause text animation
        FadeTransition pauseTransition = new FadeTransition(Duration.seconds(0.66), pauseText);
        pauseTransition.setFromValue(0.0);
        pauseTransition.setToValue(1.0);
        pauseTransition.setAutoReverse(true);
        pauseTransition.setCycleCount(Animation.INDEFINITE);

        window.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                // Pause or unpause game if player presses escape
                case ESCAPE:
                    // Pausing only works properly on the main view, so you can't pause outside of it
                    if (window.getScene() != mainView) {
                        break;
                    }

                    if (isPaused) {
                        pauseTransition.stop();
                        pauseText.setVisible(false);
                        mainTimer.start();
                    } else {
                        pauseTransition.play();
                        pauseText.setVisible(true);
                        mainTimer.stop();
                    }
                    isPaused = !isPaused;
                    break;
                // Save a screenshot with "P" key
                case P:
                    saveScr(window.getScene());
                    break;
            }
        });

        window.setScene(startView);
        window.setTitle("Asteroids!");
        window.setResizable(false);
        window.show();
    }

    private void saveScr(Scene scene) {
        WritableImage image = new WritableImage(WIDTH, HEIGHT);
        scene.snapshot(image);

        int imageNum = 0;

        File scrDir = new File("scr");
        File imgFile;

        // Keep checking if "screenshot-n.png" exists, until some number doesn't.
        while (true) {
            File f = new File(scrDir, "screenshot-" + imageNum + ".png");
            if (f.isFile()) {
                imageNum++;
                continue;
            }
            imgFile = f;
            break;
        }

        try {
            // Create screenshots folder if it doesn't exist
            if (!scrDir.isDirectory()) {
                scrDir.mkdirs();
            }

            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", imgFile);
            System.out.println("Screenshot saved: " + imgFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save screenshot: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }

}
