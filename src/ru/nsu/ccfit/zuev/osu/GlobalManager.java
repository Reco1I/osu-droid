package ru.nsu.ccfit.zuev.osu;

import android.util.DisplayMetrics;

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



    private static GlobalManager instance;
    private GameScene gameScene;
    private MainScene mainScene;
    private ScoringScene scoring;
    private SongMenu songMenu;
    private MainActivity mainActivity;
    private int loadingProgress;
    private String info;
    private SongService songService;
    private TrackInfo selectedTrack;
    private SaveServiceObject saveServiceObject;
    private String skinNow;

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
        saveServiceObject = (SaveServiceObject) mainActivity.getApplication();
        songService = saveServiceObject.getSongService();
        setLoadingProgress(10);

        MainScene = new MainScene();
        MainScene.load(Activity);
        setInfo("Loading skin...");
        skinNow = Config.getSkinPath();
        ResourceManager.getInstance().loadSkin(skinNow);
        ScoreLibrary.getInstance().load(mainActivity);
        setLoadingProgress(20);

        PropertiesLibrary.getInstance().load(mainActivity);
        setLoadingProgress(30);

        GameScene = new GameScene();
        SongMenu = new SongMenu();
        SongMenu.load();
        setLoadingProgress(40);

        ScoringScene = new ScoringScene();
        GameScene.setOldScene(SongMenu.scene);
        if (songService != null) {
            songService.stop();
            songService.hideNotification();
        }
    }

    public String getSkinNow() {
        return skinNow;
    }

    public void setSkinNow(String skinNow) {
        this.skinNow = skinNow;
    }

    public SongMenu getSongMenu() {
        return songMenu;
    }

    public void setSongMenu(SongMenu songMenu) {
        this.songMenu = songMenu;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
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

    public SongService getSongService() {
        return songService;
    }

    public void setSongService(SongService songService) {
        this.songService = songService;
    }

    public SaveServiceObject getSaveServiceObject() {
        return saveServiceObject;
    }

    public void setSaveServiceObject(SaveServiceObject saveServiceObject) {
        this.saveServiceObject = saveServiceObject;
    }

    public DisplayMetrics getDisplayMetrics() {
        final DisplayMetrics dm = new DisplayMetrics();
        mainActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm;
    }
}
