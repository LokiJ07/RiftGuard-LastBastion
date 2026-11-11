package com.lokij07.riftguard;

import org.junit.Test;
import static org.junit.Assert.*;

public final class MovementRulesTest {
    @Test public void joystickMovesInEveryDirection() {
        assertEquals(420f,MovementRules.heroNextX(400,700,20),.001f);
        assertEquals(380f,MovementRules.heroNextX(400,700,-20),.001f);
        assertEquals(720f,MovementRules.heroNextY(400,700,20),.001f);
        assertEquals(680f,MovementRules.heroNextY(400,700,-20),.001f);
    }
    @Test public void cameraWorldBoundsNeverLetHeroEscape() {
        assertEquals(35f,MovementRules.heroNextX(35,700,-100),.001f);
        assertEquals(1865f,MovementRules.heroNextX(1865,700,100),.001f);
        assertEquals(35f,MovementRules.heroNextY(500,35,-100),.001f);
        assertEquals(1015f,MovementRules.heroNextY(500,1015,100),.001f);
    }
    @Test public void slidingAlongSolidWallKeepsValidAxis() {
        float x=MovementRules.CORE_X-178;
        float y=MovementRules.CORE_Y+85;
        assertEquals(x,MovementRules.heroNextX(x,y,25),.001f);
        assertTrue(MovementRules.heroNextY(x,y,28)>y);
    }
    @Test public void southernGateAllowsEntryButOtherWallsDoNot() {
        assertFalse(MovementRules.blocksHero(950,617));
        assertFalse(MovementRules.blocksHero(950,590));
        assertTrue(MovementRules.blocksHero(1060,617));
        assertTrue(MovementRules.blocksHero(790,510));
    }
    @Test public void centralKeepCannotBeGhostedThrough() {
        assertTrue(MovementRules.blocksHero(950,510));
        assertTrue(MovementRules.blocksHero(1000,550));
        assertTrue(MovementRules.blocksHero(840,510));
        assertFalse(MovementRules.blocksHero(850,510));
    }
    @Test public void meleeMonstersActuallyChaseTheHero() {
        assertTrue(MovementRules.huntsHero(130,0));
        assertEquals(35f,MovementRules.enemyStopDistance(true,0),.01f);
        assertFalse(MovementRules.huntsHero(500,0));
        assertEquals(156f,MovementRules.enemyStopDistance(false,0),.01f);
        assertFalse(MovementRules.huntsHero(100,5));
        assertEquals(250f,MovementRules.enemyStopDistance(false,5),.01f);
    }
}
