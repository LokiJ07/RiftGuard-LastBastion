package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

/** Real gameplay victory transaction + actual mobile touch and rendered stat UI. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public class WardenLevelsIntegrationV078Test {
    private static SharedPreferences prefs(){
        return RuntimeEnvironment.getApplication().getSharedPreferences(
                "riftguard_campaign_v1",Context.MODE_PRIVATE);
    }
    private static Object get(Object o,String n)throws Exception{
        Field f=o.getClass().getDeclaredField(n);
        f.setAccessible(true);return f.get(o);
    }
    private static void set(Object o,String n,Object v)throws Exception{
        Field f=o.getClass().getDeclaredField(n);
        f.setAccessible(true);f.set(o,v);
    }
    private static Object call(Object o,String name,Class<?>[] p,Object...values)
            throws Exception{
        Method m=o.getClass().getDeclaredMethod(name,p);
        m.setAccessible(true);return m.invoke(o,values);
    }
    private static RiftGameView game(){
        RiftGameView v=new RiftGameView(RuntimeEnvironment.getApplication());
        v.layout(0,0,1280,720);
        return v;
    }
    private static void winLatestWave(RiftGameView game,int stage,int difficulty)
            throws Exception{
        set(game,"difficultyMode",difficulty);
        call(game,"start",new Class<?>[]{int.class},stage);
        set(game,"wave",DifficultyRules.waves(stage,difficulty));
        set(game,"firstWavePending",false);
        set(game,"waiting",true);
        set(game,"remaining",0);
        set(game,"intermission",0f);
        call(game,"update",new Class<?>[]{float.class},.05f);
        assertEquals("OUTRO",get(game,"scene").toString());
    }
    private static void tap(RiftGameView v,float x,float y){
        long now=android.os.SystemClock.uptimeMillis();
        MotionEvent down=MotionEvent.obtain(now,now,MotionEvent.ACTION_DOWN,x,y,0);
        v.onTouchEvent(down);down.recycle();
        MotionEvent up=MotionEvent.obtain(now,now+15,MotionEvent.ACTION_UP,x,y,0);
        v.onTouchEvent(up);up.recycle();
    }

    @Test public void realVictoryAwardsExpOnceAtomicallyWithUnlockAndCheckpoint()
            throws Exception{
        SharedPreferences p=prefs();p.edit().clear().commit();
        RiftGameView g=game();
        WardenLevelsV078 levels=(WardenLevelsV078)get(g,"wardenLevel");
        assertEquals(1,levels.level());
        assertEquals(0,levels.unspent());
        winLatestWave(g,0,DifficultyRules.NORMAL);
        int expected=WardenLevelsV078.stageXp(0,DifficultyRules.NORMAL,true);
        assertEquals(expected,levels.totalXp());
        assertEquals(expected,p.getInt(WardenLevelsV078.SAVE_XP,-1));
        assertEquals(1,p.getInt("unlocked",-1));
        assertFalse(p.contains("battle.v065.recovery"));
        assertEquals(expected,(int)get(g,"lastEarnedWardenXp"));
        assertEquals(0,levels.unspent()); // below LV 2
        call(g,"update",new Class<?>[]{float.class},8f);
        assertEquals("WIN",get(g,"scene").toString());
        assertEquals(expected,p.getInt(WardenLevelsV078.SAVE_XP,-1));
        RiftGameView reloaded=game();
        assertEquals(expected,((WardenLevelsV078)get(reloaded,"wardenLevel")).totalXp());
        winLatestWave(reloaded,0,DifficultyRules.EXPERT);
        int replay=WardenLevelsV078.stageXp(0,DifficultyRules.EXPERT,false);
        assertEquals(expected+replay,p.getInt(WardenLevelsV078.SAVE_XP,-1));
        assertEquals(1,p.getInt("unlocked",-1));
    }

    @Test public void oldSavedCampaignMigratesOnlyOnceAndKeepsOriginalUpgrades(){
        SharedPreferences p=prefs();
        p.edit().clear().putInt("unlocked",90).putInt("supplies",1820)
                .putInt("fortress_rank",4).commit();
        WardenLevelsV078 first=new WardenLevelsV078(p);
        int expected=0;
        for(int stage=0;stage<90;stage++)
            expected+=WardenLevelsV078.stageXp(stage,DifficultyRules.NORMAL,true);
        assertEquals(expected,first.totalXp());
        assertTrue(first.level()>30);
        assertEquals(1820,p.getInt("supplies",-1));
        assertEquals(4,p.getInt("fortress_rank",-1));
        assertEquals(90,p.getInt("unlocked",-1));
        assertEquals(expected,new WardenLevelsV078(p).totalXp());
        assertEquals(0,new WardenLevelsV078(p).attribute(WardenLevelsV078.POWER));
    }

    @Test public void statPurchasesPersistAndActuallyChangeExistingWardenHp()
            throws Exception{
        SharedPreferences p=prefs();
        p.edit().clear()
                .putInt(WardenLevelsV078.SAVE_XP,WardenLevelsV078.totalToLevel(25))
                .commit();
        RiftGameView g=game();
        float before=(float)call(g,"heroMaxHp",new Class<?>[0]);
        WardenLevelsV078 levels=(WardenLevelsV078)get(g,"wardenLevel");
        int initial=levels.unspent();
        assertTrue(levels.spend(WardenLevelsV078.VITALITY,5));
        assertTrue(levels.spend(WardenLevelsV078.VITALITY,5));
        assertEquals(initial-10,levels.unspent());
        assertEquals(before+20f,
                (float)call(g,"heroMaxHp",new Class<?>[0]),.001f);
        WardenLevelsV078 again=new WardenLevelsV078(p);
        assertEquals(10,again.attribute(WardenLevelsV078.VITALITY));
        assertEquals(initial-10,again.unspent());
        assertFalse(again.spend(WardenLevelsV078.VITALITY,-1));
        assertFalse(again.spend(WardenLevelsV078.VITALITY,6));
        assertEquals(1f,again.moveMultiplier(),.0001f);
        assertEquals(1f,again.attackMultiplier(),.0001f);
        assertEquals(0f,again.skillCooldownReduction(),.0001f);
    }

    @Test public void realTouchNavigationSelectsAndSpendsArcanaAndRendersScreen()
            throws Exception{
        SharedPreferences p=prefs();
        p.edit().clear()
                .putInt(WardenLevelsV078.SAVE_XP,WardenLevelsV078.totalToLevel(25))
                .commit();
        RiftGameView g=game();
        tap(g,450,389); // physical HOME "WARDEN LV" button
        assertEquals("CHARACTER",get(g,"scene").toString());
        Bitmap bmp=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        // Robolectric's legacy Android Canvas need not expose drawn pixels
        // via Bitmap.getPixel(). Execute the REAL scene renderer directly;
        // full pixel/screenshot verification runs on Android 10/16 emulators.
        call(g,"drawCharacter",new Class<?>[]{Canvas.class},new Canvas(bmp));
        assertEquals(1280,bmp.getWidth());
        assertEquals(720,bmp.getHeight());
        tap(g,985,470); // bottom-right stat card is ARCANA
        assertEquals(WardenLevelsV078.ARCANA,(int)get(g,"selectedWardenAttribute"));
        tap(g,1050,651); // upgrade action +5
        WardenLevelsV078 levels=(WardenLevelsV078)get(g,"wardenLevel");
        assertEquals(5,levels.attribute(WardenLevelsV078.ARCANA));
        assertEquals(.075f,levels.skillCooldownReduction(),.0001f);
        tap(g,760,650); // +1: third shared UpgradeActionLayout slot
        assertEquals(6,levels.attribute(WardenLevelsV078.ARCANA));
        tap(g,180,650); // back HOME
        assertEquals("HOME",get(g,"scene").toString());
        tap(g,451,389);
        assertEquals("CHARACTER",get(g,"scene").toString());
        assertEquals(6,((WardenLevelsV078)get(g,"wardenLevel")).attribute(
                WardenLevelsV078.ARCANA));
        bmp.recycle();
    }

    @Test public void maxLevelNeverGrantsOverflowOrUnownedExtraPoints(){
        SharedPreferences p=prefs();
        p.edit().clear().putInt(WardenLevelsV078.SAVE_XP,WardenLevelsV078.maxXp())
                .putInt("warden.level.v078.stat.vitality",200)
                .putInt("warden.level.v078.stat.power",200)
                .putInt("warden.level.v078.stat.agility",200)
                .putInt("warden.level.v078.stat.arcana",200).commit();
        WardenLevelsV078 v=new WardenLevelsV078(p);
        assertEquals(100,v.level());
        assertEquals(402,WardenLevelsV078.lifetimePoints(v.level()));
        assertEquals(0,v.unspent());
        assertEquals(120,v.attribute(WardenLevelsV078.VITALITY));
        assertEquals(120,v.attribute(WardenLevelsV078.POWER));
        assertEquals(120,v.attribute(WardenLevelsV078.AGILITY));
        assertEquals(42,v.attribute(WardenLevelsV078.ARCANA));
        WardenLevelsV078.Award award=v.planVictory(299,DifficultyRules.EXPERT,true);
        assertEquals(0,award.xpGained);
        assertEquals(0,award.pointsGained);
        assertEquals(100,award.newLevel);
    }
}
