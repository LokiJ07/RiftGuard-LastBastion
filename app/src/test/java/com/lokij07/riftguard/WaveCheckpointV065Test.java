package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public final class WaveCheckpointV065Test {
    private static Object get(Object a,String f)throws Exception{
        Field field=a.getClass().getDeclaredField(f);
        field.setAccessible(true);
        return field.get(a);
    }
    private static void set(Object a,String f,Object value)throws Exception{
        Field field=a.getClass().getDeclaredField(f);
        field.setAccessible(true);
        field.set(a,value);
    }
    private static Object call(Object a,String name,Class<?>[] types,Object... args)throws Exception{
        Method m=a.getClass().getDeclaredMethod(name,types);
        m.setAccessible(true);
        return m.invoke(a,args);
    }
    private static void action(RiftGameView game,String name)throws Exception{
        call(game,name,new Class<?>[0]);
    }
    private static int[] tiers(int count){int[] v=new int[count];for(int i=0;i<count;i++)v[i]=i%5;return v;}
    @Test public void immutableSnapshotsCannotBeModifiedThroughSourceArrays(){
        int n=TowerPadLayout.MAX;
        int[] t=tiers(n),ty=tiers(n),ru=tiers(n);
        float[] hp=new float[n];for(int i=0;i<n;i++)hp[i]=i*20;
        WaveCheckpointV065.Snapshot s=WaveCheckpointV065.capture(2,3,120,28,2,1,0,2,
                false,85,490,100,280,420,t,ty,ru,hp);
        t[0]=4;hp[1]=999;
        assertEquals(0,s.towerTier[0]);
        assertEquals(20f,s.towerHealth[1],.001f);
        int[] x=new int[n],y=new int[n],z=new int[n];
        float[] o=new float[n];
        s.restore(x,y,z,o);
        assertEquals(0,x[0]);
        assertEquals(20f,o[1],.001f);
        assertEquals(3,s.wave);
        assertEquals(2,s.pulses);
    }
    @Test public void easyAndNormalHaveLimitedRetriesHardAndExpertDoNot(){
        assertEquals(3,WaveCheckpointV065.retries(DifficultyRules.EASY));
        assertEquals(1,WaveCheckpointV065.retries(DifficultyRules.NORMAL));
        assertEquals(0,WaveCheckpointV065.retries(DifficultyRules.HARD));
        assertEquals(0,WaveCheckpointV065.retries(DifficultyRules.EXPERT));
    }
    @Test public void actualFailedWaveRestoresGoldStructuresAndObjectiveCounters()throws Exception{
        Context ctx=RuntimeEnvironment.getApplication();
        SharedPreferences p=ctx.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE);
        p.edit().clear().putInt("unlocked",4).putInt("difficulty",DifficultyRules.NORMAL).commit();
        RiftGameView game=new RiftGameView(ctx);
        game.layout(0,0,1280,720);
        call(game,"start",new Class<?>[]{int.class},Campaign.stage(1,0));
        set(game,"wave",2);
        set(game,"gold",317);
        set(game,"heroHp",76f);
        set(game,"baseHp",512f);
        set(game,"kills",19);
        set(game,"missionBuilds",3);
        set(game,"battlePulses",2);
        int[] towers=(int[])get(game,"towers");
        float[] towerHp=(float[])get(game,"towerHp");
        towers[3]=2;towerHp[3]=100f;
        action(game,"captureWaveCheckpoint");
        // Simulate wave loss and spent resources; snapshot is immutable.
        towers[3]=0;towerHp[3]=0;set(game,"gold",10);set(game,"heroHp",0f);
        action(game,"markDefeat");
        assertTrue((Boolean)call(game,"canRetryWave",new Class<?>[0]));
        action(game,"retryWave");
        assertEquals("PLAY",get(game,"scene").toString());
        assertEquals(2,get(game,"wave"));
        assertEquals(317,get(game,"gold"));
        assertEquals(76f,(Float)get(game,"heroHp"),.001f);
        assertEquals(512f,(Float)get(game,"baseHp"),.001f);
        assertEquals(19,get(game,"kills"));
        assertEquals(3,get(game,"missionBuilds"));
        assertEquals(2,get(game,"battlePulses"));
        assertEquals(2,((int[])get(game,"towers"))[3]);
        assertEquals(100f,((float[])get(game,"towerHp"))[3],.001f);
        assertEquals(4,p.getInt("unlocked",0));
        action(game,"markDefeat");
        assertFalse((Boolean)call(game,"canRetryWave",new Class<?>[0]));
    }
    @Test public void hardModeNeverOffersCheckpointEvenAfterSeveralWaves()throws Exception{
        Context ctx=RuntimeEnvironment.getApplication();
        SharedPreferences p=ctx.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE);
        p.edit().clear().putInt("difficulty",DifficultyRules.HARD).commit();
        RiftGameView game=new RiftGameView(ctx);
        game.layout(0,0,1280,720);
        call(game,"start",new Class<?>[]{int.class},0);
        set(game,"wave",4);
        action(game,"captureWaveCheckpoint");
        action(game,"markDefeat");
        assertFalse((Boolean)call(game,"canRetryWave",new Class<?>[0]));
    }
}
