package com.lokij07.riftguard;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class EnvironmentDistrictsTest {
    private static int stage(int world,int phase){
        return world*30+(phase==0?0:phase==1?12:21);
    }
    private static Bitmap district(int s){
        Bitmap a=Bitmap.createBitmap(760,210,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(a);
        c.scale(.4f,.4f);
        EnvironmentDistricts.draw(c,s);
        return a;
    }
    private static int diff(Bitmap a,Bitmap b){
        int changed=0;
        for(int y=95;y<196;y+=3)
            for(int x=24;x<753;x+=3)
                if(a.getPixel(x,y)!=b.getPixel(x,y))changed++;
        return changed;
    }
    @Test public void all300StagesResolveIntoThirtyNamedSubregions(){
        assertEquals(300,Campaign.STAGES);
        assertEquals(30,EnvironmentDistricts.DISTRICTS);
        Set<Integer> districtIds=new HashSet<>();
        Set<String> names=new HashSet<>();
        for(int i=0;i<Campaign.STAGES;i++){
            int id=EnvironmentDistricts.district(i);
            assertTrue(id>=0&&id<30);
            assertEquals(Campaign.world(i),id/3);
            assertTrue("Names must be descriptive",EnvironmentDistricts.name(i).length()>=8);
            districtIds.add(id);names.add(EnvironmentDistricts.name(i));
 // The 3 A/B/C maps share one district within a given level.
            if(i%3==0){
                assertEquals(id,EnvironmentDistricts.district(i+1));
                assertEquals(id,EnvironmentDistricts.district(i+2));
            }
        }
        assertEquals(30,districtIds.size());
        assertEquals("Every landscape subregion must have a unique name",30,names.size());
        assertThrows(IllegalArgumentException.class,()->EnvironmentDistricts.phase(300));
        assertThrows(IllegalArgumentException.class,()->EnvironmentDistricts.namesForWorld(10));
    }
    @Test public void threeSubregionsHaveDistinctRenderedSilhouettesInEveryBiome(){
        for(int w=0;w<10;w++){
            Bitmap grove=district(stage(w,0));
            Bitmap bridge=district(stage(w,1));
            Bitmap gate=district(stage(w,2));
            assertTrue("Wild district vs bridge must be visually distinct in world "+w,
                    diff(grove,bridge)>50);
            assertTrue("Bridge vs rift sanctuary must visibly change in world "+w,
                    diff(bridge,gate)>50);
            assertTrue("First and third district should not be palette swaps "+w,
                    diff(grove,gate)>50);
            // Top and bottom game-view area cannot be covered by new art.
            assertEquals(Color.TRANSPARENT,grove.getPixel(379,205));
            assertEquals(Color.TRANSPARENT,bridge.getPixel(379,205));
            assertEquals(Color.TRANSPARENT,gate.getPixel(379,205));
            grove.recycle();bridge.recycle();gate.recycle();
        }
    }
    @Test public void extraDistrictArtCachesInsteadOfRepaintingEveryFrame(){
        LivingBattlefield renderer=new LivingBattlefield();
        int current=stage(3,2);
        renderer.prepare(current);
        int before=renderer.cacheGenerations();
        Bitmap scene=Bitmap.createBitmap(950,260,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(scene);c.scale(.5f,.5f);
        renderer.draw(c,current,1f,1234,614,false);
        renderer.draw(c,current,1.2f,1420,614,false);
        assertEquals(before,renderer.cacheGenerations());
        assertEquals(5,renderer.activeBitmapCount());
        // New A/B/C stage maps still present all three matching district assets.
        for(int m=0;m<3;m++)
            assertEquals(EnvironmentDistricts.district(current),
                    EnvironmentDistricts.district(current+m));
        scene.recycle();renderer.release();
    }
}
