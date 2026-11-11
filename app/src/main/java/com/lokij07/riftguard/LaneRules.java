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

    /**
     * Authored gameplay rule: every stage has one unique deterministic winding lane.
     * The renderer, spawn system, enemy AI and scenery exclusion all use the
     * SAME geometry. This is not 300 individually painted battlegrounds.
     */
    public static int routeSignature(int stage) {
        Campaign.checkStage(stage);
        int v=Campaign.seed(stage) ^ 0x7F4A7C15;
        v ^= v>>>16; v*=0x7feb352d; v^=v>>>15;
        return v & 0x7fffffff;
    }
    public static float stageBendX(int stage) {
        int variant=Campaign.map(stage);
        float base=variant==1?1495f:variant==2?1435f:1515f;
        return BattleRules.clamp(base+(routeSignature(stage)%5-2)*38f,1330f,1690f);
    }
    public static float stageBendY(int stage) {
        float base=Campaign.map(stage)==1?548f:Campaign.map(stage)==2?458f:508f;
        return BattleRules.clamp(base + (Math.floorMod(routeSignature(stage)>>>5,7)-3)*26f,
                350f,695f);
    }
    public static float stageSpawnY(int stage) {
        float base=Campaign.map(stage)==1?415f:Campaign.map(stage)==2?608f:506f;
        return BattleRules.clamp(base+(Math.floorMod(routeSignature(stage)>>>9,7)-3)*27f,
                325f,745f);
    }
    private static float smooth(float t) {
        t=BattleRules.clamp(t,0f,1f);
        return t*t*(3f-2f*t);
    }
    public static float stagePathY(int stage,float x) {
        float gateY=510f;
        float bendX=stageBendX(stage),bendY=stageBendY(stage);
        int sig=routeSignature(stage);
        float sway=((sig>>>16)%5-2)*10f;
        if(x<=CASTLE_GATE_X)return gateY;
        if(x<bendX) {
            float t=(x-CASTLE_GATE_X)/(bendX-CASTLE_GATE_X);
            return gateY+(bendY-gateY)*smooth(t)
                    +(float)Math.sin(Math.PI*t)*sway;
        }
        float t=(x-bendX)/(ENEMY_SPAWN_X-bendX);
        return bendY+(stageSpawnY(stage)-bendY)*smooth(t)
                +(float)Math.sin(Math.PI*t)*sway;
    }
    public static float stageWaypointX(float enemyX,int stage) {
        return Math.max(CASTLE_GATE_X-16f,enemyX-78f);
    }
    public static float stageWaypointY(float enemyX,int stage) {
        return stagePathY(stage,stageWaypointX(enemyX,stage));
    }
    public static float stageEnemyY(int stage,float offset) {
        return BattleRules.clamp(stageSpawnY(stage)+BattleRules.clamp(offset,-48f,48f),
                290f,785f);
    }
    public static boolean stageCoversPath(int stage,float x,float y) {
        return x>=CASTLE_GATE_X-65f&&x<=ENEMY_SPAWN_X+45f&&
                Math.abs(y-stagePathY(stage,x))<113f;
    }
    public static boolean coversPath(int variant,float x,float y) {
        return x>=1090f && x<=1880f
                && Math.abs(y-pathY(variant,x))<108f;
    }
}
