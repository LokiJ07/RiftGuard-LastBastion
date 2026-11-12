package com.lokij07.riftguard;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

/**
 * Ground-bound dynamic lighting. Unlike a UI overlay, these glows share the
 * physical locations of the keep, hazard fields and active moving hero.
 * All effects render before the depth-sorted monsters and preserve HUD contrast.
 */
public final class BattlefieldLighting {
    private BattlefieldLighting(){}
    private static final Paint P=new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final int[] AMBIENT={
            0xff9be9ab,0xff9bd6c9,0xff71cba4,0xffff9557,0xffe5f5ff,
            0xffffb072,0xff79ddf6,0xff8da8ff,0xffc2a0ee,0xffff8f9b
    };
    private static int withAlpha(int color,int alpha){
        return (Math.max(0,Math.min(255,alpha))<<24)|(color&0x00ffffff);
    }
    private static float wobble(float time,float rate,float phase){
        return .86f+.14f*(float)Math.sin(time*rate+phase);
    }
    private static void light(Canvas c,float x,float y,float rx,float ry,
                              float intensity,int color){
        if(rx<=0||ry<=0)return;
        int alpha=Math.min(160,Math.max(0,Math.round(intensity*255)));
        P.setStyle(Paint.Style.FILL);
        P.setShader(new RadialGradient(x,y,rx,
                new int[]{withAlpha(color,alpha),withAlpha(color,alpha/3),
                          withAlpha(color,0)},
                new float[]{0f,.48f,1f},Shader.TileMode.CLAMP));
        c.save();
        c.translate(x,y);
        c.scale(1,ry/rx);
        c.translate(-x,-y);
        c.drawCircle(x,y,rx,P);
        c.restore();
        P.setShader(null);
    }

    public static void drawGround(Canvas c,int biome,int stage,float time,
                                  float keepX,float keepY){
        int id=Math.max(0,Math.min(9,biome));
        if(!Float.isFinite(time))return;
        // Long warm light at the keep's arched gate. The entrance and
        // courtyard are physically brighter than the far land.
        light(c,keepX,keepY+103,304,175,
                .34f*wobble(time,3.4f,1),0xffffcc8c);
        // Locally relevant glows tie the environment to biomes.
        if(id==3||id==5||id==9){
            for(int n=0;n<4;n++){
                float x=300+n*390f;
                float y=155+(n%2)*720f;
                light(c,x,y,215,112,
                        .30f*wobble(time,2.1f,n),AMBIENT[id]);
            }
        }else if(id==4||id==6||id==7){
            for(int n=0;n<3;n++){
                float x=430+n*490f;
                float y=260+(n%2)*510;
                light(c,x,y,225,130,
                        .21f*wobble(time,1.2f,n),AMBIENT[id]);
            }
        }else{
            for(int n=0;n<3;n++){
                float x=375+n*450f;
                float y=300+(n%2)*440;
                light(c,x,y,225,146,
                        .20f*wobble(time,1f,n),AMBIENT[id]);
            }
        }
        // Stage numbers only affect light phase; never randomize world
        // coordinates every frame and cause distracting flicker.
        float pulse=wobble(time,1.2f,stage*.23f);
        light(c,1660,505,255,175,.17f*pulse,AMBIENT[id]);
    }
    public static void drawHeroContact(Canvas c,float heroX,float heroY,
                                       boolean moving,float gait,int biome){
        // Weight-bearing contact shadow follows hero feet. One foot lifts as
        // the other plants, making ground support visible in a moving world.
        float stride=moving?(float)Math.abs(Math.sin(gait)):0;
        P.setShader(null);
        P.setStyle(Paint.Style.FILL);
        P.setColor(0x770c1922);
        c.drawOval(new RectF(heroX-34-stride*7,heroY-2,
                heroX+34+stride*7,heroY+11-stride*1.9f),P);
        P.setShader(new RadialGradient(heroX,heroY+3,
                46,withAlpha(AMBIENT[Math.max(0,Math.min(9,biome))],45),
                0x00000000,Shader.TileMode.CLAMP));
        c.drawOval(new RectF(heroX-46,heroY-14,heroX+46,heroY+18),P);
        P.setShader(null);
    }
}
