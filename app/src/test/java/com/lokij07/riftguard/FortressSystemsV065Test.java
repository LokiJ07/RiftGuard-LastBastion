package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public class FortressSystemsV065Test {
    private SharedPreferences prefs;
    @Before public void reset(){
        prefs=RuntimeEnvironment.getApplication().getSharedPreferences("fortress_v065_test",
                Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
    }
    @Test public void allThreeFormationsSaveAndRestoreIndependently(){
        DefenseLoadouts d=new DefenseLoadouts(prefs);
        assertFalse(d.saved(0));assertEquals("BALANCED",d.name(0));
        int[] levels={1,2,0,5,4,1,0,0},types={2,0,2,3,0,1,0,0};
        d.save(1,levels,types,6);
        d.rename(1,"Cannon stronghold");
        DefenseLoadouts reread=new DefenseLoadouts(prefs);
        assertTrue(reread.saved(1));
        assertEquals("Cannon stronghold",reread.name(1));
        assertEquals(5,reread.tier(1,3));
        assertEquals(3,reread.type(1,3));
        assertEquals(0,reread.tier(1,6));
        assertFalse(reread.saved(2));
    }
    @Test public void blueprintUsesRealGoldAndDoesNotOverrideFreeStartingTowers(){
        DefenseLoadouts d=new DefenseLoadouts(prefs);
        int[] savedLevels={3,2,2,0,0,0,0,0};
        int[] savedTypes={1,3,2,0,0,0,0,0};
        d.save(0,savedLevels,savedTypes,5);
        int[] towers={1,0,0,0,0,0,0,0},types={0,1,2,3,0,1,2,3};
        int budget=1000;
        int cost=d.deploy(0,towers,types,5,budget);
        int expected=BattleRules.towerCost(1)+BattleRules.towerCost(2)
                +BattleRules.towerCost(0)+BattleRules.towerCost(1)
                +BattleRules.towerCost(0)+BattleRules.towerCost(1);
        assertEquals(expected,cost);
        assertEquals(3,towers[0]);assertEquals(2,towers[1]);
        assertEquals(2,towers[2]);assertEquals(1,types[0]);
        assertEquals(3,types[1]);
        int[] cheap={1,0,0,0,0,0,0,0},newTypes={0,1,2,3,0,1,2,3};
        assertEquals(0,d.deploy(0,cheap,newTypes,5,20));
        assertEquals(1,cheap[0]);assertEquals(0,cheap[1]);
    }
    @Test public void physicalTowerDamageRepairAndRebuildAreConsistent(){
        for(int tier=1;tier<=5;tier++){
            float hp=TowerDurability.maxHp(tier,3);
            assertTrue(hp>0);
            float damaged=TowerDurability.damage(hp,hp*.7f);
            assertTrue(TowerDurability.repairCost(damaged,hp)>0);
            float repaired=TowerDurability.repair(damaged,hp);
            assertTrue(repaired>damaged);
            assertTrue(repaired<=hp);
            assertEquals(0,TowerDurability.repairCost(hp,hp));
            assertEquals(0f,TowerDurability.damage(hp,hp*20),0.001f);
            assertTrue(TowerDurability.rebuildCost(tier)>=BattleRules.towerCost(0));
        }
    }
    @Test public void countdownAllowsEarlyWaveBonusOnlyBeforeTimerExpires(){
        for(int diff=0;diff<4;diff++){
            assertTrue(WavePacingV065.seconds(diff,true)>=
                    WavePacingV065.seconds(diff,false));
            assertEquals(0,WavePacingV065.earlyBonus(0,diff,false));
            assertEquals(35,WavePacingV065.earlyBonus(99,diff,false));
            assertTrue(WavePacingV065.earlyBonus(5,diff,false)>0);
        }
    }
    @Test public void revivalChargeLimitsAreDifficultyAware(){
        WardenRevivalV065 revive=new WardenRevivalV065();
        revive.reset(DifficultyRules.EASY);
        assertEquals(2,revive.remainingCharges());
        assertTrue(revive.fall());assertTrue(revive.isDown());
        assertFalse(revive.tick(1));assertTrue(revive.tick(5));
        assertEquals(1,revive.remainingCharges());
        assertTrue(revive.fall());assertTrue(revive.tick(6));
        assertFalse(revive.fall());
        revive.reset(DifficultyRules.NORMAL);
        assertTrue(revive.fall());assertFalse(revive.tick(5));
        assertTrue(revive.tick(3));assertFalse(revive.fall());
        revive.reset(DifficultyRules.HARD);
        assertFalse(revive.fall());
        revive.reset(DifficultyRules.EXPERT);
        assertFalse(revive.fall());
    }
}
