package com.lokij07.riftguard;

/**
 * Deterministic, biome-aware siege decisions made at wave boundaries.
 * Never reads hidden player inputs. Orders are telegraphed and reward
 * counterplay against commanders, shields and siege engineers.
 */
public final class SquadTacticsV065 {
    private SquadTacticsV065(){}
    public static final int RAIDER=0,SHIELD=1,SAPPER=2,SUPPORT=3,COMMANDER=4;
    public static final int FLANK=0,SHIELD_WALL=1,SIEGE=2,COMMAND_PUSH=3;
    public static String name(int plan){
        switch(plan){
            case FLANK:return "FLANKING RAID";
            case SHIELD_WALL:return "SHIELD WALL";
            case SIEGE:return "SIEGE ENGINEERS";
            case COMMAND_PUSH:return "COMMANDER ASSAULT";
            default:throw new IllegalArgumentException("plan");
        }
    }
    public static int role(int kind,boolean elite,int world){
        if(world<0||world>=10)throw new IllegalArgumentException("world");
        if(elite)return COMMANDER;
        if(kind==2)return SHIELD;
        if(kind==4)return SUPPORT;
        if(kind==5)return SAPPER;
        return RAIDER;
    }
    /**
     * Formation choice is made at the beginning of the wave.
     * The tower count is an observable part of the fort, not hidden information.
     * Each world provides its own weighting without requiring network or RNG.
     */
    public static int plan(int stage,int wave,int builtTowers,int difficulty){
        Campaign.checkStage(stage);
        if(wave<1)throw new IllegalArgumentException("wave");
        int realm=Campaign.world(stage);
        int threat=(Campaign.seed(stage)>>>3)^wave*37^realm*71;
        if(builtTowers>=4&&wave>=3&&(threat&3)!=0)return SIEGE;
        if(DifficultyRules.worldBoss(stage)&&wave==DifficultyRules.waves(stage,difficulty))
            return COMMAND_PUSH;
        int value=Math.floorMod(threat,4);
        if(realm==5||realm==9)value=(value+2)%4;
        if(realm==1||realm==8)value=(value+1)%4;
        return value;
    }
    public static float speed(int role,int order,boolean commanderAlive){
        if(role==COMMANDER)return .90f;
        float multiplier=role==RAIDER?1.12f:role==SHIELD?.78f:role==SAPPER?.87f:.96f;
        if(order==FLANK&&role==RAIDER)multiplier*=1.20f;
        if(order==SHIELD_WALL&&role==SHIELD)multiplier*=.88f;
        if(commanderAlive)multiplier*=1.08f;
        return multiplier;
    }
    public static float incomingDamage(int role,int order,boolean commanderAlive){
        if(role==SHIELD&&order==SHIELD_WALL)return commanderAlive?.72f:.86f;
        if(role==SUPPORT&&commanderAlive)return .9f;
        return 1f;
    }
    public static boolean attacksTowers(int role,int order){
        return role==SAPPER||role==SHIELD||(order==SIEGE&&role==RAIDER);
    }
    public static float towerAttackMultiplier(int role,int order,boolean commanderAlive){
        float damage=role==SAPPER?1.4f:role==SHIELD?1.1f:.65f;
        if(order==SIEGE)damage*=1.15f;
        if(commanderAlive)damage*=1.08f;
        return damage;
    }
}
