package com.reco1l.osu.graphics

import android.graphics.Bitmap
import android.graphics.Bitmap.*
import android.graphics.BitmapFactory
import android.util.Log
import org.anddev.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource
import org.anddev.andengine.opengl.texture.source.BaseTextureAtlasSource
import org.anddev.andengine.util.Debug
import org.anddev.andengine.util.StreamUtils
import java.io.IOException
import java.io.InputStream
import kotlin.jvm.Throws


fun interface InputStreamProvider {

    @Throws(IOException::class)
    fun get(): InputStream

}


class InputStreamTextureAtlasSource @JvmOverloads constructor(

    private val inputStream: InputStreamProvider,
    positionX: Int = 0,
    positionY: Int = 0

) : BaseTextureAtlasSource(positionX, positionY), IBitmapTextureAtlasSource {


    private var _width = 0

    private var _height = 0


    init {

        val decodeOptions = BitmapFactory.Options()
        decodeOptions.inJustDecodeBounds = true

        try {
            inputStream.get().use {
                BitmapFactory.decodeStream(it, null, decodeOptions)

                _width = decodeOptions.outWidth
                _height = decodeOptions.outHeight
            }
        } catch (e: IOException) {
            Log.e("TextureAtlasSource", "Failed to load Bitmap", e)
        }

    }


    override fun getWidth() = _width

    override fun getHeight() = _height


    override fun onLoadBitmap(config: Config): Bitmap? {

        val decodeOptions = BitmapFactory.Options()
        decodeOptions.inPreferredConfig = config

        try {
            inputStream.get().use {
                return BitmapFactory.decodeStream(it, null, decodeOptions)
            }
        } catch (e: IOException) {
            Log.e("TextureAtlasSource", "Failed to load Bitmap", e)
        }

        return null
    }


    override fun deepCopy() = InputStreamTextureAtlasSource(inputStream, mTexturePositionX, mTexturePositionY)

}
