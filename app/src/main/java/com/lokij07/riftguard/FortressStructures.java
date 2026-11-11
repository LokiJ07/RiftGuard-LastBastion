package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/** Independent, real-time construction layers for rank silhouettes and turrets. */
public final class FortressStructures {
    private FortressStructures() {}
    private static final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private static void rect(Canvas c,float a,float b,float d,float e,int col){
        p.setStyle(Paint.Style.FILL);p.setColor(col);c.drawRect(a,b,d,e,p);
    }
    private static void circle(Canvas c,float x,float y,float r,int col){
        p.setStyle(Paint.Style.FILL);p.setColor(col);c.drawCircle(x,y,r,p);
    }
    private static void poly(Canvas c,int col,float...v){
        Path path=new Path();path.moveTo(v[0],v[1]);
        for(int i=2;i<v.length;i+=2)path.lineTo(v[i],v[i+1]);
        path.close();p.setStyle(Paint.Style.FILL);p.setColor(col);c.drawPath(path,p);
    }
    private static void line(Canvas c,float a,float b,float d,float e,int col,float w){
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(w);
        p.setStrokeCap(Paint.Cap.ROUND);p.setColor(col);
        c.drawLine(a,b,d,e,p);p.setStyle(Paint.Style.FILL);
    }
    /** Distinct components accumulate on top of the stone keep's painted source. */
    public static void rankStructures(Canvas c,float x,float y,int rank,float time){
        if(rank<3)return;
        if(rank>=4){
            // Physical gatehouse bastions, side walls and royal flags.
            for(int side:new int[]{-1,1}){
                float xx=x+side*193;
                poly(c,0xff334550,xx-27,y+77,xx-36,y-102,xx+37,y-102,xx+27,y+77);
                rect(c,xx-26,y-100,xx+26,y+69,0xff8b979a);
                rect(c,xx-20,y-93,xx-8,y+64,0xffbac0bb);
                for(int i=-1;i<=1;i++)
                    rect(c,xx+i*19-8,y-113,xx+i*19+8,y-93,0xffd0d6ca);
                poly(c,0xff2b4c68,xx-39,y-113,xx,y-159,xx+39,y-113);
                rect(c,xx-8,y-70,xx+8,y-38,0xff213a4b);
                circle(c,xx,y-54,5,0xffffc879);
                line(c,xx,y-161,xx,y-198,0xffd7cda9,4);
                float wind=(float)Math.sin(time*3.4+side)*11;
                poly(c,0xffdbb275,xx+2,y-196,xx+31+wind,y-186,xx+2,y-175);
            }
            // A functional-looking reinforced causeway, no floating wall pieces.
            poly(c,0xff586e75,x-155,y+107,x+155,y+107,x+117,y+128,x-117,y+128);
            for(int i=-4;i<=4;i++){
                float xx=x+i*29;
                rect(c,xx-10,y+107,xx+2,y+124,0xffc0b7a4);
            }
        }
        if(rank>=5){
            // Arc Citadel has a unique soaring silhouette, not a recolor.
            for(int side:new int[]{-1,1}){
                float xx=x+side*162;
                poly(c,0xff668aa4,xx-30,y-154,xx,y-240,xx+30,y-154);
                poly(c,0xffa7e6ee,xx,y-240,xx+30,y-154,xx,y-170);
                circle(c,xx,y-165,12+(float)Math.sin(time*4+side)*3,0x777de7fc);
                line(c,xx,y-135,xx,y-80,0x997ce5fb,4);
            }
            float pulse=(float)Math.sin(time*2.3f);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);
            p.setColor(0x998cefff);
            c.drawOval(new RectF(x-192-pulse*7,y-225-pulse*4,
                    x+192+pulse*7,y+121+pulse*4),p);
            p.setStyle(Paint.Style.FILL);
            for(int i=0;i<12;i++){
                double a=i*Math.PI/6+time*.55;
                circle(c,x+(float)Math.cos(a)*178,y-57+(float)Math.sin(a)*102,
                        3.3f,0xffb8f5ff);
            }
        }
    }
    /** Platforms are drawn ON the existing keep, then guns draw ABOVE mounts. */
    public static void turretMounts(Canvas c,float centerX,float centerY,int rank,
                                   float[] turretX,float[] turretY,float time){
        for(int i=0;i<4;i++){
            float x=turretX[i],y=turretY[i];
            float innerX=centerX+(x<centerX?-92:92);
            float innerY=centerY+(y<centerY?-83:74);
            int base=rank>=3?0xff7f9699:0xff78634a;
            int trim=rank>=3?0xffc2d3c8:0xffb39569;
            // Bridge buttress physically joins each gun position to the keep.
            poly(c,0xff2c4049,innerX-11,innerY+4,x-26,y+20,
                    x+27,y+20,innerX+11,innerY+4);
            poly(c,base,innerX-14,innerY-6,x-25,y-9,
                    x+25,y-9,innerX+14,innerY-6);
            line(c,innerX,innerY-1,x,y-6,trim,5);
            rect(c,x-33,y+12,x+33,y+27,0xff41545c);
            rect(c,x-36,y-9,x+36,y+14,base);
            poly(c,trim,x-43,y-10,x-34,y-24,x+34,y-24,x+43,y-10);
            for(int battlement=-1;battlement<=1;battlement++)
                rect(c,x+battlement*25-8,y-27,x+battlement*25+8,y-15,trim);
            for(int rivet=-1;rivet<=1;rivet++)
                circle(c,x+rivet*21,y+17,3,0xffffce82);
        }
    }
}
