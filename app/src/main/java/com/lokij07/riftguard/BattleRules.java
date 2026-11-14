package com.lokij07.riftguard;

/** Pure gameplay math: easy to unit-test separately from Android graphics. */
public final class BattleRules {
    private BattleRules() {}
    // Read on every enemy movement tick: never allocate a fresh speed array
    // for every active enemy, every rendered frame.
    private static final float[] ENEMY_HP_MULTIPLIERS=
            {1f,.63f,3.1f,.88f,1.45f,2f,1.8f};
    private static final float[] ENEMY_SPEEDS=
            {57f,93f,34f,77f,52f,46f,83f};
    public static final int MAX_TOWER_TIER = 5;
    public static float clamp(float x, float low, float high) {
        return Math.max(low, Math.min(high, x));
    }
    public static int towerCost(int currentTier) {
        if (currentTier < 0 || currentTier >= MAX_TOWER_TIER)
            throw new IllegalArgumentException("Tower is fully upgraded or invalid");
        return 55 + currentTier * 45;
    }
    public static float enemyHealth(int stage, int type) {
        Campaign.checkStage(stage);
        float factor = 1f + stage * 0.029f;
        return 25f * factor *
                ENEMY_HP_MULTIPLIERS[Math.floorMod(type,ENEMY_HP_MULTIPLIERS.length)];
    }
    public static float enemySpeed(int type) {
        return ENEMY_SPEEDS[Math.floorMod(type,ENEMY_SPEEDS.length)];
    }
    public static float towerDamage(int tier) {
        return 18 + 13 * tier;
    }
    public static int killCoins(int type) {
        return 8 + Math.floorMod(type, 6) * 4;
    }
}
