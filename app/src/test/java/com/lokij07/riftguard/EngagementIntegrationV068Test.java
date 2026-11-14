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
public class EngagementIntegrationV068Test {
    static Object get(Object o,String n)throws Exception{
        Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);
    }
    static void set(Object o,String n,Object v)throws Exception{
        Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);f.set(o,v);
    }
    static void call(RiftGameView game,String method,Class<?>[] types,Object... args)
            throws Exception{
        Method m=RiftGameView.class.getDeclaredMethod(method,types);
        m.setAccessible(true);m.invoke(game,args);
    }
    static void action(RiftGameView game,String m)throws Exception{
        call(game,m,new Class<?>[0]);
    }
    static SharedPreferences reset(Context ctx,int unlocked){
        SharedPreferences p=ctx.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE);
        assertTrue(p.edit().clear().putInt("unlocked",unlocked).commit());
        return p;
    }
    @Test public void realSignatureShamanHealsOneWoundedAlly()throws Exception{
        Context ctx=RuntimeEnvironment.getApplication();reset(ctx,7);
        RiftGameView g=new RiftGameView(ctx);g.layout(0,0,1280,720);
        call(g,"start",new Class<?>[]{int.class},7);
        int total=DifficultyRules.enemies(7,2,DifficultyRules.NORMAL);
        set(g,"wave",2);set(g,"remaining",Math.max(2,total/2));
        action(g,"spawnEnemy");
        @SuppressWarnings("unchecked") List<Object> mobs=(List<Object>)get(g,"enemies");
        assertEquals(1,mobs.size());
        Object shaman=mobs.get(0);assertEquals(4,get(shaman,"kind"));
        float x=(Float)get(g,"heroX"),y=(Float)get(g,"heroY");
        set(shaman,"x",x+200f);set(shaman,"y",y);set(shaman,"shamanCooldown",0f);
        Constructor<?> ctor=shaman.getClass().getDeclaredConstructor(
                float.class,float.class,int.class,float.class,float.class);
        ctor.setAccessible(true);
        Object ally=ctor.newInstance(x+230f,y,0,100f,0f);
        set(ally,"hp",40f);mobs.add(ally);
        set(g,"waiting",true);
        call(g,"update",new Class<?>[]{float.class},.016f);
        assertTrue((Float)get(ally,"hp")>40f);
        assertTrue((Float)get(ally,"hp")<=100f);
    }
    @Test public void contractPaysOnceAndCounterSurvivesColdResume()throws Exception{
        Context ctx=RuntimeEnvironment.getApplication();
        SharedPreferences prefs=reset(ctx,7);
        RiftGameView g=new RiftGameView(ctx);g.layout(0,0,1280,720);
        call(g,"start",new Class<?>[]{int.class},7);
        assertEquals(BattleContractsV068.SPELLCASTER,BattleContractsV068.kind(7));
        set(g,"battleSkillCasts",2);
        action(g,"complete");
        assertEquals(BattleContractsV068.firstClearBonus(7),get(g,"contractBonus"));
        assertEquals(8,prefs.getInt("unlocked",0));
        call(g,"start",new Class<?>[]{int.class},7);
        set(g,"battleSkillCasts",2);action(g,"complete");
        assertEquals(0,get(g,"contractBonus"));
        call(g,"start",new Class<?>[]{int.class},7);
        set(g,"wave",2);set(g,"battleSkillCasts",3);
        action(g,"captureWaveCheckpoint");
        RiftGameView resumed=new RiftGameView(ctx);resumed.layout(0,0,1280,720);
        action(resumed,"continueSavedWave");
        assertEquals(3,get(resumed,"battleSkillCasts"));
        assertEquals(2,get(resumed,"wave"));
        assertTrue((Boolean)get(resumed,"paused"));
    }
}
