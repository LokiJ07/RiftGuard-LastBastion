package com.lokij07.riftguard;

/**
 * Authoritative pad geometry for combat, rendering, building and exclusion.
 * Every displayed pad has a real construction action; only the four original
 * supports attach to the fort. The remaining pads are ground towers.
 *
 * Mission A: 5 or 6, B: 6 or 7, C: 7 or 8, with later realms
 * earning an extra usable build site. No WebView or fake map markers.
 */
public final class TowerPadLayout {
    private TowerPadLayout(){}
    public static final int MAX=8;
    private static final float[] OLD_X={810f,1092f,811f,1093f};
    private static final float[] OLD_Y={370f,370f,624f,624f};
    private static final float[] EXTRA_X={1220f,1402f,1590f,1748f};

    public static int count(int stage){
        Campaign.checkStage(stage);
        return Math.min(MAX,5+Campaign.map(stage)+(Campaign.world(stage)>=5?1:0));
    }
    public static float x(int stage,int slot){
        require(stage,slot);
        return slot<4?OLD_X[slot]:EXTRA_X[slot-4];
    }
    public static float y(int stage,int slot){
        require(stage,slot);
        if(slot<4)return OLD_Y[slot];
        float path=LaneRules.stagePathY(stage,EXTRA_X[slot-4]);
        float wanted=path+(slot%2==0?-220f:220f);
        if(wanted<195f||wanted>790f)wanted=path+(slot%2==0?220f:-220f);
        return BattleRules.clamp(wanted,195f,790f);
    }
    private static void require(int stage,int slot){
        Campaign.checkStage(stage);
        if(slot<0||slot>=count(stage))
            throw new IllegalArgumentException("inactive build slot "+slot);
    }
    public static int family(int slot){
        if(slot<0||slot>=MAX)throw new IllegalArgumentException("slot");
        return slot%4;
    }
    public static boolean clearOfRoad(int stage,int slot){
        if(slot<4)return true;
        return Math.abs(y(stage,slot)-LaneRules.stagePathY(stage,x(stage,slot)))>=180f;
    }
}