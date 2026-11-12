package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/** Structural fortress weapons unlocked by real TOWER ascension purchases.
 * Model tiers have combat effects in RiftGameView, not just light-up decorations.
 */
public final class FortressEvolutionArt {
    private FortressEvolutionArt(){}
    private static final Paint P=new Paint(Paint.ANTI_ALIAS_FLAG);

    private static void fill(int color){P.setShader(null);P.setStyle(Paint.Style.FILL);P.setColor(color);}
    private static void rect(Canvas c,float l,float t,float r,float b,int color){
        fill(color);c.drawRect(l,t,r,b,P);
    }
    private static void oval(Canvas c,float x,float y,float rx,float ry,int color){
        fill(color);c.drawOval(x-rx,y-ry,x+rx,y+ry,P);
    }
    private static void line(Canvas c,float x,float y,float xx,float yy,int color,float thickness){
        fill(color);P.setStrokeWidth(thickness);
        P.setStrokeCap(Paint.Cap.ROUND);P.setStyle(Paint.Style.STROKE);
        c.drawLine(x,y,xx,yy,P);P.setStyle(Paint.Style.FILL);
    }
    private static void poly(Canvas c,int color,float... xy){
        Path p=new Path();p.moveTo(xy[0],xy[1]);
        for(int i=2;i<xy.length;i+=2)p.lineTo(xy[i],xy[i+1]);
        p.close();fill(color);c.drawPath(p,P);
    }

    public static void draw(Canvas c,float x,float y,int rank,int arsenal,float time){
        if(arsenal<0||arsenal>4||rank<1||rank>5)
            throw new IllegalArgumentException("fortress evolution");
        if(arsenal==0)return;
        c.save();c.translate(x,y);
        // Ironbolt Battery (ascension stage one): physical bolted side nests.
        for(int side:new int[]{-1,1}){
            float bx=side*147;
            poly(c,0xff596f79,bx-39,38,bx-35,-32,
                    bx+35,-32,bx+39,38);
            rect(c,bx-40,-37,bx+40,-28,0xffc9c1a7);
            for(int bolt=-1;bolt<=1;bolt++)
                oval(c,bx+bolt*23,-32,3,3,0xfff8e1aa);
            // Both turrets have detailed rails, bowstrings and a real barrel.
            line(c,bx-30,-47,bx+30,-47,0xffe3d2a4,4);
            poly(c,0xffc3a581,bx-32,-51,bx,-71,bx+32,-51,bx,-44);
            rect(c,bx-5,-75,bx+5,-50,0xff455867);
            poly(c,0xffe6e5da,bx-8,-76,bx,-87,bx+8,-76);
        }
        if(arsenal>=2){
            // Ember Mortar: broad, supported double-shot siege mortars.
            for(int side:new int[]{-1,1}){
                float mx=side*171;
                rect(c,mx-32,-9,mx+32,16,0xff56616b);
                poly(c,0xffa78c79,mx-27,-23,mx+27,-23,mx+23,0,mx-23,0);
                rect(c,mx-19,-52,mx+19,-12,0xff343d48);
                oval(c,mx,-53,18,9,0xffbc9c80);
                oval(c,mx,-54,10,5,0xffdc8057);
                line(c,mx-25,6,mx+25,6,0xffd6bc99,5);
            }
        }
        if(arsenal>=3){
            // Storm Conductor: visible lightning coil, rigid connections
            // running into the citadel's masonry—not a detached orb.
            rect(c,-15,-187,30,90,0xff647a8d);
            for(int i=0;i<4;i++) {
                float yy=-175+i*15;
                oval(c,0,yy,22-i*2,6,0xffc2d9db);
            }
            poly(c,0xffb2dfee,-24,-187,0,-227,24,-187,0,-169);
            oval(c,0,-196,13+(float)Math.sin(time*3)*2,13,
                    0xaaa9e7ff);
            for(int side:new int[]{-1,1}){
                line(c,0,-127,side*98,-94,0xff83a1b0,6);
                oval(c,side*98,-94,8,8,0xffe7d6b9);
            }
        }
        if(arsenal>=4){
            // Guardian Prism is visibly large and attached to the roofline.
            poly(c,0xffc5ecf1,-40,-215,0,-276,40,-215,0,-178);
            poly(c,0xff6cb4d3,0,-276,40,-215,0,-178);
            poly(c,0xffe7ffff,-10,-223,0,-250,10,-223);
            for(int i=0;i<6;i++){
                double a=i*Math.PI/3+time*.35;
                float dx=(float)Math.cos(a)*58;
                float dy=(float)Math.sin(a)*34-221;
                oval(c,dx,dy,4,6,0xffa8f7f6);
            }
            line(c,-46,-143,-92,-94,0xffc6ebea,6);
            line(c,46,-143,92,-94,0xffc6ebea,6);
        }
        c.restore();
    }
    /** Real damage/tactical effects unlock in sequence with this appearance. */
    public static float damageBonus(int arsenal){
        if(arsenal<0||arsenal>4)throw new IllegalArgumentException("arsenal");
        return 1f+arsenal*.06f;
    }
    public static boolean emberShell(int arsenal){return arsenal>=2;}
    public static boolean stormChain(int arsenal){return arsenal>=3;}
    public static boolean guardianRepair(int arsenal){return arsenal>=4;}
}
