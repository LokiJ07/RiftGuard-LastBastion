package com.lokij07.riftguard;

import java.util.Locale;

/**
 * User-visible quality presets for Android devices of different RAM classes.
 * A 90/120Hz panel does not guarantee the game can render at 90/120 FPS.
 * Gameplay physics, weapons, player hitboxes and storyline are identical
 * across presets: only render pacing and noninteractive ambience change.
 */
public final class GraphicsProfileV065 {
    public static final int HIGH=0, BALANCED=1, PERFORMANCE=2;
    public static final String SAVE_KEY="graphics.v065.profile";
    private GraphicsProfileV065(){}

    public static int safe(int mode){
        return mode>=HIGH&&mode<=PERFORMANCE?mode:BALANCED;
    }
    /**
     * Android's ActivityManager memory class measures the per-app heap budget,
     * not total installed RAM. Avoid identifying performance by phone brand.
     * Dynamic overload detection still reduces noninteractive effects if
     * the GPU is weaker than its RAM class suggests.
     */
    public static int recommended(int memoryClassMb,boolean lowRamDevice){
        if(lowRamDevice||memoryClassMb<=96)return PERFORMANCE;
        if(memoryClassMb<256)return BALANCED;
        return HIGH;
    }

    /** Legacy reference-profile helper retained for existing compatibility tests. */
    public static int recommended(String manufacturer,String model){
        String maker=manufacturer==null?"":manufacturer.toLowerCase(Locale.ROOT);
        String device=model==null?"":model.toLowerCase(Locale.ROOT);
        if(maker.contains("tecno")&&
                (device.contains("lh7n")||device.contains("pova 5")))return HIGH;
        return BALANCED;
    }
    public static int cycle(int mode){
        return (safe(mode)+1)%3;
    }
    public static String label(int mode){
        switch(safe(mode)){
            case HIGH:return "HIGH";
            case PERFORMANCE:return "PERFORMANCE";
            default:return "BALANCED";
        }
    }
    /** All presets seek a stable 60fps rather than burning power at 120Hz. */
    public static int targetFps(int mode){return 60;}
    /** Secondary atmosphere only; never cull enemies, tower animations or HUD. */
    public static boolean reduceAmbience(int mode,boolean deviceSlow){
        return safe(mode)==PERFORMANCE||deviceSlow;
    }
    /** Use every other display refresh on 90/120Hz panels, not game time skips.
     * Android will schedule a future VSYNC from this delay; input invalidates
     * remain immediate, independent of the cosmetic frame loop.
     */
    public static long nextFrameDelayMs(int mode,float screenHz){
        if(!Float.isFinite(screenHz)||screenHz<88f)return 0;
        float displayInterval=1000f/screenHz;
        float targetInterval=1000f/targetFps(mode);
        return Math.max(0L,(long)Math.ceil(targetInterval-displayInterval+1f));
    }
}
