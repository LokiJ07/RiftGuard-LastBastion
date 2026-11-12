package com.lokij07.riftguard;

/**
 * CPU draw-time load shedding for Android Canvas effects. Physics, collision,
 * character sprites, roads, tower pads, HUD, and effects crucial for attack
 * telegraphs never change; only noninteractive particles and fog density do.
 * A long hysteresis prevents flashing between quality levels.
 */
public final class AdaptiveFrameQuality {
    private boolean low;
    private int slowFrames,fastFrames;
    private float avgMs=16.7f;
    public boolean lowEffects(){return low;}
    public float averageDrawMillis(){return avgMs;}
    public void reset(){low=false;slowFrames=fastFrames=0;avgMs=16.7f;}
    public void observe(long elapsedNanos){
        if(elapsedNanos<=0)return;
        float ms=Math.min(500f,elapsedNanos/1_000_000f);
        avgMs=avgMs*.89f+ms*.11f;
        if(!low){
            if(avgMs>32f)slowFrames++;
            else slowFrames=Math.max(0,slowFrames-1);
            if(slowFrames>=12){low=true;fastFrames=0;}
        }else{
            if(avgMs<17f)fastFrames++;
            else fastFrames=0;
            if(fastFrames>=120){low=false;slowFrames=0;}
        }
    }
}
