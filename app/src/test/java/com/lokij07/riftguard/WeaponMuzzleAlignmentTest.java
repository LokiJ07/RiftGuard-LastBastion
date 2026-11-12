package com.lokij07.riftguard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import static org.junit.Assert.*;

/** Checks the actual in-game firing origin, not just isolated animation metadata. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class WeaponMuzzleAlignmentTest {
    @Test public void fourRangedClassesAimTheirBitmapTipsAtTheirTargets() {
        float[] tipLengths={41f,65f,65f,68f,41f,84f};
        float[] facings={0f,(float)Math.PI,(float)Math.PI/2f,
                -(float)Math.PI/3f,2.1f};
        for(int id:new int[]{0,3,4,5})for(float facing:facings){
            WeaponRig.Pose pose=WeaponRig.aimedPose(id,0f,-1f,1f,false,facing);
            WeaponRig.Point muzzle=WeaponRig.muzzle(id,0f,-1f,1f,false,facing);
            boolean left=Math.cos(facing)<-.28f;
            float localHandX=left?-pose.handX:pose.handX;
            float dx=muzzle.x-localHandX,dy=muzzle.y-pose.handY;
            float projection=(float)(dx*Math.cos(facing)+dy*Math.sin(facing));
            assertEquals("Weapon "+id+" must point downrange",tipLengths[id],projection,.08f);
            assertEquals("Weapon tip is the actual combat model tip",tipLengths[id],
                    (float)Math.hypot(dx,dy),.08f);
        }
    }

    @Test public void heldWeaponsHaveNoOpaqueInventoryCardBackground() {
        for(int weaponId=0;weaponId<6;weaponId++){
            Bitmap bitmap=Bitmap.createBitmap(180,190,Bitmap.Config.ARGB_8888);
            Canvas canvas=new Canvas(bitmap);
            WorldArt.drawHeldWeapon(canvas,weaponId,90,140,1f,1f);
            int occupied=0;
            for(int y=50;y<167;y++)for(int x=40;x<140;x++)
                if(Color.alpha(bitmap.getPixel(x,y))>120)occupied++;
            assertTrue("Weapon must actually be visible: "+weaponId,occupied>90);
            assertTrue("Opaque card or badge must not be held as a weapon: "+weaponId,
                    occupied<4200);
            assertEquals(Color.TRANSPARENT,bitmap.getPixel(8,16));
            bitmap.recycle();
        }
    }

    @Test public void aLiveShotStartsAtTheDisplayedWeaponTipInBothDirections()
            throws Exception {
        Context ctx=RuntimeEnvironment.getApplication();
        ctx.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE)
                .edit().clear().commit();
        RiftGameView view=new RiftGameView(ctx);
        invoke(view,"start",new Class[]{int.class},0);
        invoke(view,"spawnEnemy",new Class[0]);
        @SuppressWarnings("unchecked")
        List<Object> enemies=(List<Object>)get(view,"enemies");
        @SuppressWarnings("unchecked")
        List<Object> shots=(List<Object>)get(view,"shots");
        Object target=enemies.get(0);
        float hx=(float)get(view,"heroX"),hy=(float)get(view,"heroY");
        set(view,"gameTime",1.125f);
        set(view,"heroMoving",false);
        for(int weaponId:new int[]{0,3,4,5}){
            for(int dir:new int[]{-1,1}){
                shots.clear();
                set(view,"equippedWeapon",weaponId);
                set(view,"shotTimer",-1f);
                set(view,"fireFlash",0f);
                set(target,"x",hx+dir*150f);
                set(target,"y",hy-29f);
                set(target,"hp",1000f);
                invoke(view,"fire",new Class[0]);
                assertEquals("Expected one real game projectile",1,shots.size());
                Object projectile=shots.get(0);
                float facing=(float)get(view,"heroDir");
                WeaponRig.Point muzzle=WeaponRig.muzzle(weaponId,0f,-1f,
                        1.125f,false,facing);
                assertEquals("Wrong projectile X for weapon "+weaponId,
                        hx+muzzle.x,(float)get(projectile,"x"),.03f);
                assertEquals("Wrong projectile Y for weapon "+weaponId,
                        hy+muzzle.y,(float)get(projectile,"y"),.03f);
                assertTrue("Projectile must not originate in the hero's belly",
                        Math.hypot(muzzle.x,muzzle.y)>40);
            }
        }
    }

    private static Object get(Object object,String name)throws Exception {
        Field f=object.getClass().getDeclaredField(name);
        f.setAccessible(true);return f.get(object);
    }
    private static void set(Object object,String name,Object value)throws Exception {
        Field f=object.getClass().getDeclaredField(name);
        f.setAccessible(true);f.set(object,value);
    }
    private static void invoke(Object object,String name,Class<?>[] types,
                               Object... args)throws Exception {
        Method m=object.getClass().getDeclaredMethod(name,types);
        m.setAccessible(true);m.invoke(object,args);
    }
}
