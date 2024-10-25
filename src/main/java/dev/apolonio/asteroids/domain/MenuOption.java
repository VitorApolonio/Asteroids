package dev.apolonio.asteroids.domain;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Represents a selectable option on a menu. Contains methods for getting the text, selecting and deselecting the option.
 */
public class MenuOption {
    private boolean selected;
    private boolean enabled;
    private double fontSize;
    private final Text element;
    private final String text;
    private final String selectedText;

    /**
     * Creates a new menu option with the provided text and font size.
     *
     * @param optionName the text displayed on the option.
     * @param fontSize   the font size for the text.
     */
    public MenuOption(String optionName, double fontSize) {
        selected = false;
        enabled = true;

        text = optionName;
        selectedText = "-> " + optionName + " <-";

        Text element = new Text(optionName);
        element.setFont(Font.font("Trebuchet MS", fontSize));
        element.setFill(Color.WHITE);
        this.element = element;
        this.fontSize = fontSize;
    }

    /**
     * Returns the text for this option.
     *
     * @return a {@link String} representing this option's text.
     */
    public String getOptionText() {
        return text;
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
     * Sets the text font size to the provided value.
     */
    public void setFontSize(double newSize) {
        fontSize = newSize;

        // The font is slightly larger when selected, this is to maintain that
        if (selected) {
            element.setStyle("-fx-font-size: " + fontSize * 1.15);
        } else {
            element.setStyle("-fx-font-size: " + fontSize);
        }
    }

    /**
     * Selects the menu option, highlighting it by changing its style.
     */
    public void select() {
        selected = true;

        element.setStroke(Color.BLUE);
        element.setStrokeWidth(2);
        element.setText(selectedText);
        element.setStyle("-fx-font-size: " + fontSize * 1.15);
    }

    /**
     * Deselects the option, changing its style back to the default.
     */
    public void deselect() {
        selected = false;

        element.setStrokeWidth(0);
        element.setText(text);
        element.setStyle("-fx-font-size: " + fontSize);
    }

    /**
     * Returns a boolean representing whether the option is selected.
     *
     * @return {@code true} if the option is selected, {@code false} otherwise.
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Enables or disables this option. A disabled option cannot be selected, and is greyed out.
     *
     * @param enabled {@code true} or {@code false}, to enable or disable this option, respectively.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (enabled) {
            element.setFill(Color.WHITE);
        } else {
            element.setFill(Color.GRAY);
        }
    }

    /**
     * Returns the option's activation status.
     *
     * @return {@code true} if the option is enabled, {@code false} otherwise.
     */
    public boolean getEnabled() {
        return enabled;
    }
}
