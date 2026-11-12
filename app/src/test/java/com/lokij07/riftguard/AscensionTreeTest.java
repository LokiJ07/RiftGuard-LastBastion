package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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
public final class AscensionTreeTest {
    private static final String SAVE="riftguard_campaign_v1";
    private SharedPreferences preferences(){
        SharedPreferences prefs=RuntimeEnvironment.getApplication()
                .getSharedPreferences(SAVE,Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        return prefs;
    }
    private static Object field(Object target,String name)throws Exception{
        Field f=target.getClass().getDeclaredField(name);
        f.setAccessible(true);return f.get(target);
    }
    private static void tap(RiftGameView game,float x,float y){
        long now=SystemClock.uptimeMillis();
        MotionEvent down=MotionEvent.obtain(now,now,MotionEvent.ACTION_DOWN,x,y,0);
        game.onTouchEvent(down);down.recycle();
        MotionEvent up=MotionEvent.obtain(now,now+36,MotionEvent.ACTION_UP,x,y,0);
        game.onTouchEvent(up);up.recycle();
    }
    private static void drag(RiftGameView game,float x,float from,float to){
        long now=SystemClock.uptimeMillis();
        MotionEvent down=MotionEvent.obtain(now,now,MotionEvent.ACTION_DOWN,x,from,0);
        game.onTouchEvent(down);down.recycle();
        int n=7;
        for(int i=1;i<=n;i++){
            float pos=from+(to-from)*i/n;
            MotionEvent move=MotionEvent.obtain(now,now+i*30,MotionEvent.ACTION_MOVE,x,pos,0);
            game.onTouchEvent(move);move.recycle();
        }
        MotionEvent up=MotionEvent.obtain(now,now+300,MotionEvent.ACTION_UP,x,to,0);
        game.onTouchEvent(up);up.recycle();
    }

    @Test public void fortyNodesFormConnectedUpwardUnlockGraph(){
        assertEquals(40,AscensionTree.NODES.length);
        assertEquals(10,AscensionTree.TIERS);
        assertEquals(0,AscensionTree.byId(0).tier);
        assertEquals(9,AscensionTree.byId(39).tier);
        assertTrue(AscensionTree.worldY(AscensionTree.byId(0))>
                AscensionTree.worldY(AscensionTree.byId(39)));
        for(AscensionTree.Node node:AscensionTree.NODES){
            assertEquals(node.id,AscensionTree.byId(node.id).id);
            assertTrue(node.requiredRank>=1&&node.requiredRank<=5);
            assertTrue(node.cost>=0);
            assertTrue(node.gain>=0);
            if(node.id==0)assertEquals(-1,node.predecessor);
            else {
                assertTrue(node.predecessor<node.id);
                assertEquals("Every branch climbs exactly one row",
                        node.tier-1,AscensionTree.byId(node.predecessor).tier);
            }
            assertEquals(node.id,AscensionTree.at(node.tier,node.column).id);
        }
        assertEquals("LAST BASTION",AscensionTree.byId(39).name);
    }

    @Test public void purchaseRequiresParentRankAndSuppliesAndSurvivesRestart(){
        SharedPreferences prefs=preferences();
        prefs.edit().putInt("supplies",500).commit();
        AscensionTree tree=new AscensionTree(prefs);
        assertEquals(1,tree.purchasedCount());
        assertTrue(tree.owned(0));
        assertFalse(tree.available(4,5)); // requires parent at tier 1
        assertFalse(tree.buy(4,5));
        assertTrue(tree.available(1,1));
        assertTrue(tree.buy(1,1));
        assertEquals(425,prefs.getInt("supplies",-1));
        assertFalse(tree.buy(1,1)); // no double-charge
        assertTrue(new AscensionTree(prefs).owned(1));
        assertTrue(new AscensionTree(prefs).available(4,2));
        assertFalse(new AscensionTree(prefs).available(4,1));
        assertTrue(tree.buy(4,2));
        assertEquals(265,prefs.getInt("supplies",-1));
        assertFalse(tree.available(39,5));
        assertEquals(3,tree.purchasedCount());
        assertEquals(0f,tree.bonus(AscensionTree.ATTACK),.0001f);
        assertTrue("Field Medicine node grants real hero vitality",tree.heroHp()>0);
        assertTrue(tree.coreHp()>=0);
    }

    @Test public void advancedNodesChangeActualWeaponDamageAndPersist()throws Exception{
        SharedPreferences prefs=preferences();
        prefs.edit().putInt("supplies",1200).commit();
        AscensionTree tree=new AscensionTree(prefs);
        assertTrue(tree.buy(3,1)); // first-tier attack branch
        float multiplier=tree.attackMultiplier();
        assertTrue("Bought upgrade must increase weapon power",multiplier>1f);
        AscensionTree reload=new AscensionTree(prefs);
        assertEquals(multiplier,reload.attackMultiplier(),.0001f);
        // Don't test only the arithmetic: fire a real projectile in the View.
        RiftGameView live=new RiftGameView(RuntimeEnvironment.getApplication());
        Method start=RiftGameView.class.getDeclaredMethod("start",int.class);
        start.setAccessible(true);start.invoke(live,0);
        Method spawn=RiftGameView.class.getDeclaredMethod("spawnEnemy");
        spawn.setAccessible(true);spawn.invoke(live);
        @SuppressWarnings("unchecked")
        java.util.List<Object> foes=(java.util.List<Object>)field(live,"enemies");
        Object foe=foes.get(0);
        Field x=foe.getClass().getDeclaredField("x"),y=foe.getClass().getDeclaredField("y");
        x.setAccessible(true);y.setAccessible(true);
        x.setFloat(foe,(float)field(live,"heroX")+155f);
        y.setFloat(foe,(float)field(live,"heroY"));
        Method fire=RiftGameView.class.getDeclaredMethod("fire");
        fire.setAccessible(true);fire.invoke(live);
        @SuppressWarnings("unchecked")
        java.util.List<Object> shots=(java.util.List<Object>)field(live,"shots");
        assertEquals(1,shots.size());
        Field damage=shots.get(0).getClass().getDeclaredField("damage");
        damage.setAccessible(true);
        assertEquals("Permanent ascension must modify actual shot DPS",
                Progression.WEAPONS[0].damage*multiplier,
                damage.getFloat(shots.get(0)),.01f);
    }

    @Test public void realFortressButtonOpensBottomOriginSwipeClimbsAndPurchases()
            throws Exception {
        SharedPreferences prefs=preferences();
        prefs.edit().putInt("supplies",850).commit();
        RiftGameView game=new RiftGameView(RuntimeEnvironment.getApplication());
        game.layout(0,0,1280,720);
        tap(game,467,544); // Home -> Fortress
        assertEquals("FORTRESS",field(game,"scene").toString());
        tap(game,895,657); // Fortress -> Ascension
        assertEquals("ASCENSION",field(game,"scene").toString());
        float bottom=(float)field(game,"ascensionScroll");
        assertTrue(bottom>1200);
        Bitmap image=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        game.draw(new Canvas(image));
        assertNotEquals(0,image.getPixel(450,360));
        image.recycle();
        AscensionTree.Node node=AscensionTree.byId(1);
        float nx=75+AscensionTree.x(node);
        float ny=148+AscensionTree.worldY(node)-bottom;
        assertTrue(ny>148&&ny<592);
        tap(game,nx,ny);
        assertEquals(node.id,field(game,"selectedAscensionNode"));
        tap(game,1080,555);
        assertTrue(new AscensionTree(prefs).owned(1));
        assertEquals(850-node.cost,prefs.getInt("supplies",-1));
        // User swipes UP to climb to high nodes; drag must not accidentally buy.
        drag(game,450,460,185);
        assertTrue("Swiping up should reveal the higher tiers",
                (float)field(game,"ascensionScroll")<bottom);
        assertEquals("ASCENSION",field(game,"scene").toString());
        tap(game,173,650);
        assertEquals("FORTRESS",field(game,"scene").toString());
    }
}
