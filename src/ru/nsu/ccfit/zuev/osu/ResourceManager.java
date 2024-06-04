package ru.nsu.ccfit.zuev.osu;

import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;

import androidx.annotation.Nullable;

import com.dgsrz.bancho.security.SecurityUtils;
import com.reco1l.osu.graphics.BlankTextureRegion;
import com.reco1l.osu.skinning.IniReader;
import com.reco1l.osu.skinning.SkinConverter;

import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.font.FontFactory;
import org.anddev.andengine.opengl.font.StrokeFont;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;
import org.anddev.andengine.opengl.texture.atlas.bitmap.source.FileBitmapTextureAtlasSource;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.util.Debug;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import ru.nsu.ccfit.zuev.audio.BassSoundProvider;
import ru.nsu.ccfit.zuev.osu.helper.FileUtils;
import ru.nsu.ccfit.zuev.skins.OsuSkin;
import ru.nsu.ccfit.zuev.skins.SkinJsonReader;
import ru.nsu.ccfit.zuev.skins.SkinManager;
import ru.nsu.ccfit.zuev.skins.StringSkinData;

/**
 * Ordered by Reco1l.
 */
public class ResourceManager {

    private final static Map<String, Font> fonts = new HashMap<>();
    private final static Map<String, TextureRegion> textures = new HashMap<>();
    private final static Map<String, BassSoundProvider> sounds = new HashMap<>();
    private final static Map<String, BassSoundProvider> customSounds = new HashMap<>();
    private final static Map<String, TextureRegion> customTextures = new HashMap<>();
    private final static Map<String, Integer> customFrameCount = new HashMap<>();


    private ResourceManager() {
        // Prevent instantiation.
    }


    public static void init() {

        fonts.clear();
        textures.clear();
        sounds.clear();

        customSounds.clear();
        customTextures.clear();
        customFrameCount.clear();

        SecurityUtils.getAppSignature(Osu.Activity, Osu.Activity.getPackageName());
    }


    /// General

    public static void loadSkin(String folder) {

        loadFont("font", null, 28, Color.WHITE);
        loadFont("bigFont", null, 36, Color.WHITE);
        loadFont("smallFont", null, 21, Color.WHITE);
        loadFont("middleFont", null, 24, Color.WHITE);
        loadFont("CaptionFont", null, 35, Color.WHITE);
        loadStrokeFont("strokeFont", null, 36, Color.BLACK, Color.WHITE);

        if (!folder.endsWith("/")) {
            folder = folder + "/";
        }

        loadCustomSkin(folder);

        loadTexture("ranking_enabled", "ranking_enabled.png");
        loadTexture("ranking_disabled", "ranking_disabled.png");
        loadTexture("flashlight_cursor", "flashlight_cursor.png", TextureOptions.BILINEAR_PREMULTIPLYALPHA);

        if (!textures.containsKey("lighting")) {
            textures.put("lighting", null);
        }
    }

    public static void loadCustomSkin(String folder) {

        if (!folder.endsWith("/")) {
            folder += "/";
        }

        File[] files = null;
        File skinFolder = new File(folder);

        if (!skinFolder.exists()) {
            skinFolder = null;
        } else {
            files = FileUtils.listFiles(skinFolder);
        }

        if (files != null) {

            var skinJson = new JSONObject();
            var jsonFile = new File(folder, "skin.json");

            try {
                Osu.setLoadingInfo("Reading skin configuration file...");

                if (jsonFile.exists()) {
                    skinJson = new JSONObject(OsuSkin.readFull(jsonFile));
                } else {
                    var iniFile = new File(folder, "skin.ini");

                    if (iniFile.exists()) {

                        try (var ini = new IniReader(iniFile)) {
                            skinJson = SkinConverter.convertToJson(ini);
                        }

                        SkinConverter.ensureOptionalTexture(new File(folder, "sliderendcircle.png"));
                        SkinConverter.ensureOptionalTexture(new File(folder, "sliderendcircleoverlay.png"));

                        SkinConverter.ensureTexture(new File(folder, "selection-mods.png"));
                        SkinConverter.ensureTexture(new File(folder, "selection-random.png"));
                        SkinConverter.ensureTexture(new File(folder, "selection-options.png"));

                        files = FileUtils.listFiles(skinFolder);
                    }
                }

            } catch (Exception e) {
                Log.e("Resources", "Failed to read skin configuration file.", e);
            }

            SkinJsonReader.getReader().supplyJson(skinJson);
        }

        var fileMap = new HashMap<String, File>();

        if (files != null) {
            for (var file : files) {

                if (file.isFile()) {
                    if (file.getName().startsWith("comboburst") && (file.getName().endsWith(".wav")
                        || file.getName().endsWith(".mp3"))
                        || file.getName().length() < 5
                        || file.length() == 0) {
                        continue;
                    }

                    var filename = file.getName().substring(0, file.getName().length() - 4);
                    fileMap.put(filename, file);

                    if (filename.equals("hitcircle")) {

                        if (!fileMap.containsKey("sliderstartcircle")) {
                            fileMap.put("sliderstartcircle", file);
                        }

                        if (!fileMap.containsKey("sliderendcircle")) {
                            fileMap.put("sliderendcircle", file);
                        }
                    }

                    if (filename.equals("hitcircleoverlay")) {

                        if (!fileMap.containsKey("sliderstartcircleoverlay")) {
                            fileMap.put("sliderstartcircleoverlay", file);
                        }

                        if (!fileMap.containsKey("sliderendcircleoverlay")) {
                            fileMap.put("sliderendcircleoverlay", file);
                        }
                    }
                }
            }
        }

        customFrameCount.clear();

        try {
            var gfxAssets = Osu.Activity.getAssets().list("gfx");
            assert gfxAssets != null;

            for (var asset : gfxAssets) {
                var name = asset.substring(0, asset.length() - 4);

                if (!Config.isCorovans() && (name.equals("count1") || name.equals("count2") || name.equals("count3") || name.equals("go") || name.equals("ready"))) {
                    continue;
                }

                if (fileMap.containsKey(name)) {
                    loadTexture(name, fileMap.get(name));

                    if (Character.isDigit(name.charAt(name.length() - 1))) {
                        noticeFrameCount(name);
                    }
                } else {
                    loadTexture(name, "gfx/" + asset);
                }
            }

            if (fileMap.containsKey("scorebar-kidanger")) {
                loadTexture("scorebar-kidanger", fileMap.get("scorebar-kidanger"));
                loadTexture("scorebar-kidanger2", fileMap.get(fileMap.containsKey("scorebar-kidanger2") ? "scorebar-kidanger2" : "scorebar-kidanger"));
            }

            if (fileMap.containsKey("comboburst")) {
                loadTexture("comboburst", fileMap.get("comboburst"));
            } else {
                unloadTexture("comboburst");
            }

            for (int i = 0; i < 10; i++) {

                var textureName = "comboburst-" + i;
                var file = fileMap.get(textureName);

                if (file != null) {
                    loadTexture(textureName, file);
                } else {
                    unloadTexture(textureName);
                }
            }

            String[] names = {"play-skip-", "menu-back-", "scorebar-colour-", "hit0-", "hit50-", "hit100-", "hit100k-", "hit300-", "hit300k-", "hit300g-"};

            for (var name : names) {
                for (int i = 0; i < 60; i++) {

                    var textureName = name + i;
                    var file = fileMap.get(textureName);

                    if (file != null) {
                        loadTexture(textureName, file);
                    } else {
                        unloadTexture(textureName);
                    }
                }
            }

        } catch (Exception e) {
            Log.e("Resources", "Failed to load GFX assets.", e);
        }

        SkinManager.getInstance().presetFrameCount();

        try {
            var sounds = Osu.Activity.getAssets().list("sfx");
            assert sounds != null;

            for (var s : sounds) {
                var name = s.substring(0, s.length() - 4);
                if (fileMap.containsKey(name)) {
                    loadSound(name, fileMap.get(name));
                } else {
                    loadSound(name, "sfx/" + s);
                }
            }
            if (skinFolder != null) {
                loadSound("comboburst", new File(folder + "comboburst.wav"));

                for (int i = 0; i < 10; i++) {
                    loadSound("comboburst-" + i, new File(folder + "comboburst-" + i + ".wav"));
                }
            }
        } catch (final IOException e) {
            Debug.e("Resources: " + e.getMessage(), e);
        }

        loadTexture("ranking_button", "ranking_button.png");
        loadTexture("ranking_enabled", "ranking_enabled.png");
        loadTexture("ranking_disabled", "ranking_disabled.png");
        loadTexture("selection-approved", "selection-approved.png");
        loadTexture("selection-loved", "selection-loved.png");
        loadTexture("selection-question", "selection-question.png");
        loadTexture("selection-ranked", "selection-ranked.png");
        if (!textures.containsKey("lighting")) textures.put("lighting", null);
    }

    private static void noticeFrameCount(String name) {

        String key;
        if (!name.contains("-")) {
            key = name.substring(0, name.length() - 1);
        } else {
            key = name.substring(0, name.lastIndexOf('-'));
        }

        int frameNum;
        try {
            frameNum = Integer.parseInt(name.substring(key.length()));
        } catch (final NumberFormatException e) {
            return;
        }

        if (frameNum < 0) {
            frameNum *= -1;
        }

        if (!customFrameCount.containsKey(key) || Objects.requireNonNull(customFrameCount.get(key)) < frameNum) {
            customFrameCount.put(key, frameNum);
        }
    }

    /**
     * Unloads all custom resources.
     */
    public static void clearCustomResources() {

        for (var sound : customSounds.values()) {
            sound.free();
        }

        var textures = customTextures.values();

        for (var region : textures) {
            var texture = region.getTexture();

            if (texture != null && texture.isLoadedToHardware()) {
                Osu.Engine.getTextureManager().unloadTexture(texture);
            }
        }

        customSounds.clear();
        customTextures.clear();
        customFrameCount.clear();
    }

    /**
     * Provides the number of frames of a custom texture.
     */
    public static int getFrameCount(final String name) {

        if (!customFrameCount.containsKey(name)) {
            return -1;
        }

        return Objects.requireNonNull(customFrameCount.get(name));
    }


    /// Fonts

    /**
     * Load a font from a file.
     *
     * @param asset The key name of the font.
     * @param file The filename of the font in the assets folder.
     * @param size The size of the font.
     * @param color The color of the font.
     */
    public static Font loadFont(String asset, String file, int size, int color) {

        var texture = new BitmapTextureAtlas(512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

        Font font;
        if (file == null) {
            font = new Font(texture, Typeface.create(Typeface.DEFAULT, Typeface.NORMAL), size, true, color);
        } else {
            font = FontFactory.createFromAsset(texture, Osu.Activity, "fonts/" + file, size, true, color);
        }

        Osu.Engine.getTextureManager().loadTexture(texture);
        Osu.Engine.getFontManager().loadFont(font);

        fonts.put(asset, font);
        return font;
    }

    /**
     * Load a font with stroke from a file.
     *
     * @param asset The key name of the font.
     * @param file The filename of the font in the assets folder.
     * @param size The size of the font.
     * @param color The color of the font.
     * @param strokeColor The color of the stroke.
     */
    public static StrokeFont loadStrokeFont(String asset, String file, int size, int color, int strokeColor) {

        var texture = new BitmapTextureAtlas(512, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

        StrokeFont font;
        if (file == null) {
            font = new StrokeFont(texture, Typeface.create(Typeface.DEFAULT, Typeface.NORMAL), size, true, color, 2f, strokeColor);
        } else {
            font = FontFactory.createStrokeFromAsset(texture, Osu.Activity, "fonts/" + file, size, true, color, 2f, strokeColor);
        }

        Osu.Engine.getTextureManager().loadTexture(texture);
        Osu.Engine.getFontManager().loadFont(font);

        fonts.put(asset, font);
        return font;
    }

    /**
     * Provides a font according to its key name.
     */
    public static Font getFont(String key) {

        if (!fonts.containsKey(key)) {
            return loadFont(key, null, 35, Color.WHITE);
        }

        return fonts.get(key);
    }


    /// Background

    /**
     * Load a texture intended as background.
     */
    public static TextureRegion loadBackground(String filepath) {

        try {
            var source = new FileBitmapTextureAtlasSource(new File(filepath));
            var atlas = new BitmapTextureAtlas(source.getWidth(), source.getHeight(), TextureOptions.BILINEAR);

            var texture = TextureRegionFactory.createFromSource(atlas, source, source.getWidth(), source.getHeight(), false);
            var previous = textures.put("::background", texture);

            if (previous != null) {
                Osu.Engine.getTextureManager().unloadTexture(previous.getTexture());
            }
            Osu.Engine.getTextureManager().loadTexture(texture.getTexture());

            return texture;
        } catch (Exception e) {

            Log.e("Resources", "Failed to load background.", e);
            return textures.get("menu-background");
        }
    }


    /// Textures

    /**
     * Loads a texture from assets folder.
     *
     * @param name The key name of the texture.
     * @param filename The filename of the texture in the assets folder.
     */
    public static TextureRegion loadTexture(String name, String filename) {
        return loadTexture(name, filename, TextureOptions.BILINEAR);
    }

    /**
     * Loads a texture from assets folder.
     *
     * @param name The key name of the texture.
     * @param filename The filename of the texture in the assets folder.
     * @param options The texture options.
     */
    public static TextureRegion loadTexture(String name, String filename, TextureOptions options) {

        try {
            var source = new AssetBitmapTextureAtlasSource(Osu.Activity, filename);
            var atlas = new BitmapTextureAtlas(source.getWidth(), source.getHeight(), options);

            var texture = TextureRegionFactory.createFromSource(atlas, source, source.getWidth(), source.getHeight(), false);
            var previous = textures.put(name, texture);

            if (previous != null) {
                Osu.Engine.getTextureManager().unloadTexture(previous.getTexture());
            }
            Osu.Engine.getTextureManager().loadTexture(texture.getTexture());

            return texture;
        } catch (Exception e) {

            Log.e("Resources", "Failed to load texture asset.", e);
            return BlankTextureRegion.Companion;
        }
    }

    /**
     * Loads a texture from a file, as difference from {@link #loadTexture(String, String)} this is intended
     * to load external textures.
     *
     * @param name The key name of the texture.
     * @param file The file of the texture.
     */
    public static TextureRegion loadTexture(String name, File file) {
        return loadTexture(name, file, TextureOptions.BILINEAR);
    }

    /**
     * Loads a texture from a file, as difference from {@link #loadTexture(String, String)} this is intended
     * to load external textures.
     *
     * @param name The key name of the texture.
     * @param file The file of the texture.
     * @param options The texture options.
     */
    public static TextureRegion loadTexture(String name, File file, TextureOptions options) {

        try {
            // Whenever the file is sampled in 2x size we must know before decoding.
            var isHDTexture = file.getName().contains("@2x");

            var source = new FileBitmapTextureAtlasSource(file, isHDTexture ? 2 : 1);
            var atlas = new BitmapTextureAtlas(source.getWidth(), source.getHeight(), options);

            var texture = TextureRegionFactory.createFromSource(atlas, source, source.getWidth(), source.getHeight(), false);
            var previous = textures.put(name, texture);

            if (previous != null) {
                Osu.Engine.getTextureManager().unloadTexture(previous.getTexture());
            }
            Osu.Engine.getTextureManager().loadTexture(texture.getTexture());

            return texture;
        } catch (Exception e) {

            Log.e("Resources", "Failed to load texture.", e);
            return BlankTextureRegion.Companion;
        }
    }

    public static void loadCustomTexture(final File file) {

        var name = file.getName();
        name = name.substring(0, name.length() - 4).toLowerCase();

        var delimiter = "-";
        var multiframe = false;

        if (Character.isDigit(name.charAt(name.length() - 1))) {

            String key;
            if (!name.contains("-")) {
                key = name.substring(0, name.length() - 1);
            } else {
                key = name.substring(0, name.lastIndexOf('-'));
            }

            if (!textures.containsKey(name) && SkinManager.getFrames(key) == 0) {
                return;
            }

            if (textures.containsKey(key) || textures.containsKey(key + "-0") || textures.containsKey(key + "0")) {
                int frameNum = Integer.parseInt(name.substring(key.length()));

                if (frameNum < 0) {
                    frameNum *= -1;
                }

                if (!customFrameCount.containsKey(key) || Objects.requireNonNull(customFrameCount.get(key)) < frameNum) {
                    customFrameCount.put(key, frameNum);
                }
            }
        } else if (!textures.containsKey(name)) {

            if (textures.containsKey(name + "-0") || textures.containsKey(name + "0")) {

                if (textures.containsKey(name + "0")) {
                    delimiter = "";
                }

                if (SkinManager.getFrames(name) != 0) {
                    customFrameCount.put(name, 1);
                }

                multiframe = true;

            } else {
                return;
            }
        }

        try {

            var source = new FileBitmapTextureAtlasSource(file);
            var atlas = new BitmapTextureAtlas(source.getWidth(), source.getHeight(), TextureOptions.BILINEAR);

            var texture = TextureRegionFactory.createFromSource(atlas, source, source.getWidth(), source.getHeight(), false);
            Osu.Engine.getTextureManager().loadTexture(atlas);

            if (multiframe) {

                int i = 0;
                while (textures.containsKey(name + delimiter + i)) {
                    customTextures.put(name + delimiter + i, texture);
                    i++;
                }

            } else {
                customTextures.put(name, texture);

                if (name.equals("hitcircle")) {

                    if (!customTextures.containsKey("sliderstartcircle")) {
                        customTextures.put("sliderstartcircle", texture);
                    }

                    if (!customTextures.containsKey("sliderendcircle")) {
                        customTextures.put("sliderendcircle", texture);
                    }
                }

                if (name.equals("hitcircleoverlay")) {

                    if (!customTextures.containsKey("sliderstartcircleoverlay")) {
                        customTextures.put("sliderstartcircleoverlay", texture);
                    }

                    if (!customTextures.containsKey("sliderendcircleoverlay")) {
                        customTextures.put("sliderendcircleoverlay", texture);
                    }
                }
            }

        } catch (Exception e) {
            Log.e("Resources", "Failed to load custom texture.", e);
        }
    }

    /**
     * Unloads a texture according to its key name.
     */
    public static void unloadTexture(String name) {

        var region = textures.get(name);
        if (region == null) {
            return;
        }

        Osu.Engine.getTextureManager().unloadTexture(region.getTexture());
        textures.remove(name);
    }

    /**
     * Unloads a texture from and all keys related to its TextureRegion.
     */
    public static void unloadTexture(TextureRegion texture) {

        var toRemove = new ArrayList<String>();

        for (var entry : textures.entrySet()) {
            if (entry.getValue() == texture) {
                toRemove.add(entry.getKey());
            }
        }

        for (var key : toRemove) {
            textures.remove(key);
        }

        Osu.Engine.getTextureManager().unloadTexture(texture.getTexture());
    }

    /**
     * Provides a texture according to its prefix, in case the texture is missing it will
     * returns its default equivalent.
     */
    public static TextureRegion getTextureWithPrefix(StringSkinData prefix, String name) {

        var defaultName = prefix.getDefaultValue() + "-" + name;

        if (SkinManager.isSkinEnabled() && customTextures.containsKey(defaultName)) {
            return customTextures.get(defaultName);
        }

        var customName = prefix.getCurrentValue() + "-" + name;

        if (!textures.containsKey(customName)) {
            loadTexture(customName, new File(Config.getSkinPath() + customName.replace("\\", "") + ".png"));
        }

        if (textures.get(customName) != null) {
            return textures.get(customName);
        }
        return textures.get(defaultName);
    }


    /**
     * Provides a texture according to its key name.
     *
     * @param name The key name of the texture.
     */
    public static TextureRegion getTexture(String name) {
        return getTexture(name, true);
    }

    /**
     * Provides a texture according to its key name.
     *
     * @param name The key name of the texture.
     * @param shouldLoad Whether to load the texture if it was not loaded yet, if `true` it will try to
     * load the texture from the GFX assets folder.
     */
    public static TextureRegion getTexture(String name, boolean shouldLoad) {

        if (SkinManager.isSkinEnabled() && customTextures.containsKey(name)) {
            return customTextures.get(name);
        }

        if (shouldLoad && !textures.containsKey(name)) {
            return loadTexture(name, "gfx/" + name + ".png");
        }

        return textures.get(name);
    }

    /**
     * Whenever the texture is loaded.
     */
    public static boolean isTextureLoaded(final String resname) {
        return textures.containsKey(resname);
    }


    /// Sounds

    /**
     * Loads a sound from a file.
     *
     * @param name The key name of the sound.
     * @param file The sound file.
     */
    public static BassSoundProvider loadSound(String name, File file) {

        try {
            var sound = new BassSoundProvider();

            // Try to load from assets folder instead.
            if (!sound.prepare(file.getAbsolutePath())) {
                return loadSound(name, file.getName());
            }
            sounds.put(name, sound);

            return sound;
        } catch (Exception e) {

            Log.e("Resources", "Failed to load sound file.", e);
            return BassSoundProvider.EMPTY_SOUND;
        }
    }

    /**
     * Loads a sound from an asset.
     *
     * @param name The key name of the sound.
     * @param filename The filename of the sound in the assets folder.
     */
    public static BassSoundProvider loadSound(String name, String filename) {

        try {
            var sound = new BassSoundProvider();

            if (!sound.prepare(Osu.Activity.getAssets(), filename)) {
                return null;
            }
            sounds.put(name, sound);

            return sound;
        } catch (Exception e) {

            Log.e("Resources", "Failed to load sound asset.", e);
            return BassSoundProvider.EMPTY_SOUND;
        }
    }

    /**
     * Loads a custom sound from a file.
     *
     * @param file The sound file.
     */
    public static void loadCustomSound(File file) {

        var sound = new BassSoundProvider();
        var key = file.getName();

        key = key.substring(0, key.length() - 4);
        if (key.isEmpty()) {
            return;
        }

        var matcher = Pattern.compile("([^\\d.]+)").matcher(key);

        if (matcher.find() && !sounds.containsKey(matcher.group(1))) {
            return;
        }

        try {
            if (!sound.prepare(file.getPath())) {
                return;
            }
        } catch (Exception e) {
            Log.e("Resources", "Failed to load custom sound file.", e);
            return;
        }

        customSounds.put(key, sound);
    }

    /**
     * Provides a sound according to its key name.
     */
    public static BassSoundProvider getSound(String name) {

        var sound = sounds.get(name);

        if (sound == null) {
            return loadSound(name, "sfx/" + name + ".wav");
        }
        return sound;
    }

    /**
     * Provides a custom sound according to its key name.
     *
     * @param key The key name of the sound.
     * @param set The variant number of the sound.
     */
    public static BassSoundProvider getCustomSound(String key, int set) {

        if (!SkinManager.isSkinEnabled()) {
            return getSound(key);
        }

        if (set >= 2) {
            if (customSounds.containsKey(key + set)) {
                return customSounds.get(key + set);
            } else {
                return sounds.get(key);
            }
        }

        if (customSounds.containsKey(key)) {
            return customSounds.get(key);
        }

        return sounds.get(key);
    }

}
