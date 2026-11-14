package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.view.MotionEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

/** Real battle-briefing touch sequence must not replay or block camp banter. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public final class CampBanterV080IntegrationTest {
    private static Object get(RiftGameView game,String key)throws Exception{
        Field f=RiftGameView.class.getDeclaredField(key);
        f.setAccessible(true);return f.get(game);
    }
    private static void set(RiftGameView game,String key,Object value)throws Exception{
        Field f=RiftGameView.class.getDeclaredField(key);
        f.setAccessible(true);f.set(game,value);
    }
    private static void start(RiftGameView game,int stage)throws Exception{
        Method m=RiftGameView.class.getDeclaredMethod("start",int.class);
        m.setAccessible(true);m.invoke(game,stage);
        set(game,"briefingOpen",true);
    }
    private static void tap(RiftGameView game,int x,int y){
        long now=SystemClock.uptimeMillis();
        MotionEvent event=MotionEvent.obtain(now,now,MotionEvent.ACTION_DOWN,x,y,0);
        game.onTouchEvent(event);event.recycle();
    }

    @Test public void firstClearAtLevelThreeShowsOneBeatButReplayDoesNot()
            throws Exception{
        Context context=RuntimeEnvironment.getApplication();
        SharedPreferences p=context.getSharedPreferences(
                "riftguard_campaign_v1",Context.MODE_PRIVATE);
        p.edit().clear().putInt("unlocked",6).commit(); // next stage 3-A
        RiftGameView game=new RiftGameView(context);
        game.layout(0,0,1280,720);
        start(game,6);
        assertFalse((Boolean)get(game,"storyOpen"));
        tap(game,745,609); // actual BEGIN BATTLE
        assertTrue((Boolean)get(game,"storyOpen"));
        StoryScript.Line[] beat=(StoryScript.Line[])get(game,"storyLines");
        assertEquals(2,beat.length);
        assertEquals("OLD TESSA",beat[0].speaker);
        assertTrue(p.getBoolean("story.v080.banterSeen.3",false));

        tap(game,620,614); // next line
        tap(game,620,614); // close scene
        assertFalse((Boolean)get(game,"storyOpen"));
        start(game,6); // replay without awarding a key or extra scene
        tap(game,745,609);
        assertFalse((Boolean)get(game,"storyOpen"));
        assertEquals(6,p.getInt("unlocked",-1));
        assertEquals(0,new StoryProgress(p).total());
    }

    @Test public void lateGameBeatUsesJoinedAllyNotDeadTessa()throws Exception{
        Context context=RuntimeEnvironment.getApplication();
        SharedPreferences p=context.getSharedPreferences(
                "riftguard_campaign_v1",Context.MODE_PRIVATE);
        p.edit().clear().putInt("unlocked",Campaign.stage(51,0)).commit();
        RiftGameView game=new RiftGameView(context);
        game.layout(0,0,1280,720);
        start(game,Campaign.stage(51,0)); // level51 isn't a banter step
        tap(game,745,609);
        assertFalse((Boolean)get(game,"storyOpen"));
        // Level 54 is one of the scheduled first-clear beats in world 6.
        p.edit().putInt("unlocked",Campaign.stage(54,0)).commit();
        RiftGameView late=new RiftGameView(context);
        late.layout(0,0,1280,720);
        start(late,Campaign.stage(54,0));
        tap(late,745,609);
        assertTrue((Boolean)get(late,"storyOpen"));
        StoryScript.Line[] beat=(StoryScript.Line[])get(late,"storyLines");
        assertEquals("CAPTAIN YSOLDE",beat[0].speaker);
    }
}
