package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Thirty genuinely different transparent equipment silhouettes:
 * 6 weapon families x 5 permanent mastery tiers.
 * World-space grip is always (0,0); the upper tip stays aligned with the
 * Warden's existing WeaponRig and hit timing. No painted rectangle or WebView.
 */
public final class EvolvedWeaponArt {
    private EvolvedWeaponArt(){}
    private static final Paint P=new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final int[] METAL={0xffd9e8e8,0xffe8d7b4,0xffffae62,
            0xffaea7fc,0xffe8c16d,0xffb4f4f6};
    private static final int[] SHADE={0xff4b7387,0xff687888,0xffa94e37,
            0xff555a96,0xff746244,0xff477e91};
    private static final int[] RUNE={0xffb3faff,0xffffe4a0,0xffffc270,
            0xffd6bbff,0xffffdd8c,0xffd7ffff};

    private static void fill(int color){
        P.setShader(null);P.setStyle(Paint.Style.FILL);P.setColor(color);
    }
    private static void line(Canvas c,float ax,float ay,float bx,float by,
                             int color,float width){
        fill(color);P.setStyle(Paint.Style.STROKE);P.setStrokeCap(Paint.Cap.ROUND);
        P.setStrokeWidth(width);c.drawLine(ax,ay,bx,by,P);
        P.setStyle(Paint.Style.FILL);
    }
    private static void oval(Canvas c,float x,float y,float rx,float ry,int color){
        fill(color);c.drawOval(x-rx,y-ry,x+rx,y+ry,P);
    }
    private static void rect(Canvas c,float left,float top,float right,float bottom,int color){
        fill(color);c.drawRect(left,top,right,bottom,P);
    }
    private static void poly(Canvas c,int color,float... coords){
        Path p=new Path();p.moveTo(coords[0],coords[1]);
        for(int i=2;i<coords.length;i+=2)p.lineTo(coords[i],coords[i+1]);
        p.close();fill(color);c.drawPath(p,P);
    }
    private static void arc(Canvas c,float x1,float y1,float x2,float y2,
                            float controlX,float controlY,int color,float width){
        Path p=new Path();p.moveTo(x1,y1);p.quadTo(controlX,controlY,x2,y2);
        fill(color);P.setStyle(Paint.Style.STROKE);P.setStrokeWidth(width);
        P.setStrokeCap(Paint.Cap.ROUND);c.drawPath(p,P);P.setStyle(Paint.Style.FILL);
    }
    public static void draw(Canvas c,int weapon,int tier,float x,float y,
                            float scale,float time){
        if(weapon<0||weapon>=6||tier<0||tier>=5||scale<=0)
            throw new IllegalArgumentException("weapon form");
        c.save();c.translate(x,y);c.scale(scale,scale);
        int metal=METAL[weapon],shade=SHADE[weapon],rune=RUNE[weapon];
        switch(weapon){
            case 0:case 4:bow(c,weapon,tier,metal,shade,rune,time);break;
            case 1:case 2:sword(c,weapon,tier,metal,shade,rune,time);break;
            default:staff(c,weapon,tier,metal,shade,rune,time);break;
        }
        // Real silhouette details participate in the very same scaled and
        // rotated in-game weapon layer, not in a disconnected menu graphic.
        WeaponSilhouettePolish.draw(c,weapon,tier,time);
        c.restore();
    }
    private static void grip(Canvas c,int tier,int shade,int rune){
        // Distinct handle winding and finished pommel apply to every variant.
        rect(c,-5,-6,5,28,0xff614a39);
        line(c,0,-8,0,24,0xffc69c65,2);
        for(int i=0;i<3+tier;i++){
            float yy=-3+i*4.4f;
            line(c,-5,yy,5,yy+3,shade,2);
        }
        oval(c,0,29,5+tier*.45f,6,rune);
    }
    private static void sword(Canvas c,int weapon,int tier,int metal,
                              int shade,int rune,float time){
        grip(c,tier,shade,rune);
        // All blades keep the original weapon hit-tip axis; each body differs.
        float w=6+tier*1.9f;
        float tip=-65f;
        if(tier==0){ // utilitarian sword, minimal straight edges
            poly(c,metal,-w,-15,-5,-49,0,tip,5,-49,w,-15);
        }else if(tier==1){ // heavy reinforced greatblade, blunt shoulders
            poly(c,shade,-w-4,-13,-w-6,-30,-5,-51,0,tip,6,-48,
                    w+7,-30,w+4,-13);
            poly(c,metal,-w,-14,-w-1,-31,0,tip,3,-50,5,-15);
            for(int i=0;i<3;i++)
                line(c,-9,-24-i*10,8,-24-i*10,0x669ee4e2,1.2f);
        }else if(tier==2){ // angled knight flamberge
            poly(c,metal,-w,-14,-w-4,-25,-w+2,-38,-w-5,-50,
                    -2,-58,0,tip,5,-53,w+5,-43,w-2,-31,w+5,-21,w,-14);
            poly(c,shade,0,tip,w+5,-43,w-2,-31,w,-14,2,-14);
        }else if(tier==3){ // bifurcated rune blade
            poly(c,metal,-w-2,-14,-w-3,-37,-11,-56,-3,-46,
                    0,-64,3,-46,11,-56,w+3,-37,w+2,-14);
            poly(c,shade,0,-64,w+3,-37,4,-17,0,-16);
            for(int i=0;i<4;i++)oval(c,0,-24-i*9,2.5f,3,rune);
        }else{ // final sovereign blade: winged dual edge silhouette
            poly(c,shade,-w-8,-14,-w-10,-26,-w-4,-33,-w-8,-51,
                    -5,-55,0,tip,5,-55,w+8,-51,w+4,-33,w+10,-26,w+8,-14);
            poly(c,metal,-w-5,-15,-w-2,-34,-5,-50,0,tip,5,-50,w+2,-34,w+5,-15);
            poly(c,rune,-2,-27,-6,-43,0,-50,6,-43,2,-27);
            for(int k=0;k<2;k++){
                float xx=k==0?-1:1;
                poly(c,rune,xx*13,-21,xx*22,-32,xx*19,-12);
            }
        }
        // All crossguards evolve into materially different shapes.
        if(tier==0)line(c,-16,-13,16,-13,0xffb69f73,7);
        if(tier==1){
            line(c,-23,-11,23,-11,0xffa0bbc2,8);
            poly(c,shade,-22,-11,-28,-23,-13,-10);
            poly(c,shade,22,-11,28,-23,13,-10);
        }
        if(tier==2){
            arc(c,-30,-21,0,-8,0,-24,0xffdfd1ae,6);
            arc(c,0,-8,30,-21,0,-24,0xffdfd1ae,6);
        }
        if(tier==3){
            for(int side:new int[]{-1,1}){
                poly(c,shade,side*13,-9,side*31,-21,side*29,-2);
                oval(c,side*16,-10,5,5,rune);
            }
        }
        if(tier==4){
            for(int side:new int[]{-1,1}){
                poly(c,metal,side*9,-10,side*36,-29,
                        side*32,-6,side*18,0);
                oval(c,side*30,-18,4.5f,5.5f,rune);
            }
        }
        oval(c,0,-11,4+tier*.5f,5,rune);
        if(weapon==2&&tier>=2){
            // Ember Sabre develops a forked flame spine rather than a
            // steel recolor; no bitmap allocation or geometry overflow.
            poly(c,0xffffc788,-3,-38,-13,-53,-3,-51,3,-57,6,-36);
        }
        if(tier>=3)
            oval(c,0,-42,10+(float)Math.sin(time*4)*2,13,0x335cebe9);
    }
    private static void bow(Canvas c,int weapon,int tier,int metal,
                            int shade,int rune,float time){
        grip(c,tier,shade,rune);
        rect(c,-6,-28,6,-7,0xff815e43);
        float width=24+tier*3f;
        // Five silhouettes: recurve / longbow / double-limbed / arc wings /
        // crowned compound. Repeater has a mechanical reload housing.
        if(tier==0){
            arc(c,-width,-22,0,-34,-21,-48,metal,6);
            arc(c,0,-34,width,-22,21,-48,metal,6);
        }else if(tier==1){
            arc(c,-width,-15,0,-38,-width+2,-48,metal,7);
            arc(c,0,-38,width,-15,width-2,-48,metal,7);
            poly(c,shade,-width-7,-18,-width-3,-28,-width+4,-22);
            poly(c,shade,width+7,-18,width+3,-28,width-4,-22);
        }else if(tier==2){
            for(int side:new int[]{-1,1}){
                poly(c,shade,0,-28,side*14,-49,side*(width+9),-35,
                        side*width,-14,side*15,-30);
                line(c,side*width,-19,side*6,-10,rune,2);
            }
        }else if(tier==3){
            for(int side:new int[]{-1,1}){
                poly(c,shade,side*6,-27,side*26,-52,side*(width+11),-35,
                        side*(width+3),-17,side*10,-28);
                poly(c,metal,side*18,-36,side*34,-53,side*(width+3),-31);
                oval(c,side*27,-39,4,6,rune);
            }
        }else{
            for(int side:new int[]{-1,1}){
                poly(c,metal,side*7,-31,side*22,-54,
                        side*(width+16),-42,side*(width+3),-16,
                        side*12,-18);
                poly(c,shade,side*19,-36,side*30,-46,
                        side*(width+10),-40,side*27,-28);
                poly(c,rune,side*(width+8),-43,
                        side*(width+20),-54,side*(width+12),-29);
                oval(c,side*25,-37,5,6,rune);
            }
        }
        line(c,-width,-20,0,-11,0xfff7e8bf,1.5f);
        line(c,width,-20,0,-11,0xfff7e8bf,1.5f);
        line(c,0,-11,0,-50,metal,2.6f);
        poly(c,rune,0,-59,-5,-48,5,-48);
        if(weapon==4){ // mechanical siege/repeater, not the same bow
            rect(c,-12,-17,12,13,0xff4d4c48);
            rect(c,-9,-12,9,8,0xffbc9b5f);
            for(int i=0;i<Math.min(4,1+tier);i++){
                float xx=-7+i*4.6f;
                rect(c,xx,-25,xx+2,-8,rune);
            }
            if(tier>=3){
                rect(c,-17,-5,-11,24,metal);rect(c,11,-5,17,24,metal);
            }
        }
        oval(c,0,-29,6+tier,6+tier,shade);
        oval(c,0,-29,3+tier*.45f,3+tier*.45f,rune);
        if(tier>=3){
            float shimmer=(float)Math.sin(time*4)*2;
            oval(c,0,-41,6+shimmer,9,0x447fffff);
        }
    }
    private static void staff(Canvas c,int weapon,int tier,int metal,
                              int shade,int rune,float time){
        grip(c,tier,shade,rune);
        line(c,-3,21,4,-40,shade,12);
        line(c,-1,20,4,-40,metal,5);
        // Head shape varies from capped staff, fork, scepter,
        // floating prisms, to triple-point trident.
        if(tier==0){
            poly(c,metal,-13,-43,0,-56,13,-43,0,-29);
        }else if(tier==1){
            poly(c,shade,-18,-35,-17,-50,0,-59,18,-50,18,-35);
            poly(c,rune,-9,-45,0,-64,9,-45,0,-34);
        }else if(tier==2){
            for(int side:new int[]{-1,1}){
                poly(c,metal,side*7,-32,side*23,-49,
                        side*17,-65,side*10,-49);
            }
            poly(c,rune,0,-65,15,-44,0,-25,-15,-44);
        }else if(tier==3){
            for(int i=0;i<3;i++){
                double angle=i*2*Math.PI/3+time*.55;
                float x=(float)Math.cos(angle)*19,y=-48+(float)Math.sin(angle)*12;
                poly(c,metal,x-8,y+6,x,y-13,x+8,y+6);
            }
            poly(c,rune,-10,-45,0,-65,10,-45,0,-31);
            arc(c,-28,-43,28,-43,0,-84,metal,2.4f);
        }else{
            for(int side:new int[]{-1,0,1}){
                float xx=side*20;
                poly(c,shade,xx-9,-38,xx-12,-54,xx,-65,
                        xx+12,-54,xx+9,-38);
                poly(c,rune,xx-5,-48,xx,-64,xx+5,-48);
                if(side!=0)
                    arc(c,xx,-47,0,-29,xx*.4f,-23,metal,3);
            }
            oval(c,0,-32,15,7,rune);
        }
        for(int i=0;i<tier+1;i++)
            line(c,-7,-6+i*6,8,-4+i*6,rune,2.1f);
        if(weapon==5){ // Ice lance, actual forward thrust head
            poly(c,0xffc9ffff,-9,-47,0,-84,9,-47,0,-56);
            if(tier>=2){
                for(int side:new int[]{-1,1}){
                    poly(c,rune,side*6,-39,side*28,-52,
                            side*15,-27);
                }
            }
        }else if(tier>=2){
            oval(c,0,-54,11+(float)Math.sin(time*3)*2,11,0x559fc8ff);
        }
    }
}
