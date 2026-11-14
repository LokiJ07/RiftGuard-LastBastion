package com.lokij07.riftguard;

import android.content.SharedPreferences;

/**
 * Save-backed Warden RPG levels, independent of 100 CAMPAIGN levels.
 * Lifetime XP uniquely determines character level and lifetime skill points.
 * Spent attribute points are the ONLY other permanent keys; there is no
 * separately stored "unspent" balance to drift, duplicate or be exploited.
 *
 * An EXP grant is planned in pure math and durably committed in the SAME
 * transaction as stage unlock, supply payout and checkpoint deletion.
 */
public final class WardenLevelsV078 {
    public static final int MAX_LEVEL=100,MAX_ATTRIBUTE=120,ATTRIBUTE_COUNT=4;
    public static final int VITALITY=0,POWER=1,AGILITY=2,ARCANA=3;
    public static final String SAVE_XP="warden.level.v078.total_xp";
    public static final String[] NAMES={"VITALITY","POWER","AGILITY","ARCANA"};
    public static final String[] DESCRIPTIONS={
            "+2 MAX WARDEN HP EACH POINT",
            "+0.16% WARDEN ATTACK EACH POINT",
            "+0.09% WARDEN MOVE SPEED EACH POINT",
            "-0.015s SPELL COOLDOWN EACH POINT"
    };
    private static final String[] STAT_KEYS={
            "warden.level.v078.stat.vitality","warden.level.v078.stat.power",
            "warden.level.v078.stat.agility","warden.level.v078.stat.arcana"
    };
    private static final float[] DIFFICULTY_XP={.78f,1f,1.25f,1.55f};
    private static final int MAX_XP=totalToLevel(MAX_LEVEL);
    private final SharedPreferences prefs;
    private int totalXp;
    private final int[] allocated=new int[ATTRIBUTE_COUNT];

    public static final class Award {
        public final int xpGained,newTotalXp,oldLevel,newLevel,levelsGained,pointsGained;
        private Award(int xp,int total,int old,int next){
            xpGained=xp;newTotalXp=total;oldLevel=old;newLevel=next;
            levelsGained=next-old;pointsGained=
                    lifetimePoints(next)-lifetimePoints(old);
        }
    }

    public WardenLevelsV078(SharedPreferences prefs){
        if(prefs==null)throw new IllegalArgumentException("prefs");
        this.prefs=prefs;
        // Existing save migration: award original NORMAL first-clear EXP
        // for *already unlocked/cleared* stages, never based on total kills,
        // money or other untrusted counts. Not repeated on restart/replay.
        if(!prefs.contains(SAVE_XP)){
            int cleared=Math.max(0,Math.min(Campaign.STAGES,
                    prefs.getInt("unlocked",0)));
            int migrated=0;
            for(int s=0;s<cleared;s++)
                migrated=Math.min(MAX_XP,migrated+stageXp(s,DifficultyRules.NORMAL,true));
            prefs.edit().putInt(SAVE_XP,migrated).commit();
        }
        totalXp=Math.max(0,Math.min(MAX_XP,prefs.getInt(SAVE_XP,0)));
        int budget=lifetimePoints(levelForXp(totalXp));
        for(int i=0;i<ATTRIBUTE_COUNT;i++){
            int requested=Math.max(0,Math.min(MAX_ATTRIBUTE,prefs.getInt(STAT_KEYS[i],0)));
            allocated[i]=Math.min(requested,budget);
            budget-=allocated[i]; // clamp impossible/corrupt total allocations
        }
    }

    /** Strictly increasing XP cost for each successive level. */
    public static int xpToNextLevel(int currentLevel){
        if(currentLevel<1||currentLevel>MAX_LEVEL)
            throw new IllegalArgumentException("Warden level");
        if(currentLevel==MAX_LEVEL)return 0;
        return Math.round(155f+14f*currentLevel+.40f*currentLevel*currentLevel);
    }
    /** Total XP needed to REACH the named level (LV 1 = 0). */
    public static int totalToLevel(int level){
        if(level<1||level>MAX_LEVEL)
            throw new IllegalArgumentException("Warden level");
        int sum=0;
        for(int i=1;i<level;i++)sum+=xpToNextLevel(i);
        return sum;
    }
    public static int maxXp(){return MAX_XP;}
    public static int levelForXp(int experience){
        int remaining=Math.max(0,Math.min(MAX_XP,experience));
        int lvl=1;
        while(lvl<MAX_LEVEL&&remaining>=xpToNextLevel(lvl)){
            remaining-=xpToNextLevel(lvl);
            lvl++;
        }
        return lvl;
    }
    /** Each NEW level awards progressively more points in five fair tiers. */
    public static int pointsOnLevel(int newLevel){
        if(newLevel<2||newLevel>MAX_LEVEL)
            throw new IllegalArgumentException("new Warden level");
        return newLevel<20?2:newLevel<40?3:
                newLevel<60?4:newLevel<80?5:6;
    }
    public static int lifetimePoints(int level){
        if(level<1||level>MAX_LEVEL)
            throw new IllegalArgumentException("Warden level");
        int sum=0;
        for(int v=2;v<=level;v++)sum+=pointsOnLevel(v);
        return sum;
    }
    /** 300 first-clears on NORMAL approximately reach level 100, not level 30.
     * EASY earns 78%, HARD 125%, EXPERT 155%; replay is only 22%
     * to limit endlessly farming trivial stages.
     */
    public static int stageXp(int stage,int difficulty,boolean firstClear){
        Campaign.checkStage(stage);
        int base=115+4*stage+(Campaign.map(stage)==2?30:0);
        float replay=firstClear?1f:.22f;
        return Math.max(1,Math.round(base*DIFFICULTY_XP[DifficultyRules.safe(difficulty)]*replay));
    }
    public int totalXp(){return totalXp;}
    public int level(){return levelForXp(totalXp);}
    public int xpIntoLevel(){
        if(level()>=MAX_LEVEL)return 0;
        return totalXp-totalToLevel(level());
    }
    public int xpNeeded(){return xpToNextLevel(level());}
    public float progress(){return level()>=MAX_LEVEL?1f:
            xpIntoLevel()/(float)Math.max(1,xpNeeded());}
    public int attribute(int stat){checkStat(stat);return allocated[stat];}
    private static void checkStat(int stat){
        if(stat<0||stat>=ATTRIBUTE_COUNT)
            throw new IllegalArgumentException("attribute");
    }
    public int unspent(){
        int left=lifetimePoints(level());
        for(int value:allocated)left-=value;
        return Math.max(0,left);
    }
    /** Side-effect-free preview for ONE genuinely completed campaign fight. */
    public Award planVictory(int stage,int difficulty,boolean firstClear){
        int target=Math.min(MAX_XP,totalXp+stageXp(stage,difficulty,firstClear));
        return new Award(target-totalXp,target,level(),levelForXp(target));
    }
    /** Called only AFTER the same atomic stage-commit succeeds. */
    public void applyCommittedAward(Award award){
        if(award==null||award.oldLevel!=level()||
                award.newTotalXp<totalXp||award.newTotalXp>MAX_XP)
            throw new IllegalArgumentException("out-of-order Warden EXP");
        totalXp=award.newTotalXp;
    }
    /** No supplies consumed. Stats may be upgraded in single or five-point
     * purchases; invalid, insufficient, full or failed commits do nothing.
     */
    public boolean spend(int stat,int points){
        checkStat(stat);
        if(points<1||points>5||unspent()<points||
                allocated[stat]+points>MAX_ATTRIBUTE)return false;
        int next=allocated[stat]+points;
        if(!prefs.edit().putInt(STAT_KEYS[stat],next).commit())return false;
        allocated[stat]=next;
        return true;
    }
    /** Modest, capped Warden-only bonuses; do NOT boost fortress turrets. */
    public int healthBonus(){return 2*allocated[VITALITY]+WardenMilestonesV079.healthBonus(level());}
    public float attackMultiplier(){return 1f+allocated[POWER]*.0016f+WardenMilestonesV079.attackBonus(level());}
    public float moveMultiplier(){return 1f+allocated[AGILITY]*.0009f+WardenMilestonesV079.moveBonus(level());}
    public float skillCooldownReduction(){return allocated[ARCANA]*.015f+WardenMilestonesV079.cooldownBonus(level());}
}
