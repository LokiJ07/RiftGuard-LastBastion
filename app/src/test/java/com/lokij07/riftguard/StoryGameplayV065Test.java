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

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class StoryGameplayV065Test {
    private static void field(Object obj,String name,Object value)throws Exception{
        Field f=RiftGameView.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj,value);
    }
    private static Object field(Object obj,String name)throws Exception{
        Field f=RiftGameView.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(obj);
    }
    private static void call(RiftGameView game,String method,int stage)throws Exception{
        Method m=RiftGameView.class.getDeclaredMethod(method,int.class);
        m.setAccessible(true);m.invoke(game,stage);
    }
    private static void win(RiftGameView game)throws Exception{
        Method m=RiftGameView.class.getDeclaredMethod("complete");
        m.setAccessible(true);m.invoke(game);
    }
    private static void screenshot(Bitmap b,String name)throws Exception{
        File cwd=new File(System.getProperty("user.dir"));
        File dir=new File(cwd,new File(cwd,"app/build.gradle").exists()?
                "app/build/screenshots":"build/screenshots");
        if(!dir.exists())assertTrue(dir.mkdirs());
        try(FileOutputStream out=new FileOutputStream(new File(dir,name))){
            assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));
        }
    }
    private static void touch(RiftGameView game,float x,float y){
        long now=SystemClock.uptimeMillis();
        MotionEvent ev=MotionEvent.obtain(now,now,MotionEvent.ACTION_DOWN,x,y,0);
        game.onTouchEvent(ev);ev.recycle();
    }
    @Test public void failedTrialIsReplayableAfterBattleVictory()throws Exception{
        Context context=RuntimeEnvironment.getApplication();
        SharedPreferences prefs=context.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE);
        prefs.edit().clear().putInt("unlocked",2).commit();
        RiftGameView game=new RiftGameView(context);
        game.layout(0,0,1280,720);
        int s=Campaign.stage(1,2);
        call(game,"start",s);
        field(game,"baseHp",1f);
        win(game);
        assertEquals(3,prefs.getInt("unlocked",0));
        StoryProgress story=new StoryProgress(prefs);
        assertFalse(story.hasMemory(s));
        assertFalse((Boolean)field(game,"trialAwarded"));
        call(game,"start",s);
        field(game,"baseHp",1000f);
        win(game);
        assertEquals(3,prefs.getInt("unlocked",0));
        assertTrue(story.hasMemory(s));
        assertTrue((Boolean)field(game,"trialAwarded"));
        call(game,"start",s);
        win(game);
        assertFalse((Boolean)field(game,"trialAwarded"));
        assertEquals(1,story.total());
    }
    @Test public void homeToChronicleDrawAndMemorySceneDoNotCrash()throws Exception{
        Context context=RuntimeEnvironment.getApplication();
        SharedPreferences prefs=context.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE);
        prefs.edit().clear().putInt("unlocked",3).commit();
        StoryProgress progress=new StoryProgress(prefs);
        assertTrue(progress.claimMemory(2,true,true));
        RiftGameView game=new RiftGameView(context);
        game.layout(0,0,1280,720);
        touch(game,472,455);
        assertEquals("CHRONICLE",field(game,"scene").toString());
        Bitmap b=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        game.draw(new Canvas(b));screenshot(b,"180-v065-chronicle.png");
        touch(game,665,235);
        assertEquals(true,field(game,"storyOpen"));
        game.draw(new Canvas(b));screenshot(b,"181-v065-illustrated-memory-dialogue.png");
        touch(game,1178,660);
        assertEquals(false,field(game,"storyOpen"));
        b.recycle();
    }

    @Test public void firstWorldLaunchPlaysTessaAndChapterThenRemembersSeenScenes()
            throws Exception{
        Context context=RuntimeEnvironment.getApplication();
        SharedPreferences prefs=context.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE);
        prefs.edit().clear().putInt("unlocked",0).commit();
        RiftGameView game=new RiftGameView(context);
        game.layout(0,0,1280,720);
        call(game,"start",0);
        assertFalse((Boolean)field(game,"storyOpen"));
        field(game,"briefingOpen",true);
        touch(game,750,610); // real BEGIN BATTLE button on the stage briefing
        assertTrue((Boolean)field(game,"storyOpen"));
        assertFalse((Boolean)field(game,"briefingOpen"));
        StoryScript.Line[] prologue=(StoryScript.Line[])field(game,"storyLines");
        assertEquals(StoryScript.prologue("Warden").length,prologue.length);
        touch(game,750,610); // next authored shot: Old Tessa
        StoryScript.Line[] lines=(StoryScript.Line[])field(game,"storyLines");
        int index=(Integer)field(game,"storyIndex");
        assertEquals(StoryScript.TESSA,lines[index].shot);
        Bitmap b=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        game.draw(new Canvas(b));screenshot(b,"182-v065-tessa-prologue.png");
        b.recycle();
        // Complete prologue and open the Greenhaven chapter automatically.
        for(int i=1;i<prologue.length;i++)touch(game,750,610);
        StoryScript.Line[] chapter=(StoryScript.Line[])field(game,"storyLines");
        assertTrue(chapter.length>=2);
        assertTrue(chapter[0].text.contains("Greenhaven"));
        assertTrue(prefs.getBoolean("story.v065.prologueSeen",false));
        assertTrue(prefs.getBoolean("story.v065.chapterSeen.0",false));
        for(int i=0;i<chapter.length;i++)touch(game,750,610);
        assertFalse((Boolean)field(game,"storyOpen"));
        call(game,"start",0);
        field(game,"briefingOpen",true);
        touch(game,750,610);
        assertFalse("Already seen chapters must not trap retry controls",
                (Boolean)field(game,"storyOpen"));
    }
}
