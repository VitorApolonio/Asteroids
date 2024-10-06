package dev.apolonio.asteroids;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles inputs that require holding down a button (ex. turning the ship).
 * <p>
 * This class is used because the normal behavior when holding down a key is to listen to the press event once, wait a
 * moment, then listen continuously. This is a problem for things like moving objects around, since the resulting
 * movement is delayed, so this class is used to handle those situations.
 */
public class InputHandler {
    private Map<KeyCode, Boolean> heldKeys;

    /**
     * Creates a new InputHandler, listening for inputs on the provided stage.
     *
     * @param window the Stage to listen for key presses on.
     */
    public InputHandler(Stage window) {
        heldKeys = new HashMap<>();

        /* Whenever some key is held down, it is stored in a HashMap with a boolean value of true. When it is released,
           that value is then set to false. Checking for held keys is done by looking for the boolean value associated
           with the desired KeyCode. */
        window.addEventHandler(KeyEvent.KEY_PRESSED, event -> heldKeys.put(event.getCode(), true));
        window.addEventHandler(KeyEvent.KEY_RELEASED, event -> heldKeys.put(event.getCode(), false));

        /* Set all keys to not pressed on scene root change.
           This is done to avoid stuff like the ship firing immediately when starting the game because both
           events are triggered with the same key. */
        window.getScene().rootProperty().addListener((observable, oldValue, newValue) -> {
            heldKeys.clear();
        });
    }

    /**
     * Returns true if at least one of the provided keys is held down, false otherwise.
     *
     * @param codes one or more KeyCode objects.
     * @return a boolean stating whether at least one of the keys is being held down.
     */
    public boolean isHeld(KeyCode... codes) {
        // If at least one of the provided keys is held down, return true
        for (KeyCode code : codes) {
            // A default of false is used so null isn't returned when checking for a value that's not on the map.
            if (heldKeys.getOrDefault(code, false)) {
                return true;
            }
        }
        // else return false
        return false;
    }
}
