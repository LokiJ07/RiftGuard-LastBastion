package com.lokij07.riftguard;

import org.junit.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

/** Assert world geometry and actual enemy lane logic share the same stage route. */
public final class StageRoutesTest {
    @Test public void threeHundredStagesHaveDistinctPlayableRouteGeometry() {
        Set<String> seen=new HashSet<>();
        for(int stage=0;stage<300;stage++) {
            float x=LaneRules.stageBendX(stage);
            float bend=LaneRules.stageBendY(stage);
            float spawn=LaneRules.stageSpawnY(stage);
            float y=LaneRules.stagePathY(stage,1660f);
            assertTrue("lane x outside drawable area",x>=1300 && x<=1700);
            assertTrue("lane y outside drawable area",bend>=330 && bend<=720);
            assertTrue("spawn outside valid gameplay area",spawn>=290 && spawn<=800);
            assertEquals("spawn must match actual road",
                    spawn,LaneRules.stagePathY(stage,LaneRules.ENEMY_SPAWN_X),.1f);
            assertEquals("all roads must connect to the core",
                    510f,LaneRules.stagePathY(stage,LaneRules.CASTLE_GATE_X),.001f);
            assertTrue(LaneRules.stageCoversPath(stage,1660f,y));
            assertFalse(LaneRules.stageCoversPath(stage,1660f,y+200f));
            String geometry=String.format(java.util.Locale.US,"%.3f %.3f %.3f %.3f",
                    x,bend,spawn,y);
            seen.add(geometry);
        }
        assertTrue("Too many duplicate silhouettes across 300 stages: "+seen.size(),
                seen.size()>=275);
    }
    @Test public void stageSpecificEnemyWaypointsFollowPaintedRoad(){
        for(int stage=0;stage<300;stage++) {
            for(float x=1750f;x>1200f;x-=55f) {
                float waypointX=LaneRules.stageWaypointX(x,stage);
                float waypointY=LaneRules.stageWaypointY(x,stage);
                assertTrue(waypointX<x);
                assertEquals(LaneRules.stagePathY(stage,waypointX),waypointY,.001f);
            }
        }
    }
    @Test public void deterministicRoutesAreStableAcrossRuns(){
        float[][] a=new float[300][3];
        for(int s=0;s<300;s++){
            a[s][0]=LaneRules.stageBendX(s);
            a[s][1]=LaneRules.stageBendY(s);
            a[s][2]=LaneRules.stagePathY(s,1660);
        }
        for(int s=0;s<300;s++){
            assertEquals(a[s][0],LaneRules.stageBendX(s),0f);
            assertEquals(a[s][1],LaneRules.stageBendY(s),0f);
            assertEquals(a[s][2],LaneRules.stagePathY(s,1660),0f);
        }
    }
}
