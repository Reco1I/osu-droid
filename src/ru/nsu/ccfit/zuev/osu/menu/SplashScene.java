package ru.nsu.ccfit.zuev.osu.menu;

import com.reco1l.osu.Execution;

import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.entity.modifier.*;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;

import org.anddev.andengine.util.HorizontalAlign;
import ru.nsu.ccfit.zuev.osu.Config;
import ru.nsu.ccfit.zuev.osu.Osu;
import ru.nsu.ccfit.zuev.osu.ResourceManager;

/**
 * Created by Fuuko on 2015/4/25.
 */
public class SplashScene implements IUpdateHandler {

    public static final SplashScene INSTANCE = new SplashScene();
    private final Scene scene;
    private ChangeableText infoText;
    private ChangeableText progressText;
    private Sprite mLoading;
    private boolean mStarting = true;

    public SplashScene() {
        scene = new Scene();
        initializeLoading();
        initializeProgress();
        initializeInfo();
        scene.registerUpdateHandler(this);
    }

    private void initializeLoading() {
        var loadTex = ResourceManager.getTexture("loading_start");

        mLoading = new Sprite(0, 0, loadTex);
        mLoading.setPosition((Config.screenWidth - mLoading.getWidth()) / 2f, (Config.screenHeight - mLoading.getHeight()) / 2f);
        mLoading.setRotationCenter(mLoading.getWidth() / 2f, mLoading.getHeight() / 2f);
        mLoading.setScale(0.4f);
        mLoading.setAlpha(0);

        mLoading.registerEntityModifier(new LoopEntityModifier(new RotationByModifier(2f, 360)));
        scene.attachChild(mLoading);
    }

    private void initializeInfo() {
        infoText = new ChangeableText(0, 0, ResourceManager.getFont("font"), "", HorizontalAlign.CENTER, 1024);
        infoText.setPosition((Config.screenWidth - infoText.getWidth()) / 2, Config.screenHeight - infoText.getHeight() - 20);
        infoText.setAlpha(0);
        infoText.setScale(0.6f);
        scene.attachChild(infoText);
    }

    public Scene getScene() { return scene; }

    public void playWelcomeAnimation()
    {
        mStarting = false;

        mLoading.registerEntityModifier(new FadeOutModifier(0.2f));

        // ChangeableText isn't compatible with animations unfortunately
        Execution.updateThread(() -> {
            infoText.detachSelf();
            progressText.detachSelf();
        });

        try
        {
            Thread.sleep(220);
        }
        catch (InterruptedException ignored)
        {
        }

        var welcomeTex = ResourceManager.getTexture("welcome");
        var welcomeSprite = new Sprite(0, 0, ResourceManager.getTexture("welcome"));

        var welcomeSound = ResourceManager.getSound("welcome");
        var welcomePiano = ResourceManager.getSound("welcome_piano");

        welcomeSprite.setPosition((Config.screenWidth - welcomeTex.getWidth()) / 2f, (Config.screenHeight - welcomeTex.getHeight()) / 2f);
        welcomeSprite.setAlpha(0);
        welcomeSprite.setScaleY(0);
        scene.attachChild(welcomeSprite);
        welcomeSound.play();
        welcomePiano.play();

        welcomeSprite.registerEntityModifier(new ParallelEntityModifier(
                new FadeInModifier(2.5f),
                new SequenceEntityModifier(
                        new ScaleModifier(0.25f, 1f, 1f, 0f, 1f),
                        new ScaleModifier(2.25f, 1f, 1.1f)
                )
        ));
    }

    private void initializeProgress() {
        progressText = new ChangeableText(0, 0, ResourceManager.getFont("font"), "0 %", HorizontalAlign.CENTER, 10);
        progressText.setPosition((Config.screenWidth - progressText.getWidth()) / 2f, (Config.screenHeight + mLoading.getHeight()) / 2f - mLoading.getHeight() / 4f);
        progressText.setAlpha(0);
        progressText.setScale(0.5f);
        scene.attachChild(progressText);
    }

    @Override
    public void onUpdate(float pSecondsElapsed) {
        float progress = Osu.getLoadingProgress();
        if (mStarting)
        {
            mLoading.setAlpha(mLoading.getAlpha() + 0.1f);
        }

        progressText.setText(String.format("%.0f %%", progress));
        progressText.setPosition((Config.screenWidth - progressText.getWidth()) / 2f, (Config.screenHeight + mLoading.getHeight()) / 2f - mLoading.getHeight() / 4f);

        if (Osu.getLoadingInfo() != null) {
            infoText.setText(Osu.getLoadingInfo());
            infoText.setPosition((Config.screenWidth - infoText.getWidth()) / 2, Config.screenHeight - infoText.getHeight() - 20);
        }
    }

    @Override
    public void reset()
    {

    }
}

