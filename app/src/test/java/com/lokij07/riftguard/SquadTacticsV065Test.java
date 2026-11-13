package com.lokij07.riftguard;

import org.junit.Test;
import static org.junit.Assert.*;

public final class SquadTacticsV065Test {
    @Test public void allThreeHundredStagesProduceStableNamedWaveOrders(){
        for(int stage=0;stage<300;stage++){
            int plan=SquadTacticsV065.plan(stage,3,5,DifficultyRules.NORMAL);
            assertTrue(plan>=0&&plan<4);
            assertEquals(plan,SquadTacticsV065.plan(stage,3,5,DifficultyRules.NORMAL));
            assertFalse(SquadTacticsV065.name(plan).isEmpty());
        }
    }
    @Test public void commandersChangeTeamSpeedAndArmorUntilEliminated(){
        float before=SquadTacticsV065.speed(SquadTacticsV065.RAIDER,
                SquadTacticsV065.FLANK,false);
        float after=SquadTacticsV065.speed(SquadTacticsV065.RAIDER,
                SquadTacticsV065.FLANK,true);
        assertTrue(after>before);
        float shield=SquadTacticsV065.incomingDamage(
                SquadTacticsV065.SHIELD,SquadTacticsV065.SHIELD_WALL,true);
        assertTrue(shield<1f);
        assertTrue(shield<SquadTacticsV065.incomingDamage(
                SquadTacticsV065.SHIELD,SquadTacticsV065.SHIELD_WALL,false));
    }
    @Test public void assaultRolesAndSiegeOrdersHaveActualCombatEffects(){
        assertEquals(SquadTacticsV065.COMMANDER,SquadTacticsV065.role(1,true,0));
        assertEquals(SquadTacticsV065.SHIELD,SquadTacticsV065.role(2,false,4));
        assertTrue(SquadTacticsV065.attacksTowers(SquadTacticsV065.SAPPER,
                SquadTacticsV065.FLANK));
        assertTrue(SquadTacticsV065.attacksTowers(SquadTacticsV065.RAIDER,
                SquadTacticsV065.SIEGE));
        assertFalse(SquadTacticsV065.attacksTowers(SquadTacticsV065.RAIDER,
                SquadTacticsV065.SHIELD_WALL));
        assertTrue(SquadTacticsV065.towerAttackMultiplier(SquadTacticsV065.SAPPER,
                SquadTacticsV065.SIEGE,true)>1f);
    }
    @Test public void finalBossWaveForcesCommanderAssaultWhenNotSiegeHeavy(){
        int stage=Campaign.stage(10,2),waves=DifficultyRules.waves(stage,DifficultyRules.NORMAL);
        assertEquals(SquadTacticsV065.COMMAND_PUSH,
                SquadTacticsV065.plan(stage,waves,1,DifficultyRules.NORMAL));
    }
}
