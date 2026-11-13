package com.lokij07.riftguard;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Bounded transparent raster cache for the original, articulated Warden.
 *
 * Idle and locomotion poses are quantized to eight directions and eight
 * animation frames. Real attack/cast/hurt/death clips still draw the live rig
 * directly, preserving weapon contact timing and skill/death animations.
 *
 * The cache follows the current saved weapon/mastery/armor, never produces
 * substitute equipment or changes player controls/hitboxes/save files.
 */
public final class WardenSpriteFrameCache {
    public static final int WIDTH=280, HEIGHT=264, FRAME_COUNT=8;
    public static final int MAX_CACHED_FRAMES=64;
    private static final float TWO_PI=(float)(Math.PI*2);
    private static final float FOOT_Y=232f;
    private final WardenRig rig;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    private final LinkedHashMap<Integer,Bitmap> frames=new LinkedHashMap<>(64,.75f,true);
    private int builds=0;
    private int cacheLimit=MAX_CACHED_FRAMES;
    private boolean disabled=false;

    public WardenSpriteFrameCache(WardenRig rig) {
        if(rig==null)throw new IllegalArgumentException("rig");
        this.rig=rig;
    }
    public int cachedFrames(){return frames.size();}
    public int cacheLimit(){return cacheLimit;}
    public void setCacheLimit(int limit){
        int bounded=Math.max(16,Math.min(MAX_CACHED_FRAMES,limit));
        if(cacheLimit!=bounded){cacheLimit=bounded;clear();}
    }
    public int frameBuilds(){return builds;}
    public boolean isDisabled(){return disabled;}
    /** Drop references, not native bitmaps still in use by RenderThread. */
    public void clear(){frames.clear();}
    public int direction(float facing){
        if(!Float.isFinite(facing))return 0;
        float normal=facing%TWO_PI;
        if(normal<0f)normal+=TWO_PI;
        return Math.floorMod(Math.round(normal/TWO_PI*8f),8);
    }
    public int frame(float time,boolean running){
        if(!Float.isFinite(time))return 0;
        // Animation rig's documented 11.7 rad/sec stride and 2.2 rad/sec
        // breathing cycles. Repeated timestamps reuse the same bitmap.
        float phase=(time*(running?11.7f:2.2f))%TWO_PI;
        if(phase<0f)phase+=TWO_PI;
        return Math.min(FRAME_COUNT-1,(int)(phase/TWO_PI*FRAME_COUNT));
    }
    private static int key(int weapon,int form,int armor,int mastery,
                           boolean running,int dir,int tile){
        int id=weapon*5+form;
        id=id*5+armor;
        id=id*128+mastery;
        id=id*2+(running?1:0);
        id=id*8+dir;
        return id*FRAME_COUNT+tile;
    }
    public boolean draw(Canvas canvas,float x,float y,float time,float facing,
                        boolean running,float attack,float cast,boolean hurt,
                        int weapon,int mastery,int form,int armor,float death){
        if(disabled||!Float.isFinite(x)||!Float.isFinite(y)||
           !Float.isFinite(time)||!Float.isFinite(facing)||
           attack>=0f||cast>=0f||hurt||death>=0f||
           weapon<0||weapon>=6||form<0||form>4||armor<0||armor>4||
           mastery<0||mastery>127)return false;
        int dir=direction(facing),tile=frame(time,running);
        int id=key(weapon,form,armor,mastery,running,dir,tile);
        Bitmap bitmap=frames.get(id);
        if(bitmap==null||bitmap.isRecycled()){
            try {
                bitmap=Bitmap.createBitmap(WIDTH,HEIGHT,Bitmap.Config.ARGB_8888);
                Canvas target=new Canvas(bitmap);
                float rate=running?11.7f:2.2f;
                float sample=(tile+.5f)*TWO_PI/(FRAME_COUNT*rate);
                float heading=dir*TWO_PI/8f;
                boolean okay=rig.hero(target,WIDTH*.5f,FOOT_Y,
                        sample,heading,running,-1f,-1f,false,
                        weapon,mastery,form,armor,-1f);
                if(!okay){bitmap.recycle();return false;}
                if(frames.size()>=cacheLimit){
                    Iterator<Map.Entry<Integer,Bitmap>> it=frames.entrySet().iterator();
                    if(it.hasNext()){it.next();it.remove();}
                }
                frames.put(id,bitmap);
                builds++;
            }catch(OutOfMemoryError lowMemory) {
                // A functional original vector Warden is the safe fallback.
                frames.clear();
                disabled=true;
                return false;
            }
        }
        canvas.drawBitmap(bitmap,x-WIDTH*.5f,y-FOOT_Y,paint);
        return true;
    }
}
