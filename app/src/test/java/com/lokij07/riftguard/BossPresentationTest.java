package com.lokij07.riftguard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.SystemClock;
import android.view.MotionEvent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class BossPresentationTest {
    private static Object field(Object obj,String name) throws Exception {
        Field f=obj.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(obj);
    }
    private static void set(Object obj,String name,Object val) throws Exception {
        Field f=obj.getClass().getDeclaredField(name);f.setAccessible(true);f.set(obj,val);
    }
    private static void invoke(RiftGameView v,String method,Class<?>[] args,Object... vals)
            throws Exception {
        Method m=RiftGameView.class.getDeclaredMethod(method,args);m.setAccessible(true);
        m.invoke(v,vals);
    }
    private RiftGameView view() {
        Context c=RuntimeEnvironment.getApplication();
        c.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE)
                .edit().clear().putInt("fortress_rank",5).commit();
        RiftGameView v=new RiftGameView(c);v.layout(0,0,1280,720);return v;
    }
    private void tap(RiftGameView v,float x,float y){
        long t=SystemClock.uptimeMillis();
        MotionEvent down=MotionEvent.obtain(t,t,MotionEvent.ACTION_DOWN,x,y,0);
        v.onTouchEvent(down);down.recycle();
        MotionEvent up=MotionEvent.obtain(t,t+40,MotionEvent.ACTION_UP,x,y,0);
        v.onTouchEvent(up);up.recycle();
    }
    @Test public void worldBossTelegraphsThenActuallyDamagesCore() throws Exception {
        RiftGameView game=view();
        int stage=Campaign.stage(10,2);
        invoke(game,"start",new Class<?>[]{int.class},stage);
        set(game,"wave",DifficultyRules.waves(stage,DifficultyRules.NORMAL));
        set(game,"remaining",1);
        invoke(game,"spawnEnemy",new Class<?>[0]);
        @SuppressWarnings("unchecked")
        List<Object> mobs=(List<Object>)field(game,"enemies");
        Object boss=mobs.get(0);
        assertTrue((Boolean)field(boss,"boss"));
        set(boss,"x",1290f);
        set(boss,"y",520f);
        set(boss,"specialTimer",0f);
        float coreBefore=(Float)field(game,"baseHp");
        invoke(game,"update",new Class<?>[]{float.class},.10f);
        assertTrue("Attack warning must appear before the hit",
                (Float)field(boss,"specialCharge")>0f);
        Bitmap image=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        game.draw(new Canvas(image));image.recycle();
        for(int i=0;i<11;i++)
            invoke(game,"update",new Class<?>[]{float.class},.10f);
        assertTrue("Boss shockwave must cause real fortress damage",
                (Float)field(game,"baseHp")<coreBefore);
    }
    @Test public void ultraWideScreenHasNoBlackSidePillarsAndAcceptsTouch() {
        RiftGameView game=view();
        game.layout(0,0,1780,720);
        Bitmap image=Bitmap.createBitmap(1780,720,Bitmap.Config.ARGB_8888);
        game.draw(new Canvas(image));
        assertNotEquals("Scene-matched extension must fill left gutter",
                Color.BLACK,image.getPixel(25,340));
        assertNotEquals(Color.BLACK,image.getPixel(1760,340));
        image.recycle();
        tap(game,500,450); // 250px left safe-area + normal 250px campaign button
        assertEquals("MAP",getScene(game));
    }
    private String getScene(RiftGameView game) {
        try { return field(game,"scene").toString(); }
        catch(Exception ex){throw new AssertionError(ex);}
    }
}
