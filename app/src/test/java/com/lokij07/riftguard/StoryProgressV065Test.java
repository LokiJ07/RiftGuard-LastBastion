package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public final class StoryProgressV065Test {
    private SharedPreferences prefs;
    private StoryProgress story;
    @Before public void setup(){
        prefs=RuntimeEnvironment.getApplication().getSharedPreferences("story_v065_test",Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        story=new StoryProgress(prefs);
    }
    @Test public void oneHundredMemoriesAreAddressedAtExactLastStands(){
        int count=0,cael=0;
        for(int stage=0;stage<300;stage++){
            KeyMemories.Memory memory=KeyMemories.forStage(stage);
            MemoryTrial.Objective objective=MemoryTrial.forStage(stage);
            if(Campaign.map(stage)!=2){
                assertNull(memory);assertNull(objective);continue;
            }
            assertNotNull(memory);assertNotNull(objective);
            assertNotNull(memory.text);assertTrue(memory.text.length()>20);
            if(memory.fromCael)cael++;
            count++;
        }
        assertEquals(100,count);
        assertEquals(33,cael);
        assertTrue(KeyMemories.isWorldFinalKey(Campaign.stage(10,2)));
        assertFalse(KeyMemories.isWorldFinalKey(Campaign.stage(9,2)));
    }
    @Test public void failedTrialVictoryDoesNotGrantKeyButReplayCan(){
        int stage=Campaign.stage(1,2);
        assertFalse(story.claimMemory(stage,false,true));
        assertFalse(story.hasMemory(stage));
        assertFalse(story.claimMemory(stage,true,false));
        assertTrue(story.claimMemory(stage,true,true));
        assertTrue(story.hasMemory(stage));
        assertFalse(story.claimMemory(stage,true,true));
        assertEquals(1,story.total());
        StoryProgress restarted=new StoryProgress(prefs);
        assertEquals(1,restarted.total());
        assertTrue(restarted.hasMemory(stage));
    }
    @Test public void legacyCampaignSaveYieldsWorldSealsWithoutKeys(){
        prefs.edit().putInt("unlocked",30).commit();
        assertTrue(story.hasOwnedSeal(0));
        assertFalse(story.hasRestoredSeal(0));
        assertFalse(story.hasOwnedSeal(1));
        assertEquals(1,story.ownedSeals());
        prefs.edit().putInt("unlocked",300).commit();
        assertEquals(10,story.ownedSeals());
        assertTrue(story.caveUnlocked());
        assertFalse(story.trueEndingUnlocked());
        for(int stage=2;stage<300;stage+=3)assertTrue(story.claimMemory(stage,true,true));
        assertEquals(100,story.total());
        assertEquals(10,story.restoredSeals());
        assertTrue(story.trueEndingUnlocked());
    }
    @Test public void namesAreCustomizableAndDoNotTouchExistingProgress(){
        prefs.edit().putInt("unlocked",107).putInt("fortress_rank",4).putInt("supplies",992).commit();
        assertEquals("Warden",story.wardenName());
        story.setWardenName("  Aeron  the  Brave ");
        assertEquals("Aeron the Brave",story.wardenName());
        story.setWardenName("!!!");
        assertEquals("Warden",story.wardenName());
        assertEquals(107,prefs.getInt("unlocked",-1));
        assertEquals(4,prefs.getInt("fortress_rank",-1));
        assertEquals(992,prefs.getInt("supplies",-1));
    }
    @Test public void realLiveTrialMetricsAreEvaluated(){
        int coreStage=Campaign.stage(1,2);
        MemoryTrial.Objective obj=MemoryTrial.forStage(coreStage);
        assertEquals(MemoryTrial.CORE,obj.type);
        assertFalse(MemoryTrial.evaluate(obj,4,45,.70f,1f,true));
        assertTrue(MemoryTrial.evaluate(obj,4,45,.95f,1f,true));
        assertNotNull(MemoryTrial.forStage(Campaign.stage(10,2)));
        assertEquals(MemoryTrial.ELITE,MemoryTrial.forStage(Campaign.stage(10,2)).type);
        for(int stage=2;stage<300;stage+=3) {
            MemoryTrial.Objective trial=MemoryTrial.forStage(stage);
            assertTrue(trial.requirement>0);
            assertTrue(trial.title.length()>0);
            assertTrue(trial.description.length()>0);
        }
    }
    @Test public void scriptedBossLinesRespectTenWorlds(){
        assertEquals(4,StoryScript.prologue("Aeron").length);
        for(int world=0;world<10;world++){
            assertTrue(StoryScript.beforeWorldBoss(world).length>=2);
            assertTrue(StoryScript.afterWorldBoss(world).length>=2);
        }
        assertNotNull(StoryScript.memory(KeyMemories.forStage(2)));
    }
}
