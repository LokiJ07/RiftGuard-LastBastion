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
 * Genuine bundled original AI-generated artwork packed into an offline WebP atlas.
 * All UI/game entities stay interactive Canvas objects layered above the illustrations.
 * The atlas is a deliberately compressed first production texture, not final-resolution art.
 */
public final class PaintedArt {
    private final Bitmap atlas;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG|Paint.DITHER_FLAG);
    private final Paint tint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final int MAP=0, HOME=1, FORTRESS=2;
    private static final Rect[] SOURCE={
        new Rect(0,0,176,132),
        new Rect(176,0,256,66),
        new Rect(176,66,256,132),
        new Rect(0,132,85,192),
        new Rect(85,132,170,192),
        new Rect(170,132,256,192)
    };

    public PaintedArt(Resources resources) {
        Bitmap image=null;
        try {
            image=BitmapFactory.decodeResource(resources,R.drawable.riftguard_atlas);
        } catch(RuntimeException ignored) {
            // Native Canvas fallback is drawn if a device cannot decode WebP.
        }
        atlas=image;
    }
    public boolean available() {
        return atlas!=null&&!atlas.isRecycled()&&atlas.getWidth()==256&&atlas.getHeight()==192;
    }
    private void draw(Canvas c,int id,float l,float t,float r,float b,float alpha) {
        if(!available())return;
        c.save();
        c.clipRect(l,t,r,b);
        paint.setAlpha((int)Math.max(0,Math.min(255,alpha*255)));
        c.drawBitmap(atlas,SOURCE[id],new RectF(l,t,r,b),paint);
        paint.setAlpha(255);
        c.restore();
    }
    public void worldMap(Canvas c,float l,float t,float r,float b) {
        draw(c,MAP,l,t,r,b,1);
    }
    public void home(Canvas c,float l,float t,float r,float b) {
        draw(c,HOME,l,t,r,b,1);
    }
    public void fortress(Canvas c,float l,float t,float r,float b) {
        draw(c,FORTRESS,l,t,r,b,1);
    }
    public void mission(Canvas c,int map,float l,float t,float r,float b) {
        int id=3+Math.max(0,Math.min(2,map));
        draw(c,id,l,t,r,b,1);
    }
    public void battleAtmosphere(Canvas c,int map,float l,float t,float r,float b) {
        draw(c,3+Math.max(0,Math.min(2,map)),l,t,r,b,.13f);
    }
}
