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
public class EnemyCoordinationIntegrationV075Test {
    private static Object get(Object o,String name)throws Exception{
        Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);
    }
    private static void set(Object o,String name,Object value)throws Exception{
        Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);f.set(o,value);
    }
    private static void call(Object o,String name,Class<?>[] signature,Object... args)throws Exception{
        Method m=o.getClass().getDeclaredMethod(name,signature);
        m.setAccessible(true);m.invoke(o,args);
    }
    private static void call(Object o,String name)throws Exception{call(o,name,new Class<?>[0]);}
    private static void tick(RiftGameView g,float step)throws Exception{
        call(g,"update",new Class<?>[]{float.class},step);
    }
    private static RiftGameView game(int stage,int plan)throws Exception{
        Context c=RuntimeEnvironment.getApplication();
        SharedPreferences p=c.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE);
        assertTrue(p.edit().clear().putInt("unlocked",stage).
                putInt("fortress_rank",5).commit());
        RiftGameView g=new RiftGameView(c);g.layout(0,0,1280,720);
        call(g,"start",new Class<?>[]{int.class},stage);
        set(g,"wave",3);set(g,"firstWavePending",false);
        set(g,"waiting",false);set(g,"remaining",0);set(g,"spawnTimer",999f);
        set(g,"squadPlan",plan);return g;
    }
    private static Object enemy(int kind,float x,float y,float hp)throws Exception{
        Class<?> type=Class.forName("com.lokij07.riftguard.RiftGameView$Enemy");
        Constructor<?> ctor=type.getDeclaredConstructor(float.class,float.class,
                int.class,float.class,float.class);
        ctor.setAccessible(true);
        Object e=ctor.newInstance(x,y,kind,hp,0f);
        set(e,"squadRole",SquadTacticsV065.role(kind,false,1));return e;
    }
    @SuppressWarnings("unchecked")
    private static List<Object> units(RiftGameView g)throws Exception{
        return (List<Object>)get(g,"enemies");
    }
    @Test public void raidReallyDamagesMarkedTowerButNotNeighbor()throws Exception{
        RiftGameView g=game(40,SquadTacticsV065.SIEGE);
        int[] towers=(int[])get(g,"towers");
        float[] hp=(float[])get(g,"towerHp");
        float[] xs=(float[])get(g,"towerX"),ys=(float[])get(g,"towerY");
        assertTrue(towers[1]>0);
        Object slinger=enemy(1,xs[1]+145f,ys[1],175f);
        units(g).add(slinger);
        float before=hp[1],other=hp[0];
        tick(g,.06f);
        assertEquals(1,(int)get(g,"siegeFocusPad"));
        assertTrue("marked gun takes the actual hit",hp[1]<before);
        assertEquals(other,hp[0],.001f);
        assertEquals(1,units(g).size());
    }
    @Test public void brokenGunReassignsFocusWithoutNewMobs()throws Exception{
        RiftGameView g=game(40,SquadTacticsV065.SIEGE);
        Object slinger=enemy(1,1550f,700f,175f);units(g).add(slinger);
        tick(g,.06f);
        int first=(int)get(g,"siegeFocusPad");
        assertTrue(first>=0);
        ((int[])get(g,"towers"))[first]=0;
        ((float[])get(g,"towerHp"))[first]=0f;
        tick(g,.06f);
        assertTrue((int)get(g,"siegeFocusPad")>=0);
        assertNotEquals(first,(int)get(g,"siegeFocusPad"));
        assertTrue((float)get(slinger,"x")<1550f);
    }
    @Test public void severalRaidersCannotCrashWhenFocusedTurretFallsInSameFrame()
            throws Exception{
        RiftGameView g=game(40,SquadTacticsV065.SIEGE);
        float[] hp=(float[])get(g,"towerHp");
        float[] xs=(float[])get(g,"towerX"),ys=(float[])get(g,"towerY");
        int[] towers=(int[])get(g,"towers");
        assertTrue(towers[1]>0);
        hp[1]=5f; // first real attack destroys the marked gun
        for(int i=0;i<3;i++)
            units(g).add(enemy(1,xs[1]+145f,ys[1]+(i-1)*4f,175f));
        tick(g,.06f);
        assertEquals(0,towers[1]);
        assertEquals(0f,hp[1],.001f);
        assertEquals(3,units(g).size()); // no extra spawns or exceptions
        tick(g,.06f);
        assertNotEquals(1,(int)get(g,"siegeFocusPad"));
    }
    @Test public void livingShieldEnablesPhysicalHealerRetreat()throws Exception{
        RiftGameView g=game(40,SquadTacticsV065.SHIELD_WALL);
        set(g,"heroX",1330f);set(g,"heroY",610f);
        Object shaman=enemy(4,1490f,550f,100f);
        set(shaman,"hp",38f);
        Object shield=enemy(2,1515f,555f,240f);
        units(g).add(shaman);units(g).add(shield);
        float x=(float)get(shaman,"x");
        tick(g,.08f);
        assertTrue((float)get(shaman,"retreatTime")>0f);
        assertTrue((float)get(shaman,"x")>x);
        set(shield,"hp",0f);
        set(shaman,"retreatTime",0f);
        set(shaman,"retreatCooldown",0f);
        float before=(float)get(shaman,"x");
        tick(g,.06f);
        assertEquals(0f,(float)get(shaman,"retreatTime"),.001f);
        assertTrue((float)get(shaman,"x")<before);
    }
    @Test public void actualPincerRoutesDivergeWithoutEnemyInflation()throws Exception{
        RiftGameView g=game(40,SquadTacticsV065.FLANK);
        set(g,"heroX",LaneRules.HERO_MIN_X);set(g,"heroY",LaneRules.HERO_MIN_Y);
        Object a=enemy(0,1570f,530f,120f),b=enemy(0,1570f,530f,120f);
        set(a,"pincerSide",-1);set(b,"pincerSide",1);
        units(g).add(a);units(g).add(b);
        tick(g,.12f);
        assertTrue((float)get(a,"y")<(float)get(b,"y"));
        assertTrue((float)get(a,"x")<1570f);
        assertTrue((float)get(b,"x")<1570f);
        assertEquals(2,units(g).size());
    }
}
