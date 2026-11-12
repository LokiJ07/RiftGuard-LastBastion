package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/**
 * Animated real-gameplay projectiles for six hero weapons and four turret families.
 * Weapon and turret IDs select silhouettes, particle motion, and physical trajectories.
 */
public final class CombatVfx {
    private CombatVfx(){}
    private static final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private static void line(Canvas c,float a,float b,float d,float e,int col,float size){
        p.setStyle(Paint.Style.STROKE);p.setColor(col);p.setStrokeWidth(size);
        p.setStrokeCap(Paint.Cap.ROUND);c.drawLine(a,b,d,e,p);p.setStyle(Paint.Style.FILL);
    }
    private static void circle(Canvas c,float x,float y,float r,int col){
        p.setStyle(Paint.Style.FILL);p.setColor(col);c.drawCircle(x,y,r,p);
    }
    private static void poly(Canvas c,int col,float...v){
        Path path=new Path();path.moveTo(v[0],v[1]);
        for(int i=2;i<v.length;i+=2)path.lineTo(v[i],v[i+1]);
        path.close();p.setStyle(Paint.Style.FILL);p.setColor(col);c.drawPath(path,p);
    }
    public static void projectile(Canvas c,float x,float y,float tx,float ty,
                                  int weaponId,int turretSlot,float seconds){
        boolean turret=turretSlot>=0;
        int slot=turret?Math.min(3,turretSlot):-1;
        int w=Math.max(0,Math.min(5,weaponId));
        float angle=(float)Math.atan2(ty-y,tx-x);
        c.save();
        c.translate(x,y);c.rotate((float)Math.toDegrees(angle));
        if(slot==0 || (!turret&&(w==0||w==4))){
            // Fletched crossbow arrow, two-tier gold-repeater shafts.
            int metal=slot==0?0xffffd58e:w==4?0xffffcc75:0xffcbebf6;
            line(c,-31,0,18,0,0xff3b5054,5);
            line(c,-29,-1,20,-1,metal,2);
            poly(c,0xfff0efdc,26,0,12,-7,12,7);
            poly(c,0xffd48a62,-25,0,-33,-8,-36,-5,-28,2);
            poly(c,0xffb0bccc,-25,0,-33,8,-36,5,-28,-2);
            if(w==4 && !turret)line(c,-26,-5,10,-5,0xfff9c986,2);
        }else if(slot==1){ // Heavy shell with coiling smoke and ember wake.
            circle(c,-13,0,17,0x557f625b);
            circle(c,-2,0,13,0x77ff8a49);
            circle(c,8,0,12,0xff3b404b);
            circle(c,11,-3,7,0xffd7a970);
            for(int i=0;i<4;i++){
                float drift=(float)Math.sin(seconds*8+i*1.7f)*4;
                circle(c,-15-i*12,drift,7-i,0x556c7a7f);
            }
        }else if(slot==2 || (!turret&&w==5)){ // Frozen fractal shards.
            circle(c,-16,0,15,0x559eebfa);
            poly(c,0xffd9ffff,30,0,2,-10,-21,0,2,10);
            poly(c,0xff78c7ef,30,0,2,10,0,-1);
            for(int i=-1;i<=1;i++)
                poly(c,0xffabeafa,-8-i*14,-2,-16-i*14,-9,-20-i*14,3);
            if(slot==2){
                // Heavy Frost Battery bolt has rotating cryo vanes unlike the lance.
                float oscillation=(float)Math.sin(seconds*9)*5;
                line(c,-20,-16,6,-20+oscillation,0xff74dff4,4);
                line(c,-20,16,6,20-oscillation,0xffb0ffff,4);
                circle(c,-12,0,5,0xffffffff);
            }
        }else if(slot==3 || (!turret&&w==3)){ // Stepped lightning.
            float jitter=(float)Math.sin(seconds*45)*6;
            Path bolt=new Path();bolt.moveTo(-39,-3);
            bolt.lineTo(-24,7+jitter);bolt.lineTo(-12,-8);
            bolt.lineTo(0,8-jitter);bolt.lineTo(17,-5);bolt.lineTo(33,0);
            p.setColor(0x88a792ff);p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(13);p.setStrokeJoin(Paint.Join.ROUND);
            c.drawPath(bolt,p);
            p.setColor(0xffedfaff);p.setStrokeWidth(4);
            c.drawPath(bolt,p);p.setStyle(Paint.Style.FILL);
            circle(c,33,0,8,0x7795dffe);
            circle(c,33,0,4,0xffffffff);
            if(slot==3) {
                // Arc turret releases a wide three-pronged discharge.
                float flicker=(float)Math.sin(seconds*43)*3;
                line(c,-27,-17,4,-24+flicker,0xffbb9bff,3);
                line(c,-27,17,4,24-flicker,0xffddcbff,3);
                circle(c,-12,0,17,0x447e72e4);
            }
        }else{ // Melee shockwave projectile fallback.
            line(c,-19,0,20,0,0xffc6e9ee,5);
            circle(c,20,0,8,0xffb8f3fc);
        }
        c.restore();
    }
    /** Directional impact sparks, not a detached expanding circle animation. */
    public static void hit(Canvas c,float x,float y,float fraction,int color,float radius){
        float t=Math.max(0,Math.min(1,fraction));
        float fade=1-t;
        int alpha=Math.max(0,Math.min(255,(int)(215*fade)));
        int tint=(color&0x00ffffff)|(alpha<<24);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setColor(tint);
        p.setStrokeWidth(2.5f+3*fade);
        for(int i=0;i<9;i++){
            // Each strike produces separated fragments with uneven travel paths.
            float a=(float)(i*Math.PI*2/9+.22*Math.sin(i*2.9));
            float spread=radius*(.25f+.68f*t)*(i%3==0?1f:.70f);
            float sx=x+(float)Math.cos(a)*spread*.33f;
            float sy=y+(float)Math.sin(a)*spread*.20f;
            float ex=x+(float)Math.cos(a)*spread;
            float ey=y+(float)Math.sin(a)*spread*.55f;
            c.drawLine(sx,sy,ex,ey,p);
        }
        p.setStyle(Paint.Style.FILL);
        // Tiny fragment shards travel with the hit rather than orbiting the enemy.
        for(int i=0;i<4;i++){
            float a=(float)(i*1.64+.3);
            float r=radius*(.18f+.65f*t);
            float px=x+(float)Math.cos(a)*r;
            float py=y+(float)Math.sin(a)*r*.58f;
            float s=2.1f+2.3f*fade;
            poly(c,tint,px,py-s,px+s*.55f,py,px,py+s,px-s*.55f,py);
        }
    }

}
