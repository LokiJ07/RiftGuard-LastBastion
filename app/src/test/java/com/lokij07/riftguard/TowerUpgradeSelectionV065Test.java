package com.lokij07.riftguard;

import android.content.Context;
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
import android.view.MotionEvent;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class TowerUpgradeSelectionV065Test {
    private static Object get(Object ob,String n)throws Exception{
        Field f=ob.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(ob);
    }
    private static void set(Object ob,String n,Object val)throws Exception{
        Field f=ob.getClass().getDeclaredField(n);f.setAccessible(true);f.set(ob,val);
    }
    private static Object run(Object ob,String n,Class<?>[] types,Object... values)throws Exception{
        Method m=ob.getClass().getDeclaredMethod(n,types);m.setAccessible(true);
        return m.invoke(ob,values);
    }
    private static void tap(RiftGameView view,float x,float y){
        long at=android.os.SystemClock.uptimeMillis();
        MotionEvent ev=MotionEvent.obtain(at,at,MotionEvent.ACTION_DOWN,x,y,0);
        view.onTouchEvent(ev);ev.recycle();
    }
    @Test public void tappedLevelThreeTowerUpgradesSameSlotWithRealLevelFourPrice()throws Exception{
        Context context=RuntimeEnvironment.getApplication();
        context.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE)
                .edit().clear().commit();
        RiftGameView v=new RiftGameView(context);v.layout(0,0,1280,720);
        run(v,"start",new Class<?>[]{int.class},0);
        int[] towers=(int[])get(v,"towers");
        float[] hp=(float[])get(v,"towerHp");
        towers[0]=3;hp[0]=TowerDurability.maxHp(3,(Integer)get(v,"fortressRank"));
        set(v,"gold",999);
        float[] xs=(float[])get(v,"towerX"),ys=(float[])get(v,"towerY");
        float x=640+(xs[0]-LaneRules.VIEW_CENTER_X)*1.1f;
        float y=360+(ys[0]-LaneRules.VIEW_CENTER_Y)*1.1f;
        tap(v,x,y);
        assertEquals(0,get(v,"selectedTowerPad"));
        assertEquals(0,get(v,"buildTargetPad"));
        assertEquals(3,get(v,"nextTowerTier"));
        int before=(Integer)get(v,"gold");
        tap(v,849,589);
        assertEquals(4,towers[0]);
        assertEquals(before-BattleRules.towerCost(3),get(v,"gold"));
        assertEquals(0,get(v,"selectedTowerPad"));
        Bitmap b=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        v.draw(new Canvas(b));
        File cwd=new File(System.getProperty("user.dir"));
        File dir=new File(cwd,new File(cwd,"app/build.gradle").exists()?
                "app/build/screenshots":"build/screenshots");
        if(!dir.exists())assertTrue(dir.mkdirs());
        try(FileOutputStream out=new FileOutputStream(new File(dir,"240-v065-selected-tower-next-level.png"))){
            assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));
        }
        b.recycle();
        tap(v,x,y); // same tower toggles off
        assertEquals(-1,get(v,"selectedTowerPad"));
    }
    @Test public void cappedTowerNeverChargesGoldOrUpgradesBeyondFive()throws Exception{
        RiftGameView v=new RiftGameView(RuntimeEnvironment.getApplication());
        v.layout(0,0,1280,720);
        run(v,"start",new Class<?>[]{int.class},0);
        int[] towers=(int[])get(v,"towers");
        float[] hp=(float[])get(v,"towerHp");
        towers[1]=BattleRules.MAX_TOWER_TIER;
        hp[1]=TowerDurability.maxHp(towers[1],(Integer)get(v,"fortressRank"));
        set(v,"selectedTowerPad",1);set(v,"gold",500);
        assertEquals(BattleRules.MAX_TOWER_TIER,get(v,"nextTowerTier"));
        run(v,"build",new Class<?>[0]);
        assertEquals(BattleRules.MAX_TOWER_TIER,towers[1]);
        assertEquals(500,get(v,"gold"));
    }
}
