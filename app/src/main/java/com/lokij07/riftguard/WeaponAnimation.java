package com.lokij07.riftguard;

/**
 * Six individual strike animations. A weapon controls the duration, torso
 * anticipation, wrist travel and recovery instead of reusing one attack pose.
 * Drawing and collision both read the same normalized attack clock.
 */
public final class WeaponAnimation {
    private WeaponAnimation(){}
    private static final float[] DURATION={.20f,.34f,.27f,.32f,.13f,.37f};
    private static float clamp(float x){return Math.max(0,Math.min(1,x));}
    public static float duration(int weapon){return DURATION[check(weapon)];}
    private static int check(int weapon){
        if(weapon<0||weapon>=6)throw new IllegalArgumentException("weapon");
        return weapon;
    }
    public static float progress(int weapon,float remaining){
        if(remaining<=0)return -1f;
        return clamp(1-remaining/duration(weapon));
    }
    public static float anticipation(int weapon,float progress){
        check(weapon);
        if(progress<0)return 0;
        float t=clamp(progress);
        switch(weapon){
            case 0:return (float)Math.sin(t*Math.PI)*-3.5f;
            case 1:return t<.29f?-10f*(t/.29f):
                    t<.63f?-10f+27f*((t-.29f)/.34f):
                            17f*(1-(t-.63f)/.37f);
            case 2:return (float)Math.sin(t*2*Math.PI)*8f;
            case 3:return (float)Math.sin(t*Math.PI)*-6f;
            case 4:return (float)Math.sin(t*Math.PI*3f)*-3f;
            case 5:return (float)Math.sin(t*Math.PI)*-9f;
            default:return 0;
        }
    }
    public static float wristForward(int weapon,float progress){
        check(weapon);if(progress<0)return 0;
        float t=clamp(progress);
        if(weapon==1)return 22f*(float)Math.sin(Math.PI*t);
        if(weapon==2)return 19f*(float)Math.sin(2*Math.PI*t);
        if(weapon==3)return 15f*(float)Math.sin(Math.PI*t);
        if(weapon==5)return 27f*(float)Math.sin(Math.PI*t);
        return -10f*(float)Math.sin(Math.PI*t);
    }
    public static float recoil(int weapon,float progress){
        check(weapon);if(progress<0)return 0;
        float t=clamp(progress);
        return (float)Math.sin(Math.PI*t)*
                new float[]{13,4,6,11,7,9}[weapon];
    }
}
