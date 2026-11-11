package com.lokij07.riftguard;

/**
 * Three map variations, one readable right-to-left enemy lane.
 * Coordinates are game-world units so battle remains deterministic offline.
 */
public final class LaneRules {
    private LaneRules() {}
    public static final float VIEW_CENTER_X=1380f,VIEW_CENTER_Y=520f;
    public static final float HERO_MIN_X=1115f,HERO_MAX_X=1590f;
    public static final float HERO_MIN_Y=332f,HERO_MAX_Y=755f;
    public static final float ENEMY_SPAWN_X=1836f;
    public static final float CASTLE_GATE_X=1104f;
    public static float heroX(float value) {
        return BattleRules.clamp(value,HERO_MIN_X,HERO_MAX_X);
    }
    public static float heroY(float value) {
        return BattleRules.clamp(value,HERO_MIN_Y,HERO_MAX_Y);
    }
    public static float startX(){return 1234f;}
    public static float startY(){return 614f;}
    public static float spawnY(int variant,float offset) {
        float center=variant==1?405f:variant==2?598f:512f;
        return BattleRules.clamp(center+BattleRules.clamp(offset,-58f,58f),285f,780f);
    }
    public static float waypointX(int variant) {
        return variant==1?1500f:variant==2?1420f:CASTLE_GATE_X;
    }
    public static float waypointY(int variant) {
        return variant==1?560f:variant==2?463f:510f;
    }
    public static float targetX(float x,int variant) {
        return x>waypointX(variant)+20f ? waypointX(variant):950f;
    }
    public static float targetY(float x,int variant) {
        return x>waypointX(variant)+20f ? waypointY(variant):510f;
    }
    public static float pathY(int variant,float x) {
        float wpX=waypointX(variant),wpY=waypointY(variant);
        if(x>wpX) {
            float weight=BattleRules.clamp((x-wpX)/(ENEMY_SPAWN_X-wpX),0f,1f);
            float from=variant==1?405f:variant==2?598f:510f;
            return wpY+(from-wpY)*weight;
        }
        float weight=BattleRules.clamp((x-950f)/(wpX-950f),0f,1f);
        return 510f+(wpY-510f)*weight;
    }
    public static boolean coversPath(int variant,float x,float y) {
        return x>=1090f && x<=1880f
                && Math.abs(y-pathY(variant,x))<108f;
    }
}
