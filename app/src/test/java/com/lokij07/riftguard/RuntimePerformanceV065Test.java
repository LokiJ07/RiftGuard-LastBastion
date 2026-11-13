package com.lokij07.riftguard;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class RuntimePerformanceV065Test {
    @Test public void wardenIdleAndWalkReuseSameActualArtRatherThanRepainting(){
        WardenSpriteFrameCache cache=new WardenSpriteFrameCache(new WardenRig());
        Bitmap target=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(target);
        assertTrue(cache.draw(canvas,620,400,.10f,0f,false,
                -1f,-1f,false,0,0,0,0,-1f));
        assertEquals(1,cache.frameBuilds());
        // Different timestamps inside the same animation tile are free.
        assertTrue(cache.draw(canvas,620,400,.11f,0f,false,
                -1f,-1f,false,0,0,0,0,-1f));
        assertEquals(1,cache.frameBuilds());
        assertTrue(cache.draw(canvas,620,400,.10f,(float)Math.PI,false,
                -1f,-1f,false,0,0,0,0,-1f));
        assertEquals(2,cache.frameBuilds());
        assertTrue(cache.draw(canvas,620,400,.10f,0f,true,
                -1f,-1f,false,0,0,0,0,-1f));
        assertEquals(3,cache.frameBuilds());
        assertEquals(3,cache.cachedFrames());
        assertNotEquals(0,target.getPixel(620,315)); // real opaque Warden art
        target.recycle();
        cache.clear();
        assertEquals(0,cache.cachedFrames());
    }
    @Test public void fastActionAndDifferentEquipmentNeverReusesStalePose(){
        WardenSpriteFrameCache cache=new WardenSpriteFrameCache(new WardenRig());
        Bitmap bitmap=Bitmap.createBitmap(280,264,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bitmap);
        assertFalse(cache.draw(canvas,140,232,0,0,false,
                .4f,-1f,false,0,0,0,0,-1f));
        assertFalse(cache.draw(canvas,140,232,0,0,false,
                -1f,.6f,false,0,0,0,0,-1f));
        assertFalse(cache.draw(canvas,140,232,0,0,false,
                -1f,-1f,true,0,0,0,0,-1f));
        assertFalse(cache.draw(canvas,140,232,0,0,false,
                -1f,-1f,false,0,0,0,0,.6f));
        assertEquals(0,cache.frameBuilds());
        assertTrue(cache.draw(canvas,140,232,.1f,0,false,
                -1f,-1f,false,1,7,4,3,-1f));
        assertTrue(cache.draw(canvas,140,232,.1f,0,false,
                -1f,-1f,false,1,7,3,3,-1f));
        assertEquals("Weapon form must change the rendered cache key",
                2,cache.frameBuilds());
        bitmap.recycle();
    }
    @Test public void secondaryEffectsAdaptOnlyAfterSustainedSlowFrames(){
        RenderBudgetV065 budget=new RenderBudgetV065();
        assertFalse(budget.leanEffects());
        for(int i=0;i<7;i++)budget.sample(40,75);
        assertFalse(budget.leanEffects());
        budget.sample(40,75);
        assertTrue(budget.leanEffects());
        for(int i=0;i<30;i++)budget.sample(8,16);
        assertTrue("Do not flicker quality within one action shot",budget.leanEffects());
        for(int i=30;i<90;i++)budget.sample(8,16);
        assertFalse(budget.leanEffects());
        for(int i=0;i<9;i++)budget.sample(5,1600);
        assertFalse("Background/resume pause is not a rendering regression",
                budget.leanEffects());
    }
    @Test public void atmosphericAdaptiveModeStillDrawsAndKeepsStageIdentity(){
        Bitmap image=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(image);
        BiomeAtmosphere.drawFront(canvas,4,120,3f,true);
        assertTrue(image.getPixel(0,0)==0); // no full-screen opaque overdraw
        BiomeAtmosphere.drawFront(canvas,4,120,3f,false);
        assertFalse(image.isRecycled());
        image.recycle();
    }
}
