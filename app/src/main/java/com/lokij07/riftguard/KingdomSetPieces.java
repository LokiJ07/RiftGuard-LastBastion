package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import java.util.Random;

/**
 * Actual native battle-world architecture (not world-map thumbnails).
 * Stage-seeded setpieces are deliberately outside tower pads, enemy road,
 * and hero interaction space. Each of the ten kingdoms has a unique physical
 * visual motif, plus A/B/C mission changes. No new assets or WebView.
 */
public final class KingdomSetPieces {
    private KingdomSetPieces(){}
    private static final Paint P=new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final int[] STONE={0xff788b75,0xff466f59,0xff81916b,0xff744337,
            0xff8db5c5,0xff7a888e,0xff6f59a4,0xff7a9ba3,0xff625679,0xffa2a180};
    private static final int[] LIGHT={0xffb7eea4,0xff84eabf,0xffcbef91,0xffff9951,
            0xffe8ffff,0xffeed5ac,0xffc7a5ff,0xffb8ecff,0xffce93f0,0xffffdf99};
    private static void color(int c){P.setShader(null);P.setStyle(Paint.Style.FILL);P.setColor(c);}
    private static void rect(Canvas c,float x,float y,float x2,float y2,int col){
        color(col);c.drawRect(x,y,x2,y2,P);
    }
    private static void oval(Canvas c,float x,float y,float rx,float ry,int col){
        color(col);c.drawOval(x-rx,y-ry,x+rx,y+ry,P);
    }
    private static void line(Canvas c,float x,float y,float xx,float yy,int col,float width){
        color(col);P.setStyle(Paint.Style.STROKE);P.setStrokeWidth(width);
        P.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x,y,xx,yy,P);
        P.setStyle(Paint.Style.FILL);
    }
    private static void poly(Canvas c,int col,float...xy){
        Path path=new Path();path.moveTo(xy[0],xy[1]);
        for(int i=2;i<xy.length;i+=2)path.lineTo(xy[i],xy[i+1]);
        path.close();color(col);c.drawPath(path,P);
    }
    private static int translucent(int col,int alpha){
        return (Math.max(0,Math.min(255,alpha))<<24)|(col&0x00ffffff);
    }
    private static void motif(Canvas c,int world,int variant,float x,float y,float size,
                              int seed){
        c.save();c.translate(x,y);c.scale(size,size);
        int stone=STONE[world],light=LIGHT[world];
        oval(c,5,12,74,19,0x440a201e);
        switch(world){
            case 0: // Ancient Greenhaven gate, cracked timber supports and awning.
                rect(c,-54,-100,-42,6,0xff705039);
                rect(c,42,-100,54,6,0xff705039);
                poly(c,0xff436c54,-65,-108,0,-161,65,-108);
                poly(c,0xff7b9c7c,-65,-108,0,-161,0,-109);
                line(c,-38,-84,35,-84,0xffd4b783,8);
                rect(c,-10,-84,10,-58,0xffb5c28b);
                break;
            case 1: // Hollow giant tree arch, glowing runes, rooted trunks.
                line(c,-49,2,-39,-122,0xff64462f,18);
                line(c,49,2,39,-122,0xff64462f,18);
                poly(c,0xff365b43,-61,-122,-28,-154,29,-154,63,-122);
                for(int i=0;i<5;i++){
                    float xx=(i-2)*21;
                    oval(c,xx,-129-Math.abs(i-2)*6,24,19,0xff41855d);
                    oval(c,xx,-126,4,4,translucent(light,145));
                }
                break;
            case 2: // Swamp stilt house / shrine.
                for(int i=-1;i<=1;i++)rect(c,i*42-4,-48,i*42+4,7,0xff513c32);
                rect(c,-66,-81,66,-38,0xff715b3e);
                poly(c,0xff60764b,-83,-80,0,-141,83,-80);
                for(int i=-2;i<=2;i++)line(c,i*22,-71,i*22,-44,0xffa6ac7e,2);
                oval(c,0,10,86,12,translucent(light,27));
                break;
            case 3: // Ember forge with heat vents.
                rect(c,-64,-80,64,9,0xff543c34);
                poly(c,0xff2b272b,-85,-80,0,-147,87,-80);
                rect(c,-14,-40,14,8,0xffff9a44);
                for(int i=-1;i<=1;i++)
                    poly(c,0xffffce70,i*34-6,-24,i*34+3,-61,i*34+12,-23);
                break;
            case 4: // Frostpeak iced temple, blue layered arches.
                for(int i=-1;i<=1;i++){
                    rect(c,i*43-18,-100,i*43+18,4,0xff9eafbe);
                    poly(c,0xffe5faff,i*43-24,-100,i*43,-160,i*43+24,-100);
                }
                rect(c,-66,-43,66,-25,0xffe8f2eb);
                break;
            case 5: // Ironhold industrial watch platform with riveted conduits.
                rect(c,-73,-108,72,10,0xff5f6e72);
                rect(c,-73,-111,72,-100,0xffbec7c2);
                for(int i=0;i<7;i++){
                    float xx=-58+i*19;
                    oval(c,xx,-103,3,3,0xffedcb8e);
                    line(c,xx,-97,xx,-9,0x997c8a8f,2);
                }
                rect(c,-27,-141,27,-110,0xff394950);
                line(c,0,-141,0,-171,0xffffd68f,7);
                break;
            case 6: // Amethyst prism gate.
                poly(c,0xff483b75,-57,7,-41,-112,-9,-153,15,-104,38,6);
                poly(c,0xffc18cfc,-41,-112,-9,-153,15,-104,5,-33);
                poly(c,0xff725bc3,5,-33,15,-104,38,6);
                oval(c,3,-63,17,38,0x999b66e9);
                break;
            case 7: // Tempest wind towers and suspended copper blades.
                rect(c,-54,-99,-26,7,0xff6b8794);
                rect(c,26,-126,54,7,0xff56778b);
                for(int i=-1;i<=1;i++){
                    float yy=-113+i*34;
                    line(c,-65,yy,61,yy-13,0xffe1ddc1,4);
                }
                poly(c,0xffc4eeed,-26,-112,-36,-142,-14,-129);
                break;
            case 8: // Shadowlands ossuary, no ghost collision.
                for(int i=-2;i<=2;i++){
                    float xx=i*27;
                    poly(c,0xff45435e,xx-12,7,xx-9,-89,xx,-106,xx+11,-88,xx+14,7);
                    oval(c,xx,-94,8,7,0xff8f86a9);
                }
                break;
            default: // Eclipse obelisk with visible frame and halo.
                poly(c,0xff545253,-57,3,-43,-116,0,-155,44,-116,57,3);
                poly(c,0xffe3d9a9,-12,-77,0,-117,12,-77,0,-51);
                for(int i=0;i<4;i++){
                    float xx=(i-2)*29;
                    rect(c,xx-3,-18,xx+3,5,0xffc7a973);
                }
        }
        if(variant==1){ // bridge/supply route versions get a visible trail marker
            line(c,-70,14,72,14,0xffd8c48d,5);
        }else if(variant==2){ // last stands carry fortified palisades
            for(int i=-3;i<=3;i++)
                poly(c,0xff6f5c46,i*19-6,7,i*19, -13,i*19+6,7);
        }
        c.restore();
    }
    /** Static cached architecture, before road/pads so the gameplay stays clear. */
    public static void paint(Canvas c,int stage){
        Campaign.checkStage(stage);
        int realm=Campaign.world(stage),mission=Campaign.map(stage);
        Random r=new Random(((long)Campaign.seed(stage)<<21)^0x1a3c07fL);
        for(int i=0;i<3;i++){
            float x=i==0?450+r.nextInt(150):i==1?1130+r.nextInt(140):1630+r.nextInt(110);
            float y=(i%2==0?260:850)+(r.nextInt(40)-20);
            if(Math.abs(y-LaneRules.stagePathY(stage,x))<140)
                y=y<550?180:860;
            // Pad clearances are checked against the same authoritative map.
            boolean safe=true;
            for(int p=0;p<TowerPadLayout.count(stage);p++){
                float dx=x-TowerPadLayout.x(stage,p),dy=y-TowerPadLayout.y(stage,p);
                if(dx*dx+dy*dy<180*180){safe=false;break;}
            }
            if(safe)motif(c,realm,mission,x,y,.70f+(i%2)*.17f,Campaign.seed(stage));
        }
    }
    /** Ambient architecture glows animate *in world-space*, behind entities. */
    public static void animate(Canvas c,int stage,float time,boolean reducedMotion){
        Campaign.checkStage(stage);
        int world=Campaign.world(stage);
        float t=reducedMotion?0:Math.max(0,Float.isFinite(time)?time:0);
        int hue=LIGHT[world];
        for(int i=0;i<5;i++){
            float x=310+i*315+(Campaign.seed(stage)%39);
            float y=(i%2==0?212:867);
            if(Math.abs(y-LaneRules.stagePathY(stage,x))<100)continue;
            float pulse=2.2f+(float)Math.sin(t*(world==3?3.5f:1.8f)+i)*1.3f;
            oval(c,x,y,pulse*2.5f,pulse*2f,translucent(hue,world==3?90:57));
            oval(c,x,y,pulse*.5f,pulse*.5f,translucent(hue,180));
        }
    }
}