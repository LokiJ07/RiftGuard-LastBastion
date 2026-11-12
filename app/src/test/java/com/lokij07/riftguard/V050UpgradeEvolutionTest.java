package com.lokij07.riftguard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.content.SharedPreferences;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import static org.junit.Assert.*;

/** Verify that purchased permanent upgrades alter the REAL game renderer. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class V050UpgradeEvolutionTest {
    private static SharedPreferences saved(){
        Context c=RuntimeEnvironment.getApplication();
        SharedPreferences p=c.getSharedPreferences("v050_evolution_test",Context.MODE_PRIVATE);
        p.edit().clear().commit();return p;
    }
    private static void save(Bitmap b,String name)throws Exception{
        File root=new File(System.getProperty("user.dir"));
        File dir=new File(new File(root,"app/build.gradle").exists()?
                "app/build/screenshots":"build/screenshots");
        if(!dir.exists())assertTrue(dir.mkdirs());
        try(FileOutputStream stream=new FileOutputStream(new File(dir,name+".png"))){
            assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,stream));
        }
    }
    private static int digest(Bitmap b){
        int hash=193;
        for(int y=0;y<b.getHeight();y+=3)
            for(int x=0;x<b.getWidth();x+=3)
                hash=hash*31+b.getPixel(x,y);
        return hash;
    }
    private static Bitmap weapon(int type,int form){
        Bitmap b=Bitmap.createBitmap(280,300,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(b);
        WeaponEvolutionArt.draw(c,type,form,140,205,2.0f,0);
        return b;
    }
    @Test public void allThirtyRealWeaponFormsHaveDistinctRenderedGeometry()
            throws Exception{
        for(int type=0;type<6;type++){
            Set<Integer> hashes=new HashSet<>();
            for(int form=0;form<5;form++){
                Bitmap b=weapon(type,form);
                assertTrue("Distinct weapon silhouettes "+type+"/"+form,
                        hashes.add(digest(b)));
                save(b,"90-v050-weapon-"+type+"-form-"+form);
                b.recycle();
            }
            assertEquals(5,hashes.size());
        }
    }
    @Test public void purchasingWeaponNodeChangesAppearanceAndSurvivesReload(){
        SharedPreferences p=saved();
        p.edit().putInt("supplies",9999).commit();
        WeaponMastery mastery=new WeaponMastery(p);
        assertEquals(0,UpgradeEvolution.weaponForm(mastery,1));
        assertTrue(mastery.buy(1,1,5));
        assertEquals(1,UpgradeEvolution.weaponForm(mastery,1));
        assertTrue(mastery.buy(1,4,5));
        assertEquals(2,UpgradeEvolution.weaponForm(mastery,1));
        assertEquals(2,UpgradeEvolution.weaponForm(new WeaponMastery(p),1));
        // A second weapon's appearance remains independent.
        assertEquals(0,UpgradeEvolution.weaponForm(mastery,0));
    }
    @Test public void ascensionUnlocksTwoRealDefensesAndRealArmor(){
        SharedPreferences p=saved();
        AscensionTree asc=new AscensionTree(p);
        assertFalse(UpgradeEvolution.thunderLance(5,asc));
        assertFalse(UpgradeEvolution.guardianBeam(5,asc));
        long mask=1;
        int tower=0,hero=0,skill=0;
        for(AscensionTree.Node n:AscensionTree.NODES){
            if(n.category==AscensionTree.TOWER&&tower++<2)mask|=1L<<n.id;
            if(n.category==AscensionTree.HERO&&hero++<2)mask|=1L<<n.id;
            if(n.category==AscensionTree.SKILL&&skill++<2)mask|=1L<<n.id;
        }
        p.edit().putLong(AscensionTree.SAVE_KEY,mask).commit();
        asc=new AscensionTree(p);
        assertTrue(UpgradeEvolution.thunderLance(3,asc));
        assertFalse(UpgradeEvolution.guardianBeam(4,asc));
        assertTrue(UpgradeEvolution.guardianBeam(5,asc));
        assertTrue(UpgradeEvolution.heroArmor(asc)>0);
        assertTrue(UpgradeEvolution.skillAura(asc)>0);
        assertTrue(UpgradeEvolution.fortressForm(5,asc)>0);
        assertTrue(UpgradeEvolution.guardianBeam(5,new AscensionTree(p)));
    }
    @Test public void fortressAndNewWeaponMachinesRenderDistinctPhysicalModels()
            throws Exception{
        Set<Integer> seen=new HashSet<>();
        for(int rank=1;rank<=5;rank++){
            Bitmap b=Bitmap.createBitmap(620,490,Bitmap.Config.ARGB_8888);
            Canvas c=new Canvas(b);
            NativeDeployedMapArt.fort(c,310,305,rank,0,1);
            FortressEvolutionArt.fortress(c,310,305,rank,Math.max(0,rank-2),0);
            assertTrue("Upgrade rank "+rank+" must be visually different",
                    seen.add(digest(b)));
            save(b,"91-v050-fortress-rank-"+rank);
            b.recycle();
        }
        Bitmap vanilla=Bitmap.createBitmap(620,490,Bitmap.Config.ARGB_8888);
        Bitmap fortified=Bitmap.createBitmap(620,490,Bitmap.Config.ARGB_8888);
        FortressEvolutionArt.fortress(new Canvas(vanilla),310,305,5,0,0);
        FortressEvolutionArt.fortress(new Canvas(fortified),310,305,5,2,0);
        assertNotEquals("Ascension must add physical artillery",digest(vanilla),
                digest(fortified));
        save(fortified,"92-v050-fortress-ascension-weapons");
        vanilla.recycle();fortified.recycle();
    }
    @Test public void actualGameFiresSaveUnlockedFortressWeapons()throws Exception{
        Context ctx=RuntimeEnvironment.getApplication();
        SharedPreferences p=ctx.getSharedPreferences("riftguard_campaign_v1",
                Context.MODE_PRIVATE);
        p.edit().clear().putInt("fortress_rank",5).commit();
        long mask=1;
        int count=0;
        for(AscensionTree.Node n:AscensionTree.NODES)
            if(n.category==AscensionTree.TOWER&&count++<2)mask|=1L<<n.id;
        p.edit().putLong(AscensionTree.SAVE_KEY,mask).commit();
        RiftGameView game=new RiftGameView(ctx);
        game.layout(0,0,1280,720);
        Method start=RiftGameView.class.getDeclaredMethod("start",int.class);
        start.setAccessible(true);start.invoke(game,0);
        // Normal enemy spawns through the real server-authoritative game logic.
        Method spawn=RiftGameView.class.getDeclaredMethod("spawnEnemy");
        spawn.setAccessible(true);spawn.invoke(game);
        Field enemiesField=RiftGameView.class.getDeclaredField("enemies");
        enemiesField.setAccessible(true);
        java.util.List<?> enemies=(java.util.List<?>)enemiesField.get(game);
        assertFalse(enemies.isEmpty());
        Object foe=enemies.get(0);
        Field xField=foe.getClass().getDeclaredField("x");
        Field yField=foe.getClass().getDeclaredField("y");
        xField.setAccessible(true);yField.setAccessible(true);
        xField.setFloat(foe,1100f);yField.setFloat(foe,510f);
        Method fire=RiftGameView.class.getDeclaredMethod("fireEvolutionDefenses",
                float.class);
        fire.setAccessible(true);fire.invoke(game,.03f);
        Field shotsField=RiftGameView.class.getDeclaredField("shots");
        shotsField.setAccessible(true);
        java.util.List<?> shots=(java.util.List<?>)shotsField.get(game);
        assertEquals("Both unlocked fortress weapons launch real shots",2,shots.size());
        Set<Integer> kinds=new HashSet<>();
        for(Object shot:shots){
            Field weapon=shot.getClass().getDeclaredField("fortressWeapon");
            weapon.setAccessible(true);
            kinds.add(weapon.getInt(shot));
        }
        assertTrue(kinds.contains(1));assertTrue(kinds.contains(2));
    }
}
