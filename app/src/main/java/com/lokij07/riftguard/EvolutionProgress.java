package com.lokij07.riftguard;

/**
 * Canonical, SAVE-DERIVED evolution tiers.
 *
 * Never store duplicate cosmetic unlock flags: weapon forms come from the
 * existing weapon.mastery.*.v1 bitsets and fortress/hero/skill branches come
 * from ascension.purchased.v1. Their appearance survives a normal APK update.
 * Every rendered form is earned by an actual owned node, not the selected node.
 */
public final class EvolutionProgress {
    private EvolutionProgress(){}

    public static final int FORMS=5;

    /** Highest *owned* node tier (0..4), not number of clicks or a preview. */
    public static int weaponForm(WeaponMastery mastery,int weapon){
        if(mastery==null)throw new IllegalArgumentException("mastery");
        if(weapon<0||weapon>=Progression.WEAPONS.length)
            throw new IllegalArgumentException("weapon");
        int reached=0;
        for(int id=1;id<WeaponMastery.NODES_PER_WEAPON;id++){
            if(mastery.owned(weapon,id))
                reached=Math.max(reached,WeaponMastery.node(weapon,id).tier);
        }
        return reached;
    }
    /** Number of actually purchased ascension nodes of a given category. */
    public static int ownedCategory(AscensionTree ascension,int category){
        if(ascension==null||category<0||category>=AscensionTree.CATEGORIES.length)
            throw new IllegalArgumentException("ascension/category");
        int owned=0;
        for(AscensionTree.Node node:AscensionTree.NODES)
            if(node.category==category&&node.id!=0&&ascension.owned(node.id))
                owned++;
        return owned;
    }
    /** Fortress armament unlocks depend on actual TURRET nodes AND base rank. */
    public static int fortressArsenal(AscensionTree ascension,int fortressRank){
        if(fortressRank<1||fortressRank>Progression.MAX_RANK)
            throw new IllegalArgumentException("rank");
        int acquired=ownedCategory(ascension,AscensionTree.TOWER);
        if(fortressRank>=5&&acquired>=4)return 4;
        if(fortressRank>=4&&acquired>=3)return 3;
        if(fortressRank>=3&&acquired>=2)return 2;
        if(fortressRank>=2&&acquired>=1)return 1;
        return 0;
    }
    public static int armorForm(AscensionTree ascension,int fortressRank){
        int heroes=ownedCategory(ascension,AscensionTree.HERO);
        int attacks=ownedCategory(ascension,AscensionTree.ATTACK);
        return Math.min(4,Math.max(0,(fortressRank-1)/2+
                Math.min(2,heroes)+(attacks>=3?1:0)));
    }
    /** Other tree branches each receive a visible purchased reward. */
    public static int coreForm(AscensionTree ascension){
        return Math.min(4,ownedCategory(ascension,AscensionTree.CORE));
    }
    public static int treasuryForm(AscensionTree ascension){
        return Math.min(4,ownedCategory(ascension,AscensionTree.GOLD));
    }
    public static int speedForm(AscensionTree ascension){
        return Math.min(4,ownedCategory(ascension,AscensionTree.SPEED));
    }
    public static int skillForm(AscensionTree ascension,int fortressRank){
        int skillNodes=ownedCategory(ascension,AscensionTree.SKILL);
        return Math.min(4,Math.max(0,skillNodes+
                (fortressRank>=4?1:0)));
    }
    public static String formName(int weapon,int tier){
        if(weapon<0||weapon>=6||tier<0||tier>=FORMS)
            throw new IllegalArgumentException("weapon/tier");
        return NAMES[weapon][tier];
    }
    private static final String[][] NAMES={
            {"Scout Recurve","Ranger Bow","Warhorn Crossbow",
                    "Phantom Arc Bow","Eclipse Longbow"},
            {"Iron Sword","Steel Greatsword","Oathbreaker Blade",
                    "Royal Cleaver","Titan Ruinblade"},
            {"Ember Saber","Molten Falchion","Flamebrand",
                    "Phoenix Talon","Inferno Sovereign"},
            {"Oak Channeler","Runed Staff","Arc Scepter",
                    "Storm Spire","Tempest Crown"},
            {"Scout Repeater","Double Rail","Siege Arbalest",
                    "Drum Battery","Thunder Repeater"},
            {"Frost Spear","Glacier Pike","Crystal Lance",
                    "Winter Trident","Absolute-Zero Lance"}
    };
    public static String fortressWeapon(int arsenal){
        switch(arsenal){
            case 0:return "Timber Ballista";
            case 1:return "Ironbolt Battery";
            case 2:return "Ember Mortar";
            case 3:return "Storm Conductor";
            case 4:return "Guardian Prism";
            default:throw new IllegalArgumentException("arsenal");
        }
    }
}
