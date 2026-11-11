package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;

/**
 * Selectable ten-location journeys inside each of ten regions.
 * Independent terrain, trails, landmarks, unlocks, decorations and route markers.
 */
public final class RegionJourney {
    private RegionJourney() {}
    private static final float[] X={143,220,317,424,556,694,769,653,503,331};
    private static final float[] Y={452,368,492,396,444,357,253,193,260,177};
    private static final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    public static int hitLevel(float x,float y) {
        for(int i=0;i<X.length;i++) {
            float dx=x-X[i],dy=y-Y[i];
            if(dx*dx+dy*dy<39*39)return i;
        }
        return -1;
    }
    private static void circle(Canvas c,float x,float y,float radius,int color) {
        p.setColor(color);p.setShader(null);p.setStyle(Paint.Style.FILL);
        c.drawCircle(x,y,radius,p);
    }
    private static void line(Canvas c,float x,float y,float x2,float y2,
                             float size,int color) {
        p.setShader(null);p.setColor(color);p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeWidth(size);
        c.drawLine(x,y,x2,y2,p);p.setStyle(Paint.Style.FILL);
    }
    private static void poly(Canvas c,int color,float...xy) {
        Path path=new Path();path.moveTo(xy[0],xy[1]);
        for(int i=2;i<xy.length;i+=2)path.lineTo(xy[i],xy[i+1]);
        path.close();
        p.setColor(color);p.setShader(null);p.setStyle(Paint.Style.FILL);
        c.drawPath(path,p);
    }
    private static void label(Canvas c,String value,float x,float y,float size,int color) {
        p.setShader(null);p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(android.graphics.Typeface.create(
                "sans-serif-condensed",android.graphics.Typeface.BOLD));
        p.setTextSize(size);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);
        p.setColor(0xdd132d37);c.drawText(value,x,y,p);
        p.setStyle(Paint.Style.FILL);p.setColor(color);c.drawText(value,x,y,p);
    }
    public static void draw(Canvas c,int world,int selectedLevel,int unlocked,float time) {
        final float l=73,t=120,r=870,b=569;
        c.save();c.clipRect(l,t,r,b);
        WorldArt.drawScene(c,world,world%3,l,t,r-l,b-t,time);
        p.setShader(new LinearGradient(l,t,r,b,0x223a7188,0x99344243,Shader.TileMode.CLAMP));
        c.drawRect(l,t,r,b,p);p.setShader(null);
        // Additional outposts and terrain ornaments, unique to this biome.
        int seed=Campaign.seed(Campaign.stage(world*10+1,0));
        for(int i=0;i<26;i++) {
            float x=l+39+((seed>>>4)+(i*113))%715;
            float y=t+23+((seed>>>8)+(i*73))%396;
            if(x<l+30||x>r-30||y<t+15||y>b-18)continue;
            float radius=5+i%6;
            if(i%4==0) {
                circle(c,x,y,12+i%3*2,0x6643b4aa);
                poly(c,0xff607577,x-11,y+7,x,y-15,x+15,y+7);
            } else if(i%4==1) {
                line(c,x,y+11,x,y-8,3,0xff8b816b);
                circle(c,x,y-11,radius,world==4?0xffd9f5f8:0xff82c494);
            } else if(i%4==2) {
                poly(c,0xff7f9892,x-14,y+7,x-7,y-8,x+9,y-16,x+16,y+7);
            } else {
                circle(c,x,y,5+(float)Math.sin(time*2+i)*2,0x6696e0ea);
            }
        }
        // Meandering route; active progress glows and a courier light travels it.
        for(int i=0;i<9;i++) {
            float x0=X[i],y0=Y[i],x1=X[i+1],y1=Y[i+1];
            boolean opened=Campaign.accessible(Campaign.stage(world*10+i+1,0),unlocked);
            line(c,x0,y0,x1,y1,16,0xcc2e483f);
            line(c,x0,y0,x1,y1,10,opened?0xffd2bc85:0xff707a70);
            line(c,x0,y0,x1,y1,3,opened?0xffffe3a3:0xffadb3a0);
            if(opened) {
                float progress=(time*.22f+i*.09f)%1;
                float px=x0+(x1-x0)*progress,py=y0+(y1-y0)*progress;
                circle(c,px,py,5,0x99ffde92);
                circle(c,px,py,2,0xffffffff);
            }
        }
        for(int i=0;i<10;i++){
            int level=world*10+i+1;
            int stage=Campaign.stage(level,0);
            boolean accessible=Campaign.accessible(stage,unlocked);
            boolean complete=unlocked>stage+2;
            boolean active=selectedLevel==level;
            float x=X[i],y=Y[i],ring=active?32+(float)Math.sin(time*3)*3:28;
            circle(c,x,y+10,33,0x990e2531);
            circle(c,x,y,ring,active?0x99ffd78d:accessible?0xaa68c4ab:0xaa53636b);
            circle(c,x,y,25,accessible?0xff455e65:0xff2a3944);
            // Every location is a tiny independent structure, not a printed digit.
            if(i==9){ // local final fortress and world-boss destination
                poly(c,accessible?0xffecb77e:0xff8e8992,
                        x-19,y-8,x,y-32,x+20,y-8);
                c.drawRect(x-16,y-8,x+17,y+14,p);
                circle(c,x,y-30,6,0xffffde91);
            }else if(i%3==0){
                poly(c,0xffb1c5ad,x-17,y-11,x,y-29,x+17,y-11);
                p.setColor(0xff7f8b83);
                c.drawRect(x-14,y-11,x+14,y+13,p);
            }else if(i%3==1){
                poly(c,0xffcaa27b,x-18,y+9,x-2,y-30,x+18,y+9);
                line(c,x-2,y-24,x-2,y+10,3,0xfff3c681);
            }else{
                line(c,x,y+10,x,y-26,6,0xffd8ccb0);
                poly(c,0xff8ed8cf,x,y-26,x+19,y-17,x,y-9);
            }
            if(!accessible)circle(c,x,y,25,0x99202c3d);
            label(c,""+(i+1),x,y+24,17,active?0xffffe4a7:
                    complete?0xffb6f9cb:accessible?0xfff4eddd:0xffacb8ba);
            if(active){
                circle(c,x,y,35,0x22ffcb80);
                line(c,x-21,y+36,x+23,y+36,3,0xffffd397);
            }
        }
        // Landmark text is rendered live in addition to hitboxes.
        label(c,Campaign.WORLDS[world].toUpperCase(),l+225,t+32,23,0xffffe9bb);
        label(c,"MISSION ROUTE  •  10 LOCATIONS",l+215,b-19,16,0xffffefcb);
        c.restore();
    }
}
