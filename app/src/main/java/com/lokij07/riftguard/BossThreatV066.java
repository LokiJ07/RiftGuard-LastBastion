package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Original low-allocation boss charge telegraph.
 * A boss's existing serverless simulation rules remain authoritative:
 * - 265 world units is the real hero-hit radius
 * - the boss attacks the keep only while within 445 world units
 * - countdown is BossTactics.chargeTime(stage, phase).
 *
 * This renderer never applies damage or changes targeting. It makes real
 * hazards visible BELOW characters without obscuring the touch HUD.
 */
public final class BossThreatV066 {
    private BossThreatV066(){}
    private static final Paint P=new Paint(Paint.ANTI_ALIAS_FLAG);
    public static final float HERO_THREAT_RADIUS=265f;
    public static final float CORE_ATTACK_RANGE=445f;
    private static final int[] COLORS={0xffffbc79,0xffc6a0ed,0xff9aebc0};

    public static float progress(float remaining,float total){
        if(!Float.isFinite(remaining)||!Float.isFinite(total)||total<=0f)
            return 0f;
        return Math.max(0f,Math.min(1f,1f-remaining/total));
    }
    public static boolean threatensCore(float bossX,float bossY,
                                        float coreX,float coreY){
        return Math.hypot(bossX-coreX,bossY-coreY)<CORE_ATTACK_RANGE;
    }
    public static void draw(Canvas c,float bossX,float bossY,
                            float coreX,float coreY,
                            float secondsRemaining,float totalSeconds,
                            int tactic,int phase,boolean reducedMotion){
        draw(c,bossX,bossY,coreX,coreY,secondsRemaining,totalSeconds,
                tactic,phase,reducedMotion,HERO_THREAT_RADIUS);
    }
    /** The drawn radius must be the actual hit radius for this realm/phase. */
    public static void draw(Canvas c,float bossX,float bossY,
                            float coreX,float coreY,
                            float secondsRemaining,float totalSeconds,
                            int tactic,int phase,boolean reducedMotion,
                            float dangerRadius){
        if(!Float.isFinite(dangerRadius)||dangerRadius<=0f)return;
        if(!Float.isFinite(secondsRemaining)||secondsRemaining<=0f||
                !Float.isFinite(bossX)||!Float.isFinite(bossY)||
                tactic<0||tactic>=COLORS.length)return;
        float progress=progress(secondsRemaining,totalSeconds);
        int tint=COLORS[tactic];
        float radius=dangerRadius;
        P.setShader(null);
        // World-space ground shadow: a thin ring identifies the actual
        // distance at which the Warden will be hurt on the charge impact.
        P.setStyle(Paint.Style.FILL);
        P.setColor((tint&0x00ffffff)|0x10000000);
        c.drawCircle(bossX,bossY,radius,P);
        P.setStyle(Paint.Style.STROKE);
        P.setStrokeCap(Paint.Cap.ROUND);
        P.setStrokeWidth(2.5f+phase*.75f);
        P.setColor((tint&0x00ffffff)|0x99000000);
        c.drawCircle(bossX,bossY,radius,P);
        // The sweep tracks a real countdown, not arbitrary time-based lights.
        P.setColor(tint);
        P.setStrokeWidth(8f);
        c.drawArc(bossX-radius,bossY-radius,bossX+radius,bossY+radius,
                -90f,360f*progress,false,P);
        if(!reducedMotion){
            P.setColor((tint&0x00ffffff)|0x55000000);
            P.setStrokeWidth(3f);
            c.drawArc(bossX-radius-12,bossY-radius-12,
                    bossX+radius+12,bossY+radius+12,
                    -90f,360f*progress,false,P);
        }
        if(threatensCore(bossX,bossY,coreX,coreY)){
            // Keep target is accurate for siege charges; don't depict the
            // player as the only target when the fortress is also vulnerable.
            P.setStrokeWidth(5f);
            P.setColor((tint&0x00ffffff)|0xcc000000);
            c.drawCircle(coreX,coreY,58f+progress*8f,P);
            P.setStrokeWidth(2f);
            c.drawLine(coreX-32,coreY,coreX+32,coreY,P);
            c.drawLine(coreX,coreY-32,coreX,coreY+32,P);
        }
        P.setStyle(Paint.Style.FILL);
    }
}
