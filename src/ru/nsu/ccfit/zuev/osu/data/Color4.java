package ru.nsu.ccfit.zuev.osu.data;

import androidx.annotation.NonNull;

import org.anddev.andengine.entity.Entity;

/**
 * Reordered by Reco1l.
 */
public class Color4 {

    private float red;

    private float green;

    private float blue;

    private float alpha = 1f;


    public Color4(Color4 copy) {
        this(copy.red, copy.green, copy.blue, copy.alpha);
    }

    public Color4() {
        red = 0;
        green = 0;
        blue = 0;
    }

    public Color4(float r, float g, float b) {
        red = r;
        green = g;
        blue = b;
    }

    public Color4(float r, float g, float b, float a) {
        red = r;
        green = g;
        blue = b;
        alpha = a;
    }


    public float r() {
        return red;
    }

    public float g() {
        return green;
    }

    public float b() {
        return blue;
    }

    public float a() {
        return alpha;
    }


    public void set(float r, float g, float b) {
        red = r;
        green = g;
        blue = b;
    }


    public void apply(Entity entity) {
        entity.setColor(red, green, blue, alpha);
    }

    public void applyAll(Entity... entities) {
        for (var entity : entities) {
            entity.setColor(red, green, blue, alpha);
        }
    }


    public static Color4 createFromHex(String colorStr) {
        return new Color4(
                Integer.valueOf(colorStr.substring(1, 3), 16) / 255.0f,
                Integer.valueOf(colorStr.substring(3, 5), 16) / 255.0f,
                Integer.valueOf(colorStr.substring(5, 7), 16) / 255.0f,
                1f
        );
    }
}
