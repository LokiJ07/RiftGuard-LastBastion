package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/** Five genuinely different native weapon silhouettes per each of six weapons.
 * No invisible icons: used in Armory, tree preview and IK held combat weapon. */
public final class WeaponEvolutionArt {
    private WeaponEvolutionArt(){}
    private static final Paint P=new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final int[] GLOW={0xff92e5f3,0xffe7dbb1,0xffffa75b,
            0xffab9fff,0xfff4ca82,0xffb8f5ff};
    private static void fill(int col){P.setColor(col);P.setShader(null);P.setStyle(Paint.Style.FILL);}
    private static void line(Canvas c,float x,float y,float xx,float yy,int col,float w){
        fill(col);P.setStrokeWidth(w);P.setStyle(Paint.Style.STROKE);
        P.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x,y,xx,yy,P);
        P.setStyle(Paint.Style.FILL);
    }
    private static void circle(Canvas c,float x,float y,float r,int col){
        fill(col);c.drawCircle(x,y,r,P);
    }
    private static void poly(Canvas c,int col,float... xy){
        Path path=new Path();path.moveTo(xy[0],xy[1]);
        for(int i=2;i<xy.length;i+=2)path.lineTo(xy[i],xy[i+1]);
        path.close();fill(col);c.drawPath(path,P);
    }
    public static void draw(Canvas c,int weapon,int form,float x,float y,
                            float scale,float time){
        if(weapon<0||weapon>=6||form<0||form>4)throw new IllegalArgumentException("weapon form");
        c.save();c.translate(x,y);c.scale(scale,scale);
        WorldArt.drawHeldWeapon(c,weapon,0,0,1,time);
        if(form>0)drawEvolved(c,weapon,form,time);
        c.restore();
    }
    /** Draw true enlarged limbs, heads, side blades and stocks instead of
     * decorative text or floating colored circles. */
    private static void drawEvolved(Canvas c,int weapon,int form,float time){
        int glow=GLOW[weapon],dark=0xff334b59,bright=0xfff5efdf;
        if(weapon==0||weapon==4){
            // Crossbow: 2 reinforced limbs -> 3 heavy recurves -> 4 runic
            // compound bow -> 5 legendary split-bow magazine assembly.
            float span=33+form*9;
            int metal=form>=3?glow:0xffc1d2d1;
            line(c,-span,-25,-16,-32-form*3,metal,3+form*.65f);
            line(c,span,-25,16,-32-form*3,metal,3+form*.65f);
            line(c,-span,-25,0,-8,bright,1.2f);
            line(c,span,-25,0,-8,bright,1.2f);
            for(int side=-1;side<=1;side+=2){
                poly(c,metal,side*(span+7),-31,side*span,-40-form*3,
                        side*(span-7),-28);
                circle(c,side*(span-4),-29,3,0xfff6dba7);
            }
            if(form>=2){
                poly(c,dark,-15,4,-12,27,12,27,15,4);
                for(int i=0;i<3;i++)line(c,-11,11+i*5,11,11+i*5,glow,1.6f);
            }
            if(form>=3){
                poly(c,glow,-12,-27,0,-59-form*4,12,-27,0,-34);
                poly(c,dark,-14,-7,-23,7,-4,13,9,-7);
            }
            if(form==4){
                line(c,-span-6,-20,-span-9,-57,glow,4);
                line(c,span+6,-20,span+9,-57,glow,4);
                poly(c,bright,-8,-61,0,-87,8,-61);
            }
        }else if(weapon==1||weapon==2){
            // Sword: unique widened steel blade, langets, split guard, finally
            // a long runeblade with jagged twin edge and visible tip.
            float half=8+form*2.7f;
            float tip=-65-form*11;
            int steel=form>=3?glow:weapon==2?0xffffbd77:0xffe5f1f6;
            poly(c,steel,-half,-17,-half*.74f,tip+14,0,tip,
                    half*.8f,tip+15,half,-17);
            poly(c,0xff718e9b,0,tip,half*.8f,tip+15,half,-17,0,-20);
            line(c,0,tip+13,0,-25,bright,1.8f+form*.25f);
            poly(c,form>=3?glow:0xffb49a69,-31-form*3,-12,-11,-15,
                    0,-9,11,-15,31+form*3,-12,25+form*3,-2,
                    0,-6,-25-form*3,-2);
            circle(c,0,-10,6+form*.7f,form>=3?0xffc5f8ee:0xffe7d5a0);
            line(c,-6,10,-6,27,0xffd2ae81,3);
            if(form>=2){
                for(int i=0;i<3;i++){
                    float yy=-36-i*14;
                    poly(c,glow,-6,yy,0,yy-7,6,yy,0,yy+5);
                }
            }
            if(form>=3){
                poly(c,glow,-half,tip+23,-half-10,tip+5,-half*.3f,tip+10);
                poly(c,glow,half,tip+23,half+10,tip+5,half*.3f,tip+10);
            }
            if(form==4){
                poly(c,bright,-8,tip+13,0,tip-16,8,tip+13,0,tip+4);
                circle(c,0,32,9,glow);
            }
        }else{
            // Staff/lance: gem evolves from socket -> twin facets -> rune
            // branching -> fully articulated celestial focus.
            float top=-68-form*8;
            int gem=weapon==3?0xffb5a9ff:0xffa8f4fc;
            line(c,-3,26,4,top+15,form>=3?glow:0xff9a9188,8+form*.65f);
            poly(c,gem,-11,top+24,4,top-13,19,top+24,4,top+10);
            poly(c,0xffefffff,4,top-13,19,top+24,4,top+10);
            for(int side=-1;side<=1;side+=2){
                poly(c,form>=3?glow:0xff9bb1ba,
                        side*10,top+18,side*(17+form*4),top-6,
                        side*(10+form*2),top-24,side*3,top+12);
            }
            if(form>=2){
                for(int i=0;i<4;i++)
                    line(c,-11,-20+i*12,9,-23+i*12,gem,2.8f);
            }
            if(form>=3){
                poly(c,glow,-15,top+38,-30,top+17,-7,top+21);
                poly(c,glow,15,top+38,30,top+17,7,top+21);
            }
            if(form==4){
                poly(c,0xfff1ffff,-10,top-12,4,top-42,17,top-11,4,top+1);
                for(int i=0;i<4;i++){
                    double a=time*.45+i*Math.PI*.5;
                    float px=(float)Math.cos(a)*28,py=top+(float)Math.sin(a)*13;
                    circle(c,px,py,4,glow);
                }
            }
        }
        // Rank insignias are physically engraved on each grip.
        for(int i=0;i<form;i++)circle(c,-6+i*4.3f,18,1.6f,glow);
    }
}
