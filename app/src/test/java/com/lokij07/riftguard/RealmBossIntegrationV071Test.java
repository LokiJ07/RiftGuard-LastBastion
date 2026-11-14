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
import org.robolectric.annotation.GraphicsMode;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class RealmBossIntegrationV071Test {
    private static Object field(Object o,String n)throws Exception{
        Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);
    }
    private static void set(Object o,String n,Object v)throws Exception{
        Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);f.set(o,v);
    }
    private static Object call(Object o,String n,Class<?>[] sig,Object...args)throws Exception{
        Method m=o.getClass().getDeclaredMethod(n,sig);
        m.setAccessible(true);return m.invoke(o,args);
    }
    private static RiftGameView game(int stage)throws Exception{
        Context c=RuntimeEnvironment.getApplication();
        SharedPreferences p=c.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE);
        assertTrue(p.edit().clear().putInt("unlocked",stage).putInt("fortress_rank",5).commit());
        RiftGameView game=new RiftGameView(c);game.layout(0,0,1280,720);
        call(game,"start",new Class<?>[]{int.class},stage);
        return game;
    }
    private static Object boss(int stage,float x,float y)throws Exception{
        Class<?> type=Class.forName("com.lokij07.riftguard.RiftGameView$Enemy");
        Constructor<?> ctor=type.getDeclaredConstructor(float.class,float.class,
                int.class,float.class,float.class);
        ctor.setAccessible(true);
        Object enemy=ctor.newInstance(x,y,5,600f,0f);
        set(enemy,"boss",true);set(enemy,"worldBoss",true);
        set(enemy,"anatomy",new BossAnatomyV065(Campaign.world(stage),600f));
        set(enemy,"bossPhase",1);set(enemy,"realmLockedX",x);
        set(enemy,"realmLockedY",y);
        return enemy;
    }
    private static void strike(RiftGameView g,Object boss)throws Exception{
        call(g,"resolveRealmBossStrike",
                new Class<?>[]{boss.getClass(),float.class},boss,1f);
    }
    @Test public void liveThornhideBlastDamagesButDodgePreventsHeroDamage()
            throws Exception{
        RiftGameView g=game(Campaign.stage(10,2));
        float x=(Float)field(g,"heroX"),y=(Float)field(g,"heroY");
        Object e=boss(Campaign.stage(10,2),x+80f,y);
        float hp=(Float)field(g,"heroHp"),core=(Float)field(g,"baseHp");
        strike(g,e);
        assertTrue((Float)field(g,"heroHp")<hp);
        assertTrue((Float)field(g,"baseHp")<core);
        set(g,"hurtCooldown",0f);
        set(g,"dodgeTime",DodgeRules.DURATION);
        float afterHit=(Float)field(g,"heroHp");
        strike(g,e);
        assertEquals(afterHit,(Float)field(g,"heroHp"),.001f);
    }
    @Test public void realIronfallBossHitsItsLockedBuiltTower()throws Exception{
        int stage=Campaign.stage(60,2);
        RiftGameView g=game(stage);
        int[] towers=(int[])field(g,"towers");
        float[] health=(float[])field(g,"towerHp");
        float[] xs=(float[])field(g,"towerX"),ys=(float[])field(g,"towerY");
        assertTrue(towers[0]>0);
        float original=health[0];
        Object e=boss(stage,xs[0]+240,ys[0]);
        set(e,"realmLockedTower",0);
        set(e,"realmLockedX",xs[0]);set(e,"realmLockedY",ys[0]);
        strike(g,e);
        assertTrue(health[0]<original);
    }
    @Test public void realCrystalBossSustainStopsWhenItsCoreBreaks()throws Exception{
        int stage=Campaign.stage(70,2);RiftGameView g=game(stage);
        Object e=boss(stage,1240f,550f);
        set(e,"hp",250f);
        strike(g,e);
        float healed=(Float)field(e,"hp");
        assertTrue(healed>250);
        BossAnatomyV065 core=(BossAnatomyV065)field(e,"anatomy");
        assertTrue(core.damage(BossAnatomyV065.CORE,10000f));
        strike(g,e);
        assertEquals(healed,(Float)field(e,"hp"),.001f);
    }
    @Test public void telegraphedWorldBossHoldsItsGroundUntilImpact()throws Exception{
        int stage=Campaign.stage(10,2);RiftGameView g=game(stage);
        Object e=boss(stage,1220f,520f);
        set(e,"specialCharge",1.25f);
        set(e,"specialTimer",5f);
        @SuppressWarnings("unchecked")
        List<Object> mobs=(List<Object>)field(g,"enemies");
        mobs.add(e);
        set(g,"waiting",false);set(g,"firstWavePending",false);
        set(g,"remaining",0);set(g,"wave",2);
        float x=(Float)field(e,"x"),y=(Float)field(e,"y");
        call(g,"update",new Class<?>[]{float.class},.05f);
        assertEquals(x,(Float)field(e,"x"),.001f);
        assertEquals(y,(Float)field(e,"y"),.001f);
        assertTrue((Float)field(e,"specialCharge")<1.25f);
        assertTrue((Float)field(e,"specialCharge")>0f);
    }
    @Test public void rendererActuallyPaintsAllTenDifferentTelegraphs(){
        Bitmap bmp=Bitmap.createBitmap(640,480,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(bmp);
        for(int realm=0;realm<10;realm++){
            int stage=Campaign.stage((realm+1)*10,2);
            c.drawColor(0xff16222c);
            int before=bmp.getPixel(360,260);
            RealmBossThreatV071.draw(c,stage,2,360,260,480,260,
                    130,220,.65f,RealmBossV071.chargeSeconds(stage,2),false);
            int sample=bmp.getPixel(360,260);
            int count=0;
            for(int yy=80;yy<430;yy+=6)for(int xx=100;xx<590;xx+=6)
                if(bmp.getPixel(xx,yy)!=0xff16222c)count++;
            assertTrue("realm "+realm+" did not paint",count>20);
        }
        bmp.recycle();
    }
}
