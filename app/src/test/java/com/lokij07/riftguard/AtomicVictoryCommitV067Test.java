package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

/**
 * Storage failures are recoverable: permanent rewards must not be paid
 * in memory or destroy the still-valid replay checkpoint before commit.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public final class AtomicVictoryCommitV067Test {
    private static SharedPreferences failedCommitPreferences(SharedPreferences real){
        return (SharedPreferences)Proxy.newProxyInstance(
            SharedPreferences.class.getClassLoader(),
            new Class[]{SharedPreferences.class},
            (proxy,method,args)->{
                if(!method.getName().equals("edit"))
                    return method.invoke(real,args);
                SharedPreferences.Editor delegate=real.edit();
                return Proxy.newProxyInstance(
                    SharedPreferences.Editor.class.getClassLoader(),
                    new Class[]{SharedPreferences.Editor.class},
                    (editor,call,parameters)->{
                        if(call.getName().equals("commit"))return false;
                        if(call.getName().equals("apply"))return null;
                        Object result=call.invoke(delegate,parameters);
                        return result instanceof SharedPreferences.Editor ? editor:result;
                    });
            });
    }
    private static Object get(Object view,String name)throws Exception{
        Field f=RiftGameView.class.getDeclaredField(name);
        f.setAccessible(true);return f.get(view);
    }
    private static void set(Object view,String name,Object value)throws Exception{
        Field f=RiftGameView.class.getDeclaredField(name);
        f.setAccessible(true);f.set(view,value);
    }
    private static void call(Object view,String name,Class<?>[] signature,
                             Object... values)throws Exception{
        Method m=RiftGameView.class.getDeclaredMethod(name,signature);
        m.setAccessible(true);m.invoke(view,values);
    }
    @Test public void lateWinCallbackCannotPayOutAfterDefeat()
            throws Exception{
        Context ctx=RuntimeEnvironment.getApplication();
        SharedPreferences prefs=ctx.getSharedPreferences(
                "riftguard_campaign_v1",Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        RiftGameView game=new RiftGameView(ctx);
        call(game,"start",new Class[]{int.class},0);
        int before=(Integer)get(game,"supplies");
        call(game,"markDefeat",new Class<?>[0]);
        assertEquals("OUTRO",get(game,"scene").toString());
        call(game,"complete",new Class<?>[0]);
        assertEquals(before,get(game,"supplies"));
        assertEquals(before,prefs.getInt("supplies",0));
        call(game,"updateOutro",new Class[]{float.class},60f);
        assertEquals("LOSE",get(game,"scene").toString());
        call(game,"complete",new Class<?>[0]);
        assertEquals(before,prefs.getInt("supplies",0));
        game.releaseAudio();
    }
    @Test public void lateDeathCannotReverseACommittedVictory()
            throws Exception{
        Context ctx=RuntimeEnvironment.getApplication();
        SharedPreferences prefs=ctx.getSharedPreferences(
                "riftguard_campaign_v1",Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        RiftGameView game=new RiftGameView(ctx);
        call(game,"start",new Class[]{int.class},0);
        call(game,"complete",new Class<?>[0]);
        int winningSupplies=prefs.getInt("supplies",0);
        assertTrue(winningSupplies>0);
        call(game,"markDefeat",new Class<?>[0]);
        assertEquals("OUTRO",get(game,"scene").toString());
        assertEquals(winningSupplies,prefs.getInt("supplies",0));
        game.releaseAudio();
    }
    @Test public void failedCommitCannotGrantVictoryOrEraseRecovery()
            throws Exception{
        Context context=RuntimeEnvironment.getApplication();
        SharedPreferences original=context.getSharedPreferences(
                "riftguard_campaign_v1",Context.MODE_PRIVATE);
        original.edit().clear().commit();
        RiftGameView game=new RiftGameView(context);
        call(game,"start",new Class[]{int.class},0);
        PersistentWaveCheckpointV065 persistent=
                new PersistentWaveCheckpointV065(original);
        assertNotNull(persistent.load(0));
        int suppliesBefore=(Integer)get(game,"supplies");
        int unlockedBefore=(Integer)get(game,"unlocked");
        Object originalStore=get(game,"durableBattle");
        set(game,"durableBattle",new PersistentWaveCheckpointV065(
                failedCommitPreferences(original)));
        call(game,"complete",new Class<?>[0]);
        assertEquals("PLAY",get(game,"scene").toString());
        assertTrue((Boolean)get(game,"paused"));
        assertEquals(suppliesBefore,get(game,"supplies"));
        assertEquals(unlockedBefore,get(game,"unlocked"));
        assertEquals(suppliesBefore,original.getInt("supplies",0));
        assertEquals(unlockedBefore,original.getInt("unlocked",0));
        assertNotNull("Unfinished wave remains retrievable",persistent.load(0));
        assertFalse((Boolean)get(game,"battleRecorded"));
        // Once storage is writable again the exact same end-of-wave
        // transition may commit, without paying the first failed reward.
        set(game,"durableBattle",originalStore);
        call(game,"complete",new Class<?>[0]);
        assertEquals("OUTRO",get(game,"scene").toString());
        int winningSupplies=(Integer)get(game,"supplies");
        assertTrue(winningSupplies>suppliesBefore);
        assertEquals(winningSupplies,original.getInt("supplies",0));
        assertNull(persistent.load(1));
        call(game,"complete",new Class<?>[0]);
        assertEquals("No duplicate win payments",winningSupplies,
                original.getInt("supplies",0));
        game.releaseAudio();
    }
}
