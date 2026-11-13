package com.lokij07.riftguard;

/**
 * First playable v0.65 memory-trial set: deterministic fortress challenges using
 * live core health, hero health, placed defenses, siege kills and elite defeat.
 * More world-exclusive escort/lantern/terrain objectives are scheduled for G4/G5.
 */
public final class MemoryTrial {
    private MemoryTrial(){}
    public static final int CORE=0, HERO=1, BUILD=2, DEFEAT=3, ELITE=4;
    private static final String[] GREENHAVEN={
        "THE FIRST PROMISE","HOLD THE GATE","THE BUILDER'S OATH",
        "NO ONE LEFT BEHIND","THE NIGHT WATCH","DORIN'S DEFENSE",
        "THE LONGEST HOUR","THE GREENHAVEN GUARD","THE LAST LIGHT",
        "CHIEF'S FINAL STAND"
    };
    public static final class Objective {
        public final int stage,type,requirement;
        public final String title,description;
        private Objective(int stage,int type,int requirement,String title,String description){
            this.stage=stage;this.type=type;this.requirement=requirement;
            this.title=title;this.description=description;
        }
    }
    public static Objective forStage(int stage){
        Campaign.checkStage(stage);
        if(Campaign.map(stage)!=2)return null;
        int world=Campaign.world(stage),key=(Campaign.level(stage)-1)%10;
        int type=(key==9)?ELITE:(key%4);
        int requirement=type==CORE?Math.max(60,85-world*2):
                type==HERO?Math.max(30,55-world):
                type==BUILD?2:
                type==DEFEAT?Math.min(40,12+world*3):1;
        String title=world==0?GREENHAVEN[key]:
                Campaign.WORLDS[world].toUpperCase()+" • MEMORY "+(key+1);
        String description;
        switch(type){
            case CORE:description="Keep the fortress core above "+requirement+"% health at victory.";break;
            case HERO:description="Win with the Warden above "+requirement+"% health.";break;
            case BUILD:description="Construct at least two defenses during the siege.";break;
            case DEFEAT:description="Defeat at least "+requirement+" attackers before victory.";break;
            default:description="Defeat the world boss and survive with the core intact.";break;
        }
        return new Objective(stage,type,requirement,title,description);
    }
    public static boolean evaluate(Objective objective,int built,int killed,
                                   float coreFraction,float heroFraction,boolean defeatedElite){
        if(objective==null)return false;
        switch(objective.type){
            case CORE:return coreFraction>=objective.requirement/100f;
            case HERO:return heroFraction>=objective.requirement/100f;
            case BUILD:return built>=objective.requirement;
            case DEFEAT:return killed>=objective.requirement;
            case ELITE:return defeatedElite&&coreFraction>0;
            default:return false;
        }
    }
}
