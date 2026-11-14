package com.lokij07.riftguard;

/** Deterministic signature threats without increasing the wave's enemy count. */
public final class SiegeEncountersV068 {
    private SiegeEncountersV068(){}
    public static int signatureKind(int stage){
        Campaign.checkStage(stage);
        if(stage<4)return -1; // Teaching stages remain gentle.
        switch(((stage-4)/3)%3){
            case 0:return 6; // Charging boar.
            case 1:return 4; // Healing shaman.
            default:return 2; // Heavy bruiser.
        }
    }
    public static int chooseKind(int stage,int wave,int remaining,int total,int ordinaryKind){
        Campaign.checkStage(stage);
        if(ordinaryKind<0||ordinaryKind>6)throw new IllegalArgumentException("enemy kind");
        if(wave!=2||total<3||remaining!=Math.max(2,total/2))return ordinaryKind;
        int signature=signatureKind(stage);
        return signature>=0?signature:ordinaryKind;
    }
    public static String warning(int stage){
        switch(signatureKind(stage)){
            case 6:return "SIGNATURE WAVE: charging boars telegraph a rush. Dodge sideways.";
            case 4:return "SIGNATURE WAVE: shamans heal wounded allies. Prioritize their casts.";
            case 2:return "SIGNATURE WAVE: a heavy bruiser pressures the keep. Upgrade a turret.";
            default:return "";
        }
    }
}
