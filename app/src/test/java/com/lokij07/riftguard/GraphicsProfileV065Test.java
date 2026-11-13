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

/** Real menu integration and frame pacing guardrails for TECNO POVA 5. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class GraphicsProfileV065Test {
    private static Object read(Object obj,String name)throws Exception{
        Field f=RiftGameView.class.getDeclaredField(name);
        f.setAccessible(true);return f.get(obj);
    }
    private static void press(RiftGameView view,float x,float y){
        long t=SystemClock.uptimeMillis();
        MotionEvent down=MotionEvent.obtain(t,t,MotionEvent.ACTION_DOWN,x,y,0);
        view.onTouchEvent(down);down.recycle();
    }
    @Test public void tecnoPovaFiveGetsHighArtWithSixtyFrameTarget(){
        assertEquals(GraphicsProfileV065.HIGH,
                GraphicsProfileV065.recommended("TECNO","TECNO LH7n"));
        assertEquals(GraphicsProfileV065.HIGH,
                GraphicsProfileV065.recommended("Tecno","POVA 5"));
        assertEquals(GraphicsProfileV065.BALANCED,
                GraphicsProfileV065.recommended("Unknown","generic-phone"));
        assertEquals(60,GraphicsProfileV065.targetFps(GraphicsProfileV065.HIGH));
        assertTrue(GraphicsProfileV065.nextFrameDelayMs(GraphicsProfileV065.HIGH,120f)>0);
        assertEquals(0,GraphicsProfileV065.nextFrameDelayMs(
                GraphicsProfileV065.HIGH,60f));
        assertTrue(GraphicsProfileV065.nextFrameDelayMs(
                GraphicsProfileV065.HIGH,120f)<20);
        assertFalse(GraphicsProfileV065.reduceAmbience(
                GraphicsProfileV065.HIGH,false));
        assertTrue(GraphicsProfileV065.reduceAmbience(
                GraphicsProfileV065.HIGH,true));
        assertTrue(GraphicsProfileV065.reduceAmbience(
                GraphicsProfileV065.PERFORMANCE,false));
    }
    @Test public void homeGraphicsButtonDoesNotToggleSoundAndPersists()
            throws Exception{
        Context context=RuntimeEnvironment.getApplication();
        SharedPreferences prefs=context.getSharedPreferences(
                "riftguard_campaign_v1",Context.MODE_PRIVATE);
        prefs.edit().clear().putInt(GraphicsProfileV065.SAVE_KEY,
                GraphicsProfileV065.HIGH).commit();
        RiftGameView view=new RiftGameView(context);
        view.layout(0,0,1280,720);
        assertEquals(GraphicsProfileV065.HIGH,read(view,"graphicsMode"));
        press(view,860,70); // added graphics button
        assertEquals(GraphicsProfileV065.BALANCED,read(view,"graphicsMode"));
        assertEquals(GraphicsProfileV065.BALANCED,
                prefs.getInt(GraphicsProfileV065.SAVE_KEY,-1));
        press(view,860,70);
        assertEquals(GraphicsProfileV065.PERFORMANCE,read(view,"graphicsMode"));
        press(view,860,70);
        assertEquals(GraphicsProfileV065.HIGH,read(view,"graphicsMode"));
        RiftGameView reload=new RiftGameView(context);
        assertEquals(GraphicsProfileV065.HIGH,read(reload,"graphicsMode"));
        assertEquals("HOME",read(view,"scene").toString());
    }
    @Test public void pausedGraphicsButtonWorksWithoutLeavingCombat()
            throws Exception{
        Context context=RuntimeEnvironment.getApplication();
        SharedPreferences prefs=context.getSharedPreferences(
                "riftguard_campaign_v1",Context.MODE_PRIVATE);
        prefs.edit().clear().putInt(GraphicsProfileV065.SAVE_KEY,
                GraphicsProfileV065.BALANCED).commit();
        RiftGameView view=new RiftGameView(context);
        view.layout(0,0,1280,720);
        Method start=RiftGameView.class.getDeclaredMethod("start",int.class);
        start.setAccessible(true);start.invoke(view,0);
        assertEquals("PLAY",read(view,"scene").toString());
        assertTrue(view.handleBack()); // pause; no mission exit
        assertTrue((Boolean)read(view,"paused"));
        press(view,640,622);
        assertEquals(GraphicsProfileV065.PERFORMANCE,read(view,"graphicsMode"));
        assertTrue((Boolean)read(view,"paused"));
        assertEquals("PLAY",read(view,"scene").toString());
        press(view,640,278); // existing Resume area unchanged
        assertFalse((Boolean)read(view,"paused"));
        assertEquals(GraphicsProfileV065.PERFORMANCE,
                prefs.getInt(GraphicsProfileV065.SAVE_KEY,-1));
    }
}
