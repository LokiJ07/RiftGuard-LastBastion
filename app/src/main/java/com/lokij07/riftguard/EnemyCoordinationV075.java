package com.lokij07.riftguard;

/**
 * Tactics shared by the actual siege simulation and its visible world
 * telegraphs. This AI changes TARGETS and MOVE DECISIONS, not enemy counts.
 * Later-stage only: the first realm tutorial and all boss casts remain stable.
 */
public final class EnemyCoordinationV075 {
    private EnemyCoordinationV075(){}
    public static final float RETREAT_SECONDS=1.35f;
    public static final float RETREAT_COOLDOWN=7.4f;
    public static final float SHAMAN_GUARD_RANGE=235f;
    public static final float RETREAT_TRIGGER_DISTANCE=260f;
    public static final float SIEGE_LOCK_DISTANCE=700f;

    /** A visible attack order is issued for an already-built real gun pad.
     * Prefer dangerous, damaged towers, not a random target that follows
     * hidden inputs; tie-breakers use stable pad order.
     */
    public static int focusTower(int[] tiers,float[] hp,int count,int fortressRank){
        if(tiers==null||hp==null||count<0||count>tiers.length||
                count>hp.length)throw new IllegalArgumentException("tower layout");
        int best=-1;
        float bestScore=Float.NEGATIVE_INFINITY;
        for(int i=0;i<count;i++){
            if(tiers[i]<=0||hp[i]<=0||!Float.isFinite(hp[i]))continue;
            float max=TowerDurability.maxHp(tiers[i],fortressRank);
            float fraction=BattleRules.clamp(hp[i]/Math.max(1f,max),0f,1f);
            // The raid clearly prioritizes exposed artillery, but cannot
            // exploit unavailable tower upgrades or hidden player stats.
            float score=tiers[i]*9f+(1f-fraction)*13f+
                    (TowerPadLayout.family(i)==1?4f:0f);
            if(score>bestScore){bestScore=score;best=i;}
        }
        return best;
    }

    public static boolean raidActive(int stage,int wave,int plan,boolean missionActive,
                                     boolean caveMode,int focusedSlot){
        Campaign.checkStage(stage);
        return stage>=30&&wave>=2&&plan==SquadTacticsV065.SIEGE&&
                !missionActive&&!caveMode&&focusedSlot>=0;
    }

    /** Only THREE existing species can follow the same physically marked pad.
     * Hero engagement always overrides raid targeting (player counterplay).
     */
    public static boolean raidAttacker(int kind,boolean boss,boolean huntHero,
                                       float distToTower){
        return !boss&&!huntHero&&(kind==1||kind==2||kind==5)&&
                Float.isFinite(distToTower)&&distToTower<SIEGE_LOCK_DISTANCE;
    }

    public static float towerStop(int kind){return kind==1?118f:76f;}
    public static float towerStrikeRange(int kind){return kind==1?158f:112f;}

    /** A wounded healer retreats only while another allied shield protects it.
     * Killing that shield or intercepting the healer removes this option.
     */
    public static boolean shouldRetreat(int stage,int kind,boolean boss,
                                        float hp,float maxHp,float distHero,
                                        boolean shieldNearby,float x,
                                        float cooldown){
        Campaign.checkStage(stage);
        return stage>=30&&kind==4&&!boss&&shieldNearby&&
                Float.isFinite(hp)&&Float.isFinite(maxHp)&&maxHp>0&&hp>0&&
                hp/maxHp<=.42f&&distHero>0&&
                distHero<RETREAT_TRIGGER_DISTANCE&&cooldown<=0&&
                x<LaneRules.ENEMY_SPAWN_X-110f;
    }

    public static boolean protectiveShield(int kind,boolean boss,float hp,
                                           float sx,float sy,float hx,float hy){
        if(kind!=2||boss||hp<=0||!Float.isFinite(sx)||
                !Float.isFinite(sy)||!Float.isFinite(hx)||!Float.isFinite(hy))return false;
        float dx=sx-hx,dy=sy-hy;
        return dx*dx+dy*dy<=SHAMAN_GUARD_RANGE*SHAMAN_GUARD_RANGE;
    }

    public static float retreatX(float currentX){
        return Math.min(LaneRules.ENEMY_SPAWN_X-40f,currentX+185f);
    }
    public static float retreatY(float currentY,float heroY){
        // Leave the forward charge lane; still bounded inside playable world.
        float direction=heroY>currentY?-1f:1f;
        return BattleRules.clamp(currentY+direction*72f,325f,755f);
    }

    /** Existing fast raiders take two modest forked paths during FLANK wave.
     * Both rejoin at the castle; no ghost collisions or teleporting.
     */
    public static int pincerSide(int stage,int wave,int plan,int kind,
                                 int remaining,boolean existingFlank){
        Campaign.checkStage(stage);
        if(stage<30||wave<2||plan!=SquadTacticsV065.FLANK||
                existingFlank||(kind!=0&&kind!=1)||remaining%3!=0)return 0;
        return remaining%2==0?-1:1;
    }

    public static float pincerOffset(float enemyX,int side){
        if(side==0)return 0f;
        if(side!=-1&&side!=1)throw new IllegalArgumentException("pincer side");
        return side*72f*BattleRules.clamp(
                (enemyX-LaneRules.CASTLE_GATE_X)/485f,0f,1f);
    }
}
