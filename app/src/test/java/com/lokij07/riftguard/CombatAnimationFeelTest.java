package com.lokij07.riftguard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.SystemClock;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class CombatAnimationFeelTest {
    private static Object field(Object target,String name)throws Exception{
        Field f=target.getClass().getDeclaredField(name);
        f.setAccessible(true);return f.get(target);
    }
    private static void set(Object target,String name,float value)throws Exception{
        Field f=target.getClass().getDeclaredField(name);
        f.setAccessible(true);f.setFloat(target,value);
    }
    private static void invoke(Object target,String method,Class<?>[] params,
                               Object...args)throws Exception{
        Method m=target.getClass().getDeclaredMethod(method,params);
        m.setAccessible(true);m.invoke(target,args);
    }

    @Test public void hitsPauseAnimationNotInputOrPhysicsAndExpireQuickly(){
        CombatAnimationFeel fx=new CombatAnimationFeel();
        assertEquals(.016f,fx.frame(.016f),.0001f);
        fx.onDamage(210,250,0xffe9b787,true,350);
        assertTrue(fx.pauseRemaining()>.04f);
        assertEquals(0f,fx.frame(.016f),.0001f);
        assertTrue(Math.abs(fx.shakeX())>0f||Math.abs(fx.shakeY())>0f);
        float resume=0;
        for(int i=0;i<15;i++)resume+=fx.frame(.016f);
        assertTrue(resume>.17f);
        assertEquals(0f,fx.pauseRemaining(),.0001f);
        assertEquals(0f,fx.shakeX(),.0001f);
        assertEquals(0f,fx.shakeY(),.0001f);
        assertEquals(0f,fx.flashRemaining(),.0001f);
    }

    @Test public void accessibilitySuppressesShakeAndPauseButKeepsImpactReadable(){
        CombatAnimationFeel fx=new CombatAnimationFeel();
        fx.setReducedMotion(true);
        fx.onDamage(240,260,0xffaabbcc,false,80);
        assertEquals(0f,fx.pauseRemaining(),0f);
        assertEquals(.016f,fx.frame(.016f),.0001f);
        assertEquals(0f,fx.shakeX(),0f);
        assertEquals(0f,fx.shakeY(),0f);
        assertTrue(fx.flashRemaining()>0);
        fx.reset();
        assertEquals(0f,fx.flashRemaining(),0f);
        fx.onDamage(Float.NaN,0,Color.WHITE,true,20);
        assertEquals(0f,fx.flashRemaining(),0f);
    }

    @Test public void directionalImpactIsRealTransparentSpriteNotDecorativeText(){
        CombatAnimationFeel fx=new CombatAnimationFeel();
        Bitmap sprite=Bitmap.createBitmap(280,280,Bitmap.Config.ARGB_8888);
        fx.onDamage(140,140,0xffffaa77,true,120);
        fx.frame(.016f);
        fx.drawContact(new Canvas(sprite));
        int occupied=0;
        for(int y=87;y<170;y++)for(int x=95;x<186;x++)
            if(Color.alpha(sprite.getPixel(x,y))>20)occupied++;
        assertTrue("Contact streaks must paint actual nontransparent pixels",
                occupied>30);
        assertEquals(Color.TRANSPARENT,sprite.getPixel(2,2));
        sprite.recycle();
    }

    @Test public void realCrossbowImpactTriggersHitEventNotTriggerPull()
            throws Exception {
        Context ctx=RuntimeEnvironment.getApplication();
        ctx.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE)
                .edit().clear().commit();
        RiftGameView game=new RiftGameView(ctx);
        invoke(game,"start",new Class[]{int.class},0);
        CombatAnimationFeel fx=(CombatAnimationFeel)field(game,"combatFeel");
        invoke(game,"spawnEnemy",new Class[0]);
        @SuppressWarnings("unchecked")
        List<Object> enemies=(List<Object>)field(game,"enemies");
        Object enemy=enemies.get(0);
        float x=(float)field(game,"heroX"),y=(float)field(game,"heroY");
        set(enemy,"x",x+150);set(enemy,"y",y);set(enemy,"hp",900);
        invoke(game,"fire",new Class[0]);
        assertEquals("No impact before projectile arrives",0f,
                fx.flashRemaining(),.0001f);
        invoke(game,"update",new Class[]{float.class},.18f);
        invoke(game,"update",new Class[]{float.class},.016f);
        assertTrue("Real collision should start feedback",fx.flashRemaining()>0);
        assertTrue(fx.pauseRemaining()>0);
        assertTrue((float)field(enemy,"hp")<900);
    }
}
