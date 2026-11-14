package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class BossCounterplayV083IntegrationTest {
    private static Object get(Object o,String field)throws Exception{
        Field f=o.getClass().getDeclaredField(field);
        f.setAccessible(true);return f.get(o);
    }
    private static void set(Object o,String field,Object val)throws Exception{
        Field f=o.getClass().getDeclaredField(field);
        f.setAccessible(true);f.set(o,val);
    }
    private static Object invoke(RiftGameView game,String method,Class<?>[] sig,
                                 Object... args)throws Exception{
        Method m=RiftGameView.class.getDeclaredMethod(method,sig);
        m.setAccessible(true);return m.invoke(game,args);
    }
    private static RiftGameView stage(Context ctx,int id)throws Exception{
        RiftGameView game=new RiftGameView(ctx);
        game.layout(0,0,1280,720);
        invoke(game,"start",new Class<?>[]{int.class},id);
        return game;
    }
    private static Object enemy(int stage,boolean world,boolean cave)throws Exception{
        Class<?> cls=Class.forName("com.lokij07.riftguard.RiftGameView$Enemy");
        Constructor<?> ctor=cls.getDeclaredConstructor(
                float.class,float.class,int.class,float.class,float.class);
        ctor.setAccessible(true);
        Object e=ctor.newInstance(1220f,520f,5,600f,0f);
        set(e,"boss",true);
        set(e,"worldBoss",world);
        set(e,"anatomy",new BossAnatomyV065(Campaign.world(stage),600f));
        set(e,"specialCharge",1.3f);
        set(e,"specialTimer",6f);
        return e;
    }
    private static void hit(RiftGameView game,Object enemy,float damage)
            throws Exception{
        invoke(game,"registerBossCounterHit",
                new Class<?>[]{enemy.getClass(),float.class},enemy,damage);
    }
    private static void screenshot(RiftGameView game,String file)throws Exception{
        Bitmap image=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        game.draw(new Canvas(image));
        File cwd=new File(System.getProperty("user.dir"));
        File dir=new File(cwd,new File(cwd,"app/build.gradle").exists()?
                "app/build/screenshots":"build/screenshots");
        if(!dir.exists())assertTrue(dir.mkdirs());
        try(FileOutputStream stream=new FileOutputStream(new File(dir,file))){
            assertTrue(image.compress(Bitmap.CompressFormat.PNG,95,stream));
        }
        image.recycle();
    }

    @Test public void confirmedWardenHitsInterruptRealWorldBossCastOnce()
            throws Exception{
        int id=Campaign.stage(10,2);
        Context ctx=RuntimeEnvironment.getApplication();
        SharedPreferences prefs=ctx.getSharedPreferences(
                "riftguard_campaign_v1",Context.MODE_PRIVATE);
        prefs.edit().clear().putInt("unlocked",id).commit();
        RiftGameView game=stage(ctx,id);
        Object boss=enemy(id,true,false);
        @SuppressWarnings("unchecked")
        List<Object> mobs=(List<Object>)get(game,"enemies");
        mobs.add(boss);
        set(game,"waiting",false);
        set(boss,"counterPressure",23f);
        screenshot(game,"286-v083-boss-cast-counter-telegraph.png");
        hit(game,boss,20f);
        assertTrue((Float)get(boss,"specialCharge")>0f);
        assertEquals(43f,(Float)get(boss,"counterPressure"),.001f);
        hit(game,boss,12f);
        assertEquals(0f,(Float)get(boss,"specialCharge"),.001f);
        assertEquals(BossCounterplayV083.STAGGER_SECONDS,
                (Float)get(boss,"counterStagger"),.001f);
        assertEquals(1,get(game,"bossCounters"));
        assertEquals(0f,(Float)get(boss,"counterPressure"),.001f);
        assertEquals(1f,(Float)invoke(game,"bossArmorMultiplier",
                new Class<?>[]{boss.getClass()},boss),.001f);
        assertEquals(1.14f,(Float)invoke(game,"bossCounterWeaponMultiplier",
                new Class<?>[]{boss.getClass()},boss),.001f);
        assertTrue(((String)get(game,"notice")).contains("INTERRUPTED"));
        screenshot(game,"287-v083-boss-stagger-confirmation.png");
        hit(game,boss,200f); // cancelled cast cannot duplicate the reward
        assertEquals(1,get(game,"bossCounters"));
        invoke(game,"update",new Class<?>[]{float.class},.05f);
        assertTrue((Float)get(boss,"counterStagger")>0f);
        assertEquals(0f,(Float)get(boss,"specialCharge"),.001f);
        assertEquals(0,prefs.getInt("supplies",0));
    }

    @Test public void standardElitesAndStoryCaveBossCannotUseNewCounter()
            throws Exception{
        int id=Campaign.stage(10,2);
        Context ctx=RuntimeEnvironment.getApplication();
        SharedPreferences prefs=ctx.getSharedPreferences(
                "riftguard_campaign_v1",Context.MODE_PRIVATE);
        prefs.edit().clear().putInt("unlocked",id).commit();
        RiftGameView game=stage(ctx,id);
        Object elite=enemy(id,false,false);
        hit(game,elite,10000f);
        assertEquals(1.3f,(Float)get(elite,"specialCharge"),.001f);
        assertEquals(0,get(game,"bossCounters"));
        set(game,"caveMode",true);
        Object cave=enemy(id,true,true);
        hit(game,cave,10000f);
        assertEquals(1.3f,(Float)get(cave,"specialCharge"),.001f);
        assertEquals(0,get(game,"bossCounters"));
    }

    @Test public void realWardenProjectileInterruptsButTurretShotsNeverDo()
            throws Exception{
        int id=Campaign.stage(10,2);
        Context ctx=RuntimeEnvironment.getApplication();
        SharedPreferences prefs=ctx.getSharedPreferences(
                "riftguard_campaign_v1",Context.MODE_PRIVATE);
        prefs.edit().clear().putInt("unlocked",id).commit();
        RiftGameView game=stage(ctx,id);
        Object boss=enemy(id,true,false);
        @SuppressWarnings("unchecked")
        List<Object> mobs=(List<Object>)get(game,"enemies");
        mobs.add(boss);
        set(game,"waiting",false);
        set(game,"spawnTimer",999f);
        @SuppressWarnings("unchecked")
        List<Object> shots=(List<Object>)get(game,"shots");
        Class<?> shotType=Class.forName("com.lokij07.riftguard.RiftGameView$Shot");
        Constructor<?> shotCtor=shotType.getDeclaredConstructor(
                float.class,float.class,float.class,float.class,
                boss.getClass(),boolean.class);
        shotCtor.setAccessible(true);
        Object wardenShot=shotCtor.newInstance(1220f,510f,75f,730f,boss,false);
        set(wardenShot,"weaponId",0);
        shots.add(wardenShot);
        invoke(game,"update",new Class<?>[]{float.class},.02f);
        assertEquals(1,get(game,"bossCounters"));
        assertEquals(0f,(Float)get(boss,"specialCharge"),.001f);
        // Reset the real enemy for a later cast; turret damage may hit the
        // boss but must never grant a Warden-only pressure interrupt.
        set(boss,"counterStagger",0f);
        set(boss,"specialCharge",1.3f);
        set(boss,"counterPressure",0f);
        Object turretShot=shotCtor.newInstance(1220f,510f,175f,730f,boss,true);
        set(turretShot,"towerSlot",0);
        shots.add(turretShot);
        invoke(game,"update",new Class<?>[]{float.class},.02f);
        assertEquals(1,get(game,"bossCounters"));
        assertTrue((Float)get(boss,"specialCharge")>0f);
        assertEquals(0f,(Float)get(boss,"counterPressure"),.001f);
        assertEquals(0,prefs.getInt("supplies",0));
    }

    @Test public void firstClearMasteryUsesExistingDurableVictoryOnlyOnce()
            throws Exception{
        int id=Campaign.stage(10,2);
        Context ctx=RuntimeEnvironment.getApplication();
        SharedPreferences prefs=ctx.getSharedPreferences(
                "riftguard_campaign_v1",Context.MODE_PRIVATE);
        prefs.edit().clear().putInt("unlocked",id)
                .putInt("supplies",410).commit();
        RiftGameView game=stage(ctx,id);
        set(game,"bossCounters",12); // even many interrupts cap at two
        invoke(game,"complete",new Class<?>[0]);
        assertEquals("OUTRO",get(game,"scene").toString());
        assertEquals(38,get(game,"bossMasteryBonus"));
        assertEquals(30,get(game,"unlocked"));
        assertEquals(30,prefs.getInt("unlocked",-1));
        int afterFirst=prefs.getInt("supplies",-1);
        assertTrue(afterFirst>=410+Progression.stageReward(id,true)+38);

        invoke(game,"start",new Class<?>[]{int.class},id);
        set(game,"bossCounters",8);
        invoke(game,"complete",new Class<?>[0]);
        assertEquals(0,get(game,"bossMasteryBonus"));
        assertEquals(30,prefs.getInt("unlocked",-1));
        assertEquals(afterFirst+Progression.stageReward(id,false),
                prefs.getInt("supplies",-1));
    }

    @Test public void defeatCannotPayBossMasteryOrShowVictory()
            throws Exception{
        int id=Campaign.stage(10,2);
        Context ctx=RuntimeEnvironment.getApplication();
        SharedPreferences prefs=ctx.getSharedPreferences(
                "riftguard_campaign_v1",Context.MODE_PRIVATE);
        prefs.edit().clear().putInt("unlocked",id)
                .putInt("supplies",90).commit();
        RiftGameView game=stage(ctx,id);
        set(game,"bossCounters",1);
        // A real successful atomic commit is tested above. Defeat cannot
        // enter complete() or credit a mastery award.
        invoke(game,"markDefeat",new Class<?>[0]);
        assertEquals("OUTRO",get(game,"scene").toString());
        assertEquals(0,get(game,"bossMasteryBonus"));
        assertEquals(90,prefs.getInt("supplies",-1));
        assertEquals(id,prefs.getInt("unlocked",-1));
    }
}
