package com.lokij07.riftguard;

import android.content.Context;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

/**
 * Real in-engine spawn logic, not simply a unit test of the formation math.
 * Closing an active slot permits the next queued enemy to arrive.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public final class SiegeLiveWaveV066Test {
    private static Field field(String name)throws Exception {
        Field f=RiftGameView.class.getDeclaredField(name);
        f.setAccessible(true);return f;
    }
    @Test public void simultaneousCapQueuesUnspawnedEnemiesAcrossAllModes()
            throws Exception {
        Method start=RiftGameView.class.getDeclaredMethod("start",int.class);
        Method spawn=RiftGameView.class.getDeclaredMethod("spawnEnemy");
        Method update=RiftGameView.class.getDeclaredMethod("update",float.class);
        start.setAccessible(true);spawn.setAccessible(true);update.setAccessible(true);
        Field enemiesField=field("enemies");
        Field waiting=field("waiting"),first=field("firstWavePending");
        Field timer=field("spawnTimer"),remaining=field("remaining");
        for(int mode=0;mode<=DifficultyRules.EXPERT;mode++){
            Context ctx=RuntimeEnvironment.getApplication();
            ctx.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE)
                    .edit().clear().putInt("difficulty",mode).commit();
            RiftGameView view=new RiftGameView(ctx);
            start.invoke(view,0);
            @SuppressWarnings("unchecked")
            List<Object> enemies=(List<Object>)enemiesField.get(view);
            int cap=SiegeBalanceV066.activeLimit(mode);
            for(int i=0;i<cap;i++)spawn.invoke(view);
            assertEquals(cap,enemies.size());
            waiting.setBoolean(view,false);
            first.setBoolean(view,false);
            timer.setFloat(view,0f);
            remaining.setInt(view,3);
            update.invoke(view,.016f);
            assertEquals("Crowded stage must preserve queued enemies",3,
                    remaining.getInt(view));
            assertEquals(cap,enemies.size());
            enemies.remove(0);
            update.invoke(view,.016f);
            assertEquals("New slot must admit a queued enemy",2,
                    remaining.getInt(view));
            assertEquals(cap,enemies.size());
            view.releaseAudio();
        }
    }
}
