package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Adds real actor-state visual cues on top of already articulated sprite rigs.
 * Uses existing attack progress, hit reaction, Warden dodge time, and boss phase
 * (not a free-running decorative sprite). Zero bitmaps, no extra resources.
 */
public final class CombatAnimationLayers {
    private CombatAnimationLayers(){}
    private static final Paint P=new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final int[] TRAIT={
        0xfff5ae8a,0xffe5cb87,0xffb6cfe2,0xffd6b9f7,
        0xff92e0bf,0xffffa174,0xffffca81
    };
    private static final int[] REALM={
        0xff8cefb2,0xffaee8af,0xff95edc9,0xffff9849,0xffc8efff,
        0xffe9bd8d,0xffb7a1ff,0xff8cdef9,0xffa38bd6,0xffffbb99
    };
    private static void circle(Canvas c,float x,float y,float r,int color){
        P.setStyle(Paint.Style.FILL);P.setShader(null);P.setColor(color);
        c.drawCircle(x,y,r,P);
    }
    private static void ellipse(Canvas c,float x,float y,float rx,float ry,int color){
        P.setStyle(Paint.Style.FILL);P.setShader(null);P.setColor(color);
        c.drawOval(x-rx,y-ry,x+rx,y+ry,P);
    }
    private static void stroke(Canvas c,float x,float y,float xx,float yy,
                               int color,float width){
        P.setShader(null);P.setColor(color);P.setStyle(Paint.Style.STROKE);
        P.setStrokeCap(Paint.Cap.ROUND);P.setStrokeWidth(width);
        c.drawLine(x,y,xx,yy,P);P.setStyle(Paint.Style.FILL);
    }
    private static void poly(Canvas c,int color,float... v){
        P.setShader(null);P.setColor(color);P.setStyle(Paint.Style.FILL);
        Path p=new Path();p.moveTo(v[0],v[1]);
        for(int i=2;i<v.length;i+=2)p.lineTo(v[i],v[i+1]);
        p.close();c.drawPath(p,P);
    }
    /** Base creature-specific contact frames and genuine HP-linked boss stages. */
    public static void enemy(Canvas c,int kind,float x,float y,float time,
                             float attack,float hit,int bossPhase,boolean boss){
        if(kind<0||kind>6)return;
        if(!Float.isFinite(time))return;
        int color=TRAIT[kind];
        c.save();c.translate(x,y);
        float t=attack<0?-1:Math.min(1,Math.max(0,attack));
        if(t>=0){
            float contact=(float)Math.sin(t*Math.PI);
            if(kind==0||kind==2||kind==5){
                // Real weapon contact tracks the same .42-second melee window
                // that drives the enemy attack, not an unrelated attack circle.
                float reach=kind==5?67:kind==2?45:33;
                stroke(c,-reach*.60f,-70-contact*12,reach*.75f,-48+contact*21,
                        color,2.5f+contact*6);
                ellipse(c,reach*.73f,-46+contact*17,11*contact,5*contact,
                        (100<<24)|(color&0xffffff));
            }else if(kind==1||kind==6){
                // Charger lunges in the direction of the actual hero target.
                for(int n=0;n<3;n++){
                    float dx=22+n*16;
                    stroke(c,-dx,-22-n*7,-dx-29*contact,-25-n*7,
                            0x99e9ce9e,2.5f);
                }
            }else if(kind==3){
                for(int n=0;n<4;n++){
                    float angle=(float)(n*Math.PI/2+time*3);
                    float dx=(float)Math.cos(angle)*29,dy=(float)Math.sin(angle)*17;
                    circle(c,dx,-51+dy,4+3*contact,0x889acdf3);
                }
            }else if(kind==4){
                float height=13+20*contact;
                stroke(c,0,-88,0,-88-height,0xffa8ecce,4);
                circle(c,0,-88-height,7*contact,0x889bffdb);
            }
        }
        if(hit>0){
            // Color-coded shell fragments, falling away from actual wounded
            // enemy instead of a uniform full-screen red flash.
            float fraction=Math.min(1,hit/.21f);
            for(int n=0;n<5;n++){
                float a=n*1.2566f+kind*.4f;
                float px=(float)Math.cos(a)*(27+(1-fraction)*19);
                float py=-47+(float)Math.sin(a)*(16+(1-fraction)*29);
                poly(c,(Math.max(0,Math.min(255,(int)(188*fraction)))<<24)|
                        (color&0xffffff),px-3,py-7,px+4,py,px-3,py+5);
            }
        }
        if(boss){
            int phase=Math.max(1,Math.min(3,bossPhase));
            int glow=REALM[Math.floorMod(kind+phase,REALM.length)];
            float pulse=(float)Math.sin(time*(3+phase*1.5f))*3;
            for(int i=0;i<phase;i++){
                float r=52+i*17+pulse;
                P.setColor((85<<24)|(glow&0xffffff));
                P.setStyle(Paint.Style.STROKE);P.setStrokeWidth(2+i);
                c.drawOval(-r,-76-r*.47f,r,-76+r*.47f,P);
                P.setStyle(Paint.Style.FILL);
                for(int j=0;j<3+phase;j++){
                    float a=(float)(j*Math.PI*2/(3+phase)+time*(.4+i*.2));
                    float px=(float)Math.cos(a)*r;
                    float py=-76+(float)Math.sin(a)*r*.47f;
                    circle(c,px,py,2+i*.5f,glow);
                }
            }
            // Physical phase crest hovers over the boss and scales with real HP.
            for(int i=0;i<phase;i++){
                float cx=(i-(phase-1)*.5f)*16;
                poly(c,glow,cx-7,-144,cx,-159-pulse,cx+7,-144,cx,-139);
            }
        }
        c.restore();
    }
    /** Warden upper-body directional attack wake and actual cast charge. */
    public static void warden(Canvas c,float x,float y,float facing,
                              float attack,float cast,float dodgeProgress){
        c.save();c.translate(x,y-54);c.rotate((float)Math.toDegrees(facing));
        if(attack>=0){
            float t=Math.min(1,Math.max(0,attack));
            float reach=37+(float)Math.sin(t*Math.PI)*44;
            stroke(c,18,-25,reach,18,0x55f4e9c9,3.4f);
            circle(c,reach,18,4,0x99ffecb2);
        }
        if(cast>=0){
            float t=Math.min(1,Math.max(0,cast));
            for(int i=0;i<5;i++){
                float a=i*1.2566f+3.5f*t;
                float r=26+13*t;
                circle(c,(float)Math.cos(a)*r,(float)Math.sin(a)*r,
                        2.5f+t*2,0x997df2e9);
            }
        }
        if(dodgeProgress>=0){
            float t=Math.max(0,Math.min(1,dodgeProgress));
            for(int i=0;i<3;i++)
                ellipse(c,-24-i*13+t*45,-2+i*11,9,5,0x4475e6f0);
        }
        c.restore();
    }
}
