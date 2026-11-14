package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.view.MotionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import static org.junit.Assert.*;

/** Captures real game Canvas frames and validates actual scaled screen taps. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class StoryScreensV081IntegrationTest {
    private static Object value(Object obj,String key)throws Exception{
        Field field=RiftGameView.class.getDeclaredField(key);
        field.setAccessible(true);return field.get(obj);
    }
    private static void set(Object obj,String key,Object val)throws Exception{
        Field field=RiftGameView.class.getDeclaredField(key);
        field.setAccessible(true);field.set(obj,val);
    }
    private static void call(RiftGameView obj,String name)throws Exception{
        Method method=RiftGameView.class.getDeclaredMethod(name);
        method.setAccessible(true);method.invoke(obj);
    }
    private static void tap(RiftGameView view,float x,float y){
        long t=SystemClock.uptimeMillis();
        MotionEvent down=MotionEvent.obtain(t,t,MotionEvent.ACTION_DOWN,x,y,0);
        view.onTouchEvent(down);down.recycle();
    }
    private static void snap(RiftGameView game,int width,int height,String name)
            throws Exception{
        Bitmap bmp=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        game.draw(new Canvas(bmp));
        File cwd=new File(System.getProperty("user.dir"));
        File base=new File(cwd,new File(cwd,"app/build.gradle").exists()?
                "app/build/screenshots":"build/screenshots");
        if(!base.exists())assertTrue(base.mkdirs());
        try(FileOutputStream out=new FileOutputStream(new File(base,name))){
            assertTrue(bmp.compress(Bitmap.CompressFormat.PNG,95,out));
        }
        assertTrue("Screenshot contains a real game frame",bmp.getPixel(width/2,height/2)!=0);
        bmp.recycle();
    }

    @Test public void chronicleCardsAndDialogueAreRealScreensWithAccurateTaps()
            throws Exception{
        Context c=RuntimeEnvironment.getApplication();
        SharedPreferences p=c.getSharedPreferences(
                "riftguard_campaign_v1",Context.MODE_PRIVATE);
        p.edit().clear().putInt("unlocked",3).commit();
        assertTrue(new StoryProgress(p).claimMemory(2,true,true));
        RiftGameView game=new RiftGameView(c);
        game.layout(0,0,1280,720);
        tap(game,465,454); // HOME / CHRONICLE
        assertEquals("CHRONICLE",value(game,"scene").toString());
        snap(game,1280,720,"281-v081-chronicle-recovery-progress.png");
        tap(game,897,232); // Between visible memory cards is NOT a tap target.
        assertFalse((Boolean)value(game,"storyOpen"));
        tap(game,645,238);
        assertTrue((Boolean)value(game,"storyOpen"));
        snap(game,1280,720,"282-v081-dialogue-illustrated-panel.png");
        StoryPresentationV081.TextLayout layout=
                (StoryPresentationV081.TextLayout)value(game,"storyTextLayout");
        assertNotNull(layout);
        assertTrue(layout.maxLineWidth<=StoryPresentationV081.TEXT_MAX_WIDTH+.5f);
        // SKIP rect stays linked to real story input after the UI redesign.
        tap(game,1130,81);
        assertFalse((Boolean)value(game,"storyOpen"));
    }

    @Test public void caveChoiceCardsAndEndingRenderWithoutBreakingStoryPersistence()
            throws Exception{
        Context c=RuntimeEnvironment.getApplication();
        SharedPreferences p=c.getSharedPreferences(
                "riftguard_campaign_v1",Context.MODE_PRIVATE);
        p.edit().clear().putInt("unlocked",300).commit();
        RiftGameView game=new RiftGameView(c);
        game.layout(0,0,1280,720);
        call(game,"startEclipseCave");
        call(game,"completeEclipseCave");
        call(game,"dismissStory");
        set(game,"caveChoiceCutAt",-100f);
        assertEquals("CAVE_CHOICE",value(game,"scene").toString());
        snap(game,1280,720,"283-v081-cave-choices-locked-mercy.png");
        tap(game,913,450); // visibly disabled, no narrative action
        assertEquals("CAVE_CHOICE",value(game,"scene").toString());
        tap(game,355,445); // precisely inside the End Titan card
        assertEquals("CAVE_END",value(game,"scene").toString());
        assertEquals(StoryProgress.ENDING_GOOD,new StoryProgress(p).ending());
        call(game,"dismissStory");
        set(game,"caveEndingCutAt",-100f);
        snap(game,1280,720,"284-v081-cave-ending-summary.png");
        tap(game,640,550); // between cards, should not exit
        assertEquals("CAVE_END",value(game,"scene").toString());
        tap(game,920,560); // inside Return to Bastion button
        assertEquals("HOME",value(game,"scene").toString());
    }

    @Test public void scaledUltrawideButtonsAreTappableAtRenderedCoordinates()
            throws Exception{
        Context c=RuntimeEnvironment.getApplication();
        SharedPreferences p=c.getSharedPreferences(
                "riftguard_campaign_v1",Context.MODE_PRIVATE);
        p.edit().clear().putInt("unlocked",300).commit();
        RiftGameView game=new RiftGameView(c);
        game.layout(0,0,2340,1080); // logical 1280x720 centered, scale=1.5
        call(game,"startEclipseCave");
        call(game,"completeEclipseCave");
        call(game,"dismissStory");
        snap(game,2340,1080,"285-v081-cave-choices-ultrawide.png");
        // The 210px letterbox offset and 1.5x scaling are shared by
        // actual rendering and onTouchEvent; test the visible card center.
        tap(game,210+913*1.5f,450*1.5f);
        assertEquals("CAVE_CHOICE",value(game,"scene").toString());
        tap(game,210+355*1.5f,445*1.5f);
        assertEquals("CAVE_END",value(game,"scene").toString());
    }
}
