package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/** Larger physical weapon assemblies for existing, FUNCTIONAL fortress
 * arsenal tiers. The shot origin, damage and unlocks remain in
 * FortressArmamentRules and EvolutionProgress (no decorative fake weapon).
 */
public final class FortressWeaponShowcaseArt {
    private FortressWeaponShowcaseArt(){}
    private static final Paint P=new Paint(Paint.ANTI_ALIAS_FLAG);
    private static void fill(int col){P.setColor(col);P.setShader(null);P.setStyle(Paint.Style.FILL);}
    private static void rect(Canvas c,float l,float t,float r,float b,int color){
        fill(color);c.drawRect(l,t,r,b,P);
    }
    private static void disk(Canvas c,float x,float y,float rx,float ry,int color){
        fill(color);c.drawOval(x-rx,y-ry,x+rx,y+ry,P);
    }
    private static void line(Canvas c,float x,float y,float xx,float yy,int color,float w){
        fill(color);P.setStyle(Paint.Style.STROKE);P.setStrokeWidth(w);
        P.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x,y,xx,yy,P);
        P.setStyle(Paint.Style.FILL);
    }
    private static void poly(Canvas c,int col,float...coords){
        Path q=new Path();q.moveTo(coords[0],coords[1]);
        for(int i=2;i<coords.length;i+=2)q.lineTo(coords[i],coords[i+1]);
        q.close();fill(col);c.drawPath(q,P);
    }
    public static void draw(Canvas c,float x,float y,int arsenal,float time){
        if(arsenal<0||arsenal>4)throw new IllegalArgumentException("arsenal");
        if(arsenal==0)return; // no purchased rooftop arsenal yet
        c.save();c.translate(x,y);
        if(arsenal==1){
            // IRONBOLT BATTERY: huge twin rack-fed crossbows with ratcheted
            // iron arms and bolt magazines physically attached to side nests.
            for(int side:new int[]{-1,1}){
                float bx=side*147;
                poly(c,0xff3e5862,bx-42,-45,bx-48,-68,
                        bx+48,-68,bx+42,-45);
                rect(c,bx-12,-81,bx+12,-30,0xffc9aa6e);
                for(int j=0;j<4;j++)
                    rect(c,bx-10+j*5,-81,bx-7+j*5,-52,0xffe4dfbb);
                line(c,bx-57,-88,bx+57,-88,0xffdbe5df,4);
                poly(c,0xffc6d7d6,bx-61,-91,bx-52,-112,bx-29,-93,bx,-72,
                        bx+29,-93,bx+52,-112,bx+61,-91,bx,-67);
                line(c,bx-52,-92,bx,-67,0xffffe7b2,2);
                line(c,bx+52,-92,bx,-67,0xffffe7b2,2);
                for(int i=0;i<3;i++){
                    disk(c,bx+(i-1)*31,-72,4,4,0xffffcc79);
                }
            }
        }else if(arsenal==2){
            // EMBER MORTAR: built-in double furnace tubes, external pipes,
            // ember charge chambers and muzzle blast channels.
            for(int side:new int[]{-1,1}){
                float bx=side*169;
                poly(c,0xff344351,bx-48,20,bx-44,-60,bx+44,-60,bx+48,20);
                poly(c,0xffa78268,bx-34,-25,bx-23,-105,
                        bx+22,-105,bx+34,-25);
                rect(c,bx-23,-107,bx+23,-78,0xff35434a);
                disk(c,bx,-105,27,10,0xffdfb18a);
                disk(c,bx,-105,18,7,0xffed774a);
                disk(c,bx,-105,10,4,0xffffcb81);
                for(int j=0;j<4;j++){
                    float px=bx-31+j*20;
                    line(c,px,-9,px,-59,0xffb8a392,6);
                    disk(c,px,-34,5,5,0xfff2c188);
                }
                line(c,bx,-25,bx-side*46,-35,0xfff49c64,7);
            }
        }else if(arsenal==3){
            // STORM CONDUCTOR: 3-prong heavy induction cage and charged
            // rotating rings. A bolted central support joins the masonry.
            poly(c,0xff455a69,-44,-102,-44,-210,-23,-225,
                    23,-225,44,-210,44,-102);
            for(int i=-1;i<=1;i++){
                float bx=i*28;
                line(c,bx,-143,bx,-245,0xffb6d5e0,8);
                poly(c,0xffb4ecf2,bx-10,-243,bx,-266,bx+10,-243);
                disk(c,bx,-216,10,13,0xffe4faff);
            }
            for(int k=0;k<3;k++){
                float yy=-182-k*16;
                disk(c,0,yy,52-k*7,9,0xff54788c);
                line(c,-42+k*6,yy,42-k*6,yy,0xffaddce7,4);
            }
            for(int i=0;i<5;i++){
                float xx=(i-2)*19;
                line(c,xx,-130,xx,-102,0xffd6e5de,5);
            }
            float phase=(float)Math.sin(time*3)*6;
            poly(c,0x99c6f8ff,-12,-198,phase,-227,12,-198);
        }else{
            // GUARDIAN PRISM: large crystalline roof weapon visibly linked
            // via six real support struts, active optical chamber and fins.
            poly(c,0xff3a5c6a,-92,-107,-64,-242,-29,-272,
                    29,-272,64,-242,92,-107);
            poly(c,0xff98c7db,-59,-211,0,-313,59,-211,0,-170);
            poly(c,0xffd5f6f5,-59,-211,0,-313,0,-170);
            poly(c,0xff54a6bd,0,-313,59,-211,0,-170);
            for(int side:new int[]{-1,1}){
                float sx=side*86;
                poly(c,0xffa1bcc5,sx-19,-127,sx-24,-214,sx,-256,
                        sx+24,-214,sx+19,-127);
                poly(c,0xffbeecf3,sx-12,-216,sx,-277,sx+12,-216);
                line(c,sx,-184,side*40,-199,0xffe4eff1,8);
                line(c,sx,-148,side*35,-176,0xff769daf,11);
            }
            disk(c,0,-220,19,28,0xffe2fbff);
            disk(c,0,-220,10+(float)Math.sin(time*4)*2,15,0xff8de7f2);
            poly(c,0xffe7f7fc,-25,-256,0,-310,25,-256,0,-231);
        }
        c.restore();
    }
}
