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
 * Created by Fuuko on 2015/4/24.
 */
public class GlobalManager {


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


    @Nullable
    public static TrackInfo getSelectedTrack() {
        return selectedTrack;
    }

    public static void setSelectedTrack(@Nullable TrackInfo track) {
        selectedTrack = track;
    }

    public static int getLoadingProgress() {
        return loadingProgress;
    }

    public static void setLoadingProgress(int value) {
        loadingProgress = value;
    }

    public static String getLoadingInfo() {
        return loadingInfo;
    }

    public static void setInfo(String info) {
        loadingInfo = info;
    }

}
