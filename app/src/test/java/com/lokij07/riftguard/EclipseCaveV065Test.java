package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public class EclipseCaveV065Test {
    private SharedPreferences prefs;
    private StoryProgress story;
    @Before public void reset(){
        prefs=RuntimeEnvironment.getApplication().getSharedPreferences(
                "riftguard_campaign_v1",Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        story=new StoryProgress(prefs);
    }
    private static Object field(RiftGameView g,String key)throws Exception{
        Field f=RiftGameView.class.getDeclaredField(key);f.setAccessible(true);return f.get(g);
    }
    private static void call(RiftGameView g,String key,Class<?>[] args,Object... values)throws Exception{
        Method m=RiftGameView.class.getDeclaredMethod(key,args);m.setAccessible(true);m.invoke(g,values);
    }
    @Test public void caveRemainsLockedUntilAllThreeHundredStagesClear()throws Exception{
        RiftGameView g=new RiftGameView(RuntimeEnvironment.getApplication());
        g.layout(0,0,1280,720);
        call(g,"startEclipseCave",new Class<?>[]{});
        assertFalse((Boolean)field(g,"caveMode"));
        prefs.edit().putInt("unlocked",299).commit();
        call(g,"startEclipseCave",new Class<?>[]{});
        assertFalse((Boolean)field(g,"caveMode"));
        prefs.edit().putInt("unlocked",300).commit();
        call(g,"startEclipseCave",new Class<?>[]{});
        assertTrue((Boolean)field(g,"caveMode"));
        assertEquals(299,field(g,"stage"));
        assertEquals(1,field(g,"remaining"));
        assertEquals(300,prefs.getInt("unlocked",0));
    }
    @Test public void unnumberedBossVictoryOpensChoiceAndGoodEndingDoesNotNeedKeys()throws Exception{
        prefs.edit().putInt("unlocked",300).commit();
        RiftGameView g=new RiftGameView(RuntimeEnvironment.getApplication());
        call(g,"startEclipseCave",new Class<?>[]{});
        call(g,"completeEclipseCave",new Class<?>[]{});
        assertEquals("CAVE_CHOICE",field(g,"scene").toString());
        call(g,"chooseCaveEnding",new Class<?>[]{int.class},StoryProgress.ENDING_GOOD);
        assertEquals("CAVE_END",field(g,"scene").toString());
        assertEquals(StoryProgress.ENDING_GOOD,story.ending());
        assertEquals(300,prefs.getInt("unlocked",0));
    }
    @Test public void thirtyCaelMemoriesUnlockCoffeeChoiceAndTrueEndingPersists()throws Exception{
        prefs.edit().putInt("unlocked",300).commit();
        RiftGameView g=new RiftGameView(RuntimeEnvironment.getApplication());
        call(g,"startEclipseCave",new Class<?>[]{});
        call(g,"completeEclipseCave",new Class<?>[]{});
        assertFalse(story.trueEndingUnlocked());
        call(g,"chooseCaveEnding",new Class<?>[]{int.class},StoryProgress.ENDING_TRUE);
        assertEquals("CAVE_CHOICE",field(g,"scene").toString());
        assertEquals(StoryProgress.ENDING_NONE,story.ending());
        int cael=0;
        for(int stage=2;stage<300&&cael<StoryProgress.caelNeeded();stage+=3)
            if(KeyMemories.forStage(stage).fromCael){
                assertTrue(story.claimMemory(stage,true,true));
                cael++;
            }
        assertEquals(30,cael);
        assertTrue(story.total()<100);
        assertTrue(story.trueEndingUnlocked());
        call(g,"chooseCaveEnding",new Class<?>[]{int.class},StoryProgress.ENDING_TRUE);
        assertEquals("CAVE_END",field(g,"scene").toString());
        assertEquals(StoryProgress.ENDING_TRUE,new StoryProgress(prefs).ending());
        assertTrue(story.restoredSeals()<10);
    }
    @Test public void allThreeEndingScriptsRemainLinkedToSourceCharacters(){
        StoryScript.Line[] opening=StoryScript.eclipseCaveOpening("Warden");
        assertTrue("Chapter may expand without invalidating continuity",opening.length>=6);
        boolean findsTitan=false,findsWarden=false;
        for(StoryScript.Line line:opening){
            if("TITAN".equals(line.speaker))findsTitan=true;
            if("WARDEN".equals(line.speaker))findsWarden=true;
        }
        assertTrue("The true identity is withheld until phase three",
                findsTitan&&findsWarden);
        assertEquals("CAEL",StoryScript.eclipsePhaseThree()[1].speaker);
        assertTrue(StoryScript.eclipsePhaseTwo().length>=2);
        assertTrue(StoryScript.eclipsePhaseThree().length>=2);
        for(int ending=1;ending<=3;ending++){
            assertTrue(StoryScript.eclipseEnding(ending).length>=3);
        }
    }
}
