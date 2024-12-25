package dev.apolonio.asteroids.domain;

import java.util.List;

/**
 * Represents a menu with multiple options the user can select between.
 * <p>
 * Contains methods for selecting between the options, and checking which is currently selected.
 */
public class Menu {
    private final List<MenuOption> OPTIONS;
    private MenuOption selected;

    /**
     * Creates a new menu with the {@link MenuOption MenuOptions} passed as arguments.
     *
     * @param options The {@code MenuOptions} to include in the menu.
     */
    public Menu(MenuOption... options) {
        this.OPTIONS = List.of(options);
        selectFirst();
    }

    /**
     * Returns a {@link List} with the menu's options.
     *
     * @return a {@code List} containing all the menu's options.
     */
    public List<MenuOption> getOptions() {
        return OPTIONS;
    }

    /**
     * Returns the current selected option.
     *
     * @return the selected {@link MenuOption}.
     */
    public MenuOption getSelected() {
        return selected;
    }

    /**
     * Returns the index of the current selected option.
     *
     * @return an {@code int} value representing the index of the selected option.
     */
    public int getSelectedIndex() {
        return OPTIONS.indexOf(selected);
    }

    /**
     * Selects the first option of the menu.
     */
    public void selectFirst() {
        // If there's already a selected option, deselect it first
        if (selected != null) {
            selected.deselect();
        }
        selected = OPTIONS.get(0);
        selected.select();
    }

    /**
     * Selects the last option of the menu
     */
    public void selectLast() {
        // If there's already a selected option, deselect it first
        if (selected != null) {
            selected.deselect();
        }
        selected = OPTIONS.get(OPTIONS.size() - 1);
        selected.select();
    }

    /**
     * Selects the menu option following the current.
     * <p>
     * If the selected option is the last, wraps around selecting the first.
     */
    public void selectNext() {
        selected.deselect();

        int newIndex = (OPTIONS.indexOf(selected) + 1) % OPTIONS.size();
        while (!OPTIONS.get(newIndex).getEnabled()) {
            newIndex++;
            newIndex %= OPTIONS.size();
        }

        selected = OPTIONS.get(newIndex);
        selected.select();

    }

    /**
     * Selects the menu option preceding the current.
     * <p>
     * If the selected option is the first, wraps around selecting the last.
     */
    public void selectPrevious() {
        selected.deselect();

        int newIndex = (OPTIONS.indexOf(selected) - 1 + OPTIONS.size()) % OPTIONS.size();
        while (!OPTIONS.get(newIndex).getEnabled()) {
            newIndex += OPTIONS.size() - 1;
            newIndex %= OPTIONS.size();
        }

        selected = OPTIONS.get(newIndex);
        selected.select();
    }
}
