package com.lokij07.riftguard;

import org.junit.Test;
import static org.junit.Assert.*;

/** On-device diagnostic metrics must be bounded and honest about frame source. */
public final class BattlePerfStatsV066Test {
    @Test public void rollingWindowMeasuresFrameIntervalAndCpuDrawSeparately(){
        BattlePerfStatsV066 perf=new BattlePerfStatsV066();
        for(int i=0;i<90;i++)perf.record(8f,16f);
        for(int i=0;i<30;i++)perf.record(22f,45f);
        BattlePerfStatsV066.Snapshot shot=perf.snapshot();
        assertEquals(120,shot.samples);
        assertEquals(16f,shot.frameGapP50Ms,0f);
        assertEquals(45f,shot.frameGapP95Ms,0f);
        assertEquals(8f,shot.drawCpuP50Ms,0f);
        assertEquals(22f,shot.drawCpuP95Ms,0f);
        assertEquals(62.5f,shot.medianCadenceFps(),.001f);
    }
    @Test public void oldFramesEvictWithoutAllocatingEachCombatFrame(){
        BattlePerfStatsV066 perf=new BattlePerfStatsV066();
        for(int i=0;i<400;i++)perf.record(3f,200f);
        assertEquals(120,perf.samples());
        for(int i=0;i<120;i++)perf.record(5f,33f);
        BattlePerfStatsV066.Snapshot shot=perf.snapshot();
        assertEquals(120,shot.samples);
        assertEquals(33f,shot.frameGapP50Ms,0f);
        assertEquals(33f,shot.frameGapP95Ms,0f);
        assertEquals(5f,shot.drawCpuP95Ms,0f);
    }
    @Test public void invalidAppPausesDoNotInventFpsAndResetClearsStats(){
        BattlePerfStatsV066 perf=new BattlePerfStatsV066();
        perf.record(Float.NaN,16f);
        perf.record(7f,Float.POSITIVE_INFINITY);
        perf.record(5f,10000f);
        perf.record(5f,0f);
        perf.record(-1f,16f);
        assertEquals(0,perf.samples());
        assertEquals(0f,perf.snapshot().medianCadenceFps(),0f);
        perf.record(5f,33f);
        assertEquals(1,perf.samples());
        perf.reset();
        assertEquals(0,perf.samples());
        assertEquals(0f,perf.snapshot().frameGapP95Ms,0f);
    }
}
