package com.lokij07.riftguard;

import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Durable restart-at-wave-boundary recovery, NOT a mid-animation state dump.
 *
 * Only the deterministic in-memory WaveCheckpointV065 model is serialized.
 * This avoids trying to rehydrate active projectiles, Android Views, graphics,
 * random enemies, or in-flight hit events after Android kills the process.
 *
 * A recovered battle restarts its current wave from the saved boundary.
 * Permanent supplies, rewards and unlocks are never stored or granted here.
 */
public final class PersistentWaveCheckpointV065 {
    private static final String KEY="battle.v065.recovery";
    private static final int SCHEMA=1;
    private final SharedPreferences prefs;
    public PersistentWaveCheckpointV065(SharedPreferences prefs){
        if(prefs==null)throw new IllegalArgumentException("prefs");
        this.prefs=prefs;
    }
    public static final class Recovery {
        public final WaveCheckpointV065.Snapshot snapshot;
        public final boolean firstWavePending;
        public final int difficulty,retriesUsed;
        private Recovery(WaveCheckpointV065.Snapshot s,boolean firstPending,
                         int difficulty,int retriesUsed){
            this.snapshot=s;this.firstWavePending=firstPending;
            this.difficulty=difficulty;this.retriesUsed=retriesUsed;
        }
    }
    private static JSONArray ints(int[] data){
        JSONArray a=new JSONArray();
        for(int value:data)a.put(value);
        return a;
    }
    private static JSONArray floats(float[] data)throws Exception{
        JSONArray a=new JSONArray();
        for(float value:data){
            if(!Float.isFinite(value))throw new IllegalArgumentException("NaN");
            a.put((double)value);
        }
        return a;
    }
    private static int[] readInts(JSONObject json,String key)throws Exception{
        JSONArray a=json.getJSONArray(key);
        if(a.length()!=TowerPadLayout.MAX)throw new IllegalArgumentException("size");
        int[] values=new int[a.length()];
        for(int i=0;i<a.length();i++)values[i]=a.getInt(i);
        return values;
    }
    private static float[] readFloats(JSONObject json,String key)throws Exception{
        JSONArray a=json.getJSONArray(key);
        if(a.length()!=TowerPadLayout.MAX)throw new IllegalArgumentException("size");
        float[] values=new float[a.length()];
        for(int i=0;i<a.length();i++){
            values[i]=(float)a.getDouble(i);
            if(!Float.isFinite(values[i]))throw new IllegalArgumentException("NaN");
        }
        return values;
    }
    /** Committed at start / next wave only, never each animation frame. */
    public boolean save(WaveCheckpointV065.Snapshot snap,
                        boolean firstPending,int difficulty,int retriesUsed){
        if(snap==null||snap.wave<1||snap.wave>DifficultyRules.waves(
                snap.stage,DifficultyRules.safe(difficulty))||
                (firstPending&&snap.wave!=1)||retriesUsed<0||
                retriesUsed>WaveCheckpointV065.retries(difficulty))return false;
        try{
            JSONObject data=new JSONObject();
            data.put("v",SCHEMA);
            data.put("stage",snap.stage);
            data.put("wave",snap.wave);
            data.put("firstPending",firstPending);
            data.put("difficulty",difficulty);
            data.put("retriesUsed",retriesUsed);
            data.put("gold",snap.gold);
            data.put("kills",snap.kills);
            data.put("built",snap.built);
            data.put("revivals",snap.revivalsUsed);
            data.put("destroyed",snap.destroyedCount);
            data.put("pulses",snap.pulses);
            data.put("skillCasts",snap.skillCasts);
            data.put("elite",snap.defeatedElite);
            data.put("heroHp",snap.heroHp);
            data.put("coreHp",snap.coreHp);
            data.put("relicHp",snap.relicHp);
            data.put("heroX",snap.heroX);
            data.put("heroY",snap.heroY);
            data.put("tiers",ints(snap.towerTier));
            data.put("types",ints(snap.towerTypes));
            data.put("rubble",ints(snap.destroyedTier));
            data.put("towerHealth",floats(snap.towerHealth));
            return prefs.edit().putString(KEY,data.toString()).commit();
        }catch(Exception badData){
            return false;
        }
    }
    /** Corrupted/truncated records are rejected, never applied partially. */
    public Recovery load(int unlocked){
        String raw=prefs.getString(KEY,null);
        if(raw==null||raw.isEmpty())return null;
        try{
            JSONObject data=new JSONObject(raw);
            if(data.getInt("v")!=SCHEMA)throw new IllegalArgumentException("schema");
            int stage=data.getInt("stage");
            Campaign.checkStage(stage);
            if(stage>unlocked)throw new IllegalArgumentException("locked");
            int difficulty=data.getInt("difficulty");
            if(difficulty<0||difficulty>3)throw new IllegalArgumentException("difficulty");
            int savedWave=data.getInt("wave");
            int newCap=DifficultyRules.waves(stage,difficulty);
            // Before the formation rebalance, Expert had up to five bonus
            // waves. Preserve old legitimate saves by restarting from the
            // latest CURRENT final wave, not deleting purchases and HP.
            if(savedWave<1||savedWave>Math.max(newCap,
                    SiegeBalanceV066.legacyWaveCeiling(stage,difficulty)))
                throw new IllegalArgumentException("wave");
            int wave=Math.min(savedWave,newCap);
            int retries=data.getInt("retriesUsed");
            if(retries<0||retries>WaveCheckpointV065.retries(difficulty))
                throw new IllegalArgumentException("retries");
            boolean first=data.getBoolean("firstPending");
            if(first&&wave!=1)throw new IllegalArgumentException("first");
            int[] tiers=readInts(data,"tiers"),types=readInts(data,"types"),
                    rubble=readInts(data,"rubble");
            float[] hp=readFloats(data,"towerHealth");
            for(int i=0;i<TowerPadLayout.MAX;i++){
                if(tiers[i]<0||tiers[i]>BattleRules.MAX_TOWER_TIER||
                        rubble[i]<0||rubble[i]>BattleRules.MAX_TOWER_TIER||
                        types[i]<0||types[i]>3||
                        hp[i]<0||hp[i]>5000)throw new IllegalArgumentException("tower");
            }
            float heroHp=(float)data.getDouble("heroHp");
            float coreHp=(float)data.getDouble("coreHp");
            float relicHp=(float)data.getDouble("relicHp");
            float heroX=(float)data.getDouble("heroX");
            float heroY=(float)data.getDouble("heroY");
            if(!Float.isFinite(heroX)||!Float.isFinite(heroY)||
                    heroX<0||heroX>1900||heroY<0||heroY>1050||
                    !Float.isFinite(heroHp)||!Float.isFinite(coreHp)||
                    !Float.isFinite(relicHp)||heroHp<0||coreHp<=0||
                    heroHp>100000||coreHp>100000||relicHp<0||relicHp>100000)
                throw new IllegalArgumentException("health/position");
            int gold=data.getInt("gold"),kills=data.getInt("kills");
            int built=data.getInt("built"),revivals=data.getInt("revivals");
            int destroyed=data.getInt("destroyed"),pulses=data.getInt("pulses");
            // Version 1 save compatibility: original Pulse casts were skills.
            int skillCasts=data.optInt("skillCasts",pulses);
            if(gold<0||gold>10000000||kills<0||built<0||revivals<0||
                    destroyed<0||pulses<0||skillCasts<0||skillCasts>100000)
                throw new IllegalArgumentException("statistics");
            WaveCheckpointV065.Snapshot snap=WaveCheckpointV065.captureWithSkills(
                    stage,wave,gold,kills,built,revivals,destroyed,pulses,skillCasts,
                    data.getBoolean("elite"),heroHp,coreHp,relicHp,heroX,heroY,
                    tiers,types,rubble,hp);
            return new Recovery(snap,first,difficulty,retries);
        }catch(Exception badRecord){
            clear();
            return null;
        }
    }
    /**
     * The completion boundary is one atomic preferences transaction:
     * permanent earned supplies/unlocks and deletion of the recoverable wave.
     * A process death cannot leave "reward granted" and "retry checkpoint"
     * both committed on disk.
     */
    public boolean commitVictory(int unlocked,int supplies){
        if(unlocked<0||unlocked>Campaign.STAGES||supplies<0)return false;
        return prefs.edit().putInt("unlocked",unlocked)
                .putInt("supplies",supplies).remove(KEY).commit();
    }
    /**
     * v0.78: the Warden's XP reward must be durable in the EXACT SAME
     * preferences commit as supplies, campaign unlock and checkpoint clear.
     * Death/replay cannot both collect XP for the same completed battle.
     */
    public boolean commitVictory(int unlocked,int supplies,int earnedWardenTotalXp){
        if(unlocked<0||unlocked>Campaign.STAGES||supplies<0||
                earnedWardenTotalXp<0||
                earnedWardenTotalXp>WardenLevelsV078.maxXp())return false;
        return prefs.edit().putInt("unlocked",unlocked)
                .putInt("supplies",supplies)
                .putInt(WardenLevelsV078.SAVE_XP,earnedWardenTotalXp)
                .remove(KEY).commit();
    }
    public void clear(){prefs.edit().remove(KEY).commit();}
}
