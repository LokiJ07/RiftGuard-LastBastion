package com.lokij07.riftguard;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Sixty original, reusable environmental prop sprites:
 * 10 biomes x [broadleaf, conifer/variant, rock, bush, ruin, ambient crystal].
 * Individual transparent layers retain collision-free depth ordering and independent sway.
 * No screenshot UI is included in the atlas.
 */
public final class BiomeProps {
    public static final int BIOMES=10, VARIANTS=6;
    private static final int TILE_W=168,TILE_H=196;
    private final Bitmap atlas;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG |
            Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);

    public BiomeProps(Resources resources) {
        Bitmap image=null;
        try {
            BitmapFactory.Options opts=new BitmapFactory.Options();
            opts.inScaled=false;
            image=BitmapFactory.decodeResource(resources,R.drawable.riftguard_world_props,opts);
        } catch(RuntimeException ignored) {
            // The original procedural renderer remains an emergency fallback.
        }
        atlas=image;
    }
    public boolean ready() {
        return atlas!=null&&!atlas.isRecycled()
                &&atlas.getWidth()==TILE_W*VARIANTS
                &&atlas.getHeight()==TILE_H*BIOMES;
    }
    public static float groundDepth(float y) { return y+5; }
    public static boolean overlapsHero(float x,float y,float scale,float hx,float hy) {
        return Math.abs(x-hx)<55*scale && y>hy-20 && y<hy+78;
    }
    public boolean draw(Canvas c,int biome,int variant,float x,float y,float scale,
                        float time,int alpha) {
        if(!ready() || biome<0 || biome>=BIOMES ||
           variant<0 || variant>=VARIANTS || scale<=0) return false;
        float size=scale*(variant<2?1.12f:variant==4?.94f:.84f);
        float width=TILE_W*size,height=TILE_H*size;
        float sway=(variant<2?(float)Math.sin(time*1.7f + x*.021f)*2.1f:0);
        int left=variant*TILE_W,top=biome*TILE_H;
        Rect src=new Rect(left,top,left+TILE_W,top+TILE_H);
        c.save();
        if(sway!=0)c.rotate(sway,x,y-75*size);
        paint.setColor(Color.WHITE);
        paint.setAlpha(Math.max(0,Math.min(255,alpha)));
        c.drawBitmap(atlas,src,
                new RectF(x-width*.5f,y-height+26*size,x+width*.5f,y+26*size),paint);
        paint.setAlpha(255);
        c.restore();
        return true;
    }
}
