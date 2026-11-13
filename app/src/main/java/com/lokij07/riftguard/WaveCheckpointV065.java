package com.lokij07.riftguard;

import java.util.Arrays;

/**
 * In-memory wave checkpoint. Saves only deterministic gameplay state, no
 * Android views, Context, bitmaps or cross-stage preferences.
 * No persistent paid resources or campaign flags can be replayed.
 */
public final class WaveCheckpointV065 {
    private WaveCheckpointV065(){}
    public static int retries(int mode){
        return new int[]{3,1,0,0}[DifficultyRules.safe(mode)];
    }
    public static final class Snapshot {
        public final int wave,gold,kills,built,revivalsUsed;
        public final boolean defeatedElite;
        public final float heroHp,coreHp,relicHp;
        public final int[] towerTier,towerTypes,destroyedTier;
        public final float[] towerHealth;
        private Snapshot(int wave,int gold,int kills,int built,int revivalsUsed,
                         boolean defeatedElite,float heroHp,float coreHp,float relicHp,
                         int[] towerTier,int[] towerTypes,int[] destroyedTier,
                         float[] towerHealth){
            this.wave=wave;this.gold=gold;this.kills=kills;this.built=built;
            this.revivalsUsed=revivalsUsed;this.defeatedElite=defeatedElite;
            this.heroHp=heroHp;this.coreHp=coreHp;this.relicHp=relicHp;
            this.towerTier=towerTier.clone();
            this.towerTypes=towerTypes.clone();
            this.destroyedTier=destroyedTier.clone();
            this.towerHealth=towerHealth.clone();
        }
        public int gold(){return gold;}
        public void restore(int[] tiers,int[] types,int[] destroyed,float[] hp){
            if(tiers.length!=towerTier.length||types.length!=towerTypes.length||
                    destroyed.length!=destroyedTier.length||hp.length!=towerHealth.length)
                throw new IllegalArgumentException("pad arrays");
            System.arraycopy(towerTier,0,tiers,0,tiers.length);
            System.arraycopy(towerTypes,0,types,0,types.length);
            System.arraycopy(destroyedTier,0,destroyed,0,destroyed.length);
            System.arraycopy(towerHealth,0,hp,0,hp.length);
        }
    }
    public static Snapshot capture(int wave,int gold,int kills,int built,int revivalsUsed,
                                   boolean defeatedElite,float heroHp,float coreHp,float relicHp,
                                   int[] tiers,int[] types,int[] destroyed,float[] hp){
        if(wave<1||gold<0||heroHp<0||coreHp<0||relicHp<0||
                tiers.length!=TowerPadLayout.MAX||types.length!=TowerPadLayout.MAX||
                destroyed.length!=TowerPadLayout.MAX||hp.length!=TowerPadLayout.MAX)
            throw new IllegalArgumentException("invalid checkpoint");
        return new Snapshot(wave,gold,kills,built,revivalsUsed,defeatedElite,
                heroHp,coreHp,relicHp,tiers,types,destroyed,hp);
    }
}
