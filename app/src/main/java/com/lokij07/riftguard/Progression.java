package com.lokij07.riftguard;

/** Permanent fortress progression and equipable weapons; independent of Android. */
public final class Progression {
    private Progression() {}
    public static final int MAX_RANK=5;
    public static final class Weapon {
        public final String name, type, effect;
        public final int requiredRank, color;
        public final float damage, cooldown, range;
        public Weapon(String name,String type,String effect,int requiredRank,int color,
                      float damage,float cooldown,float range) {
            this.name=name;this.type=type;this.effect=effect;this.requiredRank=requiredRank;
            this.color=color;this.damage=damage;this.cooldown=cooldown;this.range=range;
        }
    }
    // Each model has its own combat timing, reachable target radius and projectile visual.
    public static final Weapon[] WEAPONS={
        new Weapon("Warden Crossbow","RANGED","STEEL BOLT",1,0xff8bd9e1,34,.25f,480),
        new Weapon("Iron Greatsword","MELEE","CRESCENT SLASH",1,0xffdfeaf3,75,.39f,118),
        new Weapon("Ember Sabre","MELEE","BURNING CLEAVE",2,0xffff9c54,105,.43f,145),
        new Weapon("Arc Staff","MAGIC","CHAIN LIGHTNING",3,0xffaab6ff,59,.38f,500),
        new Weapon("Siege Repeater","RANGED","REPEATING BOLTS",4,0xffffc277,45,.13f,520),
        new Weapon("Frost Lance","MAGIC","FROST SHARD",5,0xff9cf2ff,125,.65f,560)
    };
    public static final String[] RANK_NAMES={"","Woodland Outpost","Reinforced Camp",
             "Stone Keep","War Stronghold","Arc Citadel"};
    // Index is the rank being purchased. Rewards come from campaign victories.
    private static final int[] COST={0,0,120,300,650,1100};
    public static int upgradeCost(int rank) {
        if(rank<1||rank>MAX_RANK)throw new IllegalArgumentException("Invalid rank");
        return rank==MAX_RANK?0:COST[rank+1];
    }
    public static boolean canUpgrade(int rank,int supply) {
        return rank>=1 && rank<MAX_RANK && supply>=upgradeCost(rank);
    }
    public static boolean canEquip(int rank,int weaponId) {
        return rank>=1 && rank<=MAX_RANK && weaponId>=0 && weaponId<WEAPONS.length
                && WEAPONS[weaponId].requiredRank<=rank;
    }
    public static int stageReward(int stage,boolean firstClear) {
        Campaign.checkStage(stage);
        return firstClear?120+10*Campaign.world(stage)+15*Campaign.map(stage):45;
    }
    public static int maxCoreHp(int rank) {
        if(rank<1||rank>MAX_RANK)throw new IllegalArgumentException("Invalid rank");
        return 1000+(rank-1)*235;
    }
}
