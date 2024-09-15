package dev.apolonio.asteroids;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class InputHandler {
    private Map<KeyCode, Boolean> heldKeys;
    private Stage window;

    public InputHandler(Stage window) {
        this.window = window;
        heldKeys = new HashMap<>();

        window.addEventHandler(KeyEvent.KEY_PRESSED, event -> heldKeys.put(event.getCode(), true));
        window.addEventHandler(KeyEvent.KEY_RELEASED, event -> heldKeys.put(event.getCode(), false));

        /* Set all keys to not pressed on scene change.
           This is done to avoid stuff like the ship firing immediately when starting the game because both
           events are triggered with the same key. */
        window.sceneProperty().addListener((observable, oldValue, newValue) -> {
            heldKeys.clear();
        });
    }

    public boolean isHeld(KeyCode... codes) {
        // If at least one of the provided keys is held down, return true
        for (KeyCode code : codes) {
            if (heldKeys.getOrDefault(code, false)) {
                return true;
            }
        }
        // else return false
        return false;
    }
}
