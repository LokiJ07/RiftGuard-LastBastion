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
public class FieldObjectivesIntegrationV074Test {
    private static Object get(Object obj,String name)throws Exception{
        Field f=obj.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(obj);
    }
    private static void set(Object obj,String name,Object value)throws Exception{
        Field f=obj.getClass().getDeclaredField(name);f.setAccessible(true);f.set(obj,value);
    }
    private static Object invoke(Object obj,String name,Class<?>[] sig,Object... args)throws Exception{
        Method m=obj.getClass().getDeclaredMethod(name,sig);m.setAccessible(true);
        return m.invoke(obj,args);
    }
    private static void call(RiftGameView game,String name)throws Exception{
        invoke(game,name,new Class<?>[0]);
    }
    private static void tick(RiftGameView game,float dt)throws Exception{
        invoke(game,"update",new Class<?>[]{float.class},dt);
    }
    private static RiftGameView start(int stage)throws Exception{
        Context c=RuntimeEnvironment.getApplication();
        SharedPreferences pref=c.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE);
        assertTrue(pref.edit().clear().putInt("unlocked",stage).commit());
        RiftGameView g=new RiftGameView(c);g.layout(0,0,1280,720);
        invoke(g,"start",new Class<?>[]{int.class},stage);
        set(g,"wave",2);set(g,"firstWavePending",false);set(g,"waiting",false);
        set(g,"remaining",0);set(g,"spawnTimer",999f);
        call(g,"resetSiegeMission");
        return g;
    }
    private static void moveTo(RiftGameView g,float x,float y)throws Exception{
        set(g,"heroX",x);set(g,"heroY",y);
    }
    @SuppressWarnings("unchecked")
    private static List<Object> enemies(RiftGameView g)throws Exception{
        return (List<Object>)get(g,"enemies");
    }
    private static Object enemy(float x,float y)throws Exception{
        Class<?> type=Class.forName("com.lokij07.riftguard.RiftGameView$Enemy");
        Constructor<?> c=type.getDeclaredConstructor(
                float.class,float.class,int.class,float.class,float.class);
        c.setAccessible(true);return c.newInstance(x,y,1,150f,0f);
    }
    @Test public void rescueRequiresActualHeroVisitsThenPaysExactlyOnce()throws Exception{
        RiftGameView g=start(45);
        assertEquals(SiegeMissionsV069.RESCUE,SiegeMissionsV069.mode(45));
        tick(g,.05f);assertFalse((Boolean)get(g,"waiting"));
        int starting=(Integer)get(g,"gold");
        for(int i=0;i<FieldObjectivesV074.SURVIVORS;i++){
            moveTo(g,FieldObjectivesV074.rescueX(45,i),FieldObjectivesV074.rescueY(45,i));
            tick(g,.06f);
            assertEquals(i+1,(int)get(g,"villagersSaved"));
            assertEquals(1,((int[])get(g,"villagerState"))[i]);
        }
        assertTrue((Boolean)get(g,"waiting"));
        assertTrue((Boolean)get(g,"missionEventRewarded"));
        int bonus=CampaignEncountersV073.bonusGold(45,SiegeMissionsV069.RESCUE)+20+25;
        assertEquals(starting+bonus,(int)get(g,"gold"));
        tick(g,.2f);assertEquals(starting+bonus,(int)get(g,"gold"));
    }
    @Test public void realEnemyTargetsUnrescuedVillagerAndCanCauseFailure()throws Exception{
        RiftGameView g=start(45);
        moveTo(g,LaneRules.HERO_MIN_X,LaneRules.HERO_MIN_Y);
        for(int i=1;i<3;i++){
            float x=FieldObjectivesV074.rescueX(45,i),
                  y=FieldObjectivesV074.rescueY(45,i);
            Object npcHunter=enemy(x,y);
            enemies(g).add(npcHunter);
            ((float[])get(g,"villagerHp"))[i]=5f;
        }
        tick(g,.06f);
        assertEquals(2,(int)get(g,"villagersLost"));
        assertEquals("OUTRO",get(g,"scene").toString());
        assertEquals(2,((int[])get(g,"villagerState"))[1]);
        assertEquals(2,((int[])get(g,"villagerState"))[2]);
    }
    @Test public void riftRequiresStationaryChannelAndStopsDrainAfterSealing()throws Exception{
        RiftGameView g=start(54);
        assertEquals(SiegeMissionsV069.SEAL,SiegeMissionsV069.mode(54));
        moveTo(g,LaneRules.HERO_MIN_X,LaneRules.HERO_MIN_Y);
        set(g,"sealPulseTimer",.02f);
        float base=(Float)get(g,"baseHp");tick(g,.08f);
        assertTrue((Float)get(g,"baseHp")<base);
        assertFalse((Boolean)get(g,"waiting"));
        int oldGold=(Integer)get(g,"gold");
        for(int i=0;i<FieldObjectivesV074.SEALS;i++){
            moveTo(g,FieldObjectivesV074.sealX(54,i),FieldObjectivesV074.sealY(54,i));
            for(int frame=0;frame<20;frame++)tick(g,.12f);
            assertTrue(((boolean[])get(g,"sealDone"))[i]);
            assertEquals(i+1,(int)get(g,"riftsSealed"));
        }
        assertTrue((Boolean)get(g,"waiting"));
        assertEquals(oldGold+CampaignEncountersV073.bonusGold(54,
                        SiegeMissionsV069.SEAL)+25,(int)get(g,"gold"));
        float baseDone=(Float)get(g,"baseHp");
        tick(g,.25f);assertEquals(baseDone,(Float)get(g,"baseHp"),.0001f);
    }
    @Test public void coldRecoveryResetsUncommittedFieldObjectiveState()throws Exception{
        for(int stage:new int[]{45,54}){
            RiftGameView g=start(stage);
            if(stage==45){
                moveTo(g,FieldObjectivesV074.rescueX(stage,0),
                        FieldObjectivesV074.rescueY(stage,0));tick(g,.06f);
                assertEquals(1,(int)get(g,"villagersSaved"));
            }else{
                moveTo(g,FieldObjectivesV074.sealX(stage,0),
                        FieldObjectivesV074.sealY(stage,0));tick(g,.25f);
                assertTrue(((float[])get(g,"sealCharge"))[0]>0f);
            }
            call(g,"captureWaveCheckpoint");
            RiftGameView resumed=new RiftGameView(RuntimeEnvironment.getApplication());
            resumed.layout(0,0,1280,720);call(resumed,"continueSavedWave");
            assertEquals(2,(int)get(resumed,"wave"));
            assertTrue((Boolean)get(resumed,"paused"));
            assertEquals(0,(int)get(resumed,"villagersSaved"));
            assertEquals(0,(int)get(resumed,"riftsSealed"));
            assertEquals(0f,((float[])get(resumed,"sealCharge"))[0],.0001f);
        }
    }
}
