package com.lokij07.riftguard;

import java.lang.reflect.Field;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;
import static org.junit.Assert.*;

/**
 * Android may background the app during its opening splash. A queued
 * callback must never create a live SoundPool or ticking game off-screen.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public final class MainActivityLifecycleV067Test {
    private static Object field(Object obj,String name)throws Exception{
        Field f=obj.getClass().getDeclaredField(name);
        f.setAccessible(true);return f.get(obj);
    }
    @Test public void splashTimerNeverStartsGameInBackgroundAndCanResume()
            throws Exception{
        ActivityController<MainActivity> c=Robolectric
                .buildActivity(MainActivity.class).create().start().resume();
        MainActivity activity=c.get();
        Runnable enter=(Runnable)field(activity,"enterGame");
        assertNull(field(activity,"game"));
        c.pause();
        // Simulate the old delayed splash timer racing with onPause.
        enter.run();
        assertNull("Backgrounded app must not initialize live gameplay",
                field(activity,"game"));
        c.resume();
        enter.run();
        RiftGameView game=(RiftGameView)field(activity,"game");
        assertNotNull("Foreground app should still enter game",game);
        assertTrue((Boolean)field(game,"active"));
        c.pause();
        assertFalse("Pause must stop game render loop", (Boolean)field(game,"active"));
        c.resume();
        assertTrue("Resume must resume active battle", (Boolean)field(game,"active"));
        c.pause().stop().destroy();
    }
}
