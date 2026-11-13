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

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class DestructibleTerrainV065Test {
    private static Object get(Object o,String key)throws Exception{
        Field f=o.getClass().getDeclaredField(key);f.setAccessible(true);return f.get(o);
    }
    private static void set(Object o,String key,Object value)throws Exception{
        Field f=o.getClass().getDeclaredField(key);f.setAccessible(true);f.set(o,value);
    }
    private static Object call(Object o,String name,Class<?>[] types,Object... args)throws Exception{
        Method m=o.getClass().getDeclaredMethod(name,types);m.setAccessible(true);
        return m.invoke(o,args);
    }
    @Test public void allThreeHundredStagesHavePhysicalWorldSpecificProps(){
        for(int stage=0;stage<300;stage++){
            DestructibleTerrainV065 land=new DestructibleTerrainV065(stage);
            assertEquals(stage,land.stage());
            assertTrue(land.count()>=3&&land.count()<=4);
            assertEquals(land.count(),land.intact());
            for(int i=0;i<land.count();i++){
                DestructibleTerrainV065.Prop prop=land.at(i);
                assertEquals(Campaign.world(stage),prop.type);
                assertTrue(prop.x>1100&&prop.x<1900);
                assertTrue(prop.maxHp>75);
                assertEquals(1f,prop.fraction(),.001f);
            }
        }
    }
    @Test public void explosionsCollapseObstaclesAndPermanentlyChangeRoutes(){
        DestructibleTerrainV065 land=new DestructibleTerrainV065(Campaign.stage(10,1));
        DestructibleTerrainV065.Prop p=land.at(0);
        float steer=land.waypointOffset(p.x,p.y);
        assertTrue(Math.abs(steer)>0);
        assertEquals(1,land.splash(p.x,p.y,45,10000));
        assertTrue(p.broken());
        assertEquals(0f,land.waypointOffset(p.x,p.y),.001f);
        assertTrue(land.rubbleSlow(p.x,p.y)<1f);
        assertEquals(0,land.splash(p.x,p.y,45,10000));
        assertFalse(p.damage(20));
    }
    @Test public void playerCanAimRealGreatswordAttackAtTerrain()throws Exception{
        Context context=RuntimeEnvironment.getApplication();
        SharedPreferences p=context.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE);
        p.edit().clear().putInt("unlocked",3).commit();
        RiftGameView game=new RiftGameView(context);
        game.layout(0,0,1280,720);
        call(game,"start",new Class<?>[]{int.class},0);
        DestructibleTerrainV065 land=(DestructibleTerrainV065)get(game,"terrain");
        DestructibleTerrainV065.Prop target=land.at(0);
        set(game,"heroX",target.x-60f);set(game,"heroY",target.y);
        set(game,"equippedWeapon",1);
        set(game,"focusedTerrain",target);
        float before=target.hp;
        call(game,"fire",new Class<?>[0]);
        assertTrue((Boolean)get(game,"pendingMelee"));
        call(game,"resolveMeleeHit",new Class<?>[0]);
        assertTrue("Actual sword contact should reduce landmark health",target.hp<before);
        assertFalse((Boolean)get(game,"pendingMelee"));
    }
    @Test public void targetedRangedWeaponGeneratesARealTerrainProjectile()throws Exception{
        Context context=RuntimeEnvironment.getApplication();
        SharedPreferences p=context.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE);
        p.edit().clear().commit();
        RiftGameView game=new RiftGameView(context);
        game.layout(0,0,1280,720);
        call(game,"start",new Class<?>[]{int.class},0);
        DestructibleTerrainV065 land=(DestructibleTerrainV065)get(game,"terrain");
        DestructibleTerrainV065.Prop target=land.at(0);
        set(game,"heroX",target.x-120f);set(game,"heroY",target.y);
        set(game,"equippedWeapon",0);set(game,"focusedTerrain",target);
        float before=target.hp;
        call(game,"fire",new Class<?>[0]);
        for(int i=0;i<8;i++)call(game,"updateTerrainBolts",new Class<?>[]{float.class},.08f);
        assertTrue(target.hp<before);
        assertEquals(0,((java.util.List<?>)get(game,"terrainBolts")).size());
    }
    @Test public void visualBreakableTerrainHasActualRenderingGallery()throws Exception{
        Context context=RuntimeEnvironment.getApplication();
        SharedPreferences p=context.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE);
        p.edit().clear().commit();
        RiftGameView game=new RiftGameView(context);
        game.layout(0,0,1280,720);
        call(game,"start",new Class<?>[]{int.class},0);
        DestructibleTerrainV065 land=(DestructibleTerrainV065)get(game,"terrain");
        set(game,"focusedTerrain",land.at(0));
        Bitmap b=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        game.draw(new Canvas(b));
        File cwd=new File(System.getProperty("user.dir"));
        File dir=new File(cwd,new File(cwd,"app/build.gradle").exists()?
                "app/build/screenshots":"build/screenshots");
        if(!dir.exists())assertTrue(dir.mkdirs());
        try(FileOutputStream out=new FileOutputStream(new File(dir,"220-v065-breakable-terrain.png"))){
            assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));
        }
        b.recycle();
    }
}
