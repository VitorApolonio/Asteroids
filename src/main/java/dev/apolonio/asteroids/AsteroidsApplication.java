package dev.apolonio.asteroids;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import dev.apolonio.asteroids.domain.*;
import javafx.animation.*;
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
import javafx.scene.layout.HBox;
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

    private int selectedMenuOption = 0;

    private Map<String, Integer> scores = new HashMap<>();

    @Override
    public void start(Stage window) {

        // Create title screen layout
        VBox startLayout = new VBox(HEIGHT / 8.0);
        startLayout.setPrefSize(WIDTH, HEIGHT);
        startLayout.setStyle("-fx-background-color: black");
        startLayout.setAlignment(Pos.CENTER);

        // Create title text
        Text titleText = getTextLarge("ASTEROIDS");
        startLayout.getChildren().add(titleText);

        Text instruction = getTextSmall("PRESS SPACE TO START");
        startLayout.getChildren().add(instruction);

        // Create main menu layout
        StackPane mainMenuLayout = new StackPane();
        mainMenuLayout.setPrefSize(WIDTH, HEIGHT);
        mainMenuLayout.setStyle("-fx-background-color: black");
        mainMenuLayout.setAlignment(Pos.CENTER);

        // Create menu options
        List<MenuOption> menuOptionsList = new ArrayList<>();
        MenuOption startOption = new MenuOption("START GAME");
        MenuOption settingsOption = new MenuOption("SETTINGS");
        MenuOption quitOption = new MenuOption("QUIT");

        menuOptionsList.add(startOption);
        menuOptionsList.add(settingsOption);
        menuOptionsList.add(quitOption);

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
        Text scoreText = getTextMedium("SCORE: 0");
        scoreText.setTranslateX(10);
        scoreText.setTranslateY(50);
        mainLayout.getChildren().add(scoreText);
        AtomicInteger points = new AtomicInteger();

        // Create game over screen layout
        VBox endScreenPane = new VBox(HEIGHT / 12.0);
        endScreenPane.setPrefSize(WIDTH, HEIGHT);
        endScreenPane.setStyle("-fx-background-color: black");
        endScreenPane.setAlignment(Pos.CENTER);

        // Create game over text
        Text gameOverText = getTextLarge("GAME OVER!");
        endScreenPane.getChildren().add(gameOverText);

        Text finalScoreText = getTextMedium("FINAL SCORE: 0");
        endScreenPane.getChildren().add(finalScoreText);

        Text tryAgainText = getTextSmall("PRESS SPACE TO CONTINUE");
        tryAgainText.setVisible(false);
        endScreenPane.getChildren().add(tryAgainText);

        // Pause text
        Text pauseText = getTextLarge("PAUSED");
        pauseText.setVisible(false);
        pauseText.setTranslateX(WIDTH / 2.0 - pauseText.getBoundsInParent().getMaxX() / 2);
        pauseText.setTranslateY(HEIGHT / 2.0 + pauseText.getBoundsInParent().getMaxY());
        mainLayout.getChildren().add(pauseText);

        // Pause flashing text animation
        FadeTransition pauseFade = new FadeTransition(Duration.millis(666), pauseText);
        pauseFade.setFromValue(0.0);
        pauseFade.setToValue(0.8);
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
                if (inputHandler.isHeld(KeyCode.SPACE) && cooldown <= 0 && projectiles.size() < 3) {

                    // Fires 1 projectile, or 3 if spread shot is active
                    for (int i = -15; i <= 15; i+=15) {
                        if (shotgun || i == 0) {
                            Projectile proj = new Projectile(((int) ship.getCharacter().getTranslateX()), (int) ship.getCharacter().getTranslateY());
                            proj.getCharacter().setRotate(ship.getCharacter().getRotate() + i);
                            projectiles.add(proj);

                            proj.accelerate();
                            proj.setMovement(proj.getMovement().normalize().multiply(4).add(ship.getMovement().multiply(0.5)));

                            mainLayout.getChildren().add(0, proj.getCharacter());
                        }
                    }

                    cooldown += 30;
                }

                if (cooldown > 0) {
                    cooldown -= 1;
                }

                // Continuously spawn asteroids starting with a 0.5% chance, raising by 0.5% more every 2500 points
                if (Math.random() < 0.005 * (1 + points.get() / 2500.0)) {
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
                        KeyValue scaleX = new KeyValue(collided.getCharacter().scaleXProperty(), 1.5);
                        KeyValue scaleY = new KeyValue(collided.getCharacter().scaleYProperty(), 1.5);
                        KeyValue opacity = new KeyValue(collided.getCharacter().opacityProperty(), 0);

                        KeyFrame frame = new KeyFrame(Duration.millis(500), scaleX, scaleY, opacity);

                        Timeline timeline = new Timeline(frame);
                        timeline.setOnFinished(event -> mainLayout.getChildren().remove(collided.getCharacter()));
                        timeline.play();

                        // This isn't on the animation, since otherwise you could still hit the asteroid until it finishes
                        asteroids.remove(collided);

                        // Fewer points are awarded for spread shot kills
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
                        FadeTransition deathFade = new FadeTransition(Duration.millis(1000), ship.getCharacter());
                        deathFade.setFromValue(1.0);
                        deathFade.setToValue(0.0);
                        deathFade.setOnFinished(event -> {
                            finalScoreText.setText("FINAL SCORE: " + points.get());
                            window.getScene().setRoot(endScreenPane);

                            // Delay before "PRESS SPACE" text pops up
                            PauseTransition tryAgainPause = new PauseTransition(Duration.millis(1000));
                            tryAgainPause.setOnFinished(event2 -> tryAgainText.setVisible(true));
                            tryAgainPause.play();
                        });
                        deathFade.play();
                        break;
                    }
                }
            }
        };

        // Keeps track of how many keys were pressed in the correct sequence for the spread shot cheat code
        AtomicInteger correctPresses = new AtomicInteger();
        KeyCode[] correctSequence = {
                KeyCode.UP,
                KeyCode.UP,
                KeyCode.DOWN,
                KeyCode.DOWN,
                KeyCode.LEFT,
                KeyCode.RIGHT,
                KeyCode.LEFT,
                KeyCode.RIGHT,
                KeyCode.SPACE
        };

        // This is for inputs that aren't held down, so they don't use the custom input handler
        window.addEventHandler(KeyEvent.KEY_PRESSED, event -> {

            // This is used to figure out which screen the player is at when he presses a key
            Parent windowRoot = window.getScene().getRoot();

            // Detects the sequence that toggles spread shot mode.
            if (isPaused && !shotgun) {
                if (event.getCode() == correctSequence[correctPresses.get()]) {
                    correctPresses.getAndIncrement();
                } else {
                    correctPresses.set(0);
                }

                if (correctPresses.get() == correctSequence.length) {
                    powerUpSfx.play();
                    correctPresses.set(0);
                    shotgun = true;
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

                if (selectedMenuOption > menuOptionsList.size() - 1) {
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
                    case 2:
                        window.close();
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
                mainLayout.getChildren().removeAll(asteroids.stream().map(c -> c.getCharacter()).toList());
                mainLayout.getChildren().removeAll(projectiles.stream().map(c -> c.getCharacter()).toList());
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

    private Text getTextLarge(String textContent) {
        Text text = new Text(textContent);
        text.setFont(Font.font("Verdana", FontWeight.BOLD, 90));
        text.setFill(Color.WHITE);
        text.setStroke(Color.BLUE);
        text.setStrokeWidth(3);

        return text;
    }

    private Text getTextMedium(String textContent) {
        Text text = new Text(textContent);
        text.setFont(Font.font("Trebuchet MS", FontWeight.SEMI_BOLD, 55));
        text.setFill(Color.WHITE);
        text.setStroke(Color.BLUE);
        text.setStrokeWidth(1.25);

        return text;
    }

    private Text getTextSmall(String textContent) {
        Text text = new Text(textContent);
        text.setFont(Font.font("Trebuchet MS", 40));
        text.setFill(Color.WHITE);

        return text;
    }

    private void saveScr(Scene scene) {
        WritableImage image = new WritableImage(WIDTH, HEIGHT);
        scene.snapshot(image);

        int imageNum = 0;

        String documentsFolderPath = System.getProperty("user.home") + "/Documents";
        File scrDir = new File(documentsFolderPath + "/Asteroids/Screenshots");
        File imgFile;

        // Keep checking if "screenshot-n.png" exists, until some number doesn't.
        while (true) {
            File f = new File(scrDir, "screenshot_" + imageNum + ".png");
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
