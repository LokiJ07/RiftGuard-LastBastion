package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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

/** Uses the actual gameplay update kill loop, save-backed level and avatar draw. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public class WardenMilestonesV079IntegrationTest {
    private static Field field(Object obj,String name)throws Exception{
        Field f=obj.getClass().getDeclaredField(name);
        f.setAccessible(true);return f;
    }
    private static void set(Object obj,String name,Object value)throws Exception{
        field(obj,name).set(obj,value);
    }
    private static Object get(Object obj,String name)throws Exception{
        return field(obj,name).get(obj);
    }
    private static Object call(Object obj,String name,Class<?>[] params,Object...args)
            throws Exception{
        Method m=obj.getClass().getDeclaredMethod(name,params);
        m.setAccessible(true);return m.invoke(obj,args);
    }
    private static RiftGameView gameAt(int level)throws Exception{
        SharedPreferences prefs=RuntimeEnvironment.getApplication()
                .getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE);
        prefs.edit().clear()
                .putInt(WardenLevelsV078.SAVE_XP,WardenLevelsV078.totalToLevel(level))
                .commit();
        RiftGameView g=new RiftGameView(RuntimeEnvironment.getApplication());
        g.layout(0,0,1280,720);
        call(g,"start",new Class<?>[]{int.class},0);
        return g;
    }
    @SuppressWarnings("unchecked")
    private static void addDeadEnemy(RiftGameView g)throws Exception{
        Class<?> c=Class.forName("com.lokij07.riftguard.RiftGameView$Enemy");
        Constructor<?> ctor=c.getDeclaredConstructor(
                float.class,float.class,int.class,float.class,float.class);
        ctor.setAccessible(true);
        Object dead=ctor.newInstance(920f,550f,0,0f,0f);
        ((List<Object>)get(g,"enemies")).add(dead);
    }
    @Test public void permanentOathHpAppliesToActualHeroAndNoNewWallet()
            throws Exception{
        RiftGameView low=gameAt(9);
        float base=(float)call(low,"heroMaxHp",new Class<?>[0]);
        RiftGameView high=gameAt(10);
        float withOath=(float)call(high,"heroMaxHp",new Class<?>[0]);
        assertEquals(base+12f,withOath,.001f);
        WardenLevelsV078 levels=(WardenLevelsV078)get(high,"wardenLevel");
        assertEquals(WardenLevelsV078.lifetimePoints(10),levels.unspent());
    }
    @Test public void actualKillLoopHealsOnlyAtMilestoneAndNeverPastMaximum()
            throws Exception{
        RiftGameView below=gameAt(24);
        float maxBelow=(float)call(below,"heroMaxHp",new Class<?>[0]);
        set(below,"heroHp",maxBelow-30f);
        set(below,"kills",7);
        addDeadEnemy(below);
        call(below,"update",new Class<?>[]{float.class},.01f);
        assertEquals(8,(int)get(below,"kills"));
        assertEquals(maxBelow-30f,(float)get(below,"heroHp"),.01f);

        RiftGameView active=gameAt(25);
        float max=(float)call(active,"heroMaxHp",new Class<?>[0]);
        set(active,"heroHp",max-3f);
        set(active,"kills",7);
        addDeadEnemy(active);
        call(active,"update",new Class<?>[]{float.class},.01f);
        assertEquals(8,(int)get(active,"kills"));
        assertEquals(max,(float)get(active,"heroHp"),.01f);

        set(active,"heroHp",max-30f);
        set(active,"kills",8);
        addDeadEnemy(active);
        call(active,"update",new Class<?>[]{float.class},.01f);
        assertEquals(9,(int)get(active,"kills"));
        assertEquals(max-30f,(float)get(active,"heroHp"),.01f);
    }
    @Test public void masteryAvatarAndCharacterScreenRenderAtLevel100()
            throws Exception{
        RiftGameView g=gameAt(100);
        WardenLevelsV078 levels=(WardenLevelsV078)get(g,"wardenLevel");
        assertEquals(1.10f,levels.attackMultiplier(),.001f);
        assertEquals(1.04f,levels.moveMultiplier(),.001f);
        assertEquals(.20f,levels.skillCooldownReduction(),.001f);
        Bitmap bmp=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        call(g,"drawHero",new Class<?>[]{Canvas.class},new Canvas(bmp));
        call(g,"drawCharacter",new Class<?>[]{Canvas.class},new Canvas(bmp));
        assertEquals(1280,bmp.getWidth());
        bmp.recycle();
    }
}
