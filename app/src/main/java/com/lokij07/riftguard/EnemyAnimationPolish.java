package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Combat-timed species equipment and contact poses over the bundled
 * illustrated enemy sprite sheet. These details track the real sprite's
 * foot-position, attack clock, hit-stagger, and boss phase.
 */
public final class EnemyAnimationPolish {
    private EnemyAnimationPolish(){}
    private static final Paint P=new Paint(Paint.ANTI_ALIAS_FLAG);
    private static void fill(int color){
        P.setStyle(Paint.Style.FILL);P.setShader(null);P.setColor(color);
    }
    private static void line(Canvas c,float x,float y,float ex,float ey,int col,float width){
        fill(col);P.setStyle(Paint.Style.STROKE);P.setStrokeWidth(width);
        P.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x,y,ex,ey,P);
        P.setStyle(Paint.Style.FILL);
    }
    private static void oval(Canvas c,float x,float y,float rx,float ry,int col){
        fill(col);c.drawOval(x-rx,y-ry,x+rx,y+ry,P);
    }
    private static void shape(Canvas c,int col,float...xy){
        Path path=new Path();path.moveTo(xy[0],xy[1]);
        for(int i=2;i<xy.length;i+=2)path.lineTo(xy[i],xy[i+1]);
        path.close();fill(col);c.drawPath(path,P);
    }
    /** Only the real attacker receives a connected weapon anticipation/swing. */
    public static void draw(Canvas c,int kind,float x,float y,float time,
                            float attack,float hit,boolean boss,int phase) {
        if(kind<0||kind>6)return;
        float strike=attack>=0?(float)Math.sin(Math.PI*Math.min(1,attack)):0;
        float gait=(float)Math.sin(time*(kind==2?5:kind==3?13:10));
        float recoil=hit>0?(float)Math.sin(Math.PI*Math.min(1,hit/.21f))*9:0;
        c.save();c.translate(x,y);
        float reach=19+strike*22;
        switch(kind){
            case 0: // Marauder: axe in physical hand socket.
                line(c,27,-35,30+reach*.42f,-68+strike*13,0xff674a37,6);
                shape(c,0xffc0d2d3,22+reach*.4f,-73+strike*13,
                      43+reach*.4f,-82+strike*13,
                      49+reach*.4f,-61+strike*13,
                      30+reach*.4f,-57+strike*13);
                break;
            case 1: // Swift stalker: paired knives, reverse grip on strike.
                line(c,20,-31,23+reach*.3f,-48+strike*8,0xffe3d7ae,3);
                line(c,-16,-38,-27-strike*9,-49+gait*3,0xffadcecb,4);
                break;
            case 2: // Brute: huge forward club, weight shifts with windup.
                line(c,34,-49,40+reach*.7f,-90+strike*25,0xff584536,10);
                oval(c,40+reach*.7f,-95+strike*25,12,21,0xff889b98);
                for(int k=-1;k<=1;k++)
                    line(c,35+reach*.7f+k*6,-105+strike*25,
                         45+reach*.7f+k*6,-90+strike*25,0xffbbc0aa,2);
                break;
            case 3: // Spectral flyer: articulated purple wing-tips.
                for(int dir:new int[]{-1,1}){
                    float flap=17*(float)Math.sin(time*8+dir*.2);
                    line(c,dir*22,-53,dir*54,-81-flap,0x889c8ce6,4);
                    line(c,dir*54,-81-flap,dir*70,-49-flap,0x66c4aeed,3);
                }
                break;
            case 4: // Bog shaman: staff and real casting crystal.
                line(c,29,-14,33,-98,0xff725c42,6);
                oval(c,33,-102,8+strike*5,12+strike*6,0xff77dabb);
                if(strike>0)oval(c,33,-102,16*strike,16*strike,0x557affc4);
                break;
            case 5: // Warlord: shield, horned mace, phase runes.
                oval(c,-39,-43,23,30,0xff556a79);
                oval(c,-39,-43,16,20,0xff9badad);
                line(c,26,-39,34+reach*.65f,-102+strike*19,0xff71604a,8);
                oval(c,34+reach*.65f,-106+strike*19,13,14,0xffc1b6a1);
                break;
            default: // Charger: coordinated tusks and accelerating hoof arcs.
                for(int dir:new int[]{-1,1}){
                    shape(c,0xffdec9ad,dir*17,-36,dir*31,-59,dir*34,-38);
                    oval(c,dir*20,-3+gait*4,7,3,0x55694f40);
                }
        }
        if(boss){
            int glow=phase>=3?0xffe9a9fc:phase==2?0xfffac984:0xffa9dcff;
            float energy=2+(float)Math.sin(time*(phase+2))*2;
            for(int k=0;k<3;k++){
                float px=(k-1)*25;
                oval(c,px,-117-energy,3+phase,5+energy,glow);
            }
            // Boss's physical chest crest is phase-specific, not a HUD-only
            // detached status ring. It visibly glows during real movement.
            shape(c,glow,-9,-66,0,-79-phase*2,9,-66,0,-54);
        }
        if(recoil>0){
            line(c,-20,-27,-20-recoil,-27-recoil*.3f,0xfff7dfd1,3);
            line(c,20,-27,20+recoil,-27-recoil*.3f,0xfff7dfd1,3);
        }
        c.restore();
    }
}
