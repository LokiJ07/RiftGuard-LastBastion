package com.lokij07.riftguard;

/**
 * Distinct, animated and gameplay-active hazards in each kingdom and mission.
 * Terrain animation, combat effects and test logic share the same coordinates.
 */
public final class StageHazards {
    private StageHazards(){}
    public static final String[] NAMES={
        "BRAMBLE SNARE","SPECTRAL MIST","VENOM POOL","MAGMA VENT",
        "FROST VORTEX","ROCKFALL","CRYSTAL SURGE","THUNDER CHARGE",
        "SHADOW RIFT","ECLIPSE NOVA"};
    public static final int[] COLORS={
        0xffb2d47c,0xffbdb2fb,0xff8edb75,0xffff9661,0xff9deeff,
        0xffd4ab86,0xff8fe7f0,0xffc5b6ff,0xffa67de4,0xffffbd87};
    public static int count(int stage){
        Campaign.checkStage(stage);
        return 1+Campaign.map(stage)+(DifficultyRules.worldBoss(stage)?1:0);
    }
    public static int kind(int stage){return Campaign.world(stage);}
    public static float x(int stage,int index){
        check(stage,index);
        int seed=LaneRules.routeSignature(stage);
        float base=1290+(index*141)+(Math.floorMod(seed >>> (index*3),5)*39);
        return BattleRules.clamp(base,1285,1775);
    }
    public static float y(int stage,int index){
        check(stage,index);
        int seed=LaneRules.routeSignature(stage);
        float offset=(Math.floorMod(seed >>> ((index*4)+3),5)-2)*21f;
        return BattleRules.clamp(LaneRules.stagePathY(stage,x(stage,index))+offset,355,685);
    }
    public static float radius(int stage,int index){
        check(stage,index);
        return 59f+(index%2)*12f;
    }
    private static void check(int stage,int index){
        Campaign.checkStage(stage);
        if(index<0||index>=count(stage))throw new IllegalArgumentException("hazard index");
    }
    public static float charge(int stage,int index,float time){
        check(stage,index);
        if(!Float.isFinite(time))return 0;
        float period=5.2f+(kind(stage)%3)*.6f;
        float t=(time+index*1.3f+(LaneRules.routeSignature(stage)%11)*.11f)%period;
        if(t<0)t+=period;
        return t/period;
    }
    public static boolean active(int stage,int index,float time){
        float phase=charge(stage,index,time);
        return phase<.36f; // Telegraphs are visible before each active pulse.
    }
    public static float heroDamage(int stage,int difficulty){
        if(kind(stage)==0 || kind(stage)==4)return 0; // Roots and ice slow movement instead.
        return (3f+Campaign.world(stage)*.9f)*
                DifficultyRules.enemyDamage(difficulty);
    }
    public static float monsterDamage(int stage){
        return 2.2f+Campaign.world(stage)*1.4f;
    }
    public static boolean slows(int stage){
        int w=kind(stage);
        return w==0||w==1||w==2||w==4||w==8;
    }
}
