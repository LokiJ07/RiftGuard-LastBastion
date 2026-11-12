package com.lokij07.riftguard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.view.MotionEvent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

import java.io.File;
import java.io.FileOutputStream;

import static org.junit.Assert.*;

/**
 * CI generates screenshots of actual Android View render output.
 * This is not a physical-device screenshot, but it detects missing menu images and crashes.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class ScreenGalleryTest {
    private final Context context=RuntimeEnvironment.getApplication();

    private static Object getField(Object o,String name) throws Exception {
        java.lang.reflect.Field f=o.getClass().getDeclaredField(name);
        f.setAccessible(true);return f.get(o);
    }



    private void tap(RiftGameView view,float x,float y) {
        long n=SystemClock.uptimeMillis();
        MotionEvent down=MotionEvent.obtain(n,n,MotionEvent.ACTION_DOWN,x,y,0);
        view.onTouchEvent(down);down.recycle();
        MotionEvent up=MotionEvent.obtain(n,n+30,MotionEvent.ACTION_UP,x,y,0);
        view.onTouchEvent(up);up.recycle();
    }

    private void swipe(RiftGameView view,float x,float from,float to){
        long now=SystemClock.uptimeMillis();
        MotionEvent down=MotionEvent.obtain(now,now,MotionEvent.ACTION_DOWN,x,from,0);
        view.onTouchEvent(down);down.recycle();
        for(int i=1;i<=8;i++){
            float y=from+(to-from)*i/8;
            MotionEvent move=MotionEvent.obtain(now,now+i*26,
                    MotionEvent.ACTION_MOVE,x,y,0);
            view.onTouchEvent(move);move.recycle();
        }
        MotionEvent up=MotionEvent.obtain(now,now+250,MotionEvent.ACTION_UP,x,to,0);
        view.onTouchEvent(up);up.recycle();
    }

    private void capture(RiftGameView view,String name) throws Exception {
        File root=new File(System.getProperty("user.dir"));
        File folder=new File(root,new File("app/build.gradle").exists()
                ?"app/build/screenshots":"build/screenshots");
        if(!folder.exists())assertTrue(folder.mkdirs());
        Bitmap image=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(image);
        view.draw(canvas);
        File file=new File(folder,name+".png");
        try(FileOutputStream stream=new FileOutputStream(file)){
            assertTrue(image.compress(Bitmap.CompressFormat.PNG,100,stream));
        }
        assertTrue("Rendered screenshot should have image data",file.length()>4000);
        image.recycle();
    }

    private void saveCustom(Bitmap image,String name)throws Exception {
        File root=new File(System.getProperty("user.dir"));
        File folder=new File(root,new File("app/build.gradle").exists()
                ?"app/build/screenshots":"build/screenshots");
        if(!folder.exists())assertTrue(folder.mkdirs());
        File path=new File(folder,name+".png");
        try(FileOutputStream stream=new FileOutputStream(path)){
            assertTrue(image.compress(Bitmap.CompressFormat.PNG,100,stream));
        }
        assertTrue(path.length()>4000);
    }

    /** Real hero rig at gameplay animation poses, not external concept art. */
    private void captureCleanHeroRig() throws Exception {
        Bitmap image=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(image);
        c.drawColor(0xff0d2030);
        android.graphics.Paint label=new android.graphics.Paint(
                android.graphics.Paint.ANTI_ALIAS_FLAG);
        label.setColor(0xfff5d39a);label.setTextSize(28);label.setFakeBoldText(true);
        c.drawText("RIFTGUARD — ACTUAL PAINTED WARDEN ANIMATION",46,56,label);
        label.setTextSize(18);label.setColor(0xffbad3d8);label.setFakeBoldText(false);
        c.drawText("Original armor art + separate weapon rig — four combat poses",47,83,label);
        MotionArt productionRig=new MotionArt(context);
        String[] states={"IDLE / UNARMED","RUN / FOOT PLANT","ATTACK / LEAN","SPELL / CAST"};
        for(int i=0;i<4;i++){
            float left=26+i*315;
            label.setColor(0xff294454);
            c.drawRoundRect(left,108,left+296,682,18,18,label);
            label.setColor(0xffe8d2aa);label.setTextSize(20);
            c.drawText(states[i],left+24,151,label);
            c.save();
            c.translate(left-70,155);
            c.scale(2.65f,2.65f);
            assertTrue("Preview MUST show actual in-game painted hybrid",
                    productionRig.heroBody(c,83,172,i==1?.14f:.7f,
                            0f,i==1,i==2?.55f:-1f,i==3?.5f:-1f,false));
            c.restore();
            label.setColor(0xff8faab0);label.setTextSize(17);
            c.drawText(i==0?"Masked legacy weapon":i==1?"Independent steps":
                    i==2?"Torso + cape recoil":"Arm + cape casting",left+24,651,label);
        }
        saveCustom(image,"36-weaponfree-warden-animation");
        image.recycle();
    }
    private void captureBiomeAtmosphere()throws Exception {
        Bitmap image=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(image);
        c.drawColor(0xff0c1f2b);
        android.graphics.Paint label=new android.graphics.Paint(
                android.graphics.Paint.ANTI_ALIAS_FLAG);
        label.setColor(0xffedd4a5);label.setTextSize(27);label.setFakeBoldText(true);
        c.drawText("RIFTGUARD — TEN LIVING BIOME ATMOSPHERES",30,43,label);
        String[] names={"GREENHAVEN","HOLLOW WOODS","VENOM MIRE","ASH CANYON",
                "FROST PEAKS","IRON FOUNDRY","CRYSTAL DEPTHS","TEMPEST ISLES",
                "SHADOWFALL","ECLIPSE KEEP"};
        for(int biome=0;biome<10;biome++){
            float x=20+(biome%5)*254,y=72+(biome/5)*319;
            c.save();c.translate(x,y);c.clipRect(0,0,245,290);
            label.setColor(0xff25404f);
            c.drawRoundRect(0,0,245,290,12,12,label);
            c.save();c.translate(0,34);c.scale(.13f,.19f);
            BiomeAtmosphere.drawBack(c,biome,biome*25,2f);
            BiomeAtmosphere.drawFront(c,biome,biome*25,2f);
            c.restore();
            label.setColor(0xffe5d3a4);label.setTextSize(17);
            c.drawText(names[biome],12,25,label);
            c.restore();
        }
        saveCustom(image,"37-animated-biome-atmosphere");
        image.recycle();
    }

    @Test public void captureActualUiAsVisualGallery() throws Exception {
        context.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE)
                .edit().putInt("fortress_rank",2).putInt("supplies",220)
                .putInt("weapon",2).commit();
        captureCleanHeroRig();
        captureBiomeAtmosphere();
        RiftGameView view=new RiftGameView(context);
        view.layout(0,0,1280,720);
        capture(view,"01-home");
        tap(view,900,475); // Actual Kingdom hub icon / control on Home
        capture(view,"17-daily-rewards");
        tap(view,410,179);capture(view,"18-daily-quests");
        tap(view,620,179);capture(view,"19-weekly-event");
        tap(view,855,179);capture(view,"20-achievements");
        tap(view,1080,179);capture(view,"21-permanent-shop");
        tap(view,200,650); // Home
        tap(view,220,388); // optional, ad-free resource download screen
        capture(view,"10-resources-offline");
        tap(view,1020,601); // return home, no network required
        tap(view,270,450); // realm atlas
        capture(view,"02-atlas");
        tap(view,190,470); // Open Greenhaven's own route (ten locations)
        capture(view,"13-greenhaven-mission-route");
        tap(view,776,70); // Go back to full world overview
        tap(view,100,639);
        tap(view,215,544); // armory
        capture(view,"03-armory");
        tap(view,842,651); // NEW: equipable skill armory
        capture(view,"29-ability-loadout");
        tap(view,450,651); // skill screen -> weapons
        tap(view,450,656); // fortress
        capture(view,"04-fortress");
        tap(view,890,655); // Fortress -> the upward Ascension Tree
        capture(view,"32-ascension-bottom-origin");
        // Demonstrate genuinely PURCHASED paths, not just brightened icons.
        android.content.SharedPreferences ascPrefs=context.getSharedPreferences(
                "riftguard_campaign_v1",Context.MODE_PRIVATE);
        ascPrefs.edit().putInt("supplies",1600).commit();
        AscensionTree exampleTree=new AscensionTree(ascPrefs);
        for(int node=1;node<=8;node++)
            assertTrue("The growing example follows real prerequisites",
                    exampleTree.buy(node,2));
        java.lang.reflect.Field ascSupply=RiftGameView.class.getDeclaredField("supplies");
        ascSupply.setAccessible(true);
        ascSupply.setInt(view,ascPrefs.getInt("supplies",0));
        capture(view,"35-ascension-purchased-paths");
        // Six upward swipes are enough to reach the true summit of the tree.
        for(int i=0;i<7;i++)swipe(view,445,462,178);
        capture(view,"33-ascension-top-summit");
        tap(view,185,649); // Return to fortress, then Home
        tap(view,120,654); // home
        tap(view,250,449);
        tap(view,630,613); // deploy with the animated chronicle gate
        capture(view,"28-mission-briefing");
        tap(view,900,620); // deliberately BEGIN BATTLE
        // Show actual moving enemies for screenshot verification, not an empty map.
        java.lang.reflect.Method spawn=RiftGameView.class.getDeclaredMethod("spawnEnemy");
        spawn.setAccessible(true);
        java.lang.reflect.Field enemiesField=RiftGameView.class.getDeclaredField("enemies");
        enemiesField.setAccessible(true);
        java.util.List<?> living=(java.util.List<?>)enemiesField.get(view);
        for(int kind=0;kind<=6;kind++) {
            spawn.invoke(view);
            Object enemy=living.get(living.size()-1);
            java.lang.reflect.Field type=enemy.getClass().getDeclaredField("kind");
            java.lang.reflect.Field px=enemy.getClass().getDeclaredField("x");
            java.lang.reflect.Field py=enemy.getClass().getDeclaredField("y");
            type.setAccessible(true);px.setAccessible(true);py.setAccessible(true);
            type.setInt(enemy,kind);
            // The visual gallery must show the actual right-to-left attack lane,
            // not old enemies artificially placed beside/behind the fortress.
            float ex=1800-kind*75f;
            px.setFloat(enemy,ex);
            py.setFloat(enemy,LaneRules.stagePathY(0,ex)+(kind%3-1)*40f);
        }
        capture(view,"05-battle");
        // Every mode snapshot starts a REAL new battle so the hero HP,
        // initial towers and initial gold actually reflect its mode.
        java.lang.reflect.Field modeField=RiftGameView.class.getDeclaredField("difficultyMode");
        modeField.setAccessible(true);
        java.lang.reflect.Method restart=RiftGameView.class.getDeclaredMethod(
                "start",int.class);
        restart.setAccessible(true);
        for(int mode=0;mode<4;mode++){
            modeField.setInt(view,mode);
            restart.invoke(view,0);
            for(int k=0;k<7;k++)spawn.invoke(view);
            capture(view,"34-difficulty-"+DifficultyRules.NAMES[mode].toLowerCase());
        }
        modeField.setInt(view,DifficultyRules.NORMAL);
        restart.invoke(view,0);
        for(int k=0;k<7;k++)spawn.invoke(view);
        // Exercise actual movement and weapon attack state and capture the renderer.
        java.lang.reflect.Field axis=RiftGameView.class.getDeclaredField("axisX");
        axis.setAccessible(true);axis.setFloat(view,.9f);
        java.lang.reflect.Method update=RiftGameView.class.getDeclaredMethod("update",float.class);
        update.setAccessible(true);
        for(int i=0;i<8;i++)update.invoke(view,.04f);
        capture(view,"08-warden-running");
        for(int i=0;i<6;i++)update.invoke(view,.04f);
        capture(view,"27-warden-midstride");
        axis.setFloat(view,0f);
        java.lang.reflect.Field firing=RiftGameView.class.getDeclaredField("firing");
        firing.setAccessible(true);firing.setBoolean(view,true);
        update.invoke(view,.12f);
        firing.setBoolean(view,false);
        capture(view,"09-weapon-attack");
        // Render the two other genuine battlefield variants for visual regression.
        java.lang.reflect.Method start=RiftGameView.class.getDeclaredMethod("start",int.class);
        start.setAccessible(true);
        start.invoke(view,1); // Bridge Defense: visible defenders and an approaching enemy
        spawn.invoke(view);
        capture(view,"06-bridge-defense");
        start.invoke(view,2); // Siege: separate battlefield path and monster
        spawn.invoke(view);
        capture(view,"07-fortress-siege");
        // Verify that different levels, not only ABC, produce different actual roads.
        start.invoke(view,Campaign.stage(1,0));
        capture(view,"14-route-greenhaven-1A");
        start.invoke(view,Campaign.stage(5,0));
        capture(view,"15-route-greenhaven-5A");
        start.invoke(view,Campaign.stage(27,0));
        capture(view,"16-route-venom-27A");
        start.invoke(view,Campaign.stage(34,2));
        capture(view,"22-ember-magma-hazards");
        start.invoke(view,Campaign.stage(44,1));
        capture(view,"23-frost-battle-hazards");
        // Render a named world-boss siege with four distinct mounted defenses.
        start.invoke(view,Campaign.stage(10,2));
        int[] battlements=(int[])getField(view,"towers");
        for(int slot=0;slot<4;slot++)battlements[slot]=3;
        java.lang.reflect.Field waveField=RiftGameView.class.getDeclaredField("wave");
        java.lang.reflect.Field remainingField=RiftGameView.class.getDeclaredField("remaining");
        waveField.setAccessible(true);remainingField.setAccessible(true);
        waveField.setInt(view,DifficultyRules.waves(Campaign.stage(10,2),1));
        remainingField.setInt(view,1);
        spawn.invoke(view);
        capture(view,"11-world-boss-defenses");
        // Capture three actual fortress upgrade ranks, with all four weapons mounted.
        java.lang.reflect.Field rankField=RiftGameView.class.getDeclaredField("fortressRank");
        rankField.setAccessible(true);
        java.lang.reflect.Field coreHealth=RiftGameView.class.getDeclaredField("baseHp");
        coreHealth.setAccessible(true);
        for(int rank=3;rank<=5;rank++){
            rankField.setInt(view,rank);
            coreHealth.setFloat(view,Progression.maxCoreHp(rank));
            capture(view,"24-fortress-rank-"+rank);
        }
        rankField.setInt(view,5);
        coreHealth.setFloat(view,Progression.maxCoreHp(5)*.13f);
        capture(view,"31-fortress-real-damage");
        coreHealth.setFloat(view,Progression.maxCoreHp(5));
        java.lang.reflect.Field weaponField=RiftGameView.class.getDeclaredField("equippedWeapon");
        weaponField.setAccessible(true);
        for(int weapon=0;weapon<6;weapon++){
            weaponField.setInt(view,weapon);
            capture(view,"25-equipped-weapon-"+weapon);
        }
        // Record genuinely different in-battle skill VFX from live animation.
        java.lang.reflect.Field rankForSkills=RiftGameView.class.getDeclaredField("fortressRank");
        rankForSkills.setAccessible(true);rankForSkills.setInt(view,5);
        java.lang.reflect.Field skillField=RiftGameView.class.getDeclaredField("equippedSkill");
        skillField.setAccessible(true);
        java.lang.reflect.Field cdField=RiftGameView.class.getDeclaredField("pulseCooldown");
        cdField.setAccessible(true);
        java.lang.reflect.Method castSkill=RiftGameView.class.getDeclaredMethod("castSkill");
        castSkill.setAccessible(true);
        for(int skill=0;skill<WardenSkills.SKILLS.length;skill++){
            skillField.setInt(view,skill);cdField.setFloat(view,0f);
            castSkill.invoke(view);
            capture(view,"30-cast-skill-"+skill);
        }
        for(int turretTier:new int[]{1,3,5}){
            for(int slot=0;slot<4;slot++)battlements[slot]=turretTier;
            capture(view,"26-upgraded-turrets-tier-"+turretTier);
        }
        // Layout audit for ultrawide phones (safe-area side extensions).
        view.layout(0,0,1780,720);
        Bitmap panorama=Bitmap.createBitmap(1780,720,Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(panorama));
        File wideRoot=new File(System.getProperty("user.dir"));
        File wideFolder=new File(wideRoot,new File("app/build.gradle").exists()
                ?"app/build/screenshots":"build/screenshots");
        File wide=new File(wideFolder,"12-ultrawide-battle.png");
        try(FileOutputStream stream=new FileOutputStream(wide)){
            assertTrue(panorama.compress(Bitmap.CompressFormat.PNG,100,stream));
        }
        assertTrue(wide.length()>4000);
        panorama.recycle();
        view.layout(0,0,1280,720);
    }
}
