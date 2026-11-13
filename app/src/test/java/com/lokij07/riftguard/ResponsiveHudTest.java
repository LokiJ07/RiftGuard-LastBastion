package com.lokij07.riftguard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.view.MotionEvent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class ResponsiveHudTest {
    private RiftGameView game() throws Exception {
        Context ctx=RuntimeEnvironment.getApplication();
        ctx.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE)
                .edit().clear().putInt("fortress_rank",3).commit();
        RiftGameView view=new RiftGameView(ctx);
        view.layout(0,0,1780,720);
        Method start=RiftGameView.class.getDeclaredMethod("start",int.class);
        start.setAccessible(true);start.invoke(view,0);
        return view;
    }
    private static float number(Object obj,String name)throws Exception{
        Field f=obj.getClass().getDeclaredField(name);f.setAccessible(true);
        return f.getFloat(obj);
    }
    private static Object value(Object obj,String name)throws Exception{
        Field f=obj.getClass().getDeclaredField(name);f.setAccessible(true);
        return f.get(obj);
    }
    private void down(RiftGameView view,float x,float y){
        long t=SystemClock.uptimeMillis();
        MotionEvent e=MotionEvent.obtain(t,t,MotionEvent.ACTION_DOWN,x,y,0);
        view.onTouchEvent(e);e.recycle();
    }
    private void up(RiftGameView view,float x,float y){
        long t=SystemClock.uptimeMillis();
        MotionEvent e=MotionEvent.obtain(t,t+30,MotionEvent.ACTION_UP,x,y,0);
        view.onTouchEvent(e);e.recycle();
    }
    private void tap(RiftGameView v,float x,float y){down(v,x,y);up(v,x,y);}
    @Test public void ultraWideJoystickAndActionHitboxesMatchActualDrawnControls()
            throws Exception {
        RiftGameView view=game();
        assertEquals(153f,numberInvoke(view,"stickHudX"),.001f);
        assertEquals(1130f,numberInvoke(view,"attackHudX"),.001f);
        float before=number(view,"heroX");
        down(view,495,568); // virtual MOVE 153 + 250 gutter + 92px joystick travel
        assertTrue(number(view,"axisX")>.9f);
        Method update=RiftGameView.class.getDeclaredMethod("update",float.class);
        update.setAccessible(true);update.invoke(view,.12f);
        assertTrue(number(view,"heroX")>before+20f);
        up(view,495,568);
        assertEquals(0f,number(view,"axisX"),.001f);
        down(view,1380,560); // virtual ATTACK 1130 + 250px gutter
        assertTrue((Boolean)value(view,"firing"));
        up(view,1380,560);
        assertFalse((Boolean)value(view,"firing"));
        int[] towers=(int[])value(view,"towers");
        int beforeBuild=towers[1]+towers[2]+towers[3];
        tap(view,1099,589); // virtual BUILD 849 + 250px gutter
        assertTrue(towers[1]+towers[2]+towers[3]>beforeBuild);
        tap(view,1224,593); // virtual SKILL 974 + 250px gutter
        assertTrue(number(view,"pulseCooldown")>0);
    }
    @Test public void ultraWideRenderShowsRealControlsOnBothSides() throws Exception {
        RiftGameView view=game();
        Bitmap bitmap=Bitmap.createBitmap(1780,720,Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(bitmap));
        assertTrue(android.graphics.Color.alpha(bitmap.getPixel(403,568))>0);
        assertTrue(android.graphics.Color.alpha(bitmap.getPixel(1380,560))>0);
        bitmap.recycle();
        view.layout(0,0,1280,720);
        assertEquals(153f,numberInvoke(view,"stickHudX"),.001f);
        assertEquals(1130f,numberInvoke(view,"attackHudX"),.001f);
    }
    private static float numberInvoke(RiftGameView view,String method)throws Exception{
        Method m=RiftGameView.class.getDeclaredMethod(method);
        m.setAccessible(true);return (Float)m.invoke(view);
    }
}
