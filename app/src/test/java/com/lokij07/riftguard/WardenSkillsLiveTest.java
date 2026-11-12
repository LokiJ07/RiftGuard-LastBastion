package com.lokij07.riftguard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.view.MotionEvent;
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

/** Tests ACTUAL view and enemy mutations: effects cannot be cosmetic-only. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class WardenSkillsLiveTest {
    private static Object get(Object o,String name)throws Exception{
        Field field=o.getClass().getDeclaredField(name);field.setAccessible(true);
        return field.get(o);
    }
    private static void set(Object o,String name,Object value)throws Exception{
        Field field=o.getClass().getDeclaredField(name);field.setAccessible(true);
        field.set(o,value);
    }
    private static void call(Object o,String name,Class<?>[] args,Object...params)throws Exception{
        Method method=o.getClass().getDeclaredMethod(name,args);method.setAccessible(true);
        method.invoke(o,params);
    }
    private static RiftGameView game(){
        Context context=RuntimeEnvironment.getApplication();
        context.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE)
                .edit().clear().commit();
        RiftGameView view=new RiftGameView(context);
        view.layout(0,0,1280,720);
        return view;
    }
    private static void tap(RiftGameView view,float x,float y){
        long now=SystemClock.uptimeMillis();
        MotionEvent down=MotionEvent.obtain(now,now,MotionEvent.ACTION_DOWN,x,y,0);
        view.onTouchEvent(down);down.recycle();
        MotionEvent up=MotionEvent.obtain(now,now+30,MotionEvent.ACTION_UP,x,y,0);
        view.onTouchEvent(up);up.recycle();
    }
    private static Object enemy(RiftGameView game,float x,float y)throws Exception{
        call(game,"spawnEnemy",new Class[0]);
        @SuppressWarnings("unchecked")
        List<Object> enemies=(List<Object>)get(game,"enemies");
        Object target=enemies.get(enemies.size()-1);
        set(target,"x",x);set(target,"y",y);
        set(target,"hp",1200f);set(target,"maxHp",1200f);
        return target;
    }
    @Test public void sixSkillsHaveRankGatesAndPersistentIndependentLoadout()throws Exception{
        RiftGameView view=game();
        assertTrue(WardenSkills.unlocked(1,0));
        for(int i=1;i<6;i++)assertFalse(WardenSkills.unlocked(1,i));
        assertFalse(WardenSkills.unlocked(5,6));
        assertEquals(6,WardenSkills.SKILLS.length);
        assertEquals(11f,WardenSkills.effectiveCooldown(0,0),.01f);
        assertEquals(6f,WardenSkills.effectiveCooldown(0,5),.01f);
        assertEquals(25f,WardenSkills.clampHealth(13,12,100),.01f);
        assertEquals(100f,WardenSkills.clampHealth(99,100,100),.01f);
        tap(view,180,534); // Home -> Armory
        tap(view,840,652); // Armory -> Abilities
        assertEquals("SKILLS",get(view,"scene").toString());
        tap(view,1041,444); // Locked Meteor
        assertEquals(0,get(view,"equippedSkill"));
        set(view,"fortressRank",5);
        tap(view,1041,444); // Unlocked Meteor
        assertEquals(WardenSkills.METEOR,get(view,"equippedSkill"));
        assertEquals(5,RuntimeEnvironment.getApplication()
                .getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE)
                .getInt("skill",-1));
        // Real fortress-rank loadout never lets players use spells while locked.
        RiftGameView reload= new RiftGameView(RuntimeEnvironment.getApplication());
        assertEquals(WardenSkills.PULSE,get(reload,"equippedSkill"));
        RuntimeEnvironment.getApplication().getSharedPreferences(
                "riftguard_campaign_v1",Context.MODE_PRIVATE)
                .edit().putInt("fortress_rank",5).commit();
        reload=new RiftGameView(RuntimeEnvironment.getApplication());
        assertEquals(WardenSkills.METEOR,get(reload,"equippedSkill"));
    }

    @Test public void eachEquippedSkillChangesRealWorldCombatOrHealth()throws Exception{
        RiftGameView view=game();
        set(view,"fortressRank",5);
        call(view,"start",new Class[]{int.class},0);
        float x=(float)get(view,"heroX"),y=(float)get(view,"heroY");
        set(view,"heroDir",0f);
        Object foe=enemy(view,x+96,y);
        // PULSE knocks enemies back and deals damage.
        set(view,"equippedSkill",WardenSkills.PULSE);
        call(view,"castSkill",new Class[0]);
        assertTrue((float)get(foe,"hp")<1200f);
        assertTrue((float)get(foe,"x")>x+96);
        assertTrue((float)get(view,"pulseCooldown")>0);
        set(view,"pulseCooldown",0f);
        // EMBER must burn and keep damaging in update frames.
        set(foe,"x",x+110);set(foe,"y",y);set(foe,"hp",1200f);
        set(view,"equippedSkill",WardenSkills.EMBER);
        call(view,"castSkill",new Class[0]);
        float burned=(float)get(foe,"hp");
        assertTrue(burned<1200);
        assertTrue((float)get(foe,"burnTimer")>0);
        call(view,"update",new Class[]{float.class},.15f);
        assertTrue((float)get(foe,"hp")<burned);
        set(view,"pulseCooldown",0f);
        // FROST causes damage and genuine movement slowing.
        set(foe,"x",x+90);set(foe,"y",y);set(foe,"hp",1200f);
        set(view,"equippedSkill",WardenSkills.FROST);
        call(view,"castSkill",new Class[0]);
        assertTrue((float)get(foe,"slowTimer")>=3f);
        assertTrue((float)get(foe,"hp")<1200);
        set(view,"pulseCooldown",0f);
        // BANNER heals hero immediately and core only when placed near it.
        set(view,"heroHp",22f);
        set(view,"equippedSkill",WardenSkills.BANNER);
        call(view,"castSkill",new Class[0]);
        assertTrue((float)get(view,"heroHp")>22f);
        assertTrue((float)get(view,"bannerTime")>0);
        set(view,"bannerX",950f);set(view,"bannerY",510f);
        set(view,"baseHp",750f);
        call(view,"update",new Class[]{float.class},.1f);
        assertTrue((float)get(view,"baseHp")>750f);
        set(view,"pulseCooldown",0f);
        // TRAP is placed ahead and damages a creature entering it.
        set(view,"equippedSkill",WardenSkills.TRAP);
        call(view,"castSkill",new Class[0]);
        assertTrue((float)get(view,"trapTime")>0);
        set(foe,"x",get(view,"trapX"));set(foe,"y",get(view,"trapY"));
        set(foe,"hp",1200f);
        call(view,"update",new Class[]{float.class},.03f);
        assertEquals(0f,(float)get(view,"trapTime"),.001f);
        assertTrue((float)get(foe,"hp")<1200f);
        set(view,"pulseCooldown",0f);
        // METEOR telegraphs first, then hits the actual target on countdown.
        set(foe,"x",x+120);set(foe,"y",y);set(foe,"hp",1200f);
        set(view,"equippedSkill",WardenSkills.METEOR);
        call(view,"castSkill",new Class[0]);
        assertTrue((float)get(view,"meteorTime")>0);
        assertEquals(1200f,(float)get(foe,"hp"),.001f);
        call(view,"update",new Class[]{float.class},1.2f);
        assertEquals(0f,(float)get(view,"meteorTime"),.001f);
        assertTrue((float)get(foe,"hp")<1200f);
        Bitmap screen=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(screen));screen.recycle();
    }

    @Test public void castingCooldownAndLockedSkillCannotBeBypassed()throws Exception{
        RiftGameView game=game();
        call(game,"start",new Class[]{int.class},0);
        set(game,"equippedSkill",WardenSkills.METEOR);
        call(game,"castSkill",new Class[0]);
        assertEquals(0f,(float)get(game,"pulseCooldown"),.001f);
        set(game,"equippedSkill",WardenSkills.PULSE);
        call(game,"castSkill",new Class[0]);
        float oldCooldown=(float)get(game,"pulseCooldown");
        call(game,"castSkill",new Class[0]);
        assertEquals(oldCooldown,(float)get(game,"pulseCooldown"),.001f);
        assertTrue(oldCooldown>0);
    }
}
