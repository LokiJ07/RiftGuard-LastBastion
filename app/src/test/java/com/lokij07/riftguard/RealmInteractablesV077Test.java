package com.lokij07.riftguard;

import org.junit.Test;
import static org.junit.Assert.*;

/** World-side objects are reachable, evenly scheduled, and mechanically real. */
public final class RealmInteractablesV077Test {
    @Test public void sixtyActualSitesAmongThreeHundredStagesAndNineRealms(){
        int[] counts=new int[4];
        int[] perRealm=new int[10];
        for(int stage=0;stage<Campaign.STAGES;stage++){
            int type=RealmInteractablesV077.type(stage);
            assertTrue(type>=0&&type<=3);
            if(stage<30)assertEquals(RealmInteractablesV077.NONE,type);
            if(type==RealmInteractablesV077.NONE)continue;
            counts[type]++;perRealm[Campaign.world(stage)]++;
            assertFalse(SiegeMissionsV069.mode(stage)!=SiegeMissionsV069.NONE);
            MemoryTrial.Objective trial=MemoryTrial.forStage(stage);
            assertTrue(trial==null||trial.type!=MemoryTrial.RELIC);
            assertFalse(RealmInteractablesV077.name(type).isEmpty());
            assertTrue(RealmInteractablesV077.guidance(type).contains("HOLD"));
        }
        assertEquals(24,counts[RealmInteractablesV077.WARD]);
        assertEquals(18,counts[RealmInteractablesV077.FORGE]);
        assertEquals(18,counts[RealmInteractablesV077.PURIFIER]);
        assertEquals(0,perRealm[0]);
        for(int realm=1;realm<10;realm++)assertTrue(perRealm[realm]>=6);
        assertEquals(RealmInteractablesV077.PURIFIER,RealmInteractablesV077.type(31));
        assertEquals(RealmInteractablesV077.FORGE,RealmInteractablesV077.type(33));
        assertEquals(RealmInteractablesV077.WARD,RealmInteractablesV077.type(40));
    }
    @Test public void everyPlacedRingIsWithinRealWardenNavigationBounds(){
        for(int stage=0;stage<Campaign.STAGES;stage++){
            if(!RealmInteractablesV077.scheduled(stage))continue;
            float[] location=RealmInteractablesV077.position(stage);
            assertTrue(location[0]>=LaneRules.HERO_MIN_X);
            assertTrue(location[0]<=LaneRules.HERO_MAX_X);
            assertTrue(location[1]>=LaneRules.HERO_MIN_Y);
            assertTrue(location[1]<=LaneRules.HERO_MAX_Y);
            assertFalse(MovementRules.blocksHero(location[0],location[1]));
            assertTrue(RealmInteractablesV077.inRing(
                    location[0],location[1],location[0],location[1]));
            assertFalse(RealmInteractablesV077.inRing(
                    location[0]+RealmInteractablesV077.RADIUS+1,location[1],
                    location[0],location[1]));
            float[] repeat=RealmInteractablesV077.position(stage);
            assertArrayEquals(location,repeat,.00001f);
            for(int i=0;i<TowerPadLayout.count(stage);i++)
                assertTrue("site on top of a playable tower "+stage+"/"+i,
                        RealmInteractablesV077.distance(location[0],location[1],
                        TowerPadLayout.x(stage,i),TowerPadLayout.y(stage,i))>56f);
        }
        assertThrows(IllegalArgumentException.class,
                ()->RealmInteractablesV077.position(300));
    }
    @Test public void standToChannelLeavingDecaysAndEffectClockIsBounded(){
        assertEquals(0f,RealmInteractablesV077.channel(0f,0f,true),.0001f);
        assertEquals(.7f,RealmInteractablesV077.channel(.5f,.2f,true),.0001f);
        assertEquals(.34f,RealmInteractablesV077.channel(.5f,.2f,false),.0001f);
        assertEquals(0f,RealmInteractablesV077.channel(.1f,.3f,false),.0001f);
        assertEquals(RealmInteractablesV077.CHANNEL_SECONDS,
                RealmInteractablesV077.channel(1f,25f,true),.0001f);
        assertThrows(IllegalArgumentException.class,
                ()->RealmInteractablesV077.channel(0f,-.1f,true));
        assertFalse(RealmInteractablesV077.inRing(Float.NaN,0,0,0));
        assertEquals(72f,RealmInteractablesV077.coreDamage(100f,4f),.001f);
        assertEquals(100f,RealmInteractablesV077.coreDamage(100f,0f),.001f);
        assertEquals(114f,RealmInteractablesV077.turretDamage(100f,10f),.001f);
        assertEquals(100f,RealmInteractablesV077.turretDamage(100f,0f),.001f);
    }
    @Test public void forgeRepairsOnlyRealExistingDamagedDefenses(){
        int[] tiers={1,3,0,2};
        float[] hp={TowerDurability.maxHp(1,4),
                TowerDurability.maxHp(3,4)*.4f,0,
                TowerDurability.maxHp(2,4)*.98f};
        assertEquals(1,RealmInteractablesV077.mostDamagedPad(tiers,hp,4,4));
        float max=TowerDurability.maxHp(tiers[1],4);
        assertEquals(Math.min(max,hp[1]+max*.22f),
                RealmInteractablesV077.repair(hp[1],max),.0001f);
        tiers[1]=0;hp[1]=0;
        assertEquals(3,RealmInteractablesV077.mostDamagedPad(tiers,hp,4,4));
        tiers[3]=0;
        assertEquals(-1,RealmInteractablesV077.mostDamagedPad(tiers,hp,4,4));
        assertThrows(IllegalArgumentException.class,
                ()->RealmInteractablesV077.repair(0,-1));
    }
}
