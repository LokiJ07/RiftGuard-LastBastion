package com.lokij07.riftguard;

/**
 * Four distinct actual combat modes, not just labels or small HP adjustments.
 * Every coefficient is applied in the live battle loop and tested against
 * the same stage/fortress loadout. NORMAL retains the baseline economy.
 */
public final class DifficultyRules {
    private DifficultyRules(){}
    public static final int EASY=0,NORMAL=1,HARD=2,EXPERT=3;
    public static final String[] NAMES={"EASY","NORMAL","HARD","EXPERT"};
    public static final String[] DESCRIPTIONS={
        "Relaxed defense  /  extra supplies",
        "Standard siege  /  balanced enemies",
        "Veteran assault  /  elite reinforcement",
        "RIFT NIGHTMARE  /  relentless elites"
    };
    private static final float[] HEALTH={.58f,1f,2.10f,3.85f};
    private static final float[] DAMAGE={.46f,1f,1.78f,2.95f};
    private static final float[] SPEED={.84f,1f,1.20f,1.40f};
    private static final float[] SPAWN={1.36f,1f,.72f,.46f};
    private static final int[] WAVE_BONUS={-1,0,2,5};
    private static final int[] ENEMY_BONUS={-2,0,5,11};
    private static final float[] REWARD_BONUS={0f,0f,.43f,1.18f};
    private static final int[] GOLD={250,150,115,70};
    private static final float[] HERO_HP={1.30f,1f,.91f,.78f};
    private static final float[] HAZARD={.40f,1f,1.7f,2.55f};

    public static int safe(int mode){return Math.max(EASY,Math.min(EXPERT,mode));}
    public static float health(int mode){return HEALTH[safe(mode)];}
    public static float enemyDamage(int mode){return DAMAGE[safe(mode)];}
    public static float enemySpeed(int mode){return SPEED[safe(mode)];}
    public static float heroHealth(int mode){return HERO_HP[safe(mode)];}
    public static float hazardDamageMultiplier(int mode){return HAZARD[safe(mode)];}
    public static int bonusWaves(int mode){return WAVE_BONUS[safe(mode)];}
    public static int bonusEnemies(int mode){return ENEMY_BONUS[safe(mode)];}
    public static int supplyBonus(int mode,int baseReward){
        return Math.round(baseReward*REWARD_BONUS[safe(mode)]);
    }
    public static int waves(int stage,int mode){
        return Math.max(3,Campaign.waves(stage)+bonusWaves(mode));
    }
    public static int enemies(int stage,int wave,int mode){
        return Math.max(3,Campaign.enemies(stage,wave)+bonusEnemies(mode));
    }
    /** Expert spawns in roughly half the normal time, even on early stages. */
    public static float spawnInterval(int stage,int mode){
        return Math.max(.12f,Math.max(.23f,1.1f-stage*.0016f)*SPAWN[safe(mode)]);
    }
    public static float betweenWaves(int mode){
        return new float[]{4f,2.5f,1.85f,.9f}[safe(mode)];
    }
    public static int startingGold(int rank,int mode){
        return GOLD[safe(mode)]+(Math.max(1,rank)-1)*20;
    }
    public static int initialTowers(int rank,int mode){
        int normal=rank>=5?3:rank>=4?2:1;
        return mode==EXPERT?Math.max(0,normal-1):normal;
    }
    /**
     * Hard adds an elite every third wave, Expert every second wave.
     * Story finales still retain their original special boss.
     */
    public static boolean eliteWave(int stage,int wave,int mode){
        if(eliteFinalWave(stage,wave,mode))return true;
        if(mode==HARD)return wave>=3&&wave%3==0;
        if(mode==EXPERT)return wave>=2&&wave%2==0;
        return false;
    }
    public static float eliteMultiplier(int stage,int wave,int mode){
        if(eliteFinalWave(stage,wave,mode))return finalBossMultiplier(stage);
        return mode==EXPERT?4.5f:mode==HARD?3.2f:1f;
    }
    /** Every C map finishes with an elite; every tenth level has a world boss. */
    public static boolean worldBoss(int stage){
        return Campaign.map(stage)==2&&Campaign.level(stage)%10==0;
    }
    public static boolean eliteFinalWave(int stage,int wave,int mode){
        return Campaign.map(stage)==2&&wave==waves(stage,mode);
    }
    public static float finalBossMultiplier(int stage){
        return worldBoss(stage)?13f:4f;
    }
    public static String worldBossName(int stage){
        String[] names={"THORNHIDE CHIEF","HOLLOW TREANT","BOG LEVIATHAN",
                "ASHFANG COLOSSUS","WHITEHORN GIANT","IRONFALL TYRANT",
                "CRYSTAL WRAITH","TEMPEST KING","SHADOW REVENANT","ECLIPSE TITAN"};
        return names[Campaign.world(stage)];
    }
}