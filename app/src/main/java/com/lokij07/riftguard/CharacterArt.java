package com.lokij07.riftguard;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

/** Original AI-generated individual Warden and monster designs, not baked-in enemies. */
public final class CharacterArt {
    private final Bitmap atlas;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG|Paint.DITHER_FLAG);
    private static final Rect HERO=new Rect(0,0,240,300);
    private static final Rect[] MONSTERS={
        new Rect(240,0,368,150),    // goblin runner
        new Rect(368,0,496,150),    // goblin slinger
        new Rect(496,0,624,150),    // forest brute
        new Rect(240,150,368,300),  // wild boar
        new Rect(368,150,496,300),  // totem shaman
        new Rect(496,150,624,300)  // thornhide siege beast
    };
    public CharacterArt(Resources res) {
        Bitmap decoded=null;
        try { decoded=BitmapFactory.decodeResource(res,R.drawable.riftguard_characters); }
        catch(RuntimeException ignored) { /* fallback to Canvas avatars */ }
        atlas=decoded;
    }
    public boolean available() {
        return atlas!=null&&!atlas.isRecycled()&&atlas.getWidth()==624&&atlas.getHeight()==300;
    }
    public void hero(Canvas c,float x,float y,float time,boolean moving,boolean attacking,boolean hurt) {
        if(!available()) return;
        float bob=moving?(float)Math.sin(time*13)*2.5f:(float)Math.sin(time*2.1f)*1.4f;
        c.save();c.translate(x,y+bob);
        float tilt=moving?(float)Math.sin(time*7)*3f:0;
        if(attacking)tilt-=9;
        c.rotate(tilt,0,-20);
        if(hurt)paint.setAlpha(175);
        c.drawBitmap(atlas,HERO,new RectF(-39,-89,39,7),paint);
        paint.setAlpha(255);
        c.restore();
    }
    public boolean enemy(Canvas c,int kind,float x,float y,float time,boolean hurt) {
        if(!available())return false;
        int sprite;
        if(kind==0)sprite=0;
        else if(kind==1)sprite=1;
        else if(kind==2)sprite=2;
        else if(kind==4)sprite=4;
        else if(kind==5)sprite=5;
        else if(kind==6)sprite=3;
        else return false; // flight enemy intentionally remains its own winged vector form.
        float width=kind==5?100:kind==2?72:kind==6?61:46;
        float height=kind==5?106:kind==2?85:kind==6?63:57;
        float bob=(float)Math.sin(time*6+kind)*2.4f;
        c.save();
        c.translate(x,y+bob);
        c.rotate((float)Math.sin(time*4+kind)*3,0,-25);
        if(hurt)paint.setAlpha(185);
        c.drawBitmap(atlas,MONSTERS[sprite],new RectF(-width/2,-height,width/2,5),paint);
        paint.setAlpha(255);
        c.restore();
        return true;
    }
}
