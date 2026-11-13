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

/** Verifies real collision, animation and dodge, not just warning artwork. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public final class BossAssaultV067Test {
    private static Field field(Object obj,String name)throws Exception{
        Field f=obj.getClass().getDeclaredField(name);
        f.setAccessible(true);return f;
    }
    private static Object get(Object obj,String name)throws Exception{
        return field(obj,name).get(obj);
    }
    private static void set(Object obj,String name,Object value)throws Exception{
        field(obj,name).set(obj,value);
    }
    private static void call(Object obj,String name,Class<?>[] types,Object... values)
            throws Exception{
        Method m=obj.getClass().getDeclaredMethod(name,types);
        m.setAccessible(true);m.invoke(obj,values);
    }
    @Test public void allTenRealmsAndThreePhasesExposeConsistentDodgeZones(){
        for(int stage=0;stage<Campaign.STAGES;stage++){
            for(int phase=1;phase<=3;phase++){
                float radius=BossAssaultV067.heroRadius(stage,phase);
                assertTrue(radius>=225f&&radius<=300f);
                assertTrue(BossAssaultV067.heroInRange(stage,phase,
                        100f,100f,100f+radius-.2f,100f));
                assertFalse(BossAssaultV067.heroInRange(stage,phase,
                        100f,100f,100f+radius+.2f,100f));
                assertFalse(BossAssaultV067.heroInRange(stage,phase,
                        Float.NaN,100f,100f,100f));
                if(phase>1)assertTrue(radius>BossAssaultV067.heroRadius(stage,phase-1));
            }
        }
        assertEquals(BossThreatV066.HERO_THREAT_RADIUS,
                BossAssaultV067.heroRadius(0,1),.001f);
    }
    @Test public void renewalIsDifferentFromSeismicAndRoots(){
        for(int world=0;world<10;world++){
            int stage=Campaign.stage(world*10+1,0);
            float healing=BossAssaultV067.healFraction(stage,3);
            assertEquals(BossTactics.tactic(stage)==1?.06f:0f,healing,.0001f);
            assertEquals(healing>0,BossTactics.tactic(stage)==1);
        }
        assertTrue(BossAssaultV067.heroRadius(0,1)>
                BossAssaultV067.heroRadius(3,1));
    }
    @Test public void realBossHitTriggersHurtAnimationAndProtection()
            throws Exception {
        Context ctx=RuntimeEnvironment.getApplication();
        SharedPreferences prefs=ctx.getSharedPreferences(
                "riftguard_campaign_v1",Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        RiftGameView game=new RiftGameView(ctx);
        call(game,"start",new Class[]{int.class},29);
        set(game,"wave",DifficultyRules.waves(29,DifficultyRules.NORMAL));
        set(game,"remaining",1);
        call(game,"spawnEnemy",new Class<?>[0]);
        @SuppressWarnings("unchecked")
        List<Object> enemies=(List<Object>)get(game,"enemies");
        assertEquals(1,enemies.size());
        Object boss=enemies.get(0);
        assertTrue((Boolean)get(boss,"boss"));
        float heroX=(Float)get(game,"heroX"),heroY=(Float)get(game,"heroY");
        set(boss,"x",heroX+100f);
        set(boss,"y",heroY);
        set(boss,"specialCharge",.01f);
        set(boss,"specialTimer",99f);
        set(game,"waiting",false);
        float oldHp=(Float)get(game,"heroHp");
        call(game,"update",new Class[]{float.class},.025f);
        assertTrue("Boss slam must physically hurt the Warden",
                (Float)get(game,"heroHp")<oldHp);
        assertTrue("Collision must grant normal invulnerability window",
                (Float)get(game,"hurtCooldown")>.7f);
        WardenAnimationMachine animation=(WardenAnimationMachine)get(game,
                "heroAnimation");
        assertEquals(WardenAnimationMachine.State.HURT,animation.state());
        game.releaseAudio();
    }
    @Test public void dodgingInsideTelegraphAvoidsBossImpact()throws Exception{
        Context ctx=RuntimeEnvironment.getApplication();
        SharedPreferences prefs=ctx.getSharedPreferences(
                "riftguard_campaign_v1",Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        RiftGameView game=new RiftGameView(ctx);
        call(game,"start",new Class[]{int.class},29);
        set(game,"wave",DifficultyRules.waves(29,DifficultyRules.NORMAL));
        set(game,"remaining",1);
        call(game,"spawnEnemy",new Class<?>[0]);
        @SuppressWarnings("unchecked")
        List<Object> enemies=(List<Object>)get(game,"enemies");
        Object boss=enemies.get(0);
        float heroX=(Float)get(game,"heroX"),heroY=(Float)get(game,"heroY");
        set(boss,"x",heroX+100f);
        set(boss,"y",heroY);
        set(boss,"specialCharge",.01f);
        set(boss,"specialTimer",99f);
        set(game,"waiting",false);
        set(game,"dodgeTime",.12f);
        set(game,"dodgeDirX",0f);
        set(game,"dodgeDirY",0f);
        float oldHp=(Float)get(game,"heroHp");
        call(game,"update",new Class[]{float.class},.025f);
        assertEquals("Dodge i-frames must counter a telegraphed boss slam",
                oldHp,(Float)get(game,"heroHp"),.0001f);
        game.releaseAudio();
    }
}
