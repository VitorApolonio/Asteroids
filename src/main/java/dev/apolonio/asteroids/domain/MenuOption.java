package dev.apolonio.asteroids.domain;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Represents a selectable option on a menu. Contains methods for getting the text, selecting and deselecting the option.
 */
public class MenuOption {
    private boolean selected;
    private final Text element;
    private final String text;
    private final String selectedText;

    /**
     * Creates a new menu option with the provided text.
     *
     * @param optionName the text displayed on the option.
     */
    public MenuOption(String optionName) {
        selected = false;

        text = optionName;
        selectedText = "-> " + optionName + " <-";

        Text element = new Text(optionName);
        element.setFont(Font.font("Trebuchet MS", 50));
        element.setFill(Color.WHITE);
        this.element = element;
    }

    /**
     * Returns a {@link Text} that can be displayed on the screen on a menu.
     *
     * @return the Text used for the option.
     */
    public Text getTextElement() {
        return element;
    }

    /**
     * Selects the menu option, highlighting it by changing its style.
     */
    public void select() {
        selected = true;

        element.setStroke(Color.BLUE);
        element.setStrokeWidth(2);
        element.setText(selectedText);
        element.setStyle("-fx-font-size: 60;");
    }

    /**
     * Deselects the option, changing its style back to the default.
     */
    public void deselect() {
        selected = false;

        element.setStrokeWidth(0);
        element.setText(text);
        element.setStyle("-fx-font-size: 50;");
    }

    /**
     * Returns a boolean representing whether the option is selected.
     *
     * @return {@code true} if the option is selected, {@code false} otherwise.
     */
    public boolean isSelected() {
        return selected;
    }
}
