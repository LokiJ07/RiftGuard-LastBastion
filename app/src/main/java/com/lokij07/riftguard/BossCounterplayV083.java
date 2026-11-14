package com.lokij07.riftguard;

/**
 * Counterplay for the TEN actual kingdom bosses (not ordinary elites/Cael).
 * During a visibly locked cast, real Warden weapon hits fill a short pressure
 * window. Enough landed damage INTERRUPTS the cast and opens a brief stagger.
 * Pure budget rules: no increased enemy count, no new persistent save fields.
 */
public final class BossCounterplayV083 {
    private BossCounterplayV083(){}

    public static final float STAGGER_SECONDS=2.15f;
    public static final float STAGGER_WEAPON_MULTIPLIER=1.14f;
    public static final float CAST_RECOVERY_SECONDS=2.8f;

    /** Early bosses require about two starter crossbow hits; endgame bosses
     * remain reachable with upgraded weapons before a 1.1–1.8s cast resolves. */
    public static float pressureRequired(int stage,int phase,float bossMaxHp){
        Campaign.checkStage(stage);
        if(!Float.isFinite(bossMaxHp)||bossMaxHp<=0)
            throw new IllegalArgumentException("bossMaxHp");
        int p=Math.max(1,Math.min(3,phase));
        float raw=bossMaxHp*.027f+5f*(Campaign.world(stage)%3)+(p-1)*9f;
        return Math.max(48f,Math.min(174f,raw));
    }

    public static float fraction(float landed,float threshold){
        if(!Float.isFinite(landed)||!Float.isFinite(threshold)||threshold<=0)
            return 0f;
        return BattleRules.clamp(landed/threshold,0f,1f);
    }

    public static boolean interrupted(float landed,float threshold){
        return Float.isFinite(landed)&&Float.isFinite(threshold)&&
                threshold>0f&&landed>=threshold;
    }

    /** Optional boss mastery is first-clear only, and capped at two
     * interrupts; no replay farming or supply payout for Cave/normal stages.
     * Existing first-clear stage reward remains untouched.
     */
    public static int firstClearMastery(int stage,int counters,boolean firstClear){
        Campaign.checkStage(stage);
        if(!firstClear||!DifficultyRules.worldBoss(stage)||counters<=0)return 0;
        return Math.min(2,counters)*(19+Campaign.world(stage)*2);
    }

    public static String prompt(int stage){
        Campaign.checkStage(stage);
        switch(Campaign.world(stage)%4){
            case 0:return "STRIKE DURING THE WIND-UP TO BREAK THE CAST";
            case 1:return "HIT THE CHARGING BOSS • INTERRUPT THE ATTACK";
            case 2:return "PRESSURE THE BOSS BEFORE THE MARK DETONATES";
            default:return "LAND WEAPON HITS TO STAGGER THE CHARGE";
        }
    }
}
