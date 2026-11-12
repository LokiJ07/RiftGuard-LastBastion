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
        // Frame comparison covers the WHOLE world instead of cropping the
        // north-west quarter, where most seeded snow could be absent.
        canvas.drawColor(Color.TRANSPARENT,android.graphics.PorterDuff.Mode.CLEAR);
        canvas.save();canvas.scale(840f/1900,550f/1050);
        BiomeAtmosphere.drawFront(canvas,BiomeAtmosphere.SNOW,22,.2f);
        canvas.restore();
        Bitmap first=original.copy(Bitmap.Config.ARGB_8888,false);
        canvas.drawColor(Color.TRANSPARENT,android.graphics.PorterDuff.Mode.CLEAR);
        canvas.save();canvas.scale(840f/1900,550f/1050);
        BiomeAtmosphere.drawFront(canvas,BiomeAtmosphere.SNOW,22,2.4f);
        canvas.restore();
        int movingPixels=0;
        for(int y=0;y<550;y++)for(int x=0;x<840;x++)
            if(first.getPixel(x,y)!=original.getPixel(x,y))movingPixels++;
        assertTrue("Snow must physically move across the FULL battlefield ("+
                movingPixels+" pixels)",movingPixels>150);
        original.recycle();first.recycle();
    }
    @Test public void realProjectileTriggersTimedEnemyStaggerAnimation()
            throws Exception {
        android.content.Context context=RuntimeEnvironment.getApplication();
        context.getSharedPreferences("riftguard_campaign_v1",
                android.content.Context.MODE_PRIVATE).edit().clear().commit();
        RiftGameView live=new RiftGameView(context);
        java.lang.reflect.Method start=RiftGameView.class.getDeclaredMethod("start",int.class);
        java.lang.reflect.Method spawn=RiftGameView.class.getDeclaredMethod("spawnEnemy");
        java.lang.reflect.Method fire=RiftGameView.class.getDeclaredMethod("fire");
        java.lang.reflect.Method update=RiftGameView.class.getDeclaredMethod("update",float.class);
        start.setAccessible(true);spawn.setAccessible(true);
        fire.setAccessible(true);update.setAccessible(true);
        start.invoke(live,0);spawn.invoke(live);
        java.lang.reflect.Field heroX=RiftGameView.class.getDeclaredField("heroX");
        java.lang.reflect.Field heroY=RiftGameView.class.getDeclaredField("heroY");
        java.lang.reflect.Field enemies=RiftGameView.class.getDeclaredField("enemies");
        heroX.setAccessible(true);heroY.setAccessible(true);enemies.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.List<Object> group=(java.util.List<Object>)enemies.get(live);
        Object enemy=group.get(0);
        java.lang.reflect.Field x=enemy.getClass().getDeclaredField("x"),
                y=enemy.getClass().getDeclaredField("y"),
                hp=enemy.getClass().getDeclaredField("hp"),
                reaction=enemy.getClass().getDeclaredField("hitReaction");
        x.setAccessible(true);y.setAccessible(true);hp.setAccessible(true);
        reaction.setAccessible(true);
        float xx=heroX.getFloat(live),yy=heroY.getFloat(live);
        x.setFloat(enemy,xx+142);y.setFloat(enemy,yy);
        hp.setFloat(enemy,1800f);
        fire.invoke(live);
        update.invoke(live,.18f);
        // The projectile integrator advances to its target on frame one and
        // resolves collision at the start of the following simulation frame.
        update.invoke(live,.016f);
        assertTrue("An ACTUAL projectile impact must animate the enemy",
                reaction.getFloat(enemy)>0);
        update.invoke(live,.28f);
        assertEquals("Hit-stagger must end instead of staying stuck",
                0f,reaction.getFloat(enemy),.0001f);
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
