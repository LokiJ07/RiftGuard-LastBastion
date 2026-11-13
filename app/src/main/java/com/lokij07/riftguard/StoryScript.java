package com.lokij07.riftguard;

/** Authored opening, world-boss and memory scenes based on supplied story draft. */
public final class StoryScript {
    private StoryScript(){}
    public static final class Line {
        public final String speaker,text;
        public Line(String speaker,String text){this.speaker=speaker;this.text=text;}
    }
    private static Line l(String s,String t){return new Line(s,t);}
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
        "Give it back, then.","Good. Then they know I never lose.",
        "Then I am going down.","Chada never breaks twice.",
        "I have a blade shard and a bad temper.","No deal.",
        "Admire it. I am taking it back.","I heard you! Hold on!",
        "Before? How many?","Open the cave."
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
    public static Line[] prologue(String name){
        return new Line[]{
            l("NARRATOR","The Rift opened over the Last Bastion. Three treasures vanished into the dark."),
            l("TESSA","Hold this cup, "+name+". Whatever happens, drink it while it is hot."),
            l("WARDEN","My kape. Chada. Bantay. I am bringing them home."),
            l("CAEL","Come and take them back... if you can reach the cave.")
        };
    }
    public static Line[] beforeWorldBoss(int world){
        return new Line[]{l("TITAN",BOSS_OPEN[world]),l("WARDEN",KNIGHT_REPLY[world])};
    }
    public static Line[] afterWorldBoss(int world){
        if(world==4)return new Line[]{
            l("WHITEHORN GIANT",BOSS_LAST[world]),
            l("NARRATOR","The Warden returns to the Bastion. Tessa's cup sits empty on the wall."),
            l("TITAN","Now you know what I know."),
            l("WARDEN","...Keep the kettle warm.")
        };
        if(world==9)return new Line[]{
            l("ECLIPSE GATE ASPECT",BOSS_LAST[world]),
            l("WARDEN","Cael. I am coming."),
            l("NARRATOR","The tenth seal resonates. Beyond the gate, the Eclipse Cave awakens.")
        };
        return new Line[]{
            l(Campaign.WORLDS[world].toUpperCase()+" GUARDIAN",BOSS_LAST[world]),
            l("TITAN",world<3?"The Rift remembers every victory.":world<7?
                "How many more must I bury?":"Do not leave me alone."),
            l("WARDEN",world<4?"One world closer.":world<7?
                "I will stand for them.":"I will find the truth.")
        };
    }
    public static Line[] memory(KeyMemories.Memory m){
        if(m==null)return null;
        return new Line[]{l(m.fromCael?"CAEL'S MEMORY":"WARDEN'S MEMORY",m.text)};
    }
}
