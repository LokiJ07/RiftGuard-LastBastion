package com.lokij07.riftguard;

import android.content.SharedPreferences;

/**
 * Save-backed, screen-space controls in RiftGuard's 1280x720 virtual canvas.
 * Rendering, tapping, joystick movement and the HUD editor all read the SAME
 * mutable positions. No hardcoded legacy hitboxes remain after rearranging.
 */
public final class HudLayoutV065 {
    public static final int MOVE=0,DODGE=1,BUILD=2,SKILL=3,ATTACK=4,COUNT=5;
    public static final String[] NAMES={"MOVE","DODGE","BUILD","SKILL","ATTACK"};
    private static final String PREFIX="hud.v065.control.";
    private static final float[] DEFAULT_X={153,729,849,974,1130};
    private static final float[] DEFAULT_Y={568,590,589,593,560};
    private static final float[] RADIUS={94,42,50,49,70};
    private final float[] x=DEFAULT_X.clone(),y=DEFAULT_Y.clone();
    private final SharedPreferences prefs;

    public HudLayoutV065(SharedPreferences prefs){
        this.prefs=prefs;
        for(int i=0;i<COUNT;i++){
            x[i]=clamp(prefs.getFloat(PREFIX+i+".x",DEFAULT_X[i]),95,1182);
            y[i]=clamp(prefs.getFloat(PREFIX+i+".y",DEFAULT_Y[i]),220,625);
        }
    }
    private static void check(int control){
        if(control<0||control>=COUNT)throw new IllegalArgumentException("control");
    }
    private static float clamp(float v,float min,float max){
        if(!Float.isFinite(v))return min;
        return Math.max(min,Math.min(max,v));
    }
    public float x(int control){check(control);return x[control];}
    public float y(int control){check(control);return y[control];}
    public float radius(int control){check(control);return RADIUS[control];}
    /** A touch on a moved button is consumed only at its new location. */
    public boolean hit(int control,float px,float py,float extra){
        check(control);
        return Math.hypot(px-x[control],py-y[control])<RADIUS[control]+extra;
    }
    /** Return nearest control for editing rather than following legacy tap order. */
    public int nearest(float px,float py){
        int result=-1;
        float closest=Float.POSITIVE_INFINITY;
        for(int i=0;i<COUNT;i++){
            float d=(float)Math.hypot(px-x[i],py-y[i]);
            if(d<=RADIUS[i]+24&&d<closest){closest=d;result=i;}
        }
        return result;
    }
    public void move(int control,float px,float py){
        check(control);
        x[control]=clamp(px,95,1182);
        y[control]=clamp(py,220,625);
    }
    public void save(){
        SharedPreferences.Editor e=prefs.edit();
        for(int i=0;i<COUNT;i++){
            e.putFloat(PREFIX+i+".x",x[i]).putFloat(PREFIX+i+".y",y[i]);
        }
        e.apply();
    }
    public void reset(){
        for(int i=0;i<COUNT;i++){x[i]=DEFAULT_X[i];y[i]=DEFAULT_Y[i];}
        save();
    }
}
