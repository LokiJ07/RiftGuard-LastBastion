package com.lokij07.riftguard;

/**
 * In-place 2D Warden animation controller.
 *
 * Engine: Android Canvas + the existing painted 4x4 Warden sprite atlas.
 * Animation states are independent of physics and input; one-shot clips do
 * not wrap, and frame markers fire exactly once even at low frame rates.
 *
 * Priority: DEATH > HURT > CAST > ATTACK > RUN > IDLE.
 * Attack/cast blend only the torso; lower-body motion stays separately
 * articulated by MotionArt. Weapon hitboxes read the same event clock.
 */
public final class WardenAnimationMachine {
    public enum State { IDLE,RUN,ATTACK,CAST,HURT,DEATH }
    public enum Event { ATTACK_CONTACT, FOOTSTEP, CLIP_FINISHED }

    private static final float CAST_SECONDS=.55f;
    private static final float HURT_SECONDS=.19f;
    private static final float DEATH_SECONDS=.66f;
    private static final float RUN_STEP_SECONDS=.235f;
    private static final float MAX_FRAME_STEP=.20f;
    private static final float[] CONTACT={.28f,.43f,.37f,.44f,.23f,.51f};
    private State state=State.IDLE;
    private float time=0, duration=0, footTimer=0;
    private int weapon=0;
    private boolean contactSent, contactEvent, footEvent, clipFinishEvent;
    private boolean moving;

    public void reset(){
        state=State.IDLE;time=duration=footTimer=0;
        weapon=0;contactSent=contactEvent=footEvent=clipFinishEvent=false;
        moving=false;
    }
    public State state(){return state;}
    public int weapon(){return weapon;}
    public float elapsed(){return time;}
    public float normalized(){
        if(state==State.IDLE||state==State.RUN)return 0;
        return duration<=0?1f:Math.min(1f,time/duration);
    }
    public float attackProgress(){return state==State.ATTACK?normalized():-1f;}
    public float castProgress(){return state==State.CAST?normalized():-1f;}
    public float deathProgress(){return state==State.DEATH?normalized():-1f;}
    public boolean isHurt(){return state==State.HURT;}
    public boolean locked(){return state==State.HURT||state==State.DEATH;}
    public float duration(){return duration;}
    public static float contactProgress(int w){
        if(w<0||w>=CONTACT.length)throw new IllegalArgumentException("weapon");
        return CONTACT[w];
    }
    /** Called when a real projectile or melee animation is started. */
    public boolean attack(int w){
        if(state==State.DEATH||state==State.HURT)return false;
        if(w<0||w>=6)throw new IllegalArgumentException("weapon");
        weapon=w;
        state=State.ATTACK;
        time=0;duration=WeaponAnimation.duration(w);
        contactSent=contactEvent=false;
        clipFinishEvent=false;
        return true;
    }
    public boolean cast(){
        if(state==State.DEATH||state==State.HURT)return false;
        state=State.CAST;time=0;duration=CAST_SECONDS;
        contactSent=contactEvent=false;
        clipFinishEvent=false;
        return true;
    }
    public void hurt(){
        if(state==State.DEATH)return;
        state=State.HURT;time=0;duration=HURT_SECONDS;
        contactSent=contactEvent=false;
        clipFinishEvent=false;
    }
    public void die(){
        if(state==State.DEATH)return;
        state=State.DEATH;time=0;duration=DEATH_SECONDS;
        contactSent=contactEvent=false;
        clipFinishEvent=false;
    }
    private static boolean singleShot(State state){
        return state==State.ATTACK||state==State.CAST||
                state==State.HURT||state==State.DEATH;
    }
    /** Variable-step animation clock; physics and touch sampling stay separate. */
    public void advance(float dt,boolean moving){
        if(!Float.isFinite(dt)||dt<0)return;
        this.moving=moving;
        dt=Math.min(MAX_FRAME_STEP,dt);
        if(state==State.IDLE||state==State.RUN)
            state=moving?State.RUN:State.IDLE;
        // Feet always keep a stable phase on transitions; attack upper body
        // does not reset the gait clock or break a planted foot.
        if(moving&&state!=State.DEATH){
            footTimer+=dt;
            if(footTimer>=RUN_STEP_SECONDS){
                footTimer%=RUN_STEP_SECONDS;
                footEvent=true;
            }
        }else if(!moving){
            footTimer=0;
        }
        if(!singleShot(state))return;
        float previous=time;
        time=Math.min(duration,time+dt);
        if(state==State.ATTACK&&!contactSent &&
                previous<duration*CONTACT[weapon] &&
                time>=duration*CONTACT[weapon]){
            contactSent=true;contactEvent=true;
        }
        if(time+0.00001f>=duration&&previous<duration){
            clipFinishEvent=true;
            if(state!=State.DEATH){
                state=moving?State.RUN:State.IDLE;
                time=0;duration=0;
            }
        }
    }
    public boolean consume(Event event){
        if(event==Event.ATTACK_CONTACT){
            boolean result=contactEvent;contactEvent=false;return result;
        }
        if(event==Event.FOOTSTEP){
            boolean result=footEvent;footEvent=false;return result;
        }
        boolean result=clipFinishEvent;clipFinishEvent=false;return result;
    }
    /** The bundled Warden atlas offers IDLE/RUN/ATTACK/CAST rows only. */
    public int sourceRow(){
        switch(state){
            case RUN:return 1;
            case ATTACK:return 2;
            case CAST:return 3;
            default:return 0; // HURT/DEATH use tinted/rotated idle until new art arrives.
        }
    }
    public float rowProgress(){
        if(state==State.ATTACK||state==State.CAST)return normalized();
        if(state==State.HURT||state==State.DEATH)return normalized();
        return -1;
    }
}
