package com.lokij07.riftguard;

import android.content.SharedPreferences;

/**
 * Three named blueprints. Never grants free structures: deployment calls the
 * same BattleRules.towerCost used by the regular build button.
 */
public final class DefenseLoadouts {
    public static final int COUNT=3;
    private static final String[] DEFAULT_NAMES={"BALANCED","ANTI-SWARM","BOSS BREAKER"};
    private static final String ROOT="fort.v065.loadout.";
    private final SharedPreferences prefs;
    public DefenseLoadouts(SharedPreferences prefs){this.prefs=prefs;}
    private static void check(int id){
        if(id<0||id>=COUNT)throw new IllegalArgumentException("loadout");
    }
    public String name(int id){
        check(id);
        String n=prefs.getString(ROOT+id+".name",DEFAULT_NAMES[id]);
        return n==null||n.trim().isEmpty()?DEFAULT_NAMES[id]:n;
    }
    public void rename(int id,String requested){
        check(id);
        String n=requested==null?"":requested.replaceAll("[^a-zA-Z0-9 _-]","").trim();
        if(n.length()>20)n=n.substring(0,20);
        prefs.edit().putString(ROOT+id+".name",n.isEmpty()?DEFAULT_NAMES[id]:n).commit();
    }
    public boolean saved(int id){check(id);return prefs.getBoolean(ROOT+id+".saved",false);}
    public void save(int id,int[] tiers,int[] types,int liveCount){
        check(id);
        if(liveCount<1||liveCount>TowerPadLayout.MAX)throw new IllegalArgumentException("pads");
        long packed=0;
        for(int pad=0;pad<TowerPadLayout.MAX;pad++){
            int tier=pad<liveCount?Math.max(0,Math.min(5,tiers[pad])):0;
            int type=pad<liveCount?TowerDurability.clampType(types[pad]):0;
            packed|=((long)(tier|(type<<3)))<<(pad*5);
        }
        prefs.edit().putLong(ROOT+id+".plan",packed)
                .putBoolean(ROOT+id+".saved",true).commit();
    }
    public int tier(int id,int pad){
        check(id);if(pad<0||pad>=TowerPadLayout.MAX)throw new IllegalArgumentException("pad");
        if(!saved(id))return 0;
        return (int)((prefs.getLong(ROOT+id+".plan",0L)>>(pad*5))&7L);
    }
    public int type(int id,int pad){
        check(id);if(pad<0||pad>=TowerPadLayout.MAX)throw new IllegalArgumentException("pad");
        if(!saved(id))return pad%4;
        return (int)((prefs.getLong(ROOT+id+".plan",0L)>>(pad*5+3))&3L);
    }
    /** Mutates battle-only arrays; returns gold spent, never spends more than budget. */
    public int deploy(int id,int[] towers,int[] types,int count,int budget){
        check(id);
        if(!saved(id))return 0;
        int spent=0;
        for(int pad=0;pad<count;pad++){
            int desired=Math.min(5,tier(id,pad));
            if(desired<=0)continue;
            // Existing free start defenses are retained and not re-charged.
            int current=towers[pad];
            while(current<desired){
                int cost=BattleRules.towerCost(current);
                if(spent+cost>budget)break;
                spent+=cost;current++;
            }
            if(current>0){towers[pad]=current;types[pad]=type(id,pad);}
        }
        return spent;
    }
}
