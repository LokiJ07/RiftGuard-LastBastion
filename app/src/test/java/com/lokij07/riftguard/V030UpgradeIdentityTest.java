package com.lokij07.riftguard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.SystemClock;
import android.view.MotionEvent;
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
public final class V030UpgradeIdentityTest {
    private static Object field(Object object,String key)throws Exception{
        Field f=object.getClass().getDeclaredField(key);f.setAccessible(true);
        return f.get(object);
    }
    private static void tap(RiftGameView view,float x,float y){
        long t=SystemClock.uptimeMillis();
        MotionEvent down=MotionEvent.obtain(t,t,MotionEvent.ACTION_DOWN,x,y,0);
        view.onTouchEvent(down);down.recycle();
        MotionEvent up=MotionEvent.obtain(t,t+33,MotionEvent.ACTION_UP,x,y,0);
        view.onTouchEvent(up);up.recycle();
    }
    private static RiftGameView game(int supplies)throws Exception{
        Context ctx=RuntimeEnvironment.getApplication();
        ctx.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE).edit()
                .clear().putInt("supplies",supplies).commit();
        RiftGameView view=new RiftGameView(ctx);
        view.layout(0,0,1280,720);
        return view;
    }
    @Test public void allFooterHitboxesMatchVisiblePositions(){
        assertEquals(4,UpgradeActionLayout.SLOTS);
        for(int i=0;i<4;i++){
            assertEquals(i,UpgradeActionLayout.hit(UpgradeActionLayout.centerX(i),
                    UpgradeActionLayout.TOP+15));
            assertEquals(i,UpgradeActionLayout.hit(UpgradeActionLayout.centerX(i),
                    UpgradeActionLayout.BOTTOM-5));
            assertEquals(-1,UpgradeActionLayout.hit(UpgradeActionLayout.centerX(i),580));
        }
        assertEquals(-1,UpgradeActionLayout.hit(20,655));
        assertEquals(-1,UpgradeActionLayout.hit(345,655)); // genuine button gap
        assertEquals(-1,UpgradeActionLayout.hit(1209,655));
    }
    @Test public void upgradeScreensShareActionRowAndFortressPurchaseIsReal()
            throws Exception {
        RiftGameView view=game(1800);
        tap(view,460,535); // Home -> Fortress
        assertEquals("FORTRESS",field(view,"scene").toString());
        Bitmap screen=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(screen);
        view.draw(canvas);
        assertNotEquals(Color.TRANSPARENT,screen.getPixel(1050,658));
        int rank=(Integer)field(view,"fortressRank");
        tap(view,UpgradeActionLayout.centerX(3),656); // primary purchase
        assertEquals(rank+1,field(view,"fortressRank"));
        tap(view,UpgradeActionLayout.centerX(2),656); // tree from same bottom slot
        assertEquals("ASCENSION",field(view,"scene").toString());
        view.draw(canvas);
        tap(view,UpgradeActionLayout.centerX(1),656); // back to Armory
        assertEquals("ARMORY",field(view,"scene").toString());
        view.draw(canvas);
        tap(view,UpgradeActionLayout.centerX(3),656); // weapon mastery entry
        assertEquals("WEAPON_TREE",field(view,"scene").toString());
        view.draw(canvas);
        tap(view,UpgradeActionLayout.centerX(0),656); // return from weapon tree
        assertEquals("ARMORY",field(view,"scene").toString());
        tap(view,UpgradeActionLayout.centerX(2),656);
        assertEquals("SKILLS",field(view,"scene").toString());
        tap(view,UpgradeActionLayout.centerX(3),656); // skills -> mastery
        assertEquals("WEAPON_TREE",field(view,"scene").toString());
        screen.recycle();
    }
    @Test public void lokiJOpeningIsActualNativeRenderedScene(){
        LokiSplashView splash=new LokiSplashView(RuntimeEnvironment.getApplication());
        splash.layout(0,0,1280,720);
        Bitmap image=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        splash.draw(new Canvas(image));
        assertNotEquals(image.getPixel(30,25),image.getPixel(640,270));
        assertTrue(Color.red(image.getPixel(640,530))>0);
        image.recycle();
    }
    @Test public void fullNewFortressIsDifferentForEveryUpgradeTier(){
        Bitmap picture=Bitmap.createBitmap(600,370,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(picture);
        java.util.Set<Integer> paints=new java.util.HashSet<>();
        for(int tier=1;tier<=5;tier++){
            picture.eraseColor(Color.TRANSPARENT);
            NativeDeployedMapArt.fort(canvas,300,178,tier,0.2f,1);
            int hash=1;
            for(int x=170;x<=430;x+=11)
                for(int y=30;y<=294;y+=11)hash=hash*31+picture.getPixel(x,y);
            assertTrue("Tier "+tier+" must change actual visible fortress art",
                    paints.add(hash));
        }
        picture.recycle();
    }
}
