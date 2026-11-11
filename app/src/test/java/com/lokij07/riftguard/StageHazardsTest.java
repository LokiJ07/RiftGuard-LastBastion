package com.lokij07.riftguard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class StageHazardsTest {
    private static void set(Object obj,String name,Object val)throws Exception{
        Field f=obj.getClass().getDeclaredField(name);f.setAccessible(true);f.set(obj,val);
    }
    private static float num(Object obj,String name)throws Exception{
        Field f=obj.getClass().getDeclaredField(name);f.setAccessible(true);
        return f.getFloat(obj);
    }
    private static void call(RiftGameView game,String name,Class<?>[] sig,Object... args)
            throws Exception{
        Method m=RiftGameView.class.getDeclaredMethod(name,sig);
        m.setAccessible(true);m.invoke(game,args);
    }
    private RiftGameView game() {
        Context context=RuntimeEnvironment.getApplication();
        context.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE)
                .edit().clear().putInt("fortress_rank",5).commit();
        RiftGameView game=new RiftGameView(context);
        game.layout(0,0,1280,720);
        return game;
    }
    private static float activeTime(int stage,int index) {
        for(float t=0;t<12;t+=.04f)
            if(StageHazards.active(stage,index,t))return t;
        throw new AssertionError("Hazard must activate during a cycle");
    }
    @Test public void all300StagesHaveUniqueDeterministicHazardsOnActualRoad() {
        Set<String> positions=new HashSet<>();
        for(int stage=0;stage<300;stage++){
            int n=StageHazards.count(stage);
            assertTrue(n>=1&&n<=4);
            assertEquals(Campaign.world(stage),StageHazards.kind(stage));
            for(int i=0;i<n;i++){
                float x=StageHazards.x(stage,i),y=StageHazards.y(stage,i);
                assertTrue(x>=1280&&x<1785);
                assertTrue(y>=350&&y<=690);
                assertTrue(Math.abs(y-LaneRules.stagePathY(stage,x))<125);
                assertTrue(StageHazards.radius(stage,i)>=55);
                assertTrue(StageHazards.charge(stage,i,.5f)>=0);
                assertTrue(StageHazards.charge(stage,i,.5f)<1f);
                positions.add(stage+"-"+i+"-"+x+"-"+y);
            }
        }
        assertTrue(positions.size()>=600);
        assertEquals(4,StageHazards.count(Campaign.stage(10,2)));
    }
    @Test public void animatedPoisonHazardActuallyDamagesHero() throws Exception {
        int stage=Campaign.stage(24,2); // Venom Marsh / Map C
        RiftGameView game=game();
        call(game,"start",new Class<?>[]{int.class},stage);
        set(game,"heroX",StageHazards.x(stage,0));
        set(game,"heroY",StageHazards.y(stage,0));
        set(game,"gameTime",activeTime(stage,0));
        float before=num(game,"heroHp");
        for(int i=0;i<20;i++)
            call(game,"update",new Class<?>[]{float.class},.04f);
        assertTrue("Poison pool must cause actual in-game damage",num(game,"heroHp")<before);
        Bitmap bitmap=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        game.draw(new Canvas(bitmap));bitmap.recycle();
    }
    @Test public void brambleHazardActuallySlowsHeroMovement() throws Exception {
        RiftGameView game=game();
        int stage=0; // Greenhaven Bramble Snare
        call(game,"start",new Class<?>[]{int.class},stage);
        set(game,"heroX",StageHazards.x(stage,0));
        set(game,"heroY",StageHazards.y(stage,0));
        set(game,"gameTime",activeTime(stage,0));
        for(int i=0;i<20;i++)
            call(game,"update",new Class<?>[]{float.class},.04f);
        assertTrue("Bramble must slow hero",num(game,"hazardSlowTimer")>0f);
    }
}
