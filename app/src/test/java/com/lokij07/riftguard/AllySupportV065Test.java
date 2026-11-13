package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
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
public final class AllySupportV065Test {
    private static Object get(Object o,String key)throws Exception{
        Field f=o.getClass().getDeclaredField(key);f.setAccessible(true);return f.get(o);
    }
    private static void set(Object o,String key,Object v)throws Exception{
        Field f=o.getClass().getDeclaredField(key);f.setAccessible(true);f.set(o,v);
    }
    private static Object call(Object o,String name,Class<?>[] sig,Object... p)throws Exception{
        Method m=o.getClass().getDeclaredMethod(name,sig);m.setAccessible(true);
        return m.invoke(o,p);
    }
    private static Context ctx(){return RuntimeEnvironment.getApplication();}
    @Test public void recruitmentFollowsExactlyEightWorldBossSealsAndOldSaves(){
        SharedPreferences p=ctx().getSharedPreferences("v065_ally_prefs",Context.MODE_PRIVATE);
        p.edit().clear().commit();
        StoryProgress story=new StoryProgress(p);
        assertEquals(0,AllySupportV065.recruited(story));
        for(int w=0;w<8;w++){
            p.edit().putInt("unlocked",30*(w+1)).commit();
            assertTrue(AllySupportV065.unlocked(story,w));
            assertEquals(w+1,AllySupportV065.recruited(story));
            assertTrue(AllySupportV065.interval(w)>=17);
            assertFalse(AllySupportV065.NAMES[w].isEmpty());
        }
        assertThrows(IllegalArgumentException.class,()->AllySupportV065.interval(-1));
        assertThrows(IllegalArgumentException.class,()->AllySupportV065.interval(8));
    }
    @Test public void healingIsClampedAndForgeBuffActuallyIncreasesDamage(){
        assertEquals(80f,AllySupportV065.heal(60,80,40),.001f);
        assertEquals(60f,AllySupportV065.heal(60,80,-40),.001f);
        assertTrue(AllySupportV065.wardenDamageMultiplier(true)>
                AllySupportV065.wardenDamageMultiplier(false));
    }
    @Test public void actualBattleDorinAndHaleRestoreTowerAndHero()throws Exception{
        SharedPreferences p=ctx().getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE);
        p.edit().clear().putInt("unlocked",100).commit();
        RiftGameView game=new RiftGameView(ctx());
        game.layout(0,0,1280,720);
        call(game,"start",new Class<?>[]{int.class},0);
        int[] towers=(int[])get(game,"towers");
        float[] hp=(float[])get(game,"towerHp");
        towers[0]=2;hp[0]=10f;
        set(game,"heroHp",30f);
        float[] timers=(float[])get(game,"allyCooldown");
        timers[AllySupportV065.DORIN]=0f;
        timers[AllySupportV065.HALE]=0f;
        call(game,"updateAllySupport",new Class<?>[]{float.class},.05f);
        assertTrue(hp[0]>10);
        assertTrue((Float)get(game,"heroHp")>30);
    }
    @Test public void actualPellDeploysRealArtilleryProjectile()throws Exception{
        SharedPreferences p=ctx().getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE);
        p.edit().clear().putInt("unlocked",210).commit();
        RiftGameView game=new RiftGameView(ctx());
        game.layout(0,0,1280,720);
        call(game,"start",new Class<?>[]{int.class},0);
        call(game,"spawnEnemy",new Class<?>[0]);
        @SuppressWarnings("unchecked")
        List<Object> enemies=(List<Object>)get(game,"enemies");
        assertFalse(enemies.isEmpty());
        Object enemy=enemies.get(0);set(enemy,"x",1100f);set(enemy,"y",510f);
        float[] timers=(float[])get(game,"allyCooldown");
        timers[AllySupportV065.PELL]=0f;
        call(game,"updateAllySupport",new Class<?>[]{float.class},.05f);
        @SuppressWarnings("unchecked")
        List<Object> shots=(List<Object>)get(game,"shots");
        assertFalse("Pell should fire a real tracked projectile",shots.isEmpty());
        assertTrue((Boolean)get(shots.get(0),"tower"));
    }
}
