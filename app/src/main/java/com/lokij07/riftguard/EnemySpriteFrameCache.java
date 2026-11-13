package com.lokij07.riftguard;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * v0.60.0: original detailed Java actor art is rasterized into a bounded
 * 10-frame animation cache on demand. This avoids redrawing hundreds of
 * Canvas paths for every visible enemy and every screen refresh.
 *
 * The asset pack supplied by the user is reference-only; these sprite
 * frames are authored by RiftGuard's own EnemyRig at runtime, not imported
 * or redistributed third-party images.
 */
public final class EnemySpriteFrameCache {
    public static final int FRAME_COUNT=10, WIDTH=208, HEIGHT=238;
    private static final int FOOT_Y=220;
    // < 15 MB of ARGB_8888 frames. Oldest frames are recycled first.
    public static final int MAX_CACHED_FRAMES=72;
    private final EnemyRig rig;
    private final LinkedHashMap<Integer,Bitmap> frames=
            new LinkedHashMap<>(96,.75f,true);
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    private int builds=0;
    private boolean cacheDisabled=false;

    public EnemySpriteFrameCache(EnemyRig rig){
        if(rig==null)throw new IllegalArgumentException("rig");
        this.rig=rig;
    }
    public static int frame(float phase,float attack){
        if(attack>=0) {
            float t=Math.max(0,Math.min(.9999f,attack));
            return Math.max(0,Math.min(9,(int)(t*FRAME_COUNT)));
        }
        if(!Float.isFinite(phase))return 0;
        float turns=phase/(float)(2*Math.PI);
        float fraction=turns-(float)Math.floor(turns);
        return Math.min(9,Math.max(0,(int)(fraction*FRAME_COUNT)));
    }
    private static int id(int kind,int biome,int variant,boolean boss,
                          int bossPhase,int state,boolean hurt,int frame){
        int k=kind;
        k=k*10+biome;
        k=k*4+variant;
        k=k*2+(boss?1:0);
        k=k*3+(bossPhase-1);
        k=k*3+state;
        k=k*2+(hurt?1:0);
        return k*10+frame;
    }
    public int cachedFrames(){return frames.size();}
    public int frameBuilds(){return builds;}
    public void clear(){
        for(Bitmap b:frames.values())if(!b.isRecycled())b.recycle();
        frames.clear();
    }

    /**
     * Draw an actual raster frame of an enemy's real animated rig.
     * State follows the same attack timer + body phase as EnemyRig.
     * Damage, AI, direction, particle overlays, shadows, boss rules
     * and collision stay in RiftGameView.
     */
    public boolean draw(Canvas c,int kind,int biome,int variant,float x,float y,
                        float phase,boolean moving,float attack,boolean hurt,
                        float faceX,boolean boss,int bossPhase){
        if(kind<0||kind>=7||!Float.isFinite(x)||!Float.isFinite(y)
                ||!Float.isFinite(phase))return false;
        if(cacheDisabled)return false;
        int world=Math.floorMod(biome,10);
        int form=Math.floorMod(variant,4);
        int phaseTier=Math.max(1,Math.min(3,bossPhase));
        int state=attack>=0?2:moving?1:0;
        int tile=frame(phase,attack);
        int key=id(kind,world,form,boss,phaseTier,state,hurt,tile);
        Bitmap b=frames.get(key);
        if(b==null || b.isRecycled()){
            try {
                b=Bitmap.createBitmap(WIDTH,HEIGHT,Bitmap.Config.ARGB_8888);
                Canvas raster=new Canvas(b);
                float time=(float)(tile*2*Math.PI/FRAME_COUNT);
                float frameAttack=state==2?(tile+.5f)/FRAME_COUNT:-1f;
                boolean success=rig.enemy(raster,kind,world,form,WIDTH*.5f,
                        FOOT_Y,time,state==1,frameAttack,hurt,1f,boss,phaseTier);
                if(!success){b.recycle();return false;}
            }catch(OutOfMemoryError memory){
                clear();
                cacheDisabled=true;
                return false; // prefer original sprite fallback on low-memory devices
            }
            if(frames.size()>=MAX_CACHED_FRAMES){
                Iterator<Map.Entry<Integer,Bitmap>> it=frames.entrySet().iterator();
                Map.Entry<Integer,Bitmap> eldest=it.next();
                if(!eldest.getValue().isRecycled())eldest.getValue().recycle();
                it.remove();
            }
            frames.put(key,b);builds++;
        }
        c.save();
        if(faceX<0f)c.scale(-1f,1f,x,y);
        p.setColor(Color.WHITE);p.setAlpha(255);
        c.drawBitmap(b,x-WIDTH*.5f,y-FOOT_Y,p);
        c.restore();
        return true;
    }
}
