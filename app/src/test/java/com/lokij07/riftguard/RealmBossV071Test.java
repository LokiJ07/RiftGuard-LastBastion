package com.lokij07.riftguard;

import org.junit.Test;
import static org.junit.Assert.*;

/** Geometry and act invariants must match the live boss telegraphs. */
public class RealmBossV071Test {
    private static int finale(int world){return Campaign.stage((world+1)*10,2);}
    @Test public void eachRealmHasUniqueWorldBossAndRecognizableCounters(){
        String[] names=new String[10];
        for(int world=0;world<10;world++){
            int stage=finale(world);
            assertTrue(DifficultyRules.worldBoss(stage));
            assertTrue(RealmBossV071.enabled(stage,true,false));
            assertFalse(RealmBossV071.enabled(stage,false,false));
            assertFalse(RealmBossV071.enabled(stage,true,true));
            assertFalse(RealmBossV071.enabled(stage-1,true,false));
            names[world]=RealmBossV071.name(stage);
            assertTrue(RealmBossV071.counter(stage).length()>40);
            assertTrue(RealmBossV071.chargeSeconds(stage,3)>=1.14f);
            assertTrue(RealmBossV071.intervalSeconds(stage,3)>7f);
            assertTrue(RealmBossV071.heroDamage(stage)>0f);
            assertTrue(RealmBossV071.coreDamage(stage)>0f);
            assertEquals(world,RealmBossV071.realm(stage));
            MissionStory.Briefing b=MissionStory.forStage(stage);
            assertTrue(b.warning.contains(RealmBossV071.counter(stage)));
        }
        for(int i=0;i<names.length;i++)for(int j=0;j<i;j++)
            assertNotEquals("same boss power in two realms",names[i],names[j]);
    }
    @Test public void lockedMarksCanBeEscapedAndDoNotFollowTheHero(){
        for(int world:new int[]{1,2,3,5,7}){
            int s=finale(world);
            float r=RealmBossV071.radius(s,2);
            assertTrue(RealmBossV071.hitsHero(s,2,800,500,1250,550,1250,550));
            assertFalse(RealmBossV071.hitsHero(s,2,800,500,1250,550,
                    1250+r+28,550));
        }
        assertFalse(RealmBossV071.hitsHero(finale(7),2,
                800,500,1250,550,Float.NaN,550));
    }
    @Test public void beamAttacksRequireSidestepAndStopAtTarget(){
        for(int world:new int[]{4,8}){
            int s=finale(world);
            assertTrue(RealmBossV071.hitsHero(s,2,800,500,1200,500,1000,500));
            assertFalse(RealmBossV071.hitsHero(s,2,800,500,1200,500,1000,700));
            assertFalse(RealmBossV071.hitsHero(s,2,800,500,1200,500,1490,500));
            assertFalse(RealmBossV071.hitsHero(s,2,800,500,1200,500,450,500));
        }
    }
    @Test public void prismRingHasReadableSafeInnerAndOuterZones(){
        int s=finale(6);
        float inner=RealmBossV071.innerRadius(s,2),outer=RealmBossV071.radius(s,2);
        assertTrue(inner>0f&&outer>inner+60f);
        assertFalse(RealmBossV071.hitsHero(s,2,1000,500,1300,500,1000,500));
        assertTrue(RealmBossV071.hitsHero(s,2,1000,500,1300,500,
                1000+(inner+outer)*.5f,500));
        assertFalse(RealmBossV071.hitsHero(s,2,1000,500,1300,500,
                1000+outer+25f,500));
        for(int world:new int[]{0,9}){
            int stage=finale(world);
            assertTrue(RealmBossV071.hitsHero(stage,2,1000,500,1300,500,1000,500));
            assertFalse(RealmBossV071.hitsHero(stage,2,1000,500,1300,500,
                    1000+RealmBossV071.radius(stage,2)+30,500));
        }
    }
    @Test public void destroyingBossComponentsDisablesHealsAndWeakensAttacks(){
        for(int world:new int[]{2,6}){
            int stage=finale(world);
            assertTrue(RealmBossV071.selfHealFraction(stage,3,true)>0);
            assertEquals(0f,RealmBossV071.selfHealFraction(stage,3,false),.0001f);
            BossAnatomyV065 anatomy=new BossAnatomyV065(world,1000);
            assertTrue(anatomy.damage(BossAnatomyV065.WEAPON,10000));
            assertTrue(anatomy.siegeMultiplier()<.50f);
            assertTrue(anatomy.damage(BossAnatomyV065.CORE,10000));
            assertFalse(anatomy.canSummon());
        }
        assertEquals(0f,RealmBossV071.towerDamage(finale(7)),.001f);
        assertTrue(RealmBossV071.towerDamage(finale(5))>0);
        assertTrue(RealmBossV071.skillSuppression(finale(8))>0);
        assertTrue(RealmBossV071.slowSeconds(finale(4))>0);
    }
    @Test public void everyRealmHasThreeActsWithoutAdditionalEnemyCounts(){
        for(int realm=0;realm<10;realm++){
            int[] acts={0,0,0};
            for(int s=realm*30;s<realm*30+30;s++){
                acts[RealmActsV071.act(s)]++;
                assertFalse(RealmActsV071.name(s).isEmpty());
                assertFalse(RealmActsV071.orders(s).isEmpty());
                int total=DifficultyRules.enemies(s,2,DifficultyRules.NORMAL);
                int old=1,newKind=RealmActsV071.chooseKind(
                        s,2,Math.max(2,total/2),total,old);
                assertTrue(newKind>=0&&newKind<=6);
                assertEquals(old,RealmActsV071.chooseKind(s,3,3,total,old));
                if(realm>0)assertEquals(RealmActsV071.threatKind(s),newKind);
            }
            assertArrayEquals(new int[]{9,12,9},acts);
        }
    }
}
