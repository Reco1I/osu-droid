package ru.nsu.ccfit.zuev.osu.game;

import android.graphics.PointF;

import com.reco1l.framework.Pool;
import com.reco1l.osu.graphics.AnimatedSprite;
import com.reco1l.osu.graphics.ExtendedSprite;
import com.rian.osu.math.Vector2;

import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.util.MathUtils;

import java.util.ArrayList;

import ru.nsu.ccfit.zuev.osu.ResourceManager;
import ru.nsu.ccfit.zuev.skins.SkinManager;

public class FollowTrack extends GameObject {

    private static int FRAME_COUNT = 1;

    public static final Pool<ExtendedSprite> pointSpritePool = new Pool<>(pool -> {

        if (FRAME_COUNT == 1) {
            var sprite = new ExtendedSprite();
            sprite.setTextureRegion(ResourceManager.getInstance().getTexture("followpoint"));
            return sprite;
        } else {
            return new AnimatedSprite("followpoint-", FRAME_COUNT);
        }
    });

    private final ArrayList<ExtendedSprite> points = new ArrayList<>();
    private GameObjectListener listener;
    private float timeLeft;
    private float time;
    private boolean empty;
    private float approach;

    public FollowTrack() {
        FRAME_COUNT = SkinManager.getFrames("followpoint");
    }

    public void init(final GameObjectListener listener, final Scene scene,
                     final Vector2 start, final Vector2 end, final float time,
                     final float approachtime, final float scale) {
        this.listener = listener;
        this.approach = approachtime;
        timeLeft = time;
        this.time = 0;

        final float dist = MathUtils.distance(start.x, start.y, end.x, end.y);
        final float angle = (float) Math.atan2(end.y - start.y, end.x - start.x);
        TextureRegion region = ResourceManager.getInstance().getTexture(FRAME_COUNT > 1 ? "followpoint-0" : "followpoint");
        if (region == null) {
            region = ResourceManager.getInstance().getTexture("followpoint");
        }
        final float pointSize = region.getWidth() * scale;
        int count = (int) ((dist - 64 * scale) / pointSize);
        if (count > 0) {
            count--;
        }
        count = Math.min(count, 30);
        if (count <= 0) {
            empty = true;
            GameObjectPool.getInstance().putTrack(this);
            return;
        }
        empty = false;

        points.clear();
        float percent;
        for (int i = 0; i < count; i++) {
            percent = 1 - (i + 1) / (float) (count + 1)/* - 32 * scale / dist */;

            var x = start.x * percent + end.x * (1 - percent);
            var y = start.y * percent + end.y * (1 - percent);

            var point = pointSpritePool.obtain();

            point.setPosition(x - pointSize * 0.5f, y - pointSize * 0.5f);
            point.setScale(scale);
            point.setAlpha(0);
            point.setRotation((float) (angle * 180 / Math.PI));
            scene.attachChild(point, 0);
            points.add(point);
        }

        listener.addPassiveObject(this);
    }


    @Override
    public void update(final float dt) {
        if (empty) {
            return;
        }
        time += dt;

        if (timeLeft <= approach) {
            float percent = (time) / (approach * 0.5f);
            if (percent > 1) {
                percent = 1;
            }
            for (int i = 0; i < points.size(); i++) {
                points.get(i).setAlpha(percent);
            }
        } else if (time < timeLeft - approach) {
            float percent = (time) / (timeLeft - approach);
            if (percent > 1) {
                percent = 1;
            }
            for (int i = 0; i < percent * points.size(); i++) {
                points.get(i).setAlpha(1);
            }
            if (percent < 1) {
                points.get((int) (percent * points.size())).setAlpha(
                        percent - (int) percent);
            }
        } else {
            float percent = 1 - (timeLeft - time) / approach;
            if (percent > 1) {
                percent = 1;
            }
            for (int i = 0; i < percent * points.size(); i++) {
                points.get(i).setAlpha(0);
            }
            if (percent >= 0 && percent < 1) {
                points.get((int) (percent * points.size())).setAlpha(
                        1 - percent);
            }
        }

        if (time >= timeLeft) {
            empty = true;

            for (int i = 0, pointsSize = points.size(); i < pointsSize; i++) {
                var sp = points.get(i);
                sp.detachSelf();
                pointSpritePool.free(sp);
            }
            listener.removePassiveObject(FollowTrack.this);
            GameObjectPool.getInstance().putTrack(FollowTrack.this);
        }
    }
}
