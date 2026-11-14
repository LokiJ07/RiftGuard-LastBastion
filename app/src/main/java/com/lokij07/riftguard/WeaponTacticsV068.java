package com.lokij07.riftguard;

/** Bought SPECIAL mastery node #6 has real utility before the final node. */
public final class WeaponTacticsV068 {
    private WeaponTacticsV068(){}
    public static float slowSeconds(int weapon){
        switch(weapon){
            case 0:return .8f; // Hunter's Mark
            case 1:return 1.0f; // Heavy Cleave
            case 3:return .75f; // Static Chain
            case 4:return 1.15f; // Suppression
            case 5:return 1.8f; // Chillbind
            default:return 0f;
        }
    }
    public static float burnSeconds(int weapon){
        return weapon==2?2.4f:0f; // Ignite
    }
}
