package com.lokij07.riftguard;

/**
 * Original RiftGuard actor identities, informed by the user's three-knight /
 * seven-action animation reference. No third-party artwork is redistributed.
 *
 * The silhouettes are selected from equipped real combat families: they
 * cannot accidentally change their appearance in the middle of an attack.
 */
public final class WardenVisualStyles {
    private WardenVisualStyles() {}
    public static final int RANGER=0, VANGUARD=1, ARCANIST=2;
    public static final int IDLE=0, WALK=1, RUN=2, JUMP=3,
                            ATTACK=4, HURT=5, DIE=6, CAST=7;
    public static final int FRAMES_PER_ACTION=10;
    public static final int ARMOR_STYLES=3;

    public static int forWeapon(int weaponId){
        if(weaponId<0||weaponId>=6)throw new IllegalArgumentException("weapon");
        if(weaponId==0||weaponId==4)return RANGER;
        if(weaponId==1||weaponId==2)return VANGUARD;
        return ARCANIST;
    }
    public static String name(int style){
        switch(style){
            case RANGER:return "Ranger";
            case VANGUARD:return "Vanguard";
            case ARCANIST:return "Arcanist";
            default:throw new IllegalArgumentException("style");
        }
    }
    public static int cape(int style){
        switch(style){
            case RANGER:return 0xff28574b; // deep evergreen layered hunter's cape
            case VANGUARD:return 0xff922c3e; // royal crimson cloth
            case ARCANIST:return 0xff56377c; // enchanted violet mantle
            default:throw new IllegalArgumentException("style");
        }
    }
    public static int capeShadow(int style){
        switch(style){
            case RANGER:return 0xff18392f;
            case VANGUARD:return 0xff5d1d2d;
            case ARCANIST:return 0xff302253;
            default:throw new IllegalArgumentException("style");
        }
    }
    public static int capeHighlight(int style){
        switch(style){
            case RANGER:return 0xff478d70;
            case VANGUARD:return 0xffc14c5d;
            case ARCANIST:return 0xff9471c2;
            default:throw new IllegalArgumentException("style");
        }
    }
    /** A ten-frame clip clock, mirroring the seven-action reference pack.
     * One-shot states do not wrap; movement/idle states loop.
     */
    public static int frame(int action,float seconds,float duration){
        if(action<0||action>CAST)throw new IllegalArgumentException("action");
        if(!Float.isFinite(seconds)||!Float.isFinite(duration)
                ||duration<=0)return 0;
        boolean oneShot=action==ATTACK||action==HURT||action==DIE||
                action==JUMP||action==CAST;
        float progress=seconds/duration;
        if(oneShot)return Math.max(0,Math.min(9,(int)Math.floor(
                Math.max(0,progress)*FRAMES_PER_ACTION)));
        float wrapped=progress-(float)Math.floor(progress);
        return Math.max(0,Math.min(9,(int)(wrapped*FRAMES_PER_ACTION)));
    }
    public static int action(WardenAnimationMachine.State state,boolean moving){
        if(state==null)return IDLE;
        switch(state){
            case RUN:return moving?RUN:WALK;
            case ATTACK:return ATTACK;
            case CAST:return CAST;
            case DODGE:return JUMP;
            case HURT:return HURT;
            case DEATH:return DIE;
            default:return IDLE;
        }
    }
}
