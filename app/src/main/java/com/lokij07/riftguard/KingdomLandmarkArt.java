package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.LinearGradient;
import android.graphics.Shader;

/**
 * Ten ACTUAL biome landmark families, composited into the Java Canvas game.
 * Static geometry is cached in NativeDeployedMapArt's 1900x1050 RGB565 image;
 * ambient motion is drawn after the cached image and below actors/HUD.
 * World positions are outside the traversable path and tower exclusion zone.
 */
public final class KingdomLandmarkArt {
    private KingdomLandmarkArt(){}
    private static final Paint P=new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final int[] ACCENT={
        0xffd8dba2,0xff8de6c2,0xffa5f1c5,0xffff9c5f,0xffdff8ff,
        0xfff0c18b,0xffb99afc,0xff92d9ed,0xffa987e2,0xffffc68d
    };
    public static int family(int stage){Campaign.checkStage(stage);return Campaign.world(stage);}
    private static void fill(int col){P.setShader(null);P.setStyle(Paint.Style.FILL);P.setColor(col);}
    private static void rect(Canvas c,float x,float y,float w,float h,int col){
        fill(col);c.drawRect(x,y,x+w,y+h,P);
    }
    private static void oval(Canvas c,float x,float y,float rx,float ry,int col){
        fill(col);c.drawOval(x-rx,y-ry,x+rx,y+ry,P);
    }
    private static void line(Canvas c,float x,float y,float xx,float yy,int color,float width){
        fill(color);P.setStyle(Paint.Style.STROKE);
        P.setStrokeCap(Paint.Cap.ROUND);P.setStrokeWidth(width);
        c.drawLine(x,y,xx,yy,P);P.setStyle(Paint.Style.FILL);
    }
    private static void polygon(Canvas c,int color,float... v){
        Path p=new Path();p.moveTo(v[0],v[1]);
        for(int i=2;i<v.length;i+=2)p.lineTo(v[i],v[i+1]);
        p.close();fill(color);c.drawPath(p,P);
    }
    /** Landscape landmarks have three deliberately distinct placements. */
    private static float x(int stage,int n){
        return (n==0?400:n==1?1150:1590)+
                (LivingBattlefield.noise(Campaign.seed(stage),980+n)-.5f)*90;
    }
    private static float y(int stage,int n){
        return n==1?850:315;
    }
    public static void drawStatic(Canvas c,int stage){
        Campaign.checkStage(stage);
        int realm=Campaign.world(stage),mission=Campaign.map(stage);
        for(int n=0;n<3;n++){
            float x=x(stage,n),y=y(stage,n);
            // The landmark's world-space footprint lies well clear of the
            // real enemy road. Do not reserve or invent a clickable hotspot.
            c.save();c.translate(x,y);
            c.scale(n==1?.83f:n==2?.72f:1f,n==1?.83f:n==2?.72f:1f);
            oval(c,0,9,104,19,0x550a2325);
            switch(realm){
                case 0: // Greenhaven: layered oak sanctuary and hanging lanterns
                    rect(c,-7,-113,13,117,0xff634c32);
                    line(c,0,-83,-58,-136,0xff66533b,8);
                    line(c,1,-100,67,-161,0xff66533b,10);
                    for(int i=0;i<7;i++){
                        float ox=(i%3-1)*44,oy=-166-(i/3)*25;
                        oval(c,ox,oy,49,33,i%2==0?0xff356b48:0xff52976a);
                    }
                    rect(c,-46,-90,10,35,0xffa58353);rect(c,58,-104,10,43,0xff9d784c);
                    oval(c,-41,-55,8,13,0xffe2cb79);oval(c,63,-60,8,12,0xffe8e399);
                    break;
                case 1: // Hollow: huge arch-root gate and glowing mushrooms
                    for(int side:new int[]{-1,1}){
                        float xx=side*55;
                        polygon(c,0xff4b6857,xx-19,0,xx-36,-121,
                                xx-12,-186,xx+17,-164,xx+28,-36,xx+12,0);
                        oval(c,xx,-138,31,29,0xff365849);
                        oval(c,xx*1.65f,-15,19,9,0xffa2d5ad);
                        oval(c,xx*1.65f,-9,7,11,0xffd0f6b7);
                    }
                    polygon(c,0xff72946d,-55,-151,0,-209,55,-151,42,-126,-42,-126);
                    break;
                case 2: // Swamp: partly submerged stilt-shrine + bog reeds
                    oval(c,0,-2,110,23,0xff3b806e);
                    for(int side:new int[]{-1,1}) {
                        rect(c,side*45-7,-113,14,125,0xff64604b);
                        line(c,side*45,-93,side*71,-120,0xff9c8c69,6);
                    }
                    polygon(c,0xffa7b68b,-78,-115,0,-177,78,-115);
                    rect(c,-55,-115,110,70,0xff617c70);
                    oval(c,0,-92,13,17,0xff8cdbbb);
                    for(int i=0;i<8;i++)line(c,-101+i*28,14,-101+i*28,-35-(i%3)*12,
                            0xff8ea87c,3);
                    break;
                case 3: // Ember: volcanic forge and split obsidian obelisks
                    polygon(c,0xff3b3030,-85,12,-73,-96,-45,-151,-7,-37,20,12);
                    polygon(c,0xff473435,30,12,41,-168,68,-131,93,12);
                    polygon(c,0xffff7d3f,-42,4,-19,-74,2,-31,21,-116,45,8);
                    rect(c,-42,-59,85,44,0xff302b2d);
                    rect(c,-50,-70,100,17,0xffa1644d);
                    oval(c,0,-46,28,13,0xffff9d48);
                    break;
                case 4: // Frostpeak: frozen watch dome and icicle stacks
                    polygon(c,0xffb5dbe2,-90,12,-65,-142,0,-203,72,-133,91,12);
                    polygon(c,0xffe6faff,-90,12,-65,-142,0,-203,1,13);
                    rect(c,-51,-72,101,68,0xff739aa8);
                    polygon(c,0xffddf4f5,-62,-72,0,-122,61,-72);
                    for(int i=0;i<9;i++){
                        float xx=-85+i*21;
                        polygon(c,0xffa0e0f2,xx,-4,xx+8,24+(i%3)*8,xx+16,-4);
                    }
                    break;
                case 5: // Ironfall: forge turbines and linked riveted gantries
                    rect(c,-84,-111,168,105,0xff58656b);
                    rect(c,-93,-125,187,22,0xffbbc0bc);
                    for(int i=0;i<6;i++){
                        rect(c,-79+i*27,-103,5,90,0xff9da5a0);
                        oval(c,-63+i*28,-110,3,3,0xfff0d7a1);
                    }
                    oval(c,0,-62,40,40,0xff2e454e);
                    oval(c,0,-62,24,24,0xff90b3b9);
                    for(int i=0;i<7;i++){
                        double a=i*Math.PI*2/7;
                        line(c,(float)Math.cos(a)*9,-62+(float)Math.sin(a)*9,
                                (float)Math.cos(a)*28,-62+(float)Math.sin(a)*28,
                                0xff293e45,7);
                    }
                    break;
                case 6: // Crystal: iridescent polygonal formation
                    for(int i=0;i<7;i++){
                        float xx=(i-3)*26, ht=67+(i*37)%119;
                        polygon(c,0xff6d73b7,xx-21,13,xx,-ht,xx+20,13);
                        polygon(c,0xffb6c8f7,xx-21,13,xx,-ht,xx+1,13);
                    }
                    oval(c,0,3,91,14,0x668cf2f2);
                    break;
                case 7: // Tempest: wind turbine/observatory with metal blades
                    rect(c,-20,-143,40,151,0xff607f8d);
                    polygon(c,0xff9ab8bd,-42,-143,0,-176,42,-143);
                    oval(c,0,-136,27,27,0xff284e66);
                    for(int i=0;i<4;i++){
                        double angle=i*Math.PI/2+.4;
                        float px=(float)Math.cos(angle),py=(float)Math.sin(angle);
                        polygon(c,0xffb4d9e0,px*16,-136+py*16,
                                px*72-py*14,-136+py*72+px*14,
                                px*71+py*16,-136+py*71-px*16);
                    }
                    break;
                case 8: // Shadow: arch ruin / ribbed ominous columns
                    for(int side:new int[]{-1,1}){
                        rect(c,side*62-14,-172,28,173,0xff4f4a63);
                        rect(c,side*62-22,-175,44,16,0xff756d88);
                    }
                    polygon(c,0xff635a77,-73,-169,0,-221,73,-169,50,-149,0,-191,-50,-149);
                    oval(c,0,-126,26,38,0xff292740);
                    oval(c,0,-126,8,21,0xff9573d0);
                    break;
                default: // Eclipse: eclipse dial & arcane rite pedestal
                    rect(c,-80,-21,160,29,0xff8b6d66);
                    polygon(c,0xff685064,-36,-17,-29,-114,0,-151,30,-114,38,-17);
                    oval(c,0,-129,69,69,0xffe6a678);
                    oval(c,0,-129,57,57,0xff3a3858);
                    oval(c,23,-145,43,43,0xffb96b76);
                    for(int i=0;i<8;i++){
                        double a=i*Math.PI/4;
                        float px=(float)Math.cos(a),py=(float)Math.sin(a);
                        line(c,px*76,-129+py*76,px*90,-129+py*90,0xffe7bfa0,4);
                    }
            }
            // A/B/C scenes add different defenses / siege wear, without
            // changing enemy collisions, actual tower pads or hit regions.
            if(mission==1){
                rect(c,-85,-15,170,12,0xffa68867);
                for(int i=0;i<5;i++)line(c,-70+i*35,-14,-70+i*35,-41,
                        0xffdbc6a1,4);
            }else if(mission==2){
                for(int i=0;i<5;i++)
                    line(c,-72+i*32,-12,-62+i*32,-31-(i%2)*13,
                            0xff2e333c,5);
            }
            c.restore();
        }
    }
    public static void drawMotion(Canvas c,int stage,float time,boolean reduced){
        drawMotion(c,stage,time,reduced,false);
    }
    public static void drawMotion(Canvas c,int stage,float time,
                                  boolean reduced,boolean lowEffects){
        Campaign.checkStage(stage);
        int realm=family(stage), seed=Campaign.seed(stage);
        if(!Float.isFinite(time))return;
        float t=reduced?0:time;
        for(int n=0;n<3;n++){
            float x=x(stage,n),y=y(stage,n),scale=n==1?.83f:n==2?.72f:1f;
            // Particles stick to *real* landmark positions.
            float ox=x,oy=y-115*scale;
            int col=ACCENT[realm];
            for(int i=0;i<(lowEffects?3:7);i++){
                float phase=(i*2.3f+seed%43);
                float px=ox+(float)Math.cos(t*(realm==7?2.6f:.8f)+phase)*
                        (18+i*7)*scale;
                float py=oy+(float)Math.sin(t*1.7f+i)*22*scale+
                        (i-3)*15*scale;
                int a=(int)(75+60*(.5+.5*Math.sin(t*2.8f+i)));
                oval(c,px,py,(i%3+2)*scale,(i%3+2)*scale,
                        ((a&255)<<24)|(col&0xffffff));
            }
        }
    }
}
