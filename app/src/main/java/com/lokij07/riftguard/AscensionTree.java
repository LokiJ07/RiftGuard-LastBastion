package com.lokij07.riftguard;

import android.content.SharedPreferences;

/**
 * Bottom-to-top ascension graph. Each paid node is a permanent, gameplay-active
 * upgrade, not an inert picture. Exactly one parent from the row below is
 * required; progress is stored in the existing offline campaign save.
 */
public final class AscensionTree {
    public static final int ATTACK=0, CORE=1, TOWER=2, HERO=3, GOLD=4,
            SKILL=5, SPEED=6;
    public static final int TIERS=10, COLUMNS=5;
    public static final String SAVE_KEY="ascension.purchased.v1";
    public static final String[] CATEGORIES={
            "WEAPON POWER","CORE INTEGRITY","TURRET POWER","WARDEN VITALITY",
            "STARTING GOLD","SPELL RECHARGE","MOVEMENT SPEED"
    };
    public static final int[] COLORS={
            0xffef977d,0xff87c9ec,0xffe4c67b,0xff91ddb1,
            0xffffd080,0xffc9a4fa,0xff83d6da
    };
    private static final String[][] TITLES={
        {"Bastion Origin"},
        {"Warden's Oath","Stone Foundation","Supply Scouts"},
        {"Hunter's Edge","Bolted Ramparts","Sentry Drills","Field Medicine","Arc Focus"},
        {"Tempered Steel","Heavy Walls","Siege Calibration","Swift Stride","Mana Relay"},
        {"Piercing Bolts","Repair Crews","Battery Overdrive","Guardian Resolve","Supply Carts"},
        {"Bloodforged Edge","Iron Bastion","Storm Batteries","Spellmaster","Combat Rations"},
        {"True Strike","Royal Masonry","Arc Conduits","Veteran Spirit","Quartermaster"},
        {"Riftbreaker","Titan Bulwark","Crossfire Mastery","Living Aegis","Runic Reserves"},
        {"Dawn Piercer","Eternal Citadel","Thunderous Guns","Undying Warden","Arc Momentum"},
        {"LAST BASTION"}
    };
    private static final int[] ROW_COST={
            0,75,160,285,410,625,845,1100,1450,2100
    };

    public static final class Node {
        public final int id,tier,column,predecessor,requiredRank,cost,category;
        public final String name;
        public final float gain;
        private Node(int id,int tier,int col,int predecessor,int rank,
                     int cost,int category,String name,float gain){
            this.id=id;this.tier=tier;this.column=col;this.predecessor=predecessor;
            this.requiredRank=rank;this.cost=cost;this.category=category;
            this.name=name;this.gain=gain;
        }
    }
    public static final Node[] NODES=makeNodes();
    private final SharedPreferences prefs;

    public AscensionTree(SharedPreferences prefs){this.prefs=prefs;}

    private static Node[] makeNodes(){
        Node[] nodes=new Node[40];
        int index=0;
        for(int tier=0;tier<TIERS;tier++){
            int count=tier==0||tier==9?1:tier==1?3:5;
            for(int slot=0;slot<count;slot++){
                int col=tier==0||tier==9?2:tier==1?slot+1:slot;
                int parent;
                if(tier==0)parent=-1;
                else if(tier==1)parent=0;
                else if(tier==2)parent=col<=1?1:col==2?2:3;
                else if(tier==9)parent=find(nodes,tier-1,2);
                else parent=find(nodes,tier-1,col);
                int category=tier==0?CORE:(tier*5+col*3)%CATEGORIES.length;
                // Ensure ascending high-rank paths contain combat specialization.
                if(tier==9)category=ATTACK;
                int rank=tier<=1?1:tier<=3?2:tier<=5?3:tier<=7?4:5;
                float gain=category==ATTACK?.075f+tier*.008f:
                        category==TOWER?.08f+tier*.010f:
                        category==CORE?45+tier*25:
                        category==HERO?11+tier*6:
                        category==GOLD?18+tier*9:
                        category==SKILL?.28f+tier*.05f:
                        .033f+tier*.006f;
                int cost=ROW_COST[tier]+(slot%3)*35;
                nodes[index]=new Node(index,tier,col,parent,rank,cost,
                        category,TITLES[tier][slot],gain);
                index++;
            }
        }
        if(index!=nodes.length)throw new IllegalStateException("ascension nodes");
        return nodes;
    }
    private static int find(Node[] nodes,int tier,int column){
        for(Node n:nodes)if(n!=null&&n.tier==tier&&n.column==column)return n.id;
        throw new IllegalStateException("missing prerequisite");
    }
    public static Node byId(int id){
        if(id<0||id>=NODES.length)throw new IllegalArgumentException("node");
        return NODES[id];
    }
    public static Node at(int tier,int column){
        for(Node n:NODES)if(n.tier==tier&&n.column==column)return n;
        return null;
    }
    public static float x(Node node){return 126f+node.column*158f;}
    public static float worldY(Node node){return (TIERS-1-node.tier)*183f+86f;}
    public static float worldHeight(){return (TIERS-1)*183f+180f;}

    private long ownedBits(){return prefs.getLong(SAVE_KEY,0L)|1L;}
    public boolean owned(int id){
        byId(id);
        return (ownedBits()&(1L<<id))!=0;
    }
    public boolean available(int id,int rank){
        Node n=byId(id);
        return !owned(id)&&rank>=n.requiredRank&&
                (n.predecessor<0||owned(n.predecessor));
    }
    /** Atomically deduct supplies and record ownership. True only if paid. */
    public boolean buy(int id,int rank){
        Node n=byId(id);
        if(!available(id,rank))return false;
        int cash=Math.max(0,prefs.getInt("supplies",0));
        if(cash<n.cost)return false;
        return prefs.edit().putLong(SAVE_KEY,ownedBits()|(1L<<id))
                .putInt("supplies",cash-n.cost).commit();
    }
    public int purchasedCount(){
        return Long.bitCount(ownedBits());
    }
    /** These totals are used DIRECTLY by the combat simulation. */
    public float bonus(int category){
        float total=0;
        for(Node n:NODES)if(n.category==category&&owned(n.id))total+=n.gain;
        return total;
    }
    public float attackMultiplier(){return 1f+bonus(ATTACK);}
    public float towerMultiplier(){return 1f+bonus(TOWER);}
    public int coreHp(){return Math.round(bonus(CORE));}
    public int heroHp(){return Math.round(bonus(HERO));}
    public int startingGold(){return Math.round(bonus(GOLD));}
    public float spellCooldownReduction(){return bonus(SKILL);}
    public float moveMultiplier(){return 1f+bonus(SPEED);}
    public static String benefit(Node n){
        switch(n.category){
            case ATTACK:return "+"+Math.round(n.gain*100)+"% WARDEN DAMAGE";
            case TOWER:return "+"+Math.round(n.gain*100)+"% TURRET DAMAGE";
            case CORE:return "+"+Math.round(n.gain)+" CORE HP";
            case HERO:return "+"+Math.round(n.gain)+" HERO HP";
            case GOLD:return "+"+Math.round(n.gain)+" START GOLD";
            case SKILL:return "-"+String.format(java.util.Locale.US,"%.2f",n.gain)+"s SPELL COOLDOWN";
            case SPEED:return "+"+Math.round(n.gain*100)+"% MOVE SPEED";
            default:throw new IllegalArgumentException("category");
        }
    }
}
