package com.lokij07.riftguard;

/** Normal preparation auto-advances; manual early start awards capped gold. */
public final class WavePacingV065 {
    private WavePacingV065(){}
    // Trim idle waiting during a 300-stage campaign while retaining
    // a meaningful turret-preparation window and longer boss telegraphs.
    private static final int[] NORMAL={22,17,13,10};
    private static final int[] BOSS={44,34,28,23};
    public static int seconds(int mode,boolean bossIncoming){
        return (bossIncoming?BOSS:NORMAL)[DifficultyRules.safe(mode)];
    }
    public static int earlyBonus(float remainingSeconds,int mode,boolean bossIncoming){
        float max=seconds(mode,bossIncoming);
        if(remainingSeconds<=0||max<=0)return 0;
        return Math.min(35,Math.max(0,(int)Math.floor(35f*remainingSeconds/max)));
    }
}
