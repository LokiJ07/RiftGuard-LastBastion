package com.lokij07.riftguard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class ActorAnimationVfxTest {
    @Test public void independentLegsAlternateAndPlantOnGround(){
        float h=140;
        ActorGait.Leg left=ActorGait.leg(0,true,-1,h);
        ActorGait.Leg right=ActorGait.leg(0,true,1,h);
        assertTrue("One foot must lift in the swing phase",left.footY<-5);
        assertEquals("Opposing foot is ground planted",0,right.footY,.0001f);
        assertFalse(left.planted);
        assertTrue(right.planted);
        ActorGait.Leg moved=ActorGait.leg((float)Math.PI/2,true,-1,h);
        assertNotEquals(left.footX,moved.footX,.001f);
        ActorGait.Leg idle=ActorGait.leg(0,false,-1,h);
        assertEquals(0,idle.footY,.0001f);
        assertTrue(idle.planted);
        // Strides must move feet without making huge disconnected boot swings.
        for(int n=0;n<36;n++){
            ActorGait.Leg pose=ActorGait.leg(n*.18f,true,-1,h);
            assertTrue(Math.abs(pose.footX-pose.hipX)<h*.14f);
            assertTrue(pose.footY>=-h*.055f);
        }
        assertThrows(IllegalArgumentException.class,()->ActorGait.leg(0,true,0,h));
    }
    @Test public void illustratedBossesAndFourLeggedAnimalsKeepIntactSourceArt(){
        for(int kind:new int[]{0,1,2,4})
            assertTrue("Ground type should have a segmented gait "+kind,
                    MotionArt.segmentedEnemyLegs(kind));
        for(int kind:new int[]{3,5,6})
            assertFalse("Unsegmented model must retain its intact art "+kind,
                    MotionArt.segmentedEnemyLegs(kind));
    }

    @Test public void sixHeroWeaponsKeepGripWhileRunning(){
        for(int id=0;id<6;id++){
            WeaponRig.Pose idle=WeaponRig.pose(id,-1,-1,.1f,false);
            WeaponRig.Pose run=WeaponRig.pose(id,-1,-1,.1f,true);
            assertTrue(Math.abs(idle.handX-run.handX)>1f ||
                    Math.abs(idle.handY-run.handY)>1f);
            WeaponRig.Pose attack=WeaponRig.pose(id,.5f,-1,.25f,true);
            assertTrue(Float.isFinite(attack.elbowX));
            assertTrue(Float.isFinite(attack.handX));
        }
    }
    @Test public void projectileModelsHaveDistinctActualRenderedImages(){
        Bitmap b=Bitmap.createBitmap(240,180,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(b);
        Set<Integer> hashes=new HashSet<>();
        for(int slot=-1;slot<4;slot++){
            for(int id:new int[]{0,3,4,5}){
                c.drawColor(Color.TRANSPARENT,android.graphics.PorterDuff.Mode.CLEAR);
                CombatVfx.projectile(c,120,90,205,105,id,slot,.25f);
                int hash=0x811c9dc5;
                for(int y=60;y<130;y+=2)for(int x=50;x<180;x+=2)
                    hash=(hash^b.getPixel(x,y))*0x01000193;
                hashes.add(hash);
            }
        }
        assertTrue("Projectile atlas must show at least eight visual families",
                hashes.size()>=8);
        b.recycle();
    }
    @Test public void realEnemyDeathPersistsVisuallyThenExpiresWithoutExtraKill()
            throws Exception {
        Context ctx=RuntimeEnvironment.getApplication();
        ctx.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE)
                .edit().clear().commit();
        RiftGameView view=new RiftGameView(ctx);view.layout(0,0,1280,720);
        call(view,"start",new Class[]{int.class},0);
        call(view,"spawnEnemy",new Class[0]);
        @SuppressWarnings("unchecked")
        List<Object> enemies=(List<Object>)field(view,"enemies");
        assertEquals(1,enemies.size());
        Object enemy=enemies.get(0);
        Field hp=enemy.getClass().getDeclaredField("hp");hp.setAccessible(true);
        hp.setFloat(enemy,-1f);
        call(view,"update",new Class[]{float.class},.016f);
        assertTrue(enemies.isEmpty());
        assertEquals(1,field(view,"kills"));
        @SuppressWarnings("unchecked")
        List<Object> deathEffects=(List<Object>)field(view,"deaths");
        assertEquals(1,deathEffects.size());
        Bitmap bmp=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(bmp));bmp.recycle();
        call(view,"update",new Class[]{float.class},.72f);
        assertEquals(0,deathEffects.size());
        assertEquals(1,field(view,"kills"));
    }
    private static Object field(Object o,String name)throws Exception{
        Field f=o.getClass().getDeclaredField(name);
        f.setAccessible(true);return f.get(o);
    }
    private static void call(Object o,String name,Class<?>[] types,Object...args)throws Exception{
        Method m=o.getClass().getDeclaredMethod(name,types);
        m.setAccessible(true);m.invoke(o,args);
    }
}
