package com.lokij07.riftguard;

/** Deterministic 100-level / 300-stage offline campaign rules. */
public final class Campaign {
    private Campaign() {}
    public static final int LEVELS = 100;
    public static final int MAPS_PER_LEVEL = 3;
    public static final int STAGES = LEVELS * MAPS_PER_LEVEL;

    public static final String[] WORLDS = {
        "Greenhaven Frontier", "Whispering Woods", "Venom Marsh", "Ember Canyon",
        "Frostpeak Mountains", "Ironfall Ruins", "Crystal Depths",
        "Stormbound Isles", "Shadowfall Kingdom", "Eclipse Citadel"
    };
    public static final String[] MAPS = { "Scout Outpost", "Broken Pass", "Last Stand" };

    public static int stage(int level, int map) {
        if (level < 1 || level > LEVELS || map < 0 || map >= MAPS_PER_LEVEL)
            throw new IllegalArgumentException("Invalid campaign level/map");
        return (level - 1) * MAPS_PER_LEVEL + map;
    }
    public static int level(int stage) {
        checkStage(stage);
        return stage / MAPS_PER_LEVEL + 1;
    }
    public static int map(int stage) {
        checkStage(stage);
        return stage % MAPS_PER_LEVEL;
    }
    public static int world(int stage) {
        return (level(stage) - 1) / 10;
    }
    public static String title(int stage) {
        return "LEVEL " + level(stage) + "-" + (char) ('A' + map(stage));
    }
    public static String worldName(int stage) {
        return WORLDS[world(stage)];
    }
    public static int seed(int stage) {
        checkStage(stage);
        return 0x1F123BB5 ^ (stage * 0x45D9F3B);
    }
    public static int waves(int stage) {
        return Math.min(12, 4 + stage / 38 + (map(stage) == 2 ? 2 : 0));
    }
    public static int enemies(int stage, int wave) {
        if (wave < 1) throw new IllegalArgumentException("Wave must be positive");
        return Math.min(55, 4 + stage / 14 + 2 * wave + (map(stage) == 2 ? 3 : 0));
    }
    /** 0 = first stage, 300 = entire campaign completed. */
    public static int unlockedAfter(int currentUnlocked, int completedStage) {
        checkStage(completedStage);
        if (currentUnlocked < 0 || currentUnlocked > STAGES)
            throw new IllegalArgumentException("Invalid unlocked count");
        if (completedStage > currentUnlocked) return currentUnlocked;
        return Math.min(STAGES, Math.max(currentUnlocked, completedStage + 1));
    }
    public static boolean accessible(int stage, int unlocked) {
        return stage >= 0 && stage < STAGES && stage <= unlocked;
    }
    public static void checkStage(int stage) {
        if (stage < 0 || stage >= STAGES)
            throw new IllegalArgumentException("Stage must be between 0 and 299");
    }
}
