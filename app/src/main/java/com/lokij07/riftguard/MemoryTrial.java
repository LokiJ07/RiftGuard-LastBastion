package com.lokij07.riftguard;

/**
 * 100 unique Last Stand objectives, two real defendable relic missions and
 * multiple measured tactical rules per world. Losing one never blocks victory.
 * Keys are claim-once even when the same battle is replayed.
 */
public final class MemoryTrial {
    private MemoryTrial(){}
    public static final int CORE=0,HERO=1,BUILD=2,DEFEAT=3,ELITE=4,RELIC=5,NO_LOSS=6;
    private static final String[] RELICS={
        "REFUGEE SUPPLY CART","ANCIENT LANTERN","ANTIDOTE SUPPLIES",
        "GARRICK'S FORGE","MOUNTAIN BEACON","FOUNDRY GENERATOR",
        "MEMORY SHRINE","STORM CONDUCTOR","SHADOW WARD","ECLIPSE SEAL"
    };
    private static final String[][] TITLES={
        {"THE FIRST PROMISE","HOLD THE GATE","THE SHELTER","THE NIGHT WATCH",
         "DORIN'S DEFENSE","THE BUILDER'S OATH","THE LAST CART","BURNING FIELDS",
         "A KNIGHT'S WORD","THORNHIDE'S FINAL STAND"},
        {"THE WHISPERED ROAD","THE MOONLIT GUARD","THE FIRST LANTERN","WHISPER HUNTERS",
         "THE UNBROKEN LINE","MIRA'S WATCH","THE LAST LANTERN","TREEBOUND VIGIL",
         "PATH OF REMEMBRANCE","THE HOLLOW GUARDIAN"},
        {"THE STONE CAUSEWAY","POISONED DAWN","THE ANTIDOTE","DROWNED VOICES",
         "HEALER'S OATH","BROTHER HALE'S WATCH","THE MARSH SUPPLIES","SWAMP OUTRIDERS",
         "SHELTER ABOVE WATER","LEVIATHAN'S SORROW"},
        {"THE FIRST ASH","EMBERS AT THE GATE","THE OLD FORGE","CANYON RAIDERS",
         "FIRES THAT REMEMBER","GARRICK'S OATH","THE LAST ANVIL","ASHES OF DUTY",
         "THE WARDEN'S HILT","ASHFANG'S FINAL FIRE"},
        {"THE FROZEN WATCH","PASS OF THE LOST","THE LAST BEACON","WHITEHORN'S SHADOW",
         "TESSA'S PROMISE","THE CAPTAIN'S OATH","THE WINTER SUPPLY LINE","SILENT SNOW",
         "A CUP LEFT EMPTY","THE GIANT'S LAMENT"},
        {"GATES OF IRON","ENGINEER'S GAMBIT","THE FOUNDRY","RUSTED BATTALIONS",
         "PELL'S MACHINE","THE LAST ENGINE","THE POWER CORE","MARCH OF STEEL",
         "A FORGE REBORN","IRONFALL'S LAST TYRANT"},
        {"SHARDS OF MEMORY","THE CRYSTAL VIGIL","THE REFLECTED SHRINE","MIRROR HUNTERS",
         "LUME'S PRAYER","THE HIDDEN PATTERN","THE SECOND SHRINE","SHATTERED ECHOES",
         "THE TRUE REFLECTION","THE CRYSTAL WRAITH"},
        {"THE FIRST THUNDER","STORMS AT THE WALL","THE CONDUCTOR","LIGHTNING RAID",
         "NESSA'S PROMISE","EYE OF THE STORM","THE BROKEN MAST","WIND OF RETURN",
         "STORMS OF MEMORY","THE TEMPEST KING"},
        {"THE SHADOW GATE","INTO THE DARK","THE WARD OF SHADOWS","LAST FOOTSTEPS",
         "THE CAPTAIN'S CHOICE","A NAME REMEMBERED","THE NIGHT WARD","ECHO HUNTERS",
         "THE FIRST WARDEN","THE SHADOW REVENANT"},
        {"A BROKEN SUN","THE FINAL WATCH","THE LAST SEAL","RIFTBOUND LEGIONS",
         "TEN WORLDS REMEMBER","A CUP FOR CAEL","THE ECLIPSE WARD","WARDEN AGAINST WARDEN",
         "THE WORLD'S LAST LIGHT","THE GATE ASPECT"}
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
        int type=key==9?ELITE:key==2||key==6?RELIC:key==4?NO_LOSS:
                key==5?BUILD:key%4;
        int requirement=type==CORE?Math.max(60,85-world*2):
                type==HERO?Math.max(30,55-world):
                type==BUILD?2+world/5:type==DEFEAT?Math.min(40,12+world*3):
                type==NO_LOSS?1:1;
        String description;
        switch(type){
            case CORE:description="Keep the fortress core above "+requirement+"% HP at victory.";break;
            case HERO:description="Win with the Warden above "+requirement+"% HP.";break;
            case BUILD:description="Build "+requirement+" new defenses during the battle.";break;
            case DEFEAT:description="Defeat at least "+requirement+" invaders before victory.";break;
            case NO_LOSS:description="Win without allowing any defensive tower to be destroyed.";break;
            case RELIC:description="Defend the "+RELICS[world]+" until the final wave.";break;
            default:description="Defeat the world boss while the fortress core survives.";break;
        }
        return new Objective(stage,type,requirement,TITLES[world][key],description);
    }
    public static String relicName(int stage){
        return RELICS[Campaign.world(stage)];
    }
    public static boolean evaluate(Objective obj,int built,int killed,
                                   float core,float hero,boolean elite){
        return evaluate(obj,built,killed,core,hero,elite,0f,0);
    }
    public static boolean evaluate(Objective obj,int built,int killed,
                                   float core,float hero,boolean elite,
                                   float relicFraction,int towersDestroyed){
        if(obj==null)return false;
        switch(obj.type){
            case CORE:return core>=obj.requirement/100f;
            case HERO:return hero>=obj.requirement/100f;
            case BUILD:return built>=obj.requirement;
            case DEFEAT:return killed>=obj.requirement;
            case ELITE:return elite&&core>0;
            case RELIC:return relicFraction>0;
            case NO_LOSS:return towersDestroyed==0&&core>0;
            default:return false;
        }
    }
}