package com.lokij07.riftguard;

/** Live, save-compatible missions across the 300-stage campaign. */
public final class SiegeMissionsV069 {
    private SiegeMissionsV069(){}
    public static final int NONE=0,ESCORT=1,ENGINE=2,SPLIT=3,RESCUE=4,SEAL=5;
    public static final int FIRST=6,LAST=20,FEATURE_WAVE=2;
    public static final float CART_START_X=1620f,CART_SAFE_X=1250f;
    public static final float CART_MAX_HP=260f,CART_SPEED=26f;
    public static final float ENGINE_WINDUP=1.5f,ENGINE_INTERVAL=7.5f;
    public static int mode(int stage){
        Campaign.checkStage(stage);
        return CampaignEncountersV073.mode(stage);
    }
    public static boolean active(int stage,int wave){
        return wave==FEATURE_WAVE&&mode(stage)!=NONE;
    }
    /** One engine REPLACES one existing spawn; enemy counts never increase. */
    public static boolean engineSpawn(int stage,int wave,int remaining,int total){
        return active(stage,wave)&&mode(stage)==ENGINE&&
                remaining==Math.max(2,total/2);
    }
    public static boolean flankSpawn(int stage,int wave,int remaining){
        return active(stage,wave)&&mode(stage)==SPLIT&&remaining%3==0;
    }
    public static int flankSide(int remaining){return remaining%2==0?-1:1;}
    public static float flankOffset(float x,int side){
        if(side!=-1&&side!=1)throw new IllegalArgumentException("flank side");
        return side*135f*BattleRules.clamp((x-LaneRules.CASTLE_GATE_X)/390f,0f,1f);
    }
    public static float cartNext(float x,float dt){
        if(!Float.isFinite(x)||!Float.isFinite(dt)||dt<0)
            throw new IllegalArgumentException("cart movement");
        return Math.max(CART_SAFE_X,x-CART_SPEED*dt);
    }
    public static boolean cartSafe(float x){return x<=CART_SAFE_X+.01f;}
    public static String name(int stage){
        switch(mode(stage)){
            case ESCORT:return "PROTECT THE REFUGEE CARAVAN";
            case ENGINE:return "INTERCEPT THE SIEGE ENGINE";
            case SPLIT:return "DEFEND AGAINST SPLIT ROUTES";
            case RESCUE:return "RESCUE THE STRANDED VILLAGERS";
            case SEAL:return "CLOSE THE UNSTABLE RIFTS";
            default:return "";
        }
    }
    public static String orders(int stage){
        switch(mode(stage)){
            case ESCORT:return "Wave two: stay close to shield and rally the moving caravan; losing it fails the battle.";
            case ENGINE:return "Wave two: interrupt the siege cannon by damaging it during its visible windup.";
            case SPLIT:return "Wave two: attackers use two entry paths. Move to intercept and cover your turret positions.";
            case RESCUE:return "Wave two: approach stranded villagers on the road to rescue them. Save at least two of three before raiders kill them.";
            case SEAL:return "Wave two: stand near both exposed rifts until their seals finish charging. Unsealed portals periodically strike your keep.";
            default:return "";
        }
    }
    public static String warning(int stage){
        switch(mode(stage)){
            case ESCORT:return "The golden caravan has health. Staying close shields it and speeds its escape.";
            case ENGINE:return "Attack during the glowing cannon windup to cancel the siege shot.";
            case SPLIT:return "Purple arrow markers reveal flankers from two approaches converging at the gate.";
            case RESCUE:return "Gold signals mark three villagers. Walk to their circles; nearby enemies will attack anyone not rescued.";
            case SEAL:return "Two glowing portals are open. Hold within each marked ring to channel its seal, or your fortress takes pulse damage.";
            default:return "";
        }
    }
    public static int bonusGold(int kind){
        return kind==ESCORT?45:kind==ENGINE?35:kind==SPLIT?30:
                kind==RESCUE?55:kind==SEAL?60:0;
    }
}
