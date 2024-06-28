package com.reco1l.osu.graphics

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES
import android.os.Build
import android.util.Log
import android.view.Surface
import org.anddev.andengine.engine.Engine
import org.anddev.andengine.engine.camera.Camera
import org.anddev.andengine.entity.sprite.Sprite
import org.anddev.andengine.opengl.texture.Texture
import org.anddev.andengine.opengl.texture.Texture.PixelFormat.UNDEFINED
import org.anddev.andengine.opengl.texture.TextureOptions
import org.anddev.andengine.opengl.texture.region.TextureRegion
import java.io.File
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL10.GL_CLAMP_TO_EDGE
import javax.microedition.khronos.opengles.GL10.GL_LINEAR
import javax.microedition.khronos.opengles.GL10.GL_NEAREST

class VideoSprite(source: String, private val engine: Engine) : Sprite(0f, 0f, VideoTexture(source).toRegion()) {


    val texture = textureRegion.texture as VideoTexture


    init {
        engine.textureManager.loadTexture(texture)
    }


    override fun doDraw(graphics: GL10, camera: Camera) {

        onInitDraw(graphics)
        graphics.glEnable(GL_TEXTURE_EXTERNAL_OES)

        textureRegion.onApply(graphics)

        onApplyVertices(graphics)
        drawVertices(graphics, camera)

        graphics.glDisable(GL_TEXTURE_EXTERNAL_OES)
    }


    fun release() {
        texture.release()
        engine.textureManager.unloadTexture(texture)
    }

    override fun finalize() {
        release()
        super.finalize()
    }
}


class VideoTexture(val source: String) : Texture(UNDEFINED, TextureOptions(GL_NEAREST, GL_LINEAR, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE, false), null) {

    private val player = MediaPlayer().apply {

        setDataSource(source)
        setVolume(0f, 0f)

        isLooping = false

        prepare()
    }


    private var surfaceTexture: SurfaceTexture? = null


    override fun getWidth() = player.videoWidth

    override fun getHeight() = player.videoHeight


    override fun writeTextureToHardware(graphics: GL10) = Unit

    override fun bindTextureOnHardware(graphics: GL10) = graphics.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mHardwareTextureID)


    override fun deleteTextureOnHardware(pGL: GL10?) {

        surfaceTexture?.release()
        surfaceTexture = null

        super.deleteTextureOnHardware(pGL)
    }


    override fun bind(graphics: GL10) {

        if (!isLoadedToHardware) {
            return
        }

        bindTextureOnHardware(graphics)
        applyTextureOptions(graphics)

        if (surfaceTexture == null) {
            surfaceTexture = SurfaceTexture(mHardwareTextureID)

            val surface = Surface(surfaceTexture)
            player.setSurface(surface)
            surface.release()
        }

        try {
            surfaceTexture?.updateTexImage()
        } catch (ignored: Exception) {

            isUpdateOnHardwareNeeded = true
        }
    }


    fun play() = player.start()

    fun pause() = player.pause()

    fun release() = player.release()


    fun seekTo(ms: Int) {

        // Unfortunately in old versions we can't seek at closest frame from the desired position.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            player.seekTo(ms.toLong(), MediaPlayer.SEEK_CLOSEST)
        } else {
            player.seekTo(ms)
        }
    }

    fun setPlaybackSpeed(speed: Float) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            player.playbackParams = player.playbackParams.setSpeed(speed)
        }
    }


    fun toRegion() = TextureRegion(this, 0, 0, width, height)


    companion object {

        /**
         * See [MediaPlayer documentation](https://developer.android.com/guide/topics/media/platform/supported-formats)
         */
        val SUPPORTED_VIDEO_FORMATS = arrayOf("3gp", "mp4", "mkv", "webm")


        @JvmStatic
        fun isSupportedVideo(file: File): Boolean = file.extension.lowercase() in SUPPORTED_VIDEO_FORMATS
    }
}