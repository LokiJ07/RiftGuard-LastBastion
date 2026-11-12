package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import java.util.Random;

/**
 * Native port of kingdom-atlas-kit.html for the ACTUAL interactive MAP scene.
 * The ten world nodes, ten waypoints per realm, river, mountains, biome props,
 * bridges, linked progression routes and lock state are Android Canvas code.
 *
 * Coordinates intentionally match AtlasArt.hitRegion and RegionJourney.hitLevel
 * to retain functioning touch hitboxes, stage unlocks and saved progress.
 */
public final class NativeKingdomAtlas {
    private NativeKingdomAtlas(){}
    private static final Paint P=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    private static final float L=73,T=120,R=870,B=569;
    private static final float[] WX={190,276,403,535,683,762,682,531,372,211};
    private static final float[] WY={470,363,455,364,419,305,209,267,188,251};
    private static final float[] LX={143,220,317,424,556,694,769,653,503,331};
    private static final float[] LY={452,368,492,396,444,357,253,193,260,177};
    private static final int[] PRIMARY={
        0xff7fd68f,0xff3fa58f,0xff9aaa60,0xfff08a4a,0xffbfeaff,
        0xffa8aeb8,0xffb36bff,0xff6fb4dc,0xffa48ac9,0xfff2c86b
    };
    private static final int[] TREE={
        0xff2f8a62,0xff1f7a58,0xff5a7a3a,0xff7a4a2a,0xff3a7f78,
        0xff4a6a60,0xff4a3a8a,0xff3a7f88,0xff4a3a8a,0xff3a7a5a
    };
    private static final int[] GROUND={
        0xff5a8f6a,0xff4a8a6a,0xff7a8f58,0xff8a5040,0xffbcd0da,
        0xff8a9098,0xff5a4a8a,0xff6a9fb0,0xff5a4a8a,0xff7a8a66
    };

    private static void fill(int color){P.setShader(null);P.setColor(color);P.setStyle(Paint.Style.FILL);}
    private static void circle(Canvas c,float x,float y,float r,int color){
        fill(color);c.drawCircle(x,y,r,P);
    }
    private static void rect(Canvas c,float l,float t,float r,float b,int color){
        fill(color);c.drawRect(l,t,r,b,P);
    }
    private static void oval(Canvas c,float x,float y,float rx,float ry,int color){
        fill(color);c.drawOval(x-rx,y-ry,x+rx,y+ry,P);
    }
    private static void line(Canvas c,float x,float y,float ex,float ey,float w,int color){
        fill(color);P.setStyle(Paint.Style.STROKE);P.setStrokeWidth(w);
        P.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x,y,ex,ey,P);P.setStyle(Paint.Style.FILL);
    }
    private static void poly(Canvas c,int color,float...points){
        Path a=new Path();a.moveTo(points[0],points[1]);
        for(int i=2;i<points.length;i+=2)a.lineTo(points[i],points[i+1]);
        a.close();fill(color);c.drawPath(a,P);
    }
    private static void caption(Canvas c,String text,float x,float y,float size,int color){
        P.setTypeface(android.graphics.Typeface.create("sans-serif-condensed",
                android.graphics.Typeface.BOLD));
        P.setTextAlign(Paint.Align.CENTER);P.setTextSize(size);
        P.setStyle(Paint.Style.STROKE);P.setStrokeWidth(3.5f);
        P.setColor(0xdd102c2d);c.drawText(text,x,y,P);
        P.setStyle(Paint.Style.FILL);P.setColor(color);c.drawText(text,x,y,P);
    }
    private static void pine(Canvas c,float x,float y,float scale,int world){
        c.save();c.translate(x,y);c.scale(scale,scale);
        oval(c,1,3,12,4,0x440e241b);
        rect(c,-2,-12,2,3,0xff614d37);
        for(int i=0;i<3;i++){
            float width=17-i*4,by=-8-i*11;
            poly(c,TREE[world],-width,by,0,by-26,width,by);
        }
        c.restore();
    }
    private static void landProp(Canvas c,Random random,int world,float x,float y){
        int kind=random.nextInt(9);
        float size=.42f+random.nextFloat()*.43f;
        if(kind<5)pine(c,x,y,size,world);
        else if(kind<7){
            oval(c,x+2,y+3,12*size,3*size,0x44000000);
            poly(c,0xff84999a,x-15*size,y,x-7*size,y-10*size,
                    x+8*size,y-13*size,x+18*size,y);
            poly(c,0xffc3d2cb,x-7*size,y-10*size,x+5*size,y-18*size,
                    x+14*size,y-11*size);
        }else if(kind==7){
            oval(c,x,y-4,10*size,9*size,TREE[world]);
            circle(c,x+3,y-8,2,PRIMARY[world]);
        }else {
            poly(c,PRIMARY[world],x-6*size,y,x,y-25*size,
                    x+7*size,y);
        }
    }
    private static void icon(Canvas c,float x,float y,int world,boolean unlocked){
        fill(unlocked?0xff334a4c:0xff22313b);
        c.drawRoundRect(x-14,y-13,x+14,y+14,2,2,P);
        for(int i=-1;i<=1;i+=2)rect(c,x+i*12-4,y-20,x+i*12+4,y-8,
                unlocked?0xffaabfba:0xff65767e);
        rect(c,x-4,y+2,x+4,y+14,0xff1c3337);
    }
    /** World atlas: 10 islands/regions, winding river, connected region badges. */
    public static void drawWorld(Canvas c,int selected,int unlocked,float time){
        c.save();c.clipRect(L,T,R,B);
        fill(0xffac8a60);c.drawRect(L,T,R,B,P);
        Random r=new Random(99);
        for(int i=0;i<220;i++){
            float x=L+r.nextFloat()*(R-L),y=T+r.nextFloat()*(B-T);
            line(c,x,y,x+6+r.nextFloat()*15,y-2,1,
                    i%2==0?0x1aefe0c8:0x1c523b2b);
        }
        // Clipped hand-shaped kingdom coastline from HTML kingdom().
        Path coast=new Path();
        coast.moveTo(85,300);coast.lineTo(117,223);
        coast.lineTo(239,157);coast.lineTo(389,143);
        coast.lineTo(567,154);coast.lineTo(720,179);
        coast.lineTo(842,277);coast.lineTo(845,381);
        coast.lineTo(724,506);coast.lineTo(558,549);
        coast.lineTo(365,532);coast.lineTo(201,495);
        coast.lineTo(101,414);coast.close();
        fill(0xff306d68);c.drawPath(coast,P);
        c.save();c.clipPath(coast);
        fill(0xff70b382);c.drawRect(92,152,845,551,P);
        P.setShader(new LinearGradient(L,T,R,B,
                0xff8ac58d,0xff57a575,Shader.TileMode.CLAMP));
        c.drawRect(92,148,848,556,P);P.setShader(null);
        for(int i=0;i<150;i++){
            float x=90+r.nextFloat()*750,y=150+r.nextFloat()*395;
            boolean near=false;int world=0;
            float dist=Float.MAX_VALUE;
            for(int k=0;k<10;k++){
                float d=(WX[k]-x)*(WX[k]-x)+(WY[k]-y)*(WY[k]-y);
                if(d<dist){dist=d;world=k;}
                if(d<2600)near=true;
            }
            if(!near)landProp(c,r,world,x,y);
        }
        c.restore();
        Path river=new Path();river.moveTo(412,105);
        river.cubicTo(389,236,546,263,450,363);
        river.cubicTo(389,446,518,479,589,571);
        fill(0xff3076a6);P.setStyle(Paint.Style.STROKE);
        P.setStrokeCap(Paint.Cap.ROUND);P.setStrokeWidth(22);c.drawPath(river,P);
        P.setStrokeWidth(14);P.setColor(0xff63b9e2);c.drawPath(river,P);
        P.setStrokeWidth(3);P.setColor(0x88f1ffff);c.drawPath(river,P);
        P.setStyle(Paint.Style.FILL);
        // Trail is connected to actual badge centers, not arbitrary dots.
        for(int i=0;i<9;i++){
            line(c,WX[i],WY[i],WX[i+1],WY[i+1],14,0xff745b33);
            line(c,WX[i],WY[i],WX[i+1],WY[i+1],10,0xffdbb77d);
            if(i==3)for(int k=0;k<5;k++)circle(c,442+k*4,410,2,0xff806447);
        }
        for(int i=0;i<10;i++){
            float x=WX[i],y=WY[i];
            boolean open=unlocked>=i*30,active=i==selected;
            oval(c,x+2,y+42,37,9,0x550b2118);
            circle(c,x,y,42,open?PRIMARY[i]:0xff57636a);
            circle(c,x,y,35,open?GROUND[i]:0xff354854);
            poly(c,0xff435d63,x-35,y+8,x-19,y-13,x,y+2,
                    x+20,y-18,x+35,y+12);
            icon(c,x,y,i,open);
            if(!open)circle(c,x,y,35,0x99102030);
            fill(active?0xfff2c86b:PRIMARY[i]);
            P.setStyle(Paint.Style.STROKE);P.setStrokeWidth(active?7:4);
            c.drawCircle(x,y,42,P);P.setStyle(Paint.Style.FILL);
            if(active){
                fill(0xaaffedbe);P.setStyle(Paint.Style.STROKE);
                P.setStrokeWidth(2);c.drawCircle(x,y,46+(float)Math.sin(time*3)*3,P);
                P.setStyle(Paint.Style.FILL);
                caption(c,Campaign.WORLDS[i].toUpperCase(),x,y-55,15,0xffffdd8b);
            }
            caption(c,open?String.valueOf(i+1):"LOCK",x,y+52,14,0xffffffff);
        }
        c.restore();
    }
    /** Region route: landscape, scattered native props, 10 linked stops. */
    public static void drawRegion(Canvas c,int world,int selectedLevel,
                                  int unlocked,float time){
        c.save();c.clipRect(L,T,R,B);
        fill(GROUND[world]);c.drawRect(L,T,R,B,P);
        P.setShader(new LinearGradient(L,T,L,B,
                GROUND[world],0xff274b47,Shader.TileMode.CLAMP));
        c.drawRect(L,T,R,B,P);P.setShader(null);
        Random r=new Random(78021+world*199);
        for(int layer=0;layer<3;layer++){
            Path hills=new Path();
            float base=T+145+layer*54;
            hills.moveTo(L,base+40);
            for(float x=L-30;x<R+40;x+=72)
                hills.lineTo(x,base-60-r.nextFloat()*76);
            hills.lineTo(R,base+75);hills.close();
            fill(layer==0?0xff7aacae:layer==1?0xff5e939c:0xff438377);
            P.setAlpha(layer==2?200:137);c.drawPath(hills,P);P.setAlpha(255);
        }
        // Native props from the HTML's tree/rock/crystal biome mix with
        // safe clear space around actual clickable route nodes.
        for(int i=0;i<190;i++){
            float x=L+8+r.nextFloat()*(R-L-15);
            float y=T+20+r.nextFloat()*(B-T-28);
            boolean near=false;
            for(int j=0;j<10;j++){
                float dx=x-LX[j],dy=y-LY[j];
                if(dx*dx+dy*dy<sq(33))near=true;
            }
            for(int j=0;j<9;j++){
                float mx=(LX[j]+LX[j+1])*.5f,my=(LY[j]+LY[j+1])*.5f;
                float dx=x-mx,dy=y-my;
                if(dx*dx+dy*dy<sq(27))near=true;
            }
            if(!near)landProp(c,r,world,x,y);
        }
        for(int i=0;i<9;i++){
            boolean open=Campaign.accessible(Campaign.stage(world*10+i+1,0),unlocked);
            line(c,LX[i],LY[i],LX[i+1],LY[i+1],17,0xff355249);
            line(c,LX[i],LY[i],LX[i+1],LY[i+1],12,
                    open?0xffd0b87b:0xff889a91);
            line(c,LX[i],LY[i],LX[i+1],LY[i+1],2,
                    open?0xfff2e5bc:0xffbecdc1);
        }
        for(int i=0;i<10;i++){
            int level=world*10+i+1;
            boolean open=Campaign.accessible(Campaign.stage(level,0),unlocked);
            boolean active=level==selectedLevel;
            float x=LX[i],y=LY[i];
            circle(c,x,y+6,32,0x440b1e2b);
            circle(c,x,y,30,0xff263b4a);
            circle(c,x,y,24,open?0xff4c7875:0xff253644);
            if(i%3==0){
                poly(c,open?0xffdfc883:0xff647883,
                        x-17,y-4,x,y-23,x+17,y-4);
                rect(c,x-13,y-4,x+13,y+11,open?0xffe0d4ac:0xff647583);
            }else if(i%3==1){
                poly(c,open?0xffc0dfd7:0xff556b75,
                        x-13,y+10,x,y-23,x+13,y+10);
            }else{
                line(c,x,y+10,x,y-23,4,open?0xfff9dda2:0xff607381);
                poly(c,open?0xffe7bc72:0xff607381,
                        x,y-23,x+18,y-16,x,y-7);
            }
            if(!open)circle(c,x,y,24,0x77202a3a);
            fill(active?0xfff5d47f:open?PRIMARY[world]:0xff6d8692);
            P.setStyle(Paint.Style.STROKE);P.setStrokeWidth(active?5:3);
            c.drawCircle(x,y,30+(active?(float)Math.sin(time*3)*2:0),P);
            P.setStyle(Paint.Style.FILL);
            caption(c,String.valueOf(i+1),x,y+27,15,active?0xffffe7a7:0xfff2f3ed);
        }
        caption(c,Campaign.WORLDS[world].toUpperCase(),L+213,T+28,23,0xffffffff);
        caption(c,"MISSION ROUTE  •  10 LOCATIONS",L+215,B-14,15,0xffffffff);
        c.restore();
    }
    private static float sq(float x){return x*x;}
}
