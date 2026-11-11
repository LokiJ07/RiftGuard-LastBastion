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

import java.io.File;
import java.io.FileOutputStream;

import static org.junit.Assert.*;

/**
 * CI generates screenshots of actual Android View render output.
 * This is not a physical-device screenshot, but it detects missing menu images and crashes.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class ScreenGalleryTest {
    private final Context context=RuntimeEnvironment.getApplication();

    private void tap(RiftGameView view,float x,float y) {
        long n=SystemClock.uptimeMillis();
        MotionEvent down=MotionEvent.obtain(n,n,MotionEvent.ACTION_DOWN,x,y,0);
        view.onTouchEvent(down);down.recycle();
        MotionEvent up=MotionEvent.obtain(n,n+30,MotionEvent.ACTION_UP,x,y,0);
        view.onTouchEvent(up);up.recycle();
    }

    private void capture(RiftGameView view,String name) throws Exception {
        File root=new File(System.getProperty("user.dir"));
        File folder=new File(root,new File("app/build.gradle").exists()
                ?"app/build/screenshots":"build/screenshots");
        if(!folder.exists())assertTrue(folder.mkdirs());
        Bitmap image=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(image);
        view.draw(canvas);
        File file=new File(folder,name+".png");
        try(FileOutputStream stream=new FileOutputStream(file)){
            assertTrue(image.compress(Bitmap.CompressFormat.PNG,100,stream));
        }
        assertTrue("Rendered screenshot should have image data",file.length()>4000);
        image.recycle();
    }

    @Test public void captureActualUiAsVisualGallery() throws Exception {
        context.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE)
                .edit().putInt("fortress_rank",2).putInt("supplies",220)
                .putInt("weapon",2).commit();
        RiftGameView view=new RiftGameView(context);
        view.layout(0,0,1280,720);
        capture(view,"01-home");
        tap(view,270,450); // realm atlas
        capture(view,"02-atlas");
        tap(view,100,639);
        tap(view,215,544); // armory
        capture(view,"03-armory");
        tap(view,450,656); // fortress
        capture(view,"04-fortress");
        tap(view,120,654); // home
        tap(view,250,449);
        tap(view,630,613); // deploy
        // Show actual moving enemies for screenshot verification, not an empty map.
        java.lang.reflect.Method spawn=RiftGameView.class.getDeclaredMethod("spawnEnemy");
        spawn.setAccessible(true);
        java.lang.reflect.Field enemiesField=RiftGameView.class.getDeclaredField("enemies");
        enemiesField.setAccessible(true);
        java.util.List<?> living=(java.util.List<?>)enemiesField.get(view);
        for(int kind=0;kind<=6;kind++) {
            spawn.invoke(view);
            Object enemy=living.get(living.size()-1);
            java.lang.reflect.Field type=enemy.getClass().getDeclaredField("kind");
            java.lang.reflect.Field px=enemy.getClass().getDeclaredField("x");
            java.lang.reflect.Field py=enemy.getClass().getDeclaredField("y");
            type.setAccessible(true);px.setAccessible(true);py.setAccessible(true);
            type.setInt(enemy,kind);
            px.setFloat(enemy,680+kind*85);
            py.setFloat(enemy,640-(kind%3)*102);
        }
        capture(view,"05-battle");
    }
}
