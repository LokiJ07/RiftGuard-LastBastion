package com.lokij07.riftguard;

/**
 * Optional, physical interactions in 63 later-campaign battlefields.
 * The Warden must reach the exact drawn world-space ring and hold position.
 * One use per wave, no permanent resources, and no extra enemies or save keys.
 */
public final class RealmInteractablesV077 {
    private RealmInteractablesV077() {}
    public static final int NONE=0,WARD=1,FORGE=2,PURIFIER=3;
    public static final float RADIUS=81f,CHANNEL_SECONDS=1.65f;
    public static final float WARD_SECONDS=18f,FORGE_SECONDS=15f,PURIFIER_SECONDS=18f;
    public static final float WARD_FACTOR=.72f,FORGE_FACTOR=1.14f;
    private static final float[] X_CANDIDATES={1255f,1365f,1470f,1550f};

    public static boolean scheduled(int stage){
        Campaign.checkStage(stage);
        if(stage<30||SiegeMissionsV069.mode(stage)!=SiegeMissionsV069.NONE)
            return false;
        if((Campaign.level(stage)+Campaign.map(stage))%3!=0)return false;
        MemoryTrial.Objective trial=MemoryTrial.forStage(stage);
        return trial==null||trial.type!=MemoryTrial.RELIC;
    }
    public static int type(int stage){
        if(!scheduled(stage))return NONE;
        return 1+(Campaign.world(stage)+RealmActsV071.act(stage)+
                Campaign.map(stage))%3;
    }
    public static String name(int type){
        switch(type){
            case WARD:return "BASTION WARDSTONE";
            case FORGE:return "FIELD FORGE";
            case PURIFIER:return "STORM PURIFIER";
            default:return "";
        }
    }
    public static String guidance(int type){
        switch(type){
            case WARD:return "HOLD INSIDE THE GOLD RING • KEEP SHIELD";
            case FORGE:return "HOLD INSIDE THE GOLD RING • REPAIR + POWER";
            case PURIFIER:return "HOLD INSIDE THE GOLD RING • CALM HAZARDS";
            default:return "";
        }
    }
    /** The site is selected once per stage, not recalculated during rendering.
     * Maximize clearance from working tower pads, environmental hazards,
     * physical terrain pieces and the Warden spawn. All candidates are in
     * the actual HERO bounds, not in remote unreachable map decoration.
     */
    public static float[] position(int stage){
        Campaign.checkStage(stage);
        DestructibleTerrainV065 terrain=new DestructibleTerrainV065(stage);
        float best=Float.NEGATIVE_INFINITY,bestX=0f,bestY=0f;
        for(float x:X_CANDIDATES)for(int side=-1;side<=1;side+=2){
            float y=BattleRules.clamp(LaneRules.stagePathY(stage,x)+
                    side*167f,LaneRules.HERO_MIN_Y+18f,
                    LaneRules.HERO_MAX_Y-18f);
            if(MovementRules.blocksHero(x,y))continue;
            float min=600f;
            for(int i=0;i<TowerPadLayout.count(stage);i++)
                min=Math.min(min,distance(x,y,TowerPadLayout.x(stage,i),
                        TowerPadLayout.y(stage,i)));
            for(int i=0;i<StageHazards.count(stage);i++)
                min=Math.min(min,distance(x,y,StageHazards.x(stage,i),
                        StageHazards.y(stage,i))-.2f*StageHazards.radius(stage,i));
            for(int i=0;i<terrain.count();i++){
                DestructibleTerrainV065.Prop prop=terrain.at(i);
                min=Math.min(min,distance(x,y,prop.x,prop.y));
            }
            // Prefer clearance rather than a remote off-camera location.
            float score=min-Math.abs(x-1370f)*.13f;
            if(score>best){best=score;bestX=x;bestY=y;}
        }
        return new float[]{bestX,bestY};
    }
    public static float distance(float x,float y,float ax,float ay){
        float dx=x-ax,dy=y-ay;
        return (float)Math.sqrt(dx*dx+dy*dy);
    }
    public static boolean inRing(float x,float y,float sx,float sy){
        return Float.isFinite(x)&&Float.isFinite(y)&&
                distance(x,y,sx,sy)<=RADIUS;
    }
    public static float channel(float current,float dt,boolean near){
        if(!Float.isFinite(current)||!Float.isFinite(dt)||dt<0)
            throw new IllegalArgumentException("channel clock");
        return near?Math.min(CHANNEL_SECONDS,current+dt):
                Math.max(0f,current-dt*.8f);
    }
    public static float coreDamage(float raw,float wardRemaining){
        if(!Float.isFinite(raw)||raw<=0)return 0f;
        return wardRemaining>0?raw*WARD_FACTOR:raw;
    }
    public static float turretDamage(float raw,float forgeRemaining){
        if(!Float.isFinite(raw)||raw<=0)return 0f;
        return forgeRemaining>0?raw*FORGE_FACTOR:raw;
    }
    /** Repairs real built towers; never revives destroyed guns or hidden slots. */
    public static int mostDamagedPad(int[] tiers,float[] hp,int count,int rank){
        if(tiers==null||hp==null||count<0||count>tiers.length||
                count>hp.length)throw new IllegalArgumentException("pads");
        int result=-1;float deficit=.001f;
        for(int i=0;i<count;i++){
            if(tiers[i]<=0||hp[i]<=0)continue;
            float max=TowerDurability.maxHp(tiers[i],rank);
            float missing=Math.max(0f,max-hp[i]);
            if(missing>deficit){deficit=missing;result=i;}
        }
        return result;
    }
    public static float repair(float hp,float maxHp){
        if(!Float.isFinite(hp)||!Float.isFinite(maxHp)||maxHp<=0)
            throw new IllegalArgumentException("hp");
        return Math.min(maxHp,Math.max(0f,hp)+maxHp*.22f);
    }
}
