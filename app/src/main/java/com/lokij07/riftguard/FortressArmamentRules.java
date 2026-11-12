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
    /** Legacy coordinate helper retained for existing battery diagnostics. */
    public static float muzzleX(float keepX,int shotCount){
        return muzzleX(keepX,shotCount,1);
    }
    /** Spawn from the actual visible weapon, not arbitrary old side towers.
     * Ironbolt and Ember have two barrels; Storm/Guardian have a central
     * mounted conductor/prism and must fire from its crystal focus. */
    public static float muzzleX(float keepX,int shotCount,int arsenal){
        require(arsenal);
        if(arsenal>=3)return keepX;
        float offset=arsenal==2?169f:147f;
        return keepX+((shotCount&1)==0?-offset:offset);
    }
    public static float muzzleY(float keepY,int arsenal){
        require(arsenal);
        switch(arsenal){
            case 1:return keepY-88f;    // Ironbolt rail between bow limbs
            case 2:return keepY-105f;   // Ember mortar muzzle
            case 3:return keepY-218f;   // Storm central induction cage
            case 4:return keepY-249f;   // Guardian prism optical focus
            default:return keepY-87f;
        }
    }
    private static void require(int arsenal){
        if(arsenal<0||arsenal>4)throw new IllegalArgumentException("arsenal");
    }
}
