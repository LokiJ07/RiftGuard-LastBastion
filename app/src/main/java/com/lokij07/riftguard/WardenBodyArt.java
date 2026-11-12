package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;

/**
 * Original resolution-independent Warden body. There is intentionally NO
 * sword, gun, bow, shield or staff in any layer. Weapons remain the separate
 * Wrist/WeaponRig overlay. Independent animated cloth, planted legs, head,
 * chest, back arm and joint shading replace the baked-in weapon torso atlas.
 */
public final class WardenBodyArt {
    private WardenBodyArt() {}
    private static final int DARK=0xff142b39, SHADOW=0xff0b1a29,
            STEEL=0xff8eb0ba, EDGE=0xffd8e3dc, GOLD=0xffd8b777;
    private static final Paint P=new Paint(Paint.ANTI_ALIAS_FLAG|
            Paint.FILTER_BITMAP_FLAG|Paint.DITHER_FLAG);
    private static float sin(float n){return (float)Math.sin(n);}
    private static float clamp(float n){return Math.max(0,Math.min(1,n));}

    private static void poly(Canvas c,int color,float... points){
        Path path=new Path();path.moveTo(points[0],points[1]);
        for(int i=2;i<points.length;i+=2)path.lineTo(points[i],points[i+1]);
        path.close();
        P.setStyle(Paint.Style.FILL);P.setShader(null);P.setColor(color);
        c.drawPath(path,P);
    }
    private static void line(Canvas c,int color,float width,
                             float x1,float y1,float x2,float y2){
        P.setShader(null);P.setColor(color);P.setStyle(Paint.Style.STROKE);
        P.setStrokeCap(Paint.Cap.ROUND);P.setStrokeWidth(width);
        c.drawLine(x1,y1,x2,y2,P);P.setStyle(Paint.Style.FILL);
    }
    private static void fill(Canvas c,float l,float t,float r,float b,
                             float rad,int left,int center,int right){
        P.setStyle(Paint.Style.FILL);
        P.setShader(new LinearGradient(l,t,r,b,new int[]{left,center,right},
                new float[]{0,.48f,1},Shader.TileMode.CLAMP));
        c.drawRoundRect(new RectF(l,t,r,b),rad,rad,P);
        P.setShader(null);
    }
    private static void ellipse(Canvas c,float l,float t,float r,float b,int color){
        P.setShader(null);P.setColor(color);P.setStyle(Paint.Style.FILL);
        c.drawOval(new RectF(l,t,r,b),P);
    }
    /** Geometric metal segment with physically articulated knee/heel. */
    private static void limb(Canvas c,float ax,float ay,float bx,float by,
                             float radius,int mid,int bright){
        float dx=bx-ax,dy=by-ay;
        float angle=(float)Math.toDegrees(Math.atan2(dy,dx))+90;
        float length=(float)Math.hypot(dx,dy);
        c.save();c.translate(ax,ay);c.rotate(angle);
        fill(c,-radius,0,radius,length,5,SHADOW,mid,DARK);
        poly(c,bright,-radius+2,length*.08f,
                -radius*.33f,length*.03f,
                radius*.3f,length*.12f,
                radius-2,length*.60f,
                radius*.3f,length*.82f,
                -radius+4,length*.65f);
        line(c,0xff829ca8,2,-radius+2,6,-radius+2,length*.83f);
        c.restore();
    }
    private static void boot(Canvas c,ActorGait.Leg leg,int side,boolean moving){
        float lift=leg.planted?0:2.5f;
        c.save();c.translate(leg.footX,leg.footY-lift);
        float pitch=moving?(leg.footY<-.7f?-9:4):0;
        c.rotate(pitch);
        fill(c,-16,-20,17,1,5,SHADOW,0xff557b8c,0xff172f42);
        poly(c,0xff96bac6,-16,-9,-8,-15,10,-14,16,-6,12,-3,-12,-3);
        line(c,0xffe0d6b3,2,-16,-1,16,-1);
        c.restore();
    }
    private static void cape(Canvas c,float phase,boolean moving,
                             float attack,float casting){
        float wind=moving?sin(phase)*10:sin(phase*.23f)*3;
        if(attack>=0)wind-=sin(clamp(attack)*(float)Math.PI)*11;
        if(casting>=0)wind+=sin(clamp(casting)*(float)Math.PI)*7;
        Path cloth=new Path();
        cloth.moveTo(-13,-105);
        cloth.cubicTo(-35,-113,-42,-80,-48+wind,-57);
        cloth.cubicTo(-65+wind,-32,-57+wind*1.2f,-8,-64+wind*1.5f,1);
        cloth.cubicTo(-39+wind*.75f,-7,-23+wind*.33f,-18,-13,-39);
        cloth.close();
        P.setColor(0xff472d48);P.setShader(new LinearGradient(-65,-105,10,14,
                new int[]{0xff1b273d,0xff77536e,0xff2c3145},
                new float[]{0,.52f,1},Shader.TileMode.CLAMP));
        c.drawPath(cloth,P);P.setShader(null);
        line(c,0xffac7e7e,2,-29,-94,-37+wind*.3f,-41);
        poly(c,0xffac8a69,-27,-98,-16,-98,-8,-84,-20,-88);
    }
    private static void torso(Canvas c,float phase,boolean running,
                              float attack,float cast){
        float lean=ActorGait.torsoLean(phase,running,attack);
        float castLean=cast>=0?-sin((float)Math.PI*clamp(cast))*3:0;
        c.save();c.rotate(lean+castLean,0,-43);
        // Thick armor, not flat single-fill circles: separate undercoat and
        // metal plate, shoulders, leather straps, scratches, studs and faceting.
        fill(c,-26,-99,29,-39,16,0xff182b3a,0xff516978,0xff0b2232);
        poly(c,0xff9fb9c0,-25,-90,-16,-106,14,-105,28,-91,12,-65,-15,-64);
        poly(c,0xff436779,-15,-101,12,-101,22,-88,6,-76,-12,-81);
        poly(c,0xffb7c8c5,-11,-98,-2,-95,13,-99,4,-93);
        // Chestplate embossed rune is visible at phone gameplay scale.
        poly(c,0xffd3b17a,-6,-88,6,-89,12,-82,2,-67,-9,-79);
        poly(c,0xff274c5e,-4,-85,4,-86,7,-80,1,-75,-6,-81);
        line(c,0xffd6e4df,2,-23,-85,-17,-70);
        line(c,0xff283b46,3,20,-82,13,-65);
        for(int i=0;i<3;i++){
            ellipse(c,-17+i*14,-64,-13+i*14,-59,0xffd9c6a2);
        }
        // Foreground waist buckles and hem animate separately from feet.
        fill(c,-24,-50,26,-31,5,0xff142c38,0xff836d62,0xff213c4a);
        poly(c,0xffcfb788,-8,-50,8,-50,10,-43,-7,-41);
        poly(c,0xff2e6070,-7,-45,8,-44,5,-38,-6,-39);
        float tabard=running?sin(phase+1.1f)*4.5f:sin(phase*.35f)*1.5f;
        poly(c,0xff243f50,-16,-37,17,-37,21+tabard,4,-20+tabard,4);
        poly(c,0xff7e9baa,-12,-31,13,-31,9+tabard,-1,-12+tabard,-2);
        line(c,0xffd6ad73,3,-5,-29,-4+tabard,0);
        line(c,0xffd6ad73,3,5,-29,5+tabard,0);
        // Two separate shoulder plates. Main right weapon arm remains
        // completely owned by WeaponRig; no painted legacy sidearm.
        fill(c,-41,-103,-15,-73,12,DARK,STEEL,0xff294758);
        fill(c,18,-103,43,-75,12,DARK,0xff98b6c4,0xff263e50);
        poly(c,0xffd7ddd3,19,-99,31,-98,36,-91,22,-94);
        line(c,0xffedcb8f,2,-37,-83,-18,-92);
        c.restore();
    }
    private static void head(Canvas c,float phase,boolean running,
                             float attack,float cast){
        float tilt=running?sin(phase+.8f)*2.8f:sin(phase*.27f)*1.0f;
        if(attack>=0)tilt+=sin(clamp(attack)*(float)Math.PI)*3f;
        if(cast>=0)tilt-=sin(clamp(cast)*(float)Math.PI)*3f;
        c.save();c.rotate(tilt,0,-111);
        fill(c,-17,-133,19,-95,13,DARK,0xff718f9d,0xff243747);
        // Masked face: no human identity and no permanently drawn gear.
        fill(c,-14,-118,16,-98,8,0xff192a36,0xff566675,0xff1a3447);
        poly(c,0xffd4c49c,-12,-117,1,-111,16,-117,13,-122,-12,-122);
        poly(c,0xff213e50,-18,-129,-13,-138,13,-138,21,-126,16,-112,
                8,-124,-10,-121);
        poly(c,0xff8299a9,-12,-132,1,-137,12,-133,8,-126,-9,-126);
        poly(c,0xffd9cda5,-4,-132,2,-134,8,-129,1,-125);
        // Eye slit: narrow glint, not an animated floating face.
        line(c,0xffbcebf0,2,-10,-114,-3,-114);
        line(c,0xffbcebf0,2,8,-114,14,-114);
        poly(c,0xff233b49,-7,-101,8,-101,11,-95,-8,-95);
        c.restore();
    }
    private static void backArm(Canvas c,float phase,boolean running,
                                float attack,float cast){
        float swing=running?sin(phase+(float)Math.PI)*7:sin(phase*.23f)*2;
        if(cast>=0)swing-=sin(clamp(cast)*(float)Math.PI)*9;
        float x0=-25,y0=-87,x1=-38+swing*.48f,y1=-64,
                x2=-35+swing,y2=-36-(cast>=0?sin(clamp(cast)*(float)Math.PI)*19:0);
        limb(c,x0,y0,x1,y1,12,0xff607887,0xffb6c6c7);
        ellipse(c,x1-6,y1-7,x1+6,y1+7,0xffd3b67f);
        limb(c,x1,y1,x2,y2,10,0xff516a7c,0xffa1b6bd);
        fill(c,x2-8,y2-8,x2+8,y2+10,6,DARK,0xff9cb3b8,0xff344a5d);
    }
    public static void draw(Canvas canvas,float x,float y,float phase,
                            boolean running,float attack,float cast,
                            boolean hurt,boolean facingLeft){
        if(!Float.isFinite(x)||!Float.isFinite(y)||!Float.isFinite(phase))return;
        canvas.save();
        canvas.translate(x,y);
        if(facingLeft)canvas.scale(-1,1);
        if(hurt)canvas.saveLayerAlpha(-85,-155,90,15,200);
        ellipse(canvas,-39,-5,43,10,0x7709101d);
        // Ground contact and alternating feet are physical, not a whole
        // bitmap translation that makes both feet float in unison.
        cape(canvas,phase,running,attack,cast);
        for(int side:new int[]{-1,1}){
            ActorGait.Leg step=ActorGait.leg(phase,running,side,140);
            limb(canvas,step.hipX,step.hipY,step.kneeX,step.kneeY,
                    13,0xff426174,0xff9db7c0);
            ellipse(canvas,step.kneeX-8,step.kneeY-9,
                    step.kneeX+8,step.kneeY+9,0xffc8c8b8);
            limb(canvas,step.kneeX,step.kneeY,step.footX,step.footY-10,
                    11,0xff344d64,0xff96b4bf);
            boot(canvas,step,side,running);
        }
        backArm(canvas,phase,running,attack,cast);
        torso(canvas,phase,running,attack,cast);
        head(canvas,phase,running,attack,cast);
        if(hurt)canvas.restore();
        canvas.restore();
    }
}
