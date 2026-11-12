package com.lokij07.riftguard;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import static org.junit.Assert.*;

/**
 * Tests actual rendered layers and temporal pose transitions, not names,
 * dummy frame counters or claims about professional/realistic source art.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class VisualOverhaulPhaseOneTest {
    private static Bitmap body(float phase,boolean moving,float attack,float cast,
                               boolean hurt,boolean flip){
        Bitmap b=Bitmap.createBitmap(300,310,Bitmap.Config.ARGB_8888);
        WardenBodyArt.draw(new Canvas(b),146,252,phase,moving,attack,cast,hurt,flip);
        return b;
    }
    private static int diff(Bitmap a,Bitmap b,int x1,int y1,int x2,int y2){
        int count=0;
        for(int y=y1;y<y2;y+=2)for(int x=x1;x<x2;x+=2)
            if(a.getPixel(x,y)!=b.getPixel(x,y))count++;
        return count;
    }
    @Test public void weaponFreeWardenIsFullBodyScalableAndHasNoGearPixels(){
        Bitmap bare=body(.8f,false,-1,-1,false,false);
        int dense=0;
        for(int y=105;y<258;y+=2)for(int x=97;x<207;x+=2)
            if(Color.alpha(bare.getPixel(x,y))>85)dense++;
        assertTrue("Clean original Warden needs armor and legs, not a placeholder",
                dense>300);
        // No fixed sidearm/gun/bow raster sprite exists in body rendering.
        assertEquals(Color.TRANSPARENT,bare.getPixel(242,198));
        assertEquals(Color.TRANSPARENT,bare.getPixel(236,214));
        Bitmap mirrored=body(.8f,false,-1,-1,false,true);
        assertTrue(diff(bare,mirrored,75,105,218,250)>100);
        bare.recycle();mirrored.recycle();
    }
    @Test public void runAttackCastHurtAndCapeHaveDistinctRealRenderedFrames(){
        Bitmap idle=body(.1f,false,-1,-1,false,false);
        Bitmap runA=body(.1f,true,-1,-1,false,false);
        Bitmap runB=body(1.65f,true,-1,-1,false,false);
        Bitmap slash=body(1.2f,false,.55f,-1,false,false);
        Bitmap spell=body(1.2f,false,-1,.55f,false,false);
        Bitmap hurt=body(.1f,false,-1,-1,true,false);
        assertTrue("Running requires planted/raised alternate foot pixels",
                diff(runA,runB,78,165,224,267)>100);
        assertTrue("Run must differ visibly from standing",
                diff(idle,runA,70,130,224,265)>100);
        assertTrue("Attack changes torso/head/cape pose",
                diff(idle,slash,76,116,214,254)>80);
        assertTrue("Cast changes back arm/cape rather than being idle",
                diff(idle,spell,75,122,214,254)>80);
        assertTrue("Damage must produce a real visible reaction",
                diff(idle,hurt,75,105,221,255)>120);
        idle.recycle();runA.recycle();runB.recycle();
        slash.recycle();spell.recycle();hurt.recycle();
    }
    @Test public void everyBiomeHasDistinctLivingOriginalWorldEffects(){
        Bitmap original=Bitmap.createBitmap(840,550,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(original);
        Set<Integer> signatures=new HashSet<>();
        for(int biome=0;biome<10;biome++){
            canvas.drawColor(Color.TRANSPARENT,android.graphics.PorterDuff.Mode.CLEAR);
            BiomeAtmosphere.drawBack(canvas,biome,biome*25,1f);
            BiomeAtmosphere.drawFront(canvas,biome,biome*25,1f);
            int checksum=0x811c9dc5;
            for(int y=25;y<500;y+=5)for(int x=30;x<815;x+=5)
                checksum=(checksum^original.getPixel(x,y))*0x01000193;
            signatures.add(checksum);
        }
        assertEquals("Ten atmospheres must not share the same image",10,
                signatures.size());
        canvas.drawColor(Color.TRANSPARENT,android.graphics.PorterDuff.Mode.CLEAR);
        BiomeAtmosphere.drawFront(canvas,BiomeAtmosphere.SNOW,22,.2f);
        Bitmap first=original.copy(Bitmap.Config.ARGB_8888,false);
        canvas.drawColor(Color.TRANSPARENT,android.graphics.PorterDuff.Mode.CLEAR);
        BiomeAtmosphere.drawFront(canvas,BiomeAtmosphere.SNOW,22,2.4f);
        assertTrue("Weather must actually animate in gameplay",
                diff(first,original,10,15,825,530)>150);
        original.recycle();first.recycle();
    }
    @Test public void originalBodyAndSeparateActualEquippedWeaponBothRender(){
        MotionArt motion=new MotionArt(RuntimeEnvironment.getApplication());
        Bitmap bare=Bitmap.createBitmap(330,330,Bitmap.Config.ARGB_8888);
        Bitmap armed=Bitmap.createBitmap(330,330,Bitmap.Config.ARGB_8888);
        for(int weapon=0;weapon<6;weapon++){
            bare.eraseColor(Color.TRANSPARENT);
            armed.eraseColor(Color.TRANSPARENT);
            assertTrue(motion.heroBody(new Canvas(bare),164,246,.72f,0,
                    true,-1,-1,false));
            assertTrue(motion.hero(new Canvas(armed),164,246,.72f,0,
                    true,-1,-1,false,weapon));
            assertTrue("Only selected weapon is drawn by WeaponRig",
                    diff(bare,armed,130,105,262,255)>40);
        }
        bare.recycle();armed.recycle();
    }
}
