package com.lokij07.riftguard;

/** Deterministic per-stage terrain/set dressing identity. No network required. */
public final class StageFlavor {
    private StageFlavor(){}
    public static int signature(int stage){
        Campaign.checkStage(stage);
        int h=Campaign.seed(stage)^(stage*0x41c64e6d);
        h^=(h>>>16);h*=0x7feb352d;h^=(h>>>15);
        return h & 0x7fffffff;
    }
    public static int landmarkStyle(int stage){return signature(stage)%7;}
    public static int props(int stage){return 4+(signature(stage)>>>4)%5;}
    public static int weather(int stage){return (signature(stage)>>>7)%4;}
    public static float landmarkX(int stage,int index){
        return 1160+((signature(stage)>>>8)+index*173L)%510;
    }
    public static float landmarkY(int stage,int index){
        return (index%2==0?215:800)+((signature(stage)>>>12)+index*37)%80;
    }
}
