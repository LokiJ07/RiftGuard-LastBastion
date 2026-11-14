package com.lokij07.riftguard;

import org.junit.Test;
import static org.junit.Assert.*;

public final class CombatFlowV082Test {
    @Test public void distinctLandedActionsCreateTemporaryBuff(){
        CombatFlowV082 flow=new CombatFlowV082();flow.reset();flow.beginWave(1);
        assertFalse(flow.onConfirmedAction(CombatFlowV082.FINISHER,1f));
        assertFalse("repeating the same move cannot farm a boost",
                flow.onConfirmedAction(CombatFlowV082.FINISHER,2f));
        assertEquals(1f,flow.weaponMultiplier(2f),.0001f);
        assertTrue(flow.onConfirmedAction(CombatFlowV082.CHARGED,4f));
        assertEquals(1.09f,flow.weaponMultiplier(4f),.0001f);
        assertEquals(7f,flow.remaining(4f),.0001f);
        assertEquals(1f,flow.weaponMultiplier(11f),.0001f);
        assertFalse(flow.onConfirmedAction(CombatFlowV082.EVADE,5f));
    }
    @Test public void timedOutPairCannotActivateAndDodgeRequiresOtherTechnique(){
        CombatFlowV082 f=new CombatFlowV082();f.reset();f.beginWave(1);
        assertFalse(f.onConfirmedAction(CombatFlowV082.EVADE,1f));
        assertFalse(f.onConfirmedAction(CombatFlowV082.CHARGED,12f));
        assertFalse(f.onConfirmedAction(CombatFlowV082.CHARGED,13f));
        assertTrue(f.onConfirmedAction(CombatFlowV082.FINISHER,15f));
        assertEquals(1.09f,f.weaponMultiplier(16f),.0001f);
    }
    @Test public void battleGoldIsOncePerWaveAndDoesNotGrowUnbounded(){
        CombatFlowV082 f=new CombatFlowV082();f.reset();
        assertEquals(0,f.awardBattleGold(50)); // cannot pay before battle start
        f.beginWave(1);
        assertEquals(14,f.awardBattleGold(0));
        assertEquals(0,f.awardBattleGold(0));
        f.beginWave(2);
        assertEquals(20,f.awardBattleGold(100)); // world 4
        assertEquals(0,f.awardBattleGold(299));
        f.beginWave(3);
        assertEquals(26,f.awardBattleGold(299)); // capped at 26
        assertEquals(0,f.awardBattleGold(299));
    }
    @Test public void cooldownCannotBeSkippedByAdvancingWaves(){
        CombatFlowV082 f=new CombatFlowV082();f.reset();f.beginWave(1);
        assertFalse(f.onConfirmedAction(CombatFlowV082.CHARGED,2f));
        assertTrue(f.onConfirmedAction(CombatFlowV082.EVADE,3f));
        f.beginWave(2);
        assertFalse(f.onConfirmedAction(CombatFlowV082.FINISHER,6f));
        assertFalse(f.onConfirmedAction(CombatFlowV082.CHARGED,7f));
        assertTrue(f.remaining(6f)>0);
        assertFalse(f.onConfirmedAction(CombatFlowV082.FINISHER,21f));
        assertTrue(f.onConfirmedAction(CombatFlowV082.CHARGED,22f));
        assertEquals(1.09f,f.weaponMultiplier(23f),.0001f);
    }
    @Test public void newBattleAndWaveRecoveryDoNotCarryUnrecordedBuff(){
        CombatFlowV082 f=new CombatFlowV082();f.reset();f.beginWave(5);
        f.onConfirmedAction(CombatFlowV082.FINISHER,1f);
        assertTrue(f.onConfirmedAction(CombatFlowV082.CHARGED,2f));
        assertTrue(f.weaponMultiplier(3f)>1f);
        f.reset();f.beginWave(5);
        assertEquals(1f,f.weaponMultiplier(3f),.0001f);
        assertEquals(0f,f.remaining(3f),.0001f);
        assertEquals(14,f.awardBattleGold(0));
    }
    @Test public void invalidAndNonFiniteActionsAreRejected(){
        CombatFlowV082 f=new CombatFlowV082();f.reset();f.beginWave(1);
        assertFalse(f.onConfirmedAction(0,2f));
        assertFalse(f.onConfirmedAction(99,2f));
        assertFalse(f.onConfirmedAction(CombatFlowV082.CHARGED,Float.NaN));
        assertFalse(f.onConfirmedAction(CombatFlowV082.EVADE,Float.POSITIVE_INFINITY));
        assertEquals(1f,f.weaponMultiplier(Float.NaN),.0001f);
        assertEquals(0f,f.remaining(Float.NaN),.0001f);
    }
}
