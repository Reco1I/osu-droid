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
        setMainScene(new MainScene());
        getMainScene().load(mainActivity);
        setInfo("Loading skin...");
        skinNow = Config.getSkinPath();
        ResourceManager.getInstance().loadSkin(skinNow);
        ScoreLibrary.getInstance().load(mainActivity);
        setLoadingProgress(20);
        PropertiesLibrary.getInstance().load(mainActivity);
        setLoadingProgress(30);
        setGameScene(new GameScene(Engine));
        setSongMenu(new SongMenu());
        setLoadingProgress(40);
        getSongMenu().init(mainActivity, Engine, getGameScene());
        getSongMenu().load();
        setScoring(new ScoringScene(Engine, getGameScene(), getSongMenu()));
        getSongMenu().setScoringScene(getScoring());
        getGameScene().setScoringScene(getScoring());
        getGameScene().setOldScene(getSongMenu().getScene());
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

    public GameScene getGameScene() {
        return gameScene;
    }

    public void setGameScene(GameScene gameScene) {
        this.gameScene = gameScene;
    }

    public MainScene getMainScene() {
        return mainScene;
    }

    public void setMainScene(MainScene mainScene) {
        this.mainScene = mainScene;
    }

    public ScoringScene getScoring() {
        return scoring;
    }

    public void setScoring(ScoringScene scoring) {
        this.scoring = scoring;
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
