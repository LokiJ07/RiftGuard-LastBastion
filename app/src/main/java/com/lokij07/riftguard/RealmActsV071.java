package com.lokij07.riftguard;

/**
 * Each realm has three visible chapter acts. The wave-two signature enemy
 * role changes with the act and realm rather than growing the mob count.
 * First 30 stages keep their previously validated tutorial encounters.
 */
public final class RealmActsV071 {
    private RealmActsV071(){}
    public static final int DISCOVERY=0,ESCALATION=1,SIEGE=2;
    private static final String[] NAMES={"DISCOVERY","ESCALATION","FINAL SIEGE"};
    private static final String[] ORDERS={
        "Learn the realm's routes and enemy signals before the siege grows.",
        "Support units and armored attackers now demand target priority.",
        "Protect upgraded defenses; the realm commander is preparing the last stand."
    };
    public static int act(int stage){
        int localLevel=(Campaign.level(stage)-1)%10;
        return localLevel<3?DISCOVERY:localLevel<7?ESCALATION:SIEGE;
    }
    public static String name(int stage){return NAMES[act(stage)];}
    public static String orders(int stage){return ORDERS[act(stage)];}
    /** These are existing seven enemy kinds; no new spawns. */
    public static int threatKind(int stage){
        Campaign.checkStage(stage);
        if(stage<30)return SiegeEncountersV068.signatureKind(stage);
        // Rotate boar/shaman/bruiser priority with realm and act.
        int cycle=(Campaign.map(stage)+act(stage)+Campaign.world(stage))%3;
        return cycle==0?6:cycle==1?4:2;
    }
    public static int chooseKind(int stage,int wave,int remaining,int total,int original){
        int existing=SiegeEncountersV068.chooseKind(stage,wave,remaining,total,original);
        if(stage<30||existing==original)return existing;
        if(wave==2&&total>=3&&remaining==Math.max(2,total/2))
            return threatKind(stage);
        return existing;
    }
    public static String warning(int stage){
        int kind=threatKind(stage);
        return kind==6?"WAVE TWO: charging boars telegraph their rush.":
                kind==4?"WAVE TWO: shamans heal wounded allies. Stop their casts.":
                kind==2?"WAVE TWO: heavy bruisers pressure your gate. Upgrade a defense.":
                "";
    }
}
