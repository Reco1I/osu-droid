package ru.nsu.ccfit.zuev.osu;

import androidx.annotation.Nullable;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.SmoothCamera;

import ru.nsu.ccfit.zuev.audio.serviceAudio.SaveServiceObject;
import ru.nsu.ccfit.zuev.audio.serviceAudio.SongService;
import ru.nsu.ccfit.zuev.osu.game.GameScene;
import ru.nsu.ccfit.zuev.osu.menu.SongMenu;
import ru.nsu.ccfit.zuev.osu.scoring.ScoreLibrary;
import ru.nsu.ccfit.zuev.osu.scoring.ScoringScene;

/**
 * Global entry for every main component used in the game.
 *
 * Originally created by Fuuko on 2015/4/24.
 * Modified by Reco1l on 2024/6/3.
 */
public class Osu {


    public static Engine Engine;

    public static SmoothCamera Camera;

    public static MainActivity Activity;

    public static SongMenu SongMenu;

    public static GameScene GameScene;

    public static MainScene MainScene;

    public static ScoringScene ScoringScene;

    public static SongService SongService;

    public static SaveServiceObject SaveServiceObject;


    private static TrackInfo selectedTrack;

    private static String loadingInfo;

    private static int loadingProgress;


    private Osu() {
        // Prevent instantiation.
    }


    /**
     * Initializes most of the game components, should be called once.
     */
    public static void init() {

        SaveServiceObject = (SaveServiceObject) Activity.getApplication();
        SongService = SaveServiceObject.getSongService();
        setLoadingProgress(10);

        MainScene = new MainScene();
        MainScene.load(Activity);
        setInfo("Loading skin...");
        ResourceManager.getInstance().loadSkin(Config.getSkinPath());
        ScoreLibrary.getInstance().load(Activity);
        setLoadingProgress(20);

        PropertiesLibrary.getInstance().load(Activity);
        setLoadingProgress(30);

        GameScene = new GameScene();
        SongMenu = new SongMenu();
        SongMenu.load();
        setLoadingProgress(40);

        ScoringScene = new ScoringScene();
        GameScene.setOldScene(SongMenu.scene);
        if (SongService != null) {
            SongService.stop();
            SongService.hideNotification();
        }
    }


    /**
     * Provides the current selected track defined by the song menu. Can be `null` when there's no song selected yet.
     */
    @Nullable
    public static TrackInfo getSelectedTrack() {
        return selectedTrack;
    }

    /**
     * Sets the current selected track.
     */
    public static void setSelectedTrack(@Nullable TrackInfo track) {
        selectedTrack = track;
    }

    /**
     * Returns the current loading progress.
     */
    public static int getLoadingProgress() {
        return loadingProgress;
    }

    /**
     * Sets the current loading progress.
     */
    public static void setLoadingProgress(int value) {
        loadingProgress = value;
    }

    /**
     * Returns the current loading information.
     */
    public static String getLoadingInfo() {
        return loadingInfo;
    }

    /**
     * Sets the current loading information.
     */
    public static void setInfo(String value) {
        loadingInfo = value;
    }

}
