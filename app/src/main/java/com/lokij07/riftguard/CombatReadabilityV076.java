package com.lokij07.riftguard;

/**
 * One-hit fairness + actionable mission instructions from LIVE battle data.
 * No new mobs, waves, resources, scene layers, or permanent save fields.
 * First-realm tutorial hits are unchanged; multi-hit tactical challenge,
 * difficulty, shield breaking and boss abilities still apply.
 */
public final class CombatReadabilityV076 {
    private CombatReadabilityV076(){}
    public static final float HERO_STRIKE_LIMIT=.29f;
    public static final float TOWER_STRIKE_LIMIT=.24f;
    public static final float KEEP_STRIKE_LIMIT=.17f;
    public static final float RELIC_STRIKE_LIMIT=.20f;

    private static float cap(int stage,float raw,float max,float ratio){
        Campaign.checkStage(stage);
        if(stage<30)return raw;
        if(!Float.isFinite(raw)||raw<=0||!Float.isFinite(max)||max<=0)return 0f;
        return Math.min(raw,max*ratio);
    }
    public static float heroHit(int stage,float raw,float heroMax){
        return cap(stage,raw,heroMax,HERO_STRIKE_LIMIT);
    }
    public static float towerHit(int stage,float raw,float towerMax){
        return cap(stage,raw,towerMax,TOWER_STRIKE_LIMIT);
    }
    public static float coreHit(int stage,float raw,float coreMax){
        return cap(stage,raw,coreMax,KEEP_STRIKE_LIMIT);
    }
    public static float relicHit(int stage,float raw,float relicMax){
        return cap(stage,raw,relicMax,RELIC_STRIKE_LIMIT);
    }
    public static String missionAction(int type,boolean cartGuarded,float cartFraction,
                                       int saved,int lost,int sealed,float chargeRemaining,
                                       float portalPulseTimer,float cannonCharge){
        switch(type){
            case SiegeMissionsV069.ESCORT:
                if(cartFraction<.31f)return "CART CRITICAL • STAND CLOSE TO SHIELD IT";
                return cartGuarded?"CART SHIELDED • STAY WITH REFUGEES":
                        "MOVE NEXT TO THE CART • GUARD = DAMAGE -39%";
            case SiegeMissionsV069.ENGINE:
                return cannonCharge>0?"CANNON "+tenths(cannonCharge)+
                        "s • HIT ENGINE TO INTERRUPT":
                        "FIND THE ARMORED ENGINE • STOP ITS NEXT SHOT";
            case SiegeMissionsV069.SPLIT:
                return "WATCH BOTH BRANCHES • INTERCEPT RAIDERS";
            case SiegeMissionsV069.RESCUE:
                if(saved+lost>=FieldObjectivesV074.SURVIVORS)
                    return "SURVIVORS SECURED • DEFEND THE KEEP";
                if(lost>0)return "VILLAGER LOST • SAVE THE OTHER TWO!";
                if(saved>=2)return "SAVE THE FINAL VILLAGER FOR EXTRA GOLD";
                return "RUN TO GOLD CIRCLES • RESCUE 2 OF 3";
            case SiegeMissionsV069.SEAL:
                if(sealed>=2)return "BOTH RIFTS SEALED • DEFEND THE KEEP";
                if(chargeRemaining>0)return "HOLD INSIDE RIFT • "+
                        tenths(chargeRemaining)+"s LEFT";
                return "ENTER GLOWING RIFT • PULSE IN "+
                        Math.max(0,(int)Math.ceil(portalPulseTimer))+"s";
            default:return "";
        }
    }
    public static String raidAction(int pad,float hpFraction){
        if(pad<0)return "";
        return hpFraction<.30f?"PAD "+(pad+1)+" CRITICAL • REPAIR OR INTERCEPT":
                "PAD "+(pad+1)+" UNDER RAID • DRAW ATTACKERS AWAY";
    }
    public static String tacticalAction(boolean pincer,boolean retreating){
        return retreating?"HEALER WITHDRAWING • BREAK ITS SHIELD":
                pincer?"PINCER RAID • WATCH TWO APPROACHES":"";
    }
    private static String tenths(float value){
        if(!Float.isFinite(value))value=0;
        int units=Math.max(0,Math.round(value*10f));
        return (units/10)+"."+(units%10);
    }
}
