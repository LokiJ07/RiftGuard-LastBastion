package com.lokij07.riftguard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.view.MotionEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MissionBriefingTest {
    private static Object field(Object o,String name)throws Exception {
        Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);
    }
    private static void tap(RiftGameView view,float x,float y) {
        long n=SystemClock.uptimeMillis();
        MotionEvent down=MotionEvent.obtain(n,n,MotionEvent.ACTION_DOWN,x,y,0);
        view.onTouchEvent(down);down.recycle();
        MotionEvent up=MotionEvent.obtain(n,n+40,MotionEvent.ACTION_UP,x,y,0);
        view.onTouchEvent(up);up.recycle();
    }
    @Test public void everyStageHasCorrectOfflineChronicleAndBossWarning(){
        for(int stage=0;stage<Campaign.STAGES;stage++){
            MissionStory.Briefing b=MissionStory.forStage(stage);
            assertTrue(b.chapter.contains(Campaign.WORLDS[Campaign.world(stage)].toUpperCase()));
            assertTrue(b.headline.contains(Campaign.MAPS[Campaign.map(stage)].toUpperCase()));
            assertTrue(b.lore.length()>35);
            assertTrue(b.order.length()>25);
            assertTrue(b.warning.length()>15);
            assertTrue(b.objective.contains("WAVES"));
            if(DifficultyRules.worldBoss(stage))
                assertTrue(b.warning.contains(DifficultyRules.worldBossName(stage)));
        }
        assertNotEquals(MissionStory.forStage(0).lore,MissionStory.forStage(299).lore);
        assertThrows(IllegalArgumentException.class,()->MissionStory.forStage(300));
    }
    @Test public void deployShowsStoryAndPreventsAccidentalTouchOrEnemyUpdates()
            throws Exception {
        Context context=RuntimeEnvironment.getApplication();
        context.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE)
                .edit().clear().commit();
        RiftGameView view=new RiftGameView(context);
        view.layout(0,0,1280,720);
        tap(view,230,445); // Home -> Atlas
        assertEquals("MAP",field(view,"scene").toString());
        tap(view,660,613); // Atlas -> mission story
        assertEquals("PLAY",field(view,"scene").toString());
        assertEquals(true,field(view,"briefingOpen"));
        // A rendered scene does not bypass the briefing or give combat input.
        Bitmap img=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(img));
        assertNotEquals(0,img.getPixel(640,330));
        img.recycle();
        float startX=(float)field(view,"heroX");
        tap(view,1120,560); // actual attack button must be gated
        assertEquals(true,field(view,"briefingOpen"));
        assertEquals(false,field(view,"firing"));
        assertEquals(startX,(float)field(view,"heroX"),.001f);
        tap(view,902,620);
        assertEquals(false,field(view,"briefingOpen"));
        assertEquals("PLAY",field(view,"scene").toString());
        tap(view,1174,559); // attack button now usable
        assertEquals(false,field(view,"briefingOpen"));
    }
    @Test public void backFromBriefingReturnsToMap() throws Exception {
        Context context=RuntimeEnvironment.getApplication();
        context.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE)
                .edit().clear().commit();
        RiftGameView view=new RiftGameView(context);view.layout(0,0,1280,720);
        tap(view,230,445);tap(view,660,613);
        assertTrue(view.handleBack());
        assertEquals("MAP",field(view,"scene").toString());
        assertEquals(false,field(view,"briefingOpen"));
    }
}
