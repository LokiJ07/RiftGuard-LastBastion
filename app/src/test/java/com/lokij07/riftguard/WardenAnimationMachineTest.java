package com.lokij07.riftguard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class WardenAnimationMachineTest {
    private static Object field(Object o,String n)throws Exception{
        Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);
        return f.get(o);
    }
    private static void set(Object o,String n,Object v)throws Exception{
        Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);f.set(o,v);
    }
    private static void call(Object o,String method,Class<?>[] args,Object...v)throws Exception{
        Method m=o.getClass().getDeclaredMethod(method,args);m.setAccessible(true);
        m.invoke(o,v);
    }
    @Test public void sixDistinctActualContactMarkersFireExactlyOnce(){
        for(int w=0;w<6;w++){
            WardenAnimationMachine a=new WardenAnimationMachine();
            assertTrue(a.attack(w));
            assertEquals(WardenAnimationMachine.State.ATTACK,a.state());
            float target=WeaponAnimation.duration(w)*WardenAnimationMachine.contactProgress(w);
            a.advance(target*.7f,false);
            assertFalse("Contact cannot precede visible strike "+w,
                    a.consume(WardenAnimationMachine.Event.ATTACK_CONTACT));
            a.advance(target*.40f,false);
            assertTrue("Weapon-specific contact callback missing "+w,
                    a.consume(WardenAnimationMachine.Event.ATTACK_CONTACT));
            assertFalse("Exactly once on subsequent query "+w,
                    a.consume(WardenAnimationMachine.Event.ATTACK_CONTACT));
            a.advance(.18f,false);
            a.advance(.18f,false);
            assertFalse("Cannot double-hit during recovery "+w,
                    a.consume(WardenAnimationMachine.Event.ATTACK_CONTACT));
            assertEquals(WardenAnimationMachine.State.IDLE,a.state());
        }
        assertTrue(WardenAnimationMachine.contactProgress(5)>
                WardenAnimationMachine.contactProgress(4));
        // Heavy sword's contact happens LATER in absolute seconds because
        // its weighted windup clip is longer, even with an earlier fraction.
        assertTrue(WeaponAnimation.duration(1)*WardenAnimationMachine.contactProgress(1)>
                WeaponAnimation.duration(2)*WardenAnimationMachine.contactProgress(2));
    }
    @Test public void movingFeetRemainStableAcrossAttackAndCast(){
        WardenAnimationMachine a=new WardenAnimationMachine();
        a.advance(.16f,true);
        assertEquals(WardenAnimationMachine.State.RUN,a.state());
        assertFalse(a.consume(WardenAnimationMachine.Event.FOOTSTEP));
        assertTrue(a.attack(1));
        a.advance(.08f,true);
        assertTrue(a.consume(WardenAnimationMachine.Event.FOOTSTEP));
        assertFalse(a.consume(WardenAnimationMachine.Event.FOOTSTEP));
        assertTrue(a.cast());
        assertEquals(WardenAnimationMachine.State.CAST,a.state());
        assertFalse("Interrupted sword cannot hit invisibly",
                a.consume(WardenAnimationMachine.Event.ATTACK_CONTACT));
        a.advance(.20f,true);
        a.advance(.20f,true);
        a.advance(.20f,true);
        assertEquals(WardenAnimationMachine.State.RUN,a.state());
        assertEquals(1,a.sourceRow());
        a.advance(.04f,false);
        assertEquals(WardenAnimationMachine.State.IDLE,a.state());
        assertEquals(0,a.sourceRow());
    }
    @Test public void hurtInterruptsAndDeathHasAbsolutePriority(){
        WardenAnimationMachine a=new WardenAnimationMachine();
        a.attack(5);
        a.advance(.07f,false);
        a.hurt();
        assertEquals(WardenAnimationMachine.State.HURT,a.state());
        assertTrue(a.locked());assertFalse(a.attack(0));
        a.advance(.10f,false);
        assertEquals(WardenAnimationMachine.State.HURT,a.state());
        a.advance(.12f,false);
        assertEquals(WardenAnimationMachine.State.IDLE,a.state());
        assertTrue(a.attack(4));
        a.die();a.hurt();
        assertEquals(WardenAnimationMachine.State.DEATH,a.state());
        assertFalse(a.cast());assertFalse(a.attack(1));
        for(int i=0;i<4;i++)a.advance(.20f,false);
        assertEquals(WardenAnimationMachine.State.DEATH,a.state());
        assertEquals(1f,a.deathProgress(),.001f);
        assertTrue(a.consume(WardenAnimationMachine.Event.CLIP_FINISHED));
        assertFalse(a.consume(WardenAnimationMachine.Event.CLIP_FINISHED));
    }
    @Test public void largeAndSmallFrameDeltaProduceOneContactNotMany(){
        for(int w:new int[]{0,1,2,3,4,5}){
            WardenAnimationMachine a=new WardenAnimationMachine();
            assertTrue(a.attack(w));
            int events=0;
            for(int i=0;i<60;i++){
                a.advance(.01666f,false);
                if(a.consume(WardenAnimationMachine.Event.ATTACK_CONTACT))events++;
            }
            assertEquals("60fps marker count "+w,1,events);
            a.reset();a.attack(w);events=0;
            for(int i=0;i<10;i++){
                a.advance(.10f,false);
                if(a.consume(WardenAnimationMachine.Event.ATTACK_CONTACT))events++;
            }
            assertEquals("10fps marker count "+w,1,events);
        }
    }
    @Test public void actualIronSwordCollisionDrivenByFrameMarker()throws Exception{
        Context ctx=RuntimeEnvironment.getApplication();
        ctx.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE)
                .edit().clear().commit();
        RiftGameView game=new RiftGameView(ctx);
        call(game,"start",new Class[]{int.class},0);
        set(game,"equippedWeapon",1);
        call(game,"spawnEnemy",new Class[0]);
        @SuppressWarnings("unchecked")
        List<Object> enemies=(List<Object>)field(game,"enemies");
        Object e=enemies.get(0);
        float x=(float)field(game,"heroX"),y=(float)field(game,"heroY");
        set(e,"x",x+77f);set(e,"y",y);
        set(e,"hp",900f);set(e,"maxHp",900f);
        call(game,"fire",new Class[0]);
        WardenAnimationMachine anim=(WardenAnimationMachine)field(game,"heroAnimation");
        assertEquals(WardenAnimationMachine.State.ATTACK,anim.state());
        assertEquals(900f,(float)field(e,"hp"),.001f);
        call(game,"update",new Class[]{float.class},.10f);
        assertEquals("No early hit before keyframe",900f,(float)field(e,"hp"),.001f);
        call(game,"update",new Class[]{float.class},.065f);
        assertTrue("Combat hit must coincide with new Greatsword marker",
                (float)field(e,"hp")<900f);
        float after=(float)field(e,"hp");
        call(game,"update",new Class[]{float.class},.11f);
        assertEquals("Cannot damage the same enemy again within one strike",
                after,(float)field(e,"hp"),.001f);
    }
    @Test public void paintedHeroDeathPoseActuallyTiltsAndFades()throws Exception{
        Context ctx=RuntimeEnvironment.getApplication();
        RiftGameView game=new RiftGameView(ctx);
        game.layout(0,0,1280,720);
        call(game,"start",new Class[]{int.class},0);
        call(game,"markDefeat",new Class[0]);
        WardenAnimationMachine a=(WardenAnimationMachine)field(game,"heroAnimation");
        assertEquals(WardenAnimationMachine.State.DEATH,a.state());
        // The LOSE screen overlays a result panel over the Warden. Draw the
        // ACTUAL in-game actor itself into a local viewport for pixel QA.
        float hx=(float)field(game,"heroX"),hy=(float)field(game,"heroY");
        Bitmap before=Bitmap.createBitmap(440,290,Bitmap.Config.ARGB_8888);
        Canvas initial=new Canvas(before);
        initial.translate(220-hx,240-hy);
        call(game,"drawHero",new Class[]{Canvas.class},initial);
        a.advance(.20f,false);
        Bitmap after=Bitmap.createBitmap(440,290,Bitmap.Config.ARGB_8888);
        Canvas later=new Canvas(after);
        later.translate(220-hx,240-hy);
        call(game,"drawHero",new Class[]{Canvas.class},later);
        int changes=0;
        for(int y=15;y<270;y+=2)for(int x=95;x<345;x+=2)
            if(before.getPixel(x,y)!=after.getPixel(x,y))changes++;
        assertTrue("Defeat must visibly change the Warden pose, not just UI",
                changes>10);
        before.recycle();after.recycle();
    }
}
