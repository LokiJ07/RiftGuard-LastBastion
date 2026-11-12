package com.lokij07.riftguard;

/** Save-backed visible forms, never a separate cosmetic unlock currency. */
public final class UpgradeEvolution {
    private UpgradeEvolution(){}
    public static final String[] FORMS={"BASE","FORGED","REINFORCED","RUNIC","LEGENDARY"};
    public static final String[] DEFENSES={"BASIC BALLISTA","ARMORED BALLISTA",
            "THUNDER LANCE","ARC BASTION","GUARDIAN BEAM"};
    public static int weaponForm(WeaponMastery mastery,int weapon){
        WeaponMastery.node(weapon,0);
        int tier=0;
        for(int id=1;id<WeaponMastery.NODES_PER_WEAPON;id++)
            if(mastery.owned(weapon,id))
                tier=Math.max(tier,WeaponMastery.node(weapon,id).tier);
        return Math.max(0,Math.min(4,tier));
    }
    public static int fortressForm(int rank,AscensionTree ascension){
        if(rank<1||rank>Progression.MAX_RANK)throw new IllegalArgumentException("rank");
        int defenseNodes=towerNodes(ascension);
        return Math.min(4,Math.max(rank-1,Math.min(4,defenseNodes+1)));
    }
    public static int towerNodes(AscensionTree ascension){
        int count=0;
        for(AscensionTree.Node n:AscensionTree.NODES)
            if(n.category==AscensionTree.TOWER && ascension.owned(n.id))count++;
        return count;
    }
    /** Both new fortress-exclusive defenses are earned by the EXISTING tree. */
    public static boolean thunderLance(int rank,AscensionTree ascension){
        return rank>=3&&towerNodes(ascension)>=1;
    }
    public static boolean guardianBeam(int rank,AscensionTree ascension){
        return rank>=5&&towerNodes(ascension)>=2;
    }
    public static int heroArmor(AscensionTree ascension){
        int tier=0;
        for(AscensionTree.Node n:AscensionTree.NODES)
            if(n.category==AscensionTree.HERO && ascension.owned(n.id))
                tier=Math.max(tier,Math.min(4,(n.tier+1)/2));
        return tier;
    }
    public static int skillAura(AscensionTree ascension){
        int tier=0;
        for(AscensionTree.Node n:AscensionTree.NODES)
            if(n.category==AscensionTree.SKILL && ascension.owned(n.id))
                tier=Math.max(tier,Math.min(4,(n.tier+1)/2));
        return tier;
    }
}
