package org.anddev.andengine.opengl.texture.atlas.bitmap.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.anddev.andengine.opengl.texture.source.BaseTextureAtlasSource;
import org.anddev.andengine.util.Debug;
import org.anddev.andengine.util.StreamUtils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;

/**
 * 
 * (c) 2010 Nicolas Gramlich 
 * (c) 2011 Zynga Inc.
 * 
 * @author Nicolas Gramlich
 * @since 16:39:22 - 10.08.2010
 */
// osu!droid modified - Added sample size support.
public class FileBitmapTextureAtlasSource extends BaseTextureAtlasSource implements IBitmapTextureAtlasSource {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private final int mWidth;
	private final int mHeight;

	private final int mSampleSize;

	private final File mFile;

	// ===========================================================
	// Constructors
	// ===========================================================

	public FileBitmapTextureAtlasSource(final File pFile) {
		this(pFile, 0, 0, 1);
	}

	public FileBitmapTextureAtlasSource(final File pFile, final int pSampleSize) {
		this(pFile, 0, 0, pSampleSize);
	}

	public FileBitmapTextureAtlasSource(final File pFile, final int pTexturePositionX, final int pTexturePositionY) {
		this(pFile, pTexturePositionX, pTexturePositionY, 1);
	}

	public FileBitmapTextureAtlasSource(final File pFile, final int pTexturePositionX, final int pTexturePositionY, final int pSampleSize) {
		super(pTexturePositionX, pTexturePositionY);
		this.mFile = pFile;
		this.mSampleSize = pSampleSize;

		final BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
		decodeOptions.inJustDecodeBounds = true;
		decodeOptions.inSampleSize = pSampleSize;

		InputStream in = null;
		try {
			in = new FileInputStream(pFile);
			BitmapFactory.decodeStream(in, null, decodeOptions);
		} catch (final IOException e) {
			Debug.e("Failed loading Bitmap in FileBitmapTextureAtlasSource. File: " + pFile, e);
		} finally {
			StreamUtils.close(in);
		}

		this.mWidth = decodeOptions.outWidth;
		this.mHeight = decodeOptions.outHeight;
	}

	FileBitmapTextureAtlasSource(final File pFile, final int pTexturePositionX, final int pTexturePositionY, final int pWidth, final int pHeight, final int pSampleSize) {
		super(pTexturePositionX, pTexturePositionY);
		this.mFile = pFile;
		this.mWidth = pWidth;
		this.mHeight = pHeight;
		this.mSampleSize = pSampleSize;
	}

	@Override
	public FileBitmapTextureAtlasSource deepCopy() {
		return new FileBitmapTextureAtlasSource(this.mFile, this.mTexturePositionX, this.mTexturePositionY, this.mWidth, this.mHeight, this.mSampleSize);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public int getWidth() {
		return this.mWidth;
	}

	@Override
	public int getHeight() {
		return this.mHeight;
	}

	@Override
	public Bitmap onLoadBitmap(final Config pBitmapConfig) {
		final BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
		decodeOptions.inPreferredConfig = pBitmapConfig;

		InputStream in = null;
		try {
			in = new FileInputStream(this.mFile);
			return BitmapFactory.decodeStream(in, null, decodeOptions);
		} catch (final IOException e) {
			Debug.e("Failed loading Bitmap in " + this.getClass().getSimpleName() + ". File: " + this.mFile, e);
			return null;
		} finally {
			StreamUtils.close(in);
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + this.mFile + ")";
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}