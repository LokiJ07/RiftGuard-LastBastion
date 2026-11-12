package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Final physical detailing for the existing 30 earnable weapon silhouettes:
 * forged riveted tangs, larger elite blades, compound bow cams, staff prongs.
 * Applied INSIDE the player's weapon orientation transform, so the new parts
 * stay attached during real attack animations and respect original hit timing.
 */
public final class WeaponSilhouettePolish {
    private WeaponSilhouettePolish(){}
    private static final Paint P=new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final int[] RUNE={0xffb7e9f4,0xffffda91,0xffffa16c,
            0xffd2b7ff,0xfff4cd82,0xffb5f3ff};
    private static void shape(Canvas c,int color,float...coords){
        Path path=new Path();path.moveTo(coords[0],coords[1]);
        for(int i=2;i<coords.length;i+=2)path.lineTo(coords[i],coords[i+1]);
        path.close();P.setColor(color);P.setStyle(Paint.Style.FILL);
        c.drawPath(path,P);
    }
    private static void circle(Canvas c,float x,float y,float r,int color){
        P.setColor(color);P.setStyle(Paint.Style.FILL);c.drawCircle(x,y,r,P);
    }
    private static void stroke(Canvas c,float x,float y,float xx,float yy,int color,float width){
        P.setColor(color);P.setStyle(Paint.Style.STROKE);P.setStrokeWidth(width);
        P.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x,y,xx,yy,P);
        P.setStyle(Paint.Style.FILL);
    }
    public static void draw(Canvas c,int weapon,int form,float time){
        if(weapon<0||weapon>=6||form<0||form>4)
            throw new IllegalArgumentException("weapon silhouette");
        if(form==0)return;
        int rune=RUNE[weapon];
        if(weapon==1||weapon==2){
            // Distinctive hilt, forging bands, reinforced fuller and widened
            // blade silhouette for the heroic sword / saber progression.
            float span=18+form*6;
            stroke(c,-span,-8,span,-8,0xffdbd6c6,2.5f+form*.5f);
            shape(c,0xff334958,-6,-10,-4,-19,4,-19,6,-10);
            shape(c,rune,-3,-23,0,-35-form*3,3,-23,0,-17);
            if(form>=2){
                for(int side:new int[]{-1,1}){
                    shape(c,rune,side*(span-4),-12,side*(span+8),-18-form*3,
                            side*(span+3),-3);
                    stroke(c,side*8,-21,side*(8+form),-45,0xffb9d5d7,2);
                }
            }
            if(form>=3){
                shape(c,0xffdee6e8,-4,-43,0,-68,4,-43,0,-35);
                for(int k=0;k<3;k++)circle(c,0,-24-k*11,1.6f,rune);
            }
            if(form==4){
                for(int side:new int[]{-1,1}){
                    shape(c,rune,side*10,-40,side*25,-51,side*11,-58);
                    shape(c,0xffeff8f6,side*16,-10,
                            side*40,-32,side*30,-7);
                }
            }
        }else if(weapon==0||weapon==4){
            // Wooden bow becomes braced compound bow with honest mechanical
            // limb cams and magazine on the repeater at higher tiers.
            float wing=27+form*5;
            for(int side:new int[]{-1,1}){
                circle(c,side*wing,-27,3+form*1.2f,
                        form>=3?rune:0xffd5bf91);
                stroke(c,side*wing,-27,side*(wing+13),-34-form*2,
                        form>=3?rune:0xff9da4aa,2+form*.35f);
                stroke(c,side*(wing+13),-34-form*2,
                        side*(wing-12),-44,0xffe5e2cd,1.5f);
            }
            if(form>=2){
                shape(c,0xff657783,-14,4,-16,-15,-8,-21,8,-21,16,-15,
                        14,4,0,9);
                circle(c,0,-20,4+form,rune);
            }
            if(form>=3){
                stroke(c,-wing,-31,0,-58,0xffeff6ec,1.7f);
                stroke(c,wing,-31,0,-58,0xffeff6ec,1.7f);
                shape(c,rune,-4,-54,0,-70-form*3,4,-54);
            }
            if(form==4){
                for(int side:new int[]{-1,1}){
                    shape(c,rune,side*(wing+4),-39,
                            side*(wing+21),-54,side*(wing+16),-30);
                }
                for(int i=0;i<4;i++){
                    stroke(c,-7+i*5,-2,-7+i*5,17,0xffe4d6a8,2);
                }
            }
        }else{
            // Scepter/ice lance: real forged forked prongs, captive floating
            // focus and crystal lattices. The ice lance keeps the thrust tip.
            float point=-58-form*5;
            for(int side:new int[]{-1,1}){
                shape(c,form>=3?rune:0xffaeb9c4,side*6,-40,
                        side*(16+form*3),point+4,
                        side*(13+form*3),point-14,
                        side*5,-48);
                stroke(c,side*8,-39,side*(14+form*2),point-5,
                        0xfff5fcff,1.8f);
            }
            if(form>=2){
                shape(c,rune,-8,point+8,0,point-17,8,point+8,0,point+1);
                for(int i=0;i<form;i++)
                    stroke(c,-9,-16+i*8,9,-20+i*8,0xffcce8e4,2);
            }
            if(form>=3){
                circle(c,0,point+12,5,rune);
                for(int side:new int[]{-1,1})
                    shape(c,0xffe2f6fd,side*22,point+9,
                            side*36,point-5,side*29,point+23);
            }
            if(form==4){
                for(int i=0;i<3;i++){
                    float ox=(i-1)*22;
                    shape(c,rune,ox-6,point-13,ox,point-37,ox+6,point-13);
                }
                circle(c,0,point-18,4+(float)Math.sin(time*3),0xfff4ffff);
            }
        }
    }
}
