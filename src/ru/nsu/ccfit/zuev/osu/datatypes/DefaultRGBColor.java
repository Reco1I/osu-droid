package ru.nsu.ccfit.zuev.osu.datatypes;

import ru.nsu.ccfit.zuev.osu.data.Color4;

public class DefaultRGBColor extends DefaultData<Color4> {
    private final String instanceDefaultHex = "#FFFFFF";

    public DefaultRGBColor(Color4 defaultValue) {
        super(defaultValue);
    }

    public String instanceDefaultHex() {
        return instanceDefaultHex;
    }

    @Override
    protected Color4 instanceDefaultValue() {
        return Color4.createFromHex(instanceDefaultHex);
    }
}
