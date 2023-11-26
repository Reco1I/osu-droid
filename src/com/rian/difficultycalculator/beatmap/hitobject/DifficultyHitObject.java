package com.rian.difficultycalculator.beatmap.hitobject;

import com.rian.difficultycalculator.beatmap.hitobject.sliderobject.SliderHitObject;
import com.rian.difficultycalculator.beatmap.hitobject.sliderobject.SliderRepeat;
import com.rian.difficultycalculator.math.MathUtils;
import com.rian.difficultycalculator.math.Vector2;

import java.util.ArrayList;

/**
 * Represents a hit object with additional information for difficulty calculation.
 */
public class DifficultyHitObject {
    /**
     * The underlying hit object.
     */
    public final HitObject object;

    /**
     * The index of this hit object in the list of all hit objects.
     * <br><br>
     * This is one less than the actual index of the hit object in the beatmap.
     */
    public int index;

    /**
     * The preempt time of the hit object.
     */
    public double baseTimePreempt;

    /**
     * Adjusted preempt time of the hit object, taking speed multiplier into account.
     */
    public double timePreempt;

    /**
     * The fade in time of the hit object.
     */
    public double timeFadeIn = 400;

    /**
     * The aim strain generated by the hit object if sliders are considered.
     */
    public double aimStrainWithSliders;

    /**
     * The aim strain generated by the hit object if sliders are not considered.
     */
    public double aimStrainWithoutSliders;

    /**
     * The tap strain generated by the hit object.
     */
    public double speedStrain;

    /**
     * The rhythm multiplier generated by the hit object. This is used to alter tap strain.
     */
    public double rhythmMultiplier;

    /**
     * The flashlight strain generated by the hit object.
     */
    public double flashlightStrain;

    /**
     * The normalized distance from the "lazy" end position of the previous hit object to the start position of this hit object.
     * <br><br>
     * The "lazy" end position is the position at which the cursor ends up if the previous hit object is followed with as minimal movement as possible (i.e. on the edge of slider follow circles).
     */
    public double lazyJumpDistance;

    /**
     * The normalized shortest distance to consider for a jump between the previous hit object and this hit object.
     * <br><br>
     * This is bounded from above by <code>lazyJumpDistance</code>, and is smaller than the former if a more natural path is able to be taken through the previous hit object.
     * <br><br>
     * Suppose a linear slider - circle pattern. Following the slider lazily (see: <code>lazyJumpDistance</code>) will result in underestimating the true end position of the slider as being closer towards the start position.
     * As a result, <code>lazyJumpDistance</code> overestimates the jump distance because the player is able to take a more natural path by following through the slider to its end,
     * such that the jump is felt as only starting from the slider's true end position.
     * <br><br>
     * Now consider a slider - circle pattern where the circle is stacked along the path inside the slider.
     * In this case, the lazy end position correctly estimates the true end position of the slider and provides the more natural movement path.
     */
    public double minimumJumpDistance;

    /**
     * The time taken to travel through <code>minimumJumpDistance</code>, with a minimum value of 25ms.
     */
    public double minimumJumpTime;

    /**
     * The normalized distance between the start and end position of this hit object.
     */
    public double travelDistance;

    /**
     * The time taken to travel through <code>travelDistance</code>, with a minimum value of 25ms for sliders.
     */
    public double travelTime;

    /**
     * Angle the player has to take to hit this hit object.
     * <br><br>
     * Calculated as the angle between the circles (current-2, current-1, current).
     * <br><br>
     * Will be <code>NaN</code> if the hit object does not form an angle.
     */
    public double angle = Double.NaN;

    /**
     * The amount of milliseconds elapsed between this hit object and the last hit object.
     */
    public double deltaTime;

    /**
     * The amount of milliseconds elapsed since the start time of the previous hit object, with a minimum of 25ms.
     */
    public double strainTime;

    /**
     * Adjusted start time of the hit object, taking speed multiplier into account.
     */
    public double startTime;

    /**
     * Adjusted end time of the hit object, taking speed multiplier into account.
     */
    public double endTime;

    /**
     * Adjusted velocity of the hit object, taking speed multiplier into account.
     */
    public double velocity;

    /**
     * Other hit objects in the beatmap, including this hit object.
     */
    private final ArrayList<DifficultyHitObject> difficultyHitObjects;

    /**
     * A distance by which all distances should be scaled in order to assume a uniform circle size.
     */
    private static final int normalizedRadius = 50;
    private static final int minDeltaTime = 25;

    private final float assumedSliderRadius = normalizedRadius * 1.8f;

    private final HitObject lastObject;
    private final HitObject lastLastObject;

    /**
     * @param object The <code>HitObject</code> which this <code>DifficultyHitObject</code> wraps.
     * @param lastObject The <code>HitObject</code> which occurs before <code>object</code>.
     * @param lastLastObject The <code>HitObject</code> which occurs before <code>lastObject</code>.
     * @param clockRate The clock rate being calculated.
     * @param difficultyHitObjects All <code>DifficultyHitObject</code>s in the processed beatmap.
     * @param index The index of the underlying <code>HitObject</code>.
     * @param timePreempt The preempt time of the <code>HitObject</code>.
     * @param isForceAR Whether force AR is used.
     */
    public DifficultyHitObject(HitObject object, HitObject lastObject, HitObject lastLastObject,
                               double clockRate, ArrayList<DifficultyHitObject> difficultyHitObjects,
                               int index, double timePreempt, boolean isForceAR) {
        this.object = object;
        this.lastObject = lastObject;
        this.lastLastObject = lastLastObject;
        this.index = index;
        this.difficultyHitObjects = difficultyHitObjects;
        baseTimePreempt = timePreempt;
        this.timePreempt = timePreempt;

        // Preempt time can go below 450ms. Normally, this is achieved via the DT mod which uniformly speeds up all animations game wide regardless of AR.
        // This uniform speedup is hard to match 1:1, however we can at least make AR>10 (via mods) feel good by extending the upper linear function above.
        // Note that this doesn't exactly match the AR>10 visuals as they're classically known, but it feels good.
        // This adjustment is necessary for AR>10, otherwise TimePreempt can become smaller leading to hit circles not fully fading in.
        timeFadeIn *= Math.min(1, timePreempt / 450);

        if (!isForceAR) {
            this.timePreempt /= clockRate;
        }

        deltaTime = (object.startTime - lastObject.getStartTime()) / clockRate;
        startTime = object.startTime / clockRate;

        if (object instanceof HitObjectWithDuration) {
            endTime = ((HitObjectWithDuration) object).endTime / clockRate;
        } else {
            endTime = startTime;
        }

        // Capped to 25ms to prevent difficulty calculation breaking from simultaneous objects.
        strainTime = Math.max(deltaTime, minDeltaTime);

        setDistances(clockRate);
    }

    /**
     * Gets the difficulty hit object at a specific index with respect to the current
     * difficulty hit object's index.
     * <br><br>
     * Will return <code>null</code> if the index is out of range.
     *
     * @param backwardsIndex The index to move backwards for.
     * @return The difficulty hit object at the index with respect to the current
     * difficulty hit object's index, <code>null</code> if the index is out of range.
     */
    public DifficultyHitObject previous(int backwardsIndex) {
        try {
            return difficultyHitObjects.get(index - (backwardsIndex + 1));
        } catch (IndexOutOfBoundsException ignored) {
            return null;
        }
    }

    /**
     * Gets the difficulty hit object at a specific index with respect to the current
     * difficulty hit object's index.
     * <br><br>
     * Will return <code>null</code> if the index is out of range.
     *
     * @param forwardsIndex The index to move forwards for.
     * @return The difficulty hit object at the index with respect to the current
     * difficulty hit object's index, <code>null</code> if the index is out of range.
     */
    public DifficultyHitObject next(int forwardsIndex) {
        try {
            return difficultyHitObjects.get(index + forwardsIndex + 1);
        } catch (IndexOutOfBoundsException ignored) {
            return null;
        }
    }

    /**
     * Calculates the opacity of the hit object at a given time.
     *
     * @param time The time to calculate the hit object's opacity at.
     * @param isHidden Whether Hidden mod is used.
     * @return The opacity of the hit object at the given time.
     */
    public double opacityAt(double time, boolean isHidden) {
        if (time > object.startTime) {
            // Consider a hit object as being invisible when its start time is passed.
            // In reality the hit object will be visible beyond its start time up until its hittable window has passed,
            // but this is an approximation and such a case is unlikely to be hit where this function is used.
            return 0;
        }

        double fadeInStartTime = object.startTime - baseTimePreempt;
        double fadeInDuration = isHidden ? baseTimePreempt * 0.4 : timeFadeIn;

        double nonHiddenOpacity = MathUtils.clamp((time - fadeInStartTime) / fadeInDuration, 0, 1);

        if (isHidden) {
            double fadeOutStartTime = fadeInStartTime + fadeInDuration;
            double fadeOutDuration = baseTimePreempt * 0.3;

            return Math.min(nonHiddenOpacity, 1 - MathUtils.clamp((time - fadeOutStartTime) / fadeOutDuration, 0, 1));
        }

        return nonHiddenOpacity;
    }

    private void setDistances(double clockRate) {
        if (object instanceof Slider) {
            Slider slider = (Slider) object;
            velocity = slider.velocity * clockRate;
            computeSliderCursorPosition(slider);

            travelDistance = slider.lazyTravelDistance;
            // Bonus for repeat sliders until a better per nested object strain system can be achieved.
            travelDistance *= (float) Math.pow(1 + (slider.repeatCount - 1) / 2.5, 1 / 2.5);

            travelTime = Math.max(slider.lazyTravelTime / clockRate, minDeltaTime);
        }

        // We don't need to calculate either angle or distance when one of the last->curr objects
        // is a spinner or there is no object before the current object.
        if (object instanceof Spinner || lastObject instanceof Spinner) {
            return;
        }

        float scalingFactor = getScalingFactor();
        Vector2 lastCursorPosition = getEndCursorPosition(lastObject);

        Vector2 lazyJumpVector = object.getStackedPosition()
                .scale(scalingFactor)
                .subtract(lastCursorPosition.scale(scalingFactor));
        lazyJumpDistance = lazyJumpVector.getLength();
        minimumJumpTime = strainTime;
        minimumJumpDistance = lazyJumpDistance;

        if (lastObject instanceof Slider) {
            minimumJumpTime = Math.max(strainTime - ((Slider) lastObject).lazyTravelTime / clockRate, minDeltaTime);

            // There are two types of slider-to-object patterns to consider in order to better approximate the real movement a player will take to jump between the hit objects.
            //
            // 1. The anti-flow pattern, where players cut the slider short in order to move to the next hit object.
            //
            //      <======o==>  ← slider
            //             |     ← most natural jump path
            //             o     ← a follow-up hit circle
            //
            // In this case the most natural jump path is approximated by LazyJumpDistance.
            //
            // 2. The flow pattern, where players follow through the slider to its visual extent into the next hit object.
            //
            //      <======o==>---o
            //                  ↑
            //        most natural jump path
            //
            // In this case the most natural jump path is better approximated by a new distance called "tailJumpDistance" - the distance between the slider's tail and the next hit object.
            //
            // Thus, the player is assumed to jump the minimum of these two distances in all cases.
            float tailJumpDistance = ((Slider) lastObject).tail
                        .getStackedPosition()
                        .subtract(object.getStackedPosition())
                        .getLength() * scalingFactor;

            float maximumSliderRadius = normalizedRadius * 2.4f;
            minimumJumpDistance = Math.max(0, Math.min(lazyJumpDistance - (maximumSliderRadius - assumedSliderRadius), tailJumpDistance - maximumSliderRadius));
        }

        if (lastLastObject != null && !(lastLastObject instanceof Spinner)) {
            Vector2 lastLastCursorPosition = getEndCursorPosition(lastLastObject);
            Vector2 v1 = lastLastCursorPosition.subtract(lastObject.getStackedPosition());
            Vector2 v2 = object.getStackedPosition().subtract(lastCursorPosition);
            float dot = v1.dot(v2);
            float det = v1.x * v2.y - v1.y * v2.x;

            angle = Math.abs(Math.atan2(det, dot));
        }
    }

    private void computeSliderCursorPosition(Slider slider) {
        if (slider.lazyEndPosition != null) {
            return;
        }

        slider.lazyTravelTime = slider.nestedHitObjects.get(slider.nestedHitObjects.size() - 1).startTime - slider.startTime;

        double endTimeMin = slider.lazyTravelTime / slider.spanDuration;
        if (endTimeMin % 2 >= 1) {
            endTimeMin = 1 - endTimeMin % 1;
        } else {
            endTimeMin %= 1;
        }

        // Temporary lazy end position until a real result can be derived.
        slider.lazyEndPosition = slider.getStackedPosition().add(slider.path.positionAt(endTimeMin));

        Vector2 currentCursorPosition = slider.getStackedPosition();
        double scalingFactor = normalizedRadius / slider.getRadius();

        for (int i = 1; i < slider.nestedHitObjects.size(); ++i) {
            SliderHitObject currentMovementObject = slider.nestedHitObjects.get(i);

            Vector2 currentMovement = currentMovementObject
                    .getStackedPosition()
                    .subtract(currentCursorPosition);
            double currentMovementLength = scalingFactor * currentMovement.getLength();

            // The amount of movement required so that the cursor position needs to be updated.
            double requiredMovement = assumedSliderRadius;

            if (i == slider.nestedHitObjects.size() - 1) {
                // The end of a slider has special aim rules due to the relaxed time constraint on position.
                // There is both a lazy end position and the actual end slider position. We assume the player takes the simpler movement.
                // For sliders that are circular, the lazy end position may actually be farther away than the sliders' true end.
                // This code is designed to prevent buffing situations where lazy end is actually a less efficient movement.
                Vector2 lazyMovement = slider.lazyEndPosition.subtract(currentCursorPosition);

                if (lazyMovement.getLength() < currentMovement.getLength()) {
                    currentMovement = lazyMovement;
                }

                currentMovementLength = scalingFactor * currentMovement.getLength();
            } else if (currentMovementObject instanceof SliderRepeat) {
                // For a slider repeat, assume a tighter movement threshold to better assess repeat sliders.
                requiredMovement = normalizedRadius;
            }

            if (currentMovementLength > requiredMovement) {
                // This finds the positional delta from the required radius and the current position,
                // and updates the currentCursorPosition accordingly, as well as rewarding distance.
                currentCursorPosition = currentCursorPosition.add(currentMovement.scale((float) ((currentMovementLength - requiredMovement) / currentMovementLength)));
                currentMovementLength *= (currentMovementLength - requiredMovement) / currentMovementLength;
                slider.lazyTravelDistance += (float) currentMovementLength;
            }

            if (i == slider.nestedHitObjects.size() - 1) {
                slider.lazyEndPosition = currentCursorPosition;
            }
        }
    }

    private float getScalingFactor() {
        // We will scale distances by this factor, so we can assume a uniform CircleSize among beatmaps.
        float radius = (float) object.getRadius();
        float scalingFactor = normalizedRadius / radius;

        // High circle size (small CS) bonus
        if (radius < 30) {
            scalingFactor *= 1 + Math.min(30 - radius, 5) / 50;
        }

        return scalingFactor;
    }

    private Vector2 getEndCursorPosition(HitObject object) {
        Vector2 pos = object.getStackedPosition();

        if (object instanceof Slider) {
            Slider slider = (Slider) object;
            computeSliderCursorPosition(slider);
            pos = slider.lazyEndPosition != null ? slider.lazyEndPosition : pos;
        }

        return pos;
    }
}
