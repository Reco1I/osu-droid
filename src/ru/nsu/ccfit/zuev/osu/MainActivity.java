package ru.nsu.ccfit.zuev.osu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.PermissionChecker;
import androidx.preference.PreferenceManager;

import com.edlplan.ui.ActivityOverlay;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.reco1l.ibancho.LobbyAPI;
import com.reco1l.osu.AccessibilityDetector;
import com.reco1l.osu.Execution;
import com.reco1l.osu.multiplayer.Multiplayer;
import com.reco1l.osu.UpdateManager;
import com.reco1l.osu.multiplayer.LobbyScene;
import com.reco1l.osu.multiplayer.RoomScene;

import com.rian.osu.difficulty.BeatmapDifficultyCalculator;
import net.lingala.zip4j.ZipFile;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.SmoothCamera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchController;
import org.anddev.andengine.extension.input.touch.exception.MultiTouchException;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.view.RenderSurfaceView;
import org.anddev.andengine.sensor.accelerometer.AccelerometerData;
import org.anddev.andengine.sensor.accelerometer.IAccelerometerListener;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.Debug;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ru.nsu.ccfit.zuev.audio.BassAudioPlayer;
import ru.nsu.ccfit.zuev.audio.serviceAudio.SaveServiceObject;
import ru.nsu.ccfit.zuev.audio.serviceAudio.SongService;
import ru.nsu.ccfit.zuev.osu.game.SpritePool;
import ru.nsu.ccfit.zuev.osu.helper.FileUtils;
import ru.nsu.ccfit.zuev.osu.helper.InputManager;
import ru.nsu.ccfit.zuev.osu.helper.StringTable;
import ru.nsu.ccfit.zuev.osu.menu.LoadingScreen;
import ru.nsu.ccfit.zuev.osu.menu.ModMenu;
import ru.nsu.ccfit.zuev.osu.menu.SplashScene;
import ru.nsu.ccfit.zuev.osu.online.OnlineManager;
import ru.nsu.ccfit.zuev.osuplus.BuildConfig;
import ru.nsu.ccfit.zuev.osuplus.R;

public class MainActivity extends BaseGameActivity implements
        IAccelerometerListener {

    public static String versionName;
    public static SongService songService;
    public ServiceConnection connection;
    private PowerManager.WakeLock wakeLock = null;
    private String beatmapToAdd = null;
    private SaveServiceObject saveServiceObject;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private FirebaseAnalytics analytics;
    private FirebaseCrashlytics crashlytics;
    private boolean willReplay = false;
    private static boolean activityVisible = true;
    private static final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    // Multiplayer
    private Uri roomInviteLink;

    @Override
    public Engine onLoadEngine() {
        if (!checkPermissions()) {
            return null;
        }
        analytics = FirebaseAnalytics.getInstance(this);
        crashlytics = FirebaseCrashlytics.getInstance();
        Config.loadConfig(this);
        initialGameDirectory();
        StringTable.setContext(this);
        ToastLogger.init(this);
        InputManager.setContext(this);
        OnlineManager.getInstance().Init(getApplicationContext());
        crashlytics.setUserId(Config.getOnlineDeviceID());

        var dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        Config.setScaleMultiplier((float) ((11 - 5.2450170716245195) / 5));

        final PowerManager manager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = manager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "osudroid:osu");

        Osu.Camera = new SmoothCamera(0, 0, Config.getRES_WIDTH(), Config.getRES_HEIGHT(), 0, 1800, 1);

        var options = new EngineOptions(true, null, new RatioResolutionPolicy(Config.getRES_WIDTH(), Config.getRES_HEIGHT()), Osu.Camera);
        options.setNeedsMusic(false);
        options.setNeedsSound(false);
        options.getRenderOptions().disableExtensionVertexBufferObjects();
        options.getTouchOptions().enableRunOnUpdateThread();

        Osu.Engine = new Engine(options);

        try {
            Osu.Engine.setTouchController(new MultiTouchController());
        } catch (final MultiTouchException e) {
            ToastLogger.showText(StringTable.get(R.string.message_error_multitouch), false);
        }

        Osu.Activity = this;

        return Osu.Engine;
    }

    private void initialGameDirectory() {
        File dir = new File(Config.getBeatmapPath());
        // Creating Osu directory if it doesn't exist
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Config.setBeatmapPath(Config.getCorePath() + "Songs/");
                dir = new File(Config.getBeatmapPath());
                if (!(dir.exists() || dir.mkdirs())) {
                    ToastLogger.showText(StringTable.format(
                                    R.string.message_error_createdir, dir.getPath()),
                            true);
                } else {
                    final SharedPreferences prefs = PreferenceManager
                            .getDefaultSharedPreferences(this);
                    final SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("directory", dir.getPath());
                    editor.commit();
                }

            }
            final File nomedia = new File(dir.getParentFile(), ".nomedia");
            try {
                nomedia.createNewFile();
            } catch (final IOException e) {
                Debug.e("LibraryManager: " + e.getMessage(), e);
            }
        }

        final File skinDir = new File(Config.getCorePath() + "/Skin");
        // Creating Osu/Skin directory if it doesn't exist
        if (!skinDir.exists()) {
            skinDir.mkdirs();
        }
    }

    private void initPreferences() {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        if (!prefs.getBoolean("qualitySet", false)) {
            final SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("qualitySet", true);
            final DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);

            if (dm.densityDpi > DisplayMetrics.DENSITY_MEDIUM) {
                editor.putBoolean("lowtextures", false);
            } else {
                editor.putBoolean("lowtextures", false);
            }
            editor.commit();
        }

        if (!prefs.getBoolean("onlineSet", false)) {

            Editor editor = prefs.edit();
            editor.putBoolean("onlineSet", true);
            editor.commit();
        }
    }

    @Override
    public void onLoadResources() {
        ResourceManager.init();
        ResourceManager.loadTexture("welcome", "gfx/welcome.png");
        ResourceManager.loadTexture("loading_start", "gfx/loading.png");

        ResourceManager.loadSound("welcome", "sfx/welcome.ogg");
        ResourceManager.loadSound("welcome_piano", "sfx/welcome_piano.ogg");

        // Setting the scene as fast as we can
        getEngine().setScene(SplashScene.INSTANCE.getScene());

        ResourceManager.loadTexture("logo", "logo.png");
        ResourceManager.loadTexture("play", "play.png");
        ResourceManager.loadTexture("solo", "solo.png");
        ResourceManager.loadTexture("multi", "multi.png");
        ResourceManager.loadTexture("back", "back.png");
        ResourceManager.loadTexture("exit", "exit.png");
        ResourceManager.loadTexture("beatmap_downloader", "beatmap_downloader.png");
        ResourceManager.loadTexture("options", "options.png");
        ResourceManager.loadTexture("offline-avatar", "offline-avatar.png");
        ResourceManager.loadTexture("star", "gfx/star.png");
        ResourceManager.loadTexture("chat", "chat.png");
        ResourceManager.loadTexture("team_vs", "team_vs.png");
        ResourceManager.loadTexture("head_head", "head_head.png");
        ResourceManager.loadTexture("crown", "crown.png");
        ResourceManager.loadTexture("missing", "missing.png");
        ResourceManager.loadTexture("lock", "lock.png");
        ResourceManager.loadTexture("music_play", "music_play.png");
        ResourceManager.loadTexture("music_pause", "music_pause.png");
        ResourceManager.loadTexture("music_stop", "music_stop.png");
        ResourceManager.loadTexture("music_next", "music_next.png");
        ResourceManager.loadTexture("music_prev", "music_prev.png");
        ResourceManager.loadTexture("music_np", "music_np.png");
        ResourceManager.loadTexture("songselect-top", "songselect-top.png");

        File bg;
        if ((bg = new File(Config.getSkinPath() + "menu-background.png")).exists() || (bg = new File(Config.getSkinPath() + "menu-background.jpg")).exists()) {
            ResourceManager.loadTexture("menu-background", bg);
        }
        ResourceManager.loadFont("font", null, 28, Color.WHITE);
        ResourceManager.loadFont("smallFont", null, 21, Color.WHITE);
        ResourceManager.loadStrokeFont("strokeFont", null, 36, Color.BLACK, Color.WHITE);

        ResourceManager.loadSound("heartbeat", "sfx/heartbeat.ogg");
    }

    @Override
    public Scene onLoadScene() {
        return SplashScene.INSTANCE.getScene();
    }

    @Override
    public void onLoadComplete() {

        // Initializing this class because they contain fragments in its constructors that should be initialized in
        // main thread because of the Looper.
        LobbyScene.INSTANCE.init();
        RoomScene.INSTANCE.init();

        Execution.async(() -> {
            BassAudioPlayer.initDevice();
            Osu.init();
            analytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null);
            Osu.setLoadingProgress(50);
            checkNewSkins();
            Config.loadSkins();
            checkNewBeatmaps();

            if (!LibraryManager.INSTANCE.loadLibraryCache(true)) {
                LibraryManager.INSTANCE.scanLibrary();
            }

            SplashScene.INSTANCE.playWelcomeAnimation();

            Execution.delayed(2500, () -> {

                UpdateManager.INSTANCE.onActivityStart();
                Osu.setLoadingInfo("");
                Osu.setLoadingProgress(100);
                ResourceManager.loadFont("font", null, 28, Color.WHITE);
                Osu.Engine.setScene(Osu.MainScene.getScene());
                
                Osu.MainScene.loadBeatmap();
                initPreferences();
                availableInternalMemory();

                scheduledExecutor.scheduleAtFixedRate(() -> {
                    AccessibilityDetector.check(MainActivity.this);
                    BeatmapDifficultyCalculator.invalidateExpiredCache();
                }, 0, 100, TimeUnit.MILLISECONDS);

                if (roomInviteLink != null) {
                    LobbyScene.INSTANCE.connectFromLink(roomInviteLink);
                    return;
                }

                if (willReplay) {
                    Osu.MainScene.watchReplay(beatmapToAdd);
                    willReplay = false;
                }
            });
        });
    }

    /*
    Accuracy isn't the best, but it's sufficient enough
    to determine whether storage is low or not
     */
    private void availableInternalMemory() {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.HALF_EVEN);

        double availableMemory;
        double minMem = 1073741824D; //1 GiB = 1073741824 bytes
        File internal = Environment.getDataDirectory();
        StatFs stat = new StatFs(internal.getPath());
        availableMemory = (double) stat.getAvailableBytes();
        String toastMessage = String.format(StringTable.get(R.string.message_low_storage_space), df.format(availableMemory / minMem));
        if (availableMemory < 0.5 * minMem) { //I set 512MiB as a minimum
            Execution.mainThread(() -> Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show());
        }
        Debug.i("Free Space: " + df.format(availableMemory / minMem));
    }

    @SuppressLint("ResourceType")
    @Override
    protected void onSetContentView() {
        this.mRenderSurfaceView = new RenderSurfaceView(this);
        this.mRenderSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 24, 0);
        this.mRenderSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
        this.mRenderSurfaceView.setRenderer(this.mEngine);

        RelativeLayout layout = new RelativeLayout(this);
        layout.setBackgroundColor(Color.argb(255, 0, 0, 0));
        layout.addView(
                mRenderSurfaceView,
                new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT) {{
                    addRule(RelativeLayout.CENTER_IN_PARENT);
                }});

        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setId(0x28371);
        layout.addView(frameLayout, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        View c = new View(this);
        c.setBackgroundColor(Color.argb(0, 0, 0, 0));
        layout.addView(c, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        this.setContentView(
                layout,
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT) {{
                    gravity = Gravity.CENTER;
                }});

        ActivityOverlay.initial(this, frameLayout.getId());
    }

    public void checkNewBeatmaps() {
        Osu.setLoadingInfo("Checking for new maps...");
        final File mainDir = new File(Config.getCorePath());
        if (beatmapToAdd != null) {
            File file = new File(beatmapToAdd);
            if (file.getName().toLowerCase().endsWith(".osz")) {
                ToastLogger.showText(
                        StringTable.get(R.string.message_lib_importing),
                        false);

                FileUtils.extractZip(beatmapToAdd, Config.getBeatmapPath());
                // LibraryManager.INSTANCE.sort();
                LibraryManager.INSTANCE.saveToCache();
            } else if (file.getName().endsWith(".odr")) {
                willReplay = true;
            }
        } else if (mainDir.exists() && mainDir.isDirectory()) {
            File[] filelist = FileUtils.listFiles(mainDir, ".osz");
            final ArrayList<String> beatmaps = new ArrayList<>();
            for (final File file : filelist) {
                try (var zip = new ZipFile(file)) {
                    if (zip.isValidZipFile()) {
                        beatmaps.add(file.getPath());
                    }
                } catch (IOException ignored) {
                }
            }

            File beatmapDir = new File(Config.getBeatmapPath());
            if (beatmapDir.exists()
                    && beatmapDir.isDirectory()) {
                filelist = FileUtils.listFiles(beatmapDir, ".osz");
                for (final File file : filelist) {
                    try (var zip = new ZipFile(file)) {
                        if (zip.isValidZipFile()) {
                            beatmaps.add(file.getPath());
                        }
                    } catch (IOException ignored) {
                    }
                }
            }

            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (Config.isSCAN_DOWNLOAD()
                    && downloadDir.exists()
                    && downloadDir.isDirectory()) {
                filelist = FileUtils.listFiles(downloadDir, ".osz");
                for (final File file : filelist) {
                    try (var zip = new ZipFile(file)) {
                        if (zip.isValidZipFile()) {
                            beatmaps.add(file.getPath());
                        }
                    } catch (IOException ignored) {
                    }
                }
            }

            if (beatmaps.size() > 0) {
                // final boolean deleteOsz = Config.isDELETE_OSZ();
                // Config.setDELETE_OSZ(true);
                ToastLogger.showText(StringTable.format(
                        R.string.message_lib_importing_several,
                        beatmaps.size()), false);
                for (final String beatmap : beatmaps) {
                    FileUtils.extractZip(beatmap, Config.getBeatmapPath());
                }
                // Config.setDELETE_OSZ(deleteOsz);

                // LibraryManager.INSTANCE.sort();
                LibraryManager.INSTANCE.saveToCache();
            }
        }
    }

    public void checkNewSkins() {
        Osu.setLoadingInfo("Checking new skins...");

        final ArrayList<String> skins = new ArrayList<>();

        // Scanning skin directory
        final File skinDir = new File(Config.getSkinTopPath());

        if (skinDir.exists() && skinDir.isDirectory()) {
            final File[] files = FileUtils.listFiles(skinDir, ".osk");

            for (final File file : files) {
                try (var zip = new ZipFile(file)) {
                    if (zip.isValidZipFile()) {
                        skins.add(file.getPath());
                    }
                } catch (IOException ignored) {
                }
            }
        }

        // Scanning download directory
        final File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        if (Config.isSCAN_DOWNLOAD()
                && downloadDir.exists()
                && downloadDir.isDirectory()) {
            final File[] files = FileUtils.listFiles(downloadDir, ".osk");

            for (final File file : files) {
                try (var zip = new ZipFile(file)) {
                    if (zip.isValidZipFile()) {
                        skins.add(file.getPath());
                    }
                } catch (IOException ignored) {
                }
            }
        }

        if (skins.size() > 0) {
            ToastLogger.showText(StringTable.format(
                    R.string.message_skin_importing_several,
                    skins.size()), false);

            for (final String skin : skins) {
                if (FileUtils.extractZip(skin, Config.getSkinTopPath())) {
                    String folderName = skin.substring(0, skin.length() - 4);
                    // We have imported the skin!
                    ToastLogger.showText(
                            StringTable.format(R.string.message_lib_imported, folderName),
                            true);
                    Config.addSkin(folderName.substring(folderName.lastIndexOf("/") + 1), skin);
                }
            }
        }
    }

    public Handler getHandler() {
        return handler;
    }

    public FirebaseAnalytics getAnalytics() {
        return analytics;
    }

    public PowerManager.WakeLock getWakeLock() {
        return wakeLock;
    }

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);

        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES).versionName;
        } catch (Exception ignored) {
        }

        if (this.mEngine == null) {
            return;
        }

        if (BuildConfig.DEBUG) {
            //Toast.makeText(this,"this is debug version",Toast.LENGTH_LONG).show();
            try {
                File d = new File(Environment.getExternalStorageDirectory(), "osu!droid/Log");
                if (!d.exists()) d.mkdirs();
                File f = new File(d, "rawlog.txt");
                if (!f.exists()) f.createNewFile();
                Runtime.getRuntime().exec("logcat -f " + (f.getAbsolutePath()));
            } catch (IOException ignored) {
            }
        }
        onBeginBindService();
    }

    public void onBeginBindService() {
        if (connection == null && songService == null) {
            connection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    songService = ((SongService.ReturnBindObject) service).getObject();
                    saveServiceObject = (SaveServiceObject) getApplication();
                    saveServiceObject.setSongService(songService);
                    Osu.SongService = songService;
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }

            };

            bindService(new Intent(MainActivity.this, SongService.class), connection, BIND_AUTO_CREATE);
        }
        Osu.SongService = songService;
        Osu.SaveServiceObject = saveServiceObject;
    }

    @Override
    protected void onStart() {
        if (getIntent().getAction() != null && getIntent().getAction().equals(Intent.ACTION_VIEW)) {

            var data = getIntent().getData();

            if (data != null) {

                if (data.toString().startsWith(LobbyAPI.INVITE_HOST))
                    roomInviteLink = data;

                if (ContentResolver.SCHEME_FILE.equals(getIntent().getData().getScheme()))
                    beatmapToAdd = getIntent().getData().getPath();
            }
        }
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.mEngine == null) {
            return;
        }
        activityVisible = true;
        if (Osu.Engine != null) {
            if (Osu.GameScene != null) {
                if (Osu.Engine.getScene() == Osu.GameScene.getScene()) {
                    Osu.Engine.getTextureManager().reloadTextures();
                }
            }
        }
        if (Osu.MainScene != null) {
            if (songService != null && Build.VERSION.SDK_INT > 10) {
                if (songService.hideNotification()) {
                    if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
                    Osu.MainScene.loadBeatmapInfo();
                    Osu.MainScene.loadTimingPoints(false);
                    Osu.MainScene.progressBar.setTime(songService.getLength());
                    Osu.MainScene.progressBar.setPassedTime(songService.getPosition());
                    Osu.MainScene.musicControl(MainScene.MusicOption.SYNC);
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        activityVisible = false;
        if (this.mEngine == null) {
            return;
        }
        if (Osu.Engine != null) {
            if (Osu.GameScene != null) {
                if (Osu.Engine.getScene() == Osu.GameScene.getScene()) {
                    SpritePool.getInstance().purge();

                    if (Multiplayer.isMultiplayer) {
                        ToastLogger.showText("You've left the match.", true);
                        Osu.GameScene.quit();
                        Multiplayer.log("Player left the match.");
                    } else {
                        Osu.GameScene.pause();
                    }
                }
            }
        }
        
        if (Osu.MainScene != null) {
            
            BeatmapInfo beatmapInfo = Osu.MainScene.beatmapInfo;
            if (songService != null && beatmapInfo != null && !songService.isGaming()) {
                songService.showNotification();

                if (wakeLock == null) {
                    PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "osudroid:MainActivity");
                }
                wakeLock.acquire();
            } else {
                if (songService != null) {
                    songService.pause();
                }
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        activityVisible = false;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (this.mEngine == null) {
            return;
        }

        if (getEngine() != null && !hasFocus) {

            if (Osu.GameScene != null) {
                if (getEngine().getScene() == Osu.GameScene.getScene()) {
                    if (!Osu.GameScene.isPaused() && !Multiplayer.isMultiplayer) {
                        Osu.GameScene.pause();
                    }
                }
            }

            if (Multiplayer.isConnected()
                    && (getEngine().getScene() == RoomScene.INSTANCE
                    || getEngine().getScene() == Osu.SongMenu.getScene())) {
                Execution.async(() -> Execution.runSafe(RoomScene.INSTANCE::invalidateStatus));
            }
        }

        if (hasFocus && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Config.isHideNaviBar()) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void onAccelerometerChanged(final AccelerometerData arg0) {
        if (this.mEngine == null) {
            return;
        }
        if (Osu.Camera.getRotation() == 0 && arg0.getY() < -5) {
            Osu.Camera.setRotation(180);
        } else {
            if (Osu.Camera.getRotation() == 180 && arg0.getY() > 5) {
                Osu.Camera.setRotation(0);
            }
        }
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if (this.mEngine == null) {
            return false;
        }

        if (AccessibilityDetector.isIllegalServiceDetected())
            return false;

        if (event.getAction() != KeyEvent.ACTION_DOWN) {
            return super.onKeyDown(keyCode, event);
        }
        
        if (Osu.Engine == null) {
            return super.onKeyDown(keyCode, event);
        }

        if (event.getAction() == TouchEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK && ActivityOverlay.onBackPress()) {
            return true;
        }

        if (Osu.GameScene != null && (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU)) {
            if (Osu.Engine.getScene() == Osu.GameScene.getScene()) {
                if (Osu.GameScene.isPaused()) {
                    Osu.GameScene.resume();
                } else {
                    Osu.GameScene.pause();
                }
                return true;
            }
        }
        if (Osu.ScoringScene != null && keyCode == KeyEvent.KEYCODE_BACK) {
            if (Osu.Engine.getScene() == Osu.ScoringScene.getScene()) {
                Osu.ScoringScene.back();
                return true;
            }
        }
        if ((keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ENTER)) {

            if (Osu.Engine != null) {
                if (Osu.SongMenu != null) {
                    if (Osu.Engine.getScene() == Osu.SongMenu.getScene()) {
                        if (Osu.SongMenu.getScene().hasChildScene()) {
                            if (Osu.SongMenu.getScene().getChildScene() == Osu.SongMenu.getFilterMenu().getScene()) {
                                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                                    InputManager.getInstance().toggleKeyboard();
                                }
                                Osu.SongMenu.getFilterMenu().hideMenu();
                            }

                            if (Osu.SongMenu.getScene().getChildScene() == ModMenu.getInstance().getScene()) {
                                ModMenu.getInstance().hide();
                            }

                            return true;
                        }
                    }
                }
            }
        }
        if (Osu.SongMenu != null) {
            if (Osu.Engine != null && keyCode == KeyEvent.KEYCODE_MENU) {
                if (Osu.Engine.getScene() == Osu.SongMenu.getScene()) {
                    if (!Osu.SongMenu.getScene().hasChildScene()) {
                        Osu.SongMenu.stopScroll(0);
                        Osu.SongMenu.showPropertiesMenu(null);
                        return true;
                    }
                }
            }
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (Osu.Engine != null && Osu.SongMenu != null &&
                    Osu.Engine.getScene() == Osu.SongMenu.getScene()) {

                //SongMenu 界面按返回按钮（系统按钮）
                Osu.SongMenu.back();
            } else {

                
                if (Osu.Engine.getScene() instanceof LoadingScreen.LoadingScene) {
                    return true;
                }

                if (Multiplayer.isMultiplayer) {
                    
                    if (Osu.Engine.getScene() == LobbyScene.INSTANCE) {
                        LobbyScene.INSTANCE.back();
                        return true;
                    }

                    
                    if (Osu.Engine.getScene() == RoomScene.INSTANCE) {

                        if (RoomScene.INSTANCE.hasChildScene() && RoomScene.INSTANCE.getChildScene() == ModMenu.getInstance().getScene()) {
                            ModMenu.getInstance().hide();
                            return true;
                        }
                        runOnUiThread(RoomScene.INSTANCE.getLeaveDialog()::show);
                        return true;
                    }
                }

                
                Osu.MainScene.showExitDialog();
            }
            return true;
        }

        if (InputManager.getInstance().isStarted()) {
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                InputManager.getInstance().pop();
            } else if (keyCode != KeyEvent.KEYCODE_ENTER) {
                final char c = (char) event.getUnicodeChar();
                if (c != 0) {
                    InputManager.getInstance().append(c);
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void forcedExit() {
        if (Osu.Engine.getScene() == Osu.GameScene.getScene()) {
            Osu.GameScene.quit();
        }
        
        Osu.Engine.setScene(Osu.MainScene.getScene());
        
        Osu.MainScene.exit();
    }

    public long getVersionCode() {
        long versionCode = 0;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(
                    getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versionCode = packageInfo.getLongVersionCode();
            } else {
                versionCode = packageInfo.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Debug.e("PackageManager: " + e.getMessage(), e);
        }
        return versionCode;
    }

    public float getRefreshRate() {
        return ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay()
                .getRefreshRate();
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                Environment.isExternalStorageManager()) {
            return true;
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R &&
                PermissionChecker.checkCallingOrSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PermissionChecker.PERMISSION_GRANTED) {
            return true;
        } else {
            Intent grantPermission = new Intent(this, PermissionActivity.class);
            startActivity(grantPermission);
            overridePendingTransition(R.anim.fast_activity_swap, R.anim.fast_activity_swap);
            finish();
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        NotificationManagerCompat.from(getApplicationContext()).cancelAll();
        super.onDestroy();
    }
}
