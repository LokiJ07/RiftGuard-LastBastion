package com.lokij07.riftguard;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

import static org.junit.Assert.*;

/** Public-release visual fidelity checks, not just a compilation smoke test. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class FortressStaticFrameCacheTest {
    @Test public void identicalFortressReusesFrameWithoutRerendering(){
        FortressStaticFrameCache cache=new FortressStaticFrameCache();
        Bitmap screenshot=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(screenshot);
        assertTrue(cache.draw(c,640,365,5,4,3,2,.20f,1f));
        int first=cache.frameBuilds();
        assertEquals(1,first);
        assertEquals(1,cache.cachedFrames());
        assertTrue(cache.draw(c,640,365,5,4,3,2,.65f,.5f));
        assertTrue(cache.draw(c,640,365,5,4,3,2,1.32f,.3f));
        assertEquals("Animating the world must not rebuild the stone castle",first,
                cache.frameBuilds());
        assertNotEquals("Actual castle geometry must be painted",0,
                screenshot.getPixel(640,365));
        screenshot.recycle();cache.clear();
    }
    @Test public void allPurchasedRankAndArsenalVisualsAreUniqueAndBounded(){
        FortressStaticFrameCache cache=new FortressStaticFrameCache();
        Bitmap canvasBitmap=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(canvasBitmap);
        for(int rank=1;rank<=5;rank++){
            for(int arsenal=0;arsenal<=4;arsenal++){
                assertTrue("Fortress must render all earned ranks/arsenals",
                        cache.draw(canvas,640,375,rank,arsenal,4,3,
                                .5f,1f));
                assertTrue(cache.cachedFrames()<=
                        FortressStaticFrameCache.MAX_FRAMES);
            }
        }
        assertEquals(25,cache.frameBuilds());
        assertFalse(cache.isDisabled());
        canvasBitmap.recycle();
    }
    @Test public void allUpgradeBranchesInvalidateCacheKey(){
        FortressStaticFrameCache cache=new FortressStaticFrameCache();
        Bitmap shot=Bitmap.createBitmap(1024,720,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(shot);
        assertTrue(cache.draw(c,500,400,3,2,0,0,0,1f));
        assertTrue(cache.draw(c,500,400,3,2,1,0,0,1f));
        assertTrue(cache.draw(c,500,400,3,2,1,1,0,1f));
        assertEquals(3,cache.frameBuilds());
        assertEquals(2,cache.cachedFrames());
        cache.clear();
        assertEquals(0,cache.cachedFrames());
        shot.recycle();
    }
    @Test public void genericQualityModesScaleAcrossRamClasses(){
        assertEquals(GraphicsProfileV065.PERFORMANCE,
                GraphicsProfileV065.recommended(64,false));
        assertEquals(GraphicsProfileV065.PERFORMANCE,
                GraphicsProfileV065.recommended(512,true));
        assertEquals(GraphicsProfileV065.BALANCED,
                GraphicsProfileV065.recommended(128,false));
        assertEquals(GraphicsProfileV065.HIGH,
                GraphicsProfileV065.recommended(256,false));
        assertEquals(GraphicsProfileV065.HIGH,
                GraphicsProfileV065.recommended(512,false));
    }
}
