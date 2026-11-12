package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.Paint;

/** Real world-space impact cues tied to combat states (never HUD overlays). */
public final class CombatImpactPolish {
    private CombatImpactPolish(){}
    private static final Paint P=new Paint(Paint.ANTI_ALIAS_FLAG);
    private static void circle(Canvas c,float x,float y,float radius,int col){
        P.setShader(null);P.setColor(col);P.setStyle(Paint.Style.FILL);
        c.drawCircle(x,y,radius,P);
    }
    private static void stroke(Canvas c,float x,float y,float radius,int col,float width){
        P.setShader(null);P.setColor(col);P.setStyle(Paint.Style.STROKE);
        P.setStrokeWidth(width);c.drawCircle(x,y,radius,P);
        P.setStyle(Paint.Style.FILL);
    }
    /** Boss warning arcs actively shorten as charge approaches impact. */
    public static void boss(Canvas c,float x,float y,float charge,int phase,
                            int realm,float time){
        if(charge<=0)return;
        int color=realm==3||realm==9?0xffffad70:
                realm==4||realm==7?0xffa5eaf5:0xffd7a1ff;
        float fraction=Math.max(0,Math.min(1,charge/1.2f));
        float radius=130-38*(1-fraction);
        stroke(c,x,y-35,radius,(color&0xffffff)|0x99000000,4+phase);
        P.setStyle(Paint.Style.STROKE);P.setStrokeCap(Paint.Cap.ROUND);
        P.setStrokeWidth(5);
        P.setColor(color);
        for(int i=0;i<8;i++){
            float a=(float)(i*Math.PI/4+time*.8);
            float radiusInner=radius-16;
            c.drawLine(x+(float)Math.cos(a)*radiusInner,
                    y-35+(float)Math.sin(a)*radiusInner*.55f,
                    x+(float)Math.cos(a)*radius,
                    y-35+(float)Math.sin(a)*radius*.55f,P);
        }
        P.setStyle(Paint.Style.FILL);
    }
    /** Elemental status only appears when the real enemy is slowed/burning. */
    public static void statuses(Canvas c,float x,float y,float time,
                                float slow,float burn){
        if(slow>0){
            for(int k=0;k<5;k++){
                float a=(float)(k*Math.PI*2/5+time*.8);
                float px=x+(float)Math.cos(a)*30;
                float py=y-18+(float)Math.sin(a)*14;
                circle(c,px,py,3.8f,0xaa9cefff);
                stroke(c,px,py,6,0x448df4ff,2);
            }
        }
        if(burn>0){
            for(int k=0;k<4;k++){
                float flicker=(float)Math.sin(time*7+k*2)*6;
                float px=x-20+k*13,py=y-10-(k%2)*15+flicker;
                circle(c,px,py,5,0x99ff7b43);
                circle(c,px,py-6,3,0xbbffcc74);
            }
        }
    }
    /** Dash shockline follows real Warden motion rather than screen origin. */
    public static void dodge(Canvas c,float x,float y,float dirX,float dirY,
                             float fraction){
        float strength=(float)Math.sin(Math.PI*Math.max(0,Math.min(1,fraction)));
        if(strength<=0)return;
        P.setShader(null);P.setStyle(Paint.Style.STROKE);
        P.setStrokeCap(Paint.Cap.ROUND);P.setStrokeWidth(3);
        P.setColor(0x99a0f2fa);
        for(int i=-2;i<=2;i++){
            float shift=i*9;
            float sx=x-dirX*(46+strength*13)-dirY*shift;
            float sy=y-22-dirY*(46+strength*13)+dirX*shift;
            c.drawLine(sx,sy,x-dirX*18-dirY*shift*.7f,
                    y-22-dirY*18+dirX*shift*.7f,P);
        }
        P.setStyle(Paint.Style.FILL);
    }
}
