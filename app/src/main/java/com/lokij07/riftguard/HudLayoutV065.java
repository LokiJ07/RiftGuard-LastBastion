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
        reload();
    }
    public void reload(){
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
    /**
     * Resolve an actual gameplay touch against the closest rendered control.
     * This is shared by the scene with all moveable buttons; old fixed screen
     * coordinates do not participate in hit testing.
     */
    public int hitControl(float px,float py){
        int hit=-1;
        float best=Float.POSITIVE_INFINITY;
        for(int i=0;i<COUNT;i++){
            float d=(float)Math.hypot(px-x[i],py-y[i]);
            float extra=i==MOVE?12f:i==ATTACK?12f:i==BUILD?15f:13f;
            if(d<=RADIUS[i]+extra&&d<best){hit=i;best=d;}
        }
        return hit;
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
    /**
     * Avoid unreachable overlapped buttons: a placement is accepted only
     * when the drawn circular controls retain independent tap areas.
     */
    public void move(int control,float px,float py){
        check(control);
        float newX=clamp(px,95,1182),newY=clamp(py,290,625);
        for(int other=0;other<COUNT;other++){
            if(other==control)continue;
            float minimum=RADIUS[control]+RADIUS[other]+12f;
            if(Math.hypot(newX-x[other],newY-y[other])<minimum)return;
        }
        x[control]=newX;
        y[control]=newY;
    }
    public void save(){
        SharedPreferences.Editor e=prefs.edit();
        for(int i=0;i<COUNT;i++){
            e.putFloat(PREFIX+i+".x",x[i]).putFloat(PREFIX+i+".y",y[i]);
        }
        e.apply();
    }
    /** Preview factory positions without changing the player's saved layout. */
    public void previewDefaults(){
        for(int i=0;i<COUNT;i++){x[i]=DEFAULT_X[i];y[i]=DEFAULT_Y[i];}
    }
    public void reset(){
        previewDefaults();
        save();
    }
}
