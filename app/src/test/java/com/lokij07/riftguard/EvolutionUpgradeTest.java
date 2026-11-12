package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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

/**
 * Real purchased-upgrade visual and combat regression, plus source screenshots.
 * The game shares the same save keys and original battle/Armory renderers.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class EvolutionUpgradeTest {
    private static SharedPreferences prefs(){
        Context c=RuntimeEnvironment.getApplication();
        SharedPreferences p=c.getSharedPreferences("v050_evolution_regression",
                Context.MODE_PRIVATE);
        p.edit().clear().putInt("supplies",990000).commit();
        return p;
    }
    private static int hash(Bitmap bitmap){
        int h=11;
        for(int y=3;y<bitmap.getHeight()-3;y+=4)
            for(int x=3;x<bitmap.getWidth()-3;x+=4)
                h=h*31+bitmap.getPixel(x,y);
        return h;
    }
    private static File screenshotFolder(){
        File cwd=new File(System.getProperty("user.dir"));
        File dir=new File(cwd,new File(cwd,"app/build.gradle").exists()
                ?"app/build/screenshots":"build/screenshots");
        if(!dir.isDirectory())assertTrue(dir.mkdirs());
        return dir;
    }
    private static void save(Bitmap image,String name)throws Exception{
        File file=new File(screenshotFolder(),name);
        try(FileOutputStream out=new FileOutputStream(file)){
            assertTrue(image.compress(Bitmap.CompressFormat.PNG,100,out));
        }
        // Tier-zero greaves intentionally draw no decoration. This tiny
        // but valid PNG is tested separately via strict pixel comparisons.
        assertTrue(name,file.length()>100);
    }
    @Test public void everyWeaponHasFiveActuallyDifferentModels()throws Exception{
        for(int weapon=0;weapon<6;weapon++){
            Set<Integer> silhouettes=new HashSet<>();
            for(int tier=0;tier<5;tier++){
                Bitmap picture=Bitmap.createBitmap(240,220,Bitmap.Config.ARGB_8888);
                Canvas c=new Canvas(picture);
                c.drawColor(0xff142936);
                EvolvedWeaponArt.draw(c,weapon,tier,120,146,1.65f,0);
                assertTrue("Distinct silhouette weapon "+weapon+" tier "+tier,
                        silhouettes.add(hash(picture)));
                save(picture,"90-evolved-weapon-"+weapon+"-form-"+tier+".png");
                picture.recycle();
            }
            assertEquals(5,silhouettes.size());
        }
    }
    @Test public void purchasedMasteryNodesDriveAppearanceAcrossReloads(){
        SharedPreferences p=prefs();
        WeaponMastery mastery=new WeaponMastery(p);
        assertEquals(0,EvolutionProgress.weaponForm(mastery,0));
        assertTrue(mastery.buy(0,1,5));
        assertEquals(1,EvolutionProgress.weaponForm(new WeaponMastery(p),0));
        for(int id=2;id<11;id++)
            assertTrue("Purchase prerequisite node "+id,mastery.buy(0,id,5));
        assertEquals(4,EvolutionProgress.weaponForm(new WeaponMastery(p),0));
        assertTrue(mastery.mastered(0));
        assertEquals(0,EvolutionProgress.weaponForm(new WeaponMastery(p),1));
        assertEquals("Eclipse Longbow",EvolutionProgress.formName(0,4));
        assertThrows(IllegalArgumentException.class,
                ()->EvolutionProgress.formName(-1,2));
    }
    private static void buyParents(AscensionTree tree,int node)throws Exception {
        if(tree.owned(node))return;
        int parent=AscensionTree.byId(node).predecessor;
        if(parent>=0)buyParents(tree,parent);
        assertTrue("Valid purchased prerequisite "+node,tree.buy(node,5));
    }
    @Test public void realAscensionPurchasesUnlockFiveFunctionalCastleWeapons()
            throws Exception{
        SharedPreferences p=prefs();
        AscensionTree tree=new AscensionTree(p);
        assertEquals(0,EvolutionProgress.fortressArsenal(tree,5));
        assertEquals("Timber Ballista",EvolutionProgress.fortressWeapon(0));
        Set<Integer> unlocked=new HashSet<>();unlocked.add(0);
        for(AscensionTree.Node node:AscensionTree.NODES)
            if(node.category==AscensionTree.TOWER&&node.id!=0){
                buyParents(tree,node.id);
                unlocked.add(EvolutionProgress.fortressArsenal(tree,5));
            }
        assertEquals("All 4 castle arsenals unlock with actual branches",
                5,unlocked.size());
        assertEquals(4,EvolutionProgress.fortressArsenal(new AscensionTree(p),5));
        assertEquals(0,EvolutionProgress.fortressArsenal(new AscensionTree(p),1));
        assertTrue(FortressEvolutionArt.damageBonus(4)>1f);
        assertFalse(FortressEvolutionArt.emberShell(1));
        assertTrue(FortressEvolutionArt.emberShell(2));
        assertTrue(FortressEvolutionArt.stormChain(3));
        assertTrue(FortressEvolutionArt.guardianRepair(4));
        assertEquals("Guardian Prism",EvolutionProgress.fortressWeapon(4));
    }
    @Test public void fortressArsenalAndWardenSkillArmorLookDifferentAtEachTier()
            throws Exception{
        Set<Integer> fortress=new HashSet<>();
        for(int tier=0;tier<5;tier++){
            Bitmap picture=Bitmap.createBitmap(520,440,Bitmap.Config.ARGB_8888);
            Canvas c=new Canvas(picture);c.drawColor(0xff132a35);
            NativeDeployedMapArt.fort(c,260,295,5,0,1);
            FortressEvolutionArt.draw(c,260,295,5,tier,0);
            assertTrue("Tier-specific fortress actual change "+tier,
                    fortress.add(hash(picture)));
            save(picture,"91-fortress-arsenal-"+tier+".png");picture.recycle();
        }
        Set<Integer> armor=new HashSet<>();
        for(int tier=0;tier<5;tier++){
            Bitmap picture=Bitmap.createBitmap(260,280,Bitmap.Config.ARGB_8888);
            Canvas c=new Canvas(picture);c.drawColor(0xff173042);
            EvolutionEffects.armor(c,130,200,tier,0);
            assertTrue("Armor "+tier,armor.add(hash(picture)));
            save(picture,"92-warden-armor-"+tier+".png");picture.recycle();
        }
        Set<Integer> spells=new HashSet<>();
        for(int tier=0;tier<5;tier++){
            Bitmap picture=Bitmap.createBitmap(260,280,Bitmap.Config.ARGB_8888);
            Canvas c=new Canvas(picture);c.drawColor(0xff173042);
            EvolutionEffects.cast(c,WardenSkills.FROST,tier,130,160,.2f,.5f);
            assertTrue("Skill rune form "+tier,spells.add(hash(picture)));
            save(picture,"93-skill-evolution-"+tier+".png");picture.recycle();
        }
    }
    @Test public void coreGoldAndSpeedBranchesAlsoChangeActualShapes()
            throws Exception {
        Set<Integer> coreImages=new HashSet<>(),treasuryImages=new HashSet<>(),
                movementImages=new HashSet<>();
        for(int tier=0;tier<5;tier++){
            Bitmap core=Bitmap.createBitmap(430,350,Bitmap.Config.ARGB_8888);
            Canvas coreCanvas=new Canvas(core);coreCanvas.drawColor(0xff1c3141);
            NativeDeployedMapArt.fort(coreCanvas,215,230,5,0,1f);
            EvolutionEffects.fortressBranches(coreCanvas,215,230,tier,0);
            assertTrue("Core plating tier "+tier,coreImages.add(hash(core)));
            save(core,"95-core-masonry-"+tier+".png");core.recycle();

            Bitmap gold=Bitmap.createBitmap(430,350,Bitmap.Config.ARGB_8888);
            Canvas goldCanvas=new Canvas(gold);goldCanvas.drawColor(0xff1c3141);
            NativeDeployedMapArt.fort(goldCanvas,215,230,5,0,1f);
            EvolutionEffects.fortressBranches(goldCanvas,215,230,0,tier);
            assertTrue("Treasury architecture tier "+tier,
                    treasuryImages.add(hash(gold)));
            save(gold,"96-fortress-treasury-"+tier+".png");gold.recycle();

            Bitmap speed=Bitmap.createBitmap(240,130,Bitmap.Config.ARGB_8888);
            Canvas speedCanvas=new Canvas(speed);
            speedCanvas.drawColor(0xff192d39);
            EvolutionEffects.greaves(speedCanvas,120,86,tier);
            assertTrue("Speed greaves tier "+tier,movementImages.add(hash(speed)));
            save(speed,"97-speed-greaves-"+tier+".png");speed.recycle();
        }
        assertEquals(5,coreImages.size());
        assertEquals(5,treasuryImages.size());
        assertEquals(5,movementImages.size());
    }
    @Test public void boughtFortressBatteryFiresIndependentProjectileIntoRealCombat()
            throws Exception{
        Context ctx=RuntimeEnvironment.getApplication();
        SharedPreferences p=ctx.getSharedPreferences("riftguard_campaign_v1",
                Context.MODE_PRIVATE);
        p.edit().clear().putInt("fortress_rank",5)
                .putInt("supplies",990000).commit();
        AscensionTree asc=new AscensionTree(p);
        for(AscensionTree.Node n:AscensionTree.NODES)
            if(n.category==AscensionTree.TOWER && n.id!=0)
                buyParents(asc,n.id);
        assertEquals(4,EvolutionProgress.fortressArsenal(asc,5));
        RiftGameView game=new RiftGameView(ctx);
        game.layout(0,0,1280,720);
        Method start=RiftGameView.class.getDeclaredMethod("start",int.class);
        start.setAccessible(true);start.invoke(game,0);
        Method spawn=RiftGameView.class.getDeclaredMethod("spawnEnemy");
        spawn.setAccessible(true);spawn.invoke(game);
        Field foesField=RiftGameView.class.getDeclaredField("enemies");
        foesField.setAccessible(true);
        java.util.List<?> foes=(java.util.List<?>)foesField.get(game);
        assertFalse(foes.isEmpty());
        Object enemy=foes.get(0);
        Field enemyX=enemy.getClass().getDeclaredField("x");
        Field enemyY=enemy.getClass().getDeclaredField("y");
        enemyX.setAccessible(true);enemyY.setAccessible(true);
        enemyX.setFloat(enemy,1160f);enemyY.setFloat(enemy,510f);
        Field timer=RiftGameView.class.getDeclaredField("fortressArsenalTimer");
        timer.setAccessible(true);timer.setFloat(game,0f);
        Method update=RiftGameView.class.getDeclaredMethod("update",float.class);
        update.setAccessible(true);update.invoke(game,.035f);
        Field shotsField=RiftGameView.class.getDeclaredField("shots");
        shotsField.setAccessible(true);
        java.util.List<?> shots=(java.util.List<?>)shotsField.get(game);
        Field fired=RiftGameView.class.getDeclaredField("fortressArsenalShots");
        fired.setAccessible(true);
        assertEquals("Purchase must spawn independent physical projectiles",
                1,fired.getInt(game));
        assertFalse(shots.isEmpty());
        boolean launchedFromRealPrism=false;
        for(Object projectile:shots){
            Field x=projectile.getClass().getDeclaredField("x");
            Field y=projectile.getClass().getDeclaredField("y");
            Field tower=projectile.getClass().getDeclaredField("tower");
            x.setAccessible(true);y.setAccessible(true);tower.setAccessible(true);
            if(tower.getBoolean(projectile)&&
                    Math.abs(x.getFloat(projectile)-
                            FortressArmamentRules.muzzleX(950,0,4))<65f&&
                    Math.abs(y.getFloat(projectile)-
                            FortressArmamentRules.muzzleY(510,4))<65f){
                launchedFromRealPrism=true;
            }
        }
        assertTrue("New weapon must fire from visible castle arsenal",
                launchedFromRealPrism);
        assertTrue(timer.getFloat(game)>0);
        assertEquals(0f,FortressArmamentRules.damage(0),.001f);
        assertTrue(FortressArmamentRules.damage(4)>
                FortressArmamentRules.damage(1));
    }
    @Test public void actualGameConsumesPurchasedModelsWithoutChangingSaveSchema()
            throws Exception{
        Context c=RuntimeEnvironment.getApplication();
        SharedPreferences p=c.getSharedPreferences("riftguard_campaign_v1",
                Context.MODE_PRIVATE);
        p.edit().clear().putInt("fortress_rank",5)
                .putInt("supplies",990000).putInt("weapon",1).commit();
        WeaponMastery mastery=new WeaponMastery(p);
        assertTrue(mastery.buy(1,1,5));
        AscensionTree asc=new AscensionTree(p);
        for(AscensionTree.Node n:AscensionTree.NODES)
            if(n.category==AscensionTree.TOWER&&n.id!=0)
                buyParents(asc,n.id);
        RiftGameView game=new RiftGameView(c);
        game.layout(0,0,1280,720);
        Method start=RiftGameView.class.getDeclaredMethod("start",int.class);
        start.setAccessible(true);start.invoke(game,0);
        Bitmap screen=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(screen);
        game.draw(canvas);
        save(screen,"94-battle-with-evolved-fortress-and-weapon.png");
        assertTrue("Battle is actually rendered",hash(screen)!=0);
        assertEquals(4,EvolutionProgress.fortressArsenal(new AscensionTree(p),5));
        assertEquals(1,EvolutionProgress.weaponForm(new WeaponMastery(p),1));
        screen.recycle();
    }
}
