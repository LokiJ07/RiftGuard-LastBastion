package com.lokij07.riftguard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
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
public final class LivingBattlefieldTest {
    private static Bitmap scene(LivingBattlefield renderer,int stage,
                                float heroX,float time,boolean reduced){
        Bitmap bitmap=Bitmap.createBitmap(950,275,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bitmap);canvas.drawColor(0xff35504a);
        canvas.scale(.5f,.5f);
        renderer.draw(canvas,stage,time,heroX,LaneRules.startY(),reduced);
        return bitmap;
    }
    private static int differences(Bitmap a,Bitmap b){
        int changes=0;
        for(int y=20;y<235;y+=4)for(int x=180;x<840;x+=4){
            if(a.getPixel(x,y)!=b.getPixel(x,y))changes++;
        }
        return changes;
    }
    @Test public void all300MissionsHaveStableDistinctEnvironmentSignatures(){
        assertEquals(300,Campaign.STAGES);
        assertEquals(10,LivingBattlefield.REGIONS);
        assertEquals(8,LivingBattlefield.DEPTH_LAYERS);
        Set<Integer> signatures=new HashSet<>();
        for(int i=0;i<Campaign.STAGES;i++){
            assertTrue("Every stage needs distinct visual seed",
                    signatures.add(LivingBattlefield.environmentSignature(i)));
            assertEquals(Campaign.world(i),LivingBattlefield.biome(i));
            assertEquals(Campaign.map(i),LivingBattlefield.mapType(i));
            assertEquals((Campaign.level(i)-1)%10,
                    LivingBattlefield.chapterBeat(i));
            assertNotNull(LivingBattlefield.stageMood(i));
        }
        assertEquals(300,signatures.size());
        assertThrows(IllegalArgumentException.class,
                ()->LivingBattlefield.environmentSignature(300));
    }
    @Test public void tenBiomesHaveTrulyDifferentPaintedPixelsAndFreshCaches(){
        LivingBattlefield renderer=new LivingBattlefield();
        Bitmap green=scene(renderer,0,LaneRules.startX(),1f,false);
        assertEquals(5,renderer.activeBitmapCount());
        assertEquals(1,renderer.cacheGenerations());
        renderer.prepare(0);
        scene(renderer,0,LaneRules.startX(),2f,false).recycle();
        assertEquals("No recreation every frame",1,renderer.cacheGenerations());
        Set<Integer> signatures=new HashSet<>();
        for(int world=0;world<10;world++){
            int stage=world*30;
            Bitmap drawn=scene(renderer,stage,LaneRules.startX(),1.3f,false);
            assertTrue("Every biome must produce different actual pixels from forest "+world,
                    world==0||differences(green,drawn)>75);
            int s=0;
            for(int y=40;y<180;y+=11)for(int x=270;x<850;x+=17)
                s=31*s+drawn.getPixel(x,y);
            assertTrue("Region art should differ "+world,signatures.add(s));
            assertEquals(5,renderer.activeBitmapCount());
            assertEquals(stage,renderer.currentStage());
            drawn.recycle();
        }
        green.recycle();
        renderer.release();
        assertEquals(0,renderer.activeBitmapCount());
    }
    @Test public void scoutBridgeAndSiegeMapsShowDifferentCastleAndLandscape(){
        LivingBattlefield renderer=new LivingBattlefield();
        Bitmap scout=scene(renderer,0,1234,1f,true);
        Bitmap bridge=scene(renderer,1,1234,1f,true);
        Bitmap siege=scene(renderer,2,1234,1f,true);
        assertTrue("Scout vs river crossing visual must differ",differences(scout,bridge)>40);
        assertTrue("Bridge vs last stand must differ",differences(bridge,siege)>40);
        scout.recycle();bridge.recycle();siege.recycle();renderer.release();
    }
    @Test public void realParallaxRespondsToHeroMovementAndReducedMotion(){
        LivingBattlefield renderer=new LivingBattlefield();
        Bitmap center=scene(renderer,0,1234,1f,false);
        Bitmap right=scene(renderer,0,1510,1f,false);
        assertTrue("Ridges, cliff and pines must parallax on movement",
                differences(center,right)>25);
        Bitmap a=scene(renderer,0,1234,1f,true);
        Bitmap b=scene(renderer,0,1510,1f,true);
        assertEquals("Reduced motion disables camera-relative parallax",
                0,differences(a,b));
        center.recycle();right.recycle();a.recycle();b.recycle();
        renderer.release();
    }
    @Test public void transparentBattleLaneAndViewRenderKeepEnemiesAndHUDReadable()
            throws Exception {
        LivingBattlefield renderer=new LivingBattlefield();
        Bitmap picture=scene(renderer,120,1234,1f,true);
        assertEquals("World renderer must not paint over the fight at y=540",
                0xff35504a,picture.getPixel(680,269));
        picture.recycle();renderer.release();
        Context ctx=RuntimeEnvironment.getApplication();
        RiftGameView game=new RiftGameView(ctx);
        game.layout(0,0,1280,720);
        Method begin=RiftGameView.class.getDeclaredMethod("start",int.class);
        begin.setAccessible(true);begin.invoke(game,299);
        Bitmap battle=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        game.draw(new Canvas(battle));
        assertNotEquals(Color.TRANSPARENT,battle.getPixel(640,360));
        assertNotEquals(Color.TRANSPARENT,battle.getPixel(1160,570));
        battle.recycle();
    }
}
