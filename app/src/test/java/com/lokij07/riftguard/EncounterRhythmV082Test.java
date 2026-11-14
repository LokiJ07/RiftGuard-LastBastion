package com.lokij07.riftguard;

import org.junit.Test;
import static org.junit.Assert.*;

/** Difficulty/encounter pacing must be about tactics, not mob inflation. */
public final class EncounterRhythmV082Test {
    @Test public void firstRealmAndTutorialWaveStayUnchanged(){
        for(int stage=0;stage<300;stage++){
            for(int wave=1;wave<=8;wave++){
                if(stage<30||wave==1){
                    assertEquals(EncounterRhythmV082.NONE,
                            EncounterRhythmV082.plan(stage,wave));
                    assertEquals(3,EncounterRhythmV082.chooseKind(
                            stage,wave,10,3,false,false));
                }
            }
        }
    }
    @Test public void laterRealmsRotateFourDistinctOrders(){
        boolean[] seen=new boolean[4];
        for(int stage=30;stage<300;stage++)
            for(int wave=2;wave<=6;wave++){
                int p=EncounterRhythmV082.plan(stage,wave);
                assertTrue(p>=0&&p<seen.length);
                seen[p]=true;
                assertFalse(EncounterRhythmV082.name(stage,wave).isEmpty());
            }
        for(boolean value:seen)assertTrue(value);
    }
    @Test public void everyFifthOrdinarySlotIsReplacedNotAdded(){
        for(int stage=30;stage<300;stage+=7)
            for(int wave=2;wave<=7;wave++){
                int signature=0;
                for(int remaining=1;remaining<=34;remaining++){
                    int kind=EncounterRhythmV082.chooseKind(stage,wave,
                            remaining,0,false,false);
                    assertTrue("existing enemy type",kind>=0&&kind<=6);
                    if(kind!=0)signature++;
                    if(remaining%5!=0||remaining==1)assertEquals(0,kind);
                    assertEquals("boss must not be overridden",5,
                            EncounterRhythmV082.chooseKind(stage,wave,
                                    remaining,5,false,true));
                    assertEquals("mission actor must not be overridden",2,
                            EncounterRhythmV082.chooseKind(stage,wave,
                                    remaining,2,true,false));
                }
                assertTrue("max one in five slots",signature<=34/5);
            }
    }
    @Test public void preparationIsShorterButBossPrepRemainsLonger(){
        for(int mode=0;mode<4;mode++){
            assertTrue(WavePacingV065.seconds(mode,false)<=22);
            assertTrue(WavePacingV065.seconds(mode,false)>=10);
            assertTrue(WavePacingV065.seconds(mode,true)>
                    WavePacingV065.seconds(mode,false));
            assertTrue(WavePacingV065.earlyBonus(5,mode,false)<=35);
            assertEquals(0,WavePacingV065.earlyBonus(0,mode,true));
        }
    }
}
