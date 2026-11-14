package com.lokij07.riftguard;

/**
 * Short Warden-only combat burst earned through TWO different *confirmed*
 * counterplay events. Prevents attack-button/charged/dodge spam from paying
 * rewards by itself. Does not touch permanent XP, saves or turret damage.
 *
 * Each wave has at most one 14-26 gold combat stipend, not per-kill farming.
 * The 9% weapon boost lasts 7s and has an 18s cooldown.
 */
public final class CombatFlowV082 {
    public static final int FINISHER=1, CHARGED=2, EVADE=3;
    public static final float WINDOW_SECONDS=9f, BOOST_SECONDS=7f,
            COOLDOWN_SECONDS=18f, DAMAGE_BONUS=.09f;
    private int firstEvent, wave;
    private float firstTime=-100f, nextReadyTime=-100f, boostUntil=-100f;
    private boolean rewardedThisWave;

    public void reset(){
        firstEvent=0;wave=0;firstTime=-100f;nextReadyTime=-100f;
        boostUntil=-100f;rewardedThisWave=false;
    }
    public void beginWave(int newWave){
        if(newWave<1)throw new IllegalArgumentException("wave");
        wave=newWave;firstEvent=0;firstTime=-100f;
        // Cooldown and an active earned boost are NOT refreshable via wave skip.
        rewardedThisWave=false;
    }
    public boolean onConfirmedAction(int action,float seconds){
        if(action<FINISHER||action>EVADE||!Float.isFinite(seconds)||
                wave==0||seconds<nextReadyTime)return false;
        if(firstEvent==0||seconds-firstTime>WINDOW_SECONDS||
                seconds<firstTime){
            firstEvent=action;firstTime=seconds;return false;
        }
        if(firstEvent==action)return false;
        firstEvent=0;firstTime=-100f;
        boostUntil=seconds+BOOST_SECONDS;
        nextReadyTime=seconds+COOLDOWN_SECONDS;
        return true;
    }
    public float weaponMultiplier(float seconds){
        return seconds<boostUntil&&Float.isFinite(seconds)?
                1f+DAMAGE_BONUS:1f;
    }
    public float remaining(float seconds){
        return Float.isFinite(seconds)?Math.max(0f,boostUntil-seconds):0f;
    }
    public int awardBattleGold(int stage){
        Campaign.checkStage(stage);
        if(wave==0||rewardedThisWave)return 0;
        rewardedThisWave=true;
        return 14+Math.min(12,Campaign.world(stage)*2);
    }
}
