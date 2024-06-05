package ru.nsu.ccfit.zuev.skins;

import androidx.annotation.NonNull;

import okio.BufferedSource;
import okio.Okio;
import ru.nsu.ccfit.zuev.osu.data.Color4;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class OsuSkin {
    private static final OsuSkin skinJson = new OsuSkin();

    protected final FloatSkinData comboTextScale = new FloatSkinData("comboTextScale", 1f);
    protected final FloatSkinData sliderHintWidth = new FloatSkinData("sliderHintWidth", 3f);
    protected final FloatSkinData sliderBodyWidth = new FloatSkinData("sliderBodyWidth", 61f);
    protected final FloatSkinData sliderBorderWidth = new FloatSkinData("sliderBorderWidth", 5.2f);
    protected final FloatSkinData sliderBodyBaseAlpha = new FloatSkinData("sliderBodyBaseAlpha", 0.7f);
    protected final FloatSkinData sliderHintAlpha = new FloatSkinData("sliderHintAlpha");
    protected final FloatSkinData sliderHintShowMinLength = new FloatSkinData("sliderHintShowMinLength", 300f);
    protected final FloatSkinData hitCircleOverlap = new FloatSkinData("hitCircleOverlap", -2);

    protected final BooleanSkinData limitComboTextLength = new BooleanSkinData("limitComboTextLength");
    protected final BooleanSkinData disableKiai = new BooleanSkinData("disableKiai");
    protected final BooleanSkinData sliderHintEnable = new BooleanSkinData("sliderHintEnable");
    protected final BooleanSkinData sliderFollowComboColor = new BooleanSkinData("sliderFollowComboColor", true);
    protected final BooleanSkinData useNewLayout = new BooleanSkinData("useNewLayout");
    protected final BooleanSkinData forceOverrideComboColor = new BooleanSkinData("forceOverride");
    protected final BooleanSkinData rotateCursor = new BooleanSkinData("rotateCursor", true);

    protected final String DEFAULT_COLOR_HEX = "#FFFFFF";
    protected final ArrayList<Color4> comboColor = new ArrayList<>();

    protected final ColorSkinData sliderBorderColor = new ColorSkinData("sliderBorderColor", DEFAULT_COLOR_HEX);
    protected final ColorSkinData sliderBodyColor = new ColorSkinData("sliderBodyColor", DEFAULT_COLOR_HEX);
    protected final ColorSkinData sliderHintColor = new ColorSkinData("sliderHintColor", DEFAULT_COLOR_HEX);

    protected final StringSkinData hitCirclePrefix = new StringSkinData("hitCirclePrefix", "default");
    protected final StringSkinData scorePrefix = new StringSkinData("scorePrefix", "score");
    protected final StringSkinData comboPrefix = new StringSkinData("comboPrefix", "score");

    protected final HashMap<String, SkinLayout> layoutData = new HashMap<>();
    protected final HashMap<String, Color4> colorData = new HashMap<>();

    public static OsuSkin get() {
        return skinJson;
    }

    @NonNull
    public static String readFull(File file) throws IOException {
        BufferedSource source = Okio.buffer(Okio.source(file));
        String result = source.readUtf8();
        source.close();
        return result;
    }

    public boolean isRotateCursor() {
        return rotateCursor.getCurrentValue();
    }

    public float getComboTextScale() {
        return comboTextScale.getCurrentValue();
    }

    public boolean isUseNewLayout() {
        return useNewLayout.getCurrentValue();
    }

    public boolean isSliderHintEnable() {
        return sliderHintEnable.getCurrentValue();
    }

    public float getSliderHintAlpha() {
        return sliderHintAlpha.getCurrentValue();
    }

    public float getSliderHintWidth() {
        return sliderHintWidth.getCurrentValue();
    }

    public Color4 getSliderHintColor() {
        return sliderHintColor.getCurrentValue();
    }

    public float getSliderHintShowMinLength() {
        return sliderHintShowMinLength.getCurrentValue();
    }

    public float getSliderBodyWidth() {
        return sliderBodyWidth.getCurrentValue();
    }

    public float getSliderBorderWidth() {
        return sliderBorderWidth.getCurrentValue();
    }

    public boolean isDisableKiai() {
        return disableKiai.getCurrentValue();
    }

    public boolean isLimitComboTextLength() {
        return limitComboTextLength.getCurrentValue();
    }

    public float getSliderBodyBaseAlpha() {
        return sliderBodyBaseAlpha.getCurrentValue();
    }

    public boolean isForceOverrideComboColor() {
        return forceOverrideComboColor.getCurrentValue();
    }

    public ArrayList<Color4> getComboColor() {
        if (comboColor.isEmpty()) {
            comboColor.add(Color4.createFromHex(DEFAULT_COLOR_HEX));
        }
        return comboColor;
    }

    public boolean isForceOverrideSliderBorderColor() {
        return !sliderBorderColor.currentIsDefault();
    }

    public Color4 getSliderBorderColor() {
        return sliderBorderColor.getCurrentValue();
    }

    public boolean isSliderFollowComboColor() {
        return sliderFollowComboColor.getCurrentValue();
    }

    public Color4 getSliderBodyColor() {
        return sliderBodyColor.getCurrentValue();
    }

    public SkinLayout getLayout(String name) {
        return layoutData.get(name);
    }

    public Color4 getColor(String name, Color4 fallback) {
        Color4 color = colorData.get(name);
        return color == null ? fallback : color;
    }

    public StringSkinData getHitCirclePrefix() {
        return hitCirclePrefix;
    }

    public StringSkinData getScorePrefix() {
        return scorePrefix;
    }

    public StringSkinData getComboPrefix() {
        return comboPrefix;
    }

    public float getHitCircleOverlap() {
        return hitCircleOverlap.getCurrentValue();
    }

    public void reset() {
        layoutData.clear();
        colorData.clear();
    }
}
