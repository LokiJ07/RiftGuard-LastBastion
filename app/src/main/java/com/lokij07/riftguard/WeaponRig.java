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
    /** A coordinate in the hero's world-space local frame, measured from the feet. */
    public static final class Point {
        public final float x,y;
        private Point(float x,float y){this.x=x;this.y=y;}
    }
    private static float clamp(float value){return Math.max(0,Math.min(1,value));}
    private static float ease(float t){return t*t*(3-2*t);}
    /** Weapon ID chooses a genuine combat grip. Sprite pivots track the glove. */
    public static Pose pose(int weaponId,float attack,float cast,float time){
        return pose(weaponId,attack,cast,time,false);
    }
    public static Pose pose(int weaponId,float attack,float cast,float time,
                            boolean moving){
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
        if(attack<0&&cast<0) {
            if(moving) {
                // Counter-rotate the weapon arm with each foot plant.
                handX+=(float)Math.sin(time*11.7f+Math.PI)*9f;
                handY+=(float)Math.cos(time*11.7f)*5f;
                angle+=(float)Math.sin(time*11.7f)*7f;
            } else handY+=(float)Math.sin(time*3)*1.7f;
        }
        float elbowX=sx+(handX-sx)*.49f+8;
        float elbowY=sy+(handY-sy)*.45f+4;
        return new Pose(sx,sy,elbowX,elbowY,handX,handY,angle);
    }

    /**
     * Ranged and magic weapons follow the actual target angle. The same pose is
     * used for drawing the held sprite and locating the spawn point of a shot.
     * The canvas mirrors the hero whenever facing left, so aim in local space.
     */
    public static Pose aimedPose(int weaponId,float attack,float cast,
                                 float time,boolean moving,float facing) {
        Pose pose=pose(weaponId,attack,cast,time,moving);
        if(weaponId==1||weaponId==2)return pose; // Real blade swing is independent.
        boolean mirrored=Math.cos(facing)<-.28f;
        float localX=(float)Math.cos(facing)*(mirrored?-1f:1f);
        float localY=(float)Math.sin(facing);
        float angle=(float)Math.toDegrees(Math.atan2(localX,-localY));
        // Keep the short recoil, but rotate the drawn muzzle toward its target.
        if(attack>=0)angle+=(float)Math.sin(Math.PI*clamp(attack))*9f;
        return new Pose(pose.shoulderX,pose.shoulderY,pose.elbowX,pose.elbowY,
                pose.handX,pose.handY,angle);
    }

    // Tip coordinates of the transparent, held weapon geometry in WorldArt.
    // This is the same artwork drawn by MotionArt for each combat class.
    private static final float[] TIP_LENGTHS={41f,65f,65f,68f,41f,84f};
    public static Point tip(int weaponId,Pose pose) {
        if(weaponId<0||weaponId>=TIP_LENGTHS.length)
            throw new IllegalArgumentException("weapon");
        float length=TIP_LENGTHS[weaponId];
        double rad=Math.toRadians(pose.angle);
        return new Point(pose.handX+(float)Math.sin(rad)*length,
                pose.handY-(float)Math.cos(rad)*length);
    }

    /** Actual world-space muzzle relative to the hero; supports left/right aim. */
    public static Point muzzle(int weaponId,float attack,float cast,
                               float time,boolean moving,float facing) {
        Point local=tip(weaponId,aimedPose(weaponId,attack,cast,time,moving,facing));
        return Math.cos(facing)<-.28f?new Point(-local.x,local.y):local;
    }
}
