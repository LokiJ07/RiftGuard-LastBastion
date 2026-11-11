package com.lokij07.riftguard;

import org.junit.Test;
import static org.junit.Assert.*;

public final class ProgressionTest {
    @Test public void fortressUpgradesUnlockUsableWeapons() {
        assertTrue(Progression.canEquip(1,0));
        assertTrue(Progression.canEquip(1,1));
        for(int rank=2;rank<=5;rank++){
            assertFalse(Progression.canEquip(rank-1,rank));
            assertTrue(Progression.canEquip(rank,rank));
        }
        assertFalse(Progression.canEquip(5,-1));
        assertFalse(Progression.canEquip(5,6));
    }
    @Test public void firstVictoryCanBuySecondFortressTier() {
        int earned=Progression.stageReward(0,true);
        assertEquals(120,earned);
        assertTrue(Progression.canUpgrade(1,earned));
        assertFalse(Progression.canUpgrade(2,earned-Progression.upgradeCost(1)));
        assertFalse(Progression.canUpgrade(5,100000));
    }
    @Test public void upgradedCoreHasMoreHealth() {
        for(int rank=2;rank<=5;rank++)
            assertTrue(Progression.maxCoreHp(rank)>Progression.maxCoreHp(rank-1));
    }
    @Test public void weaponsActuallyDifferInCombat() {
        assertTrue(Progression.WEAPONS[1].range<Progression.WEAPONS[0].range);
        assertTrue(Progression.WEAPONS[4].cooldown<Progression.WEAPONS[0].cooldown);
        assertTrue(Progression.WEAPONS[5].damage>Progression.WEAPONS[0].damage);
    }
}
