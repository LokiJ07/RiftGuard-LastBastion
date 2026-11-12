package com.lokij07.riftguard;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import static org.junit.Assert.*;

/** Pixel-level integration tests for actual painted sprites, not CSS or a concept. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class AnimationBlendVisualTest {
    @Test public void blendingNeverWrapsFinalStrikeBackToFirstWindupFrame(){
        for(float progress:new float[]{.0f,.1f,.24f,.50f,.74f,.90f,.999f,1f}){
            AnimationBlend.State s=AnimationBlend.event(progress,4);
            assertTrue(s.current>=0&&s.current<=3);
            assertTrue(s.next>=s.current&&s.next<=3);
            assertTrue(s.nextAlpha>=0&&s.nextAlpha<=.44f);
        }
        assertEquals(3,AnimationBlend.event(1f,4).current);
        assertFalse(AnimationBlend.event(1f,4).shouldBlend());
        assertFalse(AnimationBlend.event(.25f,4).shouldBlend());
        assertTrue(AnimationBlend.event(.245f,4).shouldBlend());
        assertFalse(AnimationBlend.event(Float.NaN,4).shouldBlend());
    }
    @Test public void locomotionOnlyBlendsNearTransitionEdges(){
        AnimationBlend.State start=AnimationBlend.loop(0,10f,4);
        AnimationBlend.State middle=AnimationBlend.loop(.045f,10f,4);
        AnimationBlend.State transition=AnimationBlend.loop(.095f,10f,4);
        assertEquals(0,start.current);assertEquals(0,middle.current);
        assertFalse(middle.shouldBlend());
        assertTrue(transition.shouldBlend());
        assertEquals(1,transition.next);
        assertEquals(1,AnimationBlend.loop(.10f,10f,4).current);
        assertEquals(0,AnimationBlend.loop(.395f,10f,4).next);
        assertFalse(AnimationBlend.loop(Float.NaN,10f,4).shouldBlend());
        assertFalse(AnimationBlend.loop(1f,0f,4).shouldBlend());
    }
    @Test public void enemiesHavePhysicalBreathingAndLungeCurves(){
        for(int enemy=0;enemy<=6;enemy++){
            assertNotEquals(AnimationBlend.enemyBreath(enemy,0,false),
                    AnimationBlend.enemyBreath(enemy,.42f,false),.001f);
            assertTrue(Math.abs(AnimationBlend.enemyAttackLean(enemy,.60f))>2f);
            assertEquals(0f,AnimationBlend.enemyAttackLean(enemy,-1f),0f);
            assertEquals(0f,AnimationBlend.enemyAttackLean(enemy,0f),0f);
        }
    }
    private static Bitmap hero(MotionArt art,float phase){
        Bitmap b=Bitmap.createBitmap(320,290,Bitmap.Config.ARGB_8888);
        assertTrue(art.hero(new Canvas(b),150,235,phase,0f,true,-1f,-1f,
                false,1,0));
        return b;
    }
    private static Bitmap enemy(MotionArt art,float phase,int kind,float attack){
        Bitmap b=Bitmap.createBitmap(250,250,Bitmap.Config.ARGB_8888);
        assertTrue(art.enemy(new Canvas(b),kind,125,198,phase,true,attack,false));
        return b;
    }
    private static int differences(Bitmap a,Bitmap b){
        int n=0;
        for(int y=65;y<235;y+=2)for(int x=58;x<215;x+=2)
            if(a.getPixel(x,y)!=b.getPixel(x,y))n++;
        return n;
    }
    @Test public void realPaintedHeroesRenderDistinctInBetweenMotionFrames(){
        MotionArt art=new MotionArt(RuntimeEnvironment.getApplication());
        Bitmap a=hero(art,.045f),b=hero(art,.095f);
        Bitmap c=hero(art,.100f);
        assertTrue("Actual in-between frame should change visible pixels",
                differences(a,b)>25);
        assertTrue("Next frame should not be identical to transition",
                differences(b,c)>12);
        a.recycle();b.recycle();c.recycle();
    }
    @Test public void allSevenPaintedEnemiesAnimateWithRealFrames(){
        MotionArt art=new MotionArt(RuntimeEnvironment.getApplication());
        for(int kind=0;kind<=6;kind++){
            Bitmap before=enemy(art,.015f,kind,-1f);
            Bitmap mid=enemy(art,.082f,kind,-1f);
            Bitmap attack=enemy(art,.20f,kind,.55f);
            assertTrue("Locomotion must be visible for enemy "+kind,
                    differences(before,mid)>20);
            assertTrue("Actual attack should differ from run for enemy "+kind,
                    differences(mid,attack)>20);
            before.recycle();mid.recycle();attack.recycle();
        }
    }
}
