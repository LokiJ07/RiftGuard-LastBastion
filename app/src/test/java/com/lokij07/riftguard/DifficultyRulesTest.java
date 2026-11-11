package com.lokij07.riftguard;
import org.junit.Test;
import static org.junit.Assert.*;

public final class DifficultyRulesTest {
    @Test public void selectableDifficultyChangesRealCombat() {
        assertTrue(DifficultyRules.health(0)<DifficultyRules.health(1));
        assertTrue(DifficultyRules.health(3)>DifficultyRules.health(2));
        assertTrue(DifficultyRules.enemyDamage(3)>DifficultyRules.enemyDamage(1));
        assertTrue(DifficultyRules.enemySpeed(3)>DifficultyRules.enemySpeed(1));
        assertTrue(DifficultyRules.waves(2,3)>DifficultyRules.waves(2,1));
        assertTrue(DifficultyRules.enemies(2,1,3)>DifficultyRules.enemies(2,1,1));
        assertEquals(DifficultyRules.NORMAL,DifficultyRules.safe(1));
        assertEquals(DifficultyRules.EXPERT,DifficultyRules.safe(99));
    }
    @Test public void tenBossFinalesAndAllMapsAreValid() {
        for(int world=0;world<10;world++){
            int stage=Campaign.stage((world+1)*10,2);
            assertTrue(DifficultyRules.worldBoss(stage));
            assertTrue(DifficultyRules.finalBossMultiplier(stage)>=10);
            assertEquals(world,DifficultyRules.worldBossName(stage).equals("")?-1:Campaign.world(stage));
            assertTrue(DifficultyRules.eliteFinalWave(stage,
                      DifficultyRules.waves(stage,1),1));
        }
        for(int stage=0;stage<Campaign.STAGES;stage++) {
            assertTrue(StageFlavor.props(stage)>=4);
            assertTrue(StageFlavor.landmarkX(stage,0)>=1160);
            assertTrue(StageFlavor.landmarkX(stage,0)<=1700);
            if(Campaign.map(stage)!=2||Campaign.level(stage)%10!=0)
                assertFalse(DifficultyRules.worldBoss(stage));
        }
        assertNotEquals(StageFlavor.signature(0),StageFlavor.signature(3));
    }
}
