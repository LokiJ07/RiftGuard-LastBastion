package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/** Saved ascension branches visibly transform armor and skill casting.
 * No alternate save flags; both are attached to the actual Warden/skills.
 */
public final class EvolutionEffects {
    private EvolutionEffects(){}
    private static final Paint P=new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final int[] COLORS={0xffaae9ef,0xfffaab72,0xffb7e5fa,
            0xff9fe4b3,0xffe9c79a,0xffffc89a};
    private static void fill(int col){P.setShader(null);P.setStyle(Paint.Style.FILL);P.setColor(col);}
    private static void oval(Canvas c,float x,float y,float rx,float ry,int col){
        fill(col);c.drawOval(x-rx,y-ry,x+rx,y+ry,P);
    }
    private static void line(Canvas c,float x,float y,float ex,float ey,int col,float w){
        fill(col);P.setStyle(Paint.Style.STROKE);P.setStrokeWidth(w);
        P.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x,y,ex,ey,P);
        P.setStyle(Paint.Style.FILL);
    }
    private static void poly(Canvas c,int col,float... xy){
        Path p=new Path();p.moveTo(xy[0],xy[1]);
        for(int i=2;i<xy.length;i+=2)p.lineTo(xy[i],xy[i+1]);
        p.close();fill(col);c.drawPath(p,P);
    }
    public static void armor(Canvas c,float x,float y,int form,float time){
        if(form<0||form>4)throw new IllegalArgumentException("armor form");
        if(form==0)return;
        c.save();c.translate(x,y);
        // Plated pauldrons and chest sigil are fixed to physical body.
        for(int side:new int[]{-1,1}){
            float px=side*19;
            poly(c,form>=3?0xffc0d8e5:0xff849faf,
                    px-side*4,-76,px+side*9,-88,
                    px+side*14,-72,px+side*11,-57,px-side*4,-61);
            if(form>=2)
                poly(c,0xffd6b67c,px+side*2,-80,px+side*16,-92,
                        px+side*22,-72,px+side*7,-68);
            if(form>=3){
                poly(c,0xff8dd1de,px+side*7,-84,
                        px+side*23,-103,px+side*15,-75);
            }
        }
        poly(c,0xff384f66,-19,-57,0,-68,19,-57,14,-27,0,-21,-14,-27);
        poly(c,form>=3?0xffb7d7dd:0xff83abba,-13,-54,0,-60,13,-54,
                10,-31,0,-25,-10,-31);
        if(form>=2){
            float flutter=(float)Math.sin(time*3)*4;
            poly(c,0xff305e75,-16,-44,-26,-20,-17+flutter,12,
                    -5,5,0,-24);
            poly(c,0xff947554,16,-44,26,-20,17-flutter,12,5,5,0,-24);
        }
        if(form>=3){
            // Closed guardian helmet with attached crest and visor.
            poly(c,0xff526c82,-16,-98,-13,-113,0,-123,13,-113,
                    16,-98,11,-82,-10,-82);
            poly(c,0xffb1cbd0,-13,-103,0,-116,13,-103,8,-92,-8,-92);
            line(c,-10,-95,10,-95,0xffe1d4af,3);
        }
        if(form==4){
            poly(c,0xff9fe9f2,-15,-121,-10,-141,0,-127,10,-141,
                    15,-121,0,-113);
            oval(c,0,-128,5,6,0xfff5e3ac);
            for(int i=0;i<4;i++){
                float xx=(i-1.5f)*12;
                oval(c,xx,-67,2.8f,3.2f,0xffbafaff);
            }
        }
        c.restore();
    }
    /** SPEED branch: greaves and motion studs attached to the Warden feet. */
    public static void greaves(Canvas c,float x,float y,int tier){
        if(tier<0||tier>4)throw new IllegalArgumentException("speed");
        if(tier==0)return;
        for(int side:new int[]{-1,1}){
            float px=x+side*10;
            poly(c,tier>=3?0xffb2eff0:0xff6b9ba6,
                    px-9,y-17,px+9,y-17,px+12,y+6,px-11,y+6);
            poly(c,0xff213f53,px-11,y+1,px+12,y+1,px+17,y+13,px-13,y+13);
            if(tier>=2)oval(c,px,y-11,4,5,0xffe3cf9e);
            if(tier>=3)
                poly(c,0xff9ce8eb,px-6,y+5,px+4,y-12,
                        px+10,y+4);
            if(tier==4)
                for(int i=0;i<2;i++)
                    oval(c,px+(i==0?-9:9),y-4,2,4,0xffd4fbf4);
        }
    }
    /** CORE and GOLD ascension nodes materially modify the real keep. */
    public static void fortressBranches(Canvas c,float cx,float cy,
                                        int core,int gold){
        if(core<0||core>4||gold<0||gold>4)
            throw new IllegalArgumentException("fortress branch");
        if(core>0){
            // Reinforced stones, gatehouse plates and shielded joints.
            for(int i=0;i<core;i++){
                float xx=cx-114+i*76;
                poly(c,0xff738d96,xx-23,cy+67,xx-30,cy-91,
                        xx+30,cy-91,xx+23,cy+67);
                for(int k=0;k<4;k++)
                    line(c,xx-21,cy-58+k*32,xx+21,cy-58+k*32,
                            0xffd0d6c9,2);
            }
            if(core>=3)
                for(int side:new int[]{-1,1}){
                    float x=cx+side*136;
                    poly(c,0xffbfd8d2,x-21,cy-103,
                            x,cy-137,x+21,cy-103,x+13,cy+76,x-13,cy+76);
                }
            if(core==4){
                poly(c,0xffb4e9e4,cx-28,cy+100,cx,cy+77,
                        cx+28,cy+100,cx,cy+117);
                oval(c,cx,cy+98,9,11,0xffe9ecce);
            }
        }
        if(gold>0){
            // Supply/scout flags and treasury-safe gilded roof battlements.
            for(int i=0;i<gold;i++){
                float xx=cx-111+i*73;
                line(c,xx,cy-102,xx,cy-152,0xffd8bd88,4);
                poly(c,0xffc69b5c,xx+2,cy-149,xx+33,cy-137,
                        xx+2,cy-124);
                oval(c,xx+2,cy-152,3.5f,4.2f,0xffffe6ae);
            }
            if(gold>=3){
                for(int i=0;i<5;i++){
                    float xx=cx-93+i*47;
                    poly(c,0xffedc984,xx-7,cy-19,xx,cy-27,
                            xx+7,cy-19,xx,cy-11);
                }
            }
        }
    }
    /** Special spell visuals inherit all purchased SKILL ascension nodes. */
    public static void cast(Canvas c,int skill,int form,float x,float y,
                            float time,float progress){
        if(skill<0||skill>=COLORS.length||form<0||form>4)
            throw new IllegalArgumentException("skill form");
        if(form==0)return;
        int col=COLORS[skill];
        float intensity=(float)Math.sin(Math.PI*Math.max(0,Math.min(1,progress)));
        for(int i=0;i<3+form*2;i++){
            double a=i*Math.PI*2/(3+form*2)+time*(form>=3?1.4:.6);
            float r=25+form*9+intensity*17;
            float px=x+(float)Math.cos(a)*r;
            float py=y-50+(float)Math.sin(a)*r*.66f;
            if(skill==WardenSkills.FROST||skill==WardenSkills.TRAP){
                poly(c,col,px-4,py,px,py-12-form*2,px+4,py,px,py+6);
            }else if(skill==WardenSkills.EMBER||skill==WardenSkills.METEOR){
                poly(c,col,px-6,py+5,px,py-8-form*2,px+6,py+5);
            }else if(skill==WardenSkills.BANNER){
                oval(c,px,py,5+form,2,0x99a5f2bd);
            }else{
                line(c,px-7,py+4,px+7,py-7,0xffb4f4ff,2.5f);
                oval(c,px,py,3.6f,4.5f,col);
            }
        }
        if(form>=3){
            float rr=30+form*12+intensity*18;
            fill(0x887de6e6);P.setStyle(Paint.Style.STROKE);
            P.setStrokeWidth(2f+intensity*2f);
            c.drawOval(x-rr,y-50-rr*.43f,x+rr,y-50+rr*.43f,P);
            P.setStyle(Paint.Style.FILL);
        }
    }
    public static void badge(Canvas c,float x,float y,int form,int skill){
        if(form<0||form>4||skill<0||skill>=6)
            throw new IllegalArgumentException("badge");
        if(form==0)return;
        int col=COLORS[skill];
        for(int n=0;n<Math.min(form,4);n++){
            float a=(float)(n*Math.PI/2);
            float xx=x+(float)Math.cos(a)*39,yy=y+(float)Math.sin(a)*39;
            poly(c,col,xx-5,yy,xx,yy-8,xx+5,yy,xx,yy+8);
        }
    }
}
