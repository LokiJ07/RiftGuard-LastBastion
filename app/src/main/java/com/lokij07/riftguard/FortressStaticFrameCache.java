package com.lokij07.riftguard;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Original rank-accurate fortress architecture, rasterized once per visual
 * upgrade instead of repainting hundreds of paths every combat frame.
 *
 * Only the visual structural layers are cached: the real attack-bearing tower
 * guns, aim, recoil, projectile spawn, collision, damage FX and input remain
 * live in RiftGameView. The small banner/arc lighting accents remain animated.
 */
public final class FortressStaticFrameCache {
    public static final int WIDTH=820, HEIGHT=640, BASELINE_Y=390;
    public static final int MAX_FRAMES=2;
    private final LinkedHashMap<Integer,Bitmap> frames=
            new LinkedHashMap<>(3,.75f,true);
    private final Paint bitmapPaint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    private final Paint accent=new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean disabled=false;
    private int builds=0;

    public int cachedFrames(){return frames.size();}
    public int frameBuilds(){return builds;}
    public boolean isDisabled(){return disabled;}
    /** Never recycle cached bitmaps while the GPU may be presenting a frame. */
    public void clear(){frames.clear();}

    private static int key(int rank,int arsenal,int core,int treasury){
        return ((rank*5+arsenal)*5+core)*5+treasury;
    }
    private static boolean valid(int rank,int arsenal,int core,int treasury){
        return rank>=1&&rank<=5&&arsenal>=0&&arsenal<=4&&
                core>=0&&core<=4&&treasury>=0&&treasury<=4;
    }

    /** The exact source layers, also used if a low-memory device rejects cache. */
    public static void drawOriginal(Canvas c,float x,float y,int rank,
                                    int arsenal,int core,int treasury,float time){
        NativeDeployedMapArt.fort(c,x,y,rank,time,1f);
        FortressStructures.rankStructures(c,x,y,rank,time);
        FortressSilhouetteArt.draw(c,x,y,rank,time);
        FortressEvolutionArt.draw(c,x,y,rank,arsenal,time);
        FortressWeaponShowcaseArt.draw(c,x,y,arsenal,time);
        EvolutionEffects.fortressBranches(c,x,y,core,treasury);
    }

    /** True = cached visual was rendered. False = drawOriginal is needed. */
    public boolean draw(Canvas c,float x,float y,int rank,int arsenal,
                        int core,int treasury,float time,float healthRatio){
        if(disabled||!valid(rank,arsenal,core,treasury))return false;
        int k=key(rank,arsenal,core,treasury);
        Bitmap image=frames.get(k);
        if(image==null||image.isRecycled()){
            try{
                image=Bitmap.createBitmap(WIDTH,HEIGHT,Bitmap.Config.ARGB_8888);
                Canvas art=new Canvas(image);
                drawOriginal(art,WIDTH*.5f,BASELINE_Y,rank,arsenal,
                        core,treasury,0f);
                if(frames.size()>=MAX_FRAMES){
                    Iterator<Map.Entry<Integer,Bitmap>> it=frames.entrySet().iterator();
                    if(it.hasNext()){it.next();it.remove();}
                }
                frames.put(k,image);builds++;
            }catch(OutOfMemoryError ex){
                frames.clear();
                disabled=true;return false;
            }
        }
        c.drawBitmap(image,x-WIDTH*.5f,y-BASELINE_Y,bitmapPaint);
        // These details are animated independently of the static structure.
        // Camera, tower targeting and gun recoil remain completely dynamic.
        accent.setStyle(Paint.Style.FILL);
        accent.setColor(0xffe2a847);
        float sway=(float)Math.sin(time*3f)*5f;
        c.drawRect(x+2,y-108,x+3,y-81,accent);
        android.graphics.Path banner=new android.graphics.Path();
        banner.moveTo(x+3,y-108);
        banner.lineTo(x+46+sway,y-94);
        banner.lineTo(x+3,y-82);
        banner.close();
        c.drawPath(banner,accent);
        if(rank>=5){
            accent.setColor(0x5579dff4);
            float pulse=4+(float)Math.sin(time*2.7)*3f;
            c.drawOval(x-24-pulse,y-168-pulse,
                    x+24+pulse,y-156+pulse,accent);
        }
        if(healthRatio<.48f){
            accent.setColor(0xff503e38);
            accent.setStrokeWidth(2f);
            accent.setStyle(Paint.Style.STROKE);
            for(int i=0;i<4;i++)
                c.drawLine(x-95+i*51,y-5,x-105+i*51,y+27,accent);
            accent.setStyle(Paint.Style.FILL);
        }
        return true;
    }
}
