package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Compact, deterministic animation-event feedback for Android Canvas battles.
 *
 * Hit-pause suspends sprite clocks for a few milliseconds, while simulation
 * and touch input keep updating: unlike timeScale=0 it cannot stall
 * touch/movement or damage accounting. Camera shake affects ONLY world layers,
 * never HUD or input coordinates. Every pulse comes from a real damage event,
 * never the FIRE button itself.
 */
public final class CombatAnimationFeel {
    private static final float MELEE_PAUSE=.055f, RANGED_PAUSE=.029f;
    private static final float MAX_SHAKE=.19f, FLASH=.15f;
    private float pauseRemaining, shakeRemaining, shakeDuration,
            amplitude,flashRemaining, clock, x,y;
    private int color=0xffffd3a4;
    private boolean reducedMotion;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path contactSlash=new Path();

    public void reset(){
        pauseRemaining=shakeRemaining=shakeDuration=amplitude=flashRemaining=clock=0f;
        x=y=0;
    }
    public void setReducedMotion(boolean reduced){reducedMotion=reduced;}
    public boolean reducedMotion(){return reducedMotion;}
    public void onDamage(float x,float y,int color,boolean melee,float damage){
        if(!Float.isFinite(x)||!Float.isFinite(y)||!Float.isFinite(damage)
                ||damage<=0)return;
        this.x=x;this.y=y;this.color=color;
        if(reducedMotion){
            pauseRemaining=0;
            amplitude=0;shakeRemaining=0;
            flashRemaining=Math.max(flashRemaining,.07f);
            return;
        }
        pauseRemaining=Math.max(pauseRemaining,melee?MELEE_PAUSE:RANGED_PAUSE);
        shakeDuration=melee?.155f:.105f;
        shakeRemaining=Math.max(shakeRemaining,shakeDuration);
        amplitude=Math.min(5.5f,Math.max(amplitude,
                (melee?2.8f:1.35f)+Math.min(2.3f,damage/200f)));
        flashRemaining=FLASH;
    }
    /**
     * @return animation-clock delta (may be zero during short hit-pause).
     * Always call exactly once per displayed frame. Physics still uses raw dt.
     */
    public float frame(float dt){
        if(!Float.isFinite(dt)||dt<=0)return 0;
        dt=Math.min(.05f,dt);
        clock+=dt;
        float frozen=reducedMotion?0:Math.min(dt,pauseRemaining);
        pauseRemaining=Math.max(0,pauseRemaining-dt);
        shakeRemaining=Math.max(0,shakeRemaining-dt);
        flashRemaining=Math.max(0,flashRemaining-dt);
        return dt-frozen;
    }
    public float shakeX(){
        if(reducedMotion||shakeRemaining<=0)return 0;
        float fade=shakeDuration<=0?0:shakeRemaining/shakeDuration;
        return (float)Math.sin(clock*111f)*amplitude*fade;
    }
    public float shakeY(){
        if(reducedMotion||shakeRemaining<=0)return 0;
        float fade=shakeDuration<=0?0:shakeRemaining/shakeDuration;
        return (float)Math.cos(clock*83f)*amplitude*fade*.48f;
    }
    public float flashRemaining(){return flashRemaining;}
    public float pauseRemaining(){return pauseRemaining;}
    /**
     * Draw thin directional contact arcs, not another detached circle around
     * the hero. The star lives in ENEMY world coordinates and fades fast.
     */
    public void drawContact(Canvas c){
        if(flashRemaining<=0)return;
        float progress=1-flashRemaining/FLASH;
        int alpha=Math.round(230*Math.max(0,1-progress));
        c.save();
        c.translate(x,y-12);
        c.rotate(23f);
        p.setShader(null);p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(2.8f-progress*1.6f);
        p.setColor((alpha<<24)|(color&0x00ffffff));
        for(int i=0;i<4;i++){
            float angle=(float)(Math.PI*.25f+Math.PI*.5f*i);
            float length=14+progress*18;
            float x0=(float)Math.cos(angle)*7;
            float y0=(float)Math.sin(angle)*7;
            c.drawLine(x0,y0,(float)Math.cos(angle)*length,
                    (float)Math.sin(angle)*length,p);
        }
        p.setColor((Math.min(255,alpha)<<24)|0x00fff8df);
        contactSlash.reset();
        contactSlash.moveTo(-24-progress*10,9);
        contactSlash.quadTo(3,-11,24+progress*11,-10);
        c.drawPath(contactSlash,p);
        p.setStyle(Paint.Style.FILL);
        c.restore();
    }
}
