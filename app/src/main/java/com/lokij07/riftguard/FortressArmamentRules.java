package com.lokij07.riftguard;

/**
 * Independently firing physical fortress batteries.
 * A purchased TOWER ascension arsenal provides real shots from the attached
 * fortress weapons, in addition to the existing ground/castle turret pads.
 */
public final class FortressArmamentRules {
    private FortressArmamentRules(){}
    public static float cooldown(int arsenal){
        require(arsenal);
        if(arsenal==0)return Float.POSITIVE_INFINITY;
        return Math.max(.68f,2.35f-arsenal*.29f);
    }
    public static float range(int arsenal){
        require(arsenal);
        return arsenal==0?0:360+arsenal*48;
    }
    public static float damage(int arsenal){
        require(arsenal);
        return arsenal==0?0:30+arsenal*14f;
    }
    public static float speed(int arsenal){
        require(arsenal);
        return arsenal==0?0:480+arsenal*55f;
    }
    public static int family(int arsenal){
        require(arsenal);
        if(arsenal<=1)return 0; // ironbolt / ballista
        if(arsenal==2)return 1; // ember mortar
        return 3;               // storm conductor / prism bolts
    }
    public static float muzzleX(float keepX,int shotCount){
        return keepX+((shotCount&1)==0?-147f:147f);
    }
    public static float muzzleY(float keepY,int arsenal){
        require(arsenal);
        return keepY-(arsenal>=3?166f:87f);
    }
    private static void require(int arsenal){
        if(arsenal<0||arsenal>4)throw new IllegalArgumentException("arsenal");
    }
}
