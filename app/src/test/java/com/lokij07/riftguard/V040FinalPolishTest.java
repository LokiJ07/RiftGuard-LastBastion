package com.lokij07.riftguard;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.Locale;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import static org.junit.Assert.*;

/** Tests actual graphics/audio state and captures native Android images. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class V040FinalPolishTest {
    private static File gallery(){
        File cwd=new File(System.getProperty("user.dir"));
        File dir=new File(new File(cwd,"app/build.gradle").exists()?
                "app/build/screenshots":"build/screenshots");
        if(!dir.exists())assertTrue(dir.mkdirs());
        return dir;
    }
    private static void save(Bitmap picture,String filename)throws Exception {
        File target=new File(gallery(),filename);
        try(FileOutputStream stream=new FileOutputStream(target)){
            assertTrue(picture.compress(Bitmap.CompressFormat.PNG,100,stream));
        }
        assertTrue(target.getName(),target.length()>2000);
    }
    private static int hash(Bitmap b) {
        int h=0x811c9dc5;
        for(int y=8;y<b.getHeight()-8;y+=5)
            for(int x=8;x<b.getWidth()-8;x+=5)
                h=(h^b.getPixel(x,y))*0x01000193;
        return h;
    }
    @Test public void eachOfTenKingdomsHasUniqueActualPaintedEnvironment()throws Exception {
        NativeDeployedMapArt renderer=new NativeDeployedMapArt();
        Set<Integer> fingerprints=new HashSet<>();
        for(int kingdom=0;kingdom<10;kingdom++){
            int stage=Campaign.stage(kingdom*10+1,kingdom%3);
            Bitmap shot=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);
            Canvas c=new Canvas(shot);c.scale(960f/1900f,540f/1050f);
            renderer.draw(c,stage,1f);
            assertTrue("Different realm "+kingdom+" must show distinctive art",
                    fingerprints.add(hash(shot)));
            assertEquals(kingdom,KingdomLandmarkArt.family(stage));
            save(shot,String.format(Locale.US,"70-v040-realm-%02d.png",kingdom));
            shot.recycle();
        }
        assertEquals(10,fingerprints.size());
        renderer.release();
    }
    @Test public void newKingdomsHaveGenuineMotionAndStaticReducedMotion(){
        NativeDeployedMapArt renderer=new NativeDeployedMapArt();
        Bitmap a=Bitmap.createBitmap(640,360,Bitmap.Config.ARGB_8888);
        Bitmap b=Bitmap.createBitmap(640,360,Bitmap.Config.ARGB_8888);
        Bitmap c=Bitmap.createBitmap(640,360,Bitmap.Config.ARGB_8888);
        Bitmap d=Bitmap.createBitmap(640,360,Bitmap.Config.ARGB_8888);
        Canvas ca=new Canvas(a),cb=new Canvas(b),cc=new Canvas(c),cd=new Canvas(d);
        ca.scale(640f/1900,360f/1050);cb.scale(640f/1900,360f/1050);
        cc.scale(640f/1900,360f/1050);cd.scale(640f/1900,360f/1050);
        renderer.draw(ca,90,0,false);
        renderer.draw(cb,90,6,false);
        assertNotEquals("Actual landmarks must animate",hash(a),hash(b));
        renderer.draw(cc,90,0,true);
        renderer.draw(cd,90,6,true);
        assertEquals("Accessibility: reduced motion freezes scenery",
                hash(c),hash(d));
        assertEquals("No full-scene bitmap allocations for animation",1,
                renderer.cacheBuilds());
        renderer.release();
        a.recycle();b.recycle();c.recycle();d.recycle();
    }
    @Test public void allNewOfflineAudioHasValidDistinctPcmAndNoSilence(){
        Set<Integer> hashes=new HashSet<>();
        for(int type=GameAudio.DODGE;type<=GameAudio.AMBIENT_EMBER;type++){
            byte[] data=GameAudio.synthesize(type);
            assertTrue(data.length>5000);
            assertEquals('R',(char)data[0]);
            assertEquals('W',(char)data[8]);
            assertEquals('A',(char)data[9]);
            assertEquals('V',(char)data[10]);
            assertEquals('E',(char)data[11]);
            int h=1;boolean audible=false;
            for(int i=44;i<data.length;i+=7){
                h=31*h+data[i];
                if(data[i]!=0)audible=true;
            }
            assertTrue("Audio must not be silent: "+type,audible);
            assertTrue("Different sound families are required: "+type,
                    hashes.add(h));
        }
    }
    @Test public void actualBossPhasesAndEnemyStrikeTimingChangePixels()throws Exception{
        Bitmap first=Bitmap.createBitmap(680,380,Bitmap.Config.ARGB_8888);
        Bitmap next=Bitmap.createBitmap(680,380,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(first),c2=new Canvas(next);
        CombatAnimationLayers.enemy(c,5,330,284,0,-1,0,1,true);
        CombatAnimationLayers.enemy(c2,5,330,284,.18f,.62f,.14f,3,true);
        assertNotEquals("Boss phases and actual contact make visible animation",
                hash(first),hash(next));
        save(first,"80-v040-boss-phase-one-rig.png");
        save(next,"81-v040-boss-phase-three-contact-rig.png");
        first.recycle();next.recycle();
        Bitmap hero=Bitmap.createBitmap(420,350,Bitmap.Config.ARGB_8888);
        CombatAnimationLayers.warden(new Canvas(hero),185,210,0,.38f,-1f,.43f);
        assertNotEquals(Color.TRANSPARENT,hero.getPixel(185,155));
        save(hero,"82-v040-warden-contact-and-dodge-layer.png");
        hero.recycle();
    }
    @Test public void androidCanvasCpuRenderBenchmarkCapturesMeasuredSamples()
            throws Exception{
        NativeDeployedMapArt renderer=new NativeDeployedMapArt();
        long[] frameNs=new long[24];int sample=0;
        Bitmap viewport=Bitmap.createBitmap(640,360,Bitmap.Config.RGB_565);
        Canvas c=new Canvas(viewport);c.scale(640f/1900,360f/1050);
        for(int stage:new int[]{0,93,120,211,299}){
            renderer.prepare(stage);
            for(int i=0;i<4;i++){
                long before=System.nanoTime();
                renderer.draw(c,stage,i*.13f,false);
                frameNs[sample++]=System.nanoTime()-before;
            }
        }
        java.util.Arrays.sort(frameNs,0,sample);
        double median=frameNs[sample/2]/1_000_000.0;
        double p95=frameNs[(int)((sample-1)*.95)]/1_000_000.0;
        File output=new File(gallery(),"83-v040-robolectric-frame-metrics.txt");
        try(java.io.FileWriter writer=new java.io.FileWriter(output)){
            writer.write("Environment=Robolectric software Canvas, NOT physical device\n");
            writer.write("Measured painted scenes="+sample+"\n");
            writer.write(String.format(Locale.US,"render_ms_p50=%.3f\nrender_ms_p95=%.3f\n",
                    median,p95));
            writer.write("stage_rebuilds="+renderer.cacheBuilds()+"\n");
        }
        assertEquals(5,renderer.cacheBuilds());
        assertTrue("Measurements must be finite",Double.isFinite(p95));
        viewport.recycle();renderer.release();
    }
}
