package com.edlplan.andengine;

import android.graphics.Bitmap;

import com.reco1l.osu.graphics.InputStreamTextureAtlasSource;

import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ru.nsu.ccfit.zuev.osu.Osu;

public class TextureHelper {

    private static int tmpFileId = 0;

    public static InputStreamTextureAtlasSource createSourceFromBitmap(Bitmap bitmap) {
        tmpFileId++;
        try {
            File tmp = File.createTempFile("bmp_cache" + tmpFileId, ".png");
            tmp.deleteOnExit();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(tmp));
            return new InputStreamTextureAtlasSource(() -> new FileInputStream(tmp));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static InputStreamTextureAtlasSource createMemorySourceFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return new InputStreamTextureAtlasSource(() -> new ByteArrayInputStream(bytes));
    }

    public static TextureRegion createRegion(Bitmap bitmap) {
        int tw = 4, th = 4;
        var source = createSourceFromBitmap(bitmap);
        if (source == null || source.getWidth() == 0 || source.getHeight() == 0) {
            return null;
        }

        final BitmapTextureAtlas tex = new BitmapTextureAtlas(tw, th, TextureOptions.BILINEAR);

        TextureRegion region = TextureRegionFactory.createFromSource(tex, source, 0, 0,
                false);
        
        Osu.Engine.getTextureManager().loadTexture(tex);
        return region;
    }

    public static TextureRegion create1xRegion(int color) {
        Bitmap bmp = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888);
        bmp.eraseColor(color);
        int tw = 4, th = 4;
        var source = createMemorySourceFromBitmap(bmp);
        if (source.getWidth() == 0 || source.getHeight() == 0) {
            return null;
        }
        while (tw < source.getWidth()) {
            tw *= 2;
        }
        while (th < source.getHeight()) {
            th *= 2;
        }

        final BitmapTextureAtlas tex = new BitmapTextureAtlas(tw, th, TextureOptions.BILINEAR);

        TextureRegion region = TextureRegionFactory.createFromSource(tex, source, 0, 0,
                false);
        Osu.Engine.getTextureManager().loadTexture(tex);
        return region;
    }

}
