package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.view.MotionEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

/** Accessible native story skip: no Android BACK key or hidden QA bypass. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public final class StorySkipTouchV067Test {
    private static Object field(Object object,String name)throws Exception{
        Field f=object.getClass().getDeclaredField(name);
        f.setAccessible(true);return f.get(object);
    }
    private static void open(RiftGameView view,StoryScript.Line[] lines)
            throws Exception{
        Method method=RiftGameView.class.getDeclaredMethod(
                "openStory",StoryScript.Line[].class);
        method.setAccessible(true);method.invoke(view,(Object)lines);
    }
    private static void tap(RiftGameView view,float x,float y){
        long time=SystemClock.uptimeMillis();
        MotionEvent down=MotionEvent.obtain(time,time,MotionEvent.ACTION_DOWN,x,y,0);
        view.onTouchEvent(down);down.recycle();
        MotionEvent up=MotionEvent.obtain(time,time+24,MotionEvent.ACTION_UP,x,y,0);
        view.onTouchEvent(up);up.recycle();
    }
    @Test public void skipButtonClearsQueuedPrologueAndWorldChapterWithoutDeletingSaves()
            throws Exception{
        Context ctx=RuntimeEnvironment.getApplication();
        SharedPreferences prefs=ctx.getSharedPreferences("riftguard_campaign_v1",
                Context.MODE_PRIVATE);
        prefs.edit().clear().putInt("supplies",333).commit();
        RiftGameView view=new RiftGameView(ctx);
        view.layout(0,0,1280,720);
        Method start=RiftGameView.class.getDeclaredMethod("start",int.class);
        start.setAccessible(true);start.invoke(view,0);
        open(view,StoryScript.prologue("WARDEN"));
        open(view,StoryScript.worldOpening(0,"WARDEN"));
        assertTrue((Boolean)field(view,"storyOpen"));
        @SuppressWarnings("unchecked")
        List<StoryScript.Line[]> queued=(List<StoryScript.Line[]>)
                field(view,"pendingStory");
        assertEquals(1,queued.size());
        tap(view,640,360); // Ordinary tap advances one line, does NOT skip.
        assertTrue((Boolean)field(view,"storyOpen"));
        assertEquals(1,field(view,"storyIndex"));
        tap(view,1130,76); // A visible top-right SKIP CHAPTER button.
        assertFalse((Boolean)field(view,"storyOpen"));
        assertEquals(0,queued.size());
        assertEquals("PLAY",field(view,"scene").toString());
        assertEquals(333,prefs.getInt("supplies",-1));
        assertNotNull(new PersistentWaveCheckpointV065(prefs).load(0));
        view.releaseAudio();
    }
    @Test public void skipHitboxTracksVirtualCanvasOnUltrawideScreens()
            throws Exception{
        Context ctx=RuntimeEnvironment.getApplication();
        ctx.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE)
                .edit().clear().commit();
        RiftGameView view=new RiftGameView(ctx);
        view.layout(0,0,1780,720); // 250 px gutters on each side
        Method start=RiftGameView.class.getDeclaredMethod("start",int.class);
        start.setAccessible(true);start.invoke(view,0);
        open(view,StoryScript.worldOpening(0,"WARDEN"));
        assertTrue((Boolean)field(view,"storyOpen"));
        tap(view,1380,76); // (1130,76) in centered 1280x720 coordinates
        assertFalse((Boolean)field(view,"storyOpen"));
        view.releaseAudio();
    }
}
