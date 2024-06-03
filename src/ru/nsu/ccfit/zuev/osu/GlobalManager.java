package ru.nsu.ccfit.zuev.osu;

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

    private static GlobalManager instance;
    private int loadingProgress;
    private String info;
    private TrackInfo selectedTrack;

    public static GlobalManager getInstance() {
        if (instance == null) {
            instance = new GlobalManager();
        }
        return instance;
    }

    public TrackInfo getSelectedTrack() {
        return selectedTrack;
    }

    public void setSelectedTrack(TrackInfo selectedTrack) {
        this.selectedTrack = selectedTrack;
    }

    public void init() {
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

    public int getLoadingProgress() {
        return loadingProgress;
    }

    public void setLoadingProgress(int loadingProgress) {
        this.loadingProgress = loadingProgress;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

}
