package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public final class LiveFortressAndRelicV065Test {
    private SharedPreferences prefs;
    private RiftGameView game;
    @Before public void reset(){
        Context context=RuntimeEnvironment.getApplication();
        prefs=context.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE);
        prefs.edit().clear().putInt("supplies",3000).commit();
        game=new RiftGameView(context);game.layout(0,0,1280,720);
    }
    private Object get(String key)throws Exception {
        Field f=RiftGameView.class.getDeclaredField(key);f.setAccessible(true);
        return f.get(game);
    }
    private void set(String key,Object v)throws Exception {
        Field f=RiftGameView.class.getDeclaredField(key);f.setAccessible(true);
        f.set(game,v);
    }
    private void run(String name,Class<?>[] types,Object...args)throws Exception {
        Method m=RiftGameView.class.getDeclaredMethod(name,types);m.setAccessible(true);
        m.invoke(game,args);
    }
    private void start(int stage)throws Exception{
        run("start",new Class<?>[]{int.class},stage);
    }
    @Test public void towerActuallyTakesDamageCostsGoldToRepairAndLeavesRubble()throws Exception{
        start(0);
        int[] tower=(int[])get("towers");
        float[] hp=(float[])get("towerHp");
        assertTrue(tower[0]>0);
        assertTrue(hp[0]>0);
        set("gold",500);
        set("selectedTowerPad",0);
        run("damageTower",new Class<?>[]{int.class,float.class},0,85f);
        float damaged=hp[0];
        assertTrue(damaged>0);
        run("build",new Class<?>[]{});
        assertTrue(hp[0]>damaged);
        assertTrue((Integer)get("gold")<500);
        run("damageTower",new Class<?>[]{int.class,float.class},0,99999f);
        assertEquals(0,tower[0]);
        assertEquals(1,((int[])get("destroyedTowerTier"))[0]);
        assertEquals(1,get("towersDestroyedInBattle"));
        set("gold",500);
        run("build",new Class<?>[]{});
        assertEquals(1,tower[0]);
        assertTrue(hp[0]>0);
        assertEquals(0,((int[])get("destroyedTowerTier"))[0]);
    }
    @Test public void formationDeploysWithPaymentInsideActualStage()throws Exception{
        DefenseLoadouts profiles=new DefenseLoadouts(prefs);
        int[] tiers={1,2,1,0,0,0,0,0};
        int[] types={0,2,3,3,0,0,0,0};
        profiles.save(0,tiers,types,5);
        start(0);
        int[] towers=(int[])get("towers");
        // Normal only starts with 150 gold: blueprint requests 210, but
        // affordable slots deploy partially. It must not create free upgrades.
        assertEquals(1,towers[0]);assertEquals(1,towers[1]);assertEquals(1,towers[2]);
        assertEquals(2,((int[])get("towerTypes"))[1]);
        assertEquals(40,get("gold"));
        assertTrue(((float[])get("towerHp"))[1]>0);
    }
    @Test public void firstInteractiveRelicCanFailWithoutBlockingStage()throws Exception{
        prefs.edit().putInt("unlocked",8).commit();
        game=new RiftGameView(RuntimeEnvironment.getApplication());
        game.layout(0,0,1280,720);
        int stage=Campaign.stage(3,2);
        start(stage);
        assertEquals(MemoryTrial.RELIC,((MemoryTrial.Objective)get("trial")).type);
        assertTrue((Float)get("relicHp")>0f);
        set("relicHp",0f);
        run("complete",new Class<?>[]{});
        assertEquals(9,prefs.getInt("unlocked",0));
        assertFalse(new StoryProgress(prefs).hasMemory(stage));
        start(stage);
        run("complete",new Class<?>[]{});
        assertEquals(9,prefs.getInt("unlocked",0));
        assertTrue(new StoryProgress(prefs).hasMemory(stage));
    }
    @Test public void noLossTrialTracksDestroyedTurrets()throws Exception{
        prefs.edit().putInt("unlocked",14).commit();
        game=new RiftGameView(RuntimeEnvironment.getApplication());
        int stage=Campaign.stage(5,2);
        start(stage);
        assertEquals(MemoryTrial.NO_LOSS,((MemoryTrial.Objective)get("trial")).type);
        run("damageTower",new Class<?>[]{int.class,float.class},0,9999f);
        run("complete",new Class<?>[]{});
        assertFalse(new StoryProgress(prefs).hasMemory(stage));
        start(stage);
        run("complete",new Class<?>[]{});
        assertTrue(new StoryProgress(prefs).hasMemory(stage));
    }
    @Test public void earlyWaveBonusIsPaidExactlyOnce()throws Exception{
        start(0);
        set("firstWavePending",false);
        set("waiting",true);set("wave",1);
        set("intermission",20f);
        set("gold",100);
        run("advanceWave",new Class<?>[]{boolean.class},true);
        assertEquals(2,get("wave"));
        assertEquals(135,get("gold"));
        run("advanceWave",new Class<?>[]{boolean.class},true);
        assertEquals(135,get("gold"));
    }
}
