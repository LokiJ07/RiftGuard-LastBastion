package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Low-allocation, actual-collision-matched warnings for all ten boss powers.
 * Unlike a flavor VFX, beam/circle/annulus use RealmBossV071 geometry.
 */
public final class RealmBossThreatV071 {
    private RealmBossThreatV071(){}
    private static final Paint P=new Paint(Paint.ANTI_ALIAS_FLAG);
    private static void color(int tint,int alpha){
        P.setShader(null);
        P.setColor((alpha<<24)|(tint&0x00ffffff));
    }
    private static void ring(Canvas c,float x,float y,float r,int tint,float progress){
        P.setStyle(Paint.Style.FILL);color(tint,21);
        c.drawCircle(x,y,r,P);
        P.setStyle(Paint.Style.STROKE);
        P.setStrokeWidth(4);color(tint,175);c.drawCircle(x,y,r,P);
        P.setStrokeWidth(8);color(tint,255);
        c.drawArc(x-r,y-r,x+r,y+r,-90,360*progress,false,P);
    }
    public static void draw(Canvas c,int stage,int phase,
                            float bossX,float bossY,float lockX,float lockY,
                            float coreX,float coreY,float seconds,float duration,
                            boolean reducedMotion){
        if(seconds<=0||!Float.isFinite(seconds))return;
        int world=RealmBossV071.realm(stage),tint=RealmBossV071.color(stage);
        float progress=BossThreatV066.progress(seconds,duration);
        float r=RealmBossV071.radius(stage,phase);
        if(world==4||world==8){
            // Capsule stroke with round cap matches segment-distance hit test.
            P.setStyle(Paint.Style.STROKE);
            P.setStrokeCap(Paint.Cap.ROUND);
            color(tint,34);P.setStrokeWidth(r*2);
            c.drawLine(bossX,bossY,lockX,lockY,P);
            color(tint,205);P.setStrokeWidth(3.5f);
            float dx=lockX-bossX,dy=lockY-bossY;
            float len=(float)Math.hypot(dx,dy);
            if(len>.1f){
                float nx=-dy/len*r,ny=dx/len*r;
                c.drawLine(bossX+nx,bossY+ny,lockX+nx,lockY+ny,P);
                c.drawLine(bossX-nx,bossY-ny,lockX-nx,lockY-ny,P);
            }
            ring(c,lockX,lockY,10f+progress*8f,tint,progress);
        }else if(world==6){
            float inner=RealmBossV071.innerRadius(stage,phase);
            P.setStyle(Paint.Style.STROKE);
            color(tint,27);P.setStrokeWidth(r-inner);
            float mid=(r+inner)*.5f;
            c.drawCircle(bossX,bossY,mid,P);
            color(tint,195);P.setStrokeWidth(4f);
            c.drawCircle(bossX,bossY,r,P);
            c.drawCircle(bossX,bossY,inner,P);
            color(tint,255);P.setStrokeWidth(7f);
            c.drawArc(bossX-r,bossY-r,bossX+r,bossY+r,-90,360*progress,false,P);
        }else{
            float x=(world==0||world==9)?bossX:lockX;
            float y=(world==0||world==9)?bossY:lockY;
            ring(c,x,y,r,tint,progress);
        }
        // All ten bosses also exert core siege pressure; if the core is
        // in attack range, it ALWAYS has a warning and can be damaged.
        if(BossThreatV066.threatensCore(bossX,bossY,coreX,coreY)){
            P.setStyle(Paint.Style.STROKE);color(tint,170);
            P.setStrokeWidth(3.5f);
            float cr=61f+progress*7f;
            c.drawCircle(coreX,coreY,cr,P);
            c.drawLine(coreX-26,coreY,coreX+26,coreY,P);
            c.drawLine(coreX,coreY-26,coreX,coreY+26,P);
        }
        if(!reducedMotion){
            P.setStyle(Paint.Style.STROKE);color(tint,110);P.setStrokeWidth(2);
            c.drawCircle(bossX,bossY-45,26+progress*8,P);
        }
        P.setStrokeCap(Paint.Cap.BUTT);P.setStyle(Paint.Style.FILL);
    }
}
