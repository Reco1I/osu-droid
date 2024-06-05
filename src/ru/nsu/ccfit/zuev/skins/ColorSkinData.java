package ru.nsu.ccfit.zuev.skins;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import ru.nsu.ccfit.zuev.osu.data.Color4;
import ru.nsu.ccfit.zuev.osu.datatypes.DefaultRGBColor;

public class ColorSkinData extends SkinData<Color4> {
    private final String defaultHex;
    private String currentHex;

    public ColorSkinData(String tag, String defaultHex) {
        super(tag, new DefaultRGBColor(Color4.createFromHex(defaultHex)));
        this.defaultHex = defaultHex;
        this.currentHex = defaultHex;
    }

    @Override
    public void setFromJson(@NonNull JSONObject data) {
        String hex = data.optString(getTag());
        if (hex.isEmpty()) {
            currentHex = defaultHex;
            setCurrentValue(getDefaultValue());
        } else {
            currentHex = hex;
            setCurrentValue(Color4.createFromHex(hex));
        }
    }

    @Override
    public boolean currentIsDefault() {
        return currentHex.equalsIgnoreCase(defaultHex);
    }
}
