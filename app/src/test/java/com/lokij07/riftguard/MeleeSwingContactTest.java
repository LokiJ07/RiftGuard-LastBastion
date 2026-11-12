package com.lokij07.riftguard;

import android.content.Context;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import static org.junit.Assert.*;

/** Integration test for timed directional melee, not just animation pose math. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public final class MeleeSwingContactTest {
    private static Object value(Object o,String field)throws Exception{
        Field f=o.getClass().getDeclaredField(field);f.setAccessible(true);return f.get(o);
    }
    private static void set(Object o,String field,Object v)throws Exception{
        Field f=o.getClass().getDeclaredField(field);f.setAccessible(true);f.set(o,v);
    }
    private static void call(Object o,String method,Class<?>[] types,Object... args)
            throws Exception{
        Method m=o.getClass().getDeclaredMethod(method,types);
        m.setAccessible(true);m.invoke(o,args);
    }
    @Test public void swordHitConeRejectsMonstersBehindHero(){
        assertTrue(MeleeSwingRules.hits(100,100,0,118,185,100));
        assertFalse(MeleeSwingRules.hits(100,100,0,118,15,100));
        assertFalse(MeleeSwingRules.hits(100,100,0,118,250,100));
        assertTrue(MeleeSwingRules.hits(100,100,(float)Math.PI,118,15,100));
        assertFalse(MeleeSwingRules.hitWindow(.2f));
        assertTrue(MeleeSwingRules.hitWindow(.5f));
    }
    @Test public void eachSwordMidSwingPointsTowardSelectedEnemy(){
        float[] dirs={0f,(float)Math.PI/2f,(float)Math.PI,
                -(float)Math.PI/2f};
        for(int id:new int[]{1,2})for(float dir:dirs){
            WeaponRig.Pose pose=WeaponRig.aimedPose(id,.5f,-1f,1f,false,dir);
            WeaponRig.Point point=WeaponRig.tip(id,pose);
            boolean mirrored=Math.cos(dir)<-.28f;
            float dx=(mirrored?-point.x:point.x)-(mirrored?-pose.handX:pose.handX);
            float dy=point.y-pose.handY;
            float dot=dx*(float)Math.cos(dir)+dy*(float)Math.sin(dir);
            assertTrue("Sword must actually face target at contact",dot>34f);
        }
    }
    @Test public void inGameSwordOnlyDamagesEnemiesInFrontAtContactTime()
            throws Exception {
        Context context=RuntimeEnvironment.getApplication();
        context.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE)
                .edit().clear().commit();
        RiftGameView view=new RiftGameView(context);
        call(view,"start",new Class[]{int.class},0);
        call(view,"spawnEnemy",new Class[0]);
        call(view,"spawnEnemy",new Class[0]);
        @SuppressWarnings("unchecked")
        List<Object> enemies=(List<Object>)value(view,"enemies");
        Object front=enemies.get(0),back=enemies.get(1);
        float hx=(float)value(view,"heroX"),hy=(float)value(view,"heroY");
        set(front,"x",hx+65f);set(front,"y",hy);
        set(back,"x",hx-92f);set(back,"y",hy);
        set(front,"hp",500f);set(back,"hp",500f);
        set(view,"equippedWeapon",1);set(view,"shotTimer",-1f);
        call(view,"fire",new Class[0]);
        assertEquals(500f,(float)value(front,"hp"),.01f);
        assertEquals(500f,(float)value(back,"hp"),.01f);
        call(view,"update",new Class[]{float.class},.13f);
        assertTrue("Front enemy must be struck at blade contact",
                (float)value(front,"hp")<500f);
        assertEquals("Backward enemy must not take 360-degree sword damage",
                500f,(float)value(back,"hp"),.01f);
        float damaged=(float)value(front,"hp");
        call(view,"update",new Class[]{float.class},.02f);
        assertEquals("Same swing must not double-hit",damaged,
                (float)value(front,"hp"),.01f);
    }
}
