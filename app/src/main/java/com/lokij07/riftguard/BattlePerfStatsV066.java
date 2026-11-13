package com.lokij07.riftguard;

import java.util.Arrays;

/**
 * Allocation-free LIVE BATTLE telemetry for on-phone QA.
 *
 * "Frame gap" includes Android scheduling and rendering latency, while
 * "draw CPU" is only the duration of RiftGameView.onDraw. These are not
 * GPU timers or a claim of guaranteed FPS.
 *
 * No serialization, network analytics, background threads or user data.
 */
public final class BattlePerfStatsV066 {
    private static final int CAPACITY=120;
    private final float[] frameGap=new float[CAPACITY];
    private final float[] drawCpu=new float[CAPACITY];
    private int used=0,next=0;

    public void reset(){used=0;next=0;}
    public int samples(){return used;}
    public int capacity(){return CAPACITY;}
    public void record(float drawMs,float gapMs){
        // Exclude app backgrounding, invalid timestamps and skipped frames.
        if(!Float.isFinite(drawMs)||!Float.isFinite(gapMs)||
                drawMs<0||drawMs>1000||gapMs<7||gapMs>500)return;
        frameGap[next]=gapMs;
        drawCpu[next]=drawMs;
        next=(next+1)%CAPACITY;
        if(used<CAPACITY)used++;
    }
    public static final class Snapshot {
        public final int samples;
        public final float frameGapP50Ms,frameGapP95Ms;
        public final float drawCpuP50Ms,drawCpuP95Ms;
        private Snapshot(int n,float g50,float g95,float d50,float d95){
            samples=n;frameGapP50Ms=g50;frameGapP95Ms=g95;
            drawCpuP50Ms=d50;drawCpuP95Ms=d95;
        }
        public float medianCadenceFps(){
            return frameGapP50Ms<=0f?0f:1000f/frameGapP50Ms;
        }
    }
    private static float percentile(float[] sorted,float frac){
        if(sorted.length==0)return 0f;
        int index=Math.min(sorted.length-1,
                Math.max(0,(int)Math.ceil(sorted.length*frac)-1));
        return sorted[index];
    }
    /** Allocates only when explicitly displaying telemetry on the pause UI. */
    public Snapshot snapshot(){
        float[] gaps=Arrays.copyOf(frameGap,used);
        float[] draws=Arrays.copyOf(drawCpu,used);
        Arrays.sort(gaps);Arrays.sort(draws);
        return new Snapshot(used,percentile(gaps,.5f),percentile(gaps,.95f),
                percentile(draws,.5f),percentile(draws,.95f));
    }
}
