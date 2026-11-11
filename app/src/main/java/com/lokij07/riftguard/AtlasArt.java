package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;

import java.util.Random;

/**
 * Real-time layered fantasy campaign atlas. Its labels, routes and interactive
 * locations are separate game elements; none are part of a pasted screenshot.
 */
public final class AtlasArt {
    private AtlasArt() {}
    public static final float[] NODES_X={190,276,403,535,683,762,682,531,372,211};
    public static final float[] NODES_Y={470,363,455,364,419,305,209,267,188,251};
    private static final float LEFT=73,TOP=120,RIGHT=870,BOTTOM=569;
    private static final int[] COLORS={
        0xff58b883,0xff2f8d77,0xff7fa67d,0xffd99a61,0xffa9d8e9,
        0xffa59383,0xff68bfcd,0xff7f9bd3,0xff8b74b6,0xffc37975
    };
    private static final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);

    public static int hitRegion(float x,float y) {
        for(int i=0;i<NODES_X.length;i++) {
            float dx=x-NODES_X[i],dy=y-NODES_Y[i];
            if(dx*dx+dy*dy<43*43)return i;
        }
        return -1;
    }
    private static void circle(Canvas c,float x,float y,float r,int color) {
        p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);
        c.drawCircle(x,y,r,p);
    }
    private static void line(Canvas c,float x,float y,float endX,float endY,float size,int color) {
        p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(size);
        p.setStrokeCap(Paint.Cap.ROUND);p.setColor(color);c.drawLine(x,y,endX,endY,p);
        p.setStyle(Paint.Style.FILL);
    }
    private static void polygon(Canvas c,int color,float... pts) {
        if(pts.length<6)return;
        Path path=new Path();path.moveTo(pts[0],pts[1]);
        for(int i=2;i<pts.length;i+=2)path.lineTo(pts[i],pts[i+1]);
        path.close();
        p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);
        c.drawPath(path,p);
    }
    private static void label(Canvas c,String label,float x,float y,int color) {
        p.setShader(null);p.setTypeface(android.graphics.Typeface.create(
                "sans-serif-condensed",android.graphics.Typeface.BOLD));
        p.setTextSize(15);p.setColor(0xaa0b2430);p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(3);p.setTextAlign(Paint.Align.CENTER);
        c.drawText(label,x,y,p);
        p.setStyle(Paint.Style.FILL);p.setColor(color);c.drawText(label,x,y,p);
    }
    public static void draw(Canvas c,int selected,int unlocked,float time) {
        c.save();c.clipRect(LEFT,TOP,RIGHT,BOTTOM);
        p.setShader(new LinearGradient(LEFT,TOP,RIGHT,BOTTOM,
                0xff234960,0xff112f41,Shader.TileMode.CLAMP));
        c.drawRect(LEFT,TOP,RIGHT,BOTTOM,p);p.setShader(null);

        // Multi-layer shore and heartland. No text, UI or quests are baked in.
        polygon(c,0xff315f5a,95,285,130,214,266,149,448,131,652,152,
                837,232,853,358,793,497,647,552,430,551,232,536,110,423);
        polygon(c,0xff568f68,113,301,147,239,283,169,431,163,647,179,
                812,241,819,360,764,481,636,521,413,529,243,503,133,419);
        polygon(c,0xff75a46d,169,280,204,238,331,206,452,230,
                631,239,756,290,731,422,565,472,367,487,197,415);
        polygon(c,0x4465ddad,172,304,274,256,393,292,556,274,735,328,
                685,442,524,478,350,439,203,394);
        // Coastline and lake highlights.
        for(int i=0;i<40;i++){
            float x=110+(i*137)%730,y=150+(i*97)%375;
            if((i%6)==0)circle(c,x,y,4,0x4497d5c4);
        }
        Path river=new Path();
        river.moveTo(440,140);river.cubicTo(421,221,543,260,455,341);
        river.cubicTo(352,428,474,479,583,548);
        p.setColor(0xff316e89);p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(23);p.setStrokeCap(Paint.Cap.ROUND);c.drawPath(river,p);
        p.setStrokeWidth(12);p.setColor(0xff61adc5);c.drawPath(river,p);
        p.setStrokeWidth(2);p.setColor(0xffaddce5);c.drawPath(river,p);
        p.setStyle(Paint.Style.FILL);

        // Distinctive biome scenery: painted sprite scenes are clipped into
        // individual region landscapes; world labels and flags stay in code.
        for(int i=0;i<10;i++){
            float x=NODES_X[i],y=NODES_Y[i];
            int tint=COLORS[i];
            circle(c,x,y+11,55,0x66132c32);
            circle(c,x,y,48,tint);
            c.save();
            Path crop=new Path();crop.addCircle(x,y-5,39,Path.Direction.CW);
            c.clipPath(crop);
            WorldArt.drawScene(c,i,i%3,x-60,y-56,120,94,time);
            c.restore();
            if(unlocked<i*30)circle(c,x,y-5,39,0x99132537);
        }
        // Trails connect only the real region progression, not arbitrary pins.
        for(int i=0;i<9;i++){
            float x0=NODES_X[i],y0=NODES_Y[i],x1=NODES_X[i+1],y1=NODES_Y[i+1];
            float dx=x1-x0,dy=y1-y0;
            float len=(float)Math.hypot(dx,dy);
            float nx=dx/len,ny=dy/len;
            float left=x0+nx*49,top=y0+ny*49;
            float right=x1-nx*49,bottom=y1-ny*49;
            line(c,left,top,right,bottom,11,0xcc284b46);
            line(c,left,top,right,bottom,5,0xffceb382);
            if(unlocked>=i*30){
                float t=(time*.14f+i*.17f)%1f;
                float px=left+(right-left)*t,py=top+(bottom-top)*t;
                circle(c,px,py,6,0x99fff0bb);
                circle(c,px,py,2.6f,0xfffff5dc);
            }
        }
        // Living beacon, medals and dynamic region identifiers.
        for(int i=0;i<10;i++){
            float x=NODES_X[i],y=NODES_Y[i];
            boolean active=i==selected;
            boolean accessible=unlocked>=i*30;
            float pulse=active?3+(float)Math.sin(time*2.8f)*2:0;
            p.setShader(null);p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(active?5:2.7f);
            p.setColor(active?0xffffd47b:accessible?0xffcadbd1:0xff67848c);
            c.drawCircle(x,y-5,42+pulse,p);p.setStyle(Paint.Style.FILL);
            polygon(c,accessible?0xffdec38a:0xff627380,x-13,y-47,x+9,y-47,
                    x+15,y-27,x-6,y-27);
            circle(c,x+16,y-39,4,active?0xfffff1bf:0xff768f95);
            label(c,accessible?""+(i+1):"LOCK",x,y+55,
                    active?0xffffe8a8:accessible?0xfff5e4c0:0xffb7c3c1);
        }
        // Animated low cloud bank over the ocean only.
        for(int j=0;j<5;j++){
            float x=85+((j*181+time*8)%800);
            float y=135+(j%3)*202;
            circle(c,x,y,20,0x223e889c);
            circle(c,x+22,y+9,26,0x1b7aa3b2);
        }
        c.restore();
    }
}
