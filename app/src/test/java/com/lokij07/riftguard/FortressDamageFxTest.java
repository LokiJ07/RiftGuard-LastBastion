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

/** Distinguishes cosmetic damage from real core-health-linked fortress visuals. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class FortressDamageFxTest {
    private Bitmap frame(int rank,float hp){
        Bitmap bitmap=Bitmap.createBitmap(460,420,Bitmap.Config.ARGB_8888);
        FortressDamageFx.draw(new Canvas(bitmap),220,258,rank,hp,1000,1.3f);
        return bitmap;
    }
    @Test public void damageTiersFollowActualCoreFraction(){
        assertEquals(0,FortressDamageFx.severity(1000,1000));
        assertEquals(1,FortressDamageFx.severity(640,1000));
        assertEquals(2,FortressDamageFx.severity(260,1000));
        assertEquals(3,FortressDamageFx.severity(90,1000));
        assertEquals(0,FortressDamageFx.severity(10,0));
    }
    @Test public void woodAndStoneCastleVisuallyChangeWhenDamagedAndHealBack(){
        for(int rank:new int[]{1,3,5}){
            Bitmap healthy=frame(rank,1000);
            Bitmap damaged=frame(rank,200);
            int newPixels=0;
            for(int y=60;y<360;y+=3)for(int x=66;x<400;x+=3){
                if(healthy.getPixel(x,y)!=damaged.getPixel(x,y))newPixels++;
            }
            assertTrue("Fortress "+rank+" must visually crack and smoke",newPixels>100);
            assertEquals(Color.TRANSPARENT,healthy.getPixel(220,200));
            healthy.recycle();damaged.recycle();
        }
    }
}
