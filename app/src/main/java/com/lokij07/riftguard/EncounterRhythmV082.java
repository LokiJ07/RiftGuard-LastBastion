package com.lokij07.riftguard;

/**
 * Authored wave identities that reuse EXISTING enemy species and animations.
 * A signature unit replaces one ordinary slot in every five; there are no
 * extra enemies, no extra boss/minion spawns and no hidden HP inflation.
 * The first kingdom and first wave remain unchanged for onboarding.
 */
public final class EncounterRhythmV082 {
    private EncounterRhythmV082(){}

    public static final int NONE=-1, VANGUARD=0, RAIDERS=1, HEX=2, STAMPEDE=3;
    private static final String[] NAMES={
            "SHIELD VANGUARD","SWIFT RAIDERS","HEX SUPPORT","BOAR STAMPEDE"
    };
    public static int plan(int stage,int wave){
        Campaign.checkStage(stage);
        if(stage<30||wave<2)return NONE;
        return (Campaign.world(stage)*2+wave-2)%4;
    }
    public static String name(int stage,int wave){
        int p=plan(stage,wave);
        return p==NONE?"STANDARD SIEGE":NAMES[p];
    }
    public static boolean signatureSlot(int stage,int wave,int remaining,
                                        boolean specialMission,boolean elite){
        return plan(stage,wave)!=NONE&&remaining>1&&remaining%5==0&&
                !specialMission&&!elite;
    }
    public static int chooseKind(int stage,int wave,int remaining,
                                 int currentKind,boolean specialMission,
                                 boolean elite){
        if(!signatureSlot(stage,wave,remaining,specialMission,elite))return currentKind;
        switch(plan(stage,wave)){
            case VANGUARD:return 2;  // Existing heavy armored attacker
            case RAIDERS:return 1;   // Existing high-speed raider
            case HEX:return 4;       // Existing healer/projectile caster
            case STAMPEDE:return 6;  // Existing telegraphed charging boar
            default:return currentKind;
        }
    }
}
