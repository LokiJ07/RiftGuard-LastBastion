package com.lokij07.riftguard;

/**
 * Authored chapter scenes strictly grounded in the user's ten-world story draft.
 * This class contains NO invented alternate plot: Tessa is elderly and dies in
 * World Five; Bantay is alive but physically captive until the Eclipse Cave.
 * Companion appearances before rescue are explicitly memories/echoes.
 */
public final class StoryScript {
    private StoryScript(){}
    public static final int WARDEN=0,CAEL=1,TESSA=2,BANTAY=3,
            BOSS=4,TREASURE=5,EMPTY_CUP=6,ALLY=7,CAVE=8;
    public static final class Line {
        public final String speaker,text;
        public final int shot;
        public Line(String speaker,String text){this(speaker,text,WARDEN);}
        public Line(String speaker,String text,int shot){
            this.speaker=speaker;this.text=text;this.shot=shot;
        }
    }
    private static Line l(String speaker,String text,int shot){
        return new Line(speaker,text,shot);
    }
    private static final String[] BOSS_NAMES={
        "THORNHIDE CHIEF","HOLLOW TREANT","BOG LEVIATHAN",
        "ASHFANG COLOSSUS","WHITEHORN GIANT","IRONFALL TYRANT",
        "CRYSTAL WRAITH","TEMPEST KING","SHADOW REVENANT","ECLIPSE GATE ASPECT"
    };
    private static final String[] BOSS_BACKSTORY={
        "Once the beloved chief of Greenhaven, now driven to the Bastion gates by the Rift.",
        "The ancient guardian attacks everything it once tried to protect.",
        "A swamp spirit feeds on the sorrow of drowned travelers.",
        "Once loyal to the canyon forts, this beast burns from within.",
        "The guardian of mountain travelers now has a frozen heart.",
        "A king who made weapons for the Wardens wears a crown of stolen steel.",
        "The voices of fallen Wardens are trapped inside the crystal.",
        "The ruler of the isles sacrificed his bridge-builders to stop the storm.",
        "The First Warden's captain followed Cael into darkness out of loyalty.",
        "The Titan guards the Eclipse Gate. He does not want the Knight inside."
    };
    private static final String[] BOSS_OPEN={
        "A knight with no coffee. How sad.",
        "The trees remember you.",
        "Smell that? Your kettle is down there.",
        "Your sword is in pieces. So are you.",
        "Cold, knight? Good.",
        "Make me an offer. Your sword for your friend.",
        "Look at it. Your blade, shining in my wall.",
        "Hold on to nothing, knight.",
        "You are close. Closer than any Warden before.",
        "Ten seals. Ten bosses. And you still stand."
    };
    private static final String[] KNIGHT_REPLY={
        "Give it back, then.",
        "Good. Then they know I never lose.",
        "Then I am going down.",
        "Chada never breaks twice.",
        "I have a blade shard and a bad temper.",
        "No deal.",
        "Admire it. I am taking it back.",
        "I heard you! Hold on!",
        "Before? How many?",
        "Open the cave."
    };
    private static final String[] BOSS_LAST={
        "Tell them... I tried to hold the gate.",
        "The roots... they remember the First Warden.",
        "The waters... are quiet now.",
        "Fire... stops.",
        "The pass... is safe.",
        "I made it for him. He asked me to hide it.",
        "We all stood where you stand.",
        "I could not save them. Please save yours.",
        "He is not your enemy. He is the Rift's first prisoner.",
        "The gate is broken. The truth waits below."
    };
    private static final String[] ALLY_NAMES={
        "DORIN","MIRA","BROTHER HALE","GARRICK",
        "CAPTAIN YSOLDE","PELL","LUME","NESSA"
    };
    private static final String[] ALLY_JOIN={
        "I could not save the fields. But I can build a wall. Give me a hammer.",
        "Those trees were warning us, Warden. I'll keep watch from your towers.",
        "I kept a Rift shard. I thought studying it would save someone. I was afraid to tell you.",
        "Your sword remembers its owner. The hilt is still warm.",
        "I have fought the Rift longer than you know. I will hold the Bastion for Tessa.",
        "I can rebuild the guns. And the brewing corner. She deserves that much.",
        "The crystals hold more than magic. They remember every Warden who fell.",
        "I can hear him across the storm. Your companion is alive."
    };
    private static final String[] REWARDS={
        "SCORCHED KAPE BEANS","THE OLD KETTLE","KAPE RESTORED",
        "CHADA'S HILT","CHADA'S BLADE SHARD","CHADA'S EDGE",
        "CHADA REFORGED","BANTAY'S COLLAR","BANTAY'S TRAIL","ECLIPSE SEAL"
    };
    private static void check(int world){
        if(world<0||world>=10)throw new IllegalArgumentException("world");
    }
    public static String bossName(int world){check(world);return BOSS_NAMES[world];}
    public static Line[] prologue(String name){
        return new Line[]{
            l("NARRATOR","The Rift opens above the last surviving fortress. The ten regions begin to fall.",CAVE),
            l("OLD TESSA","One cup before the watch, "+name+". You are not defending these walls on an empty stomach.",TESSA),
            l("WARDEN","The kettle is the only thing in this place that still keeps its promises.",WARDEN),
            l("NARRATOR","A dark hand reaches through the Rift. Bantay and the old kettle vanish into Eclipse Cave. Chada shatters in the Knight's grip, its pieces scattering across the ten worlds.",TREASURE),
            l("BANTAY","(A startled bark fades into the Rift. The little dog is gone.)",BANTAY),
            l("TITAN","Come and take them back. If you can reach the cave.",CAEL),
            l("WARDEN","Fine. But when I get there, you're making the coffee.",WARDEN)
        };
    }
    /** Opening chapter scenes play before the first mission of each new world. */
    public static Line[] worldOpening(int world,String name){
        check(world);
        switch(world){
            case 0:return new Line[]{
                l("NARRATOR","Greenhaven's farms are burning. Refugees gather outside the Bastion gate.",CAVE),
                l("OLD TESSA","The gate won't protect them by itself. Go, "+name+". I'll keep the kettle warm.",TESSA),
                l("WARDEN","Keep a cup for me. Preferably one that isn't on fire.",WARDEN)};
            case 1:return new Line[]{
                l("NARRATOR","In Whispering Woods, every extinguished lantern closes another road.",CAVE),
                l("OLD TESSA","Keep one lantern lit for whoever is still finding their way home. And eat before you go.",TESSA),
                l("WARDEN","Something is saying my name in those trees.",WARDEN),
                l("NARRATOR","The branches whisper a warning, not a threat.",BOSS)};
            case 2:return new Line[]{
                l("NARRATOR","Venom Marsh swallows roads, travelers, and the sound of their screams.",CAVE),
                l("WARDEN","A kettle is worth a swamp. I hope.",WARDEN)};
            case 3:return new Line[]{
                l("NARRATOR","Ember Canyon is burning. Chada's broken hilt lies somewhere in the ash.",CAVE),
                l("OLD TESSA","Bring Chada home in pieces if you must. I kept your chair beside the fire.",TESSA),
                l("WARDEN","I can manage without a sword. I would rather not.",WARDEN)};
            case 4:return new Line[]{
                l("NARRATOR","Frostpeak's blizzard has sealed the last supply pass.",CAVE),
                l("OLD TESSA","Hold the pass. We will hold the home.",TESSA),
                l("WARDEN","I'll be back before the kape goes cold.",WARDEN)};
            case 5:return new Line[]{
                l("NARRATOR","The Ironfall forges have started building armies for the Rift.",CAVE),
                l("WARDEN","I should have been there when the Bastion fell silent.",WARDEN)};
            case 6:return new Line[]{
                l("NARRATOR","In Crystal Depths, every reflective wall wears the faces of dead Wardens.",CAVE),
                l("WARDEN","Then they can watch me finish their work.",WARDEN)};
            case 7:return new Line[]{
                l("NARRATOR","A faint bark cuts through the storm over the islands.",CAVE),
                l("BANTAY","(A distant bark carried by the wind. Bantay is still captive.)",BANTAY),
                l("WARDEN","I heard you! Hold on!",WARDEN)};
            case 8:return new Line[]{
                l("NARRATOR","In Shadowfall, the fallen First Warden's court is waiting.",CAVE),
                l("WARDEN","All the things I fought through. I think I'm finally scared.",WARDEN)};
            default:return new Line[]{
                l("NARRATOR","The eclipse reaches the Citadel. Every ally returns to defend the Last Bastion.",CAVE),
                l("WARDEN","One more gate. Everyone, hold your ground.",WARDEN),
                l("TITAN","You cannot keep them all safe. Nobody can.",CAEL)};
        }
    }
    public static Line[] beforeWorldBoss(int world){
        check(world);
        if(world==7)return new Line[]{
            l("NARRATOR",BOSS_BACKSTORY[world],BOSS),
            l("BANTAY","(A distant bark. The Companion is not physically here.)",BANTAY),
            l("WARDEN","I heard you! Hold on!",WARDEN),
            l("TITAN",BOSS_OPEN[world],CAEL)
        };
        return new Line[]{
            l("NARRATOR",BOSS_BACKSTORY[world],BOSS),
            l("TITAN",BOSS_OPEN[world],CAEL),
            l("WARDEN",KNIGHT_REPLY[world],WARDEN)
        };
    }
    public static Line[] afterWorldBoss(int world){
        check(world);
        if(world==4)return new Line[]{
            l("WHITEHORN GIANT",BOSS_LAST[world],BOSS),
            l("NARRATOR","While the Warden fights at Frostpeak, a Rift breaches the Last Bastion.",CAVE),
            l("OLD TESSA","Get everyone inside. I will hold this gate.",TESSA),
            l("NARRATOR","Old Tessa shields the wounded until the last defender can escape. She does not return.",TESSA),
            l("NARRATOR","The Warden comes home to an unlit fire. Tessa's cup stands empty on the wall.",EMPTY_CUP),
            l("TITAN","Now you know what I know.",CAEL),
            l("WARDEN","...",WARDEN),
            l("CAPTAIN YSOLDE",ALLY_JOIN[world],ALLY),
            l("WARDEN","Then stand with me. For her.",WARDEN),
            l("NARRATOR",REWARDS[world]+" RECOVERED. The Bastion keeps Tessa's corner untouched.",TREASURE)
        };
        if(world==9)return new Line[]{
            l("ECLIPSE GATE ASPECT",BOSS_LAST[world],BOSS),
            l("NARRATOR","Dorin, Mira, Hale, Garrick, Ysolde, Pell, Lume and Nessa secure the final gate.",ALLY),
            l("TITAN","You have reached every seal. You will still have to face me alone.",CAEL),
            l("WARDEN","Not alone. Not anymore.",WARDEN),
            l("NARRATOR","The Eclipse Seal is restored. Beneath the Citadel, the quiet Eclipse Cave opens.",CAVE)
        };
        // Shadowfall's ally is the Warden's OWN SHADOW for a single chapter,
        // not a ninth permanent recruit. Avoid indexing the eight ally arcs.
        if(world==8)return new Line[]{
            l("SHADOW REVENANT",BOSS_LAST[world],BOSS),
            l("NARRATOR","The captain of the First Warden lowers his weapon and falls into the shadow.",BOSS),
            l("TITAN","How many Wardens came before you? Enough.",CAEL),
            l("NARRATOR","For one night, the Warden's own shadow fights beside him. It does not follow him home.",ALLY),
            l("WARDEN","I am scared.",WARDEN),
            l("NARRATOR","The Bastion's fire burns beside Tessa's untouched cup. Nobody laughs.",EMPTY_CUP),
            l("NARRATOR",REWARDS[world]+" RECOVERED. Bantay's trail now leads toward the Citadel.",TREASURE)
        };
        Line boss=l(BOSS_NAMES[world],BOSS_LAST[world],BOSS);
        Line ally=l(ALLY_NAMES[world],ALLY_JOIN[world],ALLY);
        Line reward=l("NARRATOR",REWARDS[world]+" RECOVERED. A defender joins the Last Bastion.",TREASURE);
        switch(world){
            case 0:return new Line[]{
                boss,ally,reward,
                l("OLD TESSA","The beans are scorched. Let me try the kettle.",TESSA),
                l("WARDEN","It tastes like smoke.",WARDEN),
                l("OLD TESSA","Then bring me better beans.",TESSA)};
            case 1:return new Line[]{
                boss,ally,reward,
                l("NARRATOR","An old memory: Bantay once curled around Tessa's kettle by the Bastion wall.",BANTAY),
                l("WARDEN","That is not a bed.",WARDEN)};
            case 2:return new Line[]{
                boss,ally,reward,
                l("OLD TESSA","The kape is back. The real one. Here.",TESSA),
                l("WARDEN","Ahh. That is the good stuff.",WARDEN),
                l("OLD TESSA","Salt, not sugar, in the first cup. Trust me.",TESSA),
                l("WARDEN","Salt? In coffee?",WARDEN),
                l("OLD TESSA","Humor an old woman.",TESSA),
                l("TITAN","Do not get used to it.",CAEL)};
            case 3:return new Line[]{
                boss,ally,reward,
                l("GARRICK","The hilt is warm. Like it remembers you.",ALLY),
                l("WARDEN","I remember it too.",WARDEN)};
            case 5:return new Line[]{
                boss,ally,reward,
                l("NARRATOR","Pell hangs a new brewing pot where Tessa once worked.",EMPTY_CUP),
                l("WARDEN","She would have hated that pot.",WARDEN),
                l("PELL","I know. There was a note inside her old pot: 'Salt, not sugar. Trust me.'",ALLY),
                l("WARDEN","...Pour it.",WARDEN),
                l("NARRATOR","The Warden takes the first sip since Tessa died. For a moment, the Bastion feels like home.",EMPTY_CUP)};
            case 6:return new Line[]{
                boss,ally,reward,
                l("NARRATOR","Chada is whole again. The Rift screams as the Knight draws the sword.",TREASURE),
                l("BANTAY","(A bark answers from far beneath the Citadel.)",BANTAY),
                l("WARDEN","I heard that.",WARDEN)};
            case 7:return new Line[]{
                boss,ally,reward,
                l("WARDEN","His collar. Bantay is alive.",WARDEN),
                l("NESSA","I can hear him. He is waiting for you.",ALLY)};
            default:return new Line[]{
                boss,
                l("TITAN","How many Wardens came before you? Enough.",CAEL),
                l("NARRATOR","The Companion's trail winds into the dark beneath the Citadel.",TREASURE),
                l("WARDEN","I am scared.",WARDEN),
                l("NARRATOR","The fire burns beside Tessa's empty cup. Nobody laughs.",EMPTY_CUP)};
        }
    }
    public static Line[] eclipseCaveOpening(String name){
        return new Line[]{
            l("NARRATOR","The final seal breaks. The cave beyond is silent.",CAVE),
            l("NARRATOR","A full cup of kape and a living Bantay wait beside a shrine built by the First Warden. The Knight still carries the reforged Chada.",TREASURE),
            l("NARRATOR","There is an empty chair at the shrine. No one has sat there for years.",EMPTY_CUP),
            l("TITAN","Ten seals. Ten worlds. You really did it.",CAEL),
            l("WARDEN","Where is my friend?",WARDEN),
            l("TITAN","Safe. Safer than you will be.",CAEL),
            l("WARDEN","Then stop talking.",WARDEN)
        };
    }
    public static Line[] eclipsePhaseTwo(){
        return new Line[]{
            l("NARRATOR","At half strength, the Titan lowers his weapon and stops attacking.",BOSS),
            l("TITAN","I was a Warden too, you know.",CAEL),
            l("WARDEN","Then you know I will not stop.",WARDEN)
        };
    }
    public static Line[] eclipsePhaseThree(){
        return new Line[]{
            l("NARRATOR","His armor breaks open. Behind the Titan stands Cael, the First Warden.",BOSS),
            l("CAEL","I only wanted to keep them safe...",CAEL),
            l("WARDEN","Then let them come home.",WARDEN)
        };
    }
    public static Line[] eclipseEnding(int ending){
        if(ending==StoryProgress.ENDING_TRUE)return new Line[]{
            l("CAEL","I only wanted to keep them safe...",CAEL),
            l("BANTAY","(Bantay runs to the Knight and buries his head against the armor.)",BANTAY),
            l("WARDEN","There you are. Kape time.",WARDEN),
            l("CAEL","...Save a cup for me.",CAEL),
            l("NARRATOR","The Rift closes. The Knight, Bantay and Cael rest on the Bastion wall beside Tessa's cup.",EMPTY_CUP)
        };
        if(ending==StoryProgress.ENDING_PARTIAL)return new Line[]{
            l("BANTAY","(Bantay rushes to the Knight and refuses to let go.)",BANTAY),
            l("WARDEN","We are leaving. Both of us.",WARDEN),
            l("CAEL","The Rift is not done with you. Neither am I.",CAEL),
            l("NARRATOR","Behind them, Cael's broken Titan armor stirs. Another Rift opens beyond a distant world.",CAVE)
        };
        return new Line[]{
            l("NARRATOR","Chada ends the Titan. The Rift closes.",CAVE),
            l("NARRATOR","The Knight brings his kape, Chada and Bantay home. Cael dies alone.",TREASURE),
            l("WARDEN","No one should drink alone.",WARDEN),
            l("NARRATOR","The Warden pours a second cup each morning. No one ever drinks it.",EMPTY_CUP)
        };
    }
    /**
     * First-clear mid-chapter moments: one tiny skippable scene every three
     * campaign levels, never a combat objective or replay requirement.
     * Characters only speak after they could have joined the Bastion;
     * Tessa never appears in a present-day scene after Frostpeak (world 5).
     */
    public static Line[] campBanter(int level,String name){
        if(level<1||level>100)throw new IllegalArgumentException("level");
        if(level%3!=0)return new Line[0];
        int world=(level-1)/10;
        switch(world){
            case 0:return new Line[]{
                l("OLD TESSA","You kept the gate standing, "+name+". Have you eaten anything today?",TESSA),
                l("WARDEN","Does coffee count? No? I was afraid you would say that.",WARDEN)};
            case 1:return new Line[]{
                l("OLD TESSA","A single lantern cannot light the whole forest. But it can lead one traveler home.",TESSA),
                l("WARDEN","Then we leave another burning for the next traveler.",WARDEN)};
            case 2:return new Line[]{
                l("OLD TESSA","Being afraid is not the same as giving up, you know.",TESSA),
                l("WARDEN","Good. I have been afraid since the first bridge.",WARDEN)};
            case 3:return new Line[]{
                l("OLD TESSA","Chada can break. The promise you made with her cannot.",TESSA),
                l("WARDEN","Then I will carry the pieces until I can put her back together.",WARDEN)};
            case 4:return new Line[]{
                l("OLD TESSA","If the snow closes the road, come home slowly. I will keep the fire lit.",TESSA),
                l("WARDEN","I am saving a seat beside that fire.",WARDEN)};
            case 5:return new Line[]{
                l("CAPTAIN YSOLDE","I have kept the fire burning by Tessa's empty chair.",ALLY),
                l("WARDEN","Good. A home is more than the walls we can still defend.",WARDEN)};
            case 6:return new Line[]{
                l("PELL","The old brewing pot is warm again. I think Tessa would have scolded my soldering.",ALLY),
                l("WARDEN","She would have. Then she would have poured us both a cup.",WARDEN)};
            case 7:return new Line[]{
                l("LUME","These crystals remember footsteps, voices, and people we thought lost.",ALLY),
                l("WARDEN","Then listen for Bantay. I know he is still out there.",WARDEN)};
            case 8:return new Line[]{
                l("NESSA","Bantay's trail does not end in the storm. It turns toward the old citadel.",ALLY),
                l("WARDEN","I am coming, old friend. Keep barking.",WARDEN)};
            default:return new Line[]{
                l("NESSA","Everyone is at their post. The Bastion is holding because they chose to stay.",ALLY),
                l("WARDEN","Then I choose to come back to them.",WARDEN)};
        }
    }
    public static Line[] memory(KeyMemories.Memory m){
        if(m==null)return null;
        return new Line[]{l(m.fromCael?"CAEL'S MEMORY":"WARDEN'S MEMORY",m.text,
                m.fromCael?CAEL:BANTAY)};
    }
}
