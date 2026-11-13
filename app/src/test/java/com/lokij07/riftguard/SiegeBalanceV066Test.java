package com.lokij07.riftguard;

import org.junit.Test;
import static org.junit.Assert.*;

/** A full-campaign, economy-aware tuning audit; no assets are removed. */
public final class SiegeBalanceV066Test {
    @Test public void formationsRemainPresentButNeverBecomeHundredMobFloods(){
        assertEquals(6,DifficultyRules.enemies(0,1,DifficultyRules.NORMAL));
        assertEquals(9,DifficultyRules.enemies(0,4,DifficultyRules.NORMAL));
        assertEquals(28,DifficultyRules.enemies(299,12,DifficultyRules.NORMAL));
        assertEquals(31,DifficultyRules.enemies(299,12,DifficultyRules.HARD));
        assertEquals(33,DifficultyRules.enemies(299,12,DifficultyRules.EXPERT));
        for(int stage=0;stage<Campaign.STAGES;stage++){
            for(int mode=0;mode<=DifficultyRules.EXPERT;mode++){
                int waves=DifficultyRules.waves(stage,mode);
                assertTrue(waves>=3&&waves<=14);
                int previous=0;
                for(int wave=1;wave<=waves;wave++){
                    int count=DifficultyRules.enemies(stage,wave,mode);
                    assertTrue("Never empty stage "+stage,count>=4);
                    assertTrue("Crowd limit per wave stage "+stage,count<=34);
                    assertTrue("Wave should steadily intensify",count>=previous);
                    previous=count;
                }
            }
        }
    }
    @Test public void enemiesGraduallyGainHpButNeverExplodeWithinOneStage(){
        for(int wave=1;wave<=14;wave++){
            float bonus=SiegeBalanceV066.waveHealth(wave);
            assertTrue(bonus>=1f&&bonus<=1.22f);
            if(wave>1)assertTrue(bonus>=SiegeBalanceV066.waveHealth(wave-1));
        }
        assertEquals(1f,SiegeBalanceV066.waveHealth(1),.0001f);
        assertEquals(1.22f,SiegeBalanceV066.waveHealth(14),.0001f);
        assertTrue(BattleRules.enemyHealth(299,0)>BattleRules.enemyHealth(0,0)*8);
        assertEquals(1f,DifficultyRules.health(DifficultyRules.NORMAL),.001f);
        assertTrue(DifficultyRules.health(DifficultyRules.HARD)<
                DifficultyRules.health(DifficultyRules.EXPERT));
        assertTrue(DifficultyRules.health(DifficultyRules.EXPERT)<3f);
    }
    @Test public void activeEnemyCapPostponesSpawnsWithoutDeletingThem(){
        for(int mode=0;mode<=3;mode++){
            int cap=SiegeBalanceV066.activeLimit(mode);
            assertTrue(cap>=13&&cap<=22);
            assertTrue(SiegeBalanceV066.canSpawn(cap-1,mode));
            assertFalse(SiegeBalanceV066.canSpawn(cap,mode));
            assertFalse(SiegeBalanceV066.canSpawn(cap+1,mode));
        }
        assertTrue(SiegeBalanceV066.activeLimit(DifficultyRules.EXPERT)>
                SiegeBalanceV066.activeLimit(DifficultyRules.NORMAL));
    }
    @Test public void smallerWavesDoNotMakeTheFortressEconomyUnplayable(){
        // Each old enemy was worth 8 coins for kind 0. New per-kill payout
        // approximately preserves a wave's upgrade budget despite fewer kills.
        for(int stage=0;stage<Campaign.STAGES;stage++){
            for(int mode=0;mode<=3;mode++){
                for(int wave=1;wave<=DifficultyRules.waves(stage,mode);wave++){
                    int oldCount=SiegeBalanceV066.originalFormationSize(stage,wave,mode);
                    int count=DifficultyRules.enemies(stage,wave,mode);
                    int gold=SiegeBalanceV066.killGold(stage,wave,mode,0);
                    assertTrue("Gold too little at stage "+stage,
                            count*gold>=oldCount*8-count);
                    assertTrue("Gold inflation at stage "+stage,
                            count*gold<=oldCount*8+count);
                }
            }
        }
    }
    @Test public void fewerEnemiesStillGiveComparableHealthAndSupplyDrops(){
        for(int stage=0;stage<Campaign.STAGES;stage++){
            for(int mode=0;mode<=3;mode++){
                for(int wave=1;wave<=DifficultyRules.waves(stage,mode);wave++){
                    float chance=SiegeBalanceV066.pickupChance(stage,wave,mode);
                    assertTrue(chance>=.22f&&chance<=.48f);
                    int before=SiegeBalanceV066.originalFormationSize(stage,wave,mode);
                    int now=DifficultyRules.enemies(stage,wave,mode);
                    // Expected item opportunities stay within ~one pickup.
                    assertTrue("Less healing from lower mob count stage "+stage,
                            now*chance>=before*.22f-.5f);
                }
            }
        }
    }
    @Test public void bossesAndSignatureEnemyAbilitiesStayOnTheMap(){
        for(int realm=0;realm<10;realm++){
            int stage=Campaign.stage((realm+1)*10,2);
            assertTrue(DifficultyRules.worldBoss(stage));
            for(int mode=0;mode<=3;mode++){
                int finalWave=DifficultyRules.waves(stage,mode);
                assertTrue(DifficultyRules.eliteFinalWave(stage,finalWave,mode));
                assertTrue(DifficultyRules.finalBossMultiplier(stage)>=10f);
                assertTrue(DifficultyRules.enemies(stage,finalWave,mode)>=8);
            }
        }
        assertTrue(DifficultyRules.eliteWave(299,3,DifficultyRules.HARD));
        assertTrue(DifficultyRules.eliteWave(299,2,DifficultyRules.EXPERT));
        for(int stage=0;stage<Campaign.STAGES;stage++)
            assertTrue(DifficultyRules.spawnInterval(stage,DifficultyRules.EXPERT)>=.42f);
    }
    @Test public void generatedRealmArtAndWeaponTiersStayCompletelyUnchanged(){
        assertEquals(10,Campaign.WORLDS.length);
        assertEquals(300,Campaign.STAGES);
        assertEquals(6,Progression.WEAPONS.length);
        assertEquals(5,BattleRules.MAX_TOWER_TIER);
        assertEquals(5,Progression.MAX_RANK);
        for(int stage=0;stage<Campaign.STAGES;stage++){
            assertEquals(Campaign.seed(stage),NativeDeployedMapArt.signature(stage));
            assertEquals(Campaign.world(stage),NativeDeployedMapArt.world(stage));
        }
    }
    @Test(expected=IllegalArgumentException.class)
    public void rejectsInvalidStage(){SiegeBalanceV066.formationSize(300,1);}
    @Test(expected=IllegalArgumentException.class)
    public void rejectsInvalidWave(){SiegeBalanceV066.waveHealth(0);}
}
