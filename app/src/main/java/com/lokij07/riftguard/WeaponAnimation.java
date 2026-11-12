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
    /**
     * Shoulder-driven body translation. Weapons and torso use this identical
     * function so the in-hand socket follows the real Warden lunge, rather
     * than leaving the weapon floating in front of an idle torso.
     */
    public static float torsoShiftX(int weapon,float progress){
        check(weapon);
        if(progress<0)return 0;
        float t=clamp(progress);
        float wind=(float)Math.sin(Math.PI*Math.min(1f,t/.35f));
        float contact=(float)Math.sin(Math.PI*
                Math.max(0f,Math.min(1f,(t-.30f)/.70f)));
        switch(weapon){
            case 0:return -5f*contact; // Crossbow shoulder absorbs recoil
            case 1:return -11f*wind+16f*contact; // Greatsword wind-up, commit
            case 2:return -4f*wind+12f*contact; // Forward duelist step
            case 3:return -5f*wind+7f*contact; // Staff channelling push
            case 4:return -6f*contact; // Repeater resists burst
            case 5:return -9f*wind+17f*contact; // Full-body lance thrust
            default:return 0;
        }
    }
    public static float torsoShiftY(int weapon,float progress){
        check(weapon);
        if(progress<0)return 0;
        float t=clamp(progress);
        float contact=(float)Math.sin(Math.PI*t);
        return weapon==1?7f*contact:weapon==2?-3f*contact:
                weapon==3?-7f*contact:weapon==5?-4f*contact:0;
    }

    public static float recoil(int weapon,float progress){
        check(weapon);if(progress<0)return 0;
        float t=clamp(progress);
        return (float)Math.sin(Math.PI*t)*
                new float[]{13,4,6,11,7,9}[weapon];
    }
}
