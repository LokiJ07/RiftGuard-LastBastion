package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.MotionEvent;
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
public final class HudLayoutV065Test {
    private static Object field(Object ob,String name)throws Exception{
        Field f=ob.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(ob);
    }
    private static void field(Object ob,String name,Object val)throws Exception{
        Field f=ob.getClass().getDeclaredField(name);f.setAccessible(true);f.set(ob,val);
    }
    private static void tap(RiftGameView v,int type,float x,float y){
        long at=android.os.SystemClock.uptimeMillis();
        MotionEvent ev=MotionEvent.obtain(at,at,type,x,y,0);
        v.onTouchEvent(ev);ev.recycle();
    }
    @Test public void allControlsUseSavedPositionsForTapsAndJoystick() {
        SharedPreferences p=RuntimeEnvironment.getApplication().getSharedPreferences("hud_layout_test_v065",Context.MODE_PRIVATE);
        p.edit().clear().commit();
        HudLayoutV065 hud=new HudLayoutV065(p);
        assertTrue(hud.hit(HudLayoutV065.ATTACK,1130,560,5));
        hud.move(HudLayoutV065.ATTACK,1040,400);
        assertTrue(hud.hit(HudLayoutV065.ATTACK,1040,400,5));
        assertFalse(hud.hit(HudLayoutV065.ATTACK,1130,560,5));
        hud.move(HudLayoutV065.MOVE,330,525);
        assertTrue(hud.hit(HudLayoutV065.MOVE,330,525,5));
        assertFalse(hud.hit(HudLayoutV065.MOVE,153,568,5));
        hud.save();
        HudLayoutV065 again=new HudLayoutV065(p);
        assertEquals(1040,again.x(HudLayoutV065.ATTACK),.01);
        assertEquals(400,again.y(HudLayoutV065.ATTACK),.01);
        assertEquals(330,again.x(HudLayoutV065.MOVE),.01);
        again.reset();
        assertEquals(1130,new HudLayoutV065(p).x(HudLayoutV065.ATTACK),.01);
    }
    @Test public void actualPauseEditorDragsAttackAndMovesPhysicalTapHitbox()throws Exception{
        Context ctx=RuntimeEnvironment.getApplication();
        ctx.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE).edit().clear().commit();
        RiftGameView v=new RiftGameView(ctx);
        v.layout(0,0,1280,720);
        Method start=RiftGameView.class.getDeclaredMethod("start",int.class);
        start.setAccessible(true);start.invoke(v,0);
        field(v,"paused",true);
        tap(v,MotionEvent.ACTION_DOWN,600,545);
        assertTrue((Boolean)field(v,"hudEditing"));
        tap(v,MotionEvent.ACTION_DOWN,1130,560);
        tap(v,MotionEvent.ACTION_MOVE,1030,405);
        tap(v,MotionEvent.ACTION_UP,1030,405);
        HudLayoutV065 h=(HudLayoutV065)field(v,"hudLayout");
        assertEquals(1030,h.x(HudLayoutV065.ATTACK),.01);
        assertEquals(405,h.y(HudLayoutV065.ATTACK),.01);
        tap(v,MotionEvent.ACTION_DOWN,760,65); // save
        assertFalse((Boolean)field(v,"hudEditing"));
        tap(v,MotionEvent.ACTION_DOWN,600,280); // resume
        assertFalse((Boolean)field(v,"paused"));
        tap(v,MotionEvent.ACTION_DOWN,1130,560);
        assertEquals(-1,field(v,"attackPointer"));
        tap(v,MotionEvent.ACTION_DOWN,1030,405);
        assertEquals(0,field(v,"attackPointer"));
        assertTrue((Boolean)field(v,"attackGesture"));
        tap(v,MotionEvent.ACTION_UP,1030,405);
        assertEquals(-1,field(v,"attackPointer"));
        HudLayoutV065 reload=new HudLayoutV065(ctx.getSharedPreferences(
                "riftguard_campaign_v1",Context.MODE_PRIVATE));
        assertEquals(1030,reload.x(HudLayoutV065.ATTACK),.01);
    }
    @Test public void hudEditorCancelRevertsUnsavedPositionsAndResetWorks()throws Exception{
        Context ctx=RuntimeEnvironment.getApplication();
        ctx.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE).edit().clear().commit();
        RiftGameView v=new RiftGameView(ctx);v.layout(0,0,1280,720);
        Method start=RiftGameView.class.getDeclaredMethod("start",int.class);
        start.setAccessible(true);start.invoke(v,0);
        field(v,"paused",true);
        tap(v,MotionEvent.ACTION_DOWN,600,545);
        tap(v,MotionEvent.ACTION_DOWN,153,568);
        tap(v,MotionEvent.ACTION_MOVE,450,485);
        tap(v,MotionEvent.ACTION_UP,450,485);
        HudLayoutV065 h=(HudLayoutV065)field(v,"hudLayout");
        assertEquals(450,h.x(HudLayoutV065.MOVE),.1);
        tap(v,MotionEvent.ACTION_DOWN,1100,65); // cancel
        assertEquals(153,h.x(HudLayoutV065.MOVE),.1);
        assertFalse((Boolean)field(v,"hudEditing"));
    }
}
