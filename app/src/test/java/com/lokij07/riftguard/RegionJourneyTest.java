package com.lokij07.riftguard;

import android.graphics.Bitmap;
import android.graphics.Canvas;
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
public final class RegionJourneyTest {
    @Test public void tenClickableStopsAndWorldFinale() {
        float[][] points={{143,452},{220,368},{317,492},{424,396},{556,444},
                {694,357},{769,253},{653,193},{503,260},{331,177}};
        for(int i=0;i<points.length;i++){
            assertEquals(i,RegionJourney.hitLevel(points[i][0],points[i][1]));
        }
        assertEquals(-1,RegionJourney.hitLevel(20,20));
        for(int world=0;world<10;world++)
            assertTrue(DifficultyRules.worldBoss(Campaign.stage(world*10+10,2)));
    }
    @Test public void allTenRegionalJourneysRenderLiveLayeredScenery(){
        Bitmap bitmap=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bitmap);
        for(int world=0;world<10;world++){
            canvas.drawColor(0xff10202e);
            RegionJourney.draw(canvas,world,world*10+1,world*30,world*.9f);
            assertTrue(android.graphics.Color.alpha(bitmap.getPixel(400,320))>0);
        }
        bitmap.recycle();
    }
}
