package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import java.util.Random;

/** Original animated, illustrated vector environment art for campaign cards and menu scenes.
 * Drawn at a common local resolution and scaled to each viewport. No external image download.
 */
public final class WorldArt {
    private WorldArt() {}
    private static final Paint P=new Paint(Paint.ANTI_ALIAS_FLAG);
    public static final int[] SKY={
        0xff71aba0,0xff446c75,0xff7b9990,0xffdda06b,0xff8caec8,
        0xffa98b72,0xff568ba7,0xff687fb5,0xff704b78,0xffa25e65
    };
    public static final int[] LAND={
        0xff417d64,0xff2d615d,0xff4d7a68,0xffab7a4e,0xff678b93,
        0xff766963,0xff447f88,0xff5d668d,0xff504366,0xff765164
    };
    private static int color(int base,float f) {
        return Color.rgb((int)Math.max(0,Math.min(255,Color.red(base)*f)),
                         (int)Math.max(0,Math.min(255,Color.green(base)*f)),
                         (int)Math.max(0,Math.min(255,Color.blue(base)*f)));
    }
    private static void rect(Canvas c,float l,float t,float r,float b,int cl){
        P.setColor(cl);P.setStyle(Paint.Style.FILL);P.setShader(null);c.drawRect(l,t,r,b,P);
    }
    private static void circle(Canvas c,float x,float y,float rad,int cl){
        P.setColor(cl);P.setShader(null);P.setStyle(Paint.Style.FILL);c.drawCircle(x,y,rad,P);
    }
    private static void oval(Canvas c,float l,float t,float r,float b,int cl){
        P.setShader(null);P.setColor(cl);P.setStyle(Paint.Style.FILL);c.drawOval(l,t,r,b,P);
    }
    private static void line(Canvas c,float x,float y,float xx,float yy,int cl,float w) {
        P.setShader(null);P.setColor(cl);P.setStyle(Paint.Style.STROKE);
        P.setStrokeWidth(w);P.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x,y,xx,yy,P);
        P.setStyle(Paint.Style.FILL);
    }
    private static void poly(Canvas c,int cl,float... pts){
        if(pts.length<6)return;
        P.setShader(null);P.setColor(cl);P.setStyle(Paint.Style.FILL);
        Path path=new Path();path.moveTo(pts[0],pts[1]);
        for(int i=2;i<pts.length;i+=2)path.lineTo(pts[i],pts[i+1]);
        path.close();c.drawPath(path,P);
    }
    private static void rounded(Canvas c,float l,float t,float r,float b,float rad,int col){
        P.setShader(null);P.setColor(col);P.setStyle(Paint.Style.FILL);
        c.drawRoundRect(l,t,r,b,rad,rad,P);
    }
    private static void tree(Canvas c,float x,float y,float sz,int biome,float sway) {
        int foliage=biome==4?0xffe3e8e0:biome==3?0xffa77c52:
             biome==8?0xff725785:biome==9?0xffbe715d:
             biome==6?0xff61b6c1:0xff3d775c;
        oval(c,x-13*sz,y+1*sz,x+15*sz,y+8*sz,0x44000000);
        rect(c,x-3*sz,y-27*sz,x+3*sz,y+3*sz,0xff665644);
        poly(c,color(foliage,.65f),x-23*sz+sway,y-12*sz,
             x-1*sz+sway,y-61*sz,x+26*sz+sway,y-12*sz);
        poly(c,foliage,x-22*sz+sway,y-30*sz,
             x+sway,y-70*sz,x+23*sz+sway,y-30*sz);
        poly(c,color(foliage,1.27f),x+sway,y-70*sz,x+23*sz+sway,y-30*sz,
             x+sway,y-30*sz);
        circle(c,x-5*sz+sway,y-45*sz,2.2f*sz,0x99f8d8a6);
    }
    private static void fortress(Canvas c,float x,float y,float sz,int biome,float time){
        c.save();c.translate(x,y);c.scale(sz,sz);
        oval(c,-65,31,71,47,0x44000000);
        rounded(c,-55,-13,53,42,4,color(0xff879394,.9f));
        poly(c,0xff6c767e,-57,-15,-42,-42,38,-42,55,-15);
        poly(c,color(SKY[biome],.65f),-57,-15,-42,-42,38,-42,0,-15);
        for(int s=-1;s<=1;s+=2) {
            rect(c,35*s-15,-59,35*s+15,32,0xff9daaa6);
            poly(c,0xff375d69,35*s-22,-59,35*s,-87,35*s+22,-59);
            rect(c,35*s-4,-33,35*s+4,-16,0xff365d68);
        }
        rounded(c,-16,-4,15,42,5,0xff524e4a);
        rounded(c,-9,4,8,42,4,0xffc6a37e);
        line(c,0,-89,0,-116,0xffe0d3a3,3);
        float flag=(float)Math.sin(time*2.5f)*5;
        poly(c,0xfff7b66d,2,-114,34+flag,-105,2,-97);
        circle(c,0,-28,6,0xff9de8e1);
        c.restore();
    }
    private static void mountains(Canvas c,int biome,float shift){
        int far=color(SKY[biome],.76f);
        int near=color(LAND[biome],.77f);
        for(int i=-1;i<6;i++) {
            float xx=i*108+shift;
            float peak=65+(i*41)%58;
            poly(c,far,xx-90,157,xx,peak,xx+95,157);
            if(biome==4 || biome==7)
                poly(c,0xffd5e5e5,xx-22,peak+34,xx,peak,xx+25,peak+36,
                     xx+3,peak+29);
        }
        for(int i=-1;i<5;i++) {
            float xx=i*132-38;
            poly(c,near,xx-130,205,xx,112+(i*23)%39,xx+145,205);
        }
    }
    private static void stream(Canvas c,int variant,int biome) {
        Path path=new Path();
        if(variant==1){
            path.moveTo(-30,40);path.cubicTo(95,90,150,90,210,124);
            path.cubicTo(290,166,295,191,430,245);
        } else {
            path.moveTo(-20,185);path.cubicTo(112,155,154,209,213,197);
            path.cubicTo(311,172,339,188,430,132);
        }
        P.setStyle(Paint.Style.STROKE);P.setStrokeWidth(34);
        P.setColor(0xff315970);c.drawPath(path,P);
        P.setStrokeWidth(20);P.setColor(biome==4?0xffafc7d6:0xff639cad);
        c.drawPath(path,P);
        P.setStrokeWidth(2);P.setColor(0x88e4f9e9);c.drawPath(path,P);
        P.setStyle(Paint.Style.FILL);
        if(variant==1) {
            // Broken pass: three plank platforms and two watchtowers.
            poly(c,0xff5a463e,146,98,259,129,252,148,143,116);
            for(int i=0;i<8;i++){
                float x=149+i*14;
                line(c,x,100+i*3.1f,x-4,116+i*3.1f,0xffd5ad7c,10);
            }
            line(c,135,91,260,124,0xfff5c88c,2);
        }
    }
    /** 400x230 original painted style vector postcard. Variant 0 forest / 1 crossing / 2 siege. */
    public static void drawScene(Canvas c,int biome,int variant,float left,float top,
                                 float width,float height,float time) {
        int b=Math.max(0,Math.min(9,biome));
        c.save();
        c.translate(left,top);c.scale(width/400f,height/230f);
        c.clipRect(0,0,400,230);
        P.setShader(new LinearGradient(0,0,0,235,color(SKY[b],1.22f),
                color(SKY[b],.57f),Shader.TileMode.CLAMP));
        c.drawRect(0,0,400,230,P);P.setShader(null);
        circle(c,334,47,22,0x55fff4c5);
        circle(c,334,47,14,0xfff5dfb2);
        mountains(c,b,(variant*11)%42);
        poly(c,LAND[b],-5,165,82,147,160,153,231,148,317,159,405,138,405,240,-5,240);
        poly(c,color(LAND[b],.73f),-5,195,100,174,176,199,260,179,405,170,405,235,-5,235);
        if(variant==1 || b==2 || b==6) stream(c,variant,b);
        if(variant==0){
            Path road=new Path();road.moveTo(195,240);road.cubicTo(146,215,197,188,226,168);
            road.cubicTo(250,148,277,136,297,123);
            P.setColor(0xffc9ac7c);P.setStrokeWidth(17);P.setStyle(Paint.Style.STROKE);
            c.drawPath(road,P);P.setColor(0xffe2c79b);P.setStrokeWidth(2);c.drawPath(road,P);
            P.setStyle(Paint.Style.FILL);
        } else if(variant==2) {
            poly(c,0xffa88c75,37,228,172,181,210,191,349,230);
            line(c,40,228,172,183,0xffd9b48a,7);
        }
        Random rng=new Random(1234+b*93+variant*393);
        for(int i=0;i<24;i++){
            float xx=rng.nextFloat()*420-10;
            float yy=168+rng.nextFloat()*80;
            float sz=.25f+rng.nextFloat()*.56f;
            float sway=(float)Math.sin(time*1.5f+i*2)*2.3f;
            if(variant==2 && xx>110 && xx<280 && yy<222)continue;
            tree(c,xx,yy,sz,b,sway);
        }
        if(variant==2)fortress(c,205,171,.95f,b,time);
        if(variant==0)fortress(c,305,143,.44f,b,time);
        if(variant==1){
            fortress(c,331,154,.58f,b,time);
            poly(c,0xffb7a08a,55,167,93,131,113,162,96,176);
        }
        for(int i=0;i<14;i++){
            float x=(i*87+14)%400;
            float y=(i*47+19+(int)(time*12))%230;
            circle(c,x,y,1.1f,b==4?0x99ffffff:0x779df5d1);
        }
        // A subtle artwork frame.
        line(c,1,1,399,1,0x44ffffff,2);
        line(c,1,229,399,229,0x770b1723,3);
        c.restore();
    }
    /** Distinct visual treatment for six genuinely equipable weapons. */
    public static void drawWeapon(Canvas c,int id,float x,float y,float scale,float time) {
        int idx=Math.max(0,Math.min(5,id));
        c.save();c.translate(x,y);c.scale(scale,scale);
        oval(c,-27,35,28,43,0x55000000);
        drawWeaponGeometry(c,idx,time);
        c.restore();
    }

    /**
     * Held weapons need TRANSPARENT silhouettes. Inventory-card bitmaps have
     * opaque colored badge backgrounds and cannot be used as hand attachments.
     * This draws distinct no-background combat geometry until clean painted
     * transparent weapon layers are available.
     */
    public static void drawHeldWeapon(Canvas c,int id,float x,float y,
                                      float scale,float time) {
        int idx=Math.max(0,Math.min(5,id));
        c.save();c.translate(x,y);c.scale(scale,scale);
        drawWeaponGeometry(c,idx,time);
        c.restore();
    }

    private static void drawWeaponGeometry(Canvas c,int idx,float time) {
        float sway=(float)Math.sin(time*3+idx)*2f;
        if(idx==0 || idx==4) { // Real compact recurve crossbow / siege repeater.
            int metal=idx==4?0xffddb16e:0xffa5c4cf;
            int highlight=idx==4?0xffffdd9e:0xffedfaff;
            // Grip and narrow stock: arrow points up the weapon's own local -Y.
            rounded(c,-7,-33,7,30,3,0xff3c3032);
            rounded(c,-4,-28,4,24,2,0xff896b4d);
            rounded(c,-7,-18,7,-12,2,0xffd6b98b);
            line(c,-4,16,4,16,0xffdbc6a2,2);
            // Curved composite bow limbs, not four broad flat paddles.
            Path bow=new Path();
            bow.moveTo(-33,-22);
            bow.cubicTo(-28,-38,-14,-41,0,-31);
            bow.cubicTo(14,-41,28,-38,33,-22);
            P.setShader(null);P.setStyle(Paint.Style.STROKE);
            P.setStrokeCap(Paint.Cap.ROUND);P.setStrokeWidth(7);
            P.setColor(0xff354f5d);c.drawPath(bow,P);
            P.setColor(metal);P.setStrokeWidth(3.8f);c.drawPath(bow,P);
            P.setColor(highlight);P.setStrokeWidth(1.2f);c.drawPath(bow,P);
            P.setStyle(Paint.Style.FILL);
            // Draw taut bowstring behind the actual loaded bolt.
            line(c,-32,-22,0,-9,0xffe1d9c8,1.8f);
            line(c,32,-22,0,-9,0xffe1d9c8,1.8f);
            line(c,0,-31,0,-51,0xffe9e6d5,2.5f);
            poly(c,highlight,0,-58,-5,-47,5,-47);
            rounded(c,-9,-15,9,-8,3,0xff4c626c);
            circle(c,-2,-12,3,0xfff7db9a);
            // Engraved crossbow stock, trigger guard and limb rivets.
            line(c,-3,-5,-3,12,0xffffd9a1,1.5f);
            line(c,-8,17,7,17,0xff2d3839,3);
            circle(c,-26,-30,3,metal);
            circle(c,26,-30,3,metal);
            line(c,-12,-31,-19,-28,0xffd9ece6,2);
            line(c,12,-31,19,-28,0xffd9ece6,2);
            if(idx==4) {
                // Repeater has a small feed magazine and second, *short*
                // barrel rail, not extra windmill wings.
                rounded(c,-14,-2,-5,22,4,0xff51443e);
                rounded(c,-12,0,-7,15,2,0xffcfb36d);
                line(c,-5,-43,-5,-18,highlight,2.3f);
                circle(c,-3,11,4,0xffffda83);
                for(int j=0;j<3;j++)line(c,-10+j*6,20,-10+j*6,26,
                        0xffd6b477,1.5f);
            }
        } else if(idx==1 || idx==2) { // Swords
            int blade=idx==2?0xffff9c54:0xffdeebf1;
            poly(c,blade,-8,-17,-3,-55,0,-65,8,-17,3,-12);
            poly(c,color(blade,.67f),0,-63,8,-18,3,-12,0,-12);
            line(c,-21,-10,21,-10,0xffb7a17b,8);
            line(c,0,-9,0,30,0xff654c42,8);
            circle(c,0,31,7,0xffe7bc6c);
            circle(c,0,-10,6,0xffd7bc80);
            // Blade fuller, two-stage crossguard and gem-set pommel. The
            // details move with the same weapon's real attack frames.
            line(c,-1,-51,-1,-19,0xfffbfff7,1.4f);
            line(c,-16,-12,-9,-7,0xff735e54,2);
            line(c,16,-12,9,-7,0xff735e54,2);
            circle(c,0,-11,3,idx==2?0xffffe9a4:0xff7edbe2);
            line(c,-4,6,-4,24,0xffb28d67,1.6f);
            line(c,4,7,4,25,0xff392c2c,1.4f);
            if(idx==2){
                circle(c,0,-32,21,0x33ff9b49);
                poly(c,0xfffbe4a2,-3,-18,-13,-39,-3,-49,3,-29,14,-48,12,-27);
            }
        } else if(idx==3 || idx==5){ // Staff and frost lance
            int gem=idx==3?0xffa9a9ff:0xff9cf5ff;
            line(c,-3,33,4,-44,idx==3?0xff96705f:0xffc9dce7,10);
            circle(c,4,-41,12,0xff263e56);
            poly(c,gem,4+sway,-68,18,-42,4,-25,-12,-42);
            circle(c,4,-43,8,0xffffffff);
            // Socket rings and repeated shaft wrappings anchor the crystal
            // instead of making it appear disconnected from the weapon.
            for(int ring=0;ring<4;ring++){
                float y=-20+ring*14;
                line(c,-7,y,9,y+1,idx==3?0xffddba82:0xffe8fcff,2.3f);
            }
            poly(c,0xff5f7385,-8,-41,-4,-49,13,-49,18,-40,8,-35);
            circle(c,4,-42,5,gem);
            if(idx==5){
                poly(c,0xffd2faff,-17,-50,4,-84,18,-50,5,-59);
                poly(c,0xff73bcca,-27,-4,-5,-14,5,-2,-5,7);
            } else {
                circle(c,-23,-23,6,0x66b2d6ff);
                circle(c,25,-36,7,0x66b2d6ff);
            }
        }
    }
}
