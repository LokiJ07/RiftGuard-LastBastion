package com.lokij07.riftguard;

import org.junit.Test;
import static org.junit.Assert.*;

/** Every permanent oath unlock, capstone, and anti-farming boundary. */
public class WardenMilestonesV079Test {
    @Test public void milestonesOnlyUnlockAtTheirNamedLevel(){
        assertEquals(6,WardenMilestonesV079.LEVELS.length);
        assertEquals(-1,WardenMilestonesV079.currentIndex(1));
        for(int i=0;i<WardenMilestonesV079.LEVELS.length;i++){
            int at=WardenMilestonesV079.LEVELS[i];
            assertEquals(i,WardenMilestonesV079.nextIndex(at-1));
            assertEquals(i,WardenMilestonesV079.currentIndex(at));
            assertEquals(i,WardenMilestonesV079.latestUnlocked(at-1,at));
            assertEquals(-1,WardenMilestonesV079.latestUnlocked(at,at));
            assertEquals(i,WardenMilestonesV079.currentIndex(Math.min(100,at+1)));
        }
        assertEquals(-1,WardenMilestonesV079.nextIndex(100));
        assertEquals(5,WardenMilestonesV079.latestUnlocked(1,100));
        assertEquals(-1,WardenMilestonesV079.latestUnlocked(100,100));
    }

    @Test public void perksRemainSmallAndCharacterOnly(){
        for(int lv=1;lv<=100;lv++){
            assertEquals(lv>=10?12:0,WardenMilestonesV079.healthBonus(lv));
            assertEquals(lv>=40?(lv>=100?.10f:.04f):0f,
                    WardenMilestonesV079.attackBonus(lv),.0001f);
            assertEquals(lv>=80?.04f:0f,WardenMilestonesV079.moveBonus(lv),.0001f);
            assertEquals(lv>=60?.20f:0f,
                    WardenMilestonesV079.cooldownBonus(lv),.0001f);
        }
    }

    @Test public void siphonOnlyCreditsEveryEightKillsAfterLevel25(){
        for(int lv:new int[]{1,24,25,40,80,100}){
            for(int kills=0;kills<=300;kills++){
                int expected=lv>=25&&kills>0&&kills%8==0?6:0;
                assertEquals("lv "+lv+", kills "+kills,expected,
                        WardenMilestonesV079.healOnCreditedKill(lv,kills));
            }
        }
        assertEquals(0,WardenMilestonesV079.healOnCreditedKill(25,-8));
    }
}
