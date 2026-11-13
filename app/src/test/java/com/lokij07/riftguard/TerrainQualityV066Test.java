package com.lokij07.riftguard;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import static org.junit.Assert.*;

/** Actual original 2D scenery remains present in all graphics quality modes. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class TerrainQualityV066Test {
    @Test public void threePresetsHaveRealMemoryAndResolutionDifferences(){
        int high=GraphicsProfileV065.HIGH;
        int mid=GraphicsProfileV065.BALANCED;
        int low=GraphicsProfileV065.PERFORMANCE;
        assertEquals(1900,TerrainQualityV066.width(high));
        assertEquals(1050,TerrainQualityV066.height(high));
        assertTrue(TerrainQualityV066.width(high)>TerrainQualityV066.width(mid));
        assertTrue(TerrainQualityV066.width(mid)>TerrainQualityV066.width(low));
        assertTrue(TerrainQualityV066.cacheBytes(high)>
                TerrainQualityV066.cacheBytes(mid));
        assertTrue(TerrainQualityV066.cacheBytes(mid)>
                TerrainQualityV066.cacheBytes(low));
        assertTrue("Performance terrain should reduce GPU texture memory > 50%",
                TerrainQualityV066.cacheBytes(low)*2 <
                        TerrainQualityV066.cacheBytes(high));
    }

    @Test public void sameStageCachesOnceButChangingGraphicsRebuildsStaticWorld(){
        NativeDeployedMapArt world=new NativeDeployedMapArt();
        int stage=152; // midgame realm; not a small tutorial-only bitmap
        Bitmap screen=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(screen);
        for(int quality=GraphicsProfileV065.HIGH;
                quality<=GraphicsProfileV065.PERFORMANCE;quality++){
            int oldBuilds=world.cacheBuilds();
            world.draw(c,stage,2f,false,quality,false);
            assertEquals(oldBuilds+1,world.cacheBuilds());
            assertEquals(TerrainQualityV066.width(quality),world.cacheWidth());
            assertEquals(TerrainQualityV066.height(quality),world.cacheHeight());
            assertEquals(quality,world.graphicsQuality());
            assertTrue("Stage terrain must remain visible at every quality",
                    Color.alpha(screen.getPixel(640,360))>0);
            world.draw(c,stage,3f,false,quality,true);
            assertEquals("Render loop must not re-rasterize same stage",
                    oldBuilds+1,world.cacheBuilds());
        }
        screen.recycle();
        world.release();
        assertEquals(0,world.cacheWidth());
        assertEquals(-1,world.preparedStage());
    }

    @Test public void allRealmsKeepBackgroundArtAndTerrainIdentity(){
        NativeDeployedMapArt world=new NativeDeployedMapArt();
        Bitmap bitmap=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        Canvas target=new Canvas(bitmap);
        world.setGraphicsQuality(GraphicsProfileV065.PERFORMANCE);
        for(int realm=0;realm<10;realm++){
            int stage=Campaign.stage(realm*10+1,2);
            bitmap.eraseColor(Color.TRANSPARENT);
            world.draw(target,stage,0f,true,
                    GraphicsProfileV065.PERFORMANCE,true);
            assertEquals(stage,world.preparedStage());
            assertEquals(TerrainQualityV066.width(GraphicsProfileV065.PERFORMANCE),
                    world.cacheWidth());
            assertTrue("All ten kingdom grounds still paint at low quality",
                    Color.alpha(bitmap.getPixel(640,360))==255);
        }
        world.release();bitmap.recycle();
    }

    @Test public void adaptiveWeatherRetainsDifferentRealmStyles(){
        assertEquals(23,TerrainQualityV066.ambientMotes(
                GraphicsProfileV065.HIGH,false));
        assertEquals(16,TerrainQualityV066.ambientMotes(
                GraphicsProfileV065.BALANCED,false));
        assertEquals(9,TerrainQualityV066.ambientMotes(
                GraphicsProfileV065.PERFORMANCE,false));
        for(int original:new int[]{44,64}){
            int high=TerrainQualityV066.weatherParticles(
                    GraphicsProfileV065.HIGH,false,original);
            int middle=TerrainQualityV066.weatherParticles(
                    GraphicsProfileV065.BALANCED,false,original);
            int low=TerrainQualityV066.weatherParticles(
                    GraphicsProfileV065.PERFORMANCE,false,original);
            assertEquals(original,high);
            assertTrue(high>middle&&middle>low);
            assertTrue(low>0);
            assertTrue(TerrainQualityV066.weatherParticles(
                    GraphicsProfileV065.PERFORMANCE,true,original)<low);
        }
    }
}
