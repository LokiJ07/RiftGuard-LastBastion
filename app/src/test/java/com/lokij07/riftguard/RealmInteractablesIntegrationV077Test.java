package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
import java.lang.reflect.Constructor;
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
public class RealmInteractablesIntegrationV077Test {
    private static Object get(Object instance,String name)throws Exception{
        Field f=instance.getClass().getDeclaredField(name);
        f.setAccessible(true);return f.get(instance);
    }
    private static void set(Object instance,String name,Object value)throws Exception{
        Field f=instance.getClass().getDeclaredField(name);
        f.setAccessible(true);f.set(instance,value);
    }
    private static Object call(Object instance,String name,Class<?>[] args,Object...values)
            throws Exception{
        Method m=instance.getClass().getDeclaredMethod(name,args);
        m.setAccessible(true);return m.invoke(instance,values);
    }
    private static void tick(RiftGameView g,float dt)throws Exception{
        call(g,"update",new Class<?>[]{float.class},dt);
    }
    private static RiftGameView game(int stage)throws Exception{
        Context c=RuntimeEnvironment.getApplication();
        SharedPreferences p=c.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE);
        assertTrue(p.edit().clear().putInt("unlocked",stage).
                putInt("fortress_rank",5).commit());
        RiftGameView g=new RiftGameView(c);g.layout(0,0,1280,720);
        call(g,"start",new Class<?>[]{int.class},stage);
        set(g,"wave",2);set(g,"firstWavePending",false);
        set(g,"waiting",false);set(g,"remaining",1);set(g,"spawnTimer",999f);
        set(g,"squadPlan",SquadTacticsV065.SHIELD_WALL);
        call(g,"resetSiegeMission",new Class<?>[0]);
        return g;
    }
    private static Object enemy(int kind,float x,float y,float hp)throws Exception{
        Class<?> type=Class.forName("com.lokij07.riftguard.RiftGameView$Enemy");
        Constructor<?> ctor=type.getDeclaredConstructor(float.class,float.class,
                int.class,float.class,float.class);
        ctor.setAccessible(true);
        Object e=ctor.newInstance(x,y,kind,hp,0f);
        set(e,"squadRole",SquadTacticsV065.role(kind,false,1));
        return e;
    }
    @SuppressWarnings("unchecked")
    private static List<Object> enemies(RiftGameView g)throws Exception{
        return (List<Object>)get(g,"enemies");
    }
    private static void channelSite(RiftGameView g)throws Exception{
        set(g,"heroX",(float)get(g,"realmSiteX"));
        set(g,"heroY",(float)get(g,"realmSiteY"));
        tick(g,.1f);
        assertFalse("not an instant power-up",(boolean)get(g,"realmSiteUsed"));
        for(int i=0;i<17;i++)tick(g,.1f);
        assertTrue("real hero proximity channels the world site",
                (boolean)get(g,"realmSiteUsed"));
    }
    @Test public void wardstoneActuallyReducesEnemyCoreStrike()throws Exception{
        RiftGameView g=game(40);
        assertEquals(RealmInteractablesV077.WARD,RealmInteractablesV077.type(40));
        channelSite(g);
        float effect=(float)get(g,"realmWardTime");
        assertTrue(effect>0&&effect<=RealmInteractablesV077.WARD_SECONDS);
        // Step away to prevent hero aggro from diverting the enemy.
        set(g,"heroX",LaneRules.HERO_MAX_X);
        set(g,"heroY",LaneRules.HERO_MAX_Y);
        float before=(float)get(g,"baseHp");
        enemies(g).add(enemy(0,1108f,510f,3000f));
        tick(g,.08f);
        float lost=before-(float)get(g,"baseHp");
        assertTrue("enemy core strike should occur",lost>0);
        assertEquals(RealmInteractablesV077.coreDamage(12f,effect),
                lost,.02f);
    }
    @Test public void fieldForgeHealsGenuinelyDamagedTowerAndBuffsDamage()
            throws Exception{
        RiftGameView g=game(33);
        assertEquals(RealmInteractablesV077.FORGE,RealmInteractablesV077.type(33));
        int[] tier=(int[])get(g,"towers");
        float[] hp=(float[])get(g,"towerHp");
        assertTrue(tier[1]>0);
        float max=TowerDurability.maxHp(tier[1],5);
        hp[1]=max*.25f;
        float before=hp[1];
        channelSite(g);
        assertEquals(RealmInteractablesV077.repair(before,max),
                hp[1],.001f);
        assertTrue((float)get(g,"realmForgeTime")>0);
        assertTrue(RealmInteractablesV077.turretDamage(100f,
                (float)get(g,"realmForgeTime"))>100f);
    }
    @Test public void purifierReallyDisablesAnActiveHazardUntilExpired()
            throws Exception{
        RiftGameView g=game(31);
        assertEquals(RealmInteractablesV077.PURIFIER,RealmInteractablesV077.type(31));
        channelSite(g);
        assertTrue((float)get(g,"realmPurifierTime")>0);
        set(g,"heroX",StageHazards.x(31,0));
        set(g,"heroY",StageHazards.y(31,0));
        float[] timers=(float[])get(g,"hazardTimers");
        timers[0]=0f;
        float hp=(float)get(g,"heroHp");
        call(g,"updateStageHazards",new Class<?>[]{float.class},.1f);
        assertEquals("hazard must be suppressed in real battle simulation",
                hp,(float)get(g,"heroHp"),.001f);
        set(g,"realmPurifierTime",0f);
        timers[0]=0f;
        set(g,"gameTime",0f);
        call(g,"updateStageHazards",new Class<?>[]{float.class},.1f);
        assertTrue("real hazard resumes after purifier ends",
                (float)get(g,"heroHp")<hp);
    }
    @Test public void waveBoundaryResetsTheOptionalPowerWithoutNewSaveFields()
            throws Exception{
        RiftGameView g=game(40);
        channelSite(g);
        assertTrue((boolean)get(g,"realmSiteUsed"));
        call(g,"resetSiegeMission",new Class<?>[0]);
        assertFalse((boolean)get(g,"realmSiteUsed"));
        assertEquals(0f,(float)get(g,"realmWardTime"),.0001f);
        assertEquals(0f,(float)get(g,"realmSiteChannel"),.0001f);
    }
}
