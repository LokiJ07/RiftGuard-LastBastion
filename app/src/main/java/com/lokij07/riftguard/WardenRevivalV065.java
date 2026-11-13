package com.lokij07.riftguard;

/**
 * Battle-local revive state. Core destruction still defeats the stage.
 * When the Warden is down, towers and enemies continue the active siege.
 */
public final class WardenRevivalV065 {
    private static final int[] MAX={2,1,0,0};
    private static final float[] DELAY={5f,8f,0f,0f};
    private int mode,revivalsUsed;
    private float remaining;
    public void reset(int difficulty){mode=DifficultyRules.safe(difficulty);revivalsUsed=0;remaining=0;}
    public int remainingCharges(){return Math.max(0,MAX[mode]-revivalsUsed);}
    public boolean isDown(){return remaining>0;}
    public float secondsLeft(){return remaining;}
    public boolean canFall(){return !isDown()&&remainingCharges()>0;}
    public boolean fall(){
        if(!canFall())return false;
        revivalsUsed++;remaining=DELAY[mode];return true;
    }
    /** Returns true exactly once when revival finishes. */
    public boolean tick(float seconds){
        if(!isDown())return false;
        remaining=Math.max(0,remaining-Math.max(0,seconds));
        return remaining==0;
    }
}
