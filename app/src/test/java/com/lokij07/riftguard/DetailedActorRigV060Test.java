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

/**
 * v0.60 regression renders the USER-SUPPLIED detailed WardenRig and EnemyRig,
 * not the discarded placeholder LivingRigArt.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class DetailedActorRigV060Test {
    private static int hash(Bitmap b){
        int result=31;
        for(int y=4;y<b.getHeight()-4;y+=3)
            for(int x=4;x<b.getWidth()-4;x+=3)
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
    private static Bitmap hero(int weapon,int form,int armor,float attack,float death){
        Bitmap b=Bitmap.createBitmap(300,275,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(b);c.drawColor(0xff162b38);
        assertTrue(new WardenRig().hero(c,150,242,.67f,-.3f,true,
                attack,-1,false,weapon,form*2+1,form,armor,death));
        return b;
    }
    private static Bitmap enemy(int kind,int biome,int variant,float attack,
                                boolean moving,boolean boss,int phase){
        Bitmap b=Bitmap.createBitmap(310,280,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(b);c.drawColor(0xff182b36);
        assertTrue(new EnemyRig().enemy(c,kind,biome,variant,150,242,
                1.8f,moving,attack,false,-1,boss,phase));
        return b;
    }
    @Test public void originalWardenKeepsThirtyRealOwnedWeaponForms()throws Exception{
        for(int weapon=0;weapon<6;weapon++){
            Set<Integer> appearances=new HashSet<>();
            for(int tier=0;tier<5;tier++){
                Bitmap pic=hero(weapon,tier,tier,.45f,-1);
                assertTrue("Detailed Warden form "+weapon+"/"+tier,
                        appearances.add(hash(pic)));
                save(pic,"120-v060-detailed-warden-"+weapon+"-form-"+tier+".png");
                pic.recycle();
            }
            assertEquals(5,appearances.size());
        }
    }
    @Test public void armorRanksChangeMovingRigAndDeathIsDistinct()throws Exception {
        Set<Integer> ranks=new HashSet<>();
        for(int tier=0;tier<5;tier++){
            Bitmap b=hero(1,2,tier,-1,-1);
            assertTrue("Live purchased armor "+tier,ranks.add(hash(b)));
            save(b,"121-v060-detailed-armor-"+tier+".png");
            b.recycle();
        }
        Bitmap living=hero(1,2,4,-1,-1);
        Bitmap dying=hero(1,2,4,-1,.62f);
        assertNotEquals(hash(living),hash(dying));
        save(dying,"121-v060-detailed-death.png");
        living.recycle();dying.recycle();
    }
    @Test public void originalSevenEnemySpeciesHaveDistinctSkeletalAnimations()
            throws Exception{
        Set<Integer> silhouettes=new HashSet<>();
        for(int kind=0;kind<7;kind++){
            Bitmap idle=enemy(kind,0,0,-1,false,false,1);
            Bitmap combat=enemy(kind,0,0,.58f,true,false,1);
            assertNotEquals("Enemy kind "+kind+" animates",hash(idle),hash(combat));
            assertTrue("Original enemy species "+kind,
                    silhouettes.add(hash(idle)));
            save(combat,"122-v060-detailed-enemy-"+kind+".png");
            idle.recycle();combat.recycle();
        }
        assertEquals(7,silhouettes.size());
    }
    @Test public void tenRealmsAndRealVariantsChangeBodyEquipment()throws Exception{
        Set<Integer> worldLooks=new HashSet<>();
        for(int biome=0;biome<10;biome++){
            Bitmap b=enemy(4,biome,3,.5f,true,false,1);
            assertTrue("Realm "+biome,worldLooks.add(hash(b)));
            save(b,"123-v060-detailed-realm-"+biome+".png");
            b.recycle();
        }
        assertEquals(10,worldLooks.size());
        Set<Integer> variants=new HashSet<>();
        for(int v=0;v<4;v++){
            Bitmap b=enemy(2,4,v,-1,true,false,1);
            assertTrue("Different equipment silhouette for variant "+v,
                    variants.add(hash(b)));
            b.recycle();
        }
        assertEquals(4,variants.size());
    }
    @Test public void bossPhaseAnatomyChangesWithoutModifyingCombatRules()
            throws Exception{
        Set<Integer> phases=new HashSet<>();
        for(int phase=1;phase<=3;phase++){
            Bitmap b=enemy(5,8,3,.45f,true,true,phase);
            assertTrue("Physical boss phase "+phase,phases.add(hash(b)));
            save(b,"124-v060-boss-phase-"+phase+".png");
            b.recycle();
        }
        assertEquals(3,phases.size());
    }
    @Test public void unsupportedInputsUseSpriteFallback(){
        assertFalse(new WardenRig().hero(new Canvas(),0,0,
                Float.NaN,0,false,-1,-1,false,1,0,0,0,-1));
        assertFalse(new WardenRig().hero(new Canvas(),0,0,
                0,0,false,-1,-1,false,1,0,5,0,-1));
        assertFalse(new EnemyRig().enemy(new Canvas(),7,0,0,
                0,0,0,false,-1,false,-1,true,3));
    }
}
