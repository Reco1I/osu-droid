package com.edlplan.replay;

import ru.nsu.ccfit.zuev.osu.Config;

import java.io.File;

public class OdrConfig {

    public static File getSongDir() {
        return new File(Config.beatmapsDirectory);
    }

    public static File getDatabaseDir() {
        return new File(Config.mainDirectory + "/databases");
    }

    public static File getScoreDir() {
        return new File(Config.scoresDirectory);
    }

    public static File getMainDatabase() {
        return new File(getDatabaseDir(), "osudroid_test.db");
    }

}
