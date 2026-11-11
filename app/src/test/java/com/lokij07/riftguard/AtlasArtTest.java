package com.lokij07.riftguard;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class AtlasArtTest {
    @Test public void allTenRegionsHaveIndividualInteractiveCoordinates() {
        assertEquals(10,AtlasArt.NODES_X.length);
        assertEquals(10,AtlasArt.NODES_Y.length);
        for(int i=0;i<10;i++) {
            float x=AtlasArt.NODES_X[i], y=AtlasArt.NODES_Y[i];
            assertEquals(i,AtlasArt.hitRegion(x,y));
            for(int j=0;j<i;j++)
                assertTrue("Overlapping marker hitboxes",
                        Math.hypot(x-AtlasArt.NODES_X[j],y-AtlasArt.NODES_Y[j])>86);
        }
        assertEquals(-1,AtlasArt.hitRegion(-100,-100));
        assertEquals(-1,AtlasArt.hitRegion(900,610));
    }
    @Test public void mapRendersItsOwnPaintedAreasAndDynamicRouteWithoutUiScreenshot() {
        Bitmap b=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(b);
        AtlasArt.draw(c,0,0,1.5f);
        int populated=0;
        for(int y=120;y<=568;y+=12)for(int x=73;x<=869;x+=12)
            if(android.graphics.Color.alpha(b.getPixel(x,y))>0)populated++;
        assertTrue("Interactive atlas has substantial rendered imagery",populated>2000);
        AtlasArt.draw(c,9,300,3.5f);
        b.recycle();
    }
}
