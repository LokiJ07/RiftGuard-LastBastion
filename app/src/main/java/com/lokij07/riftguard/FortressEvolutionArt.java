package com.lokij07.riftguard;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/** Permanent fortress and Warden visuals physically attached to upgrade tiers.
 * This layer is drawn in actual gameplay AND the workshop preview. */
public final class FortressEvolutionArt {
    private FortressEvolutionArt(){}
    private static final Paint P=new Paint(Paint.ANTI_ALIAS_FLAG);
    private static void line(Canvas c,float x,float y,float x2,float y2,int color,float w){
        P.setShader(null);P.setColor(color);P.setStyle(Paint.Style.STROKE);
        P.setStrokeWidth(w);c.drawLine(x,y,x2,y2,P);P.setStyle(Paint.Style.FILL);
    }
    private static void rect(Canvas c,float x,float y,float w,float h,int color){
        P.setShader(null);P.setStyle(Paint.Style.FILL);P.setColor(color);
        c.drawRect(x,y,x+w,y+h,P);
    }
    private static void disc(Canvas c,float x,float y,float r,int color){
        P.setShader(null);P.setStyle(Paint.Style.FILL);P.setColor(color);
        c.drawCircle(x,y,r,P);
    }
    private static void poly(Canvas c,int color,float...v){
        Path p=new Path();p.moveTo(v[0],v[1]);
        for(int i=2;i<v.length;i+=2)p.lineTo(v[i],v[i+1]);
        p.close();P.setShader(null);P.setColor(color);P.setStyle(Paint.Style.FILL);
        c.drawPath(p,P);
    }
    public static void fortress(Canvas c,float x,float y,int rank,int ascensionTowerCount,
                                float time){
        if(rank<1||rank>5||ascensionTowerCount<0)throw new IllegalArgumentException("fortress");
        c.save();c.translate(x,y);
        // Rank changes the actual roofline and side bastion geometry.
        for(int side:new int[]{-1,1}){
            if(rank>=2){
                float px=side*(137+rank*4);
                rect(c,px-23,-107,46,157,rank>=3?0xff8e9fa3:0xff896a4e);
                for(int k=0;k<4;k++)rect(c,px-28+k*15,-123,11,20,
                        rank>=3?0xffd2d4c9:0xffd4ac79);
                rect(c,px-10,-70,20,29,0xff213a4b);
            }
            if(rank>=4){
                float px=side*147;
                line(c,px,-125,px,-168,0xff8da5ae,7);
                poly(c,0xffd8f0f1,px-23,-169,px,-194,px+23,-169);
                disc(c,px,-170,8,0xff80d1e6);
            }
        }
        if(rank>=3){
            // Reinforced layered central gate and combat parapets.
            rect(c,-88,72,176,27,0xff748b90);
            rect(c,-67,68,134,18,0xffb9bcb0);
            for(int n=0;n<7;n++)rect(c,-82+n*26,64,14,13,0xffd5dbcb);
        }
        if(rank>=5){
            poly(c,0xffd2e8ee,-31,-145,-20,-205,0,-232,20,-205,31,-145);
            poly(c,0xff84e7ed,0,-232,20,-205,31,-145,0,-157);
            disc(c,0,-180,11+3*(float)Math.sin(time*2),0xffc1ffff);
        }
        // Ascension tower purchases activate visible new WEAPON MACHINERY,
        // rather than passive colorful icon/rings.
        if(ascensionTowerCount>=1 && rank>=3){
            rect(c,-48,-146,96,23,0xff526b76);
            rect(c,-19,-168,38,39,0xffc4ad7b);
            poly(c,0xffc8f6ec,-14,-164,0,-209,14,-164);
            poly(c,0xff5488ab,0,-209,14,-164,0,-156);
            disc(c,0,-166,9,0xffd9ffff);
        }
        if(ascensionTowerCount>=2 && rank>=5){
            for(int side:new int[]{-1,1}){
                float px=side*90;
                rect(c,px-16,-134,32,46,0xff485c7b);
                poly(c,0xffc3aaff,px-19,-137,px,-169,px+19,-137);
                line(c,px,-165,px,-213,0xffc8b1fa,6);
                disc(c,px,-212,8,0xffeddbff);
            }
        }
        c.restore();
    }
    public static void heroArmor(Canvas c,float x,float y,int form,float time){
        if(form<=0)return;
        c.save();c.translate(x,y);
        int metal=form>=3?0xffe0d4ae:0xff91aeba;
        poly(c,metal,-44,-77,-21,-106,-9,-96,-19,-70,-35,-56);
        poly(c,metal,44,-77,21,-106,9,-96,19,-70,35,-56);
        rect(c,-17,-54,34,12,form>=3?0xffd4b17a:0xff70949e);
        for(int i=0;i<form;i++)
            disc(c,(i-(form-1)*.5f)*9,-49,2.4f,0xfffcf6c7);
        if(form>=2){
            poly(c,0xff536986,-26,-42,-20,5,0,22,20,5,26,-42,0,-27);
            line(c,-12,-36,0,-18,0xffaed0d6,3);
            line(c,12,-36,0,-18,0xffaed0d6,3);
        }
        if(form>=3){
            poly(c,0xffb7dbdd,-17,-107,-6,-126,7,-126,17,-107,0,-112);
            disc(c,0,-111,5,0xffedcba1);
        }
        if(form>=4){
            disc(c,0,-100,13+2*(float)Math.sin(time*3),0x6681e9ee);
            line(c,-30,-40,-24,-4,0xfff4d3a4,4);
            line(c,30,-40,24,-4,0xfff4d3a4,4);
        }
        c.restore();
    }
    public static void skillSigil(Canvas c,float x,float y,int tier,float time){
        if(tier<=0)return;
        P.setShader(null);P.setStyle(Paint.Style.STROKE);
        P.setStrokeWidth(1.3f+tier*.5f);P.setColor(0x7786f6ee);
        float radius=28+tier*6+(float)Math.sin(time*3)*2;
        c.drawCircle(x,y-42,radius,P);P.setStyle(Paint.Style.FILL);
        for(int i=0;i<tier*2;i++){
            float a=(float)(i*Math.PI/tier+time*.3);
            disc(c,x+(float)Math.cos(a)*radius,y-42+(float)Math.sin(a)*radius,
                    2.4f,0xff9beee4);
        }
    }
}
