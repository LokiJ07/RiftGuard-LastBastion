package com.lokij07.riftguard;

/** Optional tactical challenges; no mission, memory or purchase is gated by them. */
public final class BattleContractsV068 {
    private BattleContractsV068(){}
    public static final int NONE=-1,SPELLCASTER=0,ENGINEER=1,STONEWALL=2,
            VIGIL=3,GARRISON=4,VARIANTS=5;
    public static int kind(int stage){
        Campaign.checkStage(stage);
        if(stage<3)return NONE;
        return (Campaign.level(stage)+2*Campaign.map(stage)+Campaign.world(stage))%VARIANTS;
    }
    public static String name(int stage){
        switch(kind(stage)){
            case SPELLCASTER:return "CAST 2 SKILLS";
            case ENGINEER:return "UPGRADE 1 TURRET";
            case STONEWALL:return "KEEP CORE ABOVE 85%";
            case VIGIL:return "KEEP WARDEN ABOVE 70%";
            case GARRISON:return "HOLD 2 TURRETS • NO LOSSES";
            default:return "NO FIELD CONTRACT";
        }
    }
    private static void check(int[] towers,int active){
        if(towers==null||active<0||active>towers.length)
            throw new IllegalArgumentException("active towers");
    }
    public static int activeTowers(int[] towers,int active){
        check(towers,active);int count=0;
        for(int i=0;i<active;i++)if(towers[i]>0)count++;
        return count;
    }
    public static int upgradedTowers(int[] towers,int active){
        check(towers,active);int count=0;
        for(int i=0;i<active;i++)if(towers[i]>=2)count++;
        return count;
    }
    public static boolean completed(int stage,int casts,int[] towers,int active,
                                    float core,float hero,int lost){
        if(casts<0||lost<0||!Float.isFinite(core)||!Float.isFinite(hero))return false;
        switch(kind(stage)){
            case SPELLCASTER:return casts>=2;
            case ENGINEER:return upgradedTowers(towers,active)>=1;
            case STONEWALL:return core>=.85f;
            case VIGIL:return hero>=.70f;
            case GARRISON:return lost==0&&activeTowers(towers,active)>=2;
            default:return false;
        }
    }
    public static String progress(int stage,int casts,int[] towers,int active,
                                  float core,float hero,int lost){
        switch(kind(stage)){
            case SPELLCASTER:return Math.min(2,Math.max(0,casts))+"/2 SKILLS";
            case ENGINEER:return Math.min(1,upgradedTowers(towers,active))+"/1 UPGRADE";
            case STONEWALL:return Math.max(0,Math.round(core*100))+"% CORE (85% TARGET)";
            case VIGIL:return Math.max(0,Math.round(hero*100))+"% WARDEN (70% TARGET)";
            case GARRISON:return activeTowers(towers,active)+" TOWERS • "+lost+" LOST";
            default:return "FIRST DEFENSE";
        }
    }
    public static int firstClearBonus(int stage){
        Campaign.checkStage(stage);
        return kind(stage)==NONE?0:24+4*Campaign.world(stage);
    }
}
