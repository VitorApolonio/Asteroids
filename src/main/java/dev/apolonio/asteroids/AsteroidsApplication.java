package dev.apolonio.asteroids;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import javax.imageio.ImageIO;

public class AsteroidsApplication extends Application {

    private static final int SPLASH_SCR_TIME = 1500;

    public static final int WIDTH = 900;
    public static final int HEIGHT = 600;

    private boolean isPaused = false;
    private boolean shotgun = false;

    @Override
    public void start(Stage window) {
        // Shotgun activation sfx
        AudioClip powerUpSfx = new AudioClip(this.getClass().getResource("/sounds/powerup.wav").toExternalForm());
        AudioClip powerDownSfx = new AudioClip(this.getClass().getResource("/sounds/powerdown.wav").toExternalForm());

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
        
        // Create scene with layout
        Scene mainView = new Scene(mainLayout);

        // Create user score text
        Text scoreText = new Text(10, 50, "SCORE: 0");
        scoreText.setFont(Font.font("Courier New", FontWeight.BOLD, 50));
        scoreText.setFill(Color.WHITE);
        scoreText.setStroke(Color.BLACK);
        scoreText.setStrokeWidth(3);
        mainLayout.getChildren().add(scoreText);
        AtomicInteger points = new AtomicInteger();
        
        // Create title screen layout
        VBox startPane = new VBox(HEIGHT / 8);
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
        
        Text instruction = new Text("PRESS SPACE TO START");
        instruction.setFont(Font.font("Courier New", FontWeight.BOLD, 40));
        instruction.setFill(Color.WHITE);
        instruction.setStroke(Color.BLACK);
        instruction.setStrokeWidth(1);
        startPane.getChildren().add(instruction);
        
        Scene startView = new Scene(startPane);
        
        // Create game over screen layout
        VBox endScreenPane = new VBox(HEIGHT / 12);
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
        finalScoreText.setFont(Font.font("Courier New", FontWeight.BOLD, 50));
        finalScoreText.setFill(Color.WHITE);
        finalScoreText.setStroke(Color.BLACK);
        finalScoreText.setStrokeWidth(3);
        endScreenPane.getChildren().add(finalScoreText);

        Text tryAgainText = new Text("PRESS SPACE TO CONTINUE");
        tryAgainText.setFont(Font.font("Courier New", FontWeight.BOLD, 40));
        tryAgainText.setFill(Color.WHITE);
        tryAgainText.setStroke(Color.BLACK);
        tryAgainText.setStrokeWidth(1);
        tryAgainText.setVisible(false);
        endScreenPane.getChildren().add(tryAgainText);

        Scene endView = new Scene(endScreenPane);

        /* This is a system for handling key presses. When the user presses a key, it gets set to true in the map.
           When the user releases a key, it gets set to false. This is done because the default way of handling input
           has a slight delay between detecting the first press and subsequent ones, which would create a slight delay
           in ship controls. */
        InputHandler inputHandler = new InputHandler(window);
        
        /* This timer handles all real time events on the main view, like movement of the asteroids and the ship. All code here
           gets executed about 60 times a second. */
        AnimationTimer mainTimer = new AnimationTimer() {

            // This variable is a cooldown for the ship's bullets.
            private int cooldown = 0;
            
            @Override
            public void handle(long now) {

                // Ship movement
                if (inputHandler.isHeld(KeyCode.LEFT, KeyCode.A)) {
                    ship.turnLeft();
                }
                if (inputHandler.isHeld(KeyCode.RIGHT, KeyCode.D)) {
                    ship.turnRight();
                }
                if (inputHandler.isHeld(KeyCode.UP, KeyCode.W)) {
                    ship.accelerate();
                }
                
                // Projectile
                if (inputHandler.isHeld(KeyCode.SPACE) && cooldown <= 0 && projectiles.size() < (shotgun ? 6 : 3)) {

                    Projectile proj = new Projectile(((int) ship.getCharacter().getTranslateX()), (int) ship.getCharacter().getTranslateY());
                    proj.getCharacter().setRotate(ship.getCharacter().getRotate());
                    proj.getCharacter().setFill(Color.WHITE);
                    projectiles.add(proj);
                    
                    proj.accelerate();
                    proj.setMovement(proj.getMovement().normalize().multiply(4).add(ship.getMovement().multiply(0.5)));
                    
                    mainLayout.getChildren().add(proj.getCharacter());

                    // Fires 2 additional projectiles if shotgun mode is active
                    if (shotgun) {
                        Projectile proj1 = new Projectile(((int) ship.getCharacter().getTranslateX()), (int) ship.getCharacter().getTranslateY());
                        proj1.getCharacter().setRotate(ship.getCharacter().getRotate() + 10);
                        proj1.getCharacter().setFill(Color.WHITE);
                        projectiles.add(proj1);

                        Projectile proj2 = new Projectile(((int) ship.getCharacter().getTranslateX()), (int) ship.getCharacter().getTranslateY());
                        proj2.getCharacter().setRotate(ship.getCharacter().getRotate() - 10);
                        proj2.getCharacter().setFill(Color.WHITE);
                        projectiles.add(proj2);

                        proj1.accelerate();
                        proj1.setMovement(proj1.getMovement().normalize().multiply(4).add(ship.getMovement().multiply(0.5)));
                        proj2.accelerate();
                        proj2.setMovement(proj2.getMovement().normalize().multiply(4).add(ship.getMovement().multiply(0.5)));

                        mainLayout.getChildren().addAll(proj1.getCharacter(), proj2.getCharacter());
                    }
                    
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

                        // Delay before game over screen appears
                        PauseTransition deathPause = new PauseTransition(Duration.seconds(1));
                        deathPause.setOnFinished(event -> {
                            finalScoreText.setText("FINAL SCORE: " + points.get());
                            window.setScene(endView);

                            // Delay before "PRESS SPACE" text pops up
                            PauseTransition tryAgainPause = new PauseTransition(Duration.seconds(1));
                            tryAgainPause.setOnFinished(event2 -> tryAgainText.setVisible(true));

                            tryAgainPause.play();
                        });
                        deathPause.play();
                        break;
                    }
                }
            }
        };

        // Pause text
        Text pauseText = new Text("PAUSED");
        pauseText.setFont(Font.font("Verdana", FontWeight.BOLD, 70));
        pauseText.setFill(Color.WHITE);
        pauseText.setStroke(Color.BLACK);
        pauseText.setStrokeWidth(3);
        pauseText.setVisible(false);
        pauseText.setTranslateX(WIDTH / 2 - pauseText.getBoundsInParent().getMaxX() / 2);
        pauseText.setTranslateY(HEIGHT / 2 + pauseText.getBoundsInParent().getMaxY());
        mainLayout.getChildren().add(pauseText);

        // Pause flashing text animation
        FadeTransition pauseFade = new FadeTransition(Duration.seconds(0.66), pauseText);
        pauseFade.setFromValue(0.0);
        pauseFade.setToValue(1.0);
        pauseFade.setAutoReverse(true);
        pauseFade.setCycleCount(Animation.INDEFINITE);

        AtomicInteger correctPresses = new AtomicInteger();

        // This is for inputs that aren't held down, so they don't use the custom input handler
        window.addEventHandler(KeyEvent.KEY_PRESSED, event -> {

            /* Detects the key sequence: UP, DOWN, LEFT, RIGHT, SPACE
               Toggles shotgun mode. */
            if (isPaused) {
                if (correctPresses.get() == 0 && event.getCode() == KeyCode.UP) {
                    correctPresses.getAndIncrement();
                } else if (correctPresses.get() == 1 && event.getCode() == KeyCode.DOWN) {
                    correctPresses.getAndIncrement();
                } else if (correctPresses.get() == 2 && event.getCode() == KeyCode.LEFT) {
                    correctPresses.getAndIncrement();
                } else if (correctPresses.get() == 3 && event.getCode() == KeyCode.RIGHT) {
                    correctPresses.getAndIncrement();
                } else if (correctPresses.get() == 4 && event.getCode() == KeyCode.SPACE) {
                    correctPresses.set(0);
                    if (!shotgun) {
                        System.out.println("Now we're talking!");
                        shotgun = true;
                        powerUpSfx.play();
                    } else {
                        System.out.println("Fair play!");
                        shotgun = false;
                        powerDownSfx.play();
                    }
                } else {
                    correctPresses.set(0);
                }
            }

            // Start game with spacebar
            if (event.getCode() == KeyCode.SPACE && window.getScene() == startView) {

                // Spawn 5 initial asteroids at random positions
                for (int i = 0; i < 5; i++) {
                    Random rand = new Random();
                    Asteroid asteroid = new Asteroid(rand.nextInt(WIDTH / 3), rand.nextInt(HEIGHT));
                    asteroid.getCharacter().setFill(Color.GRAY);
                    asteroids.add(asteroid);
                }
                asteroids.forEach(asteroid -> mainLayout.getChildren().addFirst(asteroid.getCharacter()));

                window.setScene(mainView);
                mainTimer.start();
            }

            // Save a screenshot of the current view with P key
            if (event.getCode() == KeyCode.P) {
                saveScr(window.getScene());
            }

            // Pause game with ESC key, only allowed on mainView since it doesn't work properly on other scenes
            if ((event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.PAUSE) && window.getScene() == mainView) {
                if (isPaused) {
                    pauseFade.stop();
                    pauseText.setVisible(false);
                    mainTimer.start();
                    isPaused = false;
                } else {
                    pauseFade.play();
                    pauseText.setVisible(true);
                    mainTimer.stop();
                    isPaused = true;
                }
            }

            // Restart game
            if (event.getCode() == KeyCode.SPACE && window.getScene() == endView) {
                // Clear points
                points.set(0);
                scoreText.setText("SCORE: 0");
                finalScoreText.setText(("FINAL SCORE: 0"));

                // Delete all asteroids and projectiles
                mainLayout.getChildren().removeAll(asteroids.stream().map(Character::getCharacter).toList());
                mainLayout.getChildren().removeAll(projectiles.stream().map(Character::getCharacter).toList());
                asteroids.clear();
                projectiles.clear();

                // Center ship
                ship.getCharacter().setTranslateY(HEIGHT / 2);
                ship.getCharacter().setTranslateX(WIDTH / 2);
                ship.getCharacter().setRotate(0);
                ship.setMovement(new Point2D(0, 0));

                // Go back to title screen
                window.setScene(startView);
            }
        });

        // Splash screen
        ImageView splashImageView = new ImageView(new Image(getClass().getResourceAsStream("/images/splash.bmp")));
        Pane splashRoot = new Pane(splashImageView);
        Scene splashScene = new Scene(splashRoot, splashImageView.getImage().getWidth(), splashImageView.getImage().getHeight());

        Stage splashStage = new Stage();
        splashStage.initStyle(StageStyle.UNDECORATED);
        splashStage.setScene(splashScene);
        splashStage.show();

        // Wait a while, then close the splash and show the main window
        PauseTransition splashTransition = new PauseTransition(Duration.millis(SPLASH_SCR_TIME));
        splashTransition.setOnFinished(event -> {
            splashStage.close();
            window.setScene(startView);
            window.setTitle("Asteroids!");
            window.setResizable(false); // Resizing doesn't properly work yet
            window.show();
        });
        splashTransition.play();
    }

    private void saveScr(Scene scene) {
        WritableImage image = new WritableImage(WIDTH, HEIGHT);
        scene.snapshot(image);

        int imageNum = 0;

        File scrDir = new File("screenshots");
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
