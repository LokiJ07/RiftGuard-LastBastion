package com.lokij07.riftguard;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Lightweight structural audit of all 300 playable stages.
 * Not a substitute for human mission/balance playtesting.
 */
public final class AllStagesReleaseAuditV065Test {
    @Test public void everyStageHasARealMissionWorldLaneHazardsAndBuildSites(){
        assertEquals(300,Campaign.STAGES);
        int seenLastStand=0;
        for(int stage=0;stage<Campaign.STAGES;stage++){
            assertEquals(stage,Campaign.stage(Campaign.level(stage),Campaign.map(stage)));
            int realm=Campaign.world(stage);
            assertTrue(realm>=0&&realm<10);
            assertFalse(Campaign.worldName(stage).isEmpty());
            assertEquals(realm,StageHazards.kind(stage));
            assertTrue(Campaign.waves(stage)>=4);
            assertTrue(Campaign.enemies(stage,1)>=4);
            int padCount=TowerPadLayout.count(stage);
            assertTrue(padCount>=5&&padCount<=8);
            for(int i=0;i<padCount;i++){
                float x=TowerPadLayout.x(stage,i),y=TowerPadLayout.y(stage,i);
                assertTrue(Float.isFinite(x)&&Float.isFinite(y));
                assertTrue(x>=0&&x<=1900&&y>=0&&y<=1050);
                assertTrue("Build site must be clear of visible enemy road",
                        TowerPadLayout.clearOfRoad(stage,i));
            }
            assertTrue(StageHazards.count(stage)>=1);
            for(int i=0;i<StageHazards.count(stage);i++){
                assertTrue(Float.isFinite(StageHazards.x(stage,i)));
                assertTrue(Float.isFinite(StageHazards.y(stage,i)));
                assertTrue(StageHazards.radius(stage,i)>0);
            }
            DestructibleTerrainV065 terrain=new DestructibleTerrainV065(stage);
            assertTrue(terrain.count()>=3);
            assertTrue(terrain.intact()==terrain.count());
            for(int i=0;i<terrain.count();i++){
                DestructibleTerrainV065.Prop p=terrain.at(i);
                assertEquals(realm,p.type);
                assertTrue(p.hp>0);
                assertTrue(Float.isFinite(p.x)&&Float.isFinite(p.y));
            }
            MemoryTrial.Objective objective=MemoryTrial.forStage(stage);
            if(Campaign.map(stage)==2){
                seenLastStand++;
                assertNotNull("Every Last Stand needs an actual memory trial",objective);
            }else assertNull(objective);
        }
        assertEquals(100,seenLastStand);
    }
    @Test public void campaignProgressionAndRealmBossesStayReachable(){
        int unlocked=0;
        for(int stage=0;stage<300;stage++){
            assertTrue(Campaign.accessible(stage,unlocked));
            unlocked=Campaign.unlockedAfter(unlocked,stage);
        }
        assertEquals(300,unlocked);
        for(int w=0;w<10;w++){
            int boss=Campaign.stage((w+1)*10,2);
            assertEquals(w,Campaign.world(boss));
            assertEquals(2,Campaign.map(boss));
            assertNotNull(MemoryTrial.forStage(boss));
        }
    }
}
