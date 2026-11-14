package com.lokij07.riftguard;

import org.junit.Test;
import static org.junit.Assert.*;

public final class BossCounterplayV083Test {
    @Test public void allTenKingdomsHaveAchievableFinitePressureBudgets(){
        for(int world=0;world<10;world++){
            int stage=Campaign.stage((world+1)*10,2);
            assertTrue(DifficultyRules.worldBoss(stage));
            for(int phase=1;phase<=3;phase++){
                float required=BossCounterplayV083.pressureRequired(
                        stage,phase,world==0?1200f:8000f);
                assertTrue(required>=48f);
                assertTrue(required<=174f);
                assertFalse(BossCounterplayV083.interrupted(required-.1f,required));
                assertTrue(BossCounterplayV083.interrupted(required,required));
                assertEquals(0f,BossCounterplayV083.fraction(0f,required),.0001f);
                assertEquals(1f,BossCounterplayV083.fraction(required*2,required),.0001f);
                assertTrue(BossCounterplayV083.prompt(stage).length()>15);
            }
        }
    }

    @Test public void invalidPressureNeverInterrupts(){
        assertFalse(BossCounterplayV083.interrupted(Float.NaN,40f));
        assertFalse(BossCounterplayV083.interrupted(50f,Float.NaN));
        assertFalse(BossCounterplayV083.interrupted(50f,0f));
        assertEquals(0f,BossCounterplayV083.fraction(Float.NaN,20f),.001f);
        assertEquals(0f,BossCounterplayV083.fraction(20f,0f),.001f);
        try{
            BossCounterplayV083.pressureRequired(30,1,Float.NaN);
            fail("Invalid HP must be rejected");
        }catch(IllegalArgumentException expected){}
    }

    @Test public void optionalFirstClearBossRewardNeverBecomesReplayFarm(){
        for(int world=0;world<10;world++){
            int boss=Campaign.stage((world+1)*10,2);
            int reward=19+world*2;
            assertEquals(0,BossCounterplayV083.firstClearMastery(boss,0,true));
            assertEquals(reward,BossCounterplayV083.firstClearMastery(boss,1,true));
            assertEquals(reward*2,BossCounterplayV083.firstClearMastery(boss,2,true));
            assertEquals(reward*2,BossCounterplayV083.firstClearMastery(boss,100,true));
            assertEquals(0,BossCounterplayV083.firstClearMastery(boss,2,false));
            assertEquals(0,BossCounterplayV083.firstClearMastery(
                    Campaign.stage(world*10+1,2),9,true));
        }
    }
}
