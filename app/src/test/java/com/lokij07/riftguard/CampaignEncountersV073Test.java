package com.lokij07.riftguard;
import org.junit.Test;
import static org.junit.Assert.*;

/** Full 300-stage encounter audit with four difficulty levels. */
public class CampaignEncountersV073Test {
    @Test public void exactlyNinetySixLiveEventsCoverTenRealmsAndThreeActs(){
        int[][] perRealm=new int[10][6];
        int[][] perAct=new int[3][6];
        int total=0;
        for(int stage=0;stage<Campaign.STAGES;stage++){
            int realm=Campaign.world(stage),mode=SiegeMissionsV069.mode(stage);
            assertEquals(CampaignEncountersV073.mode(stage),mode);
            assertTrue(mode>=0&&mode<=5);
            perRealm[realm][mode]++;
            perAct[RealmActsV071.act(stage)][mode]++;
            assertFalse(DifficultyRules.worldBoss(stage)&&mode!=0);
            assertTrue(MissionStory.forStage(stage).objective.contains("WAVES"));
            if(mode!=0){
                total++;
                assertTrue(MissionStory.forStage(stage).order.contains("Wave two"));
                assertTrue(SiegeMissionsV069.active(stage,2));
                assertFalse(SiegeMissionsV069.active(stage,1));
            }
        }
        assertEquals(96,total);
        for(int realm=0;realm<10;realm++){
            int count=0;
            for(int type=1;type<=5;type++)count+=perRealm[realm][type];
            assertEquals(realm==0?15:9,count);
            if(realm==0){
                for(int type=1;type<=3;type++)assertEquals(5,perRealm[realm][type]);
                assertEquals(0,perRealm[realm][SiegeMissionsV069.RESCUE]);
                assertEquals(0,perRealm[realm][SiegeMissionsV069.SEAL]);
            }else{
                for(int type=1;type<=3;type++)assertTrue(perRealm[realm][type]>=1);
                assertEquals(1,perRealm[realm][SiegeMissionsV069.RESCUE]);
                assertEquals(1,perRealm[realm][SiegeMissionsV069.SEAL]);
            }
        }
        for(int act=0;act<3;act++)for(int type=1;type<=3;type++)
            assertTrue("missing act/mode "+act+"/"+type,perAct[act][type]>=6);
        assertEquals(9,perAct[RealmActsV071.ESCALATION][SiegeMissionsV069.RESCUE]);
        assertEquals(9,perAct[RealmActsV071.SIEGE][SiegeMissionsV069.SEAL]);
        assertEquals(SiegeMissionsV069.NONE,SiegeMissionsV069.mode(21));
        assertEquals(SiegeMissionsV069.ENGINE,SiegeMissionsV069.mode(32));
        assertEquals(SiegeMissionsV069.ESCORT,SiegeMissionsV069.mode(38));
        assertEquals(SiegeMissionsV069.NONE,SiegeMissionsV069.mode(299));
    }
    @Test public void everyDifficultyPreservesPopulationAndFinalBossSlots(){
        for(int stage=0;stage<300;stage++){
            for(int diff=0;diff<4;diff++){
                int count=DifficultyRules.enemies(stage,2,diff),engineCount=0;
                for(int remaining=count;remaining>0;remaining--){
                    if(SiegeMissionsV069.engineSpawn(stage,2,remaining,count))engineCount++;
                }
                assertEquals(SiegeMissionsV069.mode(stage)==SiegeMissionsV069.ENGINE?
                        1:0,engineCount);
                assertTrue(SiegeBalanceV066.activeLimit(diff)<=22);
                assertTrue(count>=4&&count<=34);
                assertTrue(CampaignEncountersV073.cartMaxHp(stage,diff)>0);
                assertTrue(CampaignEncountersV073.engineDamage(stage,diff,300f)<=90.001f);
                assertTrue(CampaignEncountersV073.bonusGold(stage,SiegeMissionsV069.ESCORT)
                        <=81);
            }
        }
    }
    @Test public void guardAndCannonCounterplayAreBoundedAndDeterministic(){
        assertTrue(CampaignEncountersV073.guarding(100,100,200,100));
        assertFalse(CampaignEncountersV073.guarding(100,100,300,100));
        assertFalse(CampaignEncountersV073.guarding(Float.NaN,100,100,100));
        assertTrue(CampaignEncountersV073.cartSpeed(38,true)>
                CampaignEncountersV073.cartSpeed(38,false));
        assertTrue(CampaignEncountersV073.cartDamage(38,DifficultyRules.EXPERT,
                2,true)<CampaignEncountersV073.cartDamage(38,DifficultyRules.EXPERT,
                2,false));
        assertTrue(CampaignEncountersV073.cartMaxHp(278,DifficultyRules.EXPERT)>
                CampaignEncountersV073.cartMaxHp(6,DifficultyRules.NORMAL));
        assertTrue(CampaignEncountersV073.interruptFraction(290)>.09f);
        assertTrue(CampaignEncountersV073.interruptFraction(290)<.17f);
        assertTrue(CampaignEncountersV073.engineHpMultiplier(290,DifficultyRules.EXPERT)
                <CampaignEncountersV073.engineHpMultiplier(290,DifficultyRules.NORMAL));
        assertThrows(IllegalArgumentException.class,
                ()->CampaignEncountersV073.engineDamage(20,1,Float.NaN));
    }
    @Test public void worldBossSingleChargeCannotOneShotFromFullHealth(){
        for(int realm=0;realm<10;realm++){
            int stage=Campaign.stage((realm+1)*10,2);
            for(int diff=0;diff<4;diff++)for(int phase=1;phase<=3;phase++){
                float strength=DifficultyRules.enemyDamage(diff)*
                        BossTactics.damageMultiplier(phase);
                float hero=BossFairnessV073.hero(RealmBossV071.heroDamage(stage)*strength,100);
                float core=BossFairnessV073.core(RealmBossV071.coreDamage(stage)*strength,350);
                float tower=BossFairnessV073.tower(RealmBossV071.towerDamage(stage)*strength,120);
                assertTrue(hero>=0&&hero<=34.001f);
                assertTrue(core>=0&&core<=87.501f);
                assertTrue(tower>=0&&tower<=51.601f);
                assertTrue(hero>0&&core>0);
            }
        }
        assertEquals(0f,BossFairnessV073.hero(Float.NaN,150),.001f);
        assertEquals(0f,BossFairnessV073.core(20,0),.001f);
        assertEquals(34f,BossFairnessV073.hero(1000,100),.001f);
    }
}
