package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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

/** New install must show the real equipped style on BOTH Home and battle. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class FirstRunWardenVisualV060Test {
    private static int style(RiftGameView view)throws Exception{
        Field field=RiftGameView.class.getDeclaredField("wardenRig");
        field.setAccessible(true);
        WardenRig rig=(WardenRig)field.get(view);
        Field visual=WardenRig.class.getDeclaredField("visualStyle");
        visual.setAccessible(true);
        return visual.getInt(rig);
    }
    private static void render(RiftGameView view,String filename)throws Exception{
        Bitmap b=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        try{
            view.draw(new Canvas(b));
            File cwd=new File(System.getProperty("user.dir"));
            File dir=new File(cwd,new File(cwd,"app/build.gradle").exists()
                    ?"app/build/screenshots":"build/screenshots");
            if(!dir.isDirectory())assertTrue(dir.mkdirs());
            try(FileOutputStream out=new FileOutputStream(new File(dir,filename))){
                assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));
            }
        }finally{b.recycle();}
    }
    @Test public void freshCrossbowShowsGreenRangerNotOldBlueWarden()
            throws Exception{
        Context ctx=RuntimeEnvironment.getApplication();
        SharedPreferences prefs=ctx.getSharedPreferences("riftguard_campaign_v1",
                Context.MODE_PRIVATE);
        prefs.edit().clear()
                .putInt("weapon",0).putInt("fortress_rank",1).commit();
        RiftGameView view=new RiftGameView(ctx);
        view.layout(0,0,1280,720);
        view.setActive(false);
        render(view,"134-v060-fresh-ranger-home.png");
        assertEquals("Home must render the equipped Ranger",
                WardenVisualStyles.RANGER,style(view));
        Method start=RiftGameView.class.getDeclaredMethod("start",int.class);
        start.setAccessible(true);start.invoke(view,0);
        view.setActive(false);
        render(view,"135-v060-fresh-ranger-battle.png");
        assertEquals("First gameplay frame must keep Ranger style",
                WardenVisualStyles.RANGER,style(view));
        view.releaseAudio();
    }
    @Test public void switchingWeaponChangesActualBattleActorNotJustLabel()
            throws Exception{
        Context ctx=RuntimeEnvironment.getApplication();
        SharedPreferences prefs=ctx.getSharedPreferences("riftguard_campaign_v1",
                Context.MODE_PRIVATE);
        prefs.edit().clear().putInt("fortress_rank",5)
                .putInt("weapon",2).commit();
        RiftGameView view=new RiftGameView(ctx);
        view.layout(0,0,1280,720);
        view.setActive(false);
        render(view,"136-v060-fresh-vanguard-home.png");
        assertEquals(WardenVisualStyles.VANGUARD,style(view));
        Method start=RiftGameView.class.getDeclaredMethod("start",int.class);
        start.setAccessible(true);start.invoke(view,0);
        view.setActive(false);
        render(view,"137-v060-fresh-vanguard-battle.png");
        assertEquals(WardenVisualStyles.VANGUARD,style(view));
        view.releaseAudio();
    }
}
