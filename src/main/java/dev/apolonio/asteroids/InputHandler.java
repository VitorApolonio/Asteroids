package dev.apolonio.asteroids;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles inputs that require holding down a button (i.e. turning the ship).
 * <p>
 * This class is used because the normal behavior when holding down a key is to listen to the press event once, wait a
 * moment, then listen continuously. This is a problem for things like moving objects around, since the resulting
 * movement is delayed, so this class is used to handle those situations.
 */
public class InputHandler {
    private final Map<KeyCode, Boolean> HELD_KEYS;

    /**
     * Creates a new InputHandler, listening for inputs on the provided stage.
     *
     * @param window the {@link Stage} to listen for key presses on.
     */
    public InputHandler(Stage window) {
        HELD_KEYS = new HashMap<>();

        /* Whenever some key is held down, it is stored in a HashMap with a boolean value of true. When it is released,
           that value is then set to false. Checking for held keys is done by looking for the boolean value associated
           with the desired KeyCode. */
        window.addEventHandler(KeyEvent.KEY_PRESSED, event -> HELD_KEYS.put(event.getCode(), true));
        window.addEventHandler(KeyEvent.KEY_RELEASED, event -> HELD_KEYS.put(event.getCode(), false));

        /* Set all keys to not pressed on scene root change.
           This is done to avoid stuff like the ship firing immediately when starting the game because both
           events are triggered with the same key. */
        window.getScene().rootProperty().addListener((observable, oldValue, newValue) -> HELD_KEYS.clear());
    }

    /**
     * Returns whether at least one of the provided keys is held down.
     *
     * @param codes one or more {@link KeyCode} objects.
     * @return {@code true} if at least one key is being held down, {@code false} otherwise.
     */
    public boolean isHeld(KeyCode... codes) {
        // If at least one of the provided keys is held down, return true
        for (KeyCode code : codes) {
            // A default of false is used so null isn't returned when checking for a value that's not on the map.
            if (HELD_KEYS.getOrDefault(code, false)) {
                return true;
            }
        }
        // else return false
        return false;
    }
}
