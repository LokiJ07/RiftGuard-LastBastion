package com.lokij07.riftguard;

/**
 * Eight named allies recruited through story-world boss clear.
 * They operate real, bounded combat support rather than fake cosmetic buffs.
 */
public final class AllySupportV065 {
    private AllySupportV065(){}
    public static final int DORIN=0,MIRA=1,HALE=2,GARRICK=3,
            YSOLDE=4,PELL=5,LUME=6,NESSA=7,COUNT=8;
    public static final String[] NAMES={
            "DORIN","MIRA","BROTHER HALE","GARRICK",
            "CAPTAIN YSOLDE","PELL","LUME","NESSA"
    };
    public static final String[] ACTIONS={
            "FIELD REPAIRS","SCOUT'S MARK","HEALING PRAYER","FORGE BLESSING",
            "CORE GUARD","EXTRA ARTILLERY","ARCANE FOCUS","STORM STRIKE"
    };
    private static final float[] SECONDS={23,17,21,29,25,22,26,28};
    public static boolean unlocked(StoryProgress story,int ally){
        check(ally);
        return story.hasOwnedSeal(ally);
    }
    public static int recruited(StoryProgress story){
        int n=0;for(int i=0;i<COUNT;i++)if(unlocked(story,i))n++;
        return n;
    }
    public static float interval(int ally){check(ally);return SECONDS[ally];}
    public static float initialDelay(int ally){check(ally);return 4f+ally*2.8f;}
    public static float heal(float current,float maximum,float amount){
        return Math.min(Math.max(0,maximum),Math.max(0,current)+Math.max(0,amount));
    }
    public static float wardenDamageMultiplier(boolean forged){return forged?1.14f:1f;}
    private static void check(int ally){
        if(ally<0||ally>=COUNT)throw new IllegalArgumentException("ally");
    }
}
