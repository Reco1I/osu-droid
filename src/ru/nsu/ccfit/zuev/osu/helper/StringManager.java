package ru.nsu.ccfit.zuev.osu.helper;

import androidx.annotation.StringRes;

import java.util.Formatter;

import ru.nsu.ccfit.zuev.osu.Osu;


/**
 * Reordered by Reco1l
 */
public class StringManager {


    private static StringBuilder _builder;

    private static Formatter _formatter;


    private StringManager() {
        // Prevent instantiation
    }


    public static String get(@StringRes int id) {
        return Osu.Activity.getString(id);
    }

    public static String format(@StringRes int id, Object... objects) {

        allocateBuffers();
        _formatter.format(get(id), objects);

        return _builder.toString();
    }


    private static void allocateBuffers() {

        if (_builder == null) {
            _builder = new StringBuilder();
        }

        if (_formatter == null) {
            _formatter = new Formatter(_builder);
        }

        _builder.setLength(0);

    }
}
