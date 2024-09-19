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
        element.setFont(Font.font("Trebuchet MS", 40));
        element.setFill(Color.WHITE);
        this.element = element;
    }

    public Text getTextElement() {
        return element;
    }

    public void select() {
        selected = true;

        element.setFill(Color.BLACK);
        element.setStroke(Color.WHITE);
        element.setStrokeWidth(1);
    }

    public void deselect() {
        selected = false;

        element.setFill(Color.WHITE);
        element.setStrokeWidth(0);
    }
}
