package com.lokij07.riftguard;

/**
 * Frame-time-driven cosmetic budget. Physics, hitboxes, UI, and gameplay are
 * never throttled. Hysteresis avoids flickering between art quality tiers.
 *
 * Both onDraw CPU cost and actual frame interval are observed, since emulator
 * and handset GPU stalls sometimes occur after draw(Canvas) returns.
 */
public final class RenderBudgetV065 {
    private int slowFrames=0, fastFrames=0;
    private boolean leanEffects=false;
    public boolean leanEffects(){return leanEffects;}
    public void reset(){slowFrames=0;fastFrames=0;leanEffects=false;}
    public void sample(float drawMs,float frameGapMs){
        if(!Float.isFinite(drawMs)||drawMs<0f||
                !Float.isFinite(frameGapMs)||frameGapMs<0f)return;
        // Ignore breaks caused by app pause/background rather than rendering.
        if(frameGapMs>1100f)return;
        boolean slow=drawMs>25f||(frameGapMs>44f&&frameGapMs<550f);
        boolean fast=drawMs<16f&&(frameGapMs<27f||frameGapMs==0f);
        if(!leanEffects){
            slowFrames=slow?Math.min(12,slowFrames+1):Math.max(0,slowFrames-2);
            if(slowFrames>=8){leanEffects=true;fastFrames=0;}
        }else{
            fastFrames=fast?Math.min(180,fastFrames+1):Math.max(0,fastFrames-3);
            if(fastFrames>=90){leanEffects=false;slowFrames=0;}
        }
    }
}
