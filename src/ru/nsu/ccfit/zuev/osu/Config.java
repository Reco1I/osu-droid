package ru.nsu.ccfit.zuev.osu;

import android.content.SharedPreferences;
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
import java.util.Set;
import java.util.UUID;

import ru.nsu.ccfit.zuev.osu.data.Color4;
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

    public static Color4[] comboColors;


    /// Private: Properties with custom getters.

    private static String userName;

    private static boolean showStoryboard;

    private static boolean removeSliderLock;


    private static SharedPreferences preferences;


    public static void init() {

        preferences = PreferenceManager.getDefaultSharedPreferences(Osu.Activity);

        var metrics = new DisplayMetrics();
        Osu.Activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = 1280;
        screenHeight = 1280 * metrics.heightPixels / metrics.widthPixels;


        var defaultMainDirectory = Environment.getExternalStorageDirectory() + "/osu!droid/";
        mainDirectory = ensureDirectory(get("corePath", null), defaultMainDirectory);

        skinPath = ensureDirectory(get("skinTopPath", null), skinsDirectory);
        skinsDirectory = ensureDirectory(get("skinPath", null), mainDirectory + "Skin/");
        beatmapsDirectory = ensureDirectory(get("directory", null), mainDirectory + "Songs/");
        scoresDirectory = ensureDirectory(get("scoresDirectory", null), skinsDirectory + "Scores/");


        synchronizeFrameOffsetOnInput = get("fixFrameOffset", true);
        synchronizeMusicOffset = get("syncMusic", synchronizeMusicOffset);

        animateFollowCircle = get("animateFollowCircle", true);
        animateComboText = get("animateComboText", true);

        keepBackgroundAspectRatio = get("keepBackgroundAspectRatio", false);
        keepBackgroundDimOnBreaks = get("noChangeDimInBreaks", false);

        showFirstApproachCircleOnHidden = get("showfirstapproachcircle", false);
        showAdvancedStatisticsOnResults = get("displayScoreStatistics", false);
        showAverageOffsetCounter = get("averageOffset", true);
        showHitLightingEffects = get("hitlighting", showHitLightingEffects);
        showComboburstEffects = get("comboburst", false);
        showVideoBackground = get("enableVideo", false);
        showBurstEffects = get("bursts", showBurstEffects);
        showCursorTrail = get("particles", showCursorTrail);
        showScoreboard = get("showscoreboard", true);
        showStoryboard = get("enableStoryboard", false);
        showFPSCounter = get("fps", true);
        showURCounter = get("unstableRate", true);
        showPPCounter = get("displayRealTimePPCounter", false);
        showCountdown = get("images", false);
        showCursor = get("showcursor", false);

        useNightcoreOnMultiplayer = get("player_nightcore", false);
        useSnakingInSliders = get("snakingInSliders", true);
        useCustomSounds = get("beatmapSounds", true);
        useCustomSkins = get("skin", false);

        forceMetadataRomanization = get("forceromanized", false);
        forceSkinBackground = get("safebeatmapbg", false);
        forceComboColors = get("useCustomColors", forceComboColors);

        hideReplayMarquee = get("hideReplayMarquee", false);
        hideNavigationBar = get("hidenavibar", false);
        hideHUD = get("hideInGameUI", false);

        deleteBeatmapFileOnImportSuccess = get("deleteosz", true);
        deleteBeatmapFileOnImportFail = get("deleteUnimportedBeatmaps", false);
        deleteUnsupportedVideos = get("deleteUnsupportedVideos", true);

        submitScoresOnMultiplayer = get("player_submitScore", true);
        shrinkPlayfieldDownwards = get("shrinkPlayfieldDownwards", true);
        precalculateSliderPaths = get("calculateSliderPathInGameStart", false);
        errorMeterDisplayMode = Integer.parseInt(get("errormeter", "0"));
        scanDownloadDirectory = get("scandownload", false);
        receiveAnnouncements = get("receiveAnnouncements", true);
        difficultyAlgorithm = DifficultyAlgorithm.droid;
        playMusicPreview = get("musicpreview", true);
        removeSliderLock = get("removeSliderLock", false);
        playfieldScale = get("playfieldSize", 100) / 100f;
        metronomeMode = Integer.parseInt(get("metronomeswitch", "1"));
        spinnerStyle = Integer.parseInt(get("spinnerstyle", "0"));


        try {

            globalOffset = (int) FMath.clamp(get("offset", 0), -250, 250);
            backgroundBrightness = get("bgbrightness", 25) / 100f;
            soundVolume = get("soundvolume", 100) / 100f;
            musicVolume = get("bgmvolume", 100) / 100f;
            cursorSize = get("cursorSize", 50) / 100f;

        } catch (RuntimeException e) {

            // According to old documentation this is here due to a crash produced in
            // Android 6.0, we doesn't know if the crash still exists in newer APIs.
            set("offset", 0);
            set("bgbrightness", 25);
            set("soundvolume", 100);
            set("bgmvolume", 100);
            set("cursorSize", 50);

            backgroundBrightness = 25;
            soundVolume = 100;
            musicVolume = 100;
            globalOffset = 0;
            cursorSize = 50;
        }

        comboColors = new Color4[4];
        for (int i = 1; i <= 4; i++) {
            comboColors[i - 1] = Color4.createFromHex(ColorPickerPreference.convertToRGB(get("combo" + i, 0xff000000)));
        }

        deviceUUID = get("installID", null);
        if (deviceUUID == null) {
            deviceUUID = UUID.randomUUID().toString().replace("-", "").substring(0, 32);
            set("installID", deviceUUID);
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

        userName = get("onlineUsername", "Guest");
        password = get("onlinePassword", null);
        allowOnlineConnection = get("stayOnline", false);
        loadAvatarsInScoreboard = get("loadAvatar", false);
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


    /// Better access

    /**
     * Get a value from preferences.
     * This doesn't guarantee that the stored data is of the required type, if not it'll return the fallback.
     */
    public static <T> T get(String key, T fallback) {
        try {
            //noinspection unchecked
            var value = (T) preferences.getAll().get(key);

            if (value == null) {
                return fallback;
            }
            return value;
        } catch (ClassCastException e) {
            return fallback;
        }
    }


    /**
     * Set a value to preferences. This only will accept values of type String, Int, Float, Boolean or String sets.
     */
    public static <T> void set(String key, T value) {

        var editor = preferences.edit();

        if (value instanceof String v) {
            editor.putString(key, v);
        } else if (value instanceof Integer v) {
            editor.putInt(key, v);
        } else if (value instanceof Float v) {
            editor.putFloat(key, v);
        } else if (value instanceof Boolean v) {
            editor.putBoolean(key, v);
        } else if (value instanceof Set<?>) {
            // Unfortunately there's no better way to check this, if the set isn't a string set it'll throw a CCE.
            //noinspection unchecked
            editor.putStringSet(key, (Set<String>) value);
        }

        editor.commit();
        Config.init();
    }

}