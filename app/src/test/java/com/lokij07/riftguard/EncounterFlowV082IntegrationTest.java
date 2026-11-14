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
public final class EncounterFlowV082IntegrationTest {
    private static Object field(Object target,String name)throws Exception{
        Field f=target.getClass().getDeclaredField(name);
        f.setAccessible(true);return f.get(target);
    }
    private static void set(Object target,String name,Object value)throws Exception{
        Field f=target.getClass().getDeclaredField(name);
        f.setAccessible(true);f.set(target,value);
    }
    private static void invoke(RiftGameView game,String name,Class<?>[] types,
                               Object... args)throws Exception{
        Method m=RiftGameView.class.getDeclaredMethod(name,types);
        m.setAccessible(true);m.invoke(game,args);
    }
    private static RiftGameView start(Context c,int stage)throws Exception{
        RiftGameView game=new RiftGameView(c);
        game.layout(0,0,1280,720);
        invoke(game,"start",new Class<?>[]{int.class},stage);
        return game;
    }

    @Test public void ordinaryLaterWaveUsesActualExistingShamanWithoutExtraUnits()
            throws Exception{
        Context c=RuntimeEnvironment.getApplication();
        RiftGameView game=start(c,31); // stage 31 has no scripted mission
        set(game,"wave",2);
        set(game,"remaining",10); // signature slot in wave 2
        assertEquals(EncounterRhythmV082.HEX,EncounterRhythmV082.plan(31,2));
        invoke(game,"spawnEnemy",new Class<?>[0]);
        List<?> enemies=(List<?>)field(game,"enemies");
        assertEquals("one spawn call produces one enemy",1,enemies.size());
        Object shaman=enemies.get(0);
        assertEquals("existing healer with real animations and attacks",
                4,field(shaman,"kind"));
        assertFalse((Boolean)field(shaman,"boss"));
        assertFalse((Boolean)field(shaman,"siegeEngine"));
    }

    @Test public void onlyDistinctConfirmedCombatActionsGrantWaveGoldWithoutSaveWrites()
            throws Exception{
        Context c=RuntimeEnvironment.getApplication();
        SharedPreferences prefs=c.getSharedPreferences(
                "riftguard_campaign_v1",Context.MODE_PRIVATE);
        prefs.edit().clear().putInt("supplies",410).commit();
        RiftGameView game=start(c,31);
        set(game,"waiting",false);
        int before=(Integer)field(game,"gold");
        set(game,"gameTime",1f);
        invoke(game,"registerCombatFlow",new Class<?>[]{int.class},
                CombatFlowV082.FINISHER);
        assertEquals(before,field(game,"gold"));
        set(game,"gameTime",2f);
        invoke(game,"registerCombatFlow",new Class<?>[]{int.class},
                CombatFlowV082.FINISHER);
        assertEquals(before,field(game,"gold"));
        set(game,"gameTime",3f);
        invoke(game,"registerCombatFlow",new Class<?>[]{int.class},
                CombatFlowV082.CHARGED);
        assertEquals(before+16,field(game,"gold")); // world 2 budget
        assertTrue(((String)field(game,"notice")).contains("WARDEN FLOW"));
        CombatFlowV082 flow=(CombatFlowV082)field(game,"combatFlow");
        assertEquals(1.09f,flow.weaponMultiplier(4f),.0001f);
        assertEquals(410,prefs.getInt("supplies",-1)); // no permanent payout

        // Cooldown and one-stipend cap survive multiple technique pairs.
        set(game,"gameTime",25f);
        invoke(game,"registerCombatFlow",new Class<?>[]{int.class},
                CombatFlowV082.EVADE);
        set(game,"gameTime",26f);
        invoke(game,"registerCombatFlow",new Class<?>[]{int.class},
                CombatFlowV082.CHARGED);
        assertEquals(before+16,field(game,"gold"));
        assertEquals(410,prefs.getInt("supplies",-1));

        // A fresh stage discards temporary combat buffs and preserves wallet.
        invoke(game,"start",new Class<?>[]{int.class},31);
        assertEquals(1f,((CombatFlowV082)field(game,"combatFlow"))
                .weaponMultiplier(26f),.0001f);
        assertEquals(410,prefs.getInt("supplies",-1));
    }
}
