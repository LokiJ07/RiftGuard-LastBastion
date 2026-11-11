package com.lokij07.riftguard;

/** Animation-state wrist sockets (not a baked, static weapon sticker). */
public final class WeaponRig {
    private WeaponRig(){}
    public static final class Pose {
        public final float shoulderX,shoulderY,elbowX,elbowY,handX,handY,angle;
        private Pose(float sx,float sy,float ex,float ey,float hx,float hy,float angle){
            shoulderX=sx;shoulderY=sy;elbowX=ex;elbowY=ey;
            handX=hx;handY=hy;this.angle=angle;
        }
    }
    private static float clamp(float value){return Math.max(0,Math.min(1,value));}
    private static float ease(float t){return t*t*(3-2*t);}
    /** Weapon ID chooses a genuine combat grip. Sprite pivots track the glove. */
    public static Pose pose(int weaponId,float attack,float cast,float time){
        if(weaponId<0||weaponId>5)throw new IllegalArgumentException("weapon");
        boolean blade=weaponId==1||weaponId==2;
        boolean staff=weaponId==3||weaponId==5;
        float sx=16,sy=-87;
        float handX,handY,angle;
        if(blade){
            handX=39;handY=-54;angle=14f;
        }else if(staff){
            handX=29;handY=-58;angle=-21;
        }else{
            handX=34;handY=-66;angle=-75;
        }
        if(attack>=0){
            float t=ease(clamp(attack));
            if(blade){
                // A full shoulder-driven swing repositions the wrist, not just the sprite.
                angle=-110+208*t;
                handX=25+(float)Math.sin(Math.PI*t)*22+18*t;
                handY=-76+(float)Math.sin(Math.PI*t)*29;
            }else{
                float recoil=(float)Math.sin(Math.PI*t);
                handX-=12*recoil;
                handY+=5*recoil;
                angle+=blade?0:9*recoil;
            }
        }
        if(cast>=0) {
            float a=(float)Math.sin(Math.PI*clamp(cast));
            handX+=11*a;handY-=15*a;angle+=39*a;
        }
        if(attack<0&&cast<0)handY+=(float)Math.sin(time*3)*1.7f;
        float elbowX=sx+(handX-sx)*.49f+8;
        float elbowY=sy+(handY-sy)*.45f+4;
        return new Pose(sx,sy,elbowX,elbowY,handX,handY,angle);
    }
}
