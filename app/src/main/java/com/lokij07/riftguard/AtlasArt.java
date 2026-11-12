package com.lokij07.riftguard;
import android.graphics.Canvas;

/** Native HTML kingdom atlas. Coordinates are retained for existing touch input. */
public final class AtlasArt {
    private AtlasArt(){}
    public static final float[] NODES_X={190,276,403,535,683,762,682,531,372,211};
    public static final float[] NODES_Y={470,363,455,364,419,305,209,267,188,251};
    public static int hitRegion(float x,float y){
        for(int i=0;i<NODES_X.length;i++){
            float dx=x-NODES_X[i],dy=y-NODES_Y[i];
            if(dx*dx+dy*dy<43*43)return i;
        }
        return -1;
    }
    public static void draw(Canvas c,int selected,int unlocked,float time){
        NativeKingdomAtlas.drawWorld(c,selected,unlocked,time);
    }
}
