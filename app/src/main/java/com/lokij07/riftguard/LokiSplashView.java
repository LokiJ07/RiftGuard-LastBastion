package com.lokij07.riftguard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.View;

/**
 * Real native startup branding: creator credit LokiJ.
 * Deliberately does NOT show a fake percentage or fake network download.
 * The Activity constructs the game after this frame has been displayed.
 */
public final class LokiSplashView extends View {
    private static final int GOLD=0xffedca89;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private long created=System.nanoTime();

    public LokiSplashView(Context context){super(context);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
    private void rect(Canvas c,float l,float t,float r,float b,int color){
        p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);
        c.drawRect(l,t,r,b,p);
    }
    private void label(Canvas c,String value,float x,float y,float size,int color){
        p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(Typeface.create("sans-serif-condensed",Typeface.BOLD));
        p.setTextSize(size);c.drawText(value,x,y,p);
    }
    private void poly(Canvas c,int color,float...xy){
        Path q=new Path();q.moveTo(xy[0],xy[1]);
        for(int i=2;i<xy.length;i+=2)q.lineTo(xy[i],xy[i+1]);
        q.close();p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);
        c.drawPath(q,p);
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        float w=getWidth(),h=getHeight();
        if(w<=0||h<=0)return;
        p.setShader(new LinearGradient(0,0,w,h,0xff081a26,0xff275052,
                Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);p.setShader(null);
        float sx=w/1280f,sy=h/720f;
        float scale=Math.min(sx,sy);
        c.save();c.translate((w-1280*scale)/2,(h-720*scale)/2);
        c.scale(scale,scale);
        for(int k=0;k<7;k++){
            float x=k*210-90;
            poly(c,0xff193945,x,595,x+180,270+(k%3)*62,x+380,595);
            poly(c,0xff12323b,x+47,596,x+180,385+(k%2)*58,x+320,596);
        }
        rect(c,0,594,1280,720,0xff102832);
        // Emblem has clear hierarchy even at low device resolutions.
        float x=640,y=270;
        poly(c,0xff947446,x-122,y-124,x+122,y-124,x+117,y+37,
                x+63,y+142,x,y+180,x-63,y+142,x-117,y+37);
        poly(c,GOLD,x-110,y-111,x+110,y-111,x+105,y+31,
                x+57,y+127,x,y+163,x-57,y+127,x-105,y+31);
        poly(c,0xff204958,x-97,y-96,x+97,y-96,x+90,y+27,
                x+48,y+112,x,y+144,x-48,y+112,x-90,y+27);
        // Ancient fortress silhouette with distinct crenellations.
        poly(c,0xffe6ddbf,x-67,y+58,x-67,y-18,x-51,y-18,x-51,y-35,
                x-31,y-35,x-31,y-16,x-13,y-16,x-13,y-45,
                x+12,y-45,x+12,y-16,x+32,y-16,x+32,y-35,
                x+51,y-35,x+51,y-18,x+67,y-18,x+67,y+58);
        rect(c,x-13,y+18,x+13,y+58,0xff254c5b);
        // Blade through the shield: icon echoes the gameplay's rune sword.
        poly(c,0xff9dd5d6,x-8,y-99,x+8,y-99,x+6,y+8,x,y+25,x-6,y+8);
        rect(c,x-31,y-9,x+31,y+1,0xffedc986);
        rect(c,x-6,y-7,x+6,y+43,0xffa8784a);
        label(c,"RIFTGUARD",640,530,70,0xfff5e8c9);
        label(c,"THE LAST BASTION",640,573,27,0xffb9d4d0);
        label(c,"A GAME BY  LokiJ",640,637,22,GOLD);
        long elapsed=Math.max(0,System.nanoTime()-created);
        float breath=(float)Math.sin(elapsed/1000000000.0*2.8);
        // Genuine indeterminate visual: never claims an inaccurate progress %.
        float sweep=(float)(elapsed/1000000000.0*.65%1);
        rect(c,463,674,817,678,0xff476469);
        rect(c,463+300*sweep,674,515+300*sweep,678,0xffe8c98a);
        label(c,"PREPARING YOUR KINGDOM",640,701,14,0xffa8c8c7);
        c.restore();
        if(getWindowToken()!=null)postInvalidateDelayed(34);
    }
}
