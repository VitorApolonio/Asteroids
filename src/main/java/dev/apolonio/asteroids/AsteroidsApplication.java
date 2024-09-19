package dev.apolonio.asteroids;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import dev.apolonio.asteroids.domain.*;
import dev.apolonio.asteroids.domain.Character;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
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

    // Time before closing splash screen in ms
    private static final int SPLASH_SCR_TIME = 2000;

    public static final int WIDTH = 900;
    public static final int HEIGHT = 600;

    private boolean isPaused = false;
    private boolean shotgun = false;

    private AudioClip powerUpSfx;
    private AudioClip powerDownSfx;

    // Index for option menu. Set to -1 by default so pressing space won't trigger the "START" option.
    private int selectedMenuOption = 0;

    @Override
    public void start(Stage window) {

        // Create title screen layout
        VBox startLayout = new VBox(HEIGHT / 8.0);
        startLayout.setPrefSize(WIDTH, HEIGHT);
        startLayout.setStyle("-fx-background-color: black");
        startLayout.setAlignment(Pos.CENTER);

        // Create title text
        Text titleText = new Text("ASTEROIDS");
        titleText.setFont(Font.font("Verdana", FontWeight.BOLD, 90));
        titleText.setFill(Color.WHITE);
        titleText.setStroke(Color.DARKBLUE);
        titleText.setStrokeWidth(3);
        startLayout.getChildren().add(titleText);

        Text instruction = new Text("PRESS SPACE TO START");
        instruction.setFont(Font.font("Trebuchet MS", 40));
        instruction.setFill(Color.WHITE);
        startLayout.getChildren().add(instruction);

        // Create main menu layout
        StackPane mainMenuLayout = new StackPane();
        mainMenuLayout.setPrefSize(WIDTH, HEIGHT);
        mainMenuLayout.setStyle("-fx-background-color: black");
        mainMenuLayout.setAlignment(Pos.CENTER);

        // Create menu options
        List<MenuOption> menuOptionsList = new ArrayList<>();
        MenuOption startOption = new MenuOption("START");
        MenuOption settingsOption = new MenuOption("OPTIONS");
        menuOptionsList.add(startOption);
        menuOptionsList.add(settingsOption);

        VBox menuOptionsVbox = new VBox(5);
        menuOptionsVbox.getChildren().addAll(menuOptionsList.stream().map(MenuOption::getTextElement).toList());
        menuOptionsVbox.setAlignment(Pos.CENTER);
        mainMenuLayout.getChildren().add(menuOptionsVbox);

        // Create scene with layout
        Scene view = new Scene(startLayout);
        window.setScene(view);

        // Splash screen (image that shows up before game starts)
        ImageView splashImageView = new ImageView(new Image(getClass().getResourceAsStream("/images/splash.bmp")));
        Pane splashRoot = new Pane(splashImageView);
        Scene splashScene = new Scene(splashRoot, splashImageView.getImage().getWidth(), splashImageView.getImage().getHeight());

        Stage splashStage = new Stage();
        splashStage.initStyle(StageStyle.UNDECORATED);
        splashStage.setScene(splashScene);
        splashStage.show();

        new Thread(() -> {
            try {
                // Wait some time before closing splash
                Thread.sleep(SPLASH_SCR_TIME);

                // Shotgun activation sfx
                powerUpSfx = new AudioClip(getClass().getResource("/sounds/powerup.wav").toExternalForm());
                powerDownSfx = new AudioClip(getClass().getResource("/sounds/powerdown.wav").toExternalForm());

                Platform.runLater(() -> {
                    splashStage.close();
                    window.setTitle("Asteroids!");
                    window.setResizable(false); // Resizing doesn't properly work yet
                    window.show();
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }).start();

        // Create main game layout
        Pane mainLayout = new Pane();
        mainLayout.setPrefSize(WIDTH, HEIGHT);
        mainLayout.setStyle("-fx-background-color: black");

        // Create player ship
        Ship ship = new Ship(WIDTH / 2, HEIGHT / 2);
        mainLayout.getChildren().add(ship.getCharacter());

        // Create list of asteroids and projectiles (empty for now)
        List<Asteroid> asteroids = new ArrayList<>();
        List<Projectile> projectiles = new ArrayList<>();

        // Create user score text
        Text scoreText = new Text(10, 50, "SCORE: 0");
        scoreText.setFont(Font.font("Trebuchet MS", 50));
        scoreText.setFill(Color.WHITE);
        scoreText.setStroke(Color.DARKBLUE);
        scoreText.setStrokeWidth(2);
        mainLayout.getChildren().add(scoreText);
        AtomicInteger points = new AtomicInteger();

        // Create game over screen layout
        VBox endScreenPane = new VBox(HEIGHT / 12.0);
        endScreenPane.setPrefSize(WIDTH, HEIGHT);
        endScreenPane.setStyle("-fx-background-color: black");
        endScreenPane.setAlignment(Pos.CENTER);

        // Create game over text
        Text gameOverText = new Text("GAME OVER!");
        gameOverText.setFont(Font.font("Verdana", FontWeight.BOLD, 90));
        gameOverText.setFill(Color.WHITE);
        gameOverText.setStroke(Color.DARKBLUE);
        gameOverText.setStrokeWidth(3);
        endScreenPane.getChildren().add(gameOverText);

        Text finalScoreText = new Text("FINAL SCORE: 0");
        finalScoreText.setFont(Font.font("Trebuchet MS", FontWeight.SEMI_BOLD, 60));
        finalScoreText.setFill(Color.WHITE);
        endScreenPane.getChildren().add(finalScoreText);

        Text tryAgainText = new Text("PRESS SPACE TO CONTINUE");
        tryAgainText.setFont(Font.font("Trebuchet MS", 40));
        tryAgainText.setFill(Color.WHITE);
        tryAgainText.setVisible(false);
        endScreenPane.getChildren().add(tryAgainText);

        // Pause text
        Text pauseText = new Text("PAUSED");
        pauseText.setFont(Font.font("Trebuchet MS", FontWeight.BOLD, 80));
        pauseText.setFill(Color.WHITE);
        pauseText.setStroke(Color.DARKBLUE);
        pauseText.setStrokeWidth(2.5);
        pauseText.setVisible(false);
        pauseText.setTranslateX(WIDTH / 2.0 - pauseText.getBoundsInParent().getMaxX() / 2);
        pauseText.setTranslateY(HEIGHT / 2.0 + pauseText.getBoundsInParent().getMaxY());
        mainLayout.getChildren().add(pauseText);

        // Pause flashing text animation
        FadeTransition pauseFade = new FadeTransition(Duration.seconds(0.66), pauseText);
        pauseFade.setFromValue(0.0);
        pauseFade.setToValue(1.0);
        pauseFade.setAutoReverse(true);
        pauseFade.setCycleCount(Animation.INDEFINITE);

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
                    projectiles.add(proj);

                    proj.accelerate();
                    proj.setMovement(proj.getMovement().normalize().multiply(4).add(ship.getMovement().multiply(0.5)));

                    mainLayout.getChildren().add(0, proj.getCharacter());

                    // Fires 2 additional projectiles if shotgun mode is active
                    if (shotgun) {
                        Projectile proj1 = new Projectile(((int) ship.getCharacter().getTranslateX()), (int) ship.getCharacter().getTranslateY());
                        proj1.getCharacter().setRotate(ship.getCharacter().getRotate() + 10);
                        projectiles.add(proj1);

                        Projectile proj2 = new Projectile(((int) ship.getCharacter().getTranslateX()), (int) ship.getCharacter().getTranslateY());
                        proj2.getCharacter().setRotate(ship.getCharacter().getRotate() - 10);
                        projectiles.add(proj2);

                        proj1.accelerate();
                        proj1.setMovement(proj1.getMovement().normalize().multiply(4).add(ship.getMovement().multiply(0.5)));
                        proj2.accelerate();
                        proj2.setMovement(proj2.getMovement().normalize().multiply(4).add(ship.getMovement().multiply(0.5)));

                        mainLayout.getChildren().add(0, proj1.getCharacter());
                        mainLayout.getChildren().add(0, proj2.getCharacter());
                    }

                    cooldown += 30;
                }

                if (cooldown > 0) {
                    cooldown -= 1;
                }

                // Continuously spawn asteroids starting with a 0.5% chance, raising by 0.5% more every 2000 points
                if (Math.random() < 0.005 * (1 + points.get() / 3000.0)) {
                    Asteroid asteroid = new Asteroid(0, 0);
                    asteroid.setMovement(asteroid.getMovement().multiply(Math.min(10, 1 + points.get() / 5000.0))); // Increase velocity with player score up to a max of 10x speed
                    if (!asteroid.collide(ship)) {
                        asteroids.add(asteroid);
                        mainLayout.getChildren().add(0, asteroid.getCharacter());
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
                        FadeTransition asteroidFade = new FadeTransition(Duration.seconds(0.5), collided.getCharacter());
                        asteroidFade.setFromValue(1.0);
                        asteroidFade.setToValue(0.0);
                        asteroidFade.setOnFinished(event -> mainLayout.getChildren().remove(collided.getCharacter()));
                        asteroidFade.play();

                        // This isn't on the animation, since otherwise you could still hit the asteroid until it finishes
                        asteroids.remove(collided);

                        // Fewer points are awarded for shotgun kills
                        if (shotgun) {
                            scoreText.setText("SCORE: " + points.addAndGet(50));
                        } else {
                            scoreText.setText("SCORE: " + points.addAndGet(100));
                        }
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
                        shotgun = false; // Disable cheat on death

                        // Fade away animation for ship
                        FadeTransition deathFade = new FadeTransition(Duration.seconds(1), ship.getCharacter());
                        deathFade.setFromValue(1.0);
                        deathFade.setToValue(0.0);
                        deathFade.setOnFinished(event -> {
                            finalScoreText.setText("FINAL SCORE: " + points.get());
                            window.getScene().setRoot(endScreenPane);

                            // Delay before "PRESS SPACE" text pops up
                            PauseTransition tryAgainPause = new PauseTransition(Duration.seconds(1));
                            tryAgainPause.setOnFinished(event2 -> tryAgainText.setVisible(true));
                            tryAgainPause.play();
                        });
                        deathFade.play();
                        break;
                    }
                }
            }
        };

        // Keeps track of how many keys were pressed in the correct sequence for the cheat code
        AtomicInteger correctPresses = new AtomicInteger();

        // This is for inputs that aren't held down, so they don't use the custom input handler
        window.addEventHandler(KeyEvent.KEY_PRESSED, event -> {

            Parent windowRoot = window.getScene().getRoot();
            /* Detects the key sequence: UP, UP, DOWN, DOWN, LEFT, RIGHT, LEFT, RIGHT, SPACE
               Toggles shotgun mode. */
            if (isPaused) {
                if (correctPresses.get() == 0 && event.getCode() == KeyCode.UP) {
                    correctPresses.getAndIncrement();
                } else if (correctPresses.get() == 1 && event.getCode() == KeyCode.UP) {
                    correctPresses.getAndIncrement();
                } else if (correctPresses.get() == 2 && event.getCode() == KeyCode.DOWN) {
                    correctPresses.getAndIncrement();
                } else if (correctPresses.get() == 3 && event.getCode() == KeyCode.DOWN) {
                    correctPresses.getAndIncrement();
                } else if (correctPresses.get() == 4 && event.getCode() == KeyCode.LEFT) {
                    correctPresses.getAndIncrement();
                } else if (correctPresses.get() == 5 && event.getCode() == KeyCode.RIGHT) {
                    correctPresses.getAndIncrement();
                } else if (correctPresses.get() == 6 && event.getCode() == KeyCode.LEFT) {
                    correctPresses.getAndIncrement();
                } else if (correctPresses.get() == 7 && event.getCode() == KeyCode.RIGHT) {
                    correctPresses.getAndIncrement();
                } else if (correctPresses.get() == 8 && event.getCode() == KeyCode.SPACE) {
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

            // Open main menu with space bar
            if (event.getCode() == KeyCode.SPACE && windowRoot == startLayout) {
                window.getScene().setRoot(mainMenuLayout);
                menuOptionsList.get(selectedMenuOption).select();
            }

            // Select menu options
            if (event.getCode() == KeyCode.DOWN && windowRoot == mainMenuLayout) {
                menuOptionsList.get(selectedMenuOption).deselect();
                selectedMenuOption++;

                if (selectedMenuOption > 1) {
                    selectedMenuOption = 0;
                }

                menuOptionsList.get(selectedMenuOption).select();
            }
            if (event.getCode() == KeyCode.UP && windowRoot == mainMenuLayout) {
                menuOptionsList.get(selectedMenuOption).deselect();
                selectedMenuOption--;

                if (selectedMenuOption < 0) {
                    selectedMenuOption = menuOptionsList.size() - 1;
                }

                menuOptionsList.get(selectedMenuOption).select();
            }
            if (event.getCode() == KeyCode.SPACE && windowRoot == mainMenuLayout) {
                switch (selectedMenuOption) {
                    case 0:
                        window.getScene().setRoot(mainLayout);
                        mainTimer.start();
                        // Spawn 5 initial asteroids at random positions
                        for (int i = 0; i < 5; i++) {
                            Random rand = new Random();
                            Asteroid asteroid = new Asteroid(rand.nextInt(WIDTH / 3), rand.nextInt(HEIGHT));
                            asteroids.add(asteroid);
                        }
                        asteroids.forEach(asteroid -> mainLayout.getChildren().add(0, asteroid.getCharacter()));
                        break;
                    case 1:
                        System.out.println("Coming soon!");
                        break;
                }
            }

            // Save a screenshot of the current view with P key
            if (event.getCode() == KeyCode.P) {
                saveScr(window.getScene());
            }

            // Pause game with ESC key, only allowed on main view since it doesn't work properly on other scenes
            if ((event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.PAUSE) && windowRoot == mainLayout) {
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
            if (event.getCode() == KeyCode.SPACE && windowRoot == endScreenPane && tryAgainText.isVisible()) {
                // Reset selected menu option
                selectedMenuOption = 0;

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
                ship.getCharacter().setTranslateY(HEIGHT / 2.0);
                ship.getCharacter().setTranslateX(WIDTH / 2.0);
                ship.getCharacter().setRotate(0);
                ship.setMovement(new Point2D(0, 0));
                ship.getCharacter().setOpacity(1.0);

                // Go back to title screen
                window.getScene().setRoot(startLayout);
            }
        });
    }

    private void saveScr(Scene scene) {
        WritableImage image = new WritableImage(WIDTH, HEIGHT);
        scene.snapshot(image);

        int imageNum = 0;

        String documentsFolderPath = System.getProperty("user.home") + "/Documents";
        File scrDir = new File(documentsFolderPath + "/asteroids-screenshots");
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
