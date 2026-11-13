package com.lokij07.riftguard;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * The hot-path allocation optimization is allowed to change only how data is
 * stored, NEVER enemy speed, HP, gold, damage, or any part of the painted art.
 */
public final class CombatPerformanceV067Test {
    private static final float[] EXPECTED_SPEED={57,93,34,77,52,46,83};
    private static final float[] EXPECTED_HEALTH={1f,.63f,3.1f,.88f,1.45f,2f,1.8f};
    @Test public void everyEnemyTypeKeepsTheExactPreviousSpeedAndHp(){
        for(int kind=-14;kind<=20;kind++){
            int i=Math.floorMod(kind,7);
            assertEquals(EXPECTED_SPEED[i],BattleRules.enemySpeed(kind),.00001f);
            for(int stage=0;stage<Campaign.STAGES;stage+=13){
                float old=25f*(1f+stage*.029f)*EXPECTED_HEALTH[i];
                assertEquals(old,BattleRules.enemyHealth(stage,kind),.0001f);
            }
        }
    }
    @Test public void highDensityCombatLookupRemainsStateless(){
        for(int n=0;n<30000;n++){
            int kind=n%7;
            assertEquals(EXPECTED_SPEED[kind],BattleRules.enemySpeed(kind),0);
        }
        assertEquals(6,Progression.WEAPONS.length);
        assertEquals(5,BattleRules.MAX_TOWER_TIER);
    }
}
