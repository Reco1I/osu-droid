package ru.nsu.ccfit.zuev.osu.menu;

import android.annotation.SuppressLint;

import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.entity.modifier.FadeOutModifier;
import org.anddev.andengine.entity.modifier.LoopEntityModifier;
import org.anddev.andengine.entity.modifier.RotationByModifier;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.scene.background.SpriteBackground;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.opengl.texture.region.TextureRegion;

import java.util.ArrayList;

import ru.nsu.ccfit.zuev.osu.*;
import ru.nsu.ccfit.zuev.osu.helper.CentredSprite;

public class LoadingScreen implements IUpdateHandler {
    private final LoadingScene scene;
    private final ChangeableText logText;
    private float percentage;

    public LoadingScreen() {
        ArrayList<String> toastLoggerLog = ToastLogger.getLog();

        if (toastLoggerLog != null) {
            toastLoggerLog.clear();
        }

        scene = new LoadingScene();
        scene.registerEntityModifier(new FadeOutModifier(0.4f));

        final TextureRegion tex = ResourceManager.getTexture("menu-background");
        if (tex != null) {
            float height = tex.getHeight();
            height *= Config.screenWidth
                    / (float) tex.getWidth();
            final Sprite menuBg = new Sprite(
                    0,
                    (Config.screenHeight - height) / 2, Config.screenWidth,
                    height, tex);
            scene.setBackground(new SpriteBackground(menuBg));
        } else {
            scene.setBackground(new ColorBackground(70 / 255f, 129 / 255f,
                    252 / 255f));
        }

        final TextureRegion loadingTexture = ResourceManager.getTexture("loading-title");
        final Sprite loadingTitle = new Sprite(0, 0, Config.screenWidth, loadingTexture.getHeight(), loadingTexture);
        scene.attachChild(loadingTitle);

        logText = new ChangeableText(0, 0, ResourceManager.getFont("logFont"), "", 5);
        scene.attachChild(logText);
        ToastLogger.setPercentage(-1);
        percentage = -1;

        final TextureRegion ltexture = ResourceManager.getTexture("loading");
        final Sprite circle = new CentredSprite(Config.screenWidth / 2f,
                Config.screenHeight / 2f, ltexture);
        circle.registerEntityModifier(new LoopEntityModifier(
                new RotationByModifier(2.0f, 360)));
        scene.attachChild(circle);

        scene.registerUpdateHandler(this);
    }

    public Scene getScene() {
        return scene;
    }

    public void show() {
        Osu.Engine.setScene(scene);
    }

    @SuppressLint("DefaultLocale")
    public void onUpdate(final float pSecondsElapsed) {
        if (ToastLogger.getPercentage() != percentage) {
            percentage = ToastLogger.getPercentage();
            logText.setText(String.format("%d%%", (int) percentage));
            logText.setPosition(Config.screenWidth / 2f - logText.getWidth()
                    / 2, Config.screenHeight - Utils.toRes(100));
        }
    }


    public void reset() {
        // TODO Auto-generated method stub
    }


    public static class LoadingScene extends Scene { }
}
