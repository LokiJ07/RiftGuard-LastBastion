package com.lokij07.riftguard;

import android.graphics.Paint;
import android.graphics.Typeface;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class StoryPresentationV081Test {
    private static Paint measure(){
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTypeface(Typeface.create("sans-serif-condensed",Typeface.BOLD));
        return p;
    }
    private static void assertFits(String name,String content,Paint paint){
        StoryPresentationV081.TextLayout block=StoryPresentationV081.fitText(content,paint);
        assertTrue(name+" has no subtitle lines",block.lines.size()>0);
        assertTrue(name+" exceeds five lines",block.lines.size()<=5);
        assertTrue(name+" exceeds right edge",block.maxLineWidth<=
                StoryPresentationV081.TEXT_MAX_WIDTH+.5f);
        assertTrue(name+" too small",block.fontSize>=15f);
        assertTrue(name+" clips bottom",
                block.bottomBaseline()<=StoryPresentationV081.TEXT_TOP+
                        4*StoryPresentationV081.TEXT_LINE_HEIGHT+.5f);
        String rebuild=String.join(" ",block.lines).replaceAll("\\s+"," ").trim();
        assertEquals(name+" must not drop authored words",
                content.replaceAll("\\s+"," ").trim(),rebuild);
    }
    @Test public void everyAuthoredBeatAndMemoryFitsWithoutTruncation(){
        Paint p=measure();
        for(StoryScript.Line line:StoryScript.prologue("SixteenLetterNam"))assertFits("intro",line.text,p);
        for(int world=0;world<10;world++){
            for(StoryScript.Line line:StoryScript.worldOpening(world,"SixteenLetterNam"))
                assertFits("opening "+world,line.text,p);
            for(StoryScript.Line line:StoryScript.beforeWorldBoss(world))
                assertFits("boss intro "+world,line.text,p);
            for(StoryScript.Line line:StoryScript.afterWorldBoss(world))
                assertFits("boss ending "+world,line.text,p);
            for(KeyMemories.Memory m:KeyMemories.ALL[world])
                assertFits("memory "+world,m.text,p);
        }
        for(int level=3;level<=100;level+=3)
            for(StoryScript.Line line:StoryScript.campBanter(level,"SixteenLetterNam"))
                assertFits("camp "+level,line.text,p);
        for(StoryScript.Line line:StoryScript.eclipseCaveOpening("SixteenLetterNam"))
            assertFits("cave opening",line.text,p);
        for(StoryScript.Line line:StoryScript.eclipsePhaseTwo())
            assertFits("phase2",line.text,p);
        for(StoryScript.Line line:StoryScript.eclipsePhaseThree())
            assertFits("phase3",line.text,p);
        for(int ending=1;ending<=3;ending++)
            for(StoryScript.Line line:StoryScript.eclipseEnding(ending))
                assertFits("ending "+ending,line.text,p);
    }

    @Test public void choiceHitboxesMatchPaintedCardsAndDisabledMercyNeverAcceptsTap(){
        assertEquals(StoryProgress.ENDING_GOOD,
                StoryPresentationV081.caveChoiceHit(102,405,false));
        assertEquals(StoryProgress.ENDING_GOOD,
                StoryPresentationV081.caveChoiceHit(608,502,true));
        assertEquals(StoryProgress.ENDING_TRUE,
                StoryPresentationV081.caveChoiceHit(913,465,true));
        assertEquals(StoryProgress.ENDING_NONE,
                StoryPresentationV081.caveChoiceHit(913,465,false));
        assertEquals(StoryProgress.ENDING_PARTIAL,
                StoryPresentationV081.caveChoiceHit(101,526,false));
        assertEquals(StoryProgress.ENDING_PARTIAL,
                StoryPresentationV081.caveChoiceHit(1174,600,true));
        assertEquals(StoryProgress.ENDING_NONE,
                StoryPresentationV081.caveChoiceHit(640,440,true));
        assertEquals(StoryProgress.ENDING_NONE,
                StoryPresentationV081.caveChoiceHit(20,20,true));
        assertEquals(1,StoryPresentationV081.caveEndingHit(153,522));
        assertEquals(2,StoryPresentationV081.caveEndingHit(1125,613));
        assertEquals(0,StoryPresentationV081.caveEndingHit(640,560));
        assertEquals(0,StoryPresentationV081.caveEndingHit(5,700));
    }

    @Test public void chronicleTouchNeverSelectsUnpaintedWorldOrCardGaps(){
        for(int w=0;w<10;w++){
            float y=149+w*45;
            assertEquals(w,StoryPresentationV081.chronicleWorldHit(100,y+20));
            if(w<9)assertEquals(-1,
                    StoryPresentationV081.chronicleWorldHit(100,y+43));
        }
        assertEquals(-1,StoryPresentationV081.chronicleWorldHit(100,598));
        for(int row=0;row<5;row++){
            float y=210+70*row+22;
            assertEquals(row*2,StoryPresentationV081.chronicleMemoryHit(620,y));
            assertEquals(row*2+1,
                    StoryPresentationV081.chronicleMemoryHit(913,y));
            assertEquals(-1,StoryPresentationV081.chronicleMemoryHit(897,y));
        }
        assertEquals(-1,StoryPresentationV081.chronicleMemoryHit(950,585));
    }
}
