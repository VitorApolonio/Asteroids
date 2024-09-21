package dev.apolonio.asteroids.domain;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class MenuOption {
    private boolean selected;
    private final Text element;
    private final String text;
    private final String selectedText;

    public MenuOption(String optionName) {
        selected = false;

        text = optionName;
        selectedText = "-> " + text + " <-";

        Text element = new Text(optionName);
        element.setFont(Font.font("Trebuchet MS", 60));
        element.setFill(Color.WHITE);
        this.element = element;
    }

    public Text getTextElement() {
        return element;
    }

    public void select() {
        selected = true;

        element.setStroke(Color.BLUE);
        element.setStrokeWidth(2);
        element.setText(selectedText);
    }

    public void deselect() {
        selected = false;

        element.setStrokeWidth(0);
        element.setText(text);
    }

    public boolean isSelected() {
        return selected;
    }
}
