package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class WeaponMasteryUpgradeTest {
    private SharedPreferences fresh(){
        SharedPreferences p=RuntimeEnvironment.getApplication()
                .getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE);
        p.edit().clear().commit();return p;
    }
    private static Object get(Object o,String name)throws Exception{
        Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);
    }
    private static void set(Object o,String name,Object v)throws Exception{
        Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);f.set(o,v);
    }
    private static void invoke(Object o,String name,Class<?>[] t,Object...a)throws Exception{
        Method m=o.getClass().getDeclaredMethod(name,t);m.setAccessible(true);m.invoke(o,a);
    }
    private static void tap(RiftGameView v,float x,float y){
        long n=SystemClock.uptimeMillis();
        MotionEvent d=MotionEvent.obtain(n,n,MotionEvent.ACTION_DOWN,x,y,0);
        v.onTouchEvent(d);d.recycle();
        MotionEvent u=MotionEvent.obtain(n,n+35,MotionEvent.ACTION_UP,x,y,0);
        v.onTouchEvent(u);u.recycle();
    }
    private static void drag(RiftGameView v,float x,float from,float to){
        long n=SystemClock.uptimeMillis();
        MotionEvent d=MotionEvent.obtain(n,n,MotionEvent.ACTION_DOWN,x,from,0);
        v.onTouchEvent(d);d.recycle();
        for(int i=1;i<=7;i++){
            MotionEvent move=MotionEvent.obtain(n,n+i*27,MotionEvent.ACTION_MOVE,
                    x,from+(to-from)*i/7,0);
            v.onTouchEvent(move);move.recycle();
        }
        MotionEvent u=MotionEvent.obtain(n,n+210,MotionEvent.ACTION_UP,x,to,0);
        v.onTouchEvent(u);u.recycle();
    }

    @Test public void allSixtySixNodesFormSixIndependentUpwardGraphs(){
        assertEquals(6,WeaponMastery.WEAPON_COUNT);
        assertEquals(11,WeaponMastery.NODES_PER_WEAPON);
        for(int weapon=0;weapon<6;weapon++){
            for(int id=0;id<11;id++){
                WeaponMastery.Node node=WeaponMastery.node(weapon,id);
                assertEquals(weapon,node.weapon);
                assertTrue(node.cost>=0);
                assertTrue(node.damageBonus>=0);
                if(id==0)assertEquals(-1,node.parent);
                else {
                    assertTrue(node.parent>=0&&node.parent<id);
                    assertEquals(node.tier-1,WeaponMastery.node(weapon,node.parent).tier);
                }
                assertEquals(id,WeaponMastery.at(node.tier,node.lane));
            }
            assertEquals(0,WeaponMastery.at(0,1));
        assertEquals(-1,WeaponMastery.at(0,0));
        }
        assertTrue(WeaponMastery.worldY(0)>WeaponMastery.worldY(10));
    }
    @Test public void purchasesArePerWeaponPersistAndPinnacleNeedsAllThreeBranches(){
        SharedPreferences p=fresh();
        p.edit().putInt("supplies",40000).commit();
        WeaponMastery m=new WeaponMastery(p);
        assertEquals(1,m.count(0));assertEquals(1,m.count(1));
        assertFalse(m.available(0,10,5));
        assertFalse(m.buy(0,4,5));
        assertTrue(m.buy(0,1,1));
        assertTrue(m.damageMultiplier(0)>1);
        assertEquals(1f,m.damageMultiplier(1),.001f);
        assertFalse(m.buy(0,1,1)); // no double charge
        for(int id=2;id<=9;id++)assertTrue("Can unlock node "+id,
                m.buy(0,id,5));
        assertTrue(m.available(0,10,5));
        int wallet=p.getInt("supplies",-1);
        assertTrue(m.buy(0,10,5));
        assertTrue(m.mastered(0));assertFalse(m.mastered(1));
        assertEquals(wallet-WeaponMastery.node(0,10).cost,
                p.getInt("supplies",-1));
        WeaponMastery restored=new WeaponMastery(p);
        assertEquals(11,restored.count(0));
        assertEquals(1,restored.count(1));
        assertTrue(restored.cooldownMultiplier(0)<1);
        assertTrue(restored.bonusRange(0)>0);
        assertEquals(0f,restored.bonusRange(1),.01f);
    }
    @Test public void rankAndCurrencyGatesStopUnauthorizedPurchase(){
        SharedPreferences p=fresh();
        p.edit().putInt("supplies",30).commit();
        WeaponMastery m=new WeaponMastery(p);
        assertFalse(m.buy(0,1,1));
        assertEquals(30,p.getInt("supplies",-1));
        p.edit().putInt("supplies",2000).commit();
        assertFalse(m.buy(5,1,1)); // Frost Lance needs fortress 5
        assertTrue(m.buy(5,1,5));
        assertFalse(m.buy(5,4,2));
        assertTrue(m.buy(5,4,5));
        assertThrows(IllegalArgumentException.class,()->WeaponMastery.node(6,0));
    }
    @Test public void actualProjectileDamageCooldownAndRangeChange()throws Exception{
        SharedPreferences p=fresh();p.edit().putInt("supplies",2000).commit();
        WeaponMastery m=new WeaponMastery(p);
        assertTrue(m.buy(0,1,1));
        assertTrue(m.buy(0,2,1));
        assertTrue(m.buy(0,3,1));
        RiftGameView game=new RiftGameView(RuntimeEnvironment.getApplication());
        invoke(game,"start",new Class[]{int.class},0);
        invoke(game,"spawnEnemy",new Class[0]);
        @SuppressWarnings("unchecked")
        List<Object> enemies=(List<Object>)get(game,"enemies");
        Object enemy=enemies.get(0);
        float hx=(float)get(game,"heroX"),hy=(float)get(game,"heroY");
        set(enemy,"x",hx+170);set(enemy,"y",hy);set(enemy,"hp",1000f);
        invoke(game,"fire",new Class[0]);
        @SuppressWarnings("unchecked")
        List<Object> shots=(List<Object>)get(game,"shots");
        assertEquals(1,shots.size());
        assertEquals(Progression.WEAPONS[0].damage*m.damageMultiplier(0),
                (float)get(shots.get(0),"damage"),.01f);
        assertEquals(Progression.WEAPONS[0].cooldown*m.cooldownMultiplier(0),
                (float)get(game,"shotTimer"),.001f);
        assertEquals(WeaponAnimation.duration(0),(float)get(game,"fireFlash"),.001f);
    }
    @Test public void crossbowPinnacleCreatesActualPhantomVolleyExtraDamage()
            throws Exception {
        SharedPreferences p=fresh();
        p.edit().putInt("supplies",50000).putInt("fortress_rank",5).commit();
        WeaponMastery tree=new WeaponMastery(p);
        for(int id=1;id<=10;id++)
            assertTrue("Must purchase all nodes before the signature shot",
                    tree.buy(0,id,5));
        assertTrue(tree.mastered(0));
        RiftGameView game=new RiftGameView(RuntimeEnvironment.getApplication());
        invoke(game,"start",new Class[]{int.class},0);
        invoke(game,"spawnEnemy",new Class[0]);
        @SuppressWarnings("unchecked")
        List<Object> foes=(List<Object>)get(game,"enemies");
        Object target=foes.get(0);
        float x=(float)get(game,"heroX"),y=(float)get(game,"heroY");
        set(target,"x",x+150f);set(target,"y",y);set(target,"hp",1500f);
        invoke(game,"fire",new Class[0]);
        @SuppressWarnings("unchecked")
        List<Object> bullets=(List<Object>)get(game,"shots");
        assertEquals(1,bullets.size());
        Object projectile=bullets.get(0);
        float damage=(float)get(projectile,"damage");
        set(projectile,"x",x+150f);set(projectile,"y",y-10f);
        invoke(game,"update",new Class[]{float.class},.016f);
        assertEquals("Phantom Volley adds a second 40% contact hit",
                1500f-damage*1.40f,(float)get(target,"hp"),.7f);
    }

    @Test public void armoryTreeIsActuallyTapSwipeBuyAndEquipInteractive()throws Exception{
        SharedPreferences p=fresh();p.edit().putInt("supplies",900).commit();
        RiftGameView view=new RiftGameView(RuntimeEnvironment.getApplication());
        view.layout(0,0,1280,720);
        tap(view,221,540);
        assertEquals("ARMORY",get(view,"scene").toString());
        tap(view,1049,133);
        assertEquals("WEAPON_TREE",get(view,"scene").toString());
        assertEquals(0,get(view,"selectedMasteryWeapon"));
        float bottom=(float)get(view,"weaponTreeScroll");
        Bitmap screen=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(screen));
        assertNotEquals(Color.TRANSPARENT,screen.getPixel(430,515));
        screen.recycle();
        // First-tier path1 is visible near x=249,y=374.
        tap(view,248,374);
        assertEquals(1,get(view,"selectedWeaponMasteryNode"));
        tap(view,996,550);
        assertTrue(new WeaponMastery(p).owned(0,1));
        assertEquals(900-WeaponMastery.node(0,1).cost,p.getInt("supplies",-1));
        drag(view,431,465,210);
        assertTrue((float)get(view,"weaponTreeScroll")<bottom);
        tap(view,77+5*189+30,141);
        assertEquals(5,get(view,"selectedMasteryWeapon"));
        tap(view,589,652);
        assertEquals("WEAPON_TREE",get(view,"scene").toString());
        assertEquals(0,get(view,"equippedWeapon")); // locked Frost Lance
        tap(view,198,652);
        assertEquals("ARMORY",get(view,"scene").toString());
    }
}
