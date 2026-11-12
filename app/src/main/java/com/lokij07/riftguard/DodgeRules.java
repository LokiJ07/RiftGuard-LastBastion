package com.lokij07.riftguard;

/**
 * Pure dash rules reused by input, movement and hit-avoidance.
 * One short burst (210 ms) with a readable 2.25 s cooldown.
 * The normal walls, stage bounds and built turrets still apply.
 */
public final class DodgeRules {
    private DodgeRules(){}
    public static final float DURATION=.21f;
    public static final float COOLDOWN=2.25f;
    public static final float SPEED=755f;
    public static boolean canStart(float time,float cooldown){
        return time<=0&&cooldown<=0;
    }
    public static float normalized(float x,float y,boolean getX,float fallbackAngle){
        float n=(float)Math.hypot(x,y);
        if(n>=.08f)return (getX?x:y)/n;
        return (float)(getX?Math.cos(fallbackAngle):Math.sin(fallbackAngle));
    }
    public static boolean invulnerable(float dodgeTime){return dodgeTime>0;}
}
