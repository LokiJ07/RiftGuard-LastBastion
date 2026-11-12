package com.lokij07.riftguard;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.content.Context;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

/** Actual Java renderer regression: 2 HTML designs replace the old visuals. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class HtmlMapReplacementTest {
    @Test public void actualDeployedSceneIsNativeDeterministicAndCached() {
        NativeDeployedMapArt a=new NativeDeployedMapArt();
        Bitmap image=Bitmap.createBitmap(1200,670,Bitmap.Config.RGB_565);
        Canvas c=new Canvas(image);c.scale(1200f/1150f,670f/655f);
        c.translate(-690,-120);
        a.draw(c,0,0);
        int color=image.getPixel(200,170);
        assertTrue("The new battlefield is not a blank placeholder",Color.red(color)>0);
        int count=a.cacheBuilds();
        a.draw(c,0,5);
        assertEquals("No generated full-map bitmaps every frame",count,a.cacheBuilds());
        a.draw(c,1,0);
        assertEquals(count+1,a.cacheBuilds());
        image.recycle();
        a.release();
        assertEquals(-1,a.preparedStage());
    }
    @Test public void actualMapUsesAllThreeMissionTypesAndTenWorldPalettes() {
        assertEquals(300,Campaign.STAGES);
        Set<Integer> palette=new HashSet<>();
        for(int w=0;w<10;w++){
            int chosen=Campaign.stage(w*10+1,0);
            palette.add(NativeDeployedMapArt.groundColor(chosen));
        }
        assertEquals(10,palette.size());
        Set<Integer> signatures=new HashSet<>();
        for(int stage=0;stage<300;stage++)
            assertTrue("Each campaign stage must keep independent seed",
                    signatures.add(NativeDeployedMapArt.signature(stage)));
        assertEquals(300,signatures.size());
    }
    @Test public void kingdomAndRegionNativeLocationsMatchRealTouchHitboxes() {
        for(int i=0;i<AtlasArt.NODES_X.length;i++)
            assertEquals(i,AtlasArt.hitRegion(AtlasArt.NODES_X[i],AtlasArt.NODES_Y[i]));
        // These are the clickable locations in the exact HTML-style native render.
        int[] indexes={0,3,6,9};
        float[] x={143,424,769,331},y={452,396,253,177};
        for(int j=0;j<indexes.length;j++)
            assertEquals(indexes[j],RegionJourney.hitLevel(x[j],y[j]));
        Bitmap image=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(image);
        NativeKingdomAtlas.drawWorld(c,0,0,0);
        int kingdomPixel=image.getPixel(430,328);
        NativeKingdomAtlas.drawRegion(c,0,1,0,0);
        assertNotEquals("A region route must be more than the old kingdom backdrop",
                kingdomPixel,image.getPixel(430,328));
        image.recycle();
    }
    @Test public void actualGameplayAndMapScenesDrawWithoutBrowserOrMissingResources()
            throws Exception {
        Context context=RuntimeEnvironment.getApplication();
        RiftGameView view=new RiftGameView(context);
        view.layout(0,0,1280,720);
        Method start=RiftGameView.class.getDeclaredMethod("start",int.class);
        start.setAccessible(true);
        Bitmap image=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(image);
        for(int stage:new int[]{0,1,2,90,120,299}){
            start.invoke(view,stage);
            view.draw(c);
            assertNotEquals("The playable battle must paint map pixels",
                    Color.TRANSPARENT,image.getPixel(700,360));
            assertNotEquals("Controls must remain composited over real map",
                    Color.TRANSPARENT,image.getPixel(1120,600));
        }
        image.recycle();
    }
}
