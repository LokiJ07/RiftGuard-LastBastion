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
public class EncounterCounterplayIntegrationV073Test {
    private static Object get(Object o,String n)throws Exception{
        Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);
    }
    private static void set(Object o,String n,Object v)throws Exception{
        Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);f.set(o,v);
    }
    private static void call(RiftGameView g,String name,Class<?>[] sig,Object...args)throws Exception{
        Method m=RiftGameView.class.getDeclaredMethod(name,sig);
        m.setAccessible(true);m.invoke(g,args);
    }
    private static void action(RiftGameView g,String name)throws Exception{
        call(g,name,new Class<?>[0]);
    }
    private static void tick(RiftGameView g,float t)throws Exception{
        call(g,"update",new Class<?>[]{float.class},t);
    }
    private static RiftGameView start(int stage)throws Exception{
        Context c=RuntimeEnvironment.getApplication();
        SharedPreferences pref=c.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE);
        assertTrue(pref.edit().clear().putInt("unlocked",stage).commit());
        RiftGameView g=new RiftGameView(c);g.layout(0,0,1280,720);
        call(g,"start",new Class<?>[]{int.class},stage);
        set(g,"wave",2);set(g,"firstWavePending",false);set(g,"waiting",false);
        set(g,"remaining",0);set(g,"spawnTimer",999f);
        action(g,"resetSiegeMission");return g;
    }
    @SuppressWarnings("unchecked")
    private static List<Object> enemies(RiftGameView g)throws Exception{
        return (List<Object>)get(g,"enemies");
    }
    @Test public void laterRealmCaravanMovesFasterWhenWardenStaysClose()throws Exception{
        RiftGameView g=start(38);
        assertEquals(SiegeMissionsV069.ESCORT,SiegeMissionsV069.mode(38));
        float start=(Float)get(g,"caravanX");
        tick(g,.1f);float normal=(Float)get(g,"caravanX");
        set(g,"caravanX",start);
        Method m=RiftGameView.class.getDeclaredMethod("caravanY");m.setAccessible(true);
        float y=(Float)m.invoke(g);
        set(g,"heroX",start);set(g,"heroY",y);
        tick(g,.1f);float guarded=(Float)get(g,"caravanX");
        assertTrue(guarded<normal);
        assertEquals(CampaignEncountersV073.cartMaxHp(38,DifficultyRules.NORMAL),
                (Float)get(g,"caravanHp"),.001f);
    }
    @Test public void damageDuringRealCannonWindupCancelsShotWithoutHittingKeep()
            throws Exception{
        RiftGameView g=start(32);
        assertEquals(SiegeMissionsV069.ENGINE,SiegeMissionsV069.mode(32));
        int count=DifficultyRules.enemies(32,2,DifficultyRules.NORMAL);
        set(g,"remaining",Math.max(2,count/2));
        action(g,"spawnEnemy");
        Object e=enemies(g).get(0);
        assertTrue((Boolean)get(e,"siegeEngine"));
        set(g,"remaining",0);set(g,"waiting",true);
        set(e,"engineCooldown",0f);tick(g,.05f);
        assertTrue((Float)get(e,"engineWindup")>0);
        float baseline=(Float)get(g,"baseHp");
        float max=(Float)get(e,"maxHp");
        set(e,"hp",(Float)get(e,"hp")-max*.22f);
        tick(g,.05f);
        assertEquals(0f,(Float)get(e,"engineWindup"),.001f);
        assertEquals(0,(int)get(g,"siegeShots"));
        assertEquals(baseline,(Float)get(g,"baseHp"),.001f);
    }
    @Test public void laterRealmColdCheckpointRestartsUncommittedConvoy()throws Exception{
        RiftGameView g=start(38);
        set(g,"caravanX",1400f);
        action(g,"captureWaveCheckpoint");
        Context context=RuntimeEnvironment.getApplication();
        RiftGameView relaunch=new RiftGameView(context);relaunch.layout(0,0,1280,720);
        action(relaunch,"continueSavedWave");
        assertEquals(2,(int)get(relaunch,"wave"));
        assertEquals(SiegeMissionsV069.CART_START_X,
                (Float)get(relaunch,"caravanX"),.001f);
    }
}
