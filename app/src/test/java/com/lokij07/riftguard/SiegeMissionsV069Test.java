package com.lokij07.riftguard;
import org.junit.Test;
import static org.junit.Assert.*;

/** Pure invariants: mission cards, mechanics and the original population. */
public class SiegeMissionsV069Test {
    @Test public void threeMissionsHaveDistinctRealWaveTwoRules(){
        assertEquals(SiegeMissionsV069.NONE,SiegeMissionsV069.mode(5));
        assertEquals(SiegeMissionsV069.ESCORT,SiegeMissionsV069.mode(6));
        assertEquals(SiegeMissionsV069.ENGINE,SiegeMissionsV069.mode(7));
        assertEquals(SiegeMissionsV069.SPLIT,SiegeMissionsV069.mode(8));
        assertEquals(SiegeMissionsV069.ESCORT,SiegeMissionsV069.mode(18));
        assertEquals(SiegeMissionsV069.NONE,SiegeMissionsV069.mode(21));
        int[] seen=new int[4];
        for(int stage=0;stage<Campaign.STAGES;stage++){
            int mode=SiegeMissionsV069.mode(stage);
            seen[mode]++;
            assertEquals(mode!=0,SiegeMissionsV069.active(stage,2));
            assertFalse(SiegeMissionsV069.active(stage,1));
            assertFalse(SiegeMissionsV069.active(stage,3));
            if(mode!=0){
                assertTrue(SiegeMissionsV069.name(stage).length()>14);
                assertTrue(SiegeMissionsV069.orders(stage).length()>45);
                assertTrue(SiegeMissionsV069.warning(stage).length()>30);
                assertTrue(SiegeMissionsV069.bonusGold(mode)>0);
                MissionStory.Briefing brief=MissionStory.forStage(stage);
                assertEquals(SiegeMissionsV069.orders(stage),brief.order);
            }
        }
        assertEquals(32,seen[SiegeMissionsV069.ESCORT]);
        assertEquals(32,seen[SiegeMissionsV069.ENGINE]);
        assertEquals(32,seen[SiegeMissionsV069.SPLIT]);
        assertThrows(IllegalArgumentException.class,()->SiegeMissionsV069.mode(300));
    }
    @Test public void siegeEngineReplacesOnlyOneNormalWaveSpawn(){
        for(int stage=0;stage<Campaign.STAGES;stage++){
            for(int mode=DifficultyRules.EASY;mode<=DifficultyRules.EXPERT;mode++){
                int count=DifficultyRules.enemies(stage,2,mode);
                int selected=0;
                for(int remaining=count;remaining>=1;remaining--)
                    if(SiegeMissionsV069.engineSpawn(stage,2,remaining,count))selected++;
                assertEquals(SiegeMissionsV069.mode(stage)==SiegeMissionsV069.ENGINE?1:0,
                        selected);
                assertFalse(SiegeMissionsV069.engineSpawn(stage,3,count/2,count));
            }
        }
    }
    @Test public void flankersUseTwoPathsAndConvergeAtTheFortress(){
        assertFalse(SiegeMissionsV069.flankSpawn(7,2,6));
        assertTrue(SiegeMissionsV069.flankSpawn(8,2,6));
        assertTrue(SiegeMissionsV069.flankSpawn(8,2,3));
        assertFalse(SiegeMissionsV069.flankSpawn(8,2,5));
        assertFalse(SiegeMissionsV069.flankSpawn(8,3,6));
        assertEquals(0f,SiegeMissionsV069.flankOffset(LaneRules.CASTLE_GATE_X,1),.001f);
        assertTrue(SiegeMissionsV069.flankOffset(1800,1)>110f);
        assertTrue(SiegeMissionsV069.flankOffset(1800,-1)<-110f);
        assertThrows(IllegalArgumentException.class,
                ()->SiegeMissionsV069.flankOffset(1400,0));
    }
    @Test public void convoyProgressIsBoundedAndFrameStepIndependent(){
        float x=SiegeMissionsV069.CART_START_X;
        for(int i=0;i<2000;i++)x=SiegeMissionsV069.cartNext(x,.016f);
        assertTrue(SiegeMissionsV069.cartSafe(x));
        assertEquals(SiegeMissionsV069.CART_SAFE_X,x,.001f);
        assertEquals(SiegeMissionsV069.CART_SAFE_X,
                SiegeMissionsV069.cartNext(SiegeMissionsV069.CART_START_X,100f),.001f);
        assertThrows(IllegalArgumentException.class,
                ()->SiegeMissionsV069.cartNext(1400,-1f));
    }
}
