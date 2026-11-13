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

/** Sprite-sheet-inspired seven actions / ten poses and genuinely distinct
 * silhouette studies. All pixels are produced by original game code. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class WardenStylesV060Test {
    private static int hash(Bitmap b){
        int h=7;
        for(int y=6;y<b.getHeight()-6;y+=4)
            for(int x=6;x<b.getWidth()-6;x+=4)
                h=h*31+b.getPixel(x,y);
        return h;
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
    private static Bitmap knight(int weapon,int form,int armor,float time,
                                 boolean moving,float attack,float cast,
                                 boolean hurt,float death){
        Bitmap b=Bitmap.createBitmap(310,300,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(b);c.drawColor(0xff182c3a);
        assertTrue(new WardenRig().hero(c,151,257,time,-.2f,moving,
                attack,cast,hurt,weapon,5+form,form,armor,death));
        return b;
    }
    private static Bitmap enemy(int kind,int variant,int biome){
        Bitmap b=Bitmap.createBitmap(340,300,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(b);c.drawColor(0xff162936);
        assertTrue(new EnemyRig().enemy(c,kind,biome,variant,168,254,
                2.5f,true,.36f,false,-1,variant==3,variant==3?3:1));
        return b;
    }
    @Test public void sevenActionClipReferenceHasTenFrameTiming(){
        assertEquals(10,WardenVisualStyles.FRAMES_PER_ACTION);
        assertEquals("Ranger",WardenVisualStyles.name(
                WardenVisualStyles.forWeapon(0)));
        assertEquals("Vanguard",WardenVisualStyles.name(
                WardenVisualStyles.forWeapon(1)));
        assertEquals("Arcanist",WardenVisualStyles.name(
                WardenVisualStyles.forWeapon(3)));
        assertThrows(IllegalArgumentException.class,
                ()->WardenVisualStyles.forWeapon(-1));
        for(int action=0;action<7;action++){
            Set<Integer> frames=new HashSet<>();
            for(int step=0;step<10;step++){
                int index=WardenVisualStyles.frame(action,step*.1f,1f);
                assertEquals("Action "+action+" frame "+step,step,index);
                frames.add(index);
            }
            assertEquals("All ten frame slots",10,frames.size());
        }
        assertEquals(9,WardenVisualStyles.frame(
                WardenVisualStyles.DIE,3.9f,1f));
        assertEquals(2,WardenVisualStyles.frame(
                WardenVisualStyles.WALK,1.2f,1f));
        assertEquals(WardenVisualStyles.JUMP,WardenVisualStyles.action(
                WardenAnimationMachine.State.DODGE,true));
        assertEquals(WardenVisualStyles.ATTACK,WardenVisualStyles.action(
                WardenAnimationMachine.State.ATTACK,false));
    }
    @Test public void allThreeOriginalHeroSilhouettesRenderAndAnimate()
            throws Exception{
        int[] id={0,1,3};
        Set<Integer> styles=new HashSet<>();
        for(int weapon:id){
            Bitmap idle=knight(weapon,3,3,.8f,false,-1,-1,false,-1);
            Bitmap attack=knight(weapon,3,3,.9f,true,.56f,-1,false,-1);
            assertTrue("Style silhouette differs from other classes",
                    styles.add(hash(idle)));
            assertNotEquals("Per-weapon attack animation changes pixels",
                    hash(idle),hash(attack));
            save(idle,"130-v060-style-"+WardenVisualStyles.name(
                    WardenVisualStyles.forWeapon(weapon))+".png");
            save(attack,"131-v060-style-attack-"+WardenVisualStyles.name(
                    WardenVisualStyles.forWeapon(weapon))+".png");
            idle.recycle();attack.recycle();
        }
        assertEquals(3,styles.size());
    }
    @Test public void allSevenEnemySpeciesCarryFourRealVisibleGearVariants()
            throws Exception{
        for(int kind=0;kind<7;kind++){
            Set<Integer> renders=new HashSet<>();
            for(int variant=0;variant<4;variant++){
                Bitmap b=enemy(kind,variant,4);
                assertTrue("Distinct physical equipment for kind "+kind+
                        " variant "+variant,renders.add(hash(b)));
                save(b,"132-v060-enemy-"+kind+"-loadout-"+variant+".png");
                b.recycle();
            }
            assertEquals(4,renders.size());
        }
    }
}
