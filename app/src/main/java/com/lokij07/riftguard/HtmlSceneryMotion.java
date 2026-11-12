package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Faithful native-Java Canvas adaptation of the ambient motion in the user's
 * "RiftGuard World and Environment.html" reference.
 *
 * Draw order: cached sky -> live moving clouds/light shafts -> cached distant
 * mountains -> cached cliff/castle -> live waterfall sparkle -> cached trees.
 * These effects are scenery ONLY; they never modify enemy movement, player
 * collision, castle health, input rectangles or HUD placement.
 *
 * No WebView, network, per-frame bitmaps, or timer threads.
 */
public final class HtmlSceneryMotion {
    private HtmlSceneryMotion(){}
    private static final Paint P=new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final int[] CLOUD={
            0xffd9effa,0xffb5d4bd,0xffbbd7ab,0xffffb386,0xffe1f5ff,
            0xffded3be,0xffd8e4ff,0xffd0dff3,0xffc7a4df,0xffe9a3db
    };
    private static final int[] FALL={
            0xffe8ffff,0xffbdf8e9,0xffcef8bf,0xffffa34e,0xfff6ffff,
            0xfff6bf9c,0xffb7f8ff,0xffc7f5ff,0xffe0c1ff,0xffff9ad2
    };

    private static int alpha(int color,int opacity){
        return (Math.max(0,Math.min(255,opacity))<<24)|(color&0x00ffffff);
    }
    private static float finiteTime(float time){
        return Float.isFinite(time)?Math.max(0,time):0;
    }
    private static void oval(Canvas c,float l,float t,float r,float b,
                             int color){
        P.setShader(null);P.setStyle(Paint.Style.FILL);P.setColor(color);
        c.drawOval(l,t,r,b,P);
    }
    private static void stroke(Canvas c,float x,float y,float xx,float yy,
                               int color,float width){
        P.setShader(null);P.setColor(color);P.setStyle(Paint.Style.STROKE);
        P.setStrokeWidth(width);P.setStrokeCap(Paint.Cap.ROUND);
        c.drawLine(x,y,xx,yy,P);P.setStyle(Paint.Style.FILL);
    }
    /** HTML sunshafts and multi-lobe drifting cloud silhouettes behind ridges. */
    public static void drawSky(Canvas c,int stage,float time,boolean reduced){
        Campaign.checkStage(stage);
        time=reduced?0:finiteTime(time);
        int biome=Campaign.world(stage),seed=Campaign.seed(stage);
        float sunX=450+LivingBattlefield.noise(seed,2)*1070;
        float sunY=56+LivingBattlefield.noise(seed,3)*46;
        int shine=FALL[biome];
        c.save();
        c.clipRect(0,0,1900,420);
        // Subtle beams: six slow changing volumes from the actual sky light.
        for(int i=0;i<6;i++){
            float a=1.06f+i*.23f;
            float flicker=(float)Math.sin(time*.20f+i*1.9f)*.5f+.5f;
            float ax=sunX+(float)Math.cos(a)*1350;
            float bx=sunX+(float)Math.cos(a+.13f)*1350;
            float ay=sunY+(float)Math.sin(a)*1350;
            float by=sunY+(float)Math.sin(a+.13f)*1350;
            Path ray=new Path();
            ray.moveTo(sunX,sunY);
            ray.lineTo(ax,ay);
            ray.lineTo(bx,by);
            ray.close();
            P.setColor(alpha(shine,5+(int)(flicker*7)));
            P.setStyle(Paint.Style.FILL);
            c.drawPath(ray,P);
        }
        // The source HTML's 7 multi-lobe clouds, moved at different speeds.
        for(int i=0;i<7;i++){
            float x0=-160+LivingBattlefield.noise(seed,710+i)*2200;
            float speed=5f+LivingBattlefield.noise(seed,750+i)*8f;
            float x=(x0+time*speed+210)%2310-210;
            float y=49+LivingBattlefield.noise(seed,780+i)*145;
            float size=.60f+LivingBattlefield.noise(seed,820+i)*.80f;
            int cloud=alpha(CLOUD[biome],biome==3||biome==9?26:41);
            oval(c,x-82*size,y-14*size,x+82*size,y+17*size,cloud);
            oval(c,x-28*size,y-29*size,x+55*size,y+12*size,cloud);
            oval(c,x-105*size,y-2*size,x-5*size,y+20*size,cloud);
            oval(c,x+32*size,y-5*size,x+116*size,y+21*size,cloud);
        }
        c.restore();
    }

    /**
     * The waterfall/lavafall from the HTML castle cliff gains flowing
     * highlights, not an unrelated screen-space overlay. Matches the same
     * seeded cliff coordinates as LivingBattlefield.drawCastle().
     */
    public static void drawWaterfall(Canvas c,int stage,float time,
                                     boolean reduced){
        Campaign.checkStage(stage);
        time=reduced?0:finiteTime(time);
        int world=Campaign.world(stage),seed=Campaign.seed(stage);
        float cx=915+(LivingBattlefield.noise(seed,75)-.5f)*420;
        float cliffTop=262+LivingBattlefield.noise(seed,76)*30;
        float x=cx-132,top=cliffTop+43;
        int color=FALL[world];
        c.save();
        c.clipRect(x-22,top,x+22,492);
        for(int i=0;i<7;i++){
            float xx=x-12+i*4;
            float phase=LivingBattlefield.noise(seed,905+i)*120;
            float scroll=(time*(70+i*8)+phase)%64;
            int glow=world==3?115:world==9?136:89;
            for(float yy=top+scroll-64;yy<491;yy+=64){
                float y1=Math.max(yy,top);
                float y2=Math.min(yy+27+(i%3)*9,491);
                if(y2>y1){
                    stroke(c,xx,y1,xx+1.5f,y2,
                            alpha(color,glow+(i%3)*16),1.3f+(i%2)*1.2f);
                }
            }
        }
        c.restore();
        // Spray belongs to the cliff foot, not the active battle road.
        float bubble=reduced?0:(float)Math.sin(time*6)*3;
        for(int i=0;i<5;i++){
            float xx=x+(i-2)*13;
            oval(c,xx-10,486+bubble*.1f,xx+10,490+bubble*.1f,
                    alpha(color,world==3?70:95));
        }
    }
}
