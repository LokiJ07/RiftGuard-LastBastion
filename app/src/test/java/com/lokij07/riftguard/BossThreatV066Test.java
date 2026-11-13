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

/** Boss attack cues must reflect real combat radii without touching AI. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class BossThreatV066Test {
    @Test public void previewUsesAuthoritativeBossThreatRanges(){
        assertEquals(265f,BossThreatV066.HERO_THREAT_RADIUS,0f);
        assertEquals(445f,BossThreatV066.CORE_ATTACK_RANGE,0f);
        assertTrue(BossThreatV066.threatensCore(100,100,544.9f,100));
        assertFalse(BossThreatV066.threatensCore(100,100,545.1f,100));
        for(int realm=0;realm<10;realm++){
            int stage=Campaign.stage(1+realm*10,2);
            assertTrue(BossTactics.chargeTime(stage,3)>0);
            assertTrue(BossTactics.tactic(stage)>=0);
            assertTrue(BossTactics.tactic(stage)<3);
        }
    }
    @Test public void warningProgressTracksRealRemainingCharge(){
        assertEquals(0f,BossThreatV066.progress(.8f,.8f),.0001f);
        assertEquals(.5f,BossThreatV066.progress(.4f,.8f),.0001f);
        assertEquals(1f,BossThreatV066.progress(0f,.8f),.0001f);
        assertEquals(0f,BossThreatV066.progress(Float.NaN,.8f),.0001f);
        assertEquals(0f,BossThreatV066.progress(1f,0),.0001f);
    }
    @Test public void actualWorldTargetMarkerIsVisibleAndDisabledWhenUncharged(){
        Bitmap img=Bitmap.createBitmap(1100,800,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(img);
        BossThreatV066.draw(c,600,410,880,410,.6f,1f,0,2,false);
        assertTrue("Radius interior must show subtle world-space warning",
                Color.alpha(img.getPixel(600,410))>0);
        assertTrue("Fortress siege crosshair must be rendered",
                Color.alpha(img.getPixel(880,410))>0);
        assertEquals(0,Color.alpha(img.getPixel(15,15)));
        img.eraseColor(Color.TRANSPARENT);
        BossThreatV066.draw(c,600,410,880,410,0,1f,0,2,false);
        assertEquals(0,Color.alpha(img.getPixel(600,410)));
        img.recycle();
    }
    @Test public void reducedMotionModeStillDisplaysRiskRadius(){
        Bitmap img=Bitmap.createBitmap(1100,800,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(img);
        BossThreatV066.draw(c,600,410,880,410,.3f,.8f,2,3,true);
        assertTrue(Color.alpha(img.getPixel(600,410))>0);
        img.recycle();
    }
}
