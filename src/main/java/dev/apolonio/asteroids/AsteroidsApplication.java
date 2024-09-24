package dev.apolonio.asteroids;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import dev.apolonio.asteroids.domain.Asteroid;
import dev.apolonio.asteroids.domain.Character;
import dev.apolonio.asteroids.domain.MenuOption;
import dev.apolonio.asteroids.domain.Projectile;
import dev.apolonio.asteroids.domain.Score;
import dev.apolonio.asteroids.domain.Ship;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import javax.imageio.ImageIO;

public class AsteroidsApplication extends Application {

    // Time before closing splash screen in ms
    private static final int SPLASH_SCR_TIME = 2000;

    public static final int WIDTH = 900;
    public static final int HEIGHT = 600;

    // Folder for storing screenshots and score data
    private static final String GAME_DATA_FOLDER_PATH = System.getProperty("user.home") + "/Documents/Asteroids/";

    private List<Score> scoreList;

    private boolean isPaused = false;
    private boolean shotgun = false;

    private MediaPlayer menuSelectSfx;
    private MediaPlayer menuConfirmSfx;
    private MediaPlayer pauseSfx;
    private MediaPlayer unpauseSfx;

    private MediaPlayer fireSfx;
    private MediaPlayer spreadFireSfx;
    private MediaPlayer powerUpSfx;
    private MediaPlayer asteroidSfx;
    private MediaPlayer deathSfx;

    private int selectedMenuOption = 0;

    @Override
    public void start(Stage window) {

        // Load scores from file
        try {
            loadScores();
        } catch (ClassNotFoundException e) {
            System.err.println("[DEBUG] Failed to load scores: " + e.getMessage());
        }

        // Create title screen layout
        VBox startLayout = new VBox(HEIGHT / 8.0);
        startLayout.setPrefSize(WIDTH, HEIGHT);
        startLayout.setStyle("-fx-background-color: black;");
        startLayout.setAlignment(Pos.CENTER);

        // Create title text
        Text titleText = getTextLarge("ASTEROIDS");
        startLayout.getChildren().add(titleText);

        Text pressToStart = getTextSmall("PRESS SPACE TO START");
        startLayout.getChildren().add(pressToStart);

        // Create main menu layout
        StackPane mainMenuLayout = new StackPane();
        mainMenuLayout.setPrefSize(WIDTH, HEIGHT);
        mainMenuLayout.setStyle("-fx-background-color: black;");
        mainMenuLayout.setAlignment(Pos.CENTER);

        // Create menu options
        List<MenuOption> menuOptionsList = new ArrayList<>();
        MenuOption startOption = new MenuOption("START GAME");
        MenuOption leaderboardOption = new MenuOption("HI-SCORES");
        MenuOption quitOption = new MenuOption("QUIT");

        menuOptionsList.add(startOption);
        menuOptionsList.add(leaderboardOption);
        menuOptionsList.add(quitOption);

        VBox menuOptionsVbox = new VBox(HEIGHT / 40.0);
        menuOptionsVbox.getChildren().addAll(menuOptionsList.stream().map(MenuOption::getTextElement).toList());
        menuOptionsVbox.setAlignment(Pos.CENTER);
        mainMenuLayout.getChildren().add(menuOptionsVbox);

        // Create leaderboard layout
        BorderPane leaderboardLayout = new BorderPane();
        leaderboardLayout.setStyle("-fx-background-color: black;");
        leaderboardLayout.setPrefSize(WIDTH, HEIGHT);

        Text hiScoresText = getTextMedium("HIGH SCORES");

        VBox leaderboardLeftVbox = new VBox(HEIGHT / 20.0);
        leaderboardLeftVbox.setAlignment(Pos.CENTER);
        leaderboardLeftVbox.setPrefSize(WIDTH / 2.0, HEIGHT);

        VBox leaderboardRightVbox = new VBox(HEIGHT / 20.0);
        leaderboardRightVbox.setAlignment(Pos.CENTER);
        leaderboardRightVbox.setPrefSize(WIDTH / 2.0, HEIGHT);

        // Fill leaderboards
        List<Text> scoreTexts = getHiScoreTexts();

        for (int i = 0; i < 10; i++) {
            Text scoreText = scoreTexts.get(i);

            if (i < 5) {
                leaderboardLeftVbox.getChildren().add(scoreText);
            } else {
                leaderboardRightVbox.getChildren().add(scoreText);
            }
        }

        MenuOption backOption = new MenuOption("BACK");

        leaderboardLayout.setTop(hiScoresText);
        leaderboardLayout.setLeft(leaderboardLeftVbox);
        leaderboardLayout.setRight(leaderboardRightVbox);
        leaderboardLayout.setBottom(backOption.getTextElement());

        BorderPane.setAlignment(hiScoresText, Pos.CENTER);
        BorderPane.setAlignment(backOption.getTextElement(), Pos.CENTER);
        leaderboardLayout.setPadding(new Insets(20, 0, 20, 0));

        // Create scene with layout
        Scene view = new Scene(startLayout);
        window.setScene(view);

        // Splash screen (image that shows up before game starts)
        ImageView splashImageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/splash.bmp"))));
        Pane splashRoot = new Pane(splashImageView);
        Scene splashScene = new Scene(splashRoot, splashImageView.getImage().getWidth(), splashImageView.getImage().getHeight());

        Stage splashStage = new Stage();
        splashStage.initStyle(StageStyle.UNDECORATED);
        splashStage.setScene(splashScene);
        splashStage.show();

        new Thread(() -> {
            try {
                // Menu sounds
                menuSelectSfx = new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource("/sounds/menu_select.mp3")).toExternalForm()));
                menuConfirmSfx = new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource("/sounds/menu_confirm.mp3")).toExternalForm()));
                pauseSfx = new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource("/sounds/pause.mp3")).toExternalForm()));
                unpauseSfx = new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource("/sounds/unpause.mp3")).toExternalForm()));

                // Main game sfx
                fireSfx = new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource("/sounds/fire.mp3")).toExternalForm()));
                spreadFireSfx = new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource("/sounds/fire_spread.mp3")).toExternalForm()));
                powerUpSfx = new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource("/sounds/power_up.mp3")).toExternalForm()));
                asteroidSfx = new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource("/sounds/asteroid_break.mp3")).toExternalForm()));
                deathSfx = new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource("/sounds/death.mp3")).toExternalForm()));

                // Lower volume of loud sounds
                fireSfx.setVolume(0.20);
                spreadFireSfx.setVolume(0.20);

                // Preload menu confirm sound. This is done so there isn't a delay when you first open the main menu.
                menuConfirmSfx.play();
                menuConfirmSfx.stop();

                // Wait some time
                Thread.sleep(SPLASH_SCR_TIME);

                // Close splash and show window when done
                Platform.runLater(() -> {
                    splashStage.close();
                    window.setTitle("Asteroids!");
                    window.setResizable(false); // Resizing doesn't properly work yet, fixing it would require making all menus responsive
                    window.show();
                });

            } catch (InterruptedException e) {
                System.err.println("[DEBUG] Splash thread interrupted: " + e.getMessage());
            }
        }).start();

        // Create main game layout
        Pane mainLayout = new Pane();
        mainLayout.setPrefSize(WIDTH, HEIGHT);
        mainLayout.setStyle("-fx-background-color: black;");

        // Create player ship
        Ship ship = new Ship(WIDTH / 2, HEIGHT / 2);
        mainLayout.getChildren().add(ship.getSafeZone());
        mainLayout.getChildren().add(ship.getCharacter());

        // Create list of asteroids and projectiles (empty for now)
        List<Asteroid> asteroids = new ArrayList<>();
        List<Projectile> projectiles = new ArrayList<>();

        // Create user score text
        Text scoreText = getTextMedium("SCORE: 0");
        scoreText.setTranslateX(30);
        scoreText.setTranslateY(60);
        mainLayout.getChildren().add(scoreText);
        AtomicInteger points = new AtomicInteger();

        // Create game over screen layout
        VBox endScreenLayout = new VBox(HEIGHT / 12.0);
        endScreenLayout.setPrefSize(WIDTH, HEIGHT);
        endScreenLayout.setStyle("-fx-background-color: black;");
        endScreenLayout.setAlignment(Pos.CENTER);

        // Create game over text
        Text gameOverText = getTextLarge("GAME OVER!");
        endScreenLayout.getChildren().add(gameOverText);

        Text finalScoreText = getTextMedium("FINAL SCORE: 0");
        endScreenLayout.getChildren().add(finalScoreText);

        Text tryAgainText = getTextSmall("PRESS SPACE TO CONTINUE");
        tryAgainText.setVisible(false);
        endScreenLayout.getChildren().add(tryAgainText);

        // Create insert name screen
        VBox insertNameLayout = new VBox(HEIGHT / 12.0);
        insertNameLayout.setPrefSize(WIDTH, HEIGHT);
        insertNameLayout.setStyle("-fx-background-color: black;");
        insertNameLayout.setAlignment(Pos.CENTER);

        // Create insert name text
        Text instructionText = getTextMedium("ENTER YOUR NAME");
        insertNameLayout.getChildren().add(instructionText);

        StringBuilder initialsSB = new StringBuilder("___");

        Text initialsText = getTextScore(initialsSB.toString().replace("", " ").strip());
        initialsText.setStyle("-fx-font-size: 70;");
        insertNameLayout.getChildren().add(initialsText);

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
                        if (shotgun) {
                            spreadFireSfx.seek(Duration.ZERO);
                            spreadFireSfx.play();
                        } else {
                            fireSfx.seek(Duration.ZERO);
                            fireSfx.play();
                        }

                        if (shotgun || i == 0) {
                            Projectile proj = new Projectile(((int) ship.getCharacter().getTranslateX()), (int) ship.getCharacter().getTranslateY());
                            proj.getCharacter().setRotate(ship.getCharacter().getRotate() + i);
                            projectiles.add(proj);

                            proj.accelerate();
                            proj.setMovement(proj.getMovement().normalize().multiply(4).add(ship.getMovement()));

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
                    Asteroid asteroid = new Asteroid((int) (Math.random() * WIDTH / 3), (int) (Math.random() * HEIGHT / 2));
                    asteroid.setMovement(asteroid.getMovement().multiply(Math.min(3, 1 + points.get() / 8000.0))); // Increase velocity with player score up to a max of 3x speed
                    if (!ship.inSafeZone(asteroid)) {
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

                        KeyFrame frame = new KeyFrame(Duration.millis(333), scaleX, scaleY, opacity);

                        Timeline timeline = new Timeline(frame);
                        timeline.setOnFinished(event -> mainLayout.getChildren().remove(collided.getCharacter()));
                        timeline.play();

                        // This isn't on the animation, since otherwise you could still hit the asteroid until it finishes
                        asteroids.remove(collided);

                        // Play sound
                        asteroidSfx.seek(Duration.ZERO);
                        asteroidSfx.play();

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

                        // Play sound
                        deathSfx.seek(Duration.ZERO);
                        deathSfx.play();

                        // Fade away animation for ship
                        FadeTransition deathFade = new FadeTransition(Duration.millis(1000), ship.getCharacter());
                        deathFade.setFromValue(1.0);
                        deathFade.setToValue(0.0);
                        deathFade.setOnFinished(event -> {
                            finalScoreText.setText("FINAL SCORE: " + points.get());
                            window.getScene().setRoot(insertNameLayout);

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
                    powerUpSfx.seek(Duration.ZERO);
                    powerUpSfx.play();

                    correctPresses.set(0);
                    shotgun = true;
                }
            }

            // Open main menu with space bar
            if (event.getCode() == KeyCode.SPACE && windowRoot == startLayout) {
                menuConfirmSfx.seek(Duration.ZERO);
                menuConfirmSfx.play();

                window.getScene().setRoot(mainMenuLayout);
                menuOptionsList.get(selectedMenuOption).select();
            }

            // Select menu options
            if (event.getCode() == KeyCode.DOWN && windowRoot == mainMenuLayout) {
                menuSelectSfx.seek(Duration.ZERO);
                menuSelectSfx.play();

                menuOptionsList.get(selectedMenuOption).deselect();
                selectedMenuOption++;

                if (selectedMenuOption > menuOptionsList.size() - 1) {
                    selectedMenuOption = 0;
                }

                menuOptionsList.get(selectedMenuOption).select();
            }
            if (event.getCode() == KeyCode.UP && windowRoot == mainMenuLayout) {
                menuSelectSfx.seek(Duration.ZERO);
                menuSelectSfx.play();

                menuOptionsList.get(selectedMenuOption).deselect();
                selectedMenuOption--;

                if (selectedMenuOption < 0) {
                    selectedMenuOption = menuOptionsList.size() - 1;
                }

                menuOptionsList.get(selectedMenuOption).select();
            }
            if (event.getCode() == KeyCode.SPACE && windowRoot == mainMenuLayout) {
                menuConfirmSfx.seek(Duration.ZERO);
                menuConfirmSfx.play();

                switch (selectedMenuOption) {
                    case 0:
                        window.getScene().setRoot(mainLayout);
                        // Spawn 5 initial asteroids at random positions
                        for (int i = 0; i < 5; i++) {
                            Random rand = new Random();
                            Asteroid asteroid = new Asteroid(rand.nextInt(WIDTH / 3), rand.nextInt(HEIGHT));
                            asteroids.add(asteroid);
                        }
                        asteroids.forEach(asteroid -> mainLayout.getChildren().add(0, asteroid.getCharacter()));
                        mainTimer.start();
                        break;
                    case 1:
                        window.getScene().setRoot(leaderboardLayout);
                        backOption.select();
                        break;
                    case 2:
                        window.close();
                        break;
                }
            }

            // Detect typed initials on insert name screen, up to 3 letters
            if (event.getCode().isLetterKey() && windowRoot == insertNameLayout && initialsSB.toString().contains("_")) {
                menuSelectSfx.seek(Duration.ZERO);
                menuSelectSfx.play();

                for (int i = 0; i < initialsSB.length(); i++) {
                    if (initialsSB.charAt(i) == '_') {
                        initialsSB.replace(i, i + 1, event.getText().toUpperCase());
                        break;
                    }
                }

                initialsText.setText(initialsSB.toString().replace("", " ").strip());
            }

            // Remove characters with backspace
            if ((event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.DELETE) && windowRoot == insertNameLayout && !"___".contentEquals(initialsSB)) {
                menuSelectSfx.seek(Duration.ZERO);
                menuSelectSfx.play();

                for (int i = initialsSB.length() - 1; i >= 0; i--) {
                    if (initialsSB.charAt(i) != '_') {
                        initialsSB.replace(i, i + 1, "_"); // Replace last char with underscore
                        break;
                    }
                }
                initialsText.setText(initialsSB.toString().replace("", " ").strip());
            }

            // Confirm initials
            if (event.getCode() == KeyCode.SPACE && windowRoot == insertNameLayout && !"___".contentEquals(initialsSB)) {
                menuSelectSfx.seek(Duration.ZERO);
                menuSelectSfx.play();

                // Add text to high scores
                Score score = new Score(initialsSB.toString().replaceAll("_", " "), points.get()); // Replace underscores with spaces
                scoreList.add(score);
                scoreList.sort(Score::compareTo);

                // Remove the lowest score if there are more than 10
                if (scoreList.size() > 10) {
                    scoreList.remove(scoreList.size() - 1);
                }

                // Update leaderboards
                leaderboardLeftVbox.getChildren().clear();
                leaderboardRightVbox.getChildren().clear();

                List<Text> scoreTextsList = getHiScoreTexts();

                for (int i = 0; i < 10; i++) {
                    if (i < 5) {
                        leaderboardLeftVbox.getChildren().add(scoreTextsList.get(i));
                    } else {
                        leaderboardRightVbox.getChildren().add(scoreTextsList.get(i));
                    }
                }

                // Save scores to file
                saveScores();

                // Change to game over screen
                window.getScene().setRoot(endScreenLayout);
            }

            // Leave leaderboard
            if (event.getCode() == KeyCode.SPACE && windowRoot == leaderboardLayout) {
                menuConfirmSfx.seek(Duration.ZERO);
                menuConfirmSfx.play();

                window.getScene().setRoot(mainMenuLayout);
                menuOptionsList.get(selectedMenuOption).select();
            }

            // Save a screenshot of the current view with P key. Doesn't work on the insert initials screen since the P key is used to type a letter there.
            if (event.getCode() == KeyCode.P && windowRoot != insertNameLayout) {
                saveScr(window.getScene());
            }

            // Pause game with ESC key, only allowed on main view since it doesn't work properly on other scenes
            if ((event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.PAUSE) && windowRoot == mainLayout) {
                if (isPaused) {
                    unpauseSfx.seek(Duration.ZERO);
                    unpauseSfx.play();

                    pauseFade.stop();
                    pauseText.setVisible(false);
                    mainTimer.start();
                    isPaused = false;
                } else {
                    pauseSfx.seek(Duration.ZERO);
                    pauseSfx.play();

                    pauseFade.play();
                    pauseText.setVisible(true);
                    mainTimer.stop();
                    isPaused = true;
                }
            }

            // Leave game over screen and restart game
            if (event.getCode() == KeyCode.SPACE && windowRoot == endScreenLayout && tryAgainText.isVisible()) {
                menuConfirmSfx.seek(Duration.ZERO);
                menuConfirmSfx.play();

                // Reset initials
                initialsSB.setLength(0);
                initialsSB.append("___");
                initialsText.setText(initialsSB.toString().replace("", " ").strip());

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
                ship.getCharacter().setTranslateX(WIDTH / 2.0);
                ship.getCharacter().setTranslateY(HEIGHT / 2.0);
                ship.getSafeZone().setCenterX(WIDTH / 2.0);
                ship.getSafeZone().setCenterY(HEIGHT / 2.0);
                ship.getCharacter().setRotate(0);
                ship.setMovement(new Point2D(0, 0));
                ship.getCharacter().setOpacity(1.0);

                // Go back to title screen
                window.getScene().setRoot(startLayout);
            }
        });
    }

    private List<Text> getHiScoreTexts() {
        scoreList.sort(Score::compareTo);

        List<Text> texts = new ArrayList<>();

        for (int i = 0; i < 10; i++) {

            Text scoreText = getTextScore(String.format("%02d", i + 1) + ". ");

            if (i < scoreList.size()) {
                Score s = scoreList.get(i);
                scoreText.setText(scoreText.getText() + s.playerName() + ": " + String.format("%05d", s.playerScore()));
            } else {
                scoreText.setText(scoreText.getText() + "---: -----");
            }

            scoreText.setTextAlignment(TextAlignment.CENTER);

            texts.add(scoreText);
        }

        return texts;
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
        text.setFont(Font.font("Trebuchet MS", FontWeight.SEMI_BOLD, 60));
        text.setFill(Color.WHITE);
        text.setStroke(Color.BLUE);
        text.setStrokeWidth(1.25);

        return text;
    }

    private Text getTextScore(String textContent) {
        Text text = new Text(textContent);
        text.setFont(Font.font("Courier New", FontWeight.BOLD, 45));
        text.setFill(Color.WHITE);

        return text;
    }

    private Text getTextSmall(String textContent) {
        Text text = new Text(textContent);
        text.setFont(Font.font("Trebuchet MS", 45));
        text.setFill(Color.WHITE);

        return text;
    }

    private void saveScr(Scene scene) {
        WritableImage image = new WritableImage(WIDTH, HEIGHT);
        scene.snapshot(image);

        int imageNum = 0;

        File scrDir = new File(GAME_DATA_FOLDER_PATH + "Screenshots");
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
            System.out.println("[DEBUG] Screenshot saved: " + imgFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("[DEBUG] Failed to save screenshot: " + e.getMessage());
        }
    }

    public void saveScores() {
        // Create file to store score values
        String filePath = GAME_DATA_FOLDER_PATH + "scores.ser";
        File scoreFile = new File(filePath);

        File gameDataDir = new File(GAME_DATA_FOLDER_PATH);

        // Create game data folder if it doesn't exist
        if (!gameDataDir.isDirectory()) {
            gameDataDir.mkdir();
        }

        // Create if doesn't exist
        try {
            if (!scoreFile.isFile()) {
                System.out.println("[DEBUG] File doesn't exist, creating");
                scoreFile.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("[DEBUG] Failed to create file: " + e.getMessage());
        }

        // Write list to file
        try (ObjectOutputStream objStream = new ObjectOutputStream(new FileOutputStream(filePath))) {
            objStream.writeObject(scoreList);
            System.out.println("[DEBUG] Saved scores: " + scoreFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("[DEBUG] Failed to save scores: " + e.getMessage());
        }
    }

    public void loadScores() throws ClassNotFoundException {

        String filePath = GAME_DATA_FOLDER_PATH + "scores.ser";
        File scoreFile = new File(filePath);

        if (scoreFile.isFile()) {
            try (ObjectInputStream objStream = new ObjectInputStream(new FileInputStream(filePath))) {
                scoreList = (List<Score>) objStream.readObject();
                System.out.println("[DEBUG] Loaded scores: " + scoreFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("[DEBUG] Failed to load scores: " + e.getMessage());
            }
        } else {
            System.out.println("[DEBUG] Score file doesn't exist: " + scoreFile.getAbsolutePath());
            System.out.println("[DEBUG] Creating empty list for scores");
            scoreList = new ArrayList<>();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }

}
