package com.lokij07.riftguard;

/**
 * Optional, real mission challenges. Failing a bonus objective never blocks
 * the existing 300-stage completion/save system.
 */
public final class MissionObjectives {
    private MissionObjectives(){}
    public static String name(int stage){
        Campaign.checkStage(stage);
        switch(Campaign.map(stage)){
            case 0:return "BUILD 2 DEFENSES";
            case 1:return "KEEP CORE AT 70%";
            default:return "BEAT ELITE • HERO 30%+";
        }
    }
    public static boolean completed(int stage,int constructed,float coreFraction,
                                    float heroFraction,boolean defeatedElite){
        Campaign.checkStage(stage);
        switch(Campaign.map(stage)){
            case 0:return constructed>=2;
            case 1:return coreFraction>=.70f;
            default:return defeatedElite&&heroFraction>=.30f;
        }
    }
    public static String status(int stage,int constructed,float coreFraction,
                                float heroFraction,boolean defeatedElite){
        Campaign.checkStage(stage);
        switch(Campaign.map(stage)){
            case 0:return Math.min(2,constructed)+"/2 NEW TOWERS";
            case 1:return Math.max(0,Math.round(coreFraction*100))+"% CORE HP";
            default:return (defeatedElite?"ELITE DOWN":"ELITE ACTIVE")+
                    " • HERO "+Math.max(0,Math.round(heroFraction*100))+"%";
        }
    }
    public static int bonus(int stage){
        Campaign.checkStage(stage);
        return 30+Campaign.world(stage)*5+Campaign.map(stage)*10;
    }
}
