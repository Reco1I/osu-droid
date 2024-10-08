package com.rian.osu.difficulty.evaluators

import com.rian.osu.beatmap.hitobject.Slider
import com.rian.osu.beatmap.hitobject.Spinner
import com.rian.osu.difficulty.StandardDifficultyHitObject
import kotlin.math.*

/**
 * An evaluator for calculating osu!standard rhythm difficulty.
 */
object StandardRhythmEvaluator {
    private const val RHYTHM_MULTIPLIER = 0.75
    private const val HISTORY_TIME_MAX = 5000 // 5 seconds of calculateRhythmBonus max.

    /**
     * Calculates a rhythm multiplier for the difficulty of the tap associated
     * with historic data of the current object.
     *
     * @param current The current object.
     */
    fun evaluateDifficultyOf(current: StandardDifficultyHitObject): Double {
        if (current.obj is Spinner) {
            return 0.0
        }

        var previousIslandSize = 0
        var rhythmComplexitySum = 0.0
        var islandSize = 1

        // Store the ratio of the current start of an island to buff for tighter rhythms.
        var startRatio = 0.0
        var firstDeltaSwitch = false
        var rhythmStart = 0
        val historicalNoteCount = min(current.index, 32)

        while (rhythmStart < historicalNoteCount - 2 &&
            current.startTime - current.previous(rhythmStart)!!.startTime < HISTORY_TIME_MAX
        ) {
            ++rhythmStart
        }

        for (i in rhythmStart downTo 1) {
            val currentObject = current.previous(i - 1)!!
            val prevObject = current.previous(i)!!
            val lastObject = current.previous(i + 1)!!

            // Scale note 0 to 1 from history to now.
            var currentHistoricalDecay =
                (HISTORY_TIME_MAX - (current.startTime - currentObject.startTime)) / HISTORY_TIME_MAX

            // Either we're limited by time or limited by object count.
            currentHistoricalDecay =
                min(currentHistoricalDecay, (historicalNoteCount - i).toDouble() / historicalNoteCount)

            val currentDelta = currentObject.strainTime
            val prevDelta = prevObject.strainTime
            val lastDelta = lastObject.strainTime

            val currentRatio = 1 + 6 * min(
                0.5,
                sin(Math.PI / (min(prevDelta, currentDelta) / max(prevDelta, currentDelta))).pow(2.0)
            )

            val windowPenalty =
                ((abs(prevDelta - currentDelta) - current.fullGreatWindow * 0.3) / (current.fullGreatWindow * 0.3)).coerceIn(0.0, 1.0)

            var effectiveRatio = windowPenalty * currentRatio

            if (firstDeltaSwitch) {
                if (prevDelta <= 1.25 * currentDelta && prevDelta * 1.25 >= currentDelta) {
                    // Island is still progressing, count size.
                    if (islandSize < 7) {
                        ++islandSize
                    }
                } else {
                    if (currentObject.obj is Slider) {
                        // BPM change is into slider, this is easy acc window.
                        effectiveRatio /= 8.0
                    }

                    if (prevObject.obj is Slider) {
                        // BPM change was from a slider, this is typically easier than circle -> circle.
                        effectiveRatio /= 4.0
                    }

                    if (previousIslandSize == islandSize) {
                        // Repeated island size (ex: triplet -> triplet).
                        effectiveRatio /= 4.0
                    }

                    if (previousIslandSize % 2 == islandSize % 2) {
                        // Repeated island polarity (2 -> 4, 3 -> 5).
                        effectiveRatio /= 2.0
                    }

                    if (lastDelta > prevDelta + 10 && prevDelta > currentDelta + 10) {
                        // Previous increase happened a note ago.
                        // Albeit this is a 1/1 -> 1/2-1/4 type of transition, we don't want to buff this.
                        effectiveRatio /= 8.0
                    }

                    rhythmComplexitySum +=
                        sqrt(effectiveRatio * startRatio) * currentHistoricalDecay *
                        sqrt(4.0 + islandSize) / 2 *
                        sqrt(4.0 + previousIslandSize) / 2

                    startRatio = effectiveRatio
                    previousIslandSize = islandSize

                    if (prevDelta * 1.25 < currentDelta) {
                        // We're slowing down, stop counting.
                        // If we're speeding up, this stays as is, and we keep counting island size.
                        firstDeltaSwitch = false
                    }

                    islandSize = 1
                }
            } else if (prevDelta > 1.25 * currentDelta) {
                // We want to be speeding up.
                // Begin counting island until we change speed again.
                firstDeltaSwitch = true
                startRatio = effectiveRatio
                islandSize = 1
            }
        }

        return sqrt(4 + rhythmComplexitySum * RHYTHM_MULTIPLIER) / 2
    }
}
