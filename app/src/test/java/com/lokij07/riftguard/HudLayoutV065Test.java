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

    @Test public void everyMovedButtonRemainsClickableOnlyAtItsNewPosition(){
        SharedPreferences p=RuntimeEnvironment.getApplication().getSharedPreferences(
                "hud_all_moved_v065",Context.MODE_PRIVATE);
        p.edit().clear().commit();
        HudLayoutV065 h=new HudLayoutV065(p);
        int[] controls={HudLayoutV065.MOVE,HudLayoutV065.DODGE,
                HudLayoutV065.BUILD,HudLayoutV065.SKILL,HudLayoutV065.ATTACK};
        float[][] positions={{310,370},{585,355},{805,355},{1020,340},{1130,490}};
        for(int i=0;i<controls.length;i++){
            // Move one control at a time to an independent accessible space.
            // Other controls must still retain their original hit areas.
            h.move(controls[i],positions[i][0],positions[i][1]);
        }
        for(int i=0;i<controls.length;i++){
            assertEquals(controls[i],h.hitControl(
                    h.x(controls[i]),h.y(controls[i])));
        }
        h.save();
        HudLayoutV065 restored=new HudLayoutV065(p);
        for(int i=0;i<controls.length;i++){
            assertEquals(h.x(controls[i]),restored.x(controls[i]),.001f);
            assertEquals(h.y(controls[i]),restored.y(controls[i]),.001f);
            assertEquals(controls[i],restored.hitControl(
                    restored.x(controls[i]),restored.y(controls[i])));
        }
    }
    @Test public void overlappingOrWaveButtonPlacementsCannotHideControls(){
        SharedPreferences p=RuntimeEnvironment.getApplication().getSharedPreferences(
                "hud_no_overlaps_v065",Context.MODE_PRIVATE);
        p.edit().clear().commit();
        HudLayoutV065 h=new HudLayoutV065(p);
        float originalX=h.x(HudLayoutV065.ATTACK),originalY=h.y(HudLayoutV065.ATTACK);
        h.move(HudLayoutV065.ATTACK,h.x(HudLayoutV065.SKILL),
                h.y(HudLayoutV065.SKILL));
        assertEquals(originalX,h.x(HudLayoutV065.ATTACK),.001f);
        assertEquals(originalY,h.y(HudLayoutV065.ATTACK),.001f);
        h.move(HudLayoutV065.MOVE,500,220);
        assertTrue("Controls must stay clear of Next Wave shortcut",
                h.y(HudLayoutV065.MOVE)>=290f);
    }
    @Test public void editorResetCanBeCancelledWithoutErasingSavedLayout(){
        SharedPreferences p=RuntimeEnvironment.getApplication().getSharedPreferences(
                "hud_cancel_reset_v065",Context.MODE_PRIVATE);
        p.edit().clear().commit();
        HudLayoutV065 h=new HudLayoutV065(p);
        h.move(HudLayoutV065.ATTACK,1030,405);
        h.save();
        h.previewDefaults();
        assertEquals(1130,h.x(HudLayoutV065.ATTACK),.01);
        h.reload();
        assertEquals(1030,h.x(HudLayoutV065.ATTACK),.01);
    }

    @Test public void relocatedBuildSkillAndDodgeTriggerActualBattleActions()throws Exception{
        Context ctx=RuntimeEnvironment.getApplication();
        SharedPreferences saved=ctx.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE);
        saved.edit().clear().commit();
        RiftGameView game=new RiftGameView(ctx);
        game.layout(0,0,1280,720);
        Method start=RiftGameView.class.getDeclaredMethod("start",int.class);
        start.setAccessible(true);start.invoke(game,0);
        HudLayoutV065 hud=(HudLayoutV065)field(game,"hudLayout");
        hud.move(HudLayoutV065.BUILD,800,355);
        hud.move(HudLayoutV065.SKILL,1035,360);
        hud.move(HudLayoutV065.DODGE,585,355);
        field(game,"selectedTowerPad",0);
        field(game,"gold",999);
        int[] towers=(int[])field(game,"towers");
        int before=towers[0];
        tap(game,MotionEvent.ACTION_DOWN,849,589); // old BUILD location
        assertEquals("Old BUILD location must no longer upgrade",before,towers[0]);
        tap(game,MotionEvent.ACTION_DOWN,800,355); // moved BUILD
        assertEquals("Relocated BUILD must upgrade selected tower",before+1,towers[0]);
        assertEquals(0f,(Float)field(game,"pulseCooldown"),.0001f);
        tap(game,MotionEvent.ACTION_DOWN,1035,360); // moved SKILL
        assertTrue("Relocated SKILL must execute",((Float)field(game,"pulseCooldown"))>0f);
        tap(game,MotionEvent.ACTION_DOWN,585,355); // moved DODGE
        assertTrue("Relocated DODGE must execute",((Float)field(game,"dodgeTime"))>0f);
    }
}
