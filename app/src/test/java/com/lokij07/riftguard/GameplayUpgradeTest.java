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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import static org.junit.Assert.*;

/** Exercise real view, input handlers and combat systems, not mock game results. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class GameplayUpgradeTest {
    private static Object get(Object o,String field) throws Exception {
        Field f=o.getClass().getDeclaredField(field);
        f.setAccessible(true);return f.get(o);
    }
    private static void set(Object o,String field,Object v) throws Exception {
        Field f=o.getClass().getDeclaredField(field);
        f.setAccessible(true);f.set(o,v);
    }
    private static void call(RiftGameView v,String name,Class<?>[] signature,
                             Object... args) throws Exception {
        Method m=RiftGameView.class.getDeclaredMethod(name,signature);
        m.setAccessible(true);m.invoke(v,args);
    }
    private RiftGameView create(int difficulty) {
        Context c=RuntimeEnvironment.getApplication();
        c.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE)
                .edit().clear().putInt("difficulty",difficulty).putInt("fortress_rank",5).commit();
        RiftGameView v=new RiftGameView(c);
        v.layout(0,0,1280,720);
        return v;
    }
    private static void tap(RiftGameView v,float x,float y) {
        long t=SystemClock.uptimeMillis();
        MotionEvent down=MotionEvent.obtain(t,t,MotionEvent.ACTION_DOWN,x,y,0);
        v.onTouchEvent(down);down.recycle();
        MotionEvent up=MotionEvent.obtain(t,t+22,MotionEvent.ACTION_UP,x,y,0);
        v.onTouchEvent(up);up.recycle();
    }
    @Test public void selectedDifficultyPersistsAndChangesBattleWaves() throws Exception {
        RiftGameView v=create(DifficultyRules.NORMAL);
        tap(v,240,452); // Campaign
        assertEquals("MAP",get(v,"scene").toString());
        tap(v,1114,613); // Hard selector on map
        assertEquals(DifficultyRules.HARD,get(v,"difficultyMode"));
        RiftGameView restored=new RiftGameView(RuntimeEnvironment.getApplication());
        assertEquals(DifficultyRules.HARD,get(restored,"difficultyMode"));
        call(restored,"start",new Class<?>[]{int.class},2);
        assertEquals(DifficultyRules.enemies(2,1,DifficultyRules.HARD),
                get(restored,"remaining"));
        Bitmap bmp=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        restored.layout(0,0,1280,720);
        restored.draw(new Canvas(bmp));
        assertTrue(bmp.getPixel(620,70)!=0);
        bmp.recycle();
    }
    @Test public void everyTenthLevelMapCSpawnsNamedTrueWorldBoss() throws Exception {
        RiftGameView v=create(DifficultyRules.EXPERT);
        int bossStage=Campaign.stage(10,2);
        call(v,"start",new Class<?>[]{int.class},bossStage);
        int finalWave=DifficultyRules.waves(bossStage,DifficultyRules.EXPERT);
        set(v,"wave",finalWave);
        set(v,"remaining",1);
        call(v,"spawnEnemy",new Class<?>[0]);
        @SuppressWarnings("unchecked")
        List<Object> enemies=(List<Object>)get(v,"enemies");
        assertEquals(1,enemies.size());
        Object boss=enemies.get(0);
        assertEquals(5,get(boss,"kind"));
        assertTrue((Boolean)get(boss,"boss"));
        float expected=BattleRules.enemyHealth(bossStage,5)*
                DifficultyRules.health(DifficultyRules.EXPERT)*
                SiegeBalanceV066.waveHealth(finalWave)*
                DifficultyRules.finalBossMultiplier(bossStage);
        assertEquals(expected,(Float)get(boss,"hp"),.01f);
        assertTrue(get(v,"notice").toString().contains("THORNHIDE CHIEF"));
    }
    @Test public void fourUniqueMountedTurretsFireAndFrostActuallySlows() throws Exception {
        RiftGameView v=create(DifficultyRules.NORMAL);
        call(v,"start",new Class<?>[]{int.class},0);
        int[] towers=(int[])get(v,"towers");
        for(int i=0;i<4;i++)towers[i]=2;
        call(v,"spawnEnemy",new Class<?>[0]);
        @SuppressWarnings("unchecked")
        List<Object> mobs=(List<Object>)get(v,"enemies");
        Object enemy=mobs.get(0);
        set(enemy,"kind",2);
        set(enemy,"x",1005f);
        set(enemy,"y",505f);
        set(enemy,"hp",30000f);
        set(enemy,"maxHp",30000f);
        call(v,"update",new Class<?>[]{float.class},.03f);
        @SuppressWarnings("unchecked")
        List<Object> shots=(List<Object>)get(v,"shots");
        boolean[] fired=new boolean[4];
        for(Object shot:shots) {
            int slot=(Integer)get(shot,"towerSlot");
            if(slot>=0)fired[slot]=true;
        }
        for(int slot=0;slot<4;slot++)assertTrue("Missing tower family "+slot,fired[slot]);
        for(int frame=0;frame<55;frame++)
            call(v,"update",new Class<?>[]{float.class},.04f);
        assertTrue("Frost tower should cause actual movement slowdown",
                (Float)get(enemy,"slowTimer")>0f);
        Bitmap img=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        v.draw(new Canvas(img));
        img.recycle();
    }
}
