package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.view.MotionEvent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class KingdomMetaTest {
    private static final long DAY=86400000L;
    private static final long BASE_DAY=1700000000000L;
    private Context context;
    private SharedPreferences prefs;
    private KingdomMeta meta;
    @Before public void reset(){
        context=RuntimeEnvironment.getApplication();
        prefs=context.getSharedPreferences("riftguard_meta_tests",Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        meta=new KingdomMeta(prefs);
    }
    @Test public void dailyClaimPersistsAndCannotBeRepeatedOrClockRolledBack(){
        assertTrue(meta.canClaimDaily(BASE_DAY));
        assertEquals(90,meta.claimDaily(BASE_DAY));
        assertEquals(90,meta.supplies());
        assertEquals(1,meta.dailyStreak());
        assertEquals(0,meta.claimDaily(BASE_DAY));
        assertEquals(0,meta.claimDaily(BASE_DAY-DAY));
        assertEquals(0,meta.claimDaily(BASE_DAY-DAY*2));
        assertEquals(115,meta.claimDaily(BASE_DAY+DAY));
        assertEquals(2,meta.dailyStreak());
        assertEquals(90,meta.claimDaily(BASE_DAY+4*DAY));
        assertEquals(1,meta.dailyStreak());
        KingdomMeta restarted=new KingdomMeta(prefs);
        assertEquals(295,restarted.supplies());
        assertFalse(restarted.canClaimDaily(BASE_DAY+4*DAY));
    }
    @Test public void battlesUpdateRealQuestsAndAwardsExactlyOnce(){
        int stage=Campaign.stage(1,0);
        meta.recordBattle(stage,DifficultyRules.NORMAL,22,3,false,BASE_DAY);
        assertEquals(22,meta.questProgress(0,BASE_DAY));
        assertEquals(0,meta.questProgress(1,BASE_DAY));
        assertEquals(3,meta.questProgress(2,BASE_DAY));
        assertEquals(80,meta.claimQuest(0,BASE_DAY));
        assertEquals(90,meta.claimQuest(2,BASE_DAY));
        assertEquals(0,meta.claimQuest(0,BASE_DAY));
        meta.recordBattle(stage,DifficultyRules.NORMAL,0,0,true,BASE_DAY);
        meta.recordBattle(stage,DifficultyRules.NORMAL,0,0,true,BASE_DAY);
        assertEquals(2,meta.questProgress(1,BASE_DAY));
        assertEquals(120,meta.claimQuest(1,BASE_DAY));
        assertEquals(290,meta.supplies());
        assertEquals(1,meta.achievementProgress(0)>0?1:0);
        assertEquals(80,meta.claimAchievement(0));
        assertEquals(0,meta.claimAchievement(0));
        assertEquals(0,meta.questProgress(0,BASE_DAY+DAY));
        assertEquals(0,meta.questProgress(1,BASE_DAY+DAY));
        assertEquals(0,meta.questProgress(2,BASE_DAY+DAY));
        assertEquals(0,meta.claimQuest(0,BASE_DAY+DAY));
    }
    @Test public void weeklyEventObjectivesPersistAndRotate(){
        long when=BASE_DAY;
        int type=meta.eventIndex(when);
        assertTrue(type>=0&&type<=2);
        for(int i=0;i<75;i++)
            meta.recordBattle(Campaign.stage(10,2),DifficultyRules.HARD,
                    2,1,true,when);
        assertTrue(meta.eventProgress(when)>=meta.eventTarget(when));
        int award=meta.eventReward(when);
        assertEquals(award,meta.claimEvent(when));
        assertEquals(0,meta.claimEvent(when));
        long nextWeek=when+DAY*8;
        assertEquals(0,meta.eventProgress(nextWeek));
        assertFalse(meta.eventClaimed(nextWeek));
        assertNotEquals(type,meta.eventIndex(nextWeek));
        assertEquals(75,meta.totalWins());
        assertEquals(75,meta.totalBossWins());
    }
    @Test public void shopPurchasesApplyActualStatsAndCannotOverspend(){
        prefs.edit().putInt("supplies",5000).commit();
        for(int id=0;id<4;id++){
            int prior=meta.supplies(),rank=meta.shopRank(id),cost=meta.shopCost(id);
            assertTrue(meta.buy(id));
            assertEquals(prior-cost,meta.supplies());
            assertEquals(rank+1,meta.shopRank(id));
        }
        assertEquals(115,meta.heroMaxHp());
        assertEquals(160,meta.coreBonusHp());
        assertEquals(1.09f,meta.towerDamageMultiplier(),.001f);
        assertEquals(10f,meta.pulseCooldown(),.001f);
        int remain=meta.supplies();
        try { meta.buy(-1); fail("Invalid shop index should be rejected"); }
        catch(IllegalArgumentException expected){}
        assertEquals(remain,meta.supplies());
    }
    @Test public void shopBoughtThroughRealUiChangesBattleStatsAfterRestart()
            throws Exception {
        context.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE)
                .edit().clear().putInt("supplies",3000).commit();
        RiftGameView game=new RiftGameView(context);
        game.layout(0,0,1280,720);
        touch(game,900,475); // enter hub
        touch(game,1090,177); // Shop tab
        touch(game,472,355); // Warden Vitality
        touch(game,1048,355); // Core Reinforcement
        touch(game,472,530); // Tower Arsenal
        touch(game,1048,530); // Pulse Capacitor
        assertEquals(1,new KingdomMeta(context.getSharedPreferences(
                "riftguard_campaign_v1",Context.MODE_PRIVATE)).shopRank(0));
        RiftGameView restarted=new RiftGameView(context);
        Method start=RiftGameView.class.getDeclaredMethod("start",int.class);
        start.setAccessible(true);start.invoke(restarted,0);
        assertEquals(115f,(Float)field(restarted,"heroHp"),.001f);
        assertEquals(Progression.maxCoreHp(1)+160f,
                (Float)field(restarted,"baseHp"),.001f);
        Method pulse=RiftGameView.class.getDeclaredMethod("pulse");
        pulse.setAccessible(true);pulse.invoke(restarted);
        assertEquals(10f,(Float)field(restarted,"pulseCooldown"),.001f);
        assertTrue(new KingdomMeta(context.getSharedPreferences(
                "riftguard_campaign_v1",Context.MODE_PRIVATE)).towerDamageMultiplier()>1f);
    }
    @Test public void rollingBackPhoneClockDoesNotReopenOldWeeklyEvent() {
        long t=BASE_DAY;
        int newer=meta.eventIndex(t+DAY*8);
        int older=meta.eventIndex(t);
        assertEquals("Older phone date must not change the active event",newer,older);
    }

    @Test public void theActualFiveTabKingdomUiRendersAndAcceptsRealTaps() throws Exception {
        context.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE)
                .edit().clear().putInt("supplies",1000).commit();
        RiftGameView game=new RiftGameView(context);
        game.layout(0,0,1280,720);
        touch(game,900,478);
        assertEquals("KINGDOM",field(game,"scene").toString());
        Bitmap bitmap=Bitmap.createBitmap(1280,720,Bitmap.Config.ARGB_8888);
        for(int tab=0;tab<5;tab++){
            touch(game,185+tab*226,178);
            assertEquals(tab,field(game,"kingdomTab"));
            game.draw(new Canvas(bitmap));
            assertTrue(android.graphics.Color.alpha(bitmap.getPixel(900,470))>0);
        }
        int suppliesBefore=(Integer)field(game,"supplies");
        touch(game,184,178); // daily
        touch(game,640,522); // claim reward (may be first of day)
        assertTrue((Integer)field(game,"supplies")>=suppliesBefore);
        touch(game,200,652); // leave kingdom hub
        assertEquals("HOME",field(game,"scene").toString());
        bitmap.recycle();
    }
    private static Object field(Object o,String name) throws Exception {
        Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);
        return f.get(o);
    }
    private static void touch(RiftGameView view,float x,float y){
        long t=SystemClock.uptimeMillis();
        MotionEvent down=MotionEvent.obtain(t,t,MotionEvent.ACTION_DOWN,x,y,0);
        view.onTouchEvent(down);down.recycle();
        MotionEvent up=MotionEvent.obtain(t,t+25,MotionEvent.ACTION_UP,x,y,0);
        view.onTouchEvent(up);up.recycle();
    }
}
