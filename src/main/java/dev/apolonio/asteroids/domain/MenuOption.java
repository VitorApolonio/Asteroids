package dev.apolonio.asteroids.domain;

import javafx.beans.binding.Bindings;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Represents a selectable option on a menu. Contains methods for getting the text, selecting and deselecting the option.
 */
public class MenuOption {
    private boolean selected;
    private boolean enabled;
    private final Text element;
    private final String text;
    private final String selectedText;

    /**
     * Creates a new menu option with the provided text and font size.
     *
     * @param optionName the text displayed on the option.
     * @param stage      a {@link Stage}, used for calculating the font size relative to the screen.
     */
    public MenuOption(String optionName, Stage stage) {
        selected = false;
        enabled = true;

        text = optionName;
        selectedText = "-> " + optionName + " <-";

        Text element = new Text(optionName);
        element.styleProperty().bind(Bindings.concat("-fx-font-size: ", stage.heightProperty().divide(11)));
        element.getStyleClass().add("option");

        this.element = element;
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
     * Selects the menu option, highlighting it by changing its style.
     */
    public void select() {
        selected = true;

        element.getStyleClass().add("option-selected");

        element.setScaleX(1.15);
        element.setScaleY(1.15);
    }

    /**
     * Deselects the option, changing its style back to the default.
     */
    public void deselect() {
        selected = false;

        element.getStyleClass().remove("option-selected");

        element.setScaleX(1.0);
        element.setScaleY(1.0);
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
            element.getStyleClass().remove("option-disabled");
        } else {
            element.getStyleClass().add("option-disabled");
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
