package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;

/**
 * Structural battle damage follows actual core HP. Cracks, broken beams and
 * smoke are anchored to the fortress walls (not a detached HUD animation).
 * Pure renderer; never changes simulation state.
 */
public final class FortressDamageFx {
    private FortressDamageFx(){}
    private static final int DARK=0xff28313a,STONE=0xffb1a99c,WOOD=0xffb1936a;
    public static int severity(float current,float maximum){
        if(maximum<=0)return 0;
        float ratio=Math.max(0,Math.min(1,current/maximum));
        return ratio>.74f?0:ratio>.45f?1:ratio>.19f?2:3;
    }
    private static Path crack(float x,float y,float scale,int side){
        Path path=new Path();
        path.moveTo(x,y);
        path.lineTo(x+side*10*scale,y+16*scale);
        path.lineTo(x-side*4*scale,y+27*scale);
        path.lineTo(x+side*16*scale,y+43*scale);
        path.lineTo(x+side*3*scale,y+55*scale);
        return path;
    }
    public static void draw(Canvas c,float x,float y,int rank,float hp,
                            float maximum,float time) {
        int damage=severity(hp,maximum);
        if(damage==0)return;
        boolean stone=rank>=3;
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);
        int edge=stone?DARK:0xff382a20;
        float[] ox=stone?new float[]{-108,99,-52,66}:new float[]{-123,123,-57,57};
        float[] oy=stone?new float[]{-88,-79,32,40}:new float[]{-92,-82,18,31};
        for(int n=0;n<Math.min(4,damage+1);n++){
            float px=x+ox[n],py=y+oy[n];
            float scale=(stone?1.1f:1f)+(n%2)*.22f;
            Path pattern=crack(px,py,scale,n%2==0?1:-1);
            p.setColor(0x448b8179);p.setStrokeWidth(9);
            c.drawPath(pattern,p);
            p.setColor(edge);p.setStrokeWidth(4);
            c.drawPath(pattern,p);
            p.setColor(stone?0xffddcbab:0xffd4a46a);p.setStrokeWidth(1.3f);
            c.save();c.translate(3,0);c.drawPath(pattern,p);c.restore();
            // Cracked masonry is chipped at fracture intersections.
            p.setStyle(Paint.Style.FILL);
            p.setColor(stone?STONE:WOOD);
            Path shard=new Path();
            shard.moveTo(px-7,py+22*scale);
            shard.lineTo(px+12,py+31*scale);
            shard.lineTo(px-12,py+34*scale);
            shard.close();c.drawPath(shard,p);
            p.setStyle(Paint.Style.STROKE);
        }
        p.setStyle(Paint.Style.FILL);
        if(damage>=2){
            // Broken wall panels/repair joists share the fortress's real footprint.
            for(int n=0;n<3;n++){
                float px=x-93+n*90,py=y+(n%2==0?70:43);
                p.setColor(0xff524336);
                c.drawRoundRect(px-26,py-9,px+31,py+4,3,3,p);
                p.setColor(stone?0xffc1a77a:0xff9f6a42);
                c.drawRoundRect(px-25,py-13,px+29,py-4,3,3,p);
                p.setColor(0xffdfcc9a);
                c.drawCircle(px-18,py-7,2.5f,p);
                c.drawCircle(px+22,py-7,2.5f,p);
            }
        }
        if(damage>=2){
            // Wind-driven translucent smoke instead of stationary overlay discs.
            int smoke=damage+1;
            for(int i=0;i<smoke;i++){
                float travel=(time*21+i*26)%112;
                float px=x+(i*73-104)+(float)Math.sin(time*.9f+i)*13;
                float py=y-121-travel;
                float width=10+travel*.20f;
                p.setShader(new RadialGradient(px,py,width,
                        0x68585c5e,0x00585c5e,Shader.TileMode.CLAMP));
                c.drawOval(px-width,py-width*.7f,px+width,py+width*.7f,p);
                p.setShader(null);
            }
        }
        if(damage==3){
            for(int i=0;i<4;i++){
                float px=x-117+i*76,py=y-97-(i%2)*30;
                float glow=5+(float)Math.sin(time*7+i)*2;
                p.setColor(0x88ee7b48);c.drawOval(px-glow*2,py-glow*3,
                        px+glow*2,py+glow*2,p);
                p.setColor(0xffffc47d);c.drawOval(px-3,py-7-glow,
                        px+3,py+3,p);
            }
        }
    }
}
