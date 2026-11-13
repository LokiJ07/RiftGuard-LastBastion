package com.lokij07.riftguard;

/**
 * Per-weapon three-beat attack sequences and deliberate charged strikes.
 * Stateless physics: all damage timings still come from the real Warden
 * animation contact event / projectile collision in RiftGameView.
 */
public final class WeaponComboV065 {
    public static final float CHARGE_SECONDS=.64f;
    private static final float[][] RHYTHM={
        {1f,1.12f,1.34f}, // Crossbow: precision 3rd bolt
        {1f,1.28f,1.68f}, // Greatsword: weighty finisher
        {1f,1.18f,1.40f}, // Ember Sabre: quick flourish
        {1f,1.16f,1.48f}, // Arc Staff: arc crescendo
        {1f,1.06f,1.28f}, // Siege Repeater: suppressing third burst
        {1f,1.24f,1.56f}  // Frost Lance: finishing thrust
    };
    public static final class Hit {
        public final int weapon,step;
        public final boolean charged;
        public final float multiplier;
        Hit(int weapon,int step,boolean charged,float multiplier){
            this.weapon=weapon;this.step=step;this.charged=charged;
            this.multiplier=multiplier;
        }
    }
    private int weapon=-1,step=0;
    private float lastHitTime=-100f;
    public void reset(){weapon=-1;step=0;lastHitTime=-100;}
    public int comboStep(){return step;}
    public Hit attack(int currentWeapon,float seconds,boolean charged){
        if(currentWeapon<0||currentWeapon>=6)throw new IllegalArgumentException("weapon");
        if(!Float.isFinite(seconds))throw new IllegalArgumentException("seconds");
        if(weapon!=currentWeapon||seconds-lastHitTime>1.16f||seconds<lastHitTime)
            step=0;
        step=step%3+1;
        weapon=currentWeapon;lastHitTime=seconds;
        float multiplier=RHYTHM[currentWeapon][step-1]*(charged?1.65f:1f);
        return new Hit(currentWeapon,step,charged,multiplier);
    }
}
