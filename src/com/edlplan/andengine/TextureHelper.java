package com.edlplan.andengine;

import android.graphics.Bitmap;

import com.reco1l.osu.graphics.StreamTextureSource;

import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import ru.nsu.ccfit.zuev.osu.GlobalManager;

public class TextureHelper {

    private static int tmpFileId = 0;

    public static StreamTextureSource createSourceFromBitmap(Bitmap bitmap) {
        tmpFileId++;
        try {
            File tmp = File.createTempFile("bmp_cache" + tmpFileId, ".png");
            tmp.deleteOnExit();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(tmp));
            return new StreamTextureSource(tmp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static StreamTextureSource createMemorySourceFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return new StreamTextureSource(() -> new ByteArrayInputStream(bytes));
    }

    public static TextureRegion createRegion(Bitmap bitmap) {
        var source = createSourceFromBitmap(bitmap);
        if (source == null || source.getWidth() == 0 || source.getHeight() == 0) {
            return null;
        }

        var region = TextureRegionFactory.createFromSource(source.createAtlas(TextureOptions.BILINEAR), source, 0, 0, false);
        GlobalManager.Engine.getTextureManager().loadTexture(region.getTexture());
        return region;
    }

    public static TextureRegion create1xRegion(int color) {
        Bitmap bmp = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888);
        bmp.eraseColor(color);
        var source = createMemorySourceFromBitmap(bmp);
        if (source.getWidth() == 0 || source.getHeight() == 0) {
            return null;
        }

        var region = TextureRegionFactory.createFromSource(source.createAtlas(TextureOptions.BILINEAR), source, 0, 0, false);
        GlobalManager.Engine.getTextureManager().loadTexture(region.getTexture());
        return region;
    }

}
