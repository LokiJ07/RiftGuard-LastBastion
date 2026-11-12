package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Rank-driven architectural silhouette pass. Every tier adds structural
 * mass, not just a floating VFX badge. Draw after base fort and before guns.
 * Cosmetic only: actual collision, enemy routes and pad geometry are unchanged.
 */
public final class FortressSilhouetteArt {
    private FortressSilhouetteArt(){}
    private static final Paint P=new Paint(Paint.ANTI_ALIAS_FLAG);
    private static void fill(int color){P.setColor(color);P.setStyle(Paint.Style.FILL);}
    private static void rect(Canvas c,float x1,float y1,float x2,float y2,int color){
        fill(color);c.drawRect(x1,y1,x2,y2,P);
    }
    private static void disk(Canvas c,float x,float y,float rx,float ry,int color){
        fill(color);c.drawOval(x-rx,y-ry,x+rx,y+ry,P);
    }
    private static void line(Canvas c,float x,float y,float xx,float yy,int color,float thick){
        fill(color);P.setStyle(Paint.Style.STROKE);P.setStrokeWidth(thick);
        P.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x,y,xx,yy,P);
        P.setStyle(Paint.Style.FILL);
    }
    private static void poly(Canvas c,int color,float...pts){
        Path path=new Path();path.moveTo(pts[0],pts[1]);
        for(int i=2;i<pts.length;i+=2)path.lineTo(pts[i],pts[i+1]);
        path.close();fill(color);c.drawPath(path,P);
    }
    /** Dedicated shapes are deliberately rank-specific, not color swaps.
     * Weapon mounts appear in a separate FortressArmament silhouette layer.
     */
    public static void draw(Canvas c,float x,float y,int rank,float time){
        if(rank<1||rank>5)throw new IllegalArgumentException("fortress rank");
        c.save();c.translate(x,y);
        switch(rank){
            case 1:
                // Starter outpost: horizontal timber palisade, watch scaffolds,
                // rough hewn entrance with hanging canvas banner.
                disk(c,0,134,172,17,0x55112b25);
                for(int side:new int[]{-1,1}){
                    float xx=side*139;
                    rect(c,xx-23,-38,xx+23,110,0xff715038);
                    for(int j=0;j<5;j++){
                        float px=xx-21+j*11;
                        poly(c,0xffb48b55,px,17,px+5,3,px+11,17,px+11,110,px,110);
                        line(c,px,25,px,103,0x66503822,2);
                    }
                    rect(c,xx-37,-44,xx+37,-27,0xff51483b);
                    line(c,xx-37,-27,xx+37,-27,0xffc2a675,4);
                }
                for(int j=0;j<12;j++){
                    float xx=-146+j*26;
                    rect(c,xx,101,xx+19,133,0xff916638);
                    poly(c,0xffc09a5e,xx,101,xx+9,87,xx+19,101);
                }
                rect(c,-50,105,50,134,0xff503b2d);
                for(int k=0;k<7;k++)rect(c,-45+k*14,107,-37+k*14,129,0xff765036);
                break;
            case 2:
                // Knight garrison: octagonal gate towers, stone footings and
                // layered gate portcullis, visibly replacing timber frontage.
                disk(c,0,138,187,19,0x660b1d1c);
                for(int side:new int[]{-1,1}){
                    float bx=side*139;
                    poly(c,0xff657e83,bx-44,110,bx-43,-47,
                            bx-23,-75,bx+23,-75,bx+43,-47,bx+44,110);
                    poly(c,0xffc9cec3,bx-47,-75,bx,-111,bx+47,-75,bx,-60);
                    for(int j=0;j<4;j++){
                        float px=bx-34+j*23;
                        rect(c,px,-83,px+16,-65,0xffd8d5c8);
                        rect(c,px,-13,px+3,93,0x88738376);
                    }
                    rect(c,bx-13,-28,bx+13,12,0xff263e4c);
                    disk(c,bx,-11,7,10,0xffc6a87a);
                }
                rect(c,-120,96,120,145,0xff788888);
                for(int i=0;i<15;i++)line(c,-115+i*16,97,-115+i*16,140,
                        0xff485c5f,2);
                rect(c,-62,103,62,145,0xff514737);
                for(int i=0;i<9;i++)line(c,-53+i*13,104,-53+i*13,145,
                        0xffbf9d73,3);
                break;
            case 3:
                // Runic stronghold: tall central keep, visible elevated
                // platforms and tiered stone buttresses.
                for(int side:new int[]{-1,1}){
                    float bx=side*154;
                    poly(c,0xff4b6573,bx-37,103,bx-43,-91,
                            bx-11,-112,bx+38,-91,bx+37,103);
                    poly(c,0xffafc7c6,bx-45,-99,bx,-151,bx+45,-99,
                            bx,-89);
                    rect(c,bx-15,-65,bx+15,-8,0xff253c4a);
                    line(c,bx-30,54,bx+29,54,0xffd7d5bf,6);
                }
                rect(c,-84,-155,84,-78,0xff7b8f94);
                rect(c,-77,-149,77,-136,0xffbdc8bc);
                for(int k=0;k<9;k++)
                    rect(c,-77+k*18,-169,-67+k*18,-144,0xffd1d5c5);
                rect(c,-42,-134,42,-87,0xff395b63);
                disk(c,0,-113,18,22,0xff9dd4ce);
                for(int side:new int[]{-1,1}){
                    float bx=side*109;
                    poly(c,0xff91a7a3,bx-12,107,bx-29,-49,
                            bx-5,-64,bx+19,105);
                    for(int i=0;i<4;i++)line(c,bx-11,13+i*23,bx+12,13+i*23,
                            0xffc5c9b6,3);
                }
                rect(c,-118,108,118,148,0xff8c9a96);
                for(int k=0;k<13;k++)rect(c,-117+k*19,102,-106+k*19,122,
                        0xffb7c6bc);
                break;
            case 4:
                // Royal bastion: wide buttressed curtain, tall paired
                // gunhouses, portcullis and support arches.
                for(int side:new int[]{-1,1}){
                    float bx=side*185;
                    poly(c,0xff4d6679,bx-53,118,bx-61,-113,
                            bx-24,-156,bx+26,-156,bx+59,-113,bx+54,118);
                    poly(c,0xffd7d7c7,bx-65,-144,bx,-196,bx+65,-144);
                    rect(c,bx-46,-153,bx+46,-136,0xffb9c5bf);
                    for(int i=0;i<5;i++){
                        float xx=bx-40+i*19;
                        rect(c,xx,-169,xx+13,-147,0xffe1e3d2);
                    }
                    rect(c,bx-19,-110,bx+19,-65,0xff243b51);
                    disk(c,bx,-88,11,10,0xffd1b280);
                    poly(c,0xffa4b9b4,bx-19,118,bx-26,41,bx+24,41,bx+19,118);
                }
                rect(c,-146,96,146,160,0xff778d99);
                rect(c,-115,113,115,153,0xffbdc9c6);
                for(int i=0;i<11;i++)rect(c,-140+i*27,92,-124+i*27,111,
                        0xffdae0d6);
                poly(c,0xff2c4256,-69,159,-62,102,-42,83,42,83,62,102,69,159);
                for(int k=-3;k<=3;k++)line(c,k*14,109,k*14,157,0xffd3bb89,4);
                break;
            default:
                // Arc citadel: visibly taller central spire, four guarded
                // reactor pylons with a connected shield crown.
                for(int side:new int[]{-1,1}){
                    float bx=side*193;
                    poly(c,0xff5d8095,bx-52,126,bx-57,-147,
                            bx,-214,bx+57,-147,bx+52,126);
                    poly(c,0xffb2edf2,bx-59,-167,bx,-237,bx+59,-167);
                    poly(c,0xff62b4c7,bx,-237,bx+59,-167,bx+38,-145);
                    for(int i=0;i<4;i++)line(c,bx-36,-117+i*45,bx+36,-117+i*45,
                            0xffc9e6e2,4);
                    rect(c,bx-21,-90,bx+21,-32,0xff173952);
                    disk(c,bx,-62,13,20,0xff80dce7);
                    line(c,bx,-225,bx,-264,0xffd7e9da,7);
                    poly(c,0xffa9f5ef,bx-18,-262,bx,-293,bx+18,-262,bx,-249);
                }
                rect(c,-158,-179,158,-125,0xff668ca0);
                for(int i=0;i<16;i++)rect(c,-151+i*20,-195,-138+i*20,-174,
                        0xffa2d7da);
                poly(c,0xffaacbd4,-73,-181,-54,-229,0,-254,54,-229,73,-181);
                poly(c,0xff64d8e1,0,-254,54,-229,73,-181,0,-198);
                disk(c,0,-218,17,22,0xffe2ffff);
                rect(c,-155,99,155,165,0xff6b97a3);
                for(int i=0;i<12;i++){
                    float bx=-142+i*26;
                    poly(c,0xffc4e3dc,bx-10,113,bx,89,bx+10,113);
                }
                poly(c,0xff1f4856,-76,164,-63,108,-43,87,43,87,63,108,76,164);
                line(c,-83,149,83,149,0xffa8f4ea,7);
                break;
        }
        c.restore();
    }
}
