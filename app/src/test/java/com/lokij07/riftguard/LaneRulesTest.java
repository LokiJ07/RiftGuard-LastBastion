package com.lokij07.riftguard;

import org.junit.Test;
import static org.junit.Assert.*;

/** Stage-wide acceptance tests for the single-route defensive battle geometry. */
public final class LaneRulesTest {
    @Test public void allThreeStagesHaveOneRightToLeftApproach() {
        for(int map=0;map<3;map++) {
            assertTrue(LaneRules.ENEMY_SPAWN_X>LaneRules.waypointX(map));
            assertTrue(LaneRules.waypointX(map)>LaneRules.CASTLE_GATE_X-2);
            assertEquals(950f,LaneRules.targetX(1000,map),.001f);
            assertTrue(LaneRules.targetX(1800,map)<1800f);
            assertEquals(510f,LaneRules.pathY(map,950f),.001f);
            for(int i=-58;i<=58;i+=29) {
                float sy=LaneRules.spawnY(map,i);
                assertTrue(sy>300&&sy<755);
            }
        }
    }
    @Test public void heroStaysWithinVisibleCombatBand() {
        assertEquals(LaneRules.HERO_MIN_X,LaneRules.heroX(-1000),0);
        assertEquals(LaneRules.HERO_MAX_X,LaneRules.heroX(99999),0);
        assertEquals(LaneRules.HERO_MIN_Y,LaneRules.heroY(-1000),0);
        assertEquals(LaneRules.HERO_MAX_Y,LaneRules.heroY(99999),0);
        assertEquals(LaneRules.startX(),LaneRules.heroX(LaneRules.startX()),0);
        assertEquals(LaneRules.startY(),LaneRules.heroY(LaneRules.startY()),0);
    }
    @Test public void eachBiomeMapsIntoTheSameThreeDefensibleRoutes() {
        for(int stage=0;stage<Campaign.STAGES;stage++){
            int map=Campaign.map(stage);
            assertTrue(LaneRules.coversPath(map,1550f,LaneRules.pathY(map,1550)));
            assertFalse(LaneRules.coversPath(map,450,520));
        }
    }
}
