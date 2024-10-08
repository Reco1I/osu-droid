package ru.nsu.ccfit.zuev.osu.game;

import com.reco1l.osu.graphics.ExtendedSprite;
import com.reco1l.osu.graphics.Modifiers;
import com.reco1l.osu.graphics.Origin;
import com.reco1l.osu.playfield.NumberedCirclePiece;
import com.rian.osu.mods.ModHidden;

import org.anddev.andengine.entity.scene.Scene;

import ru.nsu.ccfit.zuev.osu.Config;
import ru.nsu.ccfit.zuev.osu.RGBColor;
import ru.nsu.ccfit.zuev.osu.ResourceManager;
import ru.nsu.ccfit.zuev.osu.Utils;
import ru.nsu.ccfit.zuev.osu.scoring.ResultType;
import ru.nsu.ccfit.zuev.skins.OsuSkin;

public class HitCircle extends GameObject {

    private final ExtendedSprite approachCircle;
    private final RGBColor comboColor = new RGBColor();
    private com.rian.osu.beatmap.hitobject.HitCircle beatmapCircle;
    private GameObjectListener listener;
    private Scene scene;
    private float radiusSquared;
    private float passedTime;
    private float timePreempt;
    private boolean kiai;

    /**
     * The circle piece that represents the circle body and overlay.
     */
    private final NumberedCirclePiece circlePiece;


    public HitCircle() {
        circlePiece = new NumberedCirclePiece("hitcircle", "hitcircleoverlay");
        approachCircle = new ExtendedSprite();
        approachCircle.setOrigin(Origin.Center);
        approachCircle.setTextureRegion(ResourceManager.getInstance().getTexture("approachcircle"));
    }

    public void init(final GameObjectListener listener, final Scene pScene,
                     final com.rian.osu.beatmap.hitobject.HitCircle beatmapCircle, final float secPassed,
                     final RGBColor comboColor) {
        // Storing parameters into fields
        this.replayObjectData = null;
        this.beatmapCircle = beatmapCircle;

        var stackedPosition = beatmapCircle.getGameplayStackedPosition();
        position.set(stackedPosition.x, stackedPosition.y);

        this.endsCombo = beatmapCircle.isLastInCombo();
        this.listener = listener;
        this.scene = pScene;
        this.timePreempt = (float) beatmapCircle.timePreempt / 1000;
        passedTime = secPassed - ((float) beatmapCircle.startTime / 1000 - timePreempt);
        startHit = false;
        kiai = GameHelper.isKiai();
        this.comboColor.set(comboColor.r(), comboColor.g(), comboColor.b());

        // Calculating position of top/left corner for sprites and hit radius
        final float scale = beatmapCircle.getGameplayScale();
        radiusSquared = (float) beatmapCircle.getGameplayRadius();
        radiusSquared *= radiusSquared;

        float actualFadeInDuration = (float) beatmapCircle.timeFadeIn / 1000 / GameHelper.getSpeedMultiplier();
        float remainingFadeInDuration = Math.max(0, actualFadeInDuration - passedTime / GameHelper.getSpeedMultiplier());
        float fadeInProgress = 1 - remainingFadeInDuration / actualFadeInDuration;

        // Initializing sprites
        circlePiece.setCircleColor(comboColor.r(), comboColor.g(), comboColor.b());
        circlePiece.setScale(scale);
        circlePiece.setAlpha(fadeInProgress);
        circlePiece.setPosition(this.position.x, this.position.y);

        int comboNum = beatmapCircle.getIndexInCurrentCombo() + 1;
        if (OsuSkin.get().isLimitComboTextLength()) {
            comboNum %= 10;
        }
        circlePiece.setNumberText(comboNum);
        circlePiece.setNumberScale(OsuSkin.get().getComboTextScale());

        approachCircle.setColor(comboColor.r(), comboColor.g(), comboColor.b());
        approachCircle.setScale(scale * (3 - 2 * fadeInProgress));
        approachCircle.setAlpha(0.9f * fadeInProgress);
        approachCircle.setPosition(this.position.x, this.position.y);

        if (GameHelper.isHidden()) {
            approachCircle.setVisible(Config.isShowFirstApproachCircle() && beatmapCircle.isFirstNote());
        }


        if (GameHelper.isHidden()) {
            float actualFadeOutDuration = timePreempt * (float) ModHidden.FADE_OUT_DURATION_MULTIPLIER / GameHelper.getSpeedMultiplier();
            float remainingFadeOutDuration = Math.min(
                actualFadeOutDuration,
                Math.max(0, actualFadeOutDuration + remainingFadeInDuration - passedTime / GameHelper.getSpeedMultiplier())
            );
            float fadeOutProgress = remainingFadeOutDuration / actualFadeOutDuration;

            circlePiece.registerEntityModifier(Modifiers.sequence(
                    Modifiers.alpha(remainingFadeInDuration, fadeInProgress, 1),
                    Modifiers.alpha(remainingFadeOutDuration, fadeOutProgress, 0)
            ));
        } else {
            circlePiece.registerEntityModifier(Modifiers.alpha(remainingFadeInDuration, fadeInProgress, 1));
        }

        if (approachCircle.isVisible()) {
            approachCircle.registerEntityModifier(
                Modifiers.alpha(
                    Math.min(
                        Math.min(actualFadeInDuration * 2, remainingFadeInDuration),
                        timePreempt / GameHelper.getSpeedMultiplier()
                    ),
                    0.9f * fadeInProgress,
                    0.9f
                )
            );

            approachCircle.registerEntityModifier(Modifiers.scale(Math.max(0, timePreempt - passedTime) / GameHelper.getSpeedMultiplier(), approachCircle.getScaleX(), scale));
        }

        if (Config.isDimHitObjects()) {

            // Source: https://github.com/peppy/osu/blob/60271fb0f7e091afb754455f93180094c63fc3fb/osu.Game.Rulesets.Osu/Objects/Drawables/DrawableOsuHitObject.cs#L101
            var dimDelaySec = (timePreempt - objectHittableRange) / GameHelper.getSpeedMultiplier();
            var colorDim = 195f / 255f;

            circlePiece.setColor(colorDim, colorDim, colorDim);
            circlePiece.registerEntityModifier(Modifiers.sequence(
                Modifiers.delay(dimDelaySec),
                Modifiers.color(0.1f / GameHelper.getSpeedMultiplier(),
                    circlePiece.getRed(), 1f,
                    circlePiece.getGreen(), 1f,
                    circlePiece.getBlue(), 1f
                )
            ));
        }

        scene.attachChild(circlePiece, 0);
        scene.attachChild(approachCircle);
    }

    private void playSound() {
        listener.playSamples(beatmapCircle);
    }

    private void removeFromScene() {
        if (scene == null) {
            return;
        }

        circlePiece.clearEntityModifiers();
        approachCircle.clearEntityModifiers();

        // Detach all objects
        circlePiece.detachSelf();
        approachCircle.detachSelf();
        listener.removeObject(this);
        GameObjectPool.getInstance().putCircle(this);
        scene = null;
    }

    private boolean canBeHit() {
        return passedTime >= Math.max(0, timePreempt - objectHittableRange);
    }

    private boolean isHit() {
        for (int i = 0, count = listener.getCursorsCount(); i < count; i++) {

            var inPosition = Utils.squaredDistance(position, listener.getMousePos(i)) <= radiusSquared;
            if (GameHelper.isRelaxMod() && passedTime - timePreempt >= 0 && inPosition) {
                return true;
            }

            var isPressed = listener.isMousePressed(this, i);
            if (isPressed && inPosition) {
                return true;
            } else if (GameHelper.isAutopilotMod() && isPressed) {
                return true;
            }
        }
        return false;
    }

    private double hitOffsetToPreviousFrame() {
        // 因为这里是阻塞队列, 所以提前点的地方会影响判断
        for (int i = 0, count = listener.getCursorsCount(); i < count; i++) {

            var inPosition = Utils.squaredDistance(position, listener.getMousePos(i)) <= radiusSquared;
            if (GameHelper.isRelaxMod() && passedTime - timePreempt >= 0 && inPosition) {
                return 0;
            }

            var isPressed = listener.isMousePressed(this, i);
            if (isPressed && inPosition) {
                return listener.downFrameOffset(i);
            } else if (GameHelper.isAutopilotMod() && isPressed) {
                return 0;
            }
        }
        return 0;
    }


    @Override
    public void update(final float dt) {
        // PassedTime < 0 means circle logic is over
        if (passedTime < 0) {
            removeFromScene();
            return;
        }
        // If we have clicked circle
        if (replayObjectData != null) {
            if (passedTime - timePreempt + dt / 2 > replayObjectData.accuracy / 1000f) {
                final float acc = Math.abs(replayObjectData.accuracy / 1000f);
                if (acc <= GameHelper.getDifficultyHelper().hitWindowFor50(GameHelper.getOverallDifficulty())) {
                    playSound();
                }
                listener.registerAccuracy(replayObjectData.accuracy / 1000f);
                passedTime = -1;
                // Remove circle and register hit in update thread
                listener.onCircleHit(id, replayObjectData.accuracy / 1000f, position,endsCombo, replayObjectData.result, comboColor);
                removeFromScene();
                return;
            }
        } else if (isHit() && canBeHit()) {
            float signAcc = passedTime - timePreempt;
            if (Config.isFixFrameOffset()) {
                signAcc += (float) hitOffsetToPreviousFrame() / 1000f;
            }
            final float acc = Math.abs(signAcc);
            if (acc <= GameHelper.getDifficultyHelper().hitWindowFor50(GameHelper.getOverallDifficulty())) {
                playSound();
            }
            listener.registerAccuracy(signAcc);
            passedTime = -1;
            // Remove circle and register hit in update thread
            float finalSignAcc = signAcc;
            startHit = true;
            listener.onCircleHit(id, finalSignAcc, position, endsCombo, (byte) 0, comboColor);
            removeFromScene();
            return;
        }

        if (GameHelper.isKiai()) {
            var kiaiModifier = (float) Math.max(0, 1 - GameHelper.getCurrentBeatTime() / GameHelper.getBeatLength()) * 0.5f;
            var r = Math.min(1, comboColor.r() + (1 - comboColor.r()) * kiaiModifier);
            var g = Math.min(1, comboColor.g() + (1 - comboColor.g()) * kiaiModifier);
            var b = Math.min(1, comboColor.b() + (1 - comboColor.b()) * kiaiModifier);
            kiai = true;
            circlePiece.setCircleColor(r, g, b);
        } else if (kiai) {
            circlePiece.setCircleColor(comboColor.r(), comboColor.g(), comboColor.b());
            kiai = false;
        }

        passedTime += dt;

        // We are still at approach time. Let entity modifiers finish first.
        if (passedTime < timePreempt) {
            return;
        }

        if (autoPlay) {
            playSound();
            passedTime = -1;
            // Remove circle and register hit in update thread
            listener.onCircleHit(id, 0, position, endsCombo, ResultType.HIT300.getId(), comboColor);
            removeFromScene();
        } else {
            approachCircle.clearEntityModifiers();
            approachCircle.setAlpha(0);

            // If passed too much time, counting it as miss
            if (passedTime > timePreempt + GameHelper.getDifficultyHelper().hitWindowFor50(GameHelper.getOverallDifficulty())) {
                passedTime = -1;
                final byte forcedScore = (replayObjectData == null) ? 0 : replayObjectData.result;

                removeFromScene();
                listener.onCircleHit(id, 10, position, false, forcedScore, comboColor);
            }
        }
    } // update(float dt)

    @Override
    public void tryHit(final float dt) {
        if (isHit() && canBeHit()) {
            float signAcc = passedTime - timePreempt;
            if (Config.isFixFrameOffset()) {
                signAcc += (float) hitOffsetToPreviousFrame() / 1000f;
            }
            final float acc = Math.abs(signAcc);
            if (acc <= GameHelper.getDifficultyHelper().hitWindowFor50(GameHelper.getOverallDifficulty())) {
                playSound();
            }
            listener.registerAccuracy(signAcc);
            passedTime = -1;
            // Remove circle and register hit in update thread
            float finalSignAcc = signAcc;
            listener.onCircleHit(id, finalSignAcc, position, endsCombo, (byte) 0, comboColor);
            removeFromScene();
        }
    }

}
