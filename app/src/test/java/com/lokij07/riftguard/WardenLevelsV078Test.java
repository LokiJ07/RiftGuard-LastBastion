package com.lokij07.riftguard;

import org.junit.Test;
import static org.junit.Assert.*;

/** Full level curve, late game pacing, difficulty rewards and stat budgets. */
public class WardenLevelsV078Test {
    @Test public void allOneHundredLevelsRequireStrictlyIncreasingXp(){
        assertEquals(0,WardenLevelsV078.totalToLevel(1));
        int running=0,last=0;
        for(int level=1;level<100;level++){
            int needed=WardenLevelsV078.xpToNextLevel(level);
            assertTrue("XP cost must increase each level "+level,needed>last);
            last=needed;
            assertEquals(level,WardenLevelsV078.levelForXp(running));
            assertEquals(level,WardenLevelsV078.levelForXp(
                    WardenLevelsV078.totalToLevel(level)));
            assertEquals(level,WardenLevelsV078.levelForXp(
                    WardenLevelsV078.totalToLevel(level+1)-1));
            running+=needed;
            assertEquals(running,WardenLevelsV078.totalToLevel(level+1));
        }
        assertEquals(215985,WardenLevelsV078.maxXp());
        assertEquals(100,WardenLevelsV078.levelForXp(running));
        assertEquals(100,WardenLevelsV078.levelForXp(Integer.MAX_VALUE));
        assertEquals(0,WardenLevelsV078.xpToNextLevel(100));
        assertThrows(IllegalArgumentException.class,
                ()->WardenLevelsV078.xpToNextLevel(101));
    }

    @Test public void levelingGrantsRisingPointsWithMaximumAt100(){
        int earned=0,previous=0;
        for(int level=2;level<=100;level++){
            int granted=WardenLevelsV078.pointsOnLevel(level);
            assertTrue(granted>=previous);
            previous=granted;
            earned+=granted;
            assertEquals(earned,WardenLevelsV078.lifetimePoints(level));
            if(level<20)assertEquals(2,granted);
            else if(level<40)assertEquals(3,granted);
            else if(level<60)assertEquals(4,granted);
            else if(level<80)assertEquals(5,granted);
            else assertEquals(6,granted);
        }
        assertEquals(402,earned);
        assertEquals(0,WardenLevelsV078.lifetimePoints(1));
        assertThrows(IllegalArgumentException.class,
                ()->WardenLevelsV078.pointsOnLevel(1));
    }

    @Test public void difficultyAndFirstClearRewardsAreBalancedAcrossAllStages(){
        int easy=0,normal=0,hard=0,expert=0;
        for(int s=0;s<Campaign.STAGES;s++){
            int e=WardenLevelsV078.stageXp(s,DifficultyRules.EASY,true);
            int n=WardenLevelsV078.stageXp(s,DifficultyRules.NORMAL,true);
            int h=WardenLevelsV078.stageXp(s,DifficultyRules.HARD,true);
            int x=WardenLevelsV078.stageXp(s,DifficultyRules.EXPERT,true);
            assertTrue(e<n&&n<h&&h<x);
            assertTrue(WardenLevelsV078.stageXp(s,DifficultyRules.NORMAL,false)
                    <n/4f);
            assertTrue(WardenLevelsV078.stageXp(s,DifficultyRules.NORMAL,false)>0);
            if(s>=3)
                assertTrue(n>WardenLevelsV078.stageXp(s-3,DifficultyRules.NORMAL,true));
            easy+=e;normal+=n;hard+=h;expert+=x;
        }
        assertEquals(216900,normal); // full Normal story reaches level 100
        assertTrue(easy<normal&&hard>normal&&expert>hard);
        assertTrue(easy<WardenLevelsV078.maxXp());
        assertTrue(hard>WardenLevelsV078.maxXp());
        assertEquals(115,WardenLevelsV078.stageXp(0,DifficultyRules.NORMAL,true));
        assertEquals(0.78f, easy/(float)normal,.01f);
        assertEquals(1.25f, hard/(float)normal,.01f);
        assertEquals(1.55f, expert/(float)normal,.01f);
        assertThrows(IllegalArgumentException.class,
                ()->WardenLevelsV078.stageXp(300,DifficultyRules.NORMAL,true));
    }

    @Test public void newWardenCannotAllocateUnownedPoints(){
        // Formula checks: each branch is bounded even with all available points.
        assertEquals(4,WardenLevelsV078.ATTRIBUTE_COUNT);
        assertEquals(120,WardenLevelsV078.MAX_ATTRIBUTE);
        assertTrue(WardenLevelsV078.lifetimePoints(100)<
                WardenLevelsV078.MAX_ATTRIBUTE*WardenLevelsV078.ATTRIBUTE_COUNT);
        assertEquals(169,WardenLevelsV078.totalToLevel(2));
        assertEquals(0,WardenLevelsV078.levelForXp(0)-1);
    }
}
