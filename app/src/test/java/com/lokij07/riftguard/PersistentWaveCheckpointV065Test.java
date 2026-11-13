package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.view.MotionEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import static org.junit.Assert.*;

/**
 * Process-death test creates two actual game views sharing Android preferences;
 * no false claim of restoring transient in-flight damage/projectiles.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class PersistentWaveCheckpointV065Test {
    private static SharedPreferences reset(){
        SharedPreferences prefs=RuntimeEnvironment.getApplication()
                .getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        return prefs;
    }
    private static Object get(RiftGameView view,String name)throws Exception{
        Field f=RiftGameView.class.getDeclaredField(name);
        f.setAccessible(true);return f.get(view);
    }
    private static void run(RiftGameView view,String name,Class<?>[] types,
                            Object...args)throws Exception{
        Method m=RiftGameView.class.getDeclaredMethod(name,types);
        m.setAccessible(true);m.invoke(view,args);
    }
    private static RiftGameView game(){
        RiftGameView view=new RiftGameView(RuntimeEnvironment.getApplication());
        view.layout(0,0,1280,720);
        return view;
    }
    private static void tap(RiftGameView view,int x,int y){
        long t=SystemClock.uptimeMillis();
        MotionEvent e=MotionEvent.obtain(t,t,MotionEvent.ACTION_DOWN,x,y,0);
        view.onTouchEvent(e);e.recycle();
        MotionEvent up=MotionEvent.obtain(t,t+25,MotionEvent.ACTION_UP,x,y,0);
        view.onTouchEvent(up);up.recycle();
    }

    @Test public void checkpointSurvivesFreshPreferencesConsumer(){
        SharedPreferences p=reset();
        PersistentWaveCheckpointV065 store=new PersistentWaveCheckpointV065(p);
        int[] tiers={1,2,3,0,0,0,0,0};
        int[] types={0,1,2,3,0,1,2,3};
        int[] ruins=new int[8];float[] hp={165,240,315,0,0,0,0,0};
        WaveCheckpointV065.Snapshot src=WaveCheckpointV065.capture(
                38,3,245,29,4,0,1,6,true,79f,450f,0f,
                1030f,514f,tiers,types,ruins,hp);
        assertTrue(store.save(src,false,DifficultyRules.NORMAL,0));
        tiers[0]=0;hp[1]=0; // Stored data is a real defensive copy.
        PersistentWaveCheckpointV065.Recovery recovered=
                new PersistentWaveCheckpointV065(p).load(38);
        assertNotNull(recovered);
        assertEquals(38,recovered.snapshot.stage);
        assertEquals(3,recovered.snapshot.wave);
        assertEquals(245,recovered.snapshot.gold());
        assertEquals(2,recovered.snapshot.towerTier[1]);
        assertEquals(240f,recovered.snapshot.towerHealth[1],.01f);
        assertEquals(79f,recovered.snapshot.heroHp,.01f);
        assertFalse(recovered.firstWavePending);
        assertEquals(DifficultyRules.NORMAL,recovered.difficulty);
    }
    @Test public void truncatedInvalidAndLockedSavesAreRejected(){
        SharedPreferences p=reset();
        PersistentWaveCheckpointV065 store=new PersistentWaveCheckpointV065(p);
        p.edit().putString("battle.v065.recovery","{ broken json").commit();
        assertNull(store.load(299));
        assertFalse(p.contains("battle.v065.recovery"));
        int[] zeros=new int[8];float[] hp=new float[8];
        WaveCheckpointV065.Snapshot snap=WaveCheckpointV065.capture(
                93,1,50,0,0,0,0,0,false,100,250,0,
                900,450,zeros,zeros,zeros,hp);
        assertTrue(store.save(snap,true,DifficultyRules.NORMAL,0));
        assertNull("Stage beyond campaign unlock cannot be resumed",
                store.load(0));
        assertFalse(p.contains("battle.v065.recovery"));
    }
    @Test public void realHomeContinueRestoresLiveWaveAndCanAbandon()
            throws Exception{
        reset();
        RiftGameView playing=game();
        run(playing,"start",new Class<?>[]{int.class},0);
        assertEquals("PLAY",get(playing,"scene").toString());
        // A started wave is a committed boundary. No need to save per draw.
        run(playing,"advanceWave",new Class<?>[]{boolean.class},false);
        PersistentWaveCheckpointV065.Recovery before=
                new PersistentWaveCheckpointV065(resetlessPrefs()).load(0);
        assertNotNull(before);
        assertFalse(before.firstWavePending);
        assertEquals(1,before.snapshot.wave);
        // New Activity/View using only the saved preferences (process restart).
        RiftGameView reopened=game();
        assertEquals("HOME",get(reopened,"scene").toString());
        assertNotNull(get(reopened,"savedWave"));
        tap(reopened,950,338);
        assertEquals("PLAY",get(reopened,"scene").toString());
        assertTrue((Boolean)get(reopened,"paused"));
        assertEquals(before.snapshot.wave,((Integer)get(reopened,"wave")).intValue());
        assertEquals(before.snapshot.gold,((Integer)get(reopened,"gold")).intValue());
        assertEquals(before.snapshot.heroHp,(Float)get(reopened,"heroHp"),.01f);
        // Deliberately leaving the paused run must remove resume affordance.
        tap(reopened,640,369);
        assertEquals("MAP",get(reopened,"scene").toString());
        assertNull(new PersistentWaveCheckpointV065(resetlessPrefs()).load(0));
        playing.releaseAudio();reopened.releaseAudio();
    }
    private static SharedPreferences resetlessPrefs(){
        return RuntimeEnvironment.getApplication()
                .getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE);
    }
    @Test public void victoriesAndRecordedDefeatsCannotBeReplayedAsSave()
            throws Exception{
        SharedPreferences prefs=reset();
        RiftGameView game=game();
        run(game,"start",new Class<?>[]{int.class},0);
        assertNotNull(new PersistentWaveCheckpointV065(prefs).load(0));
        run(game,"complete",new Class<?>[]{});
        assertNull("Clear wave recovery before permanent reward is issued",
                new PersistentWaveCheckpointV065(prefs).load(1));
        int total=prefs.getInt("supplies",-1);
        assertTrue(total>0);
        RiftGameView another=game();
        assertNull(get(another,"savedWave"));
        assertEquals(total,prefs.getInt("supplies",-1));
        run(another,"start",new Class<?>[]{int.class},0);
        assertNotNull(new PersistentWaveCheckpointV065(prefs).load(1));
        run(another,"markDefeat",new Class<?>[]{});
        assertNull(new PersistentWaveCheckpointV065(prefs).load(1));
        another.releaseAudio();game.releaseAudio();
    }
}
