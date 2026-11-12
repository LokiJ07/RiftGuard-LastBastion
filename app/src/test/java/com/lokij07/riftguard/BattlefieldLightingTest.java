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

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class BattlefieldLightingTest {
    private static Bitmap draw(int biome,float time,boolean moving){
        Bitmap image=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(image);
        c.drawColor(0xff2d4c4b);
        c.save();c.scale(.67f,.67f);
        BattlefieldLighting.drawGround(c,biome,biome*22,time,970,525);
        BattlefieldLighting.drawHeroContact(c,490,560,moving,time*11,biome);
        c.restore();
        return image;
    }
    private static int differences(Bitmap a,Bitmap b){
        int n=0;
        for(int y=15;y<700;y+=4)for(int x=15;x<1260;x+=4)
            if(a.getPixel(x,y)!=b.getPixel(x,y))n++;
        return n;
    }
    @Test public void battlefieldLightingIsTrulyBiomeSpecificAndAnimated(){
        Bitmap forest=draw(0,1f,false),volcano=draw(3,1f,false);
        Bitmap snow=draw(4,1f,false),night=draw(8,1f,false);
        assertTrue("Ground materials must have distinct biome tints",
                differences(forest,volcano)>100);
        assertTrue(differences(snow,night)>100);
        Bitmap later=draw(3,3f,false);
        assertTrue("Flame pools and keep lanterns must animate in the world",
                differences(volcano,later)>90);
        forest.recycle();volcano.recycle();snow.recycle();
        night.recycle();later.recycle();
    }
    @Test public void footContactShadowChangesShapeWithStride(){
        Bitmap standing=draw(1,.2f,false),running=draw(1,.2f,true);
        // Sample the physical ankle/foot contact, not every fourth pixel
        // across an unrelated 1280x720 frame. The game-scale footprint is
        // around (x=328,y=375) under the hero.
        int footPixels=0;
        for(int y=354;y<397;y++)for(int x=288;x<368;x++)
            if(standing.getPixel(x,y)!=running.getPixel(x,y))footPixels++;
        assertTrue("Both planted foot shadows must shift with stride: "+footPixels,
                footPixels>30);
        standing.recycle();running.recycle();
    }
}
