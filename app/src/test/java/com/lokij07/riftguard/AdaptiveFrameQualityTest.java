package com.lokij07.riftguard;

import org.junit.Test;
import static org.junit.Assert.*;

/** Frame feedback must never change physics/AI/hitboxes or oscillate rapidly. */
public final class AdaptiveFrameQualityTest {
    @Test public void onlyOptionalParticlesAdaptAndRecoverWithHysteresis(){
        AdaptiveFrameQuality q=new AdaptiveFrameQuality();
        assertFalse(q.lowEffects());
        for(int i=0;i<30;i++)q.observe(110_000_000L);
        assertTrue("Long draw costs should reduce particle count",q.lowEffects());
        for(int i=0;i<60;i++)q.observe(6_000_000L);
        assertTrue("Slow recovery prevents distracting flicker",q.lowEffects());
        for(int i=0;i<160;i++)q.observe(6_000_000L);
        assertFalse("Recovered performance restores full effects",q.lowEffects());
        q.reset();
        assertFalse(q.lowEffects());
    }
    @Test public void noNaNOrZeroCanTriggerLowQuality(){
        AdaptiveFrameQuality q=new AdaptiveFrameQuality();
        for(int i=0;i<60;i++)q.observe(0);
        assertFalse(q.lowEffects());
        assertTrue(Float.isFinite(q.averageDrawMillis()));
    }
}
