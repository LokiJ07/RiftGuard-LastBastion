package com.lokij07.riftguard;

/**
 * Ten kingdom boss families share predictable warnings but gain
 * real HP-triggered phases, timed reinforcements and realm mechanics.
 * Pure math here keeps gameplay deterministic and testable offline.
 */
public final class BossTactics {
    private BossTactics(){}
    public static int phase(float hp,float max){
        if(max<=0)throw new IllegalArgumentException("max hp");
        float fraction=hp/max;
        return fraction>.66f?1:fraction>.33f?2:3;
    }
    public static int realm(int stage){
        Campaign.checkStage(stage);
        return Campaign.world(stage);
    }
    public static int tactic(int stage){
        // Three active ability groups: SEISMIC, SUSTAIN, ROOT.
        return realm(stage)%3;
    }
    public static float chargeTime(int stage,int phase){
        return Math.max(.42f,.95f-(Math.max(1,Math.min(3,phase))-1)*.13f
                +(realm(stage)%2)*.08f);
    }
    public static float interval(int stage,int phase){
        return Math.max(3.8f,8f-phase*.75f-(realm(stage)%3)*.12f);
    }
    public static float damageMultiplier(int phase){
        return 1f+(Math.max(1,Math.min(3,phase))-1)*.28f;
    }
    public static int minions(int phase){
        return phase==2?1:phase==3?2:0;
    }
    public static int minionKind(int stage){
        // Normal enemy archetypes already have movement/attacks/health rules.
        return (realm(stage)*3+Campaign.map(stage))%7;
    }
    public static String warning(int stage){
        switch(tactic(stage)){
            case 0:return "SEISMIC CHARGE";
            case 1:return "DARK RENEWAL";
            default:return "ROOTING BURST";
        }
    }
}
