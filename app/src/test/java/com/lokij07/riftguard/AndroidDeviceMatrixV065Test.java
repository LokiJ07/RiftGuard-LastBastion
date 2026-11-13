package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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

/** The public build must remain playable on more than one handset shape. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class AndroidDeviceMatrixV065Test {
    private static Object field(RiftGameView v,String n)throws Exception{
        Field f=RiftGameView.class.getDeclaredField(n);
        f.setAccessible(true);return f.get(v);
    }
    private static float[] physical(int w,int h,float x,float y){
        float scale=Math.min(w/1280f,h/720f);
        return new float[]{(w-1280*scale)*.5f+x*scale,
                (h-720*scale)*.5f+y*scale};
    }
    private static void tap(RiftGameView v,float x,float y){
        long t=SystemClock.uptimeMillis();
        MotionEvent down=MotionEvent.obtain(t,t,MotionEvent.ACTION_DOWN,x,y,0);
        v.onTouchEvent(down);down.recycle();
        MotionEvent up=MotionEvent.obtain(t,t+25,MotionEvent.ACTION_UP,x,y,0);
        v.onTouchEvent(up);up.recycle();
    }
    @Test public void fourScreenShapesKeepHomeAndBattleTouchTargetsReachable()
            throws Exception{
        int[][] devices={{960,720},{1280,800},{1280,720},{2520,1080}};
        for(int[] device:devices){
            Context context=RuntimeEnvironment.getApplication();
            context.getSharedPreferences("riftguard_campaign_v1",
                    Context.MODE_PRIVATE).edit().clear().commit();
            int w=device[0],h=device[1];
            RiftGameView v=new RiftGameView(context);
            v.layout(0,0,w,h);
            Bitmap home=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
            v.draw(new Canvas(home));
            assertTrue("Rendered home must have a visible center",
                    android.graphics.Color.alpha(home.getPixel(w/2,h/2))>0);
            home.recycle();
            float[] campaign=physical(w,h,230,451);
            tap(v,campaign[0],campaign[1]);
            assertEquals("Campaign touch mismatch at "+w+"x"+h,
                    "MAP",field(v,"scene").toString());
            assertTrue(v.handleBack());
            assertEquals("HOME",field(v,"scene").toString());
            Method start=RiftGameView.class.getDeclaredMethod("start",int.class);
            start.setAccessible(true);start.invoke(v,0);
            float[] attack=physical(w,h,1130,560);
            // The exact same virtual HUD transform is used by drawing/input.
            long time=SystemClock.uptimeMillis();
            MotionEvent press=MotionEvent.obtain(time,time,
                    MotionEvent.ACTION_DOWN,attack[0],attack[1],0);
            v.onTouchEvent(press);press.recycle();
            assertTrue("Attack must be reachable at "+w+"x"+h,
                    (Boolean)field(v,"firing"));
            MotionEvent release=MotionEvent.obtain(time,time+10,
                    MotionEvent.ACTION_UP,attack[0],attack[1],0);
            v.onTouchEvent(release);release.recycle();
            assertFalse((Boolean)field(v,"firing"));
            v.trimGraphicCaches(true);
            assertEquals("PLAY",field(v,"scene").toString());
            v.releaseAudio();
        }
    }
    @Test public void lowMemoryTrimPreservesSavedCampaignAndCombatSession()
            throws Exception{
        Context ctx=RuntimeEnvironment.getApplication();
        SharedPreferences prefs=ctx.getSharedPreferences("riftguard_campaign_v1",
                Context.MODE_PRIVATE);
        prefs.edit().clear().putInt("supplies",225)
                .putInt("fortress_rank",3).putInt("unlocked",10).commit();
        RiftGameView view=new RiftGameView(ctx);
        view.layout(0,0,1280,720);
        Method start=RiftGameView.class.getDeclaredMethod("start",int.class);
        start.setAccessible(true);start.invoke(view,0);
        view.trimGraphicCaches(false);
        view.trimGraphicCaches(true);
        assertEquals("PLAY",field(view,"scene").toString());
        assertEquals(225,prefs.getInt("supplies",-1));
        assertEquals(3,prefs.getInt("fortress_rank",-1));
        assertEquals(10,prefs.getInt("unlocked",-1));
        view.releaseAudio();
    }
}
