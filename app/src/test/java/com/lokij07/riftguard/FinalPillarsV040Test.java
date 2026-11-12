package com.lokij07.riftguard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import static org.junit.Assert.*;

/** Production native renderer, offline WAV and simulation QA for v0.40. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class FinalPillarsV040Test {
    private static File folder(String name) {
        File root=new File(System.getProperty("user.dir"));
        File dir=new File(root,new File(root,"app/build.gradle").exists()
                ?"app/build/"+name:"build/"+name);
        if(!dir.exists())assertTrue(dir.mkdirs());
        return dir;
    }
    private static void capture(Bitmap bitmap,String name)throws Exception{
        File file=new File(folder("screenshots"),name+".png");
        try(FileOutputStream out=new FileOutputStream(file)){
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,out));
        }
        assertTrue(file.length()>4000);
    }
    private static int hash(Bitmap bmp){
        int hash=0x811c9dc5;
        for(int y=14;y<bmp.getHeight()-15;y+=9)
            for(int x=12;x<bmp.getWidth()-15;x+=9)
                hash=(hash^bmp.getPixel(x,y))*0x01000193;
        return hash;
    }
    @Test public void allTenKingdomMotifsActuallyLookDifferentAndHaveThreeMissions()
            throws Exception {
        Set<Integer> hashes=new HashSet<>();
        for(int world=0;world<10;world++){
            int stage=Campaign.stage(world*10+1,0);
            Bitmap bmp=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
            Canvas c=new Canvas(bmp);c.drawColor(0xff324c46);
            c.save();c.scale(.62f,.62f);c.translate(0,-50);
            KingdomSetPieces.paint(c,stage);c.restore();
            int signature=hash(bmp);
            assertTrue("Visual realm architecture must differ: "+world,
                    hashes.add(signature));
            capture(bmp,String.format("70-realm-landmarks-%02d",world));
            bmp.recycle();
            for(int m=0;m<3;m++){
                int s=Campaign.stage(world*10+1,m);
                assertEquals(m,Campaign.map(s));
            }
        }
        assertEquals(10,hashes.size());
    }
    @Test public void staticReducedMotionKeepsLandmarkLightsStill(){
        Bitmap bmp=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(bmp);
        KingdomSetPieces.animate(c,5,0,true);
        int a=hash(bmp);
        bmp.eraseColor(Color.TRANSPARENT);
        KingdomSetPieces.animate(c,5,33,true);
        assertEquals(a,hash(bmp));
        bmp.eraseColor(Color.TRANSPARENT);
        KingdomSetPieces.animate(c,5,33,false);
        assertNotEquals(a,hash(bmp));
        bmp.recycle();
    }
    @Test public void sevenSpeciesAndBossCombatAnimationHaveDistinctPixels()
            throws Exception{
        Set<Integer> signatures=new HashSet<>();
        for(int kind=0;kind<7;kind++){
            Bitmap bmp=Bitmap.createBitmap(280,245,Bitmap.Config.ARGB_8888);
            Canvas c=new Canvas(bmp);c.drawColor(0xff152732);
            EnemyAnimationPolish.draw(c,kind,140,208,1.3f,.4f,.13f,kind==5,3);
            int h=hash(bmp);
            assertTrue("Enemy "+kind+" should have distinct combat equipment",
                    signatures.add(h));
            capture(bmp,"80-enemy-motion-"+kind);
            bmp.recycle();
        }
        Bitmap sprite=Bitmap.createBitmap(280,245,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(sprite);
        EnemyAnimationPolish.draw(c,5,140,208,1,.4f,0,true,1);
        int phase1=hash(sprite);
        sprite.eraseColor(Color.TRANSPARENT);
        EnemyAnimationPolish.draw(c,5,140,208,1,.4f,0,true,3);
        assertNotEquals(phase1,hash(sprite));
        sprite.recycle();
    }
    @Test public void fourteenOfflineAudioCuesAreValidAndUnique(){
        assertEquals(14,GameAudio.COUNT);
        Set<Integer> sounds=new HashSet<>();
        for(int type=0;type<GameAudio.COUNT;type++){
            byte[] wav=GameAudio.synthesize(type);
            assertTrue("WAV must have data "+type,wav.length>3000);
            assertEquals('R',(char)wav[0]);assertEquals('W',(char)wav[8]);
            int check=Arrays.hashCode(wav);
            assertTrue("Audio cue must be distinct "+type,sounds.add(check));
        }
    }
    @Test public void actualCanvasPerformanceSampleIsRecordedNotMisrepresentedAsPhoneFps()
            throws Exception {
        Context context=RuntimeEnvironment.getApplication();
        context.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE)
                .edit().clear().putInt("fortress_rank",5).commit();
        RiftGameView game=new RiftGameView(context);
        game.layout(0,0,1280,720);
        Method start=RiftGameView.class.getDeclaredMethod("start",int.class);
        start.setAccessible(true);
        start.invoke(game,152);
        Bitmap bmp=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(bmp);
        game.setActive(false); // measured drawing without non-deterministic physics
        for(int i=0;i<4;i++)game.draw(c);
        long[] durations=new long[18];
        for(int i=0;i<durations.length;i++){
            long begin=System.nanoTime();
            game.draw(c);
            durations[i]=System.nanoTime()-begin;
        }
        Arrays.sort(durations);
        double p50=durations[durations.length/2]/1e6;
        double p95=durations[(int)Math.floor(durations.length*.95)]/1e6;
        assertTrue(p50>0);
        assertTrue(p95>=p50);
        String result="{\n  \"platform\": \"Robolectric JVM software Canvas\",\n"+
                "  \"not_real_device_fps\": true,\n"+
                "  \"resolution\": \"1280x720\",\n"+
                "  \"stage\": 152,\n"+
                "  \"frames\": "+durations.length+",\n"+
                "  \"p50_draw_ms\": "+p50+",\n"+
                "  \"p95_draw_ms\": "+p95+"\n}\n";
        Files.write(new File(folder("performance"),"robolectric_canvas.json").toPath(),
                result.getBytes(StandardCharsets.UTF_8));
        capture(bmp,"90-stage-152-real-battle-after-art-pass");
        bmp.recycle();
    }
}