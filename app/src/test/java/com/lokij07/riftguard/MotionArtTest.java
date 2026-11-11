package com.lokij07.riftguard;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class MotionArtTest {
    private MotionArt art() {
        Resources r=RuntimeEnvironment.getApplication().getResources();
        return new MotionArt(r);
    }
    private Bitmap pose(MotionArt art,int state,int frame) {
        Bitmap b=Bitmap.createBitmap(230,230,Bitmap.Config.ARGB_8888);
        art.drawHeroFrame(new Canvas(b),state,frame,115,197,false,false);
        return b;
    }
    private int pixelDifferences(Bitmap a,Bitmap b) {
        int[] left=new int[a.getWidth()*a.getHeight()];
        int[] right=new int[left.length];
        a.getPixels(left,0,a.getWidth(),0,0,a.getWidth(),a.getHeight());
        b.getPixels(right,0,b.getWidth(),0,0,b.getWidth(),b.getHeight());
        int count=0;
        for(int i=0;i<left.length;i++)if(left[i]!=right[i])count++;
        return count;
    }
    @Test public void bundledAssetsDecodeAtExactProductionSheetDimensions() {
        MotionArt art=art();
        assertTrue("Animation and terrain resources must be bundled",art.ready());
        assertTrue("A separately layered painted castle must load",art.fortressReady());
    }
    @Test public void allRunAndAttackFramesShowRealPoseChanges() {
        MotionArt a=art();
        assertTrue(a.heroReady());
        Bitmap firstRun=pose(a,1,0);
        for(int i=1;i<4;i++){
            Bitmap other=pose(a,1,i);
            assertTrue("Running frame "+i+" must change actual rendered pixels",
                    pixelDifferences(firstRun,other)>70);
            other.recycle();
        }
        Bitmap idle=pose(a,0,0);
        Bitmap slash=pose(a,2,2);
        assertTrue("Attacking must change the hero silhouette",
                pixelDifferences(idle,slash)>70);
        firstRun.recycle();idle.recycle();slash.recycle();
    }
    @Test public void attacksRunOnTheirOwnTimeline() {
        assertEquals(0,MotionArt.eventFrame(0));
        assertEquals(1,MotionArt.eventFrame(.33f));
        assertEquals(2,MotionArt.eventFrame(.55f));
        assertEquals(3,MotionArt.eventFrame(.99f));
        assertEquals(3,MotionArt.frame(0.3f,10f,4));
    }
    @Test public void fortressArtRendersOnlyAtStoneKeepOrHigher() {
        MotionArt a=art();
        Bitmap b=Bitmap.createBitmap(480,520,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(b);
        assertFalse(a.fortress(c,240,400,1,1.0f));
        assertTrue(a.fortress(c,240,400,3,1.0f));
        assertNotEquals(0,b.getPixel(240,300));
        b.recycle();
    }

    @Test public void originalTerrainDrawsIntoActualAndroidCanvas() {
        MotionArt art=art();
        art.prepareTerrain(0);
        Bitmap a=Bitmap.createBitmap(330,240,Bitmap.Config.RGB_565);
        assertTrue(art.terrain(new Canvas(a)));
        art.prepareTerrain(180);
        Bitmap b=Bitmap.createBitmap(330,240,Bitmap.Config.RGB_565);
        assertTrue(art.terrain(new Canvas(b)));
        assertNotEquals("Biome textures should differ",a.getPixel(100,100),b.getPixel(100,100));
        a.recycle();b.recycle();
    }
}
