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

/** Rendering-only optimization and screen-shape regression checks. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class BattleViewportV066Test {
    @Test public void visibleBoundsMatchRealBattleCameraAndZoom(){
        BattleViewportV066 view=new BattleViewportV066(950,525,1280,720,1f);
        assertEquals(310f,view.left(),.01f);
        assertEquals(1590f,view.right(),.01f);
        assertEquals(165f,view.top(),.01f);
        assertEquals(885f,view.bottom(),.01f);
        assertTrue(view.visible(310f,525f,0));
        assertTrue(view.visible(1590f,525f,0));
        assertTrue(view.visible(1640f,525f,50));
        assertFalse(view.visible(1641f,525f,50));
        assertFalse(view.visible(250f,525f,40));
        assertFalse(view.visible(Float.NaN,525f,100));
        assertFalse(view.visible(950f,Float.POSITIVE_INFINITY,100));
        assertFalse(view.visible(950f,525f,Float.NaN));
    }
    @Test public void victoryCameraZoomTracksWithoutCullingItsHero(){
        BattleViewportV066 far=new BattleViewportV066(1200,525,1280,720,.64f);
        assertTrue(far.visible(1775,475,110f));
        BattleViewportV066 close=new BattleViewportV066(1200,525,1280,720,1.55f);
        assertFalse(close.visible(1775,475,110f));
        assertTrue(close.visible(1200,525,0));
        assertTrue(close.rectVisible(1100,400,1400,650));
        assertFalse(close.rectVisible(0,0,400,200));
    }
    @Test public void projectilePathReuseDoesNotLeakPreviousShape(){
        Bitmap bmp=Bitmap.createBitmap(600,300,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bmp);
        CombatVfx.projectile(canvas,110,140,190,140,0,-1,1f);  // arrow
        CombatVfx.projectile(canvas,300,140,400,140,3,3,1.3f); // lightning
        CombatVfx.projectile(canvas,500,140,550,140,5,-1,1.6f); // ice
        int seen=0;
        for(int x=70;x<580;x++)for(int y=85;y<190;y++)
            if(Color.alpha(bmp.getPixel(x,y))>0)seen++;
        assertTrue("Three independently drawn real weapon silhouettes",seen>200);
        assertEquals(0,Color.alpha(bmp.getPixel(15,15)));
        CombatVfx.hit(canvas,300,140,.4f,0xff80ffcd,42);
        assertTrue(Color.alpha(bmp.getPixel(300,140))>0);
        bmp.recycle();
    }
    @Test public void crystalEnvironmentAndHeroGroundLightingSurviveReuse(){
        Bitmap bmp=Bitmap.createBitmap(1900,1050,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(bmp);
        BiomeAtmosphere.drawFront(c,6,190,12f,false);
        BiomeAtmosphere.drawFront(c,6,191,13f,true);
        BattlefieldLighting.drawHeroContact(c,850,500,true,2.4f,6);
        assertNotEquals(0,Color.alpha(bmp.getPixel(850,500)));
        bmp.recycle();
    }
    @Test public void idleUiUsesLowerCadenceThanLiveCombat(){
        assertEquals(0L,GraphicsProfileV065.nextFrameDelayMs(
                GraphicsProfileV065.HIGH,60f));
        assertTrue(GraphicsProfileV065.idleFrameDelayMs(60f)>0L);
        assertTrue(GraphicsProfileV065.idleFrameDelayMs(120f)>
                GraphicsProfileV065.nextFrameDelayMs(
                        GraphicsProfileV065.HIGH,120f));
        assertEquals(60,GraphicsProfileV065.targetFps(GraphicsProfileV065.HIGH));
    }
}
