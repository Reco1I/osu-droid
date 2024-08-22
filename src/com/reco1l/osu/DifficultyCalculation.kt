package com.reco1l.osu

import android.util.Log
import android.widget.TextView
import com.edlplan.ui.fragment.BaseFragment
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.reco1l.osu.data.BeatmapInfo
import com.reco1l.osu.data.DatabaseManager
import com.reco1l.toolkt.kotlin.fastForEach
import com.rian.osu.beatmap.parser.BeatmapParser
import ru.nsu.ccfit.zuev.osu.GlobalManager
import ru.nsu.ccfit.zuev.osu.LibraryManager
import ru.nsu.ccfit.zuev.osu.ToastLogger
import ru.nsu.ccfit.zuev.osuplus.BuildConfig
import ru.nsu.ccfit.zuev.osuplus.R
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.max

object DifficultyCalculationManager {


    private var isRunning = false

    private var badge: LoadingBadgeFragment? = null


    @JvmStatic
    fun calculateDifficulties() {

        if (isRunning) {
            return
        }

        val beatmaps = LibraryManager.getLibrary().flatMap { set -> set.beatmaps.filter { it.needsDifficultyCalculation } }
        if (beatmaps.isEmpty()) {
            return
        }

        isRunning = true
        mainThread {
            badge = LoadingBadgeFragment().apply {
                text = "Calculating beatmap difficulties... (0%)"
                isIndeterminate = true
                show()
            }
        }

        object : Thread() {

            override fun run() {

                val threadCount = Runtime.getRuntime().availableProcessors()
                val threadPool = Executors.newFixedThreadPool(threadCount)

                var calculated = 0

                beatmaps.chunked(max(beatmaps.size / threadCount, 1)).fastForEach { chunk ->

                    threadPool.submit {

                        chunk.fastForEach { beatmapInfo ->

                            try {
                                val msStartTime = System.currentTimeMillis()

                                BeatmapParser(beatmapInfo.path).use { parser ->

                                    val data = parser.parse(true)!!
                                    val newInfo = BeatmapInfo(data, beatmapInfo.parentPath, beatmapInfo.dateImported, beatmapInfo.path, true)
                                    beatmapInfo.apply(newInfo)

                                    DatabaseManager.beatmapInfoTable.update(newInfo)
                                }

                                if (BuildConfig.DEBUG) {
                                    Log.i("DifficultyCalculation", "Calculated difficulty for ${beatmapInfo.path}, took ${System.currentTimeMillis() - msStartTime}ms.")
                                }

                                calculated++
                                mainThread {
                                    badge?.apply {
                                        isIndeterminate = false
                                        progress = calculated * 100 / beatmaps.size
                                        text = "Calculating beatmap difficulties... (${progress}%)"
                                    }
                                }

                            } catch (e: Exception) {
                                Log.e("DifficultyCalculation", "Error while calculating difficulty.", e)
                            }

                        }
                    }
                }

                threadPool.shutdown()
                try {

                    if (threadPool.awaitTermination(1, TimeUnit.HOURS)) {
                        ToastLogger.showText("Background difficulty calculation has finished successfully.", true)
                    } else {
                        ToastLogger.showText("Something went wrong during background difficulty calculation.", true)
                    }

                    mainThread {
                        badge?.dismiss()
                    }
                    GlobalManager.getInstance().songMenu?.onDifficultyCalculationEnd()

                } catch (e: InterruptedException) {
                    Log.e("DifficultyCalculation", "Failed while waiting for executor termination.", e)
                }

                isRunning = false
            }

        }.start()
    }

}


class LoadingBadgeFragment : BaseFragment() {

    override val layoutID = R.layout.loading_badge_fragment


    var progress = 0
        set(value) {
            field = value
            if (::progressView.isInitialized) {
                progressView.progress = value
            }
        }

    var isIndeterminate = true
        set(value) {
            field = value
            if (::progressView.isInitialized) {
                progressView.isIndeterminate = value
            }
        }

    var text = "Loading..."
        set(value) {
            field = value
            if (::textView.isInitialized) {
                textView.text = value
            }
        }


    private lateinit var progressView: CircularProgressIndicator

    private lateinit var textView: TextView


    override fun onLoadView() {
        progressView = findViewById(R.id.progress)!!
        textView = findViewById(R.id.text)!!

        progressView.isIndeterminate = isIndeterminate
        progressView.progress = progress
        textView.text = text
    }

}