package com.lokij07.riftguard;

/**
 * Platform-independent movement and collision rules shared with deterministic
 * unit tests. Bounds and citadel coordinates match RiftGameView's world layout.
 */
public final class MovementRules {
    public static final float WORLD_WIDTH=1900f, WORLD_HEIGHT=1050f;
    public static final float CORE_X=950f, CORE_Y=510f;
    private MovementRules() {}

    public static float heroNextX(float x,float y,float dx) {
        float moved=BattleRules.clamp(x+dx,35f,WORLD_WIDTH-35f);
        return blocksHero(moved,y) ? x : moved;
    }
    public static float heroNextY(float x,float y,float dy) {
        float moved=BattleRules.clamp(y+dy,35f,WORLD_HEIGHT-35f);
        return blocksHero(x,moved) ? y : moved;
    }
    /** Outer castle walls, guarded southern entrance, and central keep. */
    public static boolean blocksHero(float x,float y) {
        float hx=Math.abs(x-CORE_X),hy=Math.abs(y-CORE_Y);
        // The keep is solid: the hero cannot walk through the castle.
        if(hx<94 && y>CORE_Y-59 && y<CORE_Y+75) return true;
        boolean outer=hx<161 && hy<134;
        boolean southGate=hx<32 && y>CORE_Y+10;
        boolean courtyard=hx<107 && hy<79;
        return outer && !southGate && !courtyard;
    }

    public static boolean huntsHero(float distance,int kind) {
        return distance<238f && kind!=2 && kind!=5;
    }
    public static float enemyStopDistance(boolean pursuingHero,int kind) {
        if(pursuingHero) return 35f;
        return kind==5 ? 250f : 156f;
    }
}
