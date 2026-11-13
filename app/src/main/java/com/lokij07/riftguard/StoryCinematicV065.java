package com.lokij07.riftguard;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;

/**
 * v0.65 cinematic director: full-bleed authored composition, parallax camera,
 * world-specific backdrops, speaker lighting, illustrated Tessa/Bantay props,
 * memory-grade grading and movie subtitle placement. Original raster
 * illustrations remain a production milestone; never label these as final art.
 */
public final class StoryCinematicV065 {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    private Bitmap backdrop;
    private int cachedBiome=-1,cachedVariant=-1;
    private static final int GOLD=0xffe9c68e,INK=0xfff4ece0;
    private static int grade(int world,boolean memory){
        if(memory)return 0x423e322b;
        if(world>=8)return 0x66301c51;
        if(world>=4)return 0x55243750;
        return 0x33263a43;
    }
    private void color(int color){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);}
    private void circle(Canvas c,float x,float y,float r,int color){
        color(color);c.drawCircle(x,y,r,p);
    }
    private void rect(Canvas c,float l,float t,float r,float b,int color){
        color(color);c.drawRect(l,t,r,b,p);
    }
    private void stroke(Canvas c,float x,float y,float xx,float yy,int color,float width){
        color(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(width);
        p.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x,y,xx,yy,p);p.setStyle(Paint.Style.FILL);
    }
    private void poly(Canvas c,int fill,float...pts){
        color(fill);Path path=new Path();path.moveTo(pts[0],pts[1]);
        for(int i=2;i<pts.length;i+=2)path.lineTo(pts[i],pts[i+1]);
        path.close();c.drawPath(path,p);
    }
    private void prepare(int world,int variant){
        if(backdrop!=null&&cachedBiome==world&&cachedVariant==variant)return;
        release();
        backdrop=Bitmap.createBitmap(1280,720,Bitmap.Config.RGB_565);
        Canvas b=new Canvas(backdrop);
        WorldArt.drawScene(b,world,variant,0,0,1280,720,0);
        cachedBiome=world;cachedVariant=variant;
    }
    public void release(){
        if(backdrop!=null&&!backdrop.isRecycled())backdrop.recycle();
        backdrop=null;cachedBiome=-1;cachedVariant=-1;
    }
    /** Camera treatment for a single dialogue beat; caller draws real actors. */
    public void drawStage(Canvas c,int world,boolean memory,boolean cael,
                          int beat,float seconds){
        int biome=Math.max(0,Math.min(9,world)),variant=(beat+biome)%3;
        prepare(biome,variant);
        // Slow, film-like virtual camera push. Crop outside gutters.
        float zoom=1.045f+Math.min(.05f,seconds*.004f);
        c.save();
        c.translate(640,335);
        c.scale(zoom,zoom);
        c.translate(-640+(float)Math.sin(seconds*.17f+beat)*9,
                    -335+(float)Math.cos(seconds*.13f)*5);
        p.setShader(null);p.setColor(Color.WHITE);
        c.drawBitmap(backdrop,0,0,p);
        c.restore();
        rect(c,0,0,1280,720,grade(biome,memory));
        // Aperture/vignette and world-specific glow are intentionally subtle.
        p.setShader(new RadialGradient(cael?900:460,205,660,
                new int[]{0x007a5770,0x440d1826,0xd20a101c},null,
                Shader.TileMode.CLAMP));
        c.drawRect(0,0,1280,720,p);p.setShader(null);
        p.setShader(new LinearGradient(0,0,0,720,
                new int[]{0x95080f1b,0x00182232,0x120a1019,0xf10b1421},
                new float[]{0f,.23f,.58f,1f},Shader.TileMode.CLAMP));
        c.drawRect(0,0,1280,720,p);p.setShader(null);
        if(cael || biome>=8)drawRift(c,963,160,seconds);
        if(memory) {
            for(int i=0;i<3;i++)
                stroke(c,72+i*9,74,72+i*9,503,0x557a6551,2);
        }
        for(int i=0;i<32;i++){
            float x=(float)((i*137.7+seconds*(6+i%4*3))%1280);
            float y=42+(i*193)%470+(float)Math.sin(seconds*1.3+i)*9;
            circle(c,x,y,1.3f+(i%4)*.4f,
                    memory?0x88dfc58e:cael?0x779ed6fe:0x99f8cb94);
        }
        // Cinematic letterbox, not the old opaque dialogue modal.
        rect(c,0,0,1280,39,0xe8040912);
        rect(c,0,680,1280,720,0xf0070c16);
    }
    private void drawRift(Canvas c,float x,float y,float seconds){
        float pulse=(float)Math.sin(seconds*1.6f);
        circle(c,x,y,89+pulse*5,0x335e2f93);
        circle(c,x,y,63+pulse*4,0x665a3ba0);
        circle(c,x,y,42,0xff13102c);
        for(int i=0;i<9;i++){
            float a=i*.698f+seconds*.06f;
            float r=85+(float)Math.sin(seconds+i)*9;
            float xx=x+(float)Math.cos(a)*r, yy=y+(float)Math.sin(a)*r;
            stroke(c,xx,yy,xx+(float)Math.cos(a)*24,
                    yy+(float)Math.sin(a)*24,0x88cab7ff,2+(i%3));
        }
    }
    /** An explicitly recognizable cup and restored sword, not generic circles. */
    public void drawTreasure(Canvas c,float x,float y,float seconds,int kind){
        c.save();c.translate(x,y);
        if(kind==0){ // Coffee cup and steam
            for(int i=0;i<3;i++){
                float sway=(float)Math.sin(seconds*2+i)*5;
                stroke(c,-12+i*11+sway,-35,-10+i*11-sway,-76,
                        0x99ead8b4,3);
            }
            rect(c,-33,-31,28,20,0xffd3b58b);
            rect(c,-25,-28,20,10,0xffa07352);
            color(0xffd0b899);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(7);
            c.drawArc(18,-22,55,10,-82,210,false,p);p.setStyle(Paint.Style.FILL);
            circle(c,-5,2,11,0xff5d362d);
            rect(c,-46,20,47,27,0xffe7d2aa);
        }else if(kind==1){ // Chada
            stroke(c,0,40,0,-95,0xffe6e6dc,13);
            poly(c,0xffb8daee,-13,-90,0,-138,13,-90);
            stroke(c,-29,18,29,18,0xffd8ae74,12);
            circle(c,0,47,10,0xffe9bf79);
            circle(c,0,16,5,0xffa3daf7);
        }else{ // Bantay's collar
            color(0xff94764d);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(17);
            c.drawOval(-43,-30,43,21,p);p.setStyle(Paint.Style.FILL);
            rect(c,-9,20,9,40,0xffd2b378);
            circle(c,0,43,13,0xfff7d38c);
        }
        c.restore();
    }
    /** Dedicated recognizable older brewer portrait for Tessa story beats. */
    public void drawTessa(Canvas c,float x,float y,float size,float seconds){
        c.save();c.translate(x,y);c.scale(size,size);
        circle(c,0,0,122,0x22201b20);
        poly(c,0xff826d55,-74,0,-90,116,80,116,70,1);
        poly(c,0xffc09e79,-68,12,-80,105,5,121,73,105,65,14);
        circle(c,8,-61,55,0xffd7bba0);
        circle(c,51,-115,22,0xffe5e3d8);
        poly(c,0xffe5e4dc,-50,-87,-33,-139,31,-145,56,-93,
                43,-104,10,-117,-22,-97);
        stroke(c,-18,-59,-2,-56,0xff6f5d4d,3);
        stroke(c,24,-55,40,-58,0xff6f5d4d,3);
        stroke(c,1,-29,11,-25,0xffb18a72,2);
        stroke(c,-1,-14,28,-11,0xff8c6256,2);
        circle(c,54,-118,9,0xfff5f0e9);
        c.restore();
    }
    /** Loyal dog reacts nonverbally, as required by the story source. */
    public void drawBantay(Canvas c,float x,float y,float size,float seconds){
        c.save();c.translate(x,y);c.scale(size,size);
        rect(c,-52,23,48,90,0xff775b48);
        circle(c,1,-16,58,0xffad8761);
        poly(c,0xff76513d,-35,-37,-73,-85,-74,-17,-42,5);
        poly(c,0xff76513d,34,-37,70,-85,74,-13,39,3);
        circle(c,-18,-17,7,0xff211b1d);
        circle(c,21,-17,7,0xff211b1d);
        circle(c,-16,-19,2,0xfff6e5c7);circle(c,23,-19,2,0xfff6e5c7);
        circle(c,1,13,18,0xffe9d0ad);
        circle(c,1,3,10,0xff33272b);
        stroke(c,-6,23,8,23,0xff493131,2);
        stroke(c,-52,55,-60,94,0xffad8761,13);
        stroke(c,46,55,57,94,0xffad8761,13);
        stroke(c,48,49,78+(float)Math.sin(seconds*4)*6,23,0xffad8761,14);
        c.restore();
    }
}