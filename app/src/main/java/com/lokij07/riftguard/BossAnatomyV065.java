package com.lokij07.riftguard;

/**
 * Three individually damageable boss components. Actual component durability
 * is separate from the main boss HP, so damaging an armor plate never heals or
 * resets health. The same source data drives weapon abilities, visuals and UI.
 */
public final class BossAnatomyV065 {
    public static final int ARMOR=0,WEAPON=1,CORE=2,PARTS=3;
    private static final String[][] PART_NAMES={
        {"THORN PLATES","WAR HORN","HEART KNOT"},
        {"BARK SHELL","ROOT CROWN","HOLLOW HEART"},
        {"SCALE ARMOR","POISON SACS","DEEP CORE"},
        {"LAVA PLATES","FLAME HORNS","EMBER HEART"},
        {"ICE SHELL","WHITE HORNS","FROST HEART"},
        {"IRON ARMOR","SHOULDER CANNON","FORGE CORE"},
        {"CRYSTAL PLATES","MIRROR SHARDS","REFLECTED HEART"},
        {"STORM ARMOR","LIGHTNING CROWN","TEMPEST CORE"},
        {"SHADOW MAIL","SPECTRAL BLADE","NIGHT HEART"},
        {"RIFT ARMOR","CORRUPTED SWORD","CAEL'S HEART"}
    };
    private final float[] hp=new float[PARTS],maxHp=new float[PARTS];
    private final int world;
    public BossAnatomyV065(int world,float bossMaxHp){
        if(world<0||world>=10)throw new IllegalArgumentException("world");
        if(!Float.isFinite(bossMaxHp)||bossMaxHp<=0)throw new IllegalArgumentException("hp");
        this.world=world;
        maxHp[ARMOR]=Math.max(30,bossMaxHp*.27f);
        maxHp[WEAPON]=Math.max(30,bossMaxHp*.24f);
        maxHp[CORE]=Math.max(30,bossMaxHp*.18f);
        System.arraycopy(maxHp,0,hp,0,PARTS);
    }
    private static void check(int part){if(part<0||part>=PARTS)throw new IllegalArgumentException("part");}
    public String name(int part){check(part);return PART_NAMES[world][part];}
    public float health(int part){check(part);return hp[part];}
    public float maxHealth(int part){check(part);return maxHp[part];}
    public float fraction(int part){check(part);return hp[part]/maxHp[part];}
    public boolean broken(int part){check(part);return hp[part]<=0;}
    public int nextIntact(int start){
        check(start);
        for(int i=0;i<PARTS;i++){
            int p=(start+i)%PARTS;
            if(!broken(p))return p;
        }
        return -1;
    }
    /** True only on the impact that physically destroys a component. */
    public boolean damage(int part,float rawDamage){
        check(part);
        if(broken(part)||!Float.isFinite(rawDamage)||rawDamage<=0)return false;
        hp[part]=Math.max(0,hp[part]-rawDamage);
        return broken(part);
    }
    /** Armor breaks grant a readable stagger opportunity; no invulnerable phase. */
    public float incomingMultiplier(){return broken(ARMOR)?1.18f:1f;}
    /** Destroyed weapon reduces future charged siege attacks. */
    public float siegeMultiplier(){return broken(WEAPON)?.48f:1f;}
    /** A broken focus limits reinforcement summons and channeling. */
    public boolean canSummon(){return !broken(CORE);}
}
