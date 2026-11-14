package com.lokij07.riftguard;
import org.junit.Test;
import static org.junit.Assert.*;

/** Stage-wide damage caps and truthful, state-derived in-battle guidance. */
public class CombatReadabilityV076Test {
    @Test public void everyLateRealmHasReasonableSingleHitBudgetsAcrossFourModes(){
        float heroMax=120f,coreMax=420f,towerMax=310f,relicMax=275f;
        for(int stage=0;stage<Campaign.STAGES;stage++)
            for(int mode=DifficultyRules.EASY;mode<=DifficultyRules.EXPERT;mode++){
                float factor=DifficultyRules.enemyDamage(mode);
                float melee=20f*factor,charge=29f*factor;
                float elite=110f*factor,bolt=17f*factor;
                float tower=46f*factor*1.4f,core=60f*factor;
                float relic=38f*factor;
                for(float input:new float[]{melee,charge,elite,bolt}){
                    float hit=CombatReadabilityV076.heroHit(stage,input,heroMax);
                    assertTrue(hit>=0);
                    assertEquals(stage<30?input:Math.min(input,heroMax*.29f),
                            hit,.002f);
                }
                assertEquals(stage<30?tower:Math.min(tower,towerMax*.24f),
                        CombatReadabilityV076.towerHit(stage,tower,towerMax),.002f);
                assertEquals(stage<30?core:Math.min(core,coreMax*.17f),
                        CombatReadabilityV076.coreHit(stage,core,coreMax),.002f);
                assertEquals(stage<30?relic:Math.min(relic,relicMax*.20f),
                        CombatReadabilityV076.relicHit(stage,relic,relicMax),.002f);
            }
        assertEquals(0f,CombatReadabilityV076.heroHit(150,Float.NaN,100),.001f);
        assertEquals(0f,CombatReadabilityV076.coreHit(150,100,0),.001f);
        assertEquals(29f,CombatReadabilityV076.heroHit(150,500,100),.001f);
        assertEquals(24f,CombatReadabilityV076.towerHit(150,500,100),.001f);
        assertThrows(IllegalArgumentException.class,
                ()->CombatReadabilityV076.heroHit(300,10,100));
    }
    @Test public void allFiveMissionHintsReflectActionAndActualUrgency(){
        int E=SiegeMissionsV069.ESCORT,G=SiegeMissionsV069.ENGINE,
                R=SiegeMissionsV069.RESCUE,S=SiegeMissionsV069.SEAL,
                F=SiegeMissionsV069.SPLIT;
        assertTrue(CombatReadabilityV076.missionAction(E,false,.95f,0,0,0,
                0,8,0).contains("MOVE NEXT"));
        assertTrue(CombatReadabilityV076.missionAction(E,true,.95f,0,0,0,
                0,8,0).contains("SHIELDED"));
        assertTrue(CombatReadabilityV076.missionAction(E,false,.25f,0,0,0,
                0,8,0).contains("CRITICAL"));
        assertTrue(CombatReadabilityV076.missionAction(G,false,0,0,0,0,
                0,8,.9f).contains("0.9s"));
        assertTrue(CombatReadabilityV076.missionAction(G,false,0,0,0,0,
                0,8,0).contains("FIND"));
        assertTrue(CombatReadabilityV076.missionAction(R,false,0,0,1,0,
                0,8,0).contains("LOST"));
        assertTrue(CombatReadabilityV076.missionAction(R,false,0,2,0,0,
                0,8,0).contains("FINAL"));
        assertTrue(CombatReadabilityV076.missionAction(R,false,0,2,1,0,
                0,8,0).contains("SECURED"));
        assertTrue(CombatReadabilityV076.missionAction(S,false,0,0,0,1,
                1.2f,8,0).contains("1.2s"));
        assertTrue(CombatReadabilityV076.missionAction(S,false,0,0,0,1,
                0,7.1f,0).contains("8s"));
        assertTrue(CombatReadabilityV076.missionAction(S,false,0,0,0,2,
                0,7.1f,0).contains("BOTH"));
        assertTrue(CombatReadabilityV076.missionAction(F,false,0,0,0,0,
                0,8,0).contains("BOTH BRANCHES"));
    }
    @Test public void tacticalHintsNeverInventTargetOrUnbrokenShield(){
        assertEquals("",CombatReadabilityV076.raidAction(-1,.5f));
        assertTrue(CombatReadabilityV076.raidAction(3,.2f).contains("PAD 4 CRITICAL"));
        assertTrue(CombatReadabilityV076.raidAction(1,.7f).contains("PAD 2"));
        assertEquals("",CombatReadabilityV076.tacticalAction(false,false));
        assertTrue(CombatReadabilityV076.tacticalAction(true,false).contains("PINCER"));
        assertTrue(CombatReadabilityV076.tacticalAction(true,true).contains("SHIELD"));
        assertEquals("",CombatReadabilityV076.missionAction(SiegeMissionsV069.NONE,
                false,0,0,0,0,0,0,0));
    }
}
