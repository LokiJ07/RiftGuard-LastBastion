package com.lokij07.riftguard;
import org.junit.Test;
import static org.junit.Assert.*;

public class EngagementRulesV068Test {
    @Test public void contractsCoverAllStagesWithoutBlockingTutorial(){
        for(int s=0;s<3;s++){
            assertEquals(-1,BattleContractsV068.kind(s));
            assertEquals(0,BattleContractsV068.firstClearBonus(s));
        }
        boolean[] seen=new boolean[BattleContractsV068.VARIANTS];
        int[] exemplar=new int[seen.length];
        for(int i=0;i<exemplar.length;i++)exemplar[i]=-1;
        for(int s=3;s<300;s++){
            int type=BattleContractsV068.kind(s);
            assertTrue(type>=0&&type<seen.length);
            seen[type]=true;
            if(exemplar[type]<0)exemplar[type]=s;
            assertTrue(BattleContractsV068.name(s).length()>5);
            assertTrue(BattleContractsV068.firstClearBonus(s)>=24);
        }
        int[] weak={1,0,0,0,0,0,0,0},strong={2,1,0,0,0,0,0,0};
        for(int type=0;type<seen.length;type++){
            assertTrue(seen[type]);
            int stage=exemplar[type];
            assertFalse(BattleContractsV068.completed(stage,0,weak,5,.5f,.5f,1));
            assertTrue(BattleContractsV068.completed(stage,2,strong,5,.95f,.95f,0));
            assertFalse(BattleContractsV068.progress(stage,0,weak,5,.5f,.5f,1).isEmpty());
        }
        assertFalse(BattleContractsV068.completed(exemplar[BattleContractsV068.ENGINEER],
                2,weak,5,1f,1f,0));
        assertFalse(BattleContractsV068.completed(exemplar[BattleContractsV068.GARRISON],
                2,strong,5,1f,1f,1));
    }
    @Test public void signatureThreatOnlyReplacesOneWaveTwoSpawn(){
        assertEquals(-1,SiegeEncountersV068.signatureKind(0));
        assertEquals(6,SiegeEncountersV068.signatureKind(4));
        assertEquals(4,SiegeEncountersV068.signatureKind(7));
        assertEquals(2,SiegeEncountersV068.signatureKind(10));
        for(int stage=0;stage<Campaign.STAGES;stage++){
            int total=DifficultyRules.enemies(stage,2,DifficultyRules.NORMAL);
            int position=Math.max(2,total/2);
            for(int remaining=total;remaining>=1;remaining--){
                int expected=stage>=4&&remaining==position?
                        SiegeEncountersV068.signatureKind(stage):1;
                assertEquals(expected,SiegeEncountersV068.chooseKind(
                        stage,2,remaining,total,1));
                assertEquals(1,SiegeEncountersV068.chooseKind(
                        stage,3,remaining,total,1));
            }
        }
    }
    @Test public void healingAndIntermediateMasteryEffectsAreBounded(){
        assertTrue(EnemySpecials.canAid(4,100,100,300,100,35,100,false));
        assertFalse(EnemySpecials.canAid(4,100,100,400,100,35,100,false));
        assertFalse(EnemySpecials.canAid(4,100,100,200,100,35,100,true));
        assertFalse(EnemySpecials.canAid(1,100,100,200,100,35,100,false));
        assertEquals(12f,EnemySpecials.aidAmount(10,100),.001f);
        assertEquals(2f,EnemySpecials.aidAmount(98,100),.001f);
        assertEquals(42f,EnemySpecials.aidAmount(1,10000),.001f);
        assertEquals(0f,EnemySpecials.aidAmount(Float.NaN,100),.001f);
        for(int weapon=0;weapon<6;weapon++)
            assertTrue(WeaponTacticsV068.slowSeconds(weapon)>0||
                    WeaponTacticsV068.burnSeconds(weapon)>0);
        assertThrows(IllegalArgumentException.class,()->BattleContractsV068.kind(300));
    }
}
