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
            float forward=WeaponAnimation.wristForward(weaponId,attack);
            float recoil=WeaponAnimation.recoil(weaponId,attack);
            switch(weaponId){
                case 0: // Crossbow: two-hand cross-grip and firm shoulder recoil.
                    handX=34-recoil;handY=-67+recoil*.37f;angle=-75+recoil*.55f;
                    break;
                case 1: // GREAT SWORD: deep windup, shoulder follow-through.
                    angle=-132+250*t;
                    handX=20+23*t+forward;
                    handY=-91+(float)Math.sin(Math.PI*t)*35;
                    break;
                case 2: // SABRE: two fast wrist-led cutting beats.
                    angle=-90+175*t+(float)Math.sin(2*Math.PI*t)*24;
                    handX=37+forward+11*t;
                    handY=-67+(float)Math.sin(2*Math.PI*t)*15;
                    break;
                case 3: // ARC STAFF: raise and channel, then wand cast.
                    angle=-30+26*t;
                    handX=29+forward;handY=-65-14*(float)Math.sin(Math.PI*t);
                    break;
                case 4: // REPEATER: tight three-shot automatic recoil cycle.
                    angle=-75+(float)Math.sin(t*Math.PI*6)*4;
                    handX=35-recoil;handY=-66+(float)Math.abs(Math.sin(t*Math.PI*3))*4;
                    break;
                case 5: // FROST LANCE: long linear thrust from rear shoulder.
                    angle=-20+13*t;
                    handX=22+forward;handY=-59-forward*.20f;
                    break;
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
        boolean mirrored=Math.cos(facing)<-.28f;
        float localX=(float)Math.cos(facing)*(mirrored?-1f:1f);
        float localY=(float)Math.sin(facing);
        float targetAngle=(float)Math.toDegrees(Math.atan2(localX,-localY));
        if(weaponId==1||weaponId==2) {
            // The middle of the swing must point toward the selected enemy.
            // Previous swipes always faced sideways even when the target was above
            // or below the hero. A locked attack arc uses the same target heading
            // as its damage cone.
            float aimOffset=targetAngle+6f;
            return new Pose(pose.shoulderX,pose.shoulderY,pose.elbowX,pose.elbowY,
                    pose.handX,pose.handY,pose.angle+aimOffset);
        }
        float angle=targetAngle;
        // The muzzle and in-hand model use exactly the same target pose.
        // Repeater, crossbow, staff and lance have different recoil travel.
        if(attack>=0){
            float recoil=WeaponAnimation.recoil(weaponId,attack);
            angle+=weaponId==4?recoil*.18f:weaponId==5?recoil*.35f:
                    weaponId==3?recoil*.55f:recoil*.70f;
        }
        return new Pose(pose.shoulderX,pose.shoulderY,pose.elbowX,pose.elbowY,
                pose.handX,pose.handY,angle);
    }

    // Tip coordinates of the transparent, held weapon geometry in WorldArt.
    // This is the same artwork drawn by MotionArt for each combat class.
    // In-world weapons are deliberately smaller than Armory inventory cards.
    // A Warden is ~140 world units tall; the old equipment dwarfed their body.
    private static final float[] WEAPON_SCALE={.58f,.70f,.68f,.68f,.58f,.67f};
    private static final float[] TIP_LENGTHS={41f,65f,65f,68f,41f,84f};
    public static float renderScale(int weaponId){
        if(weaponId<0||weaponId>=WEAPON_SCALE.length)
            throw new IllegalArgumentException("weapon");
        return WEAPON_SCALE[weaponId];
    }
    public static float tipLength(int weaponId){
        return TIP_LENGTHS[weaponId]*renderScale(weaponId);
    }
    public static Point tip(int weaponId,Pose pose) {
        float length=tipLength(weaponId);
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
