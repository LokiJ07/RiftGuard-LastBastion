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

/** Prevents restoration of full baked-in equipment sprites during game rendering. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class HeroEquipmentCleanupTest {
    private Bitmap draw(MotionArt art,int id,boolean withEquipment,float time,
                        float facing,boolean moving) {
        Bitmap bitmap=Bitmap.createBitmap(330,330,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bitmap);
        boolean rendered=withEquipment
                ?art.hero(canvas,164,246,time,facing,moving,-1f,-1f,false,id)
                :art.heroBody(canvas,164,246,time,facing,moving,-1f,-1f,false);
        assertTrue(rendered);
        return bitmap;
    }
    @Test public void heroBaseLayerIsNotWeaponSpecificAndRemainsAnimated() {
        MotionArt art=new MotionArt(RuntimeEnvironment.getApplication());
        assertTrue(art.heroReady());
        Bitmap bare=draw(art,0,false,1f,0f,false);
        Bitmap moving=draw(art,0,false,1.2f,0f,true);
        int changed=0,coverage=0;
        for(int y=105;y<270;y+=2)for(int x=105;x<220;x+=2) {
            if(bare.getPixel(x,y)!=moving.getPixel(x,y))changed++;
            if(Color.alpha(bare.getPixel(x,y))>80)coverage++;
        }
        assertTrue("Body must still have painted armor/limbs",coverage>180);
        assertTrue("Body should animate independently of equipment",changed>60);
        // Body stays visible, while the equipped transparent object really changes.
        for(int weapon=0;weapon<6;weapon++) {
            Bitmap armed=draw(art,weapon,true,1f,0f,false);
            int diff=0;
            for(int y=120;y<220;y+=2)for(int x=150;x<255;x+=2)
                if(armed.getPixel(x,y)!=bare.getPixel(x,y))diff++;
            assertTrue("Weapon "+weapon+" must change the in-world hand region",diff>15);
            armed.recycle();
        }
        bare.recycle();moving.recycle();
    }
    @Test public void noFullAtlasRectangleAppearsAtRightWaist() {
        MotionArt art=new MotionArt(RuntimeEnvironment.getApplication());
        Bitmap bare=draw(art,0,false,.2f,0f,false);
        // The missing original sidearm is in the removed lower-right crop.
        // The body preview must not contain a rectangular opaque source edge.
        assertEquals(Color.TRANSPARENT,bare.getPixel(232,210));
        assertEquals(Color.TRANSPARENT,bare.getPixel(239,197));
        bare.recycle();
    }
}
