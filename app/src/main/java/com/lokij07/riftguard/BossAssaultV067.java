package com.lokij07.riftguard;

/**
 * Rebalances the three existing boss attack families so the Warden makes
 * meaningful dodge decisions. Every radius is shared between actual collision
 * and the ground telegraph: no invisible damage or false safe zones.
 *
 * Phase one of the first seismic boss preserves the legacy 265-unit ring.
 * Later phases extend the danger slightly; sustained-heal bosses have tighter
 * melee reach but a stronger sustain identity, and roots trade reach for slow.
 */
public final class BossAssaultV067 {
    private BossAssaultV067(){}
    public static float heroRadius(int stage,int phase){
        Campaign.checkStage(stage);
        int p=Math.max(1,Math.min(3,phase));
        int tactic=BossTactics.tactic(stage);
        float base=tactic==0?265f:tactic==1?225f:250f;
        return base+(p-1)*11f+(Campaign.world(stage)/3)*3f;
    }
    public static boolean heroInRange(int stage,int phase,
                                      float bossX,float bossY,
                                      float heroX,float heroY){
        if(!Float.isFinite(bossX)||!Float.isFinite(bossY)||
                !Float.isFinite(heroX)||!Float.isFinite(heroY))return false;
        float dx=bossX-heroX,dy=bossY-heroY;
        float radius=heroRadius(stage,phase);
        return dx*dx+dy*dy<radius*radius;
    }
    public static float healFraction(int stage,int phase){
        Campaign.checkStage(stage);
        if(BossTactics.tactic(stage)!=1)return 0f;
        return .04f+.01f*(Math.max(1,Math.min(3,phase))-1);
    }
}
