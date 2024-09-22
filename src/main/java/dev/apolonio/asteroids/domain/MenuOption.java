package dev.apolonio.asteroids.domain;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class MenuOption {
    private boolean selected;
    private Text element;
    private String text;
    private String selectedText;

    public MenuOption(String optionName) {
        selected = false;

        text = optionName;
        selectedText = "-> " + optionName + " <-";

        Text element = new Text(optionName);
        element.setFont(Font.font("Trebuchet MS", 50));
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
        element.setStyle("-fx-font-size: 60;");
    }

    public void deselect() {
        selected = false;

        element.setStrokeWidth(0);
        element.setText(text);
        element.setStyle("-fx-font-size: 50;");
    }

    public boolean isSelected() {
        return selected;
    }
}
