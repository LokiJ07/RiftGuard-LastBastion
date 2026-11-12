package com.lokij07.riftguard;

import android.content.SharedPreferences;

/**
 * Six INDEPENDENT permanent bottom-up weapon trees. Four tiers above an
 * automatically owned Origin, 11 nodes per weapon / 66 total. Real combat
 * multipliers derive solely from purchased graph nodes.
 */
public final class WeaponMastery {
    public static final int WEAPON_COUNT=6;
    public static final int NODES_PER_WEAPON=11;
    public static final int TIERS=5;
    public static final int POWER=0,CADENCE=1,SPECIAL=2;
    public static final String[] PATH_NAMES={"POWER","TECHNIQUE","SPECIAL"};
    private static final int[] COST={0,75,105,145,230,290,340,460,540,600,1100};
    private static final String[][] TITLES={
        {"ROOT BOLT","Sharpshooter","Balanced Draw","Tether Bolt","Piercing Tip",
            "Rapid Strings","Hunter's Mark","Heartseeker","Phantom Load",
            "Stagger Trap","PHANTOM VOLLEY"},
        {"IRON OATH","Crusher","Braced Grip","Guardbreaker","Armor Split",
            "Swift Recovery","Heavy Cleave","Titan Edge","Momentum Strike",
            "Earth Rend","TITAN SPLITTER"},
        {"EMBER OATH","Flame Edge","Duelist","Inferno Path","Searing Steel",
            "Quickstep","Ignite","Phoenix Fang","Combo Spark",
            "Inferno Wave","PHOENIX DRIVE"},
        {"ARC ORIGIN","Stormcall","Mystic Core","Control","Shock Pulse",
            "Fast Channel","Static Chain","Tempest Rod","Conduit",
            "Arc Prison","TEMPEST CROWN"},
        {"REPEATER CORE","Rapid Fire","Calibrated Feed","Explosive Rounds",
            "Accelerator","Piercing Chamber","Suppression","Siege Engine",
            "Chain Feed","Blast Cartridge","WAR DRUM BARRAGE"},
        {"FROST ORIGIN","Piercer","Glacier","Shatter","Ice Needle",
            "Swift Thrust","Chillbind","Crystal Point","Deep Freeze",
            "Brittle Armor","ABSOLUTE ZERO"}
    };
    private final SharedPreferences prefs;
    public WeaponMastery(SharedPreferences prefs){this.prefs=prefs;}
    public static final class Node {
        public final int weapon,id,tier,lane,parent,cost;
        public final String name;
        public final float damageBonus,speedBonus,rangeBonus;
        private Node(int weapon,int id,int tier,int lane,int parent,int cost,
                     String name,float dmg,float speed,float range){
            this.weapon=weapon;this.id=id;this.tier=tier;this.lane=lane;
            this.parent=parent;this.cost=cost;this.name=name;
            damageBonus=dmg;speedBonus=speed;rangeBonus=range;
        }
    }
    public static Node node(int weapon,int id){
        checkWeapon(weapon);
        if(id<0||id>=NODES_PER_WEAPON)throw new IllegalArgumentException("node");
        int tier=id==0?0:id<=3?1:id<=6?2:id<=9?3:4;
        int lane=id==0||id==10?1:(id-1)%3;
        int parent=id==0?-1:id<=3?0:id==10?7:id-3;
        float damage=id==0?0:lane==POWER?.07f+tier*.045f:
                id==10?.24f:0;
        float speed=id==0?0:lane==CADENCE?.042f+tier*.030f:0;
        float range=id==0?0:lane==SPECIAL?18f+tier*12f:0;
        if(id==10){damage=.24f;speed=.07f;range=26f;}
        return new Node(weapon,id,tier,lane,parent,id==0?0:COST[id]+weapon*15,
                TITLES[weapon][id],damage,speed,range);
    }
    public static int at(int tier,int lane){
        if(tier<0||tier>=TIERS||lane<0||lane>2)return -1;
        if(tier==0||tier==4)return lane==1?tier==0?0:10:-1;
        return 1+(tier-1)*3+lane;
    }
    private static void checkWeapon(int weapon){
        if(weapon<0||weapon>=WEAPON_COUNT)throw new IllegalArgumentException("weapon");
    }
    private static String key(int weapon){
        checkWeapon(weapon);
        return "weapon.mastery."+weapon+".v1";
    }
    private long bits(int weapon){
        return prefs.getLong(key(weapon),0L)|1L;
    }
    public boolean owned(int weapon,int id){
        node(weapon,id);
        return (bits(weapon)&(1L<<id))!=0;
    }
    /** Pinnacle requires ALL three preceding branches, not one cheap shortcut. */
    public boolean available(int weapon,int id,int fortressRank){
        Node n=node(weapon,id);
        if(id==0||owned(weapon,id)||!Progression.canEquip(fortressRank,weapon))
            return false;
        if(n.tier>=3&&fortressRank<Math.min(5,n.tier+1))return false;
        if(n.tier==4)return owned(weapon,7)&&owned(weapon,8)&&owned(weapon,9);
        return owned(weapon,n.parent);
    }
    public boolean buy(int weapon,int id,int fortressRank){
        Node n=node(weapon,id);
        if(!available(weapon,id,fortressRank))return false;
        int supplies=Math.max(0,prefs.getInt("supplies",0));
        if(supplies<n.cost)return false;
        return prefs.edit().putInt("supplies",supplies-n.cost)
                .putLong(key(weapon),bits(weapon)|(1L<<id)).commit();
    }
    public int count(int weapon){
        checkWeapon(weapon);
        return Long.bitCount(bits(weapon));
    }
    public float damageMultiplier(int weapon){
        float bonus=0;
        for(int id=1;id<NODES_PER_WEAPON;id++)
            if(owned(weapon,id))bonus+=node(weapon,id).damageBonus;
        return 1+bonus;
    }
    public float cooldownMultiplier(int weapon){
        float reduction=0;
        for(int id=1;id<NODES_PER_WEAPON;id++)
            if(owned(weapon,id))reduction+=node(weapon,id).speedBonus;
        return Math.max(.55f,1-reduction);
    }
    public float bonusRange(int weapon){
        float bonus=0;
        for(int id=1;id<NODES_PER_WEAPON;id++)
            if(owned(weapon,id))bonus+=node(weapon,id).rangeBonus;
        return bonus;
    }
    public boolean mastered(int weapon){return owned(weapon,10);}
    /** Dedicated signature passives are tied to the individual six weapons. */
    public String masteryName(int weapon){return node(weapon,10).name;}
    public static float worldY(int id){
        return (4-node(0,id).tier)*146+66;
    }
    public static float x(int id){return 176+node(0,id).lane*182;}
    public static String benefit(Node n){
        if(n.id==0)return "Every weapon begins here";
        if(n.id==10)return "MASTERY +24% damage, -7% cooldown, +26 reach + special";
        if(n.lane==POWER)return "+"+Math.round(n.damageBonus*100)+"% DAMAGE";
        if(n.lane==CADENCE)return "-"+Math.round(n.speedBonus*100)+"% COOLDOWN";
        return "+"+Math.round(n.rangeBonus)+" ATTACK REACH";
    }
}
