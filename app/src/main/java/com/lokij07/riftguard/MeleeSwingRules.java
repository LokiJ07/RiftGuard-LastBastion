package com.lokij07.riftguard;

/** Timing and targeting for weapon-linked melee damage, independent of Android. */
public final class MeleeSwingRules {
    private MeleeSwingRules() {}
    public static final float HIT_PROGRESS=.36f;

    public static boolean hitWindow(float normalizedProgress) {
        return normalizedProgress>=HIT_PROGRESS;
    }

    /** Targets must be within weapon reach and within the forward sweeping arc. */
    public static boolean hits(float heroX,float heroY,float facing,float range,
                               float targetX,float targetY) {
        if(range<=0 || !Float.isFinite(facing))return false;
        float dx=targetX-heroX,dy=targetY-heroY;
        float distanceSquared=dx*dx+dy*dy;
        if(distanceSquared>range*range)return false;
        if(distanceSquared<.001f)return true;
        float projection=dx*(float)Math.cos(facing)+dy*(float)Math.sin(facing);
        // ~145-degree sweeping cone: broad enough for a sword, but not a 360
        // degree radial spell that damages monsters behind the player.
        return projection/Math.max(.001f,(float)Math.sqrt(distanceSquared))>=.30f;
    }
}
