package com.lokij07.riftguard;

/** 96 live stage events. Original first-world 15 stay unchanged.
 * Later realms mix three classic defenses with two player-driven objectives;
 * no extra mobs or permanent progression key changes.
 */
public final class CampaignEncountersV073 {
    private CampaignEncountersV073(){}
    private static final int[] SLOTS={2,5,8,12,15,18,21,24,27};
    public static int mode(int stage){
        Campaign.checkStage(stage);
        if(stage>=6&&stage<=20)return SiegeMissionsV069.ESCORT+(stage-6)%3;
        if(stage<30||DifficultyRules.worldBoss(stage))return SiegeMissionsV069.NONE;
        for(int i=0;i<SLOTS.length;i++)if(SLOTS[i]==stage%30){
            // One Rescue and one Rift Seal per later realm, preserving each
            // existing mission type elsewhere in the nine-event schedule.
            if(i==4)return SiegeMissionsV069.RESCUE;
            if(i==7)return SiegeMissionsV069.SEAL;
            return SiegeMissionsV069.ESCORT+(i+Campaign.world(stage))%3;
        }
        return SiegeMissionsV069.NONE;
    }
    public static float cartMaxHp(int stage,int difficulty){
        Campaign.checkStage(stage);
        float hp=SiegeMissionsV069.CART_MAX_HP+Campaign.world(stage)*18f;
        if(difficulty==DifficultyRules.HARD)hp*=1.10f;
        if(difficulty==DifficultyRules.EXPERT)hp*=1.24f;
        return hp;
    }
    public static boolean guarding(float hx,float hy,float cx,float cy){
        return Float.isFinite(hx)&&Float.isFinite(hy)&&
                Float.isFinite(cx)&&Float.isFinite(cy)&&
                Math.hypot(hx-cx,hy-cy)<=155f;
    }
    public static float cartSpeed(int stage,boolean guarding){
        Campaign.checkStage(stage);
        return SiegeMissionsV069.CART_SPEED+Math.min(6f,Campaign.world(stage)*.65f)
                +(guarding?8f:0f);
    }
    public static float cartDamage(int stage,int difficulty,int enemyKind,boolean guarding){
        Campaign.checkStage(stage);
        float raw=(enemyKind==2?19f:11f)*
                Math.min(1.60f,DifficultyRules.enemyDamage(difficulty));
        return raw*(guarding?.61f:1f);
    }
    public static float engineDamage(int stage,int difficulty,float maxCore){
        Campaign.checkStage(stage);
        if(!Float.isFinite(maxCore)||maxCore<=0f)
            throw new IllegalArgumentException("max core");
        float raw=(65f+Campaign.world(stage)*3.2f)*
                Math.min(1.72f,DifficultyRules.enemyDamage(difficulty));
        return Math.min(raw,maxCore*.30f);
    }
    /** 9.5–16% health damage during a windup interrupts that shot. */
    public static float interruptFraction(int stage){
        Campaign.checkStage(stage);
        return Math.max(.095f,.16f-Campaign.world(stage)*.005f);
    }
    public static float engineHpMultiplier(int stage,int difficulty){
        Campaign.checkStage(stage);
        return difficulty>=DifficultyRules.HARD?1.35f:1.65f;
    }
    public static int bonusGold(int stage,int event){
        Campaign.checkStage(stage);
        return SiegeMissionsV069.bonusGold(event)+Math.min(36,Campaign.world(stage)*4);
    }
}
