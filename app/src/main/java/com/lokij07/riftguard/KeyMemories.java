package com.lokij07.riftguard;

/**
 * DRAFT: 100 key memories. World w (0..9) x key k (0..9).
 * Owner 'K' = the Knight's memory, 'C' = Cael's memory (the First Warden / Titan).
 * A key is earned only on an optional, successful Last Stand Memory Trial.
 * Level ten of each world is the boss trial. Missed trials remain replayable.
 *
 * Usage:  KeyMemories.Memory m = KeyMemories.forStage(stage);
 *         if (m != null) { show m.text (tint differently when m.fromCael) }
 */
public final class KeyMemories {
    private KeyMemories() {}

    public static final class Memory {
        public final boolean fromCael;
        public final String text;
        private Memory(boolean fromCael, String text) { this.fromCael = fromCael; this.text = text; }
    }
    private static Memory K(String t) { return new Memory(false, t); }
    private static Memory C(String t) { return new Memory(true, t); }

    public static final Memory[][] ALL = {
        // ---- World 1: Greenhaven Frontier (the Knight's beginnings) ----
        {
            K("A starving pup in the Greenhaven fields. You split your last bread and he never left."),
            K("The gate guards said no animals. You said no Knight, then."),
            K("Tessa let the pup in 'if he carries his own weight.' He carried one boot."),
            K("The first time you drew Chada, over a wolf in a farmyard. Your hands would not stop shaking."),
            K("Dorin's father showed you how to mend a fence. 'A wall is just patience.'"),
            K("Harvest festival. Your Companion stole an entire roast chicken and shared none."),
            K("Night watch. You noticed the Rift hums when everything else is quiet."),
            K("The elder laughed: 'The Rift is only a story.' The first rift opened the next morning."),
            K("The last peaceful evening. You burned your first pot of kape and Tessa laughed until she cried."),
            K("The Greenhaven chief raised a cup at the gate: 'To the Wardens. We sleep because you stand.'")
        },
        // ---- World 2: Whispering Woods ----
        {
            K("Lost in the woods as a child, you followed one lantern home. Someone always lit them."),
            K("Tessa taught you to roast beans over a fire. 'Slow. Everything good is slow.'"),
            K("Mira's mother said the trees keep the names of everyone who walks beneath them."),
            K("Your Companion chased a fox. Both came back muddy, and both looked proud."),
            K("Your first cup with Tessa on the Bastion wall. You said it was bitter. You asked for another."),
            C("Warden Cael planted a tree at the Bastion gate. 'Something that will outlast me.'"),
            K("The Treant spoke to you once when you were small. You told everyone it was a dream."),
            K("The lantern keeper's last words: 'Keep one lit, even if nobody is coming.'"),
            K("A road you walked a hundred times is gone. The trees closed over it like it never existed."),
            K("A whisper in the leaves: 'Come home, Knight.' It was not the trees.")
        },
        // ---- World 3: Venom Marsh (fear) ----
        {
            K("You were afraid of water as a child. Tessa carried you across the river and told no one."),
            K("Brother Hale tended your first real wound. His hands shook more than yours."),
            K("A causeway stone gave way. Your Companion pulled you out by the cloak."),
            K("You brewed kape with swamp water. It was awful. Tessa laughed for an hour."),
            K("You promised yourself you would never run. You ran once. You came back."),
            C("From Cael's journal: 'The Rift grows when we are afraid. I am always afraid.'"),
            K("Drowned travelers' lanterns still glow under the water, pointing the way out."),
            K("Hale confessed he kept a Rift shard. 'I was afraid of being useless.'"),
            K("The Warden before you said as he handed over Chada: 'Be afraid. Fight anyway.'"),
            K("Your first night as Warden. Alone on the wall, afraid, and the Companion would not leave your side.")
        },
        // ---- World 4: Ember Canyon (duty, Chada) ----
        {
            K("Training with a wooden sword, you hit everything but the target."),
            K("Garrick forged his first nail and kept it. 'Every sword starts as something small.'"),
            K("The day you were knighted. Chada was placed in your hands and it felt heavier than a mountain."),
            K("You named her Chada because she made a bad day feel brave."),
            C("Cael carried Chada's twin into his first battle. It never left his side. Until it did."),
            K("The Colossus once carried supplies to the canyon forts. He let you ride on his back."),
            K("You broke a training dummy in anger. Tessa handed you coffee instead of a lecture."),
            K("Ashes fell on Tessa's coffee pot. She wiped it clean and kept it on the fire."),
            C("Cael, to a young soldier: 'A sword only matters if someone is waiting for you.'"),
            K("You promised your Companion: 'I will always come back.'")
        },
        // ---- World 5: Frostpeak Mountains (midpoint) ----
        {
            K("The first snow. Your Companion did not know what it was and ate it."),
            K("Captain Ysolde's lesson: 'The Rift counts the ones you could not save. Do not count with it.'"),
            K("The Giant carried lost travelers over the pass, one by one, every winter."),
            K("Tessa sent a flask of kape up the mountain. 'For the Warden who forgets to eat.'"),
            K("You gave your last kape to a frozen soldier. He lived. You never learned his name."),
            C("Cael found a frozen calf in the pass and carried it down inside his cloak."),
            K("You told Tessa the snow was too deep to come back. She said, 'Then come back slowly.'"),
            C("Cael wrote: 'The cold is not the worst part. The worst part is nobody asking if I am cold.'"),
            K("A note from Tessa tucked into your pack: 'Come home soon. The kettle is on.'"),
            C("A Warden in old armor shared kape with his Companion in the snow. You know the voice. It is the Titan's.")
        },
        // ---- World 6: Ironfall Ruins (grief) ----
        {
            K("Tessa's kitchen. The smell of beans and woodsmoke. You can almost go back."),
            K("Pell's first turret fell apart. Tessa said, 'Build it again. Better.'"),
            K("You cannot drink kape since she died. The cup sits there and goes cold."),
            K("The Tyrant was once a king who armed every Warden fairly, rich or poor."),
            C("Cael shook the king's hand: 'Forge me a twin to Chada. I will carry both.'"),
            K("Found in her pot, a note in Tessa's handwriting: 'Salt, not sugar. Trust me.'"),
            C("In the workshop, young Cael forged Chada's twin and laughed when it came out crooked."),
            C("Cael asked the king: 'If I ever change, hide what I love from me.'"),
            K("You realize why grief is so heavy. It is only love with nowhere to go."),
            K("You drink the first cup since Tessa. With salt. It is the best thing you have ever tasted.")
        },
        // ---- World 7: Crystal Depths (the Wardens fall) ----
        {
            C("Warden Aldric stood at the Rift and did not come back. Cael kept his lantern."),
            C("Warden Sione fell. Her Companion carried her blade home alone."),
            K("You see your reflection wearing the Titan's helmet. You look away."),
            C("Warden Brannoch laughed all the way to the end. Cael has never forgiven him."),
            C("Two Wardens held the line together. Only one walked out."),
            C("Cael carved every fallen Warden's name into the Bastion wall. The wall has no room left."),
            K("Lume said, 'Crystals remember everyone who touched them. They remember you.'"),
            C("Cael: 'How many more must I bury? How many more?'"),
            C("The Rift offered Cael a trade: 'Give us what you love and we will leave the rest alone.' He said no. He thought."),
            C("All the Wardens before you fell one by one. The last one standing was Cael.")
        },
        // ---- World 8: Stormbound Isles (hope, the Companion) ----
        {
            K("Your Companion's first storm. He hid under your cloak and refused to come out."),
            C("Cael's Companion learned to swim to the Bastion's far shore, just to meet him."),
            K("Nessa taught you knots. 'A bridge is only trust, tied tight.'"),
            K("A rainy day at the Bastion. You and your Companion sharing one roof and one blanket."),
            K("The bridge-builders sang while they worked. You still hum the song."),
            C("Cael's Companion waited at the door every evening, even the evenings he did not come home."),
            K("Your Companion barked at thunder, then at you for laughing."),
            K("The Tempest King's last order: 'Bridges first. Always bridges first.'"),
            C("Cael kept his Companion's collar on the wall by the door. He never moved it."),
            K("You promised: 'I am coming. Whatever it takes.'")
        },
        // ---- World 9: Shadowfall Kingdom ----
        {
            C("The court of the First Warden. Music, kape, and a banquet table with room for everyone."),
            C("The captain knelt: 'My sword is yours, Warden. Even where you cannot take it.'"),
            K("Your shadow copied every move you made, a half-second late."),
            C("Cael taught his captain how to make kape. The captain burned it every time."),
            C("The Rift whispered to Cael for the first time: 'You are so tired. We can carry it.'"),
            C("Cael began to hear voices. He told no one but his Companion."),
            K("Your shadow stood beside you in the dark. For once it was on your side."),
            C("The captain followed Cael into the Rift. 'Where you go, I go.'"),
            C("Cael could not sleep. He counted his Companion's breaths instead of sheep."),
            C("Cael's last day as Warden. He said goodbye to his Companion and hid it deep in the cave. 'So the Rift cannot find you.'")
        },
        // ---- World 10: Eclipse Citadel (truth) ----
        {
            K("The night the Rift opened above the Bastion. You stood on the wall and could not move."),
            K("Tessa pressed a cup into your hands. 'Whatever happens, drink it while it is hot.'"),
            K("The Titan reached across the Bastion. Three things vanished: kape, sword, and friend."),
            K("Dorin, Mira, Hale, Garrick, Ysolde, Pell, Lume and Nessa sat at one table. Nobody spoke. Nobody left."),
            C("Cael's first night as the Titan: 'I forgot why I kept them. I only know I must keep them.'"),
            C("Cael talked to an empty chair every evening. The chair never answered."),
            K("You said it out loud: 'Whatever happens, I will not forget why I am fighting.'"),
            K("Dorin's first turret, crooked and proud. It still stands."),
            C("Cael: 'Someone must take my place. I cannot be the only one left.'"),
            C("Cael and the Knight, years apart, drinking kape on the Bastion wall. Both alone. Both looking at the same sunrise.")
        }
    };

    /** Key memory for a stage, or null (only Last Stand maps give a key). */
    public static Memory forStage(int stage) {
        if (Campaign.map(stage) != 2) return null;
        int world = Campaign.world(stage);
        int key = (Campaign.level(stage) - 1) % 10;
        return ALL[world][key];
    }

    /** True if this stage's key is the world's tenth (comes from the world boss). */
    public static boolean isWorldFinalKey(int stage) {
        return Campaign.map(stage) == 2 && Campaign.level(stage) % 10 == 0;
    }
}