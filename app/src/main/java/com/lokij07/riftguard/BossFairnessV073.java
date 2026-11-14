package com.lokij07.riftguard;

/** Only world bosses: per-charge damage budgets against REAL max HP.
 * The existing difficulty and phase multipliers still apply before these
 * caps. Every telegraph can hurt; none should one-shot full-health players.
 */
public final class BossFairnessV073 {
    private BossFairnessV073(){}
    public static final float HERO_FRACTION=.34f,CORE_FRACTION=.25f,TOWER_FRACTION=.43f;
    private static float cap(float raw,float hp,float share){
        if(!Float.isFinite(raw)||!Float.isFinite(hp)||hp<=0||raw<=0)return 0f;
        return Math.min(raw,hp*share);
    }
    public static float hero(float raw,float maxHp){return cap(raw,maxHp,HERO_FRACTION);}
    public static float core(float raw,float maxHp){return cap(raw,maxHp,CORE_FRACTION);}
    public static float tower(float raw,float maxHp){return cap(raw,maxHp,TOWER_FRACTION);}
}
