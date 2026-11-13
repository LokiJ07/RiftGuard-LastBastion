package com.lokij07.riftguard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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
public final class CinematicDirectionV065Test {
    private static Object value(Object object,String name)throws Exception{
        Field f=object.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(object);
    }
    private static void value(Object object,String name,Object set)throws Exception{
        Field f=object.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(object,set);
    }
    private static void call(RiftGameView view,String name,Class<?>[] args,Object... values)
            throws Exception{
        Method m=RiftGameView.class.getDeclaredMethod(name,args);
        m.setAccessible(true);
        m.invoke(view,values);
    }
    @Test public void dramaticCutsRevealWithoutNegativeAlphaOrCameraJump(){
        assertEquals(0f,CinematicDirectionV065.reveal(0,true),.001f);
        assertEquals(1f,CinematicDirectionV065.reveal(2,true),.001f);
        assertEquals(210,CinematicDirectionV065.curtainAlpha(0,true));
        assertEquals(0,CinematicDirectionV065.curtainAlpha(3,true));
        assertTrue(CinematicDirectionV065.reveal(.24f,true)<
                CinematicDirectionV065.reveal(.24f,false));
        assertTrue(CinematicDirectionV065.subtitleAlpha(.1f,true)<
                CinematicDirectionV065.subtitleAlpha(.8f,true));
        assertEquals(0f,CinematicDirectionV065.actorEntrance(2,true,true),.001f);
        assertTrue(CinematicDirectionV065.actorEntrance(0,true,true)>90f);
        assertTrue(CinematicDirectionV065.actorEntrance(0,false,true)<-90f);
        assertEquals(0f,CinematicDirectionV065.reveal(Float.NaN,true),.001f);
    }
    @Test public void reverseDialogueCameraMovesDifferentlyFromMemoryAndBoss(){
        float left=CinematicDirectionV065.storyPanX(3,0,false,false);
        float right=CinematicDirectionV065.storyPanX(3,1,false,false);
        assertTrue(left>0f);
        assertTrue(right<0f);
        float normal=CinematicDirectionV065.storyZoom(3,0,false,false);
        float memory=CinematicDirectionV065.storyZoom(3,0,true,false);
        float titan=CinematicDirectionV065.storyZoom(3,0,false,true);
        assertTrue(memory>normal);
        assertTrue(titan>memory);
        assertTrue(titan<1.14f); // no screen-filling over-zoom
    }
    @Test public void liveVictoryCameraFollowsRoadWhileDefeatStaysNearWarden(){
        BattleOutroV065 walk=new BattleOutroV065(0,true,1234,614);
        assertEquals(LaneRules.VIEW_CENTER_X,
                CinematicDirectionV065.outroCameraX(walk,LaneRules.VIEW_CENTER_X),.001f);
        walk.advance(1.6f);
        assertTrue(CinematicDirectionV065.outroCameraX(walk,LaneRules.VIEW_CENTER_X)>
                LaneRules.VIEW_CENTER_X+70f);
        float mid=CinematicDirectionV065.outroCameraX(walk,LaneRules.VIEW_CENTER_X);
        walk.advance(3f);
        assertTrue(CinematicDirectionV065.outroCameraX(walk,LaneRules.VIEW_CENTER_X)>mid);
        assertEquals(1f,CinematicDirectionV065.outroTitleAlpha(walk),.001f);
        assertTrue(CinematicDirectionV065.outroCurtainAlpha(walk)>0);
        BattleOutroV065 fall=new BattleOutroV065(0,false,1234,614);
        fall.advance(.3f);
        assertEquals(0f,CinematicDirectionV065.outroTitleAlpha(fall),.001f);
        fall.advance(1.8f);
        assertTrue(CinematicDirectionV065.outroTitleAlpha(fall)>.95f);
        float focus=CinematicDirectionV065.outroCameraX(fall,LaneRules.VIEW_CENTER_X);
        assertTrue(Math.abs(focus-LaneRules.VIEW_CENTER_X)<90f);
    }
    @Test public void realStoryShotRendersAnEntranceAndReadableSettledScene()
            throws Exception{
        Context ctx=RuntimeEnvironment.getApplication();
        ctx.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE)
                .edit().clear().commit();
        RiftGameView view=new RiftGameView(ctx);
        view.layout(0,0,1280,720);
        call(view,"start",new Class<?>[]{int.class},0);
        call(view,"openStory",new Class<?>[]{StoryScript.Line[].class},
                (Object)new StoryScript.Line[]{
                    new StoryScript.Line("TITAN","The night will remember your sword.",
                            StoryScript.CAEL)
                });
        assertTrue((Boolean)value(view,"storyOpen"));
        float start=(Float)value(view,"cinematicBeatStart");
        Bitmap early=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(early));
        value(view,"gameTime",start+1.5f);
        Bitmap settled=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(settled));
        int changed=0;
        for(int y=120;y<620;y+=50){
            for(int x=120;x<1160;x+=60){
                if(early.getPixel(x,y)!=settled.getPixel(x,y))changed++;
            }
        }
        assertTrue("Cinematic cut must visibly animate in the real Android view",
                changed>15);
        assertEquals("Story must pause combat until the player advances",
                true,value(view,"storyOpen"));
        early.recycle();
        settled.recycle();
    }

    @Test public void chronicleReplayUsesMemorysOriginalWorldAndResetsOverride()
            throws Exception{
        Context ctx=RuntimeEnvironment.getApplication();
        android.content.SharedPreferences prefs=ctx.getSharedPreferences(
                "riftguard_campaign_v1",Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        int memoryStage=Campaign.stage(81,2); // C-stage, world nine
        StoryProgress progress=new StoryProgress(prefs);
        assertTrue(progress.claimMemory(memoryStage,true,true));
        RiftGameView view=new RiftGameView(ctx);
        view.layout(0,0,1280,720);
        long now=android.os.SystemClock.uptimeMillis();
        android.view.MotionEvent open=android.view.MotionEvent.obtain(
                now,now,android.view.MotionEvent.ACTION_DOWN,472f,455f,0);
        view.onTouchEvent(open);open.recycle();
        assertEquals("CHRONICLE",value(view,"scene").toString());
        value(view,"chronicleWorld",8);
        call(view,"tapChronicle",new Class<?>[]{float.class,float.class},665f,235f);
        assertEquals(memoryStage,value(view,"chronicleMemoryStage"));
        assertTrue((Boolean)value(view,"storyOpen"));
        Bitmap scene=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(scene));
        scene.recycle();
        call(view,"dismissStory",new Class<?>[0]);
        assertEquals(-1,value(view,"chronicleMemoryStage"));
    }
}
