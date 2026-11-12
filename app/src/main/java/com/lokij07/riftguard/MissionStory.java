package com.lokij07.riftguard;

/**
 * Authored, offline campaign briefings that communicate the stakes before play.
 * Deliberately separate from the 300-stage battle simulation and unlock data.
 */
public final class MissionStory {
    private MissionStory() {}
    private static final String[] REGION_LORE={
        "The outer villages call for aid as the first rifts open in the green frontier.",
        "Whispers move between ancient trees. Every lantern gone dark means another lost road.",
        "The marsh breathes venom. Survivors cling to the last stone causeways.",
        "Ash falls over the canyon forts while ember beasts gather below the cliffs.",
        "Winter has sealed the mountain gates. The watch must hold against frozen hordes.",
        "The iron foundries are silent, but something is building armies beneath their roofs.",
        "Crystals pulse through old caverns, drawing creatures from the depths.",
        "Thunder tears the island bridges apart. The defenders fight between storms.",
        "A fallen kingdom raises its banners again, this time beneath the shadow.",
        "The eclipse has reached the citadel. The final bastion is all that remains."
    };
    private static final String[] ORDERS={
        "Scout the approach. Hold the gate while your defenses uncover the first attack route.",
        "The passage is broken. Adapt your position and survive the dangerous crossing.",
        "The enemy comes in force. Protect the fortress through the final siege."
    };
    private static final String[] WARNINGS={
        "Watch the road. Fast raiders seek weak points in your defenses.",
        "Hazards shift across the battlefield. Move before their warning fades.",
        "The final wave brings an elite siege commander. Save your strongest attacks."
    };

    public static final class Briefing {
        public final String chapter,headline,lore,order,warning,objective;
        private Briefing(String chapter,String headline,String lore,String order,
                         String warning,String objective){
            this.chapter=chapter;this.headline=headline;this.lore=lore;
            this.order=order;this.warning=warning;this.objective=objective;
        }
    }

    public static Briefing forStage(int stage) {
        Campaign.checkStage(stage);
        int world=Campaign.world(stage), map=Campaign.map(stage);
        String chapter="CHAPTER "+(world+1)+"  /  "+Campaign.WORLDS[world].toUpperCase();
        String headline=Campaign.title(stage)+"  -  "+Campaign.MAPS[map].toUpperCase();
        String warning=DifficultyRules.worldBoss(stage)
                ?"WORLD BOSS: "+DifficultyRules.worldBossName(stage)
                :WARNINGS[map];
        String objective="DEFEND YOUR CORE  /  "+
                Campaign.waves(stage)+" WAVES";
        return new Briefing(chapter,headline,REGION_LORE[world],ORDERS[map],
                warning,objective);
    }
}
