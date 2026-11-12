package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;

/**
 * Original, scale-independent multi-layer world atmosphere with biome-specific
 * geometry, drifting haze, light shafts and particle motion. Seeded world
 * coordinates ensure particles look stable as the camera moves; no network or
 * giant opaque overlaid images. Split behind and ahead of actor depth.
 */
public final class BiomeAtmosphere {
    private BiomeAtmosphere() {}
    public static final int FOREST=0, HOLLOW=1, SWAMP=2, ASH=3, SNOW=4,
            IRON=5, CRYSTAL=6, TEMPEST=7, SHADOW=8, ECLIPSE=9;
    private static final Paint P=new Paint(Paint.ANTI_ALIAS_FLAG);
    public static final int[] LIGHT={
            0xffb5ecb4,0xffb9e8c8,0xff9adbc5,0xffffbc82,0xffe8f7ff,
            0xffffc58c,0xff86eaf9,0xff9cbefa,0xffa58ad0,0xffffbba0
    };
    private static int tint(int base,int alpha){
        return (Math.max(0,Math.min(255,alpha))<<24)|(base&0x00ffffff);
    }
    private static float fract(float x){return x-(float)Math.floor(x);}
    private static float hash(float x,float y){
        return fract((float)Math.sin(x*127.1f+y*311.7f)*43758.545f);
    }
    private static void oval(Canvas c,float x,float y,float rx,float ry,int color){
        P.setStyle(Paint.Style.FILL);P.setShader(null);P.setColor(color);
        c.drawOval(x-rx,y-ry,x+rx,y+ry,P);
    }
    /** Semi-transparent environmental light interacts with the real road. */
    public static void drawBack(Canvas c,int biome,int stage,float time) {
        drawBack(c,biome,stage,time,false);
    }
    public static void drawBack(Canvas c,int biome,int stage,float time,
                                boolean lowEffects) {
        int id=Math.max(0,Math.min(9,biome));
        if(!Float.isFinite(time))return;
        float shift=(float)Math.sin(time*.12f+stage*.4f)*30f;
        // Subtle luminous beam volumes behind monsters and buildings.
        for(int i=0;i<(lowEffects?2:5);i++){
            float x=180+i*380+shift;
            int light=tint(LIGHT[id],id==3||id==9?22:13);
            Path beam=new Path();
            beam.moveTo(x-53,-80);
            beam.lineTo(x+76,-80);
            beam.lineTo(x+160,1000);
            beam.lineTo(x-195,1000);
            beam.close();
            P.setShader(new LinearGradient(x,-40,x+80,1050,
                    tint(LIGHT[id],id==7?7:19),0x00000000,Shader.TileMode.CLAMP));
            P.setColor(light);
            c.drawPath(beam,P);
            P.setShader(null);
        }
        // Spatially stable pools of volumetric fog and lighting, ten different
        // atmospheres instead of generic white dots on all worlds.
        for(int i=0;i<(lowEffects?5:12);i++){
            float x=hash(i+stage*.03f,3)*1900;
            float y=hash(i*.7f,5+stage*.011f)*1020;
            float motion=(float)Math.sin(time*.21f+i)*22f;
            float rad=80+hash(i,7)*85;
            if(id==SWAMP||id==SHADOW||id==HOLLOW||id==SNOW){
                P.setShader(new RadialGradient(x+motion,y,rad,
                        tint(LIGHT[id],id==SNOW?27:36),0x00000000,
                        Shader.TileMode.CLAMP));
                c.drawOval(x-rad+motion,y-rad*.5f,
                        x+rad+motion,y+rad*.5f,P);
                P.setShader(null);
            }
            if(id==ASH||id==IRON||id==ECLIPSE) {
                // Heated ground pockets instead of a uniform red film.
                oval(c,x,y,20+rad*.5f,11+rad*.13f,
                        tint(LIGHT[id],19));
            }
        }
    }
    /** World space particles are always drawn ahead of actors but below HUD. */
    public static void drawFront(Canvas c,int biome,int stage,float time) {
        drawFront(c,biome,stage,time,false);
    }
    public static void drawFront(Canvas c,int biome,int stage,float time,
                                 boolean lowEffects) {
        int id=Math.max(0,Math.min(9,biome));
        if(!Float.isFinite(time))return;
        int color=LIGHT[id];
        int particles=lowEffects?(id==SNOW||id==TEMPEST?28:20):
                (id==SNOW||id==TEMPEST?64:44);
        for(int i=0;i<particles;i++){
            float seed=hash(i+13,stage*.73f+17f);
            float x=hash(i+33f,stage*.17f+9f)*1900;
            float y=hash(i*1.37f,stage*.11f+29f)*1050;
            float parallax=.65f+seed*.9f;
            float wx=(id==TEMPEST?100:id==SNOW?-29:id==ASH?39:12)*parallax;
            float wy=(id==TEMPEST?82:id==SNOW?33:id==ASH?-45:7)*parallax;
            x=(x+wx*time)%1900;
            y=(y+wy*time)%1050;
            if(x<0)x+=1900;if(y<0)y+=1050;
            float size=1.5f+seed*3.9f;
            if(id==SNOW||id==TEMPEST){
                P.setShader(null);P.setStrokeWidth(id==TEMPEST?2.6f:1.6f);
                P.setStyle(Paint.Style.STROKE);
                P.setColor(tint(color,id==SNOW?145:78));
                c.drawLine(x,y,x-(id==TEMPEST?22:4),
                        y-(id==TEMPEST?12:8),P);
            }else if(id==ASH||id==IRON||id==ECLIPSE){
                oval(c,x,y,size,size*.65f,tint(color,90+(int)(seed*70)));
                if(seed>.85f) {
                    P.setColor(0x66ffda93);P.setStrokeWidth(1.5f);
                    P.setStyle(Paint.Style.STROKE);
                    c.drawLine(x,y+8,x+5,y-4,P);
                }
            }else if(id==CRYSTAL){
                Path shard=new Path();
                shard.moveTo(x,y-size*2);
                shard.lineTo(x+size,y);
                shard.lineTo(x,y+size*2);
                shard.lineTo(x-size,y);
                shard.close();
                P.setStyle(Paint.Style.FILL);
                P.setColor(tint(color,95));c.drawPath(shard,P);
            }else if(id==SWAMP){
                oval(c,x,y,size*3,size,tint(color,40));
                if(seed>.75f)oval(c,x,y-5,size*.8f,size*.8f,0x8869e7a3);
            }else{
                // Forest pollen and hollow-fireflies hover instead of raining.
                y+=(float)Math.sin(time*1.7f+i)*9;
                float glow=size*(id==HOLLOW?3.2f:1.8f);
                P.setShader(new RadialGradient(x,y,glow,
                        tint(color,id==HOLLOW?91:50),0x00000000,
                        Shader.TileMode.CLAMP));
                c.drawCircle(x,y,glow,P);P.setShader(null);
                oval(c,x,y,size*.4f,size*.4f,tint(color,175));
            }
        }
        P.setStyle(Paint.Style.FILL);
        P.setShader(null);
        // Moving atmospheric overlay is subtle enough not to hide actors.
    }
}
