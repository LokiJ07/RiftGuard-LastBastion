package com.lokij07.riftguard;

/** Bone-space leg poses with alternating planted steps, no whole-sprite bouncing. */
public final class ActorGait {
    private ActorGait(){}
    public static final class Leg {
        public final float hipX,hipY,kneeX,kneeY,footX,footY;
        public final boolean planted;
        private Leg(float hx,float hy,float kx,float ky,float fx,float fy,boolean planted){
            hipX=hx;hipY=hy;kneeX=kx;kneeY=ky;
            footX=fx;footY=fy;this.planted=planted;
        }
    }
    /**
     * One contact step per leg every two stride phases.
     * Coordinates are in local foot-centered world units and scale with height.
     */
    public static Leg leg(float phase,boolean walking,int side,float height){
        if(side!=-1&&side!=1)throw new IllegalArgumentException("Leg side");
        if(height<=0||!Float.isFinite(height))throw new IllegalArgumentException("Height");
        float hipX=side*height*.086f,hipY=-height*.23f;
        if(!walking || !Float.isFinite(phase)){
            float footX=side*height*.105f;
            return new Leg(hipX,hipY,footX+side*height*.018f,
                    -height*.105f,footX,0,true);
        }
        float angle=phase+(side==1?(float)Math.PI:0);
        float s=(float)Math.sin(angle),z=(float)Math.cos(angle);
        // The stance phase remains on the floor; the swing phase lifts one foot.
        float footX=side*height*.10f+s*height*.145f;
        float lift=Math.max(0,z)*height*.092f;
        float footY=-lift;
        float kneeX=(hipX+footX)*.5f+side*height*.034f;
        float kneeY=hipY*.45f+footY*.20f-height*.018f;
        return new Leg(hipX,hipY,kneeX,kneeY,footX,footY,z<=0);
    }
    public static float torsoLean(float phase,boolean walking,float attack){
        float lean=walking?(float)Math.sin(phase)*2.3f:0f;
        if(attack>=0){
            float t=Math.max(0,Math.min(1,attack));
            lean+=(float)Math.sin(Math.PI*t)*4.3f;
        }
        return lean;
    }
}
