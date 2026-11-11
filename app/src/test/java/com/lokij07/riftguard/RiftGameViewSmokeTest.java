package com.lokij07.riftguard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;

import org.junit.Before;
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

/**
 * Android JVM runtime smoke tests: these exercise actual View touch handling,
 * canvas drawing and frame updates, beyond isolated math-only tests.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class RiftGameViewSmokeTest {
    private RiftGameView view;

    @Before public void setup() {
        Context context=RuntimeEnvironment.getApplication();
        context.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE).edit().clear().commit();
        view=new RiftGameView(context);
        view.layout(0,0,1280,720);
    }

    private void tap(float x,float y) {
        long now=android.os.SystemClock.uptimeMillis();
        MotionEvent down=MotionEvent.obtain(now,now,MotionEvent.ACTION_DOWN,x,y,0);
        view.onTouchEvent(down);
        down.recycle();
        MotionEvent up=MotionEvent.obtain(now,now+30,MotionEvent.ACTION_UP,x,y,0);
        view.onTouchEvent(up);
        up.recycle();
    }
    private static void invoke(RiftGameView game,String method,Class<?>[] args,Object... data) throws Exception {
        Method m=RiftGameView.class.getDeclaredMethod(method,args);
        m.setAccessible(true);
        m.invoke(game,data);
    }
    private static float number(Object instance,String name) throws Exception {
        Field f=instance.getClass().getDeclaredField(name);f.setAccessible(true);
        return f.getFloat(instance);
    }
    private static Object value(Object instance,String name) throws Exception {
        Field f=instance.getClass().getDeclaredField(name);f.setAccessible(true);
        return f.get(instance);
    }
    private static void set(Object instance,String name,Object x) throws Exception {
        Field f=instance.getClass().getDeclaredField(name);f.setAccessible(true);f.set(instance,x);
    }
    private void start(int stage) throws Exception {
        invoke(view,"start",new Class<?>[]{int.class},stage);
    }
    private void advance(float seconds) throws Exception {
        invoke(view,"update",new Class<?>[]{float.class},seconds);
    }
    private void paint() {
        Bitmap image=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(image));
        assertNotEquals(0,image.getPixel(640,350));
        image.recycle();
    }

    @Test public void homeMapAndBattleRenderWithoutRuntimeException() throws Exception {
        paint();
        tap(250,451); // ENTER CAMPAIGN
        assertEquals("MAP",value(view,"scene").toString());
        paint();
        tap(640,610); // DEPLOY
        assertEquals("PLAY",value(view,"scene").toString());
        paint();
    }

    @Test public void permanentUpgradeUnlocksAndEquipsRealWeapon() throws Exception {
        start(0);
        invoke(view,"complete",new Class<?>[0]);
        assertEquals(120,value(view,"supplies"));
        tap(600,555); // Results -> campaign map
        assertEquals("MAP",value(view,"scene").toString());
        tap(100,641); // Map -> home
        assertEquals("HOME",value(view,"scene").toString());
        tap(475,540); // Home -> fortress
        assertEquals("FORTRESS",value(view,"scene").toString());
        paint();
        tap(1000,580); // Buy permanent Rank 2
        assertEquals(2,value(view,"fortressRank"));
        assertEquals(0,value(view,"supplies"));
        tap(480,660); // Fortress -> Armory
        assertEquals("ARMORY",value(view,"scene").toString());
        paint();
        tap(1010,250); // Select Rank 2 Ember Sabre
        assertEquals(2,value(view,"equippedWeapon"));
        RiftGameView reloaded=new RiftGameView(RuntimeEnvironment.getApplication());
        assertEquals(2,value(reloaded,"fortressRank"));
        assertEquals(2,value(reloaded,"equippedWeapon"));
        assertEquals(0,value(reloaded,"supplies"));
        invoke(reloaded,"start",new Class<?>[]{int.class},0);
        assertEquals(Progression.maxCoreHp(2),number(reloaded,"baseHp"),.001f);
        // Unlocked melee weapon must inflict immediate close-range damage.
        invoke(reloaded,"spawnEnemy",new Class<?>[0]);
        @SuppressWarnings("unchecked")
        List<Object> foes=(List<Object>)value(reloaded,"enemies");
        Object enemy=foes.get(0);
        set(enemy,"x",1030f);set(enemy,"y",690f);
        set(enemy,"hp",300f);set(enemy,"maxHp",300f);
        invoke(reloaded,"fire",new Class<?>[0]);
        assertTrue("Equipped Ember Sabre must damage enemies",number(enemy,"hp")<300f);
    }

    @Test public void armoryDoesNotEquipLockedWeapon() throws Exception {
        tap(220,543);
        assertEquals("ARMORY",value(view,"scene").toString());
        paint();
        tap(990,465); // Frost Lance, requires Rank 5
        assertEquals(0,value(view,"equippedWeapon"));
    }

    @Test public void joystickInputMovesHeroAndStopsOnRelease() throws Exception {
        start(0);
        float before=number(view,"heroX");
        tap(244,568); // Joystick right: press + release should reset
        assertEquals(0f,number(view,"axisX"),.001f);
        long time=android.os.SystemClock.uptimeMillis();
        MotionEvent down=MotionEvent.obtain(time,time,MotionEvent.ACTION_DOWN,243f,568f,0);
        view.onTouchEvent(down);
        down.recycle();
        assertTrue(number(view,"axisX")>.9f);
        advance(.12f);
        assertTrue(number(view,"heroX")>before+18f);
        MotionEvent up=MotionEvent.obtain(time,time+200,MotionEvent.ACTION_UP,243f,568f,0);
        view.onTouchEvent(up);up.recycle();
        assertEquals(0f,number(view,"axisX"),.001f);
        float after=number(view,"heroX");
        advance(.15f);
        assertEquals(after,number(view,"heroX"),.001f);
    }

    @Test public void enemiesApproachCoreAndPursueHeroIntoMeleeRange() throws Exception {
        start(0);
        invoke(view,"spawnEnemy",new Class<?>[0]);
        @SuppressWarnings("unchecked")
        List<Object> enemies=(List<Object>)value(view,"enemies");
        assertEquals(1,enemies.size());
        Object enemy=enemies.get(0);
        float previous=(float)Math.hypot(number(enemy,"x")-950,number(enemy,"y")-510);
        for(int i=0;i<10;i++)advance(.12f);
        float next=(float)Math.hypot(number(enemy,"x")-950,number(enemy,"y")-510);
        assertTrue("Enemy should march toward the fortress",next<previous);

        // Place a regular melee enemy near the hero, beyond the wall.
        set(enemy,"kind",0);
        set(enemy,"x",1070f);
        set(enemy,"y",690f);
        set(enemy,"hp",200f);
        set(enemy,"maxHp",200f);
        float before=number(view,"heroHp");
        for(int i=0;i<45;i++)advance(.05f);
        assertTrue("Melee enemy must reach and damage player",number(view,"heroHp")<before);
    }

    @Test public void allSixEnemyTypesAdvanceAndTowersUpgrade() throws Exception {
        start(150);
        for(int i=0;i<6;i++)invoke(view,"spawnEnemy",new Class<?>[0]);
        @SuppressWarnings("unchecked")
        List<Object> monsters=(List<Object>)value(view,"enemies");
        assertEquals(6,monsters.size());
        for(int i=0;i<6;i++){
            Object e=monsters.get(i);
            set(e,"kind",i);
            set(e,"x",1600f);
            set(e,"y",510f);
            set(e,"hp",10000f);
            set(e,"maxHp",10000f);
        }
        advance(.35f);
        for(Object e:monsters)assertTrue("Enemy class did not advance: "+value(e,"kind"),number(e,"x")<1600f);
        int goldBefore=(Integer)value(view,"gold");
        invoke(view,"build",new Class<?>[0]);
        int[] towers=(int[])value(view,"towers");
        assertEquals(1,towers[1]);
        assertTrue((Integer)value(view,"gold")<goldBefore);
        assertTrue("Hero remains alive",number(view,"heroHp")>0);
    }

    @Test public void heavyMonsterCanDamageCastleCore() throws Exception {
        start(0);
        invoke(view,"spawnEnemy",new Class<?>[0]);
        @SuppressWarnings("unchecked")
        List<Object> monsters=(List<Object>)value(view,"enemies");
        Object heavy=monsters.get(0);
        set(heavy,"kind",2);
        set(heavy,"x",1105f);
        set(heavy,"y",510f);
        set(heavy,"hp",10000f);
        set(heavy,"maxHp",10000f);
        float hp=number(view,"baseHp");
        advance(.12f);
        assertTrue(number(view,"baseHp")<hp);
    }

    @Test public void normalCombatActuallyClearsTheFirstWave() throws Exception {
        start(0);
        // Deterministic automated player: hold FIRE, periodically buy defenses.
        // Enemy HP must fall from actual projectiles; no forced kills or wave skips.
        set(view,"firing",true);
        boolean cleared=false;
        for (int frame=0;frame<2400;frame++) {
            if(frame%22==0 && (Integer)value(view,"gold")>=55) {
                int[] towers=(int[])value(view,"towers");
                boolean available=false;
                for(int tier:towers)if(tier<BattleRules.MAX_TOWER_TIER)available=true;
                if(available)invoke(view,"build",new Class<?>[0]);
            }
            if(frame%220==110)invoke(view,"pulse",new Class<?>[0]);
            advance(.05f);
            if("LOSE".equals(value(view,"scene").toString()))break;
            if((Integer)value(view,"wave")>=2 || "WIN".equals(value(view,"scene").toString())) {
                cleared=true;break;
            }
        }
        assertTrue("Normal turret and hero attacks must be able to beat wave one",cleared);
        assertTrue("Kills must come from damage, not test manipulation",(Integer)value(view,"kills")>0);
    }

    @Test public void normalCombatCompletesAllWavesOfFirstStage() throws Exception {
        start(0);
        set(view,"firing",true);
        boolean won=false;
        for(int frame=0;frame<7200;frame++) {
            if(frame%22==0 && (Integer)value(view,"gold")>=55) {
                int[] towers=(int[])value(view,"towers");
                boolean available=false;
                for(int tier:towers)if(tier<BattleRules.MAX_TOWER_TIER)available=true;
                if(available)invoke(view,"build",new Class<?>[0]);
            }
            if(frame%220==110)invoke(view,"pulse",new Class<?>[0]);
            advance(.05f);
            String scene=value(view,"scene").toString();
            if("WIN".equals(scene)){won=true;break;}
            if("LOSE".equals(scene))break;
        }
        assertTrue("Actual combat, not forced wave completion, should win Level 1-A",won);
        assertEquals(1,value(view,"unlocked"));
        assertTrue((Integer)value(view,"kills")>=6);
    }

    @Test public void lootDropsCanHealHeroAndSupplyBattleGold() throws Exception {
        start(0);
        set(view,"heroHp",60f);
        Class<?> pickup=Class.forName("com.lokij07.riftguard.RiftGameView$Pickup");
        java.lang.reflect.Constructor<?> ctor=pickup.getDeclaredConstructor(
                float.class,float.class,int.class);
        ctor.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Object> drops=(List<Object>)value(view,"pickups");
        float x=number(view,"heroX"),y=number(view,"heroY");
        drops.add(ctor.newInstance(x,y,0));
        advance(.05f);
        assertEquals(90f,number(view,"heroHp"),.001f);
        assertTrue(drops.isEmpty());
        int before=(Integer)value(view,"gold");
        drops.add(ctor.newInstance(x,y,1));
        advance(.05f);
        assertEquals(before+35,value(view,"gold"));
        assertTrue(drops.isEmpty());
    }

    @Test public void pauseAndBackgroundClearStuckMovementOrFiring() throws Exception {
        start(0);
        long now=android.os.SystemClock.uptimeMillis();
        MotionEvent joy=MotionEvent.obtain(now,now,MotionEvent.ACTION_DOWN,244f,568f,0);
        view.onTouchEvent(joy);joy.recycle();
        assertTrue(number(view,"axisX")>.5f);
        assertTrue(view.handleBack());
        assertEquals(0f,number(view,"axisX"),.001f);
        assertEquals(-1,value(view,"joyPointer"));
        view.setActive(false);
        assertFalse((Boolean)value(view,"firing"));
        view.setActive(true);
        assertTrue(view.handleBack()); // resume
        assertFalse((Boolean)value(view,"paused"));
    }

    @Test public void clearedWavesUnlockAndPersistNextStage() throws Exception {
        start(0);
        int total=Campaign.waves(0);
        for(int wave=0;wave<total;wave++) {
            set(view,"remaining",0);
            ((List<?>)value(view,"enemies")).clear();
            advance(.02f);
            advance(2.6f);
        }
        assertEquals("WIN",value(view,"scene").toString());
        assertEquals(1,value(view,"unlocked"));
        assertEquals(1,RuntimeEnvironment.getApplication()
            .getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE).getInt("unlocked",0));
        paint();
    }
}
