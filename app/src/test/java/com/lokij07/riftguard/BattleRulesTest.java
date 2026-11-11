package com.lokij07.riftguard;

import org.junit.Test;
import static org.junit.Assert.*;

public final class BattleRulesTest {
    @Test public void towersBecomeMoreExpensiveAndStronger() {
        for (int i=1; i<BattleRules.MAX_TOWER_TIER; i++) {
            assertTrue(BattleRules.towerCost(i) > BattleRules.towerCost(i-1));
            assertTrue(BattleRules.towerDamage(i) > BattleRules.towerDamage(i-1));
        }
    }
    @Test public void enemiesScaleWithCampaign() {
        for (int type=0; type<6; type++) {
            assertTrue(BattleRules.enemyHealth(299,type)>BattleRules.enemyHealth(0,type));
            assertTrue(BattleRules.enemySpeed(type)>0);
        }
    }
    @Test public void clampIsBounded() {
        assertEquals(0, BattleRules.clamp(-4, 0, 10), .001f);
        assertEquals(10, BattleRules.clamp(42, 0, 10), .001f);
    }
}
