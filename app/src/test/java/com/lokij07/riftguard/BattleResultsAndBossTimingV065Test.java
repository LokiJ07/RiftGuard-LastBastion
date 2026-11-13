package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import java.io.File;
import java.io.FileOutputStream;
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
public final class BattleResultsAndBossTimingV065Test {
    private static Object get(Object ob,String n)throws Exception{
        Field f=ob.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(ob);
    }
    private static void set(Object ob,String n,Object v)throws Exception{
        Field f=ob.getClass().getDeclaredField(n);f.setAccessible(true);f.set(ob,v);
    }
    private static Object call(Object ob,String n,Class<?>[] args,Object... vals)throws Exception{
        Method m=ob.getClass().getDeclaredMethod(n,args);
        m.setAccessible(true);return m.invoke(ob,vals);
    }
    private static RiftGameView stage(int id)throws Exception{
        Context ctx=RuntimeEnvironment.getApplication();
        SharedPreferences p=ctx.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE);
        p.edit().clear().commit();
        RiftGameView v=new RiftGameView(ctx);
        v.layout(0,0,1280,720);
        call(v,"start",new Class<?>[]{int.class},id);
        return v;
    }
    private static void tickOutro(RiftGameView v,float seconds)throws Exception{
        int steps=(int)Math.ceil(seconds/.05f);
        for(int i=0;i<steps;i++)
            call(v,"updateOutro",new Class<?>[]{float.class},.05f);
    }
    private static void screenshot(RiftGameView v,String name)throws Exception{
        Bitmap b=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        v.draw(new Canvas(b));
        File cwd=new File(System.getProperty("user.dir"));
        File dir=new File(cwd,new File(cwd,"app/build.gradle").exists()?
                "app/build/screenshots":"build/screenshots");
        if(!dir.exists())assertTrue(dir.mkdirs());
        try(FileOutputStream out=new FileOutputStream(new File(dir,name))){
            assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));
        }
        b.recycle();
    }
    @Test public void deathPlaysAnimatedSceneBeforeFailedPopup()throws Exception{
        RiftGameView v=stage(0);
        call(v,"markDefeat",new Class<?>[0]);
        assertEquals("OUTRO",get(v,"scene").toString());
        assertEquals(WardenAnimationMachine.State.DEATH,
                ((WardenAnimationMachine)get(v,"heroAnimation")).state());
        screenshot(v,"230-v065-warden-dying-before-failed.png");
        tickOutro(v,1.0f);
        assertEquals("OUTRO",get(v,"scene").toString());
        tickOutro(v,2f);
        assertEquals("LOSE",get(v,"scene").toString());
        screenshot(v,"231-v065-failed-after-death.png");
    }
    @Test public void victoryWalksTowardEnemyRoadBeforeCompletePopup()throws Exception{
        RiftGameView v=stage(0);
        float oldX=(Float)get(v,"heroX");
        call(v,"complete",new Class<?>[0]);
        assertEquals("OUTRO",get(v,"scene").toString());
        tickOutro(v,1.6f);
        assertEquals("OUTRO",get(v,"scene").toString());
        assertTrue((Float)get(v,"heroX")>oldX+40f);
        assertTrue((Float)get(v,"heroX")<LaneRules.ENEMY_SPAWN_X);
        screenshot(v,"232-v065-victory-road-walk.png");
        tickOutro(v,3f);
        assertEquals("WIN",get(v,"scene").toString());
        assertEquals(1,get(v,"unlocked"));
        screenshot(v,"233-v065-complete-after-walk.png");
    }
    @Test public void waveOneHasCountdownAndLaterWavesAutoAdvance()throws Exception{
        RiftGameView v=stage(0);
        assertTrue((Boolean)get(v,"waiting"));
        assertTrue((Boolean)get(v,"firstWavePending"));
        assertEquals(1,get(v,"wave"));
        set(v,"intermission",.02f);
        call(v,"update",new Class<?>[]{float.class},.04f);
        assertEquals(1,get(v,"wave"));
        assertFalse((Boolean)get(v,"firstWavePending"));
        assertFalse((Boolean)get(v,"waiting"));
        set(v,"waiting",true);set(v,"intermission",.02f);
        call(v,"advanceWave",new Class<?>[]{boolean.class},false);
        assertEquals(2,get(v,"wave"));
        assertFalse((Boolean)get(v,"waiting"));
    }
    @Test public void finalWorldBossGetsThreeSecondWarningOnlyInLastWave()throws Exception{
        RiftGameView v=stage(Campaign.stage(10,2));
        int cap=DifficultyRules.waves(Campaign.stage(10,2),DifficultyRules.NORMAL);
        set(v,"firstWavePending",false);
        set(v,"waiting",false);
        set(v,"wave",cap);
        set(v,"remaining",1);
        set(v,"spawnTimer",0f);
        call(v,"update",new Class<?>[]{float.class},.02f);
        assertTrue("Should warn before boss actually appears",
                (Float)get(v,"bossWarningCountdown")>2f);
        assertEquals(0,((List<?>)get(v,"enemies")).size());
        screenshot(v,"234-v065-incoming-boss-warning.png");
        call(v,"update",new Class<?>[]{float.class},3.2f);
        call(v,"update",new Class<?>[]{float.class},.03f);
        List<?> enemies=(List<?>)get(v,"enemies");
        assertEquals(1,enemies.size());
        Object boss=enemies.get(0);
        assertTrue((Boolean)get(boss,"boss"));
        assertEquals(0,get(v,"remaining"));
        assertEquals(cap,get(v,"wave"));
    }
    @Test public void outroProgressNeverReversesAndRoadEndsInsideVisibleStage(){
        BattleOutroV065 o=new BattleOutroV065(0,true,1234,614);
        o.advance(.8f);float x=o.positionX();
        o.advance(.8f);assertTrue(o.positionX()>x);
        o.advance(4f);
        assertTrue(o.finished());
        assertTrue(o.endX<LaneRules.ENEMY_SPAWN_X);
        BattleOutroV065 d=new BattleOutroV065(0,false,1234,614);
        d.advance(.8f);assertFalse(d.finished());
        d.advance(3f);assertTrue(d.finished());
    }
}
