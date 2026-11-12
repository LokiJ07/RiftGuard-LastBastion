package com.lokij07.riftguard;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import static org.junit.Assert.*;

/** Native Android verification for the original HTML scenery motion port. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class HtmlSceneryMotionTest {
    private static Bitmap sample(int stage,float time,boolean reduced){
        Bitmap b=Bitmap.createBitmap(950,270,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(b);
        c.drawColor(0xff263c45);
        c.scale(.5f,.5f);
        LivingBattlefield world=new LivingBattlefield();
        world.draw(c,stage,time,LaneRules.startX(),LaneRules.startY(),reduced);
        assertEquals("Five existing bitmaps only; scenery animation allocates none",
                5,world.activeBitmapCount());
        world.release();
        return b;
    }
    private static int differences(Bitmap a,Bitmap b){
        int changed=0;
        for(int y=15;y<255;y+=3)
            for(int x=15;x<930;x+=3)
                if(a.getPixel(x,y)!=b.getPixel(x,y))changed++;
        return changed;
    }
    @Test public void liveHtmlSkyAndWaterfallAnimateInsideRealJavaGameCanvas(){
        Bitmap start=sample(0,0f,false);
        Bitmap later=sample(0,9f,false);
        assertTrue("Moving cloud and water motion must change actual output",
                differences(start,later)>60);
        assertEquals("Scenery must never cover lower gameplay lane/HUD",
                0xff263c45,start.getPixel(700,265));
        start.recycle();later.recycle();
    }
    @Test public void reducedMotionKeepsSamePaintedSceneryWithoutDrift(){
        Bitmap first=sample(0,0f,true);
        Bitmap later=sample(0,24f,true);
        assertEquals("Reduced motion keeps every source sky/cliff highlight static",
                0,differences(first,later));
        first.recycle();later.recycle();
    }
    @Test public void fourOriginalHtmlInspiredRealmsRetainDistinctScenery(){
        // HTML Verdant, Ember, Frostpeak, Rift -> RiftGuard worlds 0,3,4,9.
        int[] stages={0,90,120,270};
        Set<Integer> signatures=new HashSet<>();
        for(int stage:stages){
            Bitmap b=sample(stage,3f,false);
            int hash=13;
            for(int y=23;y<215;y+=9)
                for(int x=140;x<835;x+=13)
                    hash=hash*31+b.getPixel(x,y);
            assertTrue("Native realm should be a real distinct visual",signatures.add(hash));
            b.recycle();
        }
        assertEquals(4,signatures.size());
    }
    @Test public void nativeMotionHandlesInvalidClockAndAllStageIndices(){
        Bitmap b=Bitmap.createBitmap(950,270,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(b);c.scale(.5f,.5f);
        for(int stage=0;stage<300;stage++){
            // Check that neither animated subsystem requires an HTML browser
            // nor attempts to index a missing palette in a later mission.
            HtmlSceneryMotion.drawSky(c,stage,Float.NaN,true);
            HtmlSceneryMotion.drawWaterfall(c,stage,Float.POSITIVE_INFINITY,true);
        }
        b.recycle();
        assertThrows(IllegalArgumentException.class,
                ()->HtmlSceneryMotion.drawSky(new Canvas(),300,0,true));
    }
}
