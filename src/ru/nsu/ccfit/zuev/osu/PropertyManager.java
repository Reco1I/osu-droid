package ru.nsu.ccfit.zuev.osu;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Reordered by Reco1l
 */
public class PropertyManager {

    private static String version = "properties1";

    private static Map<String, BeatmapProperties> properties = new HashMap<>();


    private PropertyManager() {
        // Prevent instantiation
    }


    public static void init() {

        var lib = new File(Osu.Activity.getFilesDir(), "properties");
        if (!lib.exists()) {
            return;
        }

        try(var istream = new ObjectInputStream(new FileInputStream(lib))) {

            var obj = istream.readObject();

            if (obj instanceof String) {
                if (!obj.equals(version)) {
                    return;
                }
            } else {
                return;
            }

            obj = istream.readObject();

            if (obj instanceof Map<?, ?>) {
                //noinspection unchecked
                properties = (Map<String, BeatmapProperties>) obj;
            }

        } catch (IOException | ClassNotFoundException e) {
            Log.e("PropertyLibrary", "Failed to initialize properties.", e);
        }
    }

    public static synchronized void save() {

        var lib = new File(Osu.Activity.getFilesDir(), "properties");

        try(var ostream = new ObjectOutputStream(new FileOutputStream(lib))) {

            ostream.writeObject(version);
            ostream.writeObject(properties);

        } catch (IOException e) {
            Log.e("PropertyLibrary", "Failed to save properties.", e);
        }
    }

    public static BeatmapProperties getProperties(String path) {

        if (properties.containsKey(path)) {
            return properties.get(path);
        }
        return null;
    }

    public static void setProperties(String path, BeatmapProperties beatmapProperties) {

        init();
        properties.put(path, beatmapProperties);

        if (!beatmapProperties.favorite && beatmapProperties.getOffset() == 0) {
            properties.remove(path);
        }
    }

    public static synchronized void clear() {
        new File(Osu.Activity.getFilesDir(), "properties").delete();
        properties.clear();
    }
}
