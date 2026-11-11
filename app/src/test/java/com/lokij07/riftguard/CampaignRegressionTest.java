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
import java.util.List;
import static org.junit.Assert.*;

/** Route, combat, economy and rendering stability checks across the campaign. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class CampaignRegressionTest {
    private static Object field(Object instance,String name) throws Exception {
        Field f=instance.getClass().getDeclaredField(name);
        f.setAccessible(true);return f.get(instance);
    }
    private static void set(Object instance,String name,Object value) throws Exception {
        Field f=instance.getClass().getDeclaredField(name);
        f.setAccessible(true);f.set(instance,value);
    }
    private static void call(RiftGameView v,String method,Class<?>[] types,Object... args) throws Exception {
        Method m=RiftGameView.class.getDeclaredMethod(method,types);
        m.setAccessible(true);m.invoke(v,args);
    }
    private RiftGameView newView(int rank) {
        Context context=RuntimeEnvironment.getApplication();
        context.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE)
            .edit().clear().putInt("fortress_rank",rank).commit();
        RiftGameView v=new RiftGameView(context);
        v.layout(0,0,1280,720);
        return v;
    }
    @Test public void all300StagesHaveValidProgressionAndNonemptyWaves() {
        for(int i=0;i<Campaign.STAGES;i++) {
            int level=Campaign.level(i),map=Campaign.map(i),world=Campaign.world(i);
            assertEquals(i,Campaign.stage(level,map));
            assertTrue(world>=0&&world<Campaign.WORLDS.length);
            assertTrue(Campaign.waves(i)>=4);
            assertTrue(Campaign.enemies(i,1)>=4);
            assertTrue(Progression.stageReward(i,true)>0);
            assertEquals(Math.min(300,i+1),Campaign.unlockedAfter(i,i));
            assertFalse(Campaign.accessible(i+1,i));
            for(int kind=0;kind<=6;kind++){
                assertTrue(BattleRules.enemyHealth(i,kind)>0);
                assertTrue(BattleRules.enemySpeed(kind)>0);
            }
        }
    }
    @Test public void representativeWorldAndMapBattlesRenderAndAdvance() throws Exception {
        RiftGameView v=newView(5);
        int[] stages={0,1,2,30,61,92,123,154,185,216,247,278,299};
        for(int stage:stages) {
            call(v,"start",new Class<?>[]{int.class},stage);
            call(v,"spawnEnemy",new Class<?>[0]);
            float lastX=Float.MAX_VALUE;
            @SuppressWarnings("unchecked")
            List<Object> mobs=(List<Object>)field(v,"enemies");
            assertEquals(1,mobs.size());
            Object e=mobs.get(0);
            for(int frame=0;frame<160;frame++)
                call(v,"update",new Class<?>[]{float.class},.04f);
            float x=(Float)field(e,"x");
            assertTrue("Enemy failed to travel left, stage="+stage,x<1837f);
            assertTrue("Enemy moved outside map="+stage,x>=0 && x<=1900);
            assertTrue("Hero should spawn alive, stage="+stage,(Float)field(v,"heroHp")>0);
            Bitmap bitmap=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
            v.draw(new Canvas(bitmap));
            assertTrue("Stage visual missing="+stage,android.graphics.Color.alpha(bitmap.getPixel(640,350))>0);
            bitmap.recycle();
        }
    }
    @Test public void allThreeFirstWorldVariantsCanWinThroughActualAttacks() throws Exception {
        RiftGameView v=newView(5);
        for(int stage=0;stage<3;stage++) {
            call(v,"start",new Class<?>[]{int.class},stage);
            set(v,"firing",true);
            boolean won=false;
            for(int frame=0;frame<11500;frame++) {
                if(frame%18==0){
                    int gold=(Integer)field(v,"gold");
                    if(gold>=55)call(v,"build",new Class<?>[0]);
                }
                if(frame%160==40)call(v,"pulse",new Class<?>[0]);
                call(v,"update",new Class<?>[]{float.class},.05f);
                String screen=field(v,"scene").toString();
                if("WIN".equals(screen)){won=true;break;}
                if("LOSE".equals(screen))break;
            }
            assertTrue("Stage "+stage+" could not be won by actual combat",won);
            assertTrue("No combat kills on stage="+stage,(Integer)field(v,"kills")>0);
        }
    }
}
