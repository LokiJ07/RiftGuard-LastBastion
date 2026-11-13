package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * v0.60 articulated actor pass, based on the supplied WardenRig/EnemyRig design.
 * Animation and the actual combat simulation use the same WeaponRig attack clock.
 * No bitmaps are allocated during draw, and the existing save-backed weapon form
 * is painted into the live character hand, not a disconnected menu sprite.
 */
public final class LivingRigArt {
    private static final int OUT=0xff121824, STEEL=0xff697a91, LIGHT=0xffc8dded,
            GOLD=0xffe8bf74, CAPE=0xff244fa4;
    private static final int[] SKIN={0xff75aa73,0xffc4aa7c,0xff9096b0,
            0xffc77552,0xff9fc8d6,0xff9da0a3,0xffa990d0,0xff78adb8,
            0xff77778d,0xffc5a17b};
    private static final int[] ACCENT={0xffb9dc84,0xffd6efa5,0xffadcb83,
            0xffffa568,0xffb5e8f5,0xffd8c7ac,0xffd4b8ff,0xff9fe9f7,
            0xffb594e2,0xffffe2ab};
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();

    public static boolean validForm(int weapon,int tier) {
        return weapon>=0&&weapon<6&&tier>=0&&tier<5;
    }
    public static int biomeColor(int biome) {
        return SKIN[Math.floorMod(biome,SKIN.length)];
    }
    private static float sin(float n){return (float)Math.sin(n);}
    private static float clamp(float n){return Math.max(0f,Math.min(1f,n));}
    private void fill(int color){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);p.setAlpha(255);}
    private void oval(Canvas c,float x,float y,float rx,float ry,int color){
        fill(color);c.drawOval(x-rx,y-ry,x+rx,y+ry,p);
    }
    private void stroke(Canvas c,float x1,float y1,float x2,float y2,float w,int color){
        fill(color);p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(w);c.drawLine(x1,y1,x2,y2,p);p.setStyle(Paint.Style.FILL);
    }
    private void poly(Canvas c,int col,float... xy){
        path.reset();path.moveTo(xy[0],xy[1]);
        for(int k=2;k<xy.length;k+=2)path.lineTo(xy[k],xy[k+1]);
        path.close();fill(col);c.drawPath(path,p);
    }
    private void limb(Canvas c,float sx,float sy,float hx,float hy,float bend,float thick,
                      int dark,int main) {
        float mx=(sx+hx)*.5f+bend,my=(sy+hy)*.5f;
        stroke(c,sx,sy,mx,my,thick+3,dark);
        stroke(c,mx,my,hx,hy,thick+2,dark);
        stroke(c,sx,sy,mx,my,thick,main);
        stroke(c,mx,my,hx,hy,thick,main);
        oval(c,mx,my,thick*.52f,thick*.52f,GOLD);
    }
    /**
     * Feet are at (x,y); the body is drawn from separately articulated pieces.
     * Upgrade appearance comes from the OWNED mastery tier passed by RiftGameView.
     * Returns false for invalid state so the sprite fallback remains available.
     */
    public boolean hero(Canvas c,float x,float y,float t,float facing,boolean running,
                        float attack,float cast,boolean hurt,int weapon,int form,float death){
        if(!Float.isFinite(t)||!Float.isFinite(x)||!Float.isFinite(y)
                ||!Float.isFinite(facing)||!validForm(weapon,form))return false;
        boolean left=Math.cos(facing)<-.28f;
        float phase=t*11.7f, gait=running?sin(phase):0f;
        float bob=running?-3f*Math.abs(gait):sin(t*2.2f)*.8f;
        float drop=death>=0?clamp(death)*23f:0f;
        float attackShift=WeaponAnimation.torsoShiftX(weapon,attack);
        float slash=attack>=0?sin(clamp(attack)*(float)Math.PI):0f;
        WeaponRig.Pose grip=WeaponRig.aimedPose(weapon,attack,cast,t,running,facing);
        c.save();c.translate(x,y);
        if(left)c.scale(-1,1);
        // Separate planted legs with knee joints and counter-phase foot lift.
        oval(c,0,1,30,7,0x6609141b);
        for(int side=-1;side<=1;side+=2){
            float step=running?sin(phase+(side==1?(float)Math.PI:0f)):0f;
            float footX=side*9+step*12,footY=-Math.max(0,step)*7f;
            float hipX=side*8+attackShift*.25f,hipY=-55+bob*.4f+drop;
            limb(c,hipX,hipY,(hipX+footX)*.5f,footY-25,
                    side*5f,10,OUT,STEEL);
            oval(c,footX+2,footY,10,4,0xff344459);
            stroke(c,footX-8,footY,footX+10,footY,2,LIGHT);
        }
        // Cloth moves independently from torso and legs.
        float capeWave=sin(t*3.0f)*3f-(running?gait*7f:0f)-attackShift*.5f;
        poly(c,0xff142e70,-14,-88+bob,-26+capeWave,-69+bob,
                -30+capeWave*1.4f,5+drop,-7+capeWave*.4f,-15+drop,
                14,-77+bob);
        poly(c,CAPE,-14,-85+bob,-20+capeWave,-53+bob,
                -22+capeWave*1.4f,-4+drop,-4+capeWave*.5f,-16+drop,
                8,-78+bob);
        // Layered cuirass, waist cloth, engraved plates and head.
        float torsoX=attackShift*.64f;
        c.save();c.translate(torsoX,bob+drop);
        c.rotate(running?gait*2.3f:sin(t*2.0f)*.6f,0,-65);
        poly(c,OUT,-21,-87,19,-88,25,-50,17,-28,-17,-28,-25,-53);
        poly(c,STEEL,-17,-84,17,-84,19,-54,12,-38,-12,-38,-20,-54);
        poly(c,LIGHT,-13,-80,0,-76,11,-80,9,-54,0,-47,-10,-54);
        poly(c,0xff425476,-13,-38,12,-38,18,-14,-17,-14);
        stroke(c,-13,-48,13,-48,4,GOLD);
        oval(c,0,-59,6,8,0xff577ea8);
        oval(c,0,-59,3,4,0xffcef6ff);
        for(int s=-1;s<=1;s+=2){
            stroke(c,s*9,-35,s*7,-20,3,GOLD);
            oval(c,s*19,-82,11,12,OUT);
            oval(c,s*19,-83,9,10,STEEL);
            oval(c,s*19,-87,5,4,LIGHT);
        }
        oval(c,0,-105,16,18,OUT);
        oval(c,0,-106,13,15,0xffc4a18d);
        poly(c,0xff3d5069,-16,-105,-14,-123,0,-128,14,-123,16,-105);
        poly(c,LIGHT,-13,-117,0,-125,12,-116,0,-112);
        stroke(c,3,-105,12,-106,2,0xff1b303f);
        // Shield arm with an articulated elbow; cast uses its own arc.
        float shieldY=cast>=0?-61-sin(clamp(cast)*(float)Math.PI)*33:-55;
        limb(c,-16,-82,-30+gait*4,shieldY,4,9,OUT,STEEL);
        oval(c,-32+gait*4,shieldY,12,16,OUT);
        oval(c,-32+gait*4,shieldY,9,13,0xff385a86);
        oval(c,-32+gait*4,shieldY,4,6,GOLD);
        c.restore();
        // Real weapon pose: same WeaponRig as combat hit arc / projectile muzzle.
        limb(c,grip.shoulderX,grip.shoulderY+bob+drop,grip.handX,
                grip.handY+bob+drop,2,10,OUT,STEEL);
        c.save();c.translate(grip.handX,grip.handY+bob+drop);
        c.rotate(grip.angle);
        float scale=WeaponRig.renderScale(weapon);
        c.scale(scale,scale);
        EvolvedWeaponArt.draw(c,weapon,form,0,0,1f,t);
        c.restore();
        oval(c,grip.handX,grip.handY+bob+drop,6,6,LIGHT);
        if(slash>0&&(weapon==1||weapon==2)) {
            fill(0x88cbeeff);p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(3+slash*6);
            c.drawArc(-56,-133,85,23,-80,120*slash,false,p);
        }
        if(hurt)oval(c,0,-56,31,43,0x33ff7777);
        c.restore();return true;
    }

    /**
     * Seven mechanically distinct silhouettes, with biome palette and stable
     * per-enemy variation. Uses the real attack timer, never an independent hit.
     */
    public boolean enemy(Canvas c,int kind,int biome,int variant,float x,float y,
                         float phase,boolean moving,float attack,boolean hurt,float faceX){
        if(kind<0||kind>6||!Float.isFinite(x)||!Float.isFinite(y)
                ||!Float.isFinite(phase))return false;
        final int base=biomeColor(biome),glow=ACCENT[Math.floorMod(biome,10)];
        float size=(kind==5?1.65f:kind==2?1.33f:kind==1?.85f:1f)
                *(.94f+Math.floorMod(variant,4)*.045f);
        float stride=moving?sin(phase*1.25f):0f;
        float striking=attack>=0?sin(clamp(attack)*(float)Math.PI):0f;
        c.save();c.translate(x,y);if(faceX<0)c.scale(-1,1);
        oval(c,0,2,25*size,5,0x7709131b);
        c.scale(size,size);
        if(kind==6){ // Tusks and four independent legs; not a humanoid sprite.
            for(int s=-1;s<=1;s+=2){
                stroke(c,s*16,-12,s*21+stride*6,4,9,OUT);
                stroke(c,s*12,-12,s*17-stride*6,4,7,0xff6a4a39);
            }
            oval(c,-1,-25,33,23,OUT);oval(c,0,-26,30,21,0xff78614e);
            oval(c,22,-31,18,15,base);
            poly(c,glow,27,-25,47,-34,39,-13,28,-13);
            poly(c,LIGHT,17,-22,39,-11,31,-37);
            oval(c,35,-29,3,3,0xffffe39a);
        }else if(kind==3){ // Imp: wing flap, hovering, clawed feet.
            float flap=12f*sin(phase*2f);
            poly(c,OUT,-4,-45,-30,-83+flap,-28,-29,-7,-39);
            poly(c,glow,-6,-42,-24,-76+flap,-21,-37);
            poly(c,OUT,5,-46,36,-83-flap,28,-28,7,-39);
            poly(c,glow,7,-43,29,-76-flap,23,-38);
            oval(c,0,-50+sin(phase)*4f,18,24,OUT);
            oval(c,0,-51+sin(phase)*4f,15,22,base);
            poly(c,glow,-11,-67,-7,-85,0,-69,8,-86,13,-64);
            oval(c,-7,-54,3,3,0xfffff1a8);oval(c,8,-54,3,3,0xfffff1a8);
            stroke(c,11,-39,25+striking*10,-21,5,base);
        }else{
            float width=kind==5?31:kind==2?25:kind==4?18:16;
            float shoulders=kind==5?30:kind==2?24:20;
            float bodyY=kind==5?-46:-42;
            // Animated planted legs and two moving arms with elbow joints.
            for(int s=-1;s<=1;s+=2)
                limb(c,s*10,bodyY+3,s*12+s*stride*11,1,-s*4,9,OUT,
                        kind==5?0xff534459:0xff394c59);
            poly(c,OUT,-width,bodyY-34,width,bodyY-34,width+5,bodyY+5,
                    0,bodyY+17,-width-5,bodyY+5);
            poly(c,base,-width+4,bodyY-29,width-4,bodyY-29,
                    width,bodyY+4,0,bodyY+13,-width,bodyY+4);
            poly(c,glow,-8,bodyY-25,8,bodyY-25,6,bodyY+1,-6,bodyY+1);
            oval(c,0,bodyY-48,kind==5?20:16,kind==5?22:17,OUT);
            oval(c,0,bodyY-49,kind==5?18:14,kind==5?19:15,base);
            // Species identities: blade, sling, club, wand, huge beast.
            if(kind==0){
                poly(c,glow,-17,bodyY-52,-22,bodyY-72,-10,bodyY-57);
                poly(c,glow,17,bodyY-52,22,bodyY-72,10,bodyY-57);
            }else if(kind==2||kind==5){
                poly(c,0xffc8d3d7,-18,bodyY-62,-26,bodyY-91,-7,bodyY-64);
                poly(c,0xffc8d3d7,18,bodyY-62,26,bodyY-91,7,bodyY-64);
            }else if(kind==4){
                poly(c,glow,-17,bodyY-64,0,bodyY-84,17,bodyY-64);
                oval(c,0,bodyY-84,4,5,0xfff3f6c0);
            }
            limb(c,-shoulders,bodyY-26,-31-stride*3,bodyY-3,5,kind==5?12:8,OUT,base);
            float fx=kind==1?30:shoulders+10+striking*15;
            float fy=kind==4?bodyY-44-striking*21:bodyY-4-striking*19;
            limb(c,shoulders,bodyY-26,fx,fy,-4,kind==5?13:8,OUT,base);
            if(kind==0)stroke(c,fx,fy,fx+19+striking*12,fy-17,4,0xffd8e8e9);
            if(kind==1){oval(c,fx+5,fy-10,8,4,0xff644c39);oval(c,fx+12,fy-15,4,4,glow);}
            if(kind==2||kind==5){
                stroke(c,fx,fy,fx+24,fy-37+striking*12,9,0xff654939);
                oval(c,fx+25,fy-39+striking*12,12,15,OUT);
                oval(c,fx+24,fy-40+striking*12,9,12,0xff8b8d95);
            }
            if(kind==4){
                stroke(c,fx,fy,fx+9,fy-44,5,0xff5b4c40);
                oval(c,fx+9,fy-44,9+striking*4,10+striking*4,glow);
            }
            oval(c,-6,bodyY-52,3,3,0xffffdfa6);
            oval(c,7,bodyY-52,3,3,0xffffdfa6);
        }
        if(hurt)oval(c,0,-38,25,35,0x33ff8b82);
        c.restore();return true;
    }
}
