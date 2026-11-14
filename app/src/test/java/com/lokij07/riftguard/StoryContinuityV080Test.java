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

/** Regression coverage for critic-confirmed story conflicts and compassionate ending. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public final class StoryContinuityV080Test {
    private SharedPreferences prefs;
    private StoryProgress progress;

    @Before public void reset(){
        prefs=RuntimeEnvironment.getApplication()
                .getSharedPreferences("riftguard_story_v080_test",Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        progress=new StoryProgress(prefs);
    }

    private static boolean contains(StoryScript.Line[] script,String fragment){
        for(StoryScript.Line line:script)
            if(line.text.contains(fragment))return true;
        return false;
    }

    private static void noEarlyCaelSpeaker(StoryScript.Line[] scene){
        for(StoryScript.Line line:scene)
            assertFalse("The Titan identity must stay hidden before cave phase three",
                    "CAEL".equals(line.speaker));
    }

    @Test public void titanIdentityIsNotSpoiledByMainPathBeforePhaseThree(){
        noEarlyCaelSpeaker(StoryScript.prologue("Warden"));
        for(int world=0;world<10;world++){
            noEarlyCaelSpeaker(StoryScript.worldOpening(world,"Warden"));
            noEarlyCaelSpeaker(StoryScript.beforeWorldBoss(world));
            noEarlyCaelSpeaker(StoryScript.afterWorldBoss(world));
        }
        noEarlyCaelSpeaker(StoryScript.eclipseCaveOpening("Warden"));
        noEarlyCaelSpeaker(StoryScript.eclipsePhaseTwo());
        assertEquals("CAEL",StoryScript.eclipsePhaseThree()[1].speaker);
        assertEquals("CAEL",StoryScript.eclipseEnding(StoryProgress.ENDING_TRUE)[0].speaker);
    }

    @Test public void chadaShattersAndIsReforgedBeforeCaveWithoutDuplicateSword(){
        StoryScript.Line[] intro=StoryScript.prologue("Warden");
        assertTrue(contains(intro,"Chada shatters"));
        assertTrue(contains(intro,"Bantay and the old kettle vanish"));
        assertTrue(contains(StoryScript.worldOpening(3,"Warden"),"broken hilt"));
        assertTrue(contains(StoryScript.afterWorldBoss(6),"Chada is whole again"));
        StoryScript.Line[] cave=StoryScript.eclipseCaveOpening("Warden");
        assertTrue(contains(cave,"Knight still carries the reforged Chada"));
        assertFalse(contains(cave,"kape, Chada, and"));
        assertTrue(KeyMemories.ALL[0][3].text.contains("borrowed sword"));
        assertTrue(KeyMemories.ALL[3][3].text.contains("heirloom"));
        assertTrue(KeyMemories.ALL[9][2].text.contains("Chada shattered"));
        assertTrue(KeyMemories.ALL[2][9].text.contains("trainee"));
    }

    @Test public void tessaSetupAndPayoffAreInMainPathAndBantayReturns(){
        assertTrue(contains(StoryScript.afterWorldBoss(2),"Salt, not sugar"));
        assertTrue(contains(StoryScript.afterWorldBoss(5),"Salt, not sugar"));
        assertTrue(contains(StoryScript.worldOpening(1,"Warden"),"lantern"));
        assertTrue(contains(StoryScript.worldOpening(3,"Warden"),"kept your chair"));
        assertTrue(contains(StoryScript.eclipseCaveOpening("Warden"),"living Bantay"));
        StoryScript.Line[] partial=StoryScript.eclipseEnding(StoryProgress.ENDING_PARTIAL);
        assertTrue(contains(partial,"Bantay rushes to the Knight"));
        assertTrue(contains(partial,"We are leaving. Both of us."));
        assertFalse(contains(partial,"faint bark"));
    }

    @Test public void trueEndingRequiresThirtyCaelMemoriesNotAllHundred(){
        assertEquals(33,StoryProgress.caelTotal());
        assertEquals(30,StoryProgress.caelNeeded());
        prefs.edit().putInt("unlocked",Campaign.STAGES).commit();
        assertTrue(progress.caveUnlocked());
        int collected=0;
        for(int stage=2;stage<Campaign.STAGES;stage+=3){
            KeyMemories.Memory memory=KeyMemories.forStage(stage);
            if(memory.fromCael&&collected<30){
                assertTrue(progress.claimMemory(stage,true,true));
                collected++;
                if(collected==29){
                    assertEquals(29,progress.caelCount());
                    assertFalse(progress.trueEndingUnlocked());
                    assertFalse(progress.saveEnding(StoryProgress.ENDING_TRUE));
                }
            }
        }
        assertEquals(30,collected);
        assertEquals(30,progress.caelCount());
        assertTrue("Do not force another seventy optional trials",progress.total()<100);
        assertTrue(progress.trueEndingUnlocked());
        assertTrue(progress.saveEnding(StoryProgress.ENDING_TRUE));
        StoryProgress reloaded=new StoryProgress(prefs);
        assertEquals(30,reloaded.caelCount());
        assertEquals(StoryProgress.ENDING_TRUE,reloaded.ending());
    }

    @Test public void collectingCaelHistoryCannotBypassCaveCampaignGate(){
        int collected=0;
        for(int stage=2;stage<Campaign.STAGES&&collected<30;stage+=3){
            if(KeyMemories.forStage(stage).fromCael){
                assertTrue(progress.claimMemory(stage,true,true));
                collected++;
            }
        }
        assertEquals(30,progress.caelCount());
        assertFalse(progress.caveUnlocked());
        assertFalse(progress.trueEndingUnlocked());
        assertFalse(progress.saveEnding(StoryProgress.ENDING_TRUE));
        prefs.edit().putInt("unlocked",Campaign.STAGES).commit();
        assertTrue(progress.trueEndingUnlocked());
    }
}
