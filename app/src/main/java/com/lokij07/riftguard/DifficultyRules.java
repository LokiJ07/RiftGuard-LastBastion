package com.lokij07.riftguard;

/** Player-selected difficulty, used by live combat and saved between sessions. */
public final class DifficultyRules {
    private DifficultyRules() {}
    public static final int EASY=0,NORMAL=1,HARD=2,EXPERT=3;
    public static final String[] NAMES={"EASY","NORMAL","HARD","EXPERT"};
    public static int safe(int mode){return Math.max(EASY,Math.min(EXPERT,mode));}
    public static float health(int mode) {
        return new float[]{.76f,1f,1.48f,2.05f}[safe(mode)];
    }
    public static float enemyDamage(int mode) {
        return new float[]{.65f,1f,1.33f,1.76f}[safe(mode)];
    }
    public static float enemySpeed(int mode) {
        return new float[]{.91f,1f,1.08f,1.16f}[safe(mode)];
    }
    public static int bonusWaves(int mode){return new int[]{0,0,1,2}[safe(mode)];}
    public static int bonusEnemies(int mode){return new int[]{-1,0,2,4}[safe(mode)];}
    public static int supplyBonus(int mode,int baseReward){
        return Math.round(baseReward*new float[]{0f,0f,.22f,.55f}[safe(mode)]);
    }
    public static int waves(int stage,int mode){
        return Campaign.waves(stage)+bonusWaves(mode);
    }
    public static int enemies(int stage,int wave,int mode){
        return Math.max(3,Campaign.enemies(stage,wave)+bonusEnemies(mode));
    }
    /** Every C map finishes with an elite; every tenth level has a world boss. */
    public static boolean worldBoss(int stage) {
        return Campaign.map(stage)==2 && Campaign.level(stage)%10==0;
    }
    public static boolean eliteFinalWave(int stage,int wave,int mode){
        return Campaign.map(stage)==2 && wave==waves(stage,mode);
    }
    public static float finalBossMultiplier(int stage) {
        return worldBoss(stage)?13f:4f;
    }
    public static String worldBossName(int stage) {
        String[] names={"THORNHIDE CHIEF","HOLLOW TREANT","BOG LEVIATHAN",
            "ASHFANG COLOSSUS","WHITEHORN GIANT","IRONFALL TYRANT",
            "CRYSTAL WRAITH","TEMPEST KING","SHADOW REVENANT","ECLIPSE TITAN"};
        return names[Campaign.world(stage)];
    }
}
