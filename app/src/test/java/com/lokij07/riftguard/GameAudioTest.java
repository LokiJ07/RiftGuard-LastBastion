package com.lokij07.riftguard;

import android.content.Context;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

/** Validates all original synthesized WAV assets and actual mute preferences. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public final class GameAudioTest {
    private static int le32(byte[] b,int at){
        return (b[at]&255)|((b[at+1]&255)<<8)|((b[at+2]&255)<<16)
                |((b[at+3]&255)<<24);
    }
    @Test public void allSevenEffectsHaveValidNonSilentPcmAndDifferentSignatures() {
        int[] checksums=new int[7];
        for(int type=0;type<7;type++) {
            byte[] wav=GameAudio.synthesize(type);
            assertTrue(wav.length>3000);
            assertEquals('R',(char)wav[0]);assertEquals('I',(char)wav[1]);
            assertEquals('W',(char)wav[8]);assertEquals('E',(char)wav[11]);
            assertEquals(wav.length-8,le32(wav,4));
            assertEquals(wav.length-44,le32(wav,40));
            assertEquals(22050,le32(wav,24));
            int checksum=216613626;
            boolean nonZero=false;
            for(int i=44;i<wav.length;i+=3){
                checksum=(checksum^wav[i])*16777619;
                if(wav[i]!=0)nonZero=true;
            }
            assertTrue("Synthesized audio must be audible",nonZero);
            checksums[type]=checksum;
        }
        for(int i=0;i<checksums.length;i++)
            for(int j=i+1;j<checksums.length;j++)
                assertNotEquals("Audio classes must sound distinct",checksums[i],checksums[j]);
        assertThrows(IllegalArgumentException.class,()->GameAudio.synthesize(-1));
    }
    @Test public void muteToggleIsPersistentAndDoesNotRequireNetworking() {
        Context ctx=RuntimeEnvironment.getApplication();
        android.content.SharedPreferences prefs=ctx.getSharedPreferences("riftguard_audio_test",
                Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        GameAudio audio=new GameAudio(ctx,prefs);
        assertTrue(audio.enabled());
        assertFalse(audio.toggle());
        assertFalse(new GameAudio(ctx,prefs).enabled());
        audio.play(GameAudio.SHOT); // Muted must not initialize audio hardware.
        assertTrue(audio.toggle());
        assertTrue(new GameAudio(ctx,prefs).enabled());
    }
}
