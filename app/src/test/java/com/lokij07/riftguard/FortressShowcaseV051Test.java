package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.SystemClock;
import android.view.MotionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import static org.junit.Assert.*;

/** Real Android Canvas + touch regression for fortress visual exaggeration. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class FortressShowcaseV051Test {
    private static int hash(Bitmap image){
        int h=19;
        for(int y=6;y<image.getHeight()-6;y+=4)
            for(int x=6;x<image.getWidth()-6;x+=4)
                h=h*31+image.getPixel(x,y);
        return h;
    }
    private static File folder(){
        File cwd=new File(System.getProperty("user.dir"));
        File dir=new File(cwd,new File(cwd,"app/build.gradle").exists()?
                "app/build/screenshots":"build/screenshots");
        if(!dir.isDirectory())assertTrue(dir.mkdirs());
        return dir;
    }
    private static void save(Bitmap image,String name)throws Exception{
        File file=new File(folder(),name);
        try(FileOutputStream out=new FileOutputStream(file))
            {assertTrue(image.compress(Bitmap.CompressFormat.PNG,100,out));}
        assertTrue(name,file.length()>2000);
    }
    private static int getInt(Object obj,String key)throws Exception{
        Field f=obj.getClass().getDeclaredField(key);f.setAccessible(true);
        return f.getInt(obj);
    }
    private static boolean getBoolean(Object obj,String key)throws Exception{
        Field f=obj.getClass().getDeclaredField(key);f.setAccessible(true);
        return f.getBoolean(obj);
    }
    private static void tap(RiftGameView view,float x,float y){
        long t=SystemClock.uptimeMillis();
        MotionEvent event=MotionEvent.obtain(t,t,MotionEvent.ACTION_DOWN,x,y,0);
        view.onTouchEvent(event);event.recycle();
        event=MotionEvent.obtain(t,t+28,MotionEvent.ACTION_UP,x,y,0);
        view.onTouchEvent(event);event.recycle();
    }
    private static Bitmap frame(RiftGameView view){
        Bitmap b=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(b));return b;
    }
    @Test public void fiveActualFortressRankSilhouettesAndGunsAreDifferent()
            throws Exception{
        Set<Integer> architecture=new HashSet<>();
        for(int rank=1;rank<=5;rank++){
            Bitmap b=Bitmap.createBitmap(700,610,Bitmap.Config.ARGB_8888);
            Canvas c=new Canvas(b);c.drawColor(0xff152b35);
            float cx=350,cy=397;
            NativeDeployedMapArt.fort(c,cx,cy,rank,.7f,1f);
            FortressStructures.rankStructures(c,cx,cy,rank,.7f);
            FortressSilhouetteArt.draw(c,cx,cy,rank,.7f);
            int arsenal=rank-1;
            FortressEvolutionArt.draw(c,cx,cy,rank,arsenal,.7f);
            FortressWeaponShowcaseArt.draw(c,cx,cy,arsenal,.7f);
            assertTrue("Rank "+rank+" must change genuine painted geometry",
                    architecture.add(hash(b)));
            save(b,"100-v051-fortress-rank-"+rank+".png");
            b.recycle();
        }
        assertEquals(5,architecture.size());
    }
    @Test public void everyFortressWeaponIsAnActualDifferentMountedStructure()
            throws Exception{
        Set<Integer> forms=new HashSet<>();
        for(int arsenal=0;arsenal<=4;arsenal++){
            Bitmap b=Bitmap.createBitmap(620,480,Bitmap.Config.ARGB_8888);
            Canvas c=new Canvas(b);c.drawColor(0xff18313c);
            FortressEvolutionArt.draw(c,310,355,5,arsenal,0);
            FortressWeaponShowcaseArt.draw(c,310,355,arsenal,0);
            assertTrue("Each purchased arsenal should change a physical gun",
                    forms.add(hash(b)));
            save(b,"101-v051-fortress-arsenal-"+arsenal+".png");
            b.recycle();
        }
        assertEquals(5,forms.size());
        assertThrows(IllegalArgumentException.class,
                ()->FortressWeaponShowcaseArt.draw(new Canvas(),0,0,5,0));
    }
    @Test public void allWeaponFormsHaveProgressivePhysicalDetails()
            throws Exception{
        for(int weapon=0;weapon<6;weapon++){
            Set<Integer> appearance=new HashSet<>();
            for(int tier=0;tier<5;tier++){
                Bitmap b=Bitmap.createBitmap(300,305,Bitmap.Config.ARGB_8888);
                Canvas c=new Canvas(b);c.drawColor(0xff102431);
                EvolvedWeaponArt.draw(c,weapon,tier,150,218,2.15f,.5f);
                assertTrue("Weapon "+weapon+" tier "+tier,
                        appearance.add(hash(b)));
                save(b,"102-v051-weapon-"+weapon+"-form-"+tier+".png");
                b.recycle();
            }
            assertEquals(5,appearance.size());
        }
    }
    @Test public void workshopNextLookIsOnlyPreviewAndRealUpgradeRevealsForm()
            throws Exception{
        Context context=RuntimeEnvironment.getApplication();
        SharedPreferences prefs=context.getSharedPreferences(
                "riftguard_campaign_v1",Context.MODE_PRIVATE);
        prefs.edit().clear().putInt("supplies",200000).putInt("fortress_rank",1)
                .commit();
        RiftGameView game=new RiftGameView(context);
        game.layout(0,0,1280,720);game.setActive(false);
        tap(game,464,541); // same HOME -> FORTRESS button as actual UI
        assertEquals("FORTRESS",sceneName(game));
        Bitmap current=frame(game);
        save(current,"103-v051-workshop-current.png");
        int original=hash(current);
        tap(game,530,526); // explicitly visible PREVIEW NEXT button
        assertTrue(getBoolean(game,"previewNextFortressLook"));
        assertEquals(1,getInt(game,"fortressRank"));
        assertEquals(200000,getInt(game,"supplies"));
        Bitmap preview=frame(game);
        save(preview,"104-v051-workshop-next-preview.png");
        assertNotEquals("Preview must be real new model, not text only",
                original,hash(preview));
        tap(game,530,526);
        assertFalse(getBoolean(game,"previewNextFortressLook"));
        tap(game,530,526);
        tap(game,1050,655); // same fixed UPGRADE FORT button as v0.30
        assertEquals(2,getInt(game,"fortressRank"));
        assertFalse(getBoolean(game,"previewNextFortressLook"));
        assertEquals(2,prefs.getInt("fortress_rank",0));
        Field reveal=game.getClass().getDeclaredField("evolutionRevealTimer");
        reveal.setAccessible(true);
        assertTrue("Real purchase must show visible reveal",reveal.getFloat(game)>0);
        Bitmap unlocked=frame(game);
        save(unlocked,"105-v051-workshop-upgrade-reveal.png");
        // Temporarily remove only the celebration. The fortress rank,
        // supplies, selected preview and static screen remain identical.
        reveal.setFloat(game,0f);
        Bitmap afterReveal=frame(game);
        for(int[] point:new int[][]{{795,185},{899,245},{1080,515},{1050,655}}){
            assertEquals("Reveal must NOT cover supplies, cost or action buttons",
                    afterReveal.getPixel(point[0],point[1]),
                    unlocked.getPixel(point[0],point[1]));
        }
        assertNotEquals("Celebration is still visibly present in the safe header",
                afterReveal.getPixel(906,77),unlocked.getPixel(906,77));
        save(afterReveal,"106-v051-workshop-clear-reveal.png");
        current.recycle();preview.recycle();unlocked.recycle();afterReveal.recycle();
    }
    private static String sceneName(RiftGameView game)throws Exception{
        Field field=game.getClass().getDeclaredField("scene");
        field.setAccessible(true);return String.valueOf(field.get(game));
    }
    @Test public void projectileSpawnsMatchVisibleFortressWeapons(){
        assertEquals(950f-147f,FortressArmamentRules.muzzleX(950,0,1),.001f);
        assertEquals(950f+169f,FortressArmamentRules.muzzleX(950,1,2),.001f);
        assertEquals(950f,FortressArmamentRules.muzzleX(950,0,3),.001f);
        assertEquals(950f,FortressArmamentRules.muzzleX(950,12,4),.001f);
        assertEquals(510f-88f,FortressArmamentRules.muzzleY(510,1),.001f);
        assertEquals(510f-105f,FortressArmamentRules.muzzleY(510,2),.001f);
        assertEquals(510f-218f,FortressArmamentRules.muzzleY(510,3),.001f);
        assertEquals(510f-249f,FortressArmamentRules.muzzleY(510,4),.001f);
    }
    @Test public void evolutionSoundIsOriginalValidOfflineWav(){
        byte[] wav=GameAudio.synthesize(GameAudio.EVOLUTION);
        assertTrue(wav.length>12000);
        assertEquals('R',(char)wav[0]);assertEquals('W',(char)wav[8]);
        assertEquals('A',(char)wav[9]);assertEquals('V',(char)wav[10]);
        assertEquals('E',(char)wav[11]);
        boolean nonZero=false;
        for(int i=44;i<wav.length;i++)
            if(wav[i]!=0){nonZero=true;break;}
        assertTrue("Actual synthesized unlock sound cannot be silent",nonZero);
    }
}
