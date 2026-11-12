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

/** Regression against the visibly broken generic purple flying-monster placeholder. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class FlyingEnemyAnimationTest {
    private static Bitmap render(MotionArt art,int kind,float t,float attack) {
        Bitmap bitmap=Bitmap.createBitmap(260,230,Bitmap.Config.ARGB_8888);
        assertTrue("Flying sprite must actually render source art",
                art.enemy(new Canvas(bitmap),kind,130,187,t,true,attack,false));
        return bitmap;
    }
    @Test public void illustratedImpWingsActuallyFlapAndHaveVisibleTexture() {
        MotionArt art=new MotionArt(RuntimeEnvironment.getApplication());
        assertTrue(art.enemiesReady());
        Bitmap idle=render(art,3,.02f,-1);
        Bitmap flying=render(art,3,.17f,-1);
        Bitmap casting=render(art,3,.33f,.45f);
        int cover=0,wingMoved=0,castMoved=0;
        for(int y=65;y<190;y+=2)for(int x=45;x<220;x+=2){
            if(Color.alpha(idle.getPixel(x,y))>110)cover++;
            if(idle.getPixel(x,y)!=flying.getPixel(x,y))wingMoved++;
            if(idle.getPixel(x,y)!=casting.getPixel(x,y))castMoved++;
        }
        assertTrue("Imp is a full painted sprite, not a few circles",cover>300);
        assertTrue("Wings must physically shift between phases",wingMoved>130);
        assertTrue("Spell casting must visually differ",castMoved>90);
        Bitmap ground=render(art,0,.02f,-1);
        int distinctions=0;
        for(int y=90;y<174;y+=4)for(int x=70;x<190;x+=4)
            if(idle.getPixel(x,y)!=ground.getPixel(x,y))distinctions++;
        assertTrue("Flying enemy has a distinct winged silhouette",distinctions>65);
        idle.recycle();flying.recycle();casting.recycle();ground.recycle();
    }
}
