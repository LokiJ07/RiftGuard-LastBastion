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
/** Real game-loop checks, not just separate formula tests. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public class CombatFairnessIntegrationV076Test {
    private static Object get(Object o,String n)throws Exception{
        Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);
    }
    private static void set(Object o,String n,Object v)throws Exception{
        Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);f.set(o,v);
    }
    private static Object call(Object o,String name,Class<?>[] args,Object... values)
            throws Exception{
        Method method=o.getClass().getDeclaredMethod(name,args);
        method.setAccessible(true);return method.invoke(o,values);
    }
    private static void tick(RiftGameView g,float dt)throws Exception{
        call(g,"update",new Class<?>[]{float.class},dt);
    }
    private static RiftGameView game(int stage,int difficulty)throws Exception{
        Context c=RuntimeEnvironment.getApplication();
        SharedPreferences prefs=c.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE);
        assertTrue(prefs.edit().clear().putInt("unlocked",stage).
                putInt("fortress_rank",5).commit());
        RiftGameView g=new RiftGameView(c);g.layout(0,0,1280,720);
        call(g,"start",new Class<?>[]{int.class},stage);
        set(g,"difficultyMode",difficulty);
        set(g,"wave",3);set(g,"firstWavePending",false);
        set(g,"waiting",false);set(g,"remaining",0);set(g,"spawnTimer",999f);
        set(g,"squadPlan",SquadTacticsV065.SHIELD_WALL);
        return g;
    }
    private static Object enemy(int kind,float x,float y,float hp)throws Exception{
        Class<?> type=Class.forName("com.lokij07.riftguard.RiftGameView$Enemy");
        Constructor<?> ctor=type.getDeclaredConstructor(float.class,float.class,
                int.class,float.class,float.class);ctor.setAccessible(true);
        Object e=ctor.newInstance(x,y,kind,hp,0f);
        set(e,"squadRole",SquadTacticsV065.role(kind,false,1));return e;
    }
    @SuppressWarnings("unchecked")
    private static List<Object> enemies(RiftGameView g)throws Exception{
        return (List<Object>)get(g,"enemies");
    }
    @Test public void lateExpertMeleeCannotOneShotAHealthyWarden()throws Exception{
        RiftGameView g=game(250,DifficultyRules.EXPERT);
        float max=(float)call(g,"heroMaxHp",new Class<?>[0]);
        set(g,"heroHp",max);
        float x=(float)get(g,"heroX"),y=(float)get(g,"heroY");
        enemies(g).add(enemy(2,x+22f,y,360f));
        tick(g,.05f);
        float lost=max-(float)get(g,"heroHp");
        assertTrue("melee should hurt",lost>.1f);
        assertTrue("one melee impact bounded",lost<=max*.29f+.01f);
        assertEquals("PLAY",get(g,"scene").toString());
    }
    @Test public void actualLateTurretHitIsLimitedButStillDamagesBuilding()throws Exception{
        RiftGameView g=game(250,DifficultyRules.EXPERT);
        int[] tiers=(int[])get(g,"towers");
        float[] hp=(float[])get(g,"towerHp");
        float[] xs=(float[])get(g,"towerX"),ys=(float[])get(g,"towerY");
        assertTrue(tiers[1]>0);
        float before=hp[1],max=TowerDurability.maxHp(tiers[1],5);
        enemies(g).add(enemy(2,xs[1]+135f,ys[1],360f));
        tick(g,.05f);
        float lost=before-hp[1];
        assertTrue("true tower strike expected",lost>0);
        assertTrue(lost<=max*.24f+.01f);
    }
    @Test public void livingProtectorBreakInterruptsInProgressFallback()
            throws Exception{
        RiftGameView g=game(40,DifficultyRules.NORMAL);
        set(g,"heroX",1330f);set(g,"heroY",610f);
        Object shaman=enemy(4,1490f,550f,100f),shield=enemy(2,1515f,555f,240f);
        set(shaman,"hp",38f);
        enemies(g).add(shaman);enemies(g).add(shield);
        tick(g,.08f);
        assertTrue((float)get(shaman,"retreatTime")>0f);
        set(shield,"hp",0f);
        float before=(float)get(shaman,"x");
        tick(g,.08f);
        assertEquals(0f,(float)get(shaman,"retreatTime"),.001f);
        assertTrue("unprotected shaman becomes interceptable",
                (float)get(shaman,"x")<before);
    }
    @Test public void earlyTutorialRetainsItsExactOldContactDamage()throws Exception{
        RiftGameView g=game(5,DifficultyRules.NORMAL);
        float before=(float)get(g,"heroHp");
        float x=(float)get(g,"heroX"),y=(float)get(g,"heroY");
        enemies(g).add(enemy(2,x+22f,y,260f));
        tick(g,.04f);
        assertEquals(20f,before-(float)get(g,"heroHp"),.02f);
    }
}
