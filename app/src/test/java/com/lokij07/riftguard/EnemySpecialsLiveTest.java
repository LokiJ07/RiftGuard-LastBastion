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

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public final class EnemySpecialsLiveTest {
    private static Object field(Object o,String name)throws Exception{
        Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);
        return f.get(o);
    }
    private static void set(Object o,String name,Object value)throws Exception{
        Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);
        f.set(o,value);
    }
    private static void invoke(Object o,String method,Class<?>[] args,Object...params)
            throws Exception{
        Method m=o.getClass().getDeclaredMethod(method,args);m.setAccessible(true);
        m.invoke(o,params);
    }
    private static RiftGameView game()throws Exception{
        Context ctx=RuntimeEnvironment.getApplication();
        ctx.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE)
                .edit().clear().commit();
        RiftGameView view=new RiftGameView(ctx);
        invoke(view,"start",new Class[]{int.class},0);return view;
    }
    private static Object spawn(RiftGameView game,int kind,float x,float y)throws Exception{
        invoke(game,"spawnEnemy",new Class[0]);
        @SuppressWarnings("unchecked")
        List<Object> e=(List<Object>)field(game,"enemies");
        Object enemy=e.get(e.size()-1);
        set(enemy,"kind",kind);set(enemy,"x",x);set(enemy,"y",y);
        set(enemy,"hp",2000f);set(enemy,"maxHp",2000f);
        return enemy;
    }
    @Test public void enemyAbilitiesHaveExplicitDistanceAndCooldownGates(){
        assertTrue(EnemySpecials.canBoarCharge(6,180,0));
        assertFalse(EnemySpecials.canBoarCharge(6,360,0));
        assertFalse(EnemySpecials.canBoarCharge(6,180,4));
        assertFalse(EnemySpecials.canBoarCharge(2,180,0));
        assertTrue(EnemySpecials.canShamanCast(4,220,0));
        assertFalse(EnemySpecials.canShamanCast(4,450,0));
        assertEquals(2.4f,EnemySpecials.mobilityMultiplier(0,.2f),.001f);
        assertEquals(.34f,EnemySpecials.mobilityMultiplier(.2f,0),.001f);
        assertTrue(EnemySpecials.boltConnects(100,100,145,100));
        assertFalse(EnemySpecials.boltConnects(100,100,165,100));
    }
    @Test public void shamanCreatesDodgeableLiveProjectileAndDamagesOnImpact()
            throws Exception {
        RiftGameView view=game();
        float x=(float)field(view,"heroX"),y=(float)field(view,"heroY");
        Object shaman=spawn(view,4,x+220,y);
        set(shaman,"shamanCooldown",0f);
        invoke(view,"update",new Class[]{float.class},.03f);
        @SuppressWarnings("unchecked")
        List<Object> bolts=(List<Object>)field(view,"hostileBolts");
        assertEquals("Shaman must create a real flying bolt",1,bolts.size());
        float before=(float)field(view,"heroHp");
        invoke(view,"update",new Class[]{float.class},.75f);
        invoke(view,"update",new Class[]{float.class},.05f);
        assertEquals("Projectile must expire after target impact",0,bolts.size());
        assertTrue("Stationary hero must take shaman bolt damage",
                (float)field(view,"heroHp")<before);
    }
    @Test public void boarGivesTelegraphBeforeHighSpeedCharge()throws Exception{
        RiftGameView view=game();
        float x=(float)field(view,"heroX"),y=(float)field(view,"heroY");
        Object boar=spawn(view,6,x+235,y);
        set(boar,"dashCooldown",0f);
        invoke(view,"update",new Class[]{float.class},.08f);
        assertTrue("Boar must show a charge warning first",
                (float)field(boar,"dashWindup")>.1f);
        assertEquals(0f,(float)field(boar,"dashTime"),.001f);
        invoke(view,"update",new Class[]{float.class},.52f);
        assertTrue("Boar must then enter high-speed pursuit",
                (float)field(boar,"dashTime")>0);
    }
}
