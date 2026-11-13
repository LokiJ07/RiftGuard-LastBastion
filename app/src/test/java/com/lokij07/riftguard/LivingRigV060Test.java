package com.lokij07.riftguard;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import static org.junit.Assert.*;

/** Real rendered regressions for the first v0.60 living-actor checkpoint. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class LivingRigV060Test {
    private static int hash(Bitmap b){
        int result=31;
        for(int y=5;y<b.getHeight()-5;y+=3)
            for(int x=5;x<b.getWidth()-5;x+=3)
                result=result*31+b.getPixel(x,y);
        return result;
    }
    private static void save(Bitmap b,String name)throws Exception{
        File cwd=new File(System.getProperty("user.dir"));
        File dir=new File(cwd,new File(cwd,"app/build.gradle").exists()
                ?"app/build/screenshots":"build/screenshots");
        if(!dir.isDirectory())assertTrue(dir.mkdirs());
        try(FileOutputStream out=new FileOutputStream(new File(dir,name))){
            assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));
        }
    }
    private static Bitmap hero(int weapon,int form,float attack){
        Bitmap b=Bitmap.createBitmap(265,235,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(b);c.drawColor(0xff162b38);
        assertTrue(new LivingRigArt().hero(c,132,205,.67f,-.3f,true,
                attack,-1,false,weapon,form,-1));
        return b;
    }
    private static Bitmap enemy(int kind,int biome,int variant,float attack,
                                boolean moving){
        Bitmap b=Bitmap.createBitmap(270,240,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(b);c.drawColor(0xff182b36);
        assertTrue(new LivingRigArt().enemy(c,kind,biome,variant,125,193,
                1.8f,moving,attack,false,-1));
        return b;
    }
    @Test public void sixWeaponFamiliesKeepAllThirtySaveBackedHandModels()
            throws Exception {
        for(int weapon=0;weapon<6;weapon++){
            Set<Integer> appearances=new HashSet<>();
            for(int tier=0;tier<5;tier++){
                Bitmap pic=hero(weapon,tier,.45f);
                assertTrue("Live Warden form "+weapon+"/"+tier,
                        appearances.add(hash(pic)));
                save(pic,"110-v060-warden-weapon-"+weapon+"-form-"+tier+".png");
                pic.recycle();
            }
            assertEquals(5,appearances.size());
        }
    }
    @Test public void everyEnemySpeciesHasVisibleMovementAndAttack()
            throws Exception {
        Set<Integer> silhouettes=new HashSet<>();
        for(int kind=0;kind<7;kind++){
            Bitmap idle=enemy(kind,0,0,-1,false);
            Bitmap combat=enemy(kind,0,0,.58f,true);
            assertNotEquals("Enemy "+kind+" should animate",
                    hash(idle),hash(combat));
            assertTrue("Seven species need distinct silhouettes",
                    silhouettes.add(hash(idle)));
            save(combat,"111-v060-living-enemy-"+kind+".png");
            idle.recycle();combat.recycle();
        }
        assertEquals(7,silhouettes.size());
    }
    @Test public void allTenRealmsCreateDifferentActualEnemyPixels()
            throws Exception {
        Set<Integer> worldLooks=new HashSet<>();
        for(int biome=0;biome<10;biome++){
            Bitmap b=enemy(4,biome,2,.5f,true);
            assertTrue("Realm "+biome+" has unique palette",
                    worldLooks.add(hash(b)));
            save(b,"112-v060-enemy-realm-"+biome+".png");
            b.recycle();
        }
        assertEquals(10,worldLooks.size());
    }
    @Test public void invalidActorInputsFallBackWithoutRendering(){
        assertFalse(LivingRigArt.validForm(-1,0));
        assertFalse(LivingRigArt.validForm(0,5));
        assertTrue(LivingRigArt.validForm(5,4));
        assertFalse(new LivingRigArt().hero(new Canvas(),0,0,
                Float.NaN,0,false,-1,-1,false,1,0,-1));
        assertFalse(new LivingRigArt().enemy(new Canvas(),7,0,0,
                0,0,0,false,-1,false,-1));
    }
}
