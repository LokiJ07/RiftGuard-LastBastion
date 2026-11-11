package com.lokij07.riftguard;

import org.junit.Test;
import static org.junit.Assert.*;

public final class CampaignTest {
    @Test public void allThreeHundredStagesHaveValidAddresses() {
        assertEquals(300, Campaign.STAGES);
        for (int s = 0; s < Campaign.STAGES; s++) {
            assertEquals(s, Campaign.stage(Campaign.level(s), Campaign.map(s)));
            assertTrue(Campaign.world(s) >= 0 && Campaign.world(s) < 10);
            assertEquals(Campaign.seed(s), Campaign.seed(s));
        }
        assertEquals(100, Campaign.level(297));
        assertEquals(100, Campaign.level(299));
    }
    @Test public void progressionCannotSkipStages() {
        assertTrue(Campaign.accessible(0, 0));
        assertFalse(Campaign.accessible(1, 0));
        assertEquals(0, Campaign.unlockedAfter(0, 2));
        assertEquals(1, Campaign.unlockedAfter(0, 0));
        assertEquals(2, Campaign.unlockedAfter(1, 1));
        assertEquals(300, Campaign.unlockedAfter(299, 299));
    }
    @Test(expected=IllegalArgumentException.class)
    public void invalidStageRejected() { Campaign.level(300); }
}
