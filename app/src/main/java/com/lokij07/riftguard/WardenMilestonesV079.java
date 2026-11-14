package com.lokij07.riftguard;

/**
 * Permanent character-only oath unlocks. Milestones are derived from the
 * existing save-backed Warden level: no new wallet, claim button, or migration.
 *
 * Keep these modest: enemies, towers, supplies and campaign difficulty are
 * unchanged. The kill heal is one small pulse for every eight credited kills,
 * never per hit, never during a defeat, and never above the Warden's max HP.
 */
public final class WardenMilestonesV079 {
    private WardenMilestonesV079(){}
    public static final int[] LEVELS={10,25,40,60,80,100};
    public static final String[] NAMES={
            "GUARDIAN HEART","RIFT SIPHON","BATTLE FOCUS",
            "ARC CONDUIT","VANGUARD STRIDE","RIFT SOVEREIGN"
    };
    public static final String[] DETAILS={
            "+12 WARDEN MAX HP",
            "HEAL 6 HP EVERY 8 ENEMY KILLS",
            "+4% WARDEN ATTACK",
            "-0.20s SKILL COOLDOWN",
            "+4% WARDEN MOVE SPEED",
            "+6% MORE WARDEN ATTACK"
    };
    /** Last new oath crossed by a single level gain, or -1 if none. */
    public static int latestUnlocked(int oldLevel,int newLevel){
        int found=-1;
        for(int i=0;i<LEVELS.length;i++)
            if(oldLevel<LEVELS[i]&&newLevel>=LEVELS[i])found=i;
        return found;
    }
    /** Next milestone or -1 at the capstone. */
    public static int nextIndex(int level){
        for(int i=0;i<LEVELS.length;i++)
            if(level<LEVELS[i])return i;
        return -1;
    }
    public static int currentIndex(int level){
        for(int i=LEVELS.length-1;i>=0;i--)
            if(level>=LEVELS[i])return i;
        return -1;
    }
    public static int healthBonus(int level){return level>=10?12:0;}
    public static float attackBonus(int level){
        return (level>=40?.04f:0f)+(level>=100?.06f:0f);
    }
    public static float moveBonus(int level){return level>=80?.04f:0f;}
    public static float cooldownBonus(int level){return level>=60?.20f:0f;}
    public static int healOnCreditedKill(int level,int kills){
        return level>=25&&kills>0&&kills%8==0?6:0;
    }
}
