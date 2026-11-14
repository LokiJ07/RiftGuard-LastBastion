package com.lokij07.riftguard;
import org.junit.Test;
import static org.junit.Assert.*;

/** Every map remains valid while later realms add two real hero objectives. */
public class FieldObjectivesV074Test {
    @Test public void exactlyEighteenNewFieldMissionsWithinExistingNinetySix(){
        int[] perRealm=new int[10],counts=new int[6];
        for(int stage=0;stage<Campaign.STAGES;stage++){
            int mission=SiegeMissionsV069.mode(stage);
            assertTrue(mission>=SiegeMissionsV069.NONE&&mission<=SiegeMissionsV069.SEAL);
            perRealm[Campaign.world(stage)]+=mission==0?0:1;
            counts[mission]++;
            if(mission==SiegeMissionsV069.RESCUE||mission==SiegeMissionsV069.SEAL){
                assertTrue(stage>=30);
                assertFalse(DifficultyRules.worldBoss(stage));
                assertTrue(SiegeMissionsV069.orders(stage).contains("Wave two"));
                assertTrue(SiegeMissionsV069.name(stage).length()>20);
                assertFalse(SiegeMissionsV069.active(stage,1));
                assertTrue(SiegeMissionsV069.active(stage,2));
                assertFalse(SiegeMissionsV069.active(stage,3));
            }
        }
        assertEquals(204,counts[0]);
        assertEquals(26,counts[1]);assertEquals(26,counts[2]);assertEquals(26,counts[3]);
        assertEquals(9,counts[SiegeMissionsV069.RESCUE]);
        assertEquals(9,counts[SiegeMissionsV069.SEAL]);
        assertEquals(15,perRealm[0]);
        for(int realm=1;realm<10;realm++){
            assertEquals(9,perRealm[realm]);
            assertEquals(SiegeMissionsV069.RESCUE,SiegeMissionsV069.mode(realm*30+15));
            assertEquals(SiegeMissionsV069.SEAL,SiegeMissionsV069.mode(realm*30+24));
        }
        assertEquals(SiegeMissionsV069.ENGINE,SiegeMissionsV069.mode(32));
        assertEquals(SiegeMissionsV069.ESCORT,SiegeMissionsV069.mode(38));
    }
    @Test public void allPlayerObjectivesInsideActualWardenNavigationBounds(){
        for(int stage=0;stage<300;stage++){
            for(int i=0;i<FieldObjectivesV074.SURVIVORS;i++){
                float x=FieldObjectivesV074.rescueX(stage,i),
                        y=FieldObjectivesV074.rescueY(stage,i);
                assertTrue(x>=LaneRules.HERO_MIN_X&&x<=LaneRules.HERO_MAX_X);
                assertTrue(y>=LaneRules.HERO_MIN_Y&&y<=LaneRules.HERO_MAX_Y);
            }
            for(int i=0;i<FieldObjectivesV074.SEALS;i++){
                float x=FieldObjectivesV074.sealX(stage,i),
                        y=FieldObjectivesV074.sealY(stage,i);
                assertTrue(x>=LaneRules.HERO_MIN_X&&x<=LaneRules.HERO_MAX_X);
                assertTrue(y>=LaneRules.HERO_MIN_Y&&y<=LaneRules.HERO_MAX_Y);
                assertTrue(FieldObjectivesV074.near(x,y,x,y,
                        FieldObjectivesV074.SEAL_RADIUS));
            }
        }
        assertThrows(IllegalArgumentException.class,
                ()->FieldObjectivesV074.rescueX(40,3));
        assertThrows(IllegalArgumentException.class,
                ()->FieldObjectivesV074.sealY(40,-1));
        assertThrows(IllegalArgumentException.class,
                ()->FieldObjectivesV074.sealX(300,1));
    }
    @Test public void rescueLivesAndSealThreatsScaleWithoutOneShotPressure(){
        for(int stage=0;stage<300;stage++)for(int diff=0;diff<=3;diff++){
            assertTrue(FieldObjectivesV074.survivorMaxHp(stage,diff)>100);
            for(int kind=0;kind<7;kind++)assertTrue(
                    FieldObjectivesV074.survivorHit(stage,diff,kind)<
                    FieldObjectivesV074.survivorMaxHp(stage,diff)*.5f);
            assertTrue(FieldObjectivesV074.sealDamage(stage,diff,350f)<=22.751f);
            assertTrue(FieldObjectivesV074.sealDamage(stage,diff,350f)>0f);
        }
        assertTrue(FieldObjectivesV074.canAdvanceRescue(3,0));
        assertTrue(FieldObjectivesV074.canAdvanceRescue(2,1));
        assertFalse(FieldObjectivesV074.canAdvanceRescue(2,0));
        assertFalse(FieldObjectivesV074.canAdvanceRescue(1,2));
        assertFalse(FieldObjectivesV074.near(Float.NaN,3,4,5,100));
        assertEquals(FieldObjectivesV074.SEAL_SECONDS,
                FieldObjectivesV074.charge(1f,50f,true),.0001f);
        assertEquals(1f,FieldObjectivesV074.charge(1f,2f,false),.0001f);
        assertThrows(IllegalArgumentException.class,
                ()->FieldObjectivesV074.charge(1f,-1f,true));
    }
}
