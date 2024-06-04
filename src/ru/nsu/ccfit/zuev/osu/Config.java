package ru.nsu.ccfit.zuev.osu;

import android.os.Environment;
import android.util.DisplayMetrics;

import androidx.preference.PreferenceManager;

import com.edlplan.favorite.FavoriteLibrary;
import com.edlplan.framework.math.FMath;
import com.google.firebase.messaging.FirebaseMessaging;
import com.reco1l.osu.multiplayer.Multiplayer;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ru.nsu.ccfit.zuev.osu.helper.FileUtils;

/**
 * Reordered by Reco1l.
 */
public class Config {


    /// Strings

    public static String mainDirectory;

    public static String beatmapsDirectory;

    public static String skinsDirectory;

    public static String scoresDirectory;

    public static String skinPath;

    public static String password;

    public static String deviceUUID;


    /// Booleans

    public static boolean synchronizeMusicOffset;

    public static boolean synchronizeFrameOffsetOnInput;

    public static boolean animateFollowCircle;

    public static boolean animateComboText;

    public static boolean showAverageOffsetCounter;

    public static boolean showFPSCounter;

    public static boolean showURCounter;

    public static boolean showPPCounter;

    public static boolean showHitLightingEffects;

    public static boolean showComboburstEffects;

    public static boolean showBurstEffects;

    public static boolean showFirstApproachCircleOnHidden;

    public static boolean showAdvancedStatisticsOnResults;

    public static boolean showVideoBackground;


    public static boolean showScoreboard;

    public static boolean showCountdown;

    public static boolean showCursorTrail;

    public static boolean showCursor;

    public static boolean deleteBeatmapFileOnImportSuccess;

    public static boolean deleteBeatmapFileOnImportFail;

    public static boolean deleteUnsupportedVideos;

    public static boolean forceMetadataRomanization;

    public static boolean forceSkinBackground;

    public static boolean forceComboColors;

    public static boolean precalculateSliderPaths;

    public static boolean scanDownloadDirectory;

    public static boolean submitScoresOnMultiplayer;

    public static boolean allowOnlineConnection;

    public static boolean playMusicPreview;

    public static boolean loadAvatarsInScoreboard;

    public static boolean shrinkPlayfieldDownwards;

    public static boolean keepBackgroundDimOnBreaks;

    public static boolean keepBackgroundAspectRatio;

    public static boolean useCustomSkins;

    public static boolean useCustomSounds;

    public static boolean useSnakingInSliders;

    public static boolean useNightcoreOnMultiplayer;

    public static boolean hideNavigationBar;

    public static boolean hideReplayMarquee;

    public static boolean hideHUD;

    public static boolean receiveAnnouncements;


    /// Numbers

    public static int screenWidth;

    public static int screenHeight;

    public static int errorMeterDisplayMode;

    public static int spinnerStyle;

    public static int metronomeMode;


    public static float soundVolume;

    public static float musicVolume;

    public static float globalOffset;

    public static float backgroundBrightness;

    public static float playfieldScale;

    public static float cursorSize;


    /// Misc

    public static DifficultyAlgorithm difficultyAlgorithm = DifficultyAlgorithm.droid;

    public static Map<String, String> skins = new HashMap<>();

    public static RGBColor[] comboColors;


    /// Private: Properties with custom getters.

    private static String userName;

    private static boolean showStoryboard;

    private static boolean removeSliderLock;



    public static void init() {

        var preferences = PreferenceManager.getDefaultSharedPreferences(Osu.Activity);


        var metrics = new DisplayMetrics();
        Osu.Activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = 1280;
        screenHeight = 1280 * metrics.heightPixels / metrics.widthPixels;


        var defaultMainDirectory = Environment.getExternalStorageDirectory() + "/osu!droid/";
        mainDirectory = ensureDirectory(preferences.getString("corePath", null), defaultMainDirectory);

        skinPath = ensureDirectory(preferences.getString("skinTopPath", null), skinsDirectory);
        skinsDirectory = ensureDirectory(preferences.getString("skinPath", null), mainDirectory + "Skin/");
        beatmapsDirectory = ensureDirectory(preferences.getString("directory", null), mainDirectory + "Songs/");
        scoresDirectory = ensureDirectory(preferences.getString("scoresDirectory", null), skinsDirectory + "Scores/");


        synchronizeFrameOffsetOnInput = preferences.getBoolean("fixFrameOffset", true);
        synchronizeMusicOffset = preferences.getBoolean("syncMusic", synchronizeMusicOffset);

        animateFollowCircle = preferences.getBoolean("animateFollowCircle", true);
        animateComboText = preferences.getBoolean("animateComboText", true);

        keepBackgroundAspectRatio = preferences.getBoolean("keepBackgroundAspectRatio", false);
        keepBackgroundDimOnBreaks = preferences.getBoolean("noChangeDimInBreaks", false);

        showFirstApproachCircleOnHidden = preferences.getBoolean("showfirstapproachcircle", false);
        showAdvancedStatisticsOnResults = preferences.getBoolean("displayScoreStatistics", false);
        showAverageOffsetCounter = preferences.getBoolean("averageOffset", true);
        showHitLightingEffects = preferences.getBoolean("hitlighting", showHitLightingEffects);
        showComboburstEffects = preferences.getBoolean("comboburst", false);
        showVideoBackground = preferences.getBoolean("enableVideo", false);
        showBurstEffects = preferences.getBoolean("bursts", showBurstEffects);
        showCursorTrail = preferences.getBoolean("particles", showCursorTrail);
        showScoreboard = preferences.getBoolean("showscoreboard", true);
        showStoryboard = preferences.getBoolean("enableStoryboard", false);
        showFPSCounter = preferences.getBoolean("fps", true);
        showURCounter = preferences.getBoolean("unstableRate", true);
        showPPCounter = preferences.getBoolean("displayRealTimePPCounter", false);
        showCountdown = preferences.getBoolean("images", false);
        showCursor = preferences.getBoolean("showcursor", false);

        useNightcoreOnMultiplayer = preferences.getBoolean("player_nightcore", false);
        useSnakingInSliders = preferences.getBoolean("snakingInSliders", true);
        useCustomSounds = preferences.getBoolean("beatmapSounds", true);
        useCustomSkins = preferences.getBoolean("skin", false);

        forceMetadataRomanization = preferences.getBoolean("forceromanized", false);
        forceSkinBackground = preferences.getBoolean("safebeatmapbg", false);
        forceComboColors = preferences.getBoolean("useCustomColors", forceComboColors);

        hideReplayMarquee = preferences.getBoolean("hideReplayMarquee", false);
        hideNavigationBar = preferences.getBoolean("hidenavibar", false);
        hideHUD = preferences.getBoolean("hideInGameUI", false);

        deleteBeatmapFileOnImportSuccess = preferences.getBoolean("deleteosz", true);
        deleteBeatmapFileOnImportFail = preferences.getBoolean("deleteUnimportedBeatmaps", false);
        deleteUnsupportedVideos = preferences.getBoolean("deleteUnsupportedVideos", true);

        submitScoresOnMultiplayer = preferences.getBoolean("player_submitScore", true);
        shrinkPlayfieldDownwards = preferences.getBoolean("shrinkPlayfieldDownwards", true);
        precalculateSliderPaths = preferences.getBoolean("calculateSliderPathInGameStart", false);
        errorMeterDisplayMode = Integer.parseInt(preferences.getString("errormeter", "0"));
        scanDownloadDirectory = preferences.getBoolean("scandownload", false);
        receiveAnnouncements = preferences.getBoolean("receiveAnnouncements", true);
        difficultyAlgorithm = DifficultyAlgorithm.droid;
        playMusicPreview = preferences.getBoolean("musicpreview", true);
        removeSliderLock = preferences.getBoolean("removeSliderLock", false);
        playfieldScale = preferences.getInt("playfieldSize", 100) / 100f;
        metronomeMode = Integer.parseInt(preferences.getString("metronomeswitch", "1"));
        spinnerStyle = Integer.parseInt(preferences.getString("spinnerstyle", "0"));


        try {

            globalOffset = (int) FMath.clamp(preferences.getInt("offset", 0), -250, 250);
            backgroundBrightness = preferences.getInt("bgbrightness", 25) / 100f;
            soundVolume = preferences.getInt("soundvolume", 100) / 100f;
            musicVolume = preferences.getInt("bgmvolume", 100) / 100f;
            cursorSize = preferences.getInt("cursorSize", 50) / 100f;

        } catch (RuntimeException e) {

            // According to old documentation this is here due to a crash produced in
            // Android 6.0, we doesn't know if the crash still exists in newer APIs.

            preferences.edit()
                    .putInt("offset", 0)
                    .putInt("bgbrightness", 25)
                    .putInt("soundvolume", 100)
                    .putInt("bgmvolume", 100)
                    .putInt("cursorSize", 50)
                    .commit();

            backgroundBrightness = 25;
            soundVolume = 100;
            musicVolume = 100;
            globalOffset = 0;
            cursorSize = 50;
        }

        comboColors = new RGBColor[4];
        for (int i = 1; i <= 4; i++) {
            comboColors[i - 1] = RGBColor.hex2Rgb(ColorPickerPreference.convertToRGB(preferences.getInt("combo" + i, 0xff000000)));
        }

        deviceUUID = preferences.getString("installID", null);
        if (deviceUUID == null) {
            deviceUUID = UUID.randomUUID().toString().replace("-", "").substring(0, 32);

            preferences.edit()
                .putString("installID", deviceUUID)
                .commit();
        }

        if (receiveAnnouncements) {
            FirebaseMessaging.getInstance().subscribeToTopic("announcements");
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("announcements");
        }

        loadOnline();
        FavoriteLibrary.get().load();
    }


    private static String ensureDirectory(String directory, String _default) {

        if (directory == null || directory.isEmpty() || directory.isBlank()) {
            return _default;
        }

        if (directory.charAt(directory.length() - 1) != '/') {
            directory += "/";
        }

        return directory;
    }


    public static void loadSkins() {

        var folders = FileUtils.listFiles(new File(skinPath), file -> file.isDirectory() && !file.getName().startsWith("."));

        skins = new HashMap<>();

        for (var folder : folders) {
            skins.put(folder.getName(), folder.getPath());
        }
    }

    public static void loadOnline() {

        var preferences = PreferenceManager.getDefaultSharedPreferences(Osu.Activity);

        userName = preferences.getString("onlineUsername", "Guest");
        password = preferences.getString("onlinePassword", null);
        allowOnlineConnection = preferences.getBoolean("stayOnline", false);
        loadAvatarsInScoreboard = preferences.getBoolean("loadAvatar",false);
    }


    /// Custom getters

    /**
     * Whether storyboard should be show, it'll be false if background brightness is very low.
     */
    public static boolean isShowStoryboard() {
        return backgroundBrightness > 0.02 && showStoryboard;
    }

    /**
     * Whether remove the slider lock, it'll be override by the room settings in case of multiplayer.
     */
    public static boolean isRemoveSliderLock() {
        //noinspection DataFlowIssue
        return Multiplayer.isConnected() ? Multiplayer.room.getGameplaySettings().isRemoveSliderLock() : removeSliderLock;
    }

    /**
     * The username defined by the user, if empty it'll return "Guest".
     */
    public static String getUsername() {
        return !userName.isEmpty() ? userName : "Guest";
    }

    /**
     * Whether online connections are enabled.
     */
    public static boolean isOnlineConnectionEnabled() {
        return allowOnlineConnection && Osu.hasOnlineAccess();
    }

}