package com.lokij07.riftguard;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.content.res.Resources;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

import static org.junit.Assert.*;

/** Regression tests for real in-APK textured world-prop layers. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class BiomePropsTest {
    @Test public void allSixtyPropFramesArePackedAndRenderable() {
        Resources res=RuntimeEnvironment.getApplication().getResources();
        BiomeProps props=new BiomeProps(res);
        assertTrue("Texture atlas must be bundled in Android APK",props.ready());
        Bitmap output=Bitmap.createBitmap(1024,512,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(output);
        int drawn=0;
        for(int biome=0;biome<10;biome++)for(int kind=0;kind<6;kind++) {
            if(props.draw(canvas,biome,kind,86+(biome%5)*188,165+(biome/5)*220,
                    .7f,biome*.37f+kind,.95f==1f?255:245))drawn++;
        }
        assertEquals(60,drawn);
        int visible=0;
        for(int y=0;y<output.getHeight();y+=8)for(int x=0;x<output.getWidth();x+=8)
            if(android.graphics.Color.alpha(output.getPixel(x,y))>0)visible++;
        assertTrue("Rendered props must have visible pixels",visible>200);
        output.recycle();
    }
    @Test public void occludingVegetationFadesNearHeroAndDepthUsesFeet() {
        assertTrue(BiomeProps.overlapsHero(500,530,1,500,500));
        assertFalse(BiomeProps.overlapsHero(750,530,1,500,500));
        assertTrue(BiomeProps.groundDepth(700)>BiomeProps.groundDepth(200));
    }
}
