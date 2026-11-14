package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public class SiegeMissionsIntegrationV069Test {
    private static Object get(Object o,String n)throws Exception{
        Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);
    }
    private static void set(Object o,String n,Object v)throws Exception{
        Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);f.set(o,v);
    }
    private static void call(RiftGameView g,String n,Class<?>[] t,Object... args)throws Exception{
        Method m=RiftGameView.class.getDeclaredMethod(n,t);m.setAccessible(true);
        m.invoke(g,args);
    }
    private static void action(RiftGameView g,String n)throws Exception{
        call(g,n,new Class<?>[0]);
    }
    private static void update(RiftGameView g,float dt)throws Exception{
        call(g,"update",new Class<?>[]{float.class},dt);
    }
    private static Context context(int stage){
        Context c=RuntimeEnvironment.getApplication();
        SharedPreferences p=c.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE);
        assertTrue(p.edit().clear().putInt("unlocked",stage).commit());return c;
    }
    private static RiftGameView start(Context c,int stage)throws Exception{
        RiftGameView g=new RiftGameView(c);g.layout(0,0,1280,720);
        call(g,"start",new Class<?>[]{int.class},stage);
        return g;
    }
    private static void waveTwo(RiftGameView g)throws Exception{
        set(g,"wave",2);set(g,"firstWavePending",false);set(g,"waiting",false);
        set(g,"spawnTimer",999f);
        action(g,"resetSiegeMission");
    }
    @SuppressWarnings("unchecked")
    private static List<Object> enemies(RiftGameView g)throws Exception{
        return (List<Object>)get(g,"enemies");
    }
    @Test public void realCaravanMovesAndCannotSkipItsObjective()throws Exception{
        Context c=context(6);RiftGameView g=start(c,6);waveTwo(g);
        set(g,"remaining",0);
        float beginning=(Float)get(g,"caravanX");
        update(g,.1f);
        assertTrue((Float)get(g,"caravanX")<beginning);
        assertFalse((Boolean)get(g,"waiting"));
        set(g,"caravanX",1251f);
        int oldGold=(Integer)get(g,"gold");
        update(g,.1f);
        assertEquals(1250f,(Float)get(g,"caravanX"),.001f);
        assertTrue((Boolean)get(g,"missionEventRewarded"));
        assertTrue((Boolean)get(g,"waiting"));
        assertEquals(oldGold+45+25,get(g,"gold"));
        update(g,.1f);
        assertEquals(oldGold+45+25,get(g,"gold"));
    }
    @Test public void realEnemiesCanDestroyTheCartAndTriggerDefeat()throws Exception{
        Context c=context(6);RiftGameView g=start(c,6);waveTwo(g);
        set(g,"remaining",0);
        float x=(Float)get(g,"caravanX");
        float y=(Float)callForY(g);
        Class<?> enemy=Class.forName("com.lokij07.riftguard.RiftGameView$Enemy");
        Constructor<?> ctor=enemy.getDeclaredConstructor(float.class,float.class,
                int.class,float.class,float.class);ctor.setAccessible(true);
        Object raider=ctor.newInstance(x+77f,y,1,100f,0f);
        enemies(g).add(raider);
        set(g,"caravanHp",8f);
        update(g,.016f);
        assertEquals("OUTRO",get(g,"scene").toString());
        assertEquals(0f,(Float)get(g,"caravanHp"),.001f);
    }
    private static Object callForY(RiftGameView g)throws Exception{
        Method m=RiftGameView.class.getDeclaredMethod("caravanY");
        m.setAccessible(true);return m.invoke(g);
    }
    @Test public void realSiegeMachineSpawnsAndTelegraphsItsCoreAttack()throws Exception{
        Context c=context(7);RiftGameView g=start(c,7);waveTwo(g);
        int count=DifficultyRules.enemies(7,2,DifficultyRules.NORMAL);
        set(g,"remaining",Math.max(2,count/2));
        action(g,"spawnEnemy");
        Object engine=enemies(g).get(0);
        assertTrue((Boolean)get(engine,"siegeEngine"));
        assertEquals(2,get(engine,"kind"));
        float core=(Float)get(g,"baseHp");
        set(g,"remaining",0);set(g,"waiting",true);
        set(engine,"engineCooldown",.01f);
        update(g,.05f);
        assertTrue((Float)get(engine,"engineWindup")>0f);
        // We are testing the actual cannon simulation, not invoking a fake hit.
        for(int i=0;i<36;i++)update(g,.05f);
        assertTrue((Float)get(g,"baseHp")<core);
        assertTrue((Integer)get(g,"siegeShots")>0);
        // An actual kill pays once and removes the threat.
        set(engine,"hp",0f);update(g,.05f);
        int awarded=(Integer)get(g,"gold");
        assertEquals(35,SiegeMissionsV069.bonusGold(SiegeMissionsV069.ENGINE));
        assertTrue((Boolean)get(g,"engineStopped"));
        update(g,.05f);
        assertEquals(awarded,get(g,"gold"));
    }
    @Test public void flankingEnemyActuallyUsesSecondaryRoute()throws Exception{
        Context c=context(8);RiftGameView g=start(c,8);waveTwo(g);
        set(g,"remaining",6);
        action(g,"spawnEnemy");
        Object flanker=enemies(g).get(0);
        assertTrue((Boolean)get(flanker,"flankRoute"));
        int side=(Integer)get(flanker,"flankSide");
        assertTrue(Math.abs(side)==1);
        assertTrue(Math.abs((Float)get(flanker,"y")-LaneRules.stageSpawnY(8))>100f);
        set(g,"remaining",0);update(g,.1f);
        assertEquals("PLAY",get(g,"scene").toString());
    }
    @Test public void coldRelaunchRestartsLiveEventAtWaveBoundary()throws Exception{
        Context c=context(6);RiftGameView g=start(c,6);waveTwo(g);
        set(g,"caravanX",1440f);
        action(g,"captureWaveCheckpoint");
        RiftGameView resumed=new RiftGameView(c);resumed.layout(0,0,1280,720);
        action(resumed,"continueSavedWave");
        assertEquals(2,get(resumed,"wave"));
        assertTrue((Boolean)get(resumed,"paused"));
        assertEquals(SiegeMissionsV069.CART_START_X,
                (Float)get(resumed,"caravanX"),.001f);
    }
}
