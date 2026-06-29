package com.tss.FoodApp.ui;

public abstract class AbstractMenuCommand implements MenuCommand {
    private final String label;

    public AbstractMenuCommand(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
