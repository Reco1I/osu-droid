package ru.nsu.ccfit.zuev.osu.scoring;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;

import com.rian.osu.ui.SendingPanel;

import org.anddev.andengine.util.Debug;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import ru.nsu.ccfit.zuev.osu.Config;
import ru.nsu.ccfit.zuev.osu.Osu;
import ru.nsu.ccfit.zuev.osu.ToastLogger;
import ru.nsu.ccfit.zuev.osu.game.mods.GameMod;
import ru.nsu.ccfit.zuev.osu.helper.StringManager;
import ru.nsu.ccfit.zuev.osu.helper.sql.DBOpenHelper;
import ru.nsu.ccfit.zuev.osu.online.OnlineScoring;
import ru.nsu.ccfit.zuev.osuplus.R;

/**
 * Reordered by Reco1l
 */
public class ScoreLibrary {


    private static final Pattern PATH_PATTERN = Pattern.compile("[^/]*/[^/]*\\z");

    private static SQLiteDatabase database = null;


    private ScoreLibrary() {
        // Prevent instantiation.
    }


    public static void init() {

        DBOpenHelper helper = DBOpenHelper.getOrCreate(Osu.Activity);

        try {
            database = helper.getWritableDatabase();
        } catch (SQLiteCantOpenDatabaseException e) {
            ToastLogger.showText(StringManager.get(R.string.require_storage_permission), true);
            throw new RuntimeException(e);
        }

        var folder = new File(Config.getCorePath() + "/Scores");
        if (!folder.exists()) {
            return;
        }

        var file = new File(folder, "scoreboard");
        if (!file.exists()) {
            return;
        }

        try(var in = new ObjectInputStream(new FileInputStream(file))) {

            var obj = in.readObject();
            var versionStr = "";

            if (obj instanceof String) {
                versionStr = (String) obj;

                if (!versionStr.equals("scores1") && !versionStr.equals("scores2")) {
                    in.close();
                    return;
                }
            } else {
                return;
            }

            obj = in.readObject();
            Map<String, ArrayList<StatisticV2>> scores = null;

            if (obj instanceof Map<?, ?>) {

                if (versionStr.equals("scores1")) {

                    //noinspection unchecked
                    var oldStat = (Map<String, ArrayList<Statistic>>) obj;

                    scores = new HashMap<>();

                    for (var str : oldStat.keySet()) {

                        var newStat = new ArrayList<StatisticV2>();

                        var stats = oldStat.get(str);
                        assert stats != null;

                        for (var s : stats) {
                            newStat.add(new StatisticV2(s));
                        }

                        var matcher = PATH_PATTERN.matcher(str);
                        if (matcher.find()) {
                            scores.put(matcher.group(), newStat);
                        } else {
                            scores.put(str, newStat);
                        }
                    }
                } else {
                    //noinspection unchecked
                    scores = (Map<String, ArrayList<StatisticV2>>) obj;
                }
            }

            if (scores != null) {
                for (String track : scores.keySet()) {

                    var stats = scores.get(track);
                    assert stats != null;

                    for (var stat : stats) {
                        addScore(track, stat, null);
                    }
                }
            }

        } catch (Exception e) {
            Debug.e("ScoreLibrary.loadOld: " + e.getMessage());
            return;
        }

        file.delete();
    }

    public static void pushScore(StatisticV2 stat, String replay, SendingPanel panel) {

        if (stat.getTotalScoreWithMultiplier() <= 0) {
            return;
        }

        OnlineScoring.getInstance().sendRecord(stat, panel, replay);
    }

    public static void addScore(String trackPath, StatisticV2 stat, String replay) {

        if (stat.getTotalScoreWithMultiplier() == 0 || stat.getMod().contains(GameMod.MOD_AUTO)) {
            return;
        }

        var track = getTrackPath(trackPath);

        if (database == null) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put("filename", track);
        values.put("playername", stat.getPlayerName());
        values.put("replayfile", replay);
        values.put("mode", stat.getModString());
        values.put("score", stat.getTotalScoreWithMultiplier());
        values.put("combo", stat.getMaxCombo());
        values.put("mark", stat.getMark());
        values.put("h300k", stat.getHit300k());
        values.put("h300", stat.getHit300());
        values.put("h100k", stat.getHit100k());
        values.put("h100", stat.getHit100());
        values.put("h50", stat.getHit50());
        values.put("misses", stat.getMisses());
        values.put("accuracy", stat.getAccuracy());
        values.put("time", stat.getTime());
        values.put("perfect", stat.isPerfect() ? 1 : 0);

        database.insert(DBOpenHelper.SCORES_TABLENAME, null, values);
    }

    public static Cursor getMapScores(String[] columns, String filename) {

        var track = getTrackPath(filename);

        if (database == null) {
            return null;
        }
        return database.query(DBOpenHelper.SCORES_TABLENAME, columns, "filename = ?", new String[]{track}, null, null, "score DESC");
    }

    public static String getBestMark(String trackPath) {

        var track = getTrackPath(trackPath);

        String[] columns = {"mark", "filename", "id", "score"};

        try(var response = database.query(DBOpenHelper.SCORES_TABLENAME, columns, "filename = ?", new String[]{track}, null, null, "score DESC")) {

            if (response.getCount() == 0) {
                return null;
            }

            response.moveToFirst();
            return response.getString(0);
        }
    }

    public static StatisticV2 getScore(int id) {

        try(var cursor = database.query(DBOpenHelper.SCORES_TABLENAME, null, "id = " + id, null, null, null, null)) {

            var stat = new StatisticV2();

            if (cursor.getCount() == 0) {
                return stat;
            }

            cursor.moveToFirst();

            stat.setPlayerName(cursor.getString(cursor.getColumnIndexOrThrow("playername")));
            stat.setReplayName(cursor.getString(cursor.getColumnIndexOrThrow("replayfile")));
            stat.setModFromString(cursor.getString(cursor.getColumnIndexOrThrow("mode")));
            stat.setForcedScore(cursor.getInt(cursor.getColumnIndexOrThrow("score")));
            stat.maxCombo = cursor.getInt(cursor.getColumnIndexOrThrow("combo"));
            stat.setMark(cursor.getString(cursor.getColumnIndexOrThrow("mark")));
            stat.hit300k = cursor.getInt(cursor.getColumnIndexOrThrow("h300k"));
            stat.hit300 = cursor.getInt(cursor.getColumnIndexOrThrow("h300"));
            stat.hit100k = cursor.getInt(cursor.getColumnIndexOrThrow("h100k"));
            stat.hit100 = cursor.getInt(cursor.getColumnIndexOrThrow("h100"));
            stat.hit50 = cursor.getInt(cursor.getColumnIndexOrThrow("h50"));
            stat.misses = cursor.getInt(cursor.getColumnIndexOrThrow("misses"));
            stat.accuracy = cursor.getFloat(cursor.getColumnIndexOrThrow("accuracy"));
            stat.time = cursor.getLong(cursor.getColumnIndexOrThrow("time"));
            stat.setPerfect(cursor.getInt(cursor.getColumnIndexOrThrow("perfect")) != 0);

            cursor.close();

            return stat;
        }
    }

    public static String getTrackPath(String track) {

        var matcher = PATH_PATTERN.matcher(track);

        if (matcher.find()) {
            return matcher.group();
        }
        return track;
    }

    public static String getTrackDir(String track) {

        var path = getTrackPath(track);

        if (path.endsWith(".osu")) {
            return path.substring(0, path.indexOf('/'));
        } else {
            return path.substring(path.indexOf('/') + 1);
        }
    }

}
