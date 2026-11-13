package com.lokij07.riftguard;

/**
 * Combat pacing for the 300-stage campaign. Fewer but more capable opponents,
 * without turning later battles into a flood of disposable sprites or a
 * single unkillable HP sponge.
 *
 * This is PURE GAMEPLAY MATH: it does not touch bitmap caches, backgrounds,
 * animated enemies, environment density or the appearance of tower upgrades.
 */
public final class SiegeBalanceV066 {
    private SiegeBalanceV066(){}

    /** Six to 28 normal enemies per wave, depending on stage and wave. */
    public static int formationSize(int stage,int wave){
        Campaign.checkStage(stage);
        if(wave<1)throw new IllegalArgumentException("wave");
        return Math.min(28,5+stage/30+wave+
                (Campaign.map(stage)==2?2:0));
    }

    /** Fresh enemies become more durable deeper within the same stage. */
    public static float waveHealth(int wave){
        if(wave<1)throw new IllegalArgumentException("wave");
        return 1f+Math.min(.22f,(wave-1)*.02f);
    }

    /** Limit SIMULTANEOUS enemies without deleting a queued wave spawn. */
    public static int activeLimit(int difficulty){
        switch(DifficultyRules.safe(difficulty)){
            case DifficultyRules.EASY:return 13;
            case DifficultyRules.HARD:return 19;
            case DifficultyRules.EXPERT:return 22;
            default:return 16;
        }
    }

    /**
     * Roughly preserve the combat-gold budget when the army has fewer mobs.
     * Previous count was the original v0.66 alpha3 wave formula.
     * Bosses/minions still use their existing real kill and drop logic.
     */
    static int originalFormationSize(int stage,int wave,int mode){
        Campaign.checkStage(stage);
        if(wave<1)throw new IllegalArgumentException("wave");
        int base=Math.min(55,4+stage/14+2*wave+
                (Campaign.map(stage)==2?3:0));
        int bonus=new int[]{-2,0,5,11}[DifficultyRules.safe(mode)];
        return Math.max(3,base+bonus);
    }
    public static int killGold(int stage,int wave,int mode,int enemyKind){
        int previous=originalFormationSize(stage,wave,mode);
        int current=DifficultyRules.enemies(stage,wave,mode);
        float ratio=Math.min(2.6f,Math.max(1f,previous/(float)current));
        return Math.max(1,Math.round(BattleRules.killCoins(enemyKind)*ratio));
    }

    /** A full wave is never replaced by an empty trickle. */
    public static boolean canSpawn(int active,int mode){
        return active<activeLimit(mode);
    }
}
