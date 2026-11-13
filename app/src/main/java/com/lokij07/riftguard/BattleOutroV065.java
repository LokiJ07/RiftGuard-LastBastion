package com.lokij07.riftguard;

/**
 * Result animations occur in the real battlefield before the result popup.
 * The battlefield simulation stops at the outcome; persistent rewards are
 * committed only once after the outro completes.
 */
public final class BattleOutroV065 {
    public static final float VICTORY_SECONDS=3.65f;
    public static final float DEFEAT_SECONDS=2.55f;
    public static final float VICTORY_WALK_SECONDS=2.70f;
    private static final float END_X=1734f;
    public final boolean victory;
    public final float startX,startY,endX,endY;
    private float elapsed;
    public BattleOutroV065(int stage,boolean victory,float x,float y){
        Campaign.checkStage(stage);
        this.victory=victory;
        this.startX=x;this.startY=y;
        this.endX=victory?Math.max(x,END_X):x;
        this.endY=victory?LaneRules.stagePathY(stage,endX):y;
    }
    public void advance(float dt){
        if(!Float.isFinite(dt)||dt<=0)return;
        elapsed=Math.min(duration(),elapsed+dt);
    }
    public float duration(){return victory?VICTORY_SECONDS:DEFEAT_SECONDS;}
    public float elapsed(){return elapsed;}
    public float progress(){return elapsed/duration();}
    public boolean finished(){return elapsed>=duration();}
    public boolean walking(){return victory&&elapsed<VICTORY_WALK_SECONDS;}
    public float positionX(){
        float t=walkFraction();
        return startX+(endX-startX)*t;
    }
    public float positionY(){
        float t=walkFraction();
        return startY+(endY-startY)*t;
    }
    public float direction(){
        return (float)Math.atan2(endY-startY,endX-startX);
    }
    private float walkFraction(){
        if(!victory)return 0f;
        float p=Math.max(0f,Math.min(1f,elapsed/VICTORY_WALK_SECONDS));
        // Gentle acceleration and deceleration: no snap at the finish.
        return p*p*(3f-2f*p);
    }
}
