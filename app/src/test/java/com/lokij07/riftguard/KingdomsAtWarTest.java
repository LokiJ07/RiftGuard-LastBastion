package com.lokij07.riftguard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import static org.junit.Assert.*;

/** End-to-end evidence for the *actual* Kingdoms at War gameplay code. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class KingdomsAtWarTest {
    private static Field f(Object o,String key)throws Exception{
        Field field=o.getClass().getDeclaredField(key);field.setAccessible(true);
        return field;
    }
    private static Object read(Object o,String k)throws Exception{return f(o,k).get(o);}
    private static void set(Object o,String k,Object v)throws Exception{f(o,k).set(o,v);}
    private static void call(RiftGameView v,String name,Class<?>[] sig,Object...a)throws Exception{
        Method m=RiftGameView.class.getDeclaredMethod(name,sig);
        m.setAccessible(true);m.invoke(v,a);
    }
    private static RiftGameView game(int rank)throws Exception{
        Context c=RuntimeEnvironment.getApplication();
        c.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE)
                .edit().clear().putInt("fortress_rank",rank).commit();
        RiftGameView v=new RiftGameView(c);v.layout(0,0,1280,720);
        return v;
    }
    private static void start(RiftGameView g,int stage)throws Exception{
        call(g,"start",new Class<?>[]{int.class},stage);
    }
    @Test public void everyStageHasFiveToEightDistinctRealPadsAndSafeRoadClearance(){
        Set<Integer> count=new HashSet<>();
        for(int stage=0;stage<300;stage++){
            int active=TowerPadLayout.count(stage);
            assertTrue(active>=5&&active<=8);
            count.add(active);
            Set<String> points=new HashSet<>();
            for(int i=0;i<active;i++){
                float x=TowerPadLayout.x(stage,i),y=TowerPadLayout.y(stage,i);
                assertTrue("pad inside map "+i,x>100&&x<1800);
                assertTrue("pad inside map "+i,y>160&&y<820);
                assertTrue("real extra pad off enemy lane: stage "+stage,
                        TowerPadLayout.clearOfRoad(stage,i));
                assertTrue("distinct real pads",points.add(Math.round(x)+"/"+Math.round(y)));
            }
        }
        assertEquals(4,count.size()); // 5, 6, 7, 8 live pads
        assertEquals(5,TowerPadLayout.count(0));
        assertEquals(6,TowerPadLayout.count(1));
        assertEquals(7,TowerPadLayout.count(2));
        assertEquals(8,TowerPadLayout.count(152));
        assertThrows(IllegalArgumentException.class,()->TowerPadLayout.x(0,7));
    }
    @Test public void nativeBattleActuallyBuildsAllExtraPadsAndResetsPerStage()
            throws Exception {
        RiftGameView g=game(1);
        start(g,0);
        assertEquals(5,read(g,"activeTowerCount"));
        int[] tower=(int[])read(g,"towers");
        float[] x=(float[])read(g,"towerX");
        float[] y=(float[])read(g,"towerY");
        assertEquals(TowerPadLayout.x(0,4),x[4],.001f);
        assertEquals(TowerPadLayout.y(0,4),y[4],.001f);
        set(g,"gold",4000);
        for(int i=0;i<4;i++)call(g,"build",new Class<?>[0]);
        for(int i=0;i<5;i++)assertEquals("pad "+i+" is actually constructed",1,tower[i]);
        for(int i=5;i<8;i++)assertEquals("inactive turret",0,tower[i]);
        Bitmap image=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        g.draw(new Canvas(image));
        assertNotEquals(Color.TRANSPARENT,image.getPixel(900,370));
        image.recycle();
        start(g,152);
        assertEquals(8,read(g,"activeTowerCount"));
        tower=(int[])read(g,"towers");
        assertEquals(0,tower[7]);
        assertTrue(TowerPadLayout.clearOfRoad(152,7));
    }
    @Test public void heroDodgeMovesWithCollisionAndStopsSpamDuringCooldown()
            throws Exception {
        RiftGameView g=game(1);
        start(g,0);
        set(g,"axisX",1f);set(g,"axisY",0f);
        float before=(Float)read(g,"heroX");
        call(g,"triggerDodge",new Class<?>[0]);
        assertTrue((Float)read(g,"dodgeTime")>0);
        assertTrue((Float)read(g,"dodgeCooldown")>2f);
        assertEquals(WardenAnimationMachine.State.DODGE,
                ((WardenAnimationMachine)read(g,"heroAnimation")).state());
        call(g,"update",new Class<?>[]{float.class},.08f);
        assertTrue("dash must physically move Warden",(Float)read(g,"heroX")>before+35f);
        float cooldown=(Float)read(g,"dodgeCooldown");
        call(g,"triggerDodge",new Class<?>[0]);
        assertEquals(cooldown,(Float)read(g,"dodgeCooldown"),.001f);
        assertTrue("invulnerable window is active",
                DodgeRules.invulnerable((Float)read(g,"dodgeTime")));
        call(g,"update",new Class<?>[]{float.class},.20f);
        assertEquals(0,(Float)read(g,"dodgeTime"),.001f);
        assertTrue("cooldown prevents immediate repeat",
                (Float)read(g,"dodgeCooldown")>0);
    }
    @Test public void threeBossPhasesSummonRealEnemiesWithoutIteratorCrash()
            throws Exception {
        int finale=Campaign.stage(10,2);
        RiftGameView g=game(5);
        start(g,finale);
        set(g,"wave",DifficultyRules.waves(finale,DifficultyRules.NORMAL));
        set(g,"remaining",1);
        call(g,"spawnEnemy",new Class<?>[0]);
        @SuppressWarnings("unchecked")
        List<Object> foes=(List<Object>)read(g,"enemies");
        assertEquals(1,foes.size());
        Object boss=foes.get(0);
        assertTrue((Boolean)read(boss,"boss"));
        float hp=(Float)read(boss,"maxHp");
        set(boss,"hp",hp*.26f);
        // Force the documented initial cooldown to expire so this test
        // verifies the *actual* telegraph rather than asserting an early cast.
        set(boss,"specialTimer",0f);
        set(boss,"x",1150f);set(boss,"y",510f);
        call(g,"update",new Class<?>[]{float.class},.04f);
        assertEquals(3,read(boss,"bossPhase"));
        assertTrue("boss must spawn real reinforcements",foes.size()>1);
        assertTrue("telegraphed new boss attack", (Float)read(boss,"specialCharge")>0);
        for(int i=1;i<foes.size();i++)
            assertTrue("summoned enemy has real HP",(Float)read(foes.get(i),"hp")>0);
    }
    @Test public void missionABCObjectivesChangeGenuineRewardsAndCriteria(){
        assertFalse(MissionObjectives.completed(0,0,1,1,false));
        assertTrue(MissionObjectives.completed(0,2,1,1,false));
        assertFalse(MissionObjectives.completed(1,0,.60f,1,false));
        assertTrue(MissionObjectives.completed(1,0,.80f,.5f,false));
        assertFalse(MissionObjectives.completed(2,2,1,.8f,false));
        assertTrue(MissionObjectives.completed(2,0,1,.8f,true));
        assertTrue(MissionObjectives.bonus(299)>MissionObjectives.bonus(0));
        for(int stage=0;stage<300;stage++){
            assertNotNull(MissionObjectives.name(stage));
            assertFalse(MissionObjectives.status(stage,0,1,1,false).isEmpty());
        }
    }
    @Test public void directionalDodgeMaintainsDeterministicWeaponAnimation(){
        WardenAnimationMachine machine=new WardenAnimationMachine();
        assertTrue(machine.attack(1));
        assertTrue(machine.dodge());
        assertTrue(machine.locked());
        assertFalse(machine.attack(1)); // no attack while rolling
        // Animation clock limits per-frame dt to 200 ms; complete in two frames.
        machine.advance(.12f,true);
        machine.advance(.10f,true);
        assertFalse(machine.locked());
        assertFalse(machine.consume(WardenAnimationMachine.Event.ATTACK_CONTACT));
        assertEquals(3,BossTactics.phase(.26f,1f));
        assertEquals(2,BossTactics.phase(.5f,1f));
        assertEquals(1,BossTactics.phase(.8f,1f));
        for(int stage=0;stage<300;stage++){
            assertTrue(BossTactics.chargeTime(stage,3)>0);
            assertTrue(BossTactics.interval(stage,3)>3.0f);
            assertTrue(BossTactics.damageMultiplier(3)>BossTactics.damageMultiplier(1));
        }
    }
}
