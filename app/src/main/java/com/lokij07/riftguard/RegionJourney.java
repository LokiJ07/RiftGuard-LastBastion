package com.lokij07.riftguard;
import android.graphics.Canvas;

/** HTML-style native Java journey map with unchanged selectable locations. */
public final class RegionJourney {
    private RegionJourney(){}
    private static final float[] X={143,220,317,424,556,694,769,653,503,331};
    private static final float[] Y={452,368,492,396,444,357,253,193,260,177};
    public static int hitLevel(float x,float y){
        for(int i=0;i<X.length;i++){
            float dx=x-X[i],dy=y-Y[i];
            if(dx*dx+dy*dy<39*39)return i;
        }
        return -1;
    }
    public static void draw(Canvas c,int world,int selectedLevel,int unlocked,float time){
        NativeKingdomAtlas.drawRegion(c,world,selectedLevel,unlocked,time);
    }
    public static void draw(Canvas c,int world,int selectedLevel,int unlocked,float time,
                            BiomeProps environment){
        NativeKingdomAtlas.drawRegion(c,world,selectedLevel,unlocked,time);
    }
}
