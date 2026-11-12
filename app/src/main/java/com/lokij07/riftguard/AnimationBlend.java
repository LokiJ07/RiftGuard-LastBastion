package com.lokij07.riftguard;

/**
 * Frame transitions for an existing 4-frame painted sprite atlas.
 * Normal motion blends only during the LAST 22% of each pose to avoid
 * long double-exposure ghosts. Event clips never wrap contact to windup.
 *
 * No new image assets are synthesized by this class; it is honest about
 * the limitations of the original painted atlas.
 */
public final class AnimationBlend {
    private AnimationBlend(){}
    private static final float BLEND_BEGIN=.78f;
    public static final class State {
        public final int current,next;
        public final float nextAlpha;
        State(int current,int next,float nextAlpha){
            this.current=current;this.next=next;this.nextAlpha=nextAlpha;
        }
        public boolean shouldBlend(){return next!=current&&nextAlpha>.001f;}
    }
    private static float clamp(float x){return Math.max(0,Math.min(1,x));}
    private static float smooth(float t){t=clamp(t);return t*t*(3-2*t);}
    public static State loop(float time,float framesPerSecond,int frames){
        if(!Float.isFinite(time)||!Float.isFinite(framesPerSecond)||
                framesPerSecond<=0||frames<1)return new State(0,0,0);
        float playtime=Math.max(0,time)*framesPerSecond;
        int frame=Math.floorMod((int)Math.floor(playtime),frames);
        float fraction=playtime-(float)Math.floor(playtime);
        int next=(frame+1)%frames;
        float alpha=smooth((fraction-BLEND_BEGIN)/(1f-BLEND_BEGIN))*.44f;
        return new State(frame,next,alpha);
    }
    public static State event(float completion,int frames){
        if(frames<=0)return new State(0,0,0);
        float progress=clamp(completion);
        float playtime=Math.min(frames-0.001f,progress*frames);
        int frame=Math.min(frames-1,(int)playtime);
        float fraction=playtime-frame;
        float alpha=frame==frames-1?0:
                smooth((fraction-BLEND_BEGIN)/(1f-BLEND_BEGIN))*.44f;
        return new State(frame,Math.min(frames-1,frame+1),alpha);
    }
    /**
     * Different species move their chest/mandibles with an independent rhythm.
     * The painted upper torso follows this displacement; legs stay anchored.
     */
    public static float enemyBreath(int kind,float seconds,boolean moving){
        if(!Float.isFinite(seconds))return 0;
        float speed=moving?7f:2.8f;
        return (float)Math.sin(seconds*speed+kind*.82f)*
                (kind==5?2.7f:kind==6?2.1f:1.2f);
    }
    public static float enemyAttackLean(int kind,float completion){
        if(completion<0)return 0;
        float t=clamp(completion);
        // Telegraph: lean backward then lunge in the CONTACT half.
        float anticipation=-7f*(float)Math.sin(Math.PI*Math.min(1,t/.48f));
        float lunge=14f*(float)Math.sin(Math.PI*Math.max(0,
                Math.min(1,(t-.35f)/.60f)));
        return (anticipation+lunge)*(kind==5?1.3f:1);
    }
}
