package com.lokij07.riftguard;
import org.junit.Test;
import static org.junit.Assert.*;
public class EnemyCoordinationV075Test {
    @Test public void focusChoosesDangerousRealBuiltTowerAndReassigns(){
        int[] tier={1,3,2,0,0,0,0,0};
        float[] hp={TowerDurability.maxHp(1,5),TowerDurability.maxHp(3,5),
                TowerDurability.maxHp(2,5),0,0,0,0,0};
        assertEquals(1,EnemyCoordinationV075.focusTower(tier,hp,6,5));
        tier[1]=0;hp[1]=0;
        assertEquals(2,EnemyCoordinationV075.focusTower(tier,hp,6,5));
        tier[2]=0;hp[2]=0;
        assertEquals(0,EnemyCoordinationV075.focusTower(tier,hp,6,5));
        hp[0]=0;
        assertEquals(-1,EnemyCoordinationV075.focusTower(tier,hp,6,5));
        assertThrows(IllegalArgumentException.class,
                ()->EnemyCoordinationV075.focusTower(tier,hp,9,5));
    }
    @Test public void raidRequiresRealSiegeOrderAndCanBeIntercepted(){
        for(int stage=0;stage<300;stage++){
            assertEquals(stage>=30,EnemyCoordinationV075.raidActive(stage,3,
                    SquadTacticsV065.SIEGE,false,false,1));
            assertFalse(EnemyCoordinationV075.raidActive(stage,1,
                    SquadTacticsV065.SIEGE,false,false,1));
            assertFalse(EnemyCoordinationV075.raidActive(stage,3,
                    SquadTacticsV065.SIEGE,true,false,1));
            assertFalse(EnemyCoordinationV075.raidActive(stage,3,
                    SquadTacticsV065.FLANK,false,false,1));
            assertFalse(EnemyCoordinationV075.raidActive(stage,3,
                    SquadTacticsV065.SIEGE,false,true,1));
        }
        for(int kind:new int[]{1,2,5})
            assertTrue(EnemyCoordinationV075.raidAttacker(kind,false,false,450));
        for(int kind:new int[]{0,3,4,6})
            assertFalse(EnemyCoordinationV075.raidAttacker(kind,false,false,450));
        assertFalse(EnemyCoordinationV075.raidAttacker(1,false,true,450));
        assertFalse(EnemyCoordinationV075.raidAttacker(1,true,false,450));
        assertFalse(EnemyCoordinationV075.raidAttacker(1,false,false,780));
        assertTrue(EnemyCoordinationV075.towerStrikeRange(1)>
                EnemyCoordinationV075.towerStop(1));
    }
    @Test public void shieldingAndRetreatHaveReadableCounterplay(){
        int s=42;
        assertTrue(EnemyCoordinationV075.protectiveShield(2,false,70,
                1440,530,1500,535));
        assertFalse(EnemyCoordinationV075.protectiveShield(2,false,0,
                1440,530,1500,535));
        assertFalse(EnemyCoordinationV075.protectiveShield(2,true,70,
                1440,530,1500,535));
        assertFalse(EnemyCoordinationV075.protectiveShield(2,false,70,
                1200,530,1500,535));
        assertTrue(EnemyCoordinationV075.shouldRetreat(s,4,false,
                39,100,180,true,1450,0));
        assertFalse(EnemyCoordinationV075.shouldRetreat(s,4,false,
                39,100,180,false,1450,0));
        assertFalse(EnemyCoordinationV075.shouldRetreat(s,4,false,
                39,100,180,true,1450,2.5f));
        assertFalse(EnemyCoordinationV075.shouldRetreat(s,4,false,
                60,100,180,true,1450,0));
        assertFalse(EnemyCoordinationV075.shouldRetreat(9,4,false,
                20,100,180,true,1450,0));
        assertTrue(EnemyCoordinationV075.retreatX(1420)>1420);
        assertTrue(EnemyCoordinationV075.retreatY(580,620)<580);
        assertTrue(EnemyCoordinationV075.retreatY(510,450)>510);
        assertTrue(EnemyCoordinationV075.RETREAT_SECONDS<2f);
        assertTrue(EnemyCoordinationV075.RETREAT_COOLDOWN>5f);
    }
    @Test public void pincerUsesExistingRaidersAndConvergesAtGate(){
        for(int stage=0;stage<300;stage++){
            assertEquals(stage<30?0:-1,EnemyCoordinationV075.pincerSide(
                    stage,2,SquadTacticsV065.FLANK,0,6,false));
            assertEquals(0,EnemyCoordinationV075.pincerSide(
                    stage,1,SquadTacticsV065.FLANK,0,6,false));
            assertEquals(0,EnemyCoordinationV075.pincerSide(
                    stage,2,SquadTacticsV065.SIEGE,0,6,false));
            assertEquals(0,EnemyCoordinationV075.pincerSide(
                    stage,2,SquadTacticsV065.FLANK,4,6,false));
            assertEquals(0,EnemyCoordinationV075.pincerSide(
                    stage,2,SquadTacticsV065.FLANK,0,6,true));
        }
        assertTrue(EnemyCoordinationV075.pincerOffset(1700,1)>0);
        assertTrue(EnemyCoordinationV075.pincerOffset(1700,-1)<0);
        assertEquals(0f,EnemyCoordinationV075.pincerOffset(
                LaneRules.CASTLE_GATE_X,1),.001f);
        assertEquals(0f,EnemyCoordinationV075.pincerOffset(1800,0),.001f);
        assertThrows(IllegalArgumentException.class,
                ()->EnemyCoordinationV075.pincerOffset(1800,2));
    }
}
