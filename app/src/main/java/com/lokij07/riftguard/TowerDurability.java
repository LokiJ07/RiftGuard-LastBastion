package com.lokij07.riftguard;

/**
 * Damage, rubble and repairs are battle-local. Purchased fort ranks, tower
 * blueprints and their upgrades persist independently; no destructive save IO.
 */
public final class TowerDurability {
    private TowerDurability(){}
    public static float maxHp(int tier,int fortressRank){
        if(tier<1||tier>BattleRules.MAX_TOWER_TIER)
            throw new IllegalArgumentException("tower tier");
        return 110f+tier*75f+Math.max(1,fortressRank)*25f;
    }
    public static float damage(float hp,float amount){
        return Math.max(0f,hp-Math.max(0f,amount));
    }
    public static int repairCost(float hp,float maxHp){
        if(maxHp<=0||hp>=maxHp)return 0;
        return Math.max(8,(int)Math.ceil((maxHp-Math.max(0,hp))*.20f));
    }
    public static float repair(float hp,float maxHp){
        return Math.min(maxHp,Math.max(0,hp)+maxHp*.40f);
    }
    public static int rebuildCost(int destroyedTier){
        return destroyedTier<1?BattleRules.towerCost(0):
                BattleRules.towerCost(0)+destroyedTier*22;
    }
    public static int maxType(){return 4;}
    public static int clampType(int type){return Math.max(0,Math.min(3,type));}
}
