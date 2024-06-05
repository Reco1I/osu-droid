package com.reco1l.osu.graphics

import android.graphics.Bitmap
import android.graphics.Bitmap.*
import android.graphics.BitmapFactory
import android.util.Log
import com.reco1l.toolkt.isPowerOfTwo
import com.reco1l.toolkt.nextPowerOfTwo
import org.anddev.andengine.opengl.texture.TextureOptions
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas
import org.anddev.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource
import org.anddev.andengine.opengl.texture.source.BaseTextureAtlasSource
import org.anddev.andengine.util.MathUtils
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlin.jvm.Throws


fun interface InputStreamProvider {

    @Throws(IOException::class)
    fun get(): InputStream

}


class InputStreamTextureAtlasSource @JvmOverloads constructor(

    private val inputStream: InputStreamProvider,
    private val sampleSize: Int = 1

) : BaseTextureAtlasSource(0, 0), IBitmapTextureAtlasSource {


    @JvmOverloads
    constructor(file: File, sampleSize: Int = 1) : this(file::inputStream, sampleSize)


    private var internalWidth = 0

    private var internalHeight = 0


    init {

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        options.inSampleSize = sampleSize

        try {
            inputStream.get().use {
                BitmapFactory.decodeStream(it, null, options)

                internalWidth = options.outWidth
                internalHeight = options.outHeight
            }
        } catch (e: IOException) {
            Log.e("TextureAtlasSource", "Failed to load Bitmap", e)
        }

    }


    override fun getWidth() = internalWidth

    override fun getHeight() = internalHeight


    override fun onLoadBitmap(config: Config): Bitmap? {

        val options = BitmapFactory.Options()
        options.inPreferredConfig = config
        options.inSampleSize = sampleSize

        try {
            inputStream.get().use {
                return BitmapFactory.decodeStream(it, null, options)
            }
        } catch (e: IOException) {
            Log.e("TextureAtlasSource", "Failed to load Bitmap", e)
        }

        return null
    }


    override fun deepCopy() = InputStreamTextureAtlasSource(inputStream, sampleSize)


    fun createAtlas(options: TextureOptions): BitmapTextureAtlas {

        var w = internalWidth
        var h = internalHeight

        if (!w.isPowerOfTwo()) {
            w = 4
            while (w < internalWidth) {
                w *= 2
            }
        }

        if (!h.isPowerOfTwo()) {
            h = 4
            while (h < internalHeight) {
                h *= 2
            }
        }

        return BitmapTextureAtlas(w, h, options)
    }


}
