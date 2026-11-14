package com.lokij07.riftguard;

/**
 * Two actual non-combat-click objectives; positions are world coordinates,
 * bounded by the Warden's movable camera-lane. No assets or save keys needed.
 *
 * Rescue: walk to three threatened villagers before a majority is lost.
 * Rift seal: physically stand in each of two portals to channel the seal.
 */
public final class FieldObjectivesV074 {
    private FieldObjectivesV074(){}
    public static final int SURVIVORS=3,SEALS=2;
    public static final float RESCUE_RADIUS=76f,SEAL_RADIUS=93f;
    public static final float SEAL_SECONDS=2.1f,SEAL_PULSE_INTERVAL=8.6f;
    private static void check(int index,int max){
        if(index<0||index>=max)throw new IllegalArgumentException("field index");
    }
    public static float rescueX(int stage,int index){
        Campaign.checkStage(stage);check(index,SURVIVORS);
        return 1306f+index*120f;
    }
    public static float rescueY(int stage,int index){
        Campaign.checkStage(stage);check(index,SURVIVORS);
        return BattleRules.clamp(LaneRules.stagePathY(stage,rescueX(stage,index))+
                (index==1?60f:-55f),LaneRules.HERO_MIN_Y+20f,
                LaneRules.HERO_MAX_Y-20f);
    }
    public static float sealX(int stage,int index){
        Campaign.checkStage(stage);check(index,SEALS);
        return 1355f+index*195f;
    }
    public static float sealY(int stage,int index){
        Campaign.checkStage(stage);check(index,SEALS);
        return BattleRules.clamp(LaneRules.stagePathY(stage,sealX(stage,index))+
                (index==0?65f:-65f),LaneRules.HERO_MIN_Y+20f,
                LaneRules.HERO_MAX_Y-20f);
    }
    public static boolean near(float x,float y,float tx,float ty,float radius){
        if(!Float.isFinite(x)||!Float.isFinite(y)||
                !Float.isFinite(tx)||!Float.isFinite(ty)||radius<0)return false;
        float dx=x-tx,dy=y-ty;
        return dx*dx+dy*dy<=radius*radius;
    }
    public static float survivorMaxHp(int stage,int difficulty){
        Campaign.checkStage(stage);
        return (130f+Campaign.world(stage)*10f)*
                (difficulty==DifficultyRules.EXPERT?1.22f:
                difficulty==DifficultyRules.HARD?1.10f:1f);
    }
    public static float survivorHit(int stage,int difficulty,int kind){
        Campaign.checkStage(stage);
        return (kind==2?23f:kind==5?28f:14f)*
                Math.min(1.60f,DifficultyRules.enemyDamage(difficulty));
    }
    public static float sealDamage(int stage,int difficulty,float maxCoreHp){
        Campaign.checkStage(stage);
        if(!Float.isFinite(maxCoreHp)||maxCoreHp<=0)
            throw new IllegalArgumentException("maxCoreHp");
        return Math.min(maxCoreHp*.065f,
                (15f+Campaign.world(stage)*1.7f)*
                        Math.min(1.65f,DifficultyRules.enemyDamage(difficulty)));
    }
    public static float charge(float existing,float delta,boolean nearby){
        if(!Float.isFinite(existing)||!Float.isFinite(delta)||delta<0)
            throw new IllegalArgumentException("seal charge");
        if(!nearby)return existing;
        return Math.min(SEAL_SECONDS,existing+delta);
    }
    public static boolean canAdvanceRescue(int saved,int lost){
        return saved>=2&&saved+lost==SURVIVORS;
    }
}
