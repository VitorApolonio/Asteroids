package dev.apolonio.asteroids.domain;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class MenuOption {
    private boolean selected;
    private Text element;
    private String name;

    public MenuOption(String optionName) {
        selected = false;

        name = optionName;

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
    }

    public void deselect() {
        selected = false;

        element.setStrokeWidth(0);
    }
}