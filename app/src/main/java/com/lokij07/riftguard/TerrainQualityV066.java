package com.lokij07.riftguard;

/**
 * Public Android render targets for the static painted world.
 *
 * Only the background raster is sampled at a lower resolution on smaller
 * devices. Live units, fortress/towers, projectiles, touch targets, readable
 * HUD text and physics all remain at native game coordinates and draw quality.
 */
public final class TerrainQualityV066 {
    public static final int WORLD_WIDTH=1900, WORLD_HEIGHT=1050;
    private TerrainQualityV066(){}
    public static float scale(int quality){
        switch(GraphicsProfileV065.safe(quality)){
            case GraphicsProfileV065.HIGH: return 1.00f;
            case GraphicsProfileV065.PERFORMANCE: return .68f;
            default: return .84f;
        }
    }
    public static int width(int quality){
        return Math.max(1,Math.round(WORLD_WIDTH*scale(quality)));
    }
    public static int height(int quality){
        return Math.max(1,Math.round(WORLD_HEIGHT*scale(quality)));
    }
    public static long cacheBytes(int quality){
        // Android Bitmap.Config.RGB_565 = 2 bytes per pixel.
        return 2L*width(quality)*height(quality);
    }
    public static int ambientMotes(int quality,boolean sustainedSlow){
        if(sustainedSlow)return 6;
        switch(GraphicsProfileV065.safe(quality)){
            case GraphicsProfileV065.HIGH:return 23;
            case GraphicsProfileV065.PERFORMANCE:return 9;
            default:return 16;
        }
    }
    public static int weatherParticles(int quality,boolean sustainedSlow,
                                       int original){
        if(original<=0)return 0;
        int cap=GraphicsProfileV065.safe(quality)==GraphicsProfileV065.HIGH
                ?original:GraphicsProfileV065.safe(quality)==
                 GraphicsProfileV065.PERFORMANCE?(original+2)/3:
                (original*3+3)/4;
        return sustainedSlow?(cap+1)/2:cap;
    }
}
