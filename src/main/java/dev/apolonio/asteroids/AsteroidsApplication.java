package dev.apolonio.asteroids;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import dev.apolonio.asteroids.domain.Asteroid;
import dev.apolonio.asteroids.domain.Entity;
import dev.apolonio.asteroids.domain.Menu;
import dev.apolonio.asteroids.domain.MenuOption;
import dev.apolonio.asteroids.domain.Projectile;
import dev.apolonio.asteroids.domain.Score;
import dev.apolonio.asteroids.domain.Ship;
import dev.apolonio.asteroids.domain.Star;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
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
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import javax.imageio.ImageIO;

public class AsteroidsApplication extends Application {

    // Time before closing splash screen in ms
    private static final int SPLASH_SCR_TIME = 1000;

    // Initial width and height for game window
    private static final int INITIAL_WIDTH = 800;
    private static final int INITIAL_HEIGHT = 600;

    // Folder for storing game data files
    private static final String GAME_DATA_FOLDER_PATH = System.getProperty("user.home") + "/Documents/Asteroids/";

    // List of user scores
    private final List<Score> SCORE_LIST = new ArrayList<>();

    // Whether the game is paused
    private boolean isPaused = false;

    // Whether the cheat code is active
    private boolean shotgun = false;

    // User interface SFX
    private MediaPlayer menuSelectSfx;
    private MediaPlayer menuConfirmSfx;
    private MediaPlayer pauseSfx;
    private MediaPlayer unpauseSfx;

    // Game SFX
    private MediaPlayer fireSfx;
    private MediaPlayer spreadFireSfx;
    private MediaPlayer powerUpSfx;
    private MediaPlayer asteroidSfx;
    private MediaPlayer deathSfx;

    @Override
    public void start(Stage window) {

        // Set game window size
        window.setWidth(INITIAL_WIDTH);
        window.setHeight(INITIAL_HEIGHT);

        // Create title screen layout
        VBox startLayout = new VBox(window.getHeight() / 8);
        startLayout.setAlignment(Pos.CENTER);

        // Create title text
        Text txt_titleText = new Text("ASTEROIDS");
        txt_titleText.styleProperty().bind(Bindings.concat("-fx-font-size: ", window.heightProperty().divide(7)));
        txt_titleText.getStyleClass().add("title");
        startLayout.getChildren().add(txt_titleText);

        Text txt_pressToStart = new Text("PRESS SPACE TO START");
        txt_pressToStart.styleProperty().bind(Bindings.concat("-fx-font-size: ", window.heightProperty().divide(13)));
        txt_pressToStart.getStyleClass().add("note");
        startLayout.getChildren().add(txt_pressToStart);

        // Create scene with layout
        Scene view = new Scene(startLayout);
        view.setFill(Color.BLACK);
        window.setScene(view);

        // Load CSS from resources
        view.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm());

        // Splash screen (image that shows up before game starts)
        ImageView splashImageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/splash.png"))));
        Pane splashRoot = new Pane(splashImageView);
        Scene splashScene = new Scene(splashRoot, splashImageView.getImage().getWidth(), splashImageView.getImage().getHeight());

        Stage splashStage = new Stage();
        splashStage.initStyle(StageStyle.UNDECORATED);
        splashStage.setScene(splashScene);
        splashStage.show();

        // This thread loads some game data such as sounds, then closes the splash screen.
        new Thread(() -> {
            try {
                // Load scores from file
                try {
                    loadScores(GAME_DATA_FOLDER_PATH);
                } catch (ClassNotFoundException e) {
                    System.err.println("[DEBUG] Failed to load scores: " + e.getMessage());
                }

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
                    window.setTitle("Asteroids!");
                    window.setResizable(false); // Resizing the window directly would cause problems, so it can only be resized in-game
                    splashStage.close();
                    // This is so the game doesn't exit fullscreen when pausing
                    window.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
                    window.show();
                });

            } catch (InterruptedException e) {
                System.err.println("[DEBUG] Splash thread interrupted: " + e.getMessage());
            }
        }).start();

        // Create main menu
        MenuOption startOption = new MenuOption("START GAME", window);
        MenuOption leaderboardOption = new MenuOption("HI-SCORES", window);
        MenuOption resChangeOption = new MenuOption("RESOLUTION", window);
        MenuOption quitOption = new MenuOption("QUIT", window);

        Menu mainMenu = new Menu(startOption, leaderboardOption, resChangeOption, quitOption);

        VBox mainMenuLayout = new VBox(window.getHeight() / 40);
        mainMenuLayout.getChildren().addAll(mainMenu.getOptions().stream().map(MenuOption::getTextElement).toList());
        mainMenuLayout.setAlignment(Pos.CENTER);

        // Create menu title
        Text txt_resMenuTitle = new Text("RESOLUTION SELECT");
        txt_resMenuTitle.styleProperty().bind(Bindings.concat("-fx-font-size: ", window.heightProperty().divide(11)));
        txt_resMenuTitle.getStyleClass().add("subtitle");

        // Create resolution menu options
        MenuOption res640x480 = new MenuOption("640x480", window);
        MenuOption res800x600 = new MenuOption("800x600", window);
        MenuOption res1280x720 = new MenuOption("1280x720", window);
        MenuOption res1920x1080 = new MenuOption("1920x1080", window);
        MenuOption resBackOption = new MenuOption("BACK", window);

        Menu resMenu = new Menu(res640x480, res800x600, res1280x720, res1920x1080, resBackOption);

        VBox resMenuLayout = new VBox(window.getHeight() / 40);
        resMenuLayout.getChildren().add(txt_resMenuTitle);
        resMenuLayout.getChildren().addAll(resMenu.getOptions().stream().map(MenuOption::getTextElement).toList());
        resMenuLayout.setAlignment(Pos.CENTER);

        // Create leaderboard layout
        BorderPane leaderboardLayout = new BorderPane();

        Text txt_hiScoresTitle = new Text("HIGH SCORES");
        txt_hiScoresTitle.styleProperty().bind(Bindings.concat("-fx-font-size: ", window.heightProperty().divide(11)));
        txt_hiScoresTitle.getStyleClass().add("subtitle");

        VBox leaderboardLeftVbox = new VBox();
        leaderboardLeftVbox.spacingProperty().bind(window.heightProperty().divide(20));
        leaderboardLeftVbox.setAlignment(Pos.CENTER);

        VBox leaderboardRightVbox = new VBox();
        leaderboardRightVbox.spacingProperty().bind(window.heightProperty().divide(20));
        leaderboardRightVbox.setAlignment(Pos.CENTER);

        HBox scoreContainerHbox = new HBox();
        scoreContainerHbox.spacingProperty().bind(window.heightProperty().divide(13));
        scoreContainerHbox.setAlignment(Pos.CENTER);
        scoreContainerHbox.getChildren().addAll(leaderboardLeftVbox, leaderboardRightVbox);

        // Fill leaderboards
        List<Text> scoreTexts = getHiScoreTexts(window);

        for (int i = 0; i < 10; i++) {
            Text scoreText = scoreTexts.get(i);

            if (i < 5) {
                leaderboardLeftVbox.getChildren().add(scoreText);
            } else {
                leaderboardRightVbox.getChildren().add(scoreText);
            }
        }

        MenuOption scoresBackOption = new MenuOption("BACK", window);

        leaderboardLayout.setTop(txt_hiScoresTitle);
        leaderboardLayout.setCenter(scoreContainerHbox);
        leaderboardLayout.setBottom(scoresBackOption.getTextElement());

        BorderPane.setAlignment(txt_hiScoresTitle, Pos.CENTER);
        BorderPane.setAlignment(scoresBackOption.getTextElement(), Pos.CENTER);
        BorderPane.setAlignment(leaderboardLeftVbox, Pos.CENTER);
        BorderPane.setAlignment(leaderboardRightVbox, Pos.CENTER);
        leaderboardLayout.setPadding(new Insets(20, 0, 20, 0));

        // Create main game layout
        Pane mainLayout = new Pane();

        // Create player ship
        Ship ship = new Ship(window.getWidth() / 2, window.getHeight() / 2, window.getHeight() / 4);
        mainLayout.getChildren().add(ship.getSafeZone());
        mainLayout.getChildren().add(ship.getCharacter());

        // Create entity lists (empty for now)
        List<Asteroid> asteroids = new ArrayList<>();
        List<Projectile> projectiles = new ArrayList<>();
        List<Star> stars = new ArrayList<>();

        /* A separate layout is created for the asteroids so that spawning more of them won't mess with
           the element order of the main layout. */
        Pane asteroidLayer = new Pane();
        mainLayout.getChildren().add(asteroidLayer);

        // Create list for star animations (empty for now)
        List<ScaleTransition> starAnimations = new ArrayList<>();

        // Create user score text
        Text txt_currentScoreText = new Text("SCORE: 0");
        txt_currentScoreText.styleProperty().bind(Bindings.concat("-fx-font-size: ", window.heightProperty().divide(11)));
        txt_currentScoreText.getStyleClass().add("subtitle");
        txt_currentScoreText.setTranslateX(window.getWidth() / 20);
        txt_currentScoreText.setTranslateY(window.getHeight() / 10);
        mainLayout.getChildren().add(txt_currentScoreText);
        AtomicInteger points = new AtomicInteger();

        // Create game over screen layout
        VBox endScreenLayout = new VBox(window.getHeight() / 12);
        endScreenLayout.setAlignment(Pos.CENTER);

        // Create game over text
        Text txt_gameOverText = new Text("GAME OVER!");
        txt_gameOverText.styleProperty().bind(Bindings.concat("-fx-font-size: ", window.heightProperty().divide(7)));
        txt_gameOverText.getStyleClass().add("title");
        endScreenLayout.getChildren().add(txt_gameOverText);

        Text txt_finalScoreText = new Text("FINAL SCORE: 0");
        txt_finalScoreText.styleProperty().bind(Bindings.concat("-fx-font-size: ", window.heightProperty().divide(11)));
        txt_finalScoreText.getStyleClass().add("subtitle");
        endScreenLayout.getChildren().add(txt_finalScoreText);

        Text txt_tryAgainText = new Text("PRESS SPACE TO CONTINUE");
        txt_tryAgainText.styleProperty().bind(Bindings.concat("-fx-font-size: ", window.heightProperty().divide(13)));
        txt_tryAgainText.getStyleClass().add("note");
        txt_tryAgainText.setVisible(false);
        endScreenLayout.getChildren().add(txt_tryAgainText);

        // Create insert name screen
        VBox insertNameLayout = new VBox(window.getHeight() / 12);
        insertNameLayout.setAlignment(Pos.CENTER);

        // Create insert name text
        Text txt_enterYourNameText = new Text("ENTER YOUR NAME");
        txt_enterYourNameText.styleProperty().bind(Bindings.concat("-fx-font-size: ", window.heightProperty().divide(11)));
        txt_enterYourNameText.getStyleClass().add("subtitle");
        insertNameLayout.getChildren().add(txt_enterYourNameText);

        StringBuilder initialsSB = new StringBuilder("___");

        Text txt_initialsText = new Text(initialsSB.toString().replace("", " ").strip());
        txt_initialsText.styleProperty().bind(Bindings.concat("-fx-font-size: ", window.heightProperty().divide(7)));
        txt_initialsText.getStyleClass().add("score");
        insertNameLayout.getChildren().add(txt_initialsText);

        // Pause text
        Text txt_pauseText = new Text("PAUSED");
        txt_pauseText.styleProperty().bind(Bindings.concat("-fx-font-size: ", window.heightProperty().divide(7)));
        txt_pauseText.getStyleClass().add("title");
        txt_pauseText.setVisible(false);
        // This is to center the pause text
        txt_pauseText.xProperty().bind(window.heightProperty().divide(2));
        txt_pauseText.yProperty().bind(window.heightProperty().divide(2));
        mainLayout.getChildren().add(txt_pauseText);

        // Pause flashing text animation
        FadeTransition pauseFade = new FadeTransition(Duration.millis(666), txt_pauseText);
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

                            mainLayout.getChildren().add(proj.getCharacter());
                        }
                    }

                    cooldown += 30;
                }

                // Decrease cooldown if not already at 0
                if (cooldown > 0) {
                    cooldown -= 1;
                }

                // Continuously spawn asteroids starting with a 0.5% chance, raising by 0.5% more every 2500 points
                if (Math.random() < 0.005 * (1 + (double) points.get() / 2500)) {
                    Asteroid asteroid = new Asteroid((int) (Math.random() * window.getWidth() / 3), (int) (Math.random() * window.getHeight() / 2));
                    asteroid.setMovement(asteroid.getMovement().multiply(Math.min(3, 1 + points.get() / 8000))); // Increase velocity with player score up to a max of 3x speed
                    if (!ship.inSafeZone(asteroid)) {
                        asteroids.add(asteroid);
                        asteroidLayer.getChildren().add(asteroid.getCharacter());
                    }
                }

                // Ship and asteroid movement
                ship.move(window.getWidth(), window.getHeight());
                asteroids.forEach(a -> a.move(window.getWidth(), window.getHeight()));
                projectiles.forEach(Projectile::move);

                // Handle collisions between shots and asteroids
                for (Projectile projectile : projectiles) {
                    List<Asteroid> collisions = asteroids.stream()
                            .filter(asteroid -> asteroid.collide(projectile))
                            .toList();

                    // Increase score with hits
                    for (Asteroid collided : collisions) {
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
                            txt_currentScoreText.setText("SCORE: " + points.addAndGet(50));
                        } else {
                            txt_currentScoreText.setText("SCORE: " + points.addAndGet(100));
                        }
                    }
                }

                // Remove off-screen projectiles
                Iterator<Projectile> projIt = projectiles.iterator();
                while (projIt.hasNext()) {
                    Projectile proj = projIt.next();

                    if (proj.getCharacter().getTranslateX() < 0
                            || proj.getCharacter().getTranslateX() > window.getWidth()
                            || proj.getCharacter().getTranslateY() < 0
                            || proj.getCharacter().getTranslateY() > window.getHeight()) {
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
                            txt_finalScoreText.setText("FINAL SCORE: " + points.get());
                            window.getScene().setRoot(insertNameLayout);

                            // Delay before "PRESS SPACE" text pops up
                            PauseTransition tryAgainPause = new PauseTransition(Duration.millis(1000));
                            tryAgainPause.setOnFinished(event2 -> txt_tryAgainText.setVisible(true));
                            tryAgainPause.play();
                        });
                        deathFade.play();
                        break;
                    }
                }
            }
        };

        // Resizes elements whenever the window changes size
        window.heightProperty().addListener((ov, oldVal, newVal) -> {

            // Redo position calculations for text and ship
            ship.getCharacter().setTranslateX(window.getWidth() / 2);
            ship.getCharacter().setTranslateY(window.getHeight() / 2);
            txt_currentScoreText.setTranslateX(window.getWidth() / 15);
            txt_currentScoreText.setTranslateY(window.getHeight() / 10);
        });

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

            // Whether the current screen is a menu
            boolean onMenuScreen = (windowRoot == mainMenuLayout || windowRoot == resMenuLayout);

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
            }

            // Select next menu option
            if ((event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.S) && onMenuScreen) {
                menuSelectSfx.seek(Duration.ZERO);
                menuSelectSfx.play();

                if (windowRoot.equals(mainMenuLayout)) {
                    mainMenu.selectNext();
                } else if (windowRoot.equals(resMenuLayout)) {
                    resMenu.selectNext();
                }
            }

            // Select previous menu option
            if ((event.getCode() == KeyCode.UP || event.getCode() == KeyCode.W) && onMenuScreen) {
                menuSelectSfx.seek(Duration.ZERO);
                menuSelectSfx.play();

                if (windowRoot.equals(mainMenuLayout)) {
                    mainMenu.selectPrevious();
                } else if (windowRoot.equals(resMenuLayout)) {
                    resMenu.selectPrevious();
                }
            }

            // Confirm selection on main menu
            if (event.getCode() == KeyCode.SPACE && windowRoot == mainMenuLayout) {
                menuConfirmSfx.seek(Duration.ZERO);
                menuConfirmSfx.play();

                switch (mainMenu.getSelectedIndex()) {
                    // Start
                    case 0: {
                        window.getScene().setRoot(mainLayout);
                        // A Random object is used to randomize entity positions on the screen.
                        Random rand = new Random();
                        // Spawn 40 stars at random positions
                        for (int i = 0; i < 40; i++) {
                            Star star = new Star(rand.nextDouble(window.getWidth()), rand.nextDouble(window.getHeight()));
                            stars.add(star);
                        }
                        // Spawn 5 initial asteroids at random positions
                        for (int i = 0; i < 5; i++) {
                            Asteroid asteroid = new Asteroid(rand.nextDouble(window.getWidth() / 3), rand.nextDouble(window.getHeight()));
                            asteroids.add(asteroid);
                        }
                        // Create star animations
                        starAnimations.addAll(getStarAnimations(stars));

                        // Add elements to screen
                        asteroids.forEach(asteroid -> asteroidLayer.getChildren().add(0, asteroid.getCharacter()));
                        stars.forEach(star -> mainLayout.getChildren().add(0, star.getCharacter()));

                        // Play star animations
                        starAnimations.forEach(Animation::play);
                        // Start main timer
                        mainTimer.start();
                        break;
                    }
                    // Hi-Scores
                    case 1: {
                        window.getScene().setRoot(leaderboardLayout);
                        scoresBackOption.select();
                        break;
                    }
                    // Resolution change menu
                    case 2: {
                        window.getScene().setRoot(resMenuLayout);
                        resMenu.selectLast();
                        break;
                    }
                    // Exit
                    case 3: {
                        window.close();
                        break;
                    }
                }
            }

            // Confirm selection on resolution change menu
            if (event.getCode() == KeyCode.SPACE && windowRoot == resMenuLayout) {
                menuConfirmSfx.seek(Duration.ZERO);
                menuConfirmSfx.play();

                // Back option
                if (resMenu.getSelectedIndex() == resMenu.getOptions().size() - 1) {
                    window.getScene().setRoot(mainMenuLayout);
                    mainMenu.selectFirst();
                } else {
                    // Since resolutions are in the format <width>x<height> we can just split them by the x to get both
                    String[] values = resMenu.getSelected().getOptionText().split("x");

                    // The window size isn't altered on fullscreen mode
                    if (!window.isFullScreen()) {
                        window.setWidth(Integer.parseInt(values[0]));
                        window.setHeight(Integer.parseInt(values[1]));
                    }
                }
            }

            // Detect typed initials on insert name screen, up to 3 letters
            if ((event.getCode().isLetterKey() || event.getCode().isDigitKey()) && windowRoot == insertNameLayout && initialsSB.toString().contains("_")) {
                menuSelectSfx.seek(Duration.ZERO);
                menuSelectSfx.play();

                for (int i = 0; i < initialsSB.length(); i++) {
                    if (initialsSB.charAt(i) == '_') {
                        initialsSB.replace(i, i + 1, event.getText().toUpperCase()); // Replace last non-blank char with typed key
                        break;
                    }
                }

                txt_initialsText.setText(initialsSB.toString().replace("", " ").strip());
            }

            // Remove characters with backspace
            if ((event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.DELETE) && windowRoot == insertNameLayout && !"___".contentEquals(initialsSB)) {
                menuSelectSfx.seek(Duration.ZERO);
                menuSelectSfx.play();

                for (int i = initialsSB.length() - 1; i >= 0; i--) {
                    if (initialsSB.charAt(i) != '_') {
                        initialsSB.replace(i, i + 1, "_"); // Replace last non-blank char with underscore
                        break;
                    }
                }
                txt_initialsText.setText(initialsSB.toString().replace("", " ").strip());
            }

            // Confirm initials
            if (event.getCode() == KeyCode.SPACE && windowRoot == insertNameLayout && !"___".contentEquals(initialsSB)) {
                menuSelectSfx.seek(Duration.ZERO);
                menuSelectSfx.play();

                // Add text to high scores
                Score score = new Score(initialsSB.toString().replaceAll("_", " "), points.get()); // Replace underscores with spaces
                SCORE_LIST.add(score);
                SCORE_LIST.sort(Score::compareTo);

                // Remove the lowest score if there are more than 10
                if (SCORE_LIST.size() > 10) {
                    SCORE_LIST.remove(SCORE_LIST.size() - 1);
                }

                // Update leaderboards
                leaderboardLeftVbox.getChildren().clear();
                leaderboardRightVbox.getChildren().clear();

                List<Text> scoreTextsList = getHiScoreTexts(window);

                for (int i = 0; i < 10; i++) {
                    if (i < 5) {
                        leaderboardLeftVbox.getChildren().add(scoreTextsList.get(i));
                    } else {
                        leaderboardRightVbox.getChildren().add(scoreTextsList.get(i));
                    }
                }

                // Save scores to file
                saveScores(GAME_DATA_FOLDER_PATH);

                // Change to game over screen
                window.getScene().setRoot(endScreenLayout);
            }

            // Leave leaderboard
            if (event.getCode() == KeyCode.SPACE && windowRoot == leaderboardLayout) {
                menuConfirmSfx.seek(Duration.ZERO);
                menuConfirmSfx.play();

                window.getScene().setRoot(mainMenuLayout);
                mainMenu.selectFirst();
            }

            /* Toggle fullscreen with ALT + ENTER. Only allowed on menu screens except for the resolution change menu.
               On the main layout the stars are distributed based on the resolution, so changing it mid-game would
               result in poorly distributed stars, and the resolution menu is disabled on fullscreen */
            KeyCombination fsKeyCombo = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.ALT_DOWN);
            if (fsKeyCombo.match(event) && windowRoot != mainLayout && windowRoot != resMenuLayout) {
                // Disable menu on fullscreen, re-enable if exiting
                resChangeOption.setEnabled(window.isFullScreen());

                // Select next option if resolution menu is disabled and selected
                if (resChangeOption.isSelected() && !resChangeOption.getEnabled()) {
                    mainMenu.selectNext();
                }

                // Toggle fullscreen
                window.setFullScreen(!window.isFullScreen());
            }

            /* Save a screenshot of the current view with P key.
               Doesn't work on the insert initials screen since the P key is used to type a letter there. */
            if (event.getCode() == KeyCode.P && windowRoot != insertNameLayout) {
                String filePath = GAME_DATA_FOLDER_PATH + "/Screenshots";
                saveScr(window.getScene(), filePath, (int) window.getWidth(), (int) window.getHeight());
            }

            // Pause game with ESC key, only allowed on main view since it doesn't work properly on other scenes
            if ((event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.PAUSE) && windowRoot == mainLayout) {
                if (isPaused) {
                    unpauseSfx.seek(Duration.ZERO);
                    unpauseSfx.play();

                    pauseFade.stop();
                    txt_pauseText.setVisible(false);
                    mainTimer.start();
                    isPaused = false;
                } else {
                    pauseSfx.seek(Duration.ZERO);
                    pauseSfx.play();

                    pauseFade.play();
                    txt_pauseText.setVisible(true);
                    mainTimer.stop();
                    isPaused = true;
                }
            }

            // Leave game over screen and restart game
            if (event.getCode() == KeyCode.SPACE && windowRoot == endScreenLayout && txt_tryAgainText.isVisible()) {
                menuConfirmSfx.seek(Duration.ZERO);
                menuConfirmSfx.play();

                // Reset initials
                initialsSB.setLength(0);
                initialsSB.append("___");
                txt_initialsText.setText(initialsSB.toString().replace("", " ").strip());

                // Reset selected menu option
                mainMenu.selectFirst();

                // Clear points
                points.set(0);
                txt_currentScoreText.setText("SCORE: 0");
                txt_finalScoreText.setText(("FINAL SCORE: 0"));

                // Delete all entities
                mainLayout.getChildren().removeAll(stars.stream().map(Entity::getCharacter).toList());
                mainLayout.getChildren().removeAll(projectiles.stream().map(Entity::getCharacter).toList());
                asteroidLayer.getChildren().clear();
                stars.clear();
                asteroids.clear();
                projectiles.clear();
                starAnimations.clear();

                // Center ship
                ship.getCharacter().setTranslateX(window.getWidth() / 2);
                ship.getCharacter().setTranslateY(window.getHeight() / 2);
                ship.getSafeZone().setCenterX(window.getWidth() / 2);
                ship.getSafeZone().setCenterY(window.getHeight() / 2);
                ship.getCharacter().setRotate(0);
                ship.setMovement(new Point2D(0, 0));
                ship.getCharacter().setOpacity(1.0);

                // Go back to title screen
                window.getScene().setRoot(startLayout);
            }
        });
    }

    /**
     * Returns a {@link List} of blinking animations for {@link Star} elements.
     *
     * @param stars a List of stars.
     * @return      a List with an animation for each of the provided stars.
     */
    private static List<ScaleTransition> getStarAnimations(List<Star> stars) {
        // Create list for animations
        List<ScaleTransition> animations = new ArrayList<>();

        // Create animations
        for (Star s : stars) {
            ScaleTransition starAnim = new ScaleTransition(Duration.millis(Math.random() * 200), s.getCharacter());
            starAnim.setFromX(1.0);
            starAnim.setFromY(1.0);
            starAnim.setToX(0.9);
            starAnim.setToY(0.9);
            starAnim.setAutoReverse(true);
            starAnim.setCycleCount(Animation.INDEFINITE);
            animations.add(starAnim);
        }

        return animations;
    }

    /**
     * Returns a list of 10 TextElements, each containing a score value formatted as {@code NUL: 00000} where {@code NUL}
     * is the player's initials and {@code 00000} is the number of points.
     * <p>
     * In the event there are not 10 scores, the remaining spaces
     * will be filled with dashed lines {@code ---: -----}.
     *
     * @param stage    a {@link Stage}, used for calculating the font size relative to the screen.
     * @return         a List containing 10 TextElements.
     */
    private List<Text> getHiScoreTexts(Stage stage) {
        // Sorts the list of user scores
        SCORE_LIST.sort(Score::compareTo);

        // Create list to store score texts
        List<Text> texts = new ArrayList<>();

        // Format scores to be added to leaderboard
        for (int i = 0; i < 10; i++) {

            // Format points to 2 places, padded with 0s
            Text txt_scoreText = new Text(String.format("%02d", i + 1) + ". ");
            txt_scoreText.styleProperty().bind(Bindings.concat("-fx-font-size: ", stage.heightProperty().divide(14)));
            txt_scoreText.getStyleClass().add("score");

            if (i < SCORE_LIST.size()) {
                // If score is in the list, format points to 5 places, padded with 0s
                Score s = SCORE_LIST.get(i);
                // The mod 100000 guarantees the number will not have more than 5 digits
                txt_scoreText.setText(txt_scoreText.getText() + s.playerName() + ": " + String.format("%05d", s.playerPoints() % 100000));
            } else {
                // If score is not in the list, set player name and points to dashes
                txt_scoreText.setText(txt_scoreText.getText() + "---: -----");
            }

            // Center score text
            txt_scoreText.setTextAlignment(TextAlignment.CENTER);

            // Add to list
            texts.add(txt_scoreText);
        }

        return texts;
    }

    /**
     * Attempts to save a screenshot of the scene at the provided folder.
     *
     * @param scene      the scene to save as a screenshot.
     * @param folderPath the path to the folder where the screenshot will be saved.
     * @param scrWidth   the horizontal screen resolution in pixels
     * @param scrHeight  the vertical screen resolution in pixels
     */
    private void saveScr(Scene scene, String folderPath, int scrWidth, int scrHeight) {
        // Save snapshot of scene as a WritableImage
        WritableImage image = new WritableImage(scrWidth, scrHeight);
        scene.snapshot(image);

        // Inicial number for image file name
        int imageNum = 0;

        // Folder to save screenshots at
        File scrDir = new File(folderPath);
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

    /**
     * Attempts to save the leaderboard to a file at the specified folder.
     * <p>
     * The scores will be saved to a file with the name {@code scores.csv}.
     *
     * @param folderPath the path to the folder where the score file will be saved.
     */
    public void saveScores(String folderPath) {
        // Create file to store score values
        String filePath = folderPath + "scores.csv";
        File scoreFile = new File(filePath);

        File gameDataDir = new File(folderPath);

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
        try (FileWriter writer = new FileWriter(scoreFile)) {
            StringBuilder scoreSB = new StringBuilder();

            for (Score s : SCORE_LIST) {
                scoreSB.append(s.toString()).append(",");
            }

            writer.write(scoreSB.toString());
            System.out.println("[DEBUG] Saved scores: " + scoreFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("[DEBUG] Failed to save scores: " + e.getMessage());
        }
    }

    /**
     * Attempts to load the leaderboard from a file at the specified folder path.
     * <p>
     * The scores are expected to be saved to a file with the name {@code scores.csv}.
     *
     * @param folderPath path to the folder where the score file is located.
     * @throws ClassNotFoundException if the Scores class doesn't exist.
     */
    public void loadScores(String folderPath) throws ClassNotFoundException {

        String filePath = folderPath + "scores.csv";
        File scoreFile = new File(filePath);

        if (scoreFile.isFile()) {
            /* A BufferedReader is used for its ability to read whole lines at a time, and because it throws an IOException
               instead of hiding file reading errors like the Scanner. */
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String[] nameScorePairs = reader.readLine().split(",");
                List<Score> tempScoreList = new ArrayList<>(); // A temporary list is made to store scores as they're read

                for (String pair : nameScorePairs) {
                    String[] values = pair.split(":"); // Scores are separated from initials by a colon
                    tempScoreList.add(new Score(values[0], Integer.parseInt(values[1].strip())));
                }

                SCORE_LIST.addAll(tempScoreList); // The values from the temp list are copied to the final list if nothing goes wrong
                System.out.println("[DEBUG] Loaded scores: " + scoreFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("[DEBUG] Failed to load scores: " + e.getMessage());
            }
        } else {
            System.out.println("[DEBUG] Score file doesn't exist: " + scoreFile.getAbsolutePath());
            System.out.println("[DEBUG] Score list will be empty.");
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
