package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Compact cooldown/charge progress at the button's REAL edited location.
 * This is a world-independent HUD layer: no extra modal or touch hitbox.
 * A small ring is shown only while the action is unavailable/charging.
 */
public final class HudActionFeedbackV067 {
    private HudActionFeedbackV067(){}
    private static final Paint P=new Paint(Paint.ANTI_ALIAS_FLAG);

    /** 0 = no recovery yet, 1 = ready. */
    public static float readyFraction(float remaining,float duration){
        if(!Float.isFinite(remaining)||!Float.isFinite(duration)||
                duration<=0f)return 1f;
        return Math.max(0f,Math.min(1f,1f-Math.max(0f,remaining)/duration));
    }
    /** Only a real held attack can animate a charge indicator. */
    public static float chargeFraction(boolean holding,float seconds){
        if(!holding||!Float.isFinite(seconds)||seconds<=0f)return 0f;
        return Math.max(0f,Math.min(1f,
                seconds/WeaponComboV065.CHARGE_SECONDS));
    }
    public static void cooldown(Canvas c,float x,float y,float radius,
                                float remaining,float duration,int tint){
        if(c==null||remaining<=0||!Float.isFinite(remaining))return;
        float progress=readyFraction(remaining,duration);
        P.setShader(null);P.setStyle(Paint.Style.STROKE);
        P.setStrokeCap(Paint.Cap.ROUND);
        P.setStrokeWidth(5f);
        P.setColor((tint&0x00ffffff)|0x66000000);
        float r=radius+6f;
        c.drawCircle(x,y,r,P);
        P.setColor((tint&0x00ffffff)|0xf0000000);
        c.drawArc(x-r,y-r,x+r,y+r,-90f,360f*progress,false,P);
        P.setStyle(Paint.Style.FILL);
    }
    public static void charge(Canvas c,float x,float y,float radius,
                              boolean holding,float seconds){
        float fraction=chargeFraction(holding,seconds);
        if(c==null||fraction<=0)return;
        P.setShader(null);P.setStyle(Paint.Style.STROKE);
        P.setStrokeCap(Paint.Cap.ROUND);P.setStrokeWidth(6f);
        P.setColor(fraction>=1f?0xfffce2a7:0xffd5c6ff);
        float r=radius+7f;
        c.drawArc(x-r,y-r,x+r,y+r,-90f,360f*fraction,false,P);
        P.setStyle(Paint.Style.FILL);
    }
}
