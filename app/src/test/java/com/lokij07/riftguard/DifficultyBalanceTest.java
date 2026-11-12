package com.lokij07.riftguard;

import android.content.Context;
import android.content.SharedPreferences;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

/** Easy/Normal/Hard/Expert should be structurally distinct in LIVE gameplay. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public final class DifficultyBalanceTest {
    private static float pressure(int mode){
        int stage=0;
        return DifficultyRules.health(mode)*DifficultyRules.enemyDamage(mode)*
                DifficultyRules.enemies(stage,1,mode)*
                DifficultyRules.waves(stage,mode)/
                DifficultyRules.spawnInterval(stage,mode);
    }
    private static float value(Object target,String name)throws Exception{
        Field f=target.getClass().getDeclaredField(name);f.setAccessible(true);
        return f.getFloat(target);
    }
    private static int number(Object target,String name)throws Exception{
        Field f=target.getClass().getDeclaredField(name);f.setAccessible(true);
        return f.getInt(target);
    }
    private static void set(Object target,String name,int amount)throws Exception{
        Field f=target.getClass().getDeclaredField(name);f.setAccessible(true);
        f.setInt(target,amount);
    }
    private static RiftGameView game(int mode)throws Exception{
        Context ctx=RuntimeEnvironment.getApplication();
        SharedPreferences p=ctx.getSharedPreferences("riftguard_campaign_v1",
                Context.MODE_PRIVATE);
        p.edit().clear().putInt("difficulty",mode).commit();
        RiftGameView view=new RiftGameView(ctx);
        Method start=RiftGameView.class.getDeclaredMethod("start",int.class);
        start.setAccessible(true);start.invoke(view,0);
        return view;
    }
    @Test public void allFourModesHaveLargeMonotonicPressureSeparation(){
        for(int mode=0;mode<3;mode++) {
            assertTrue(DifficultyRules.health(mode+1)>DifficultyRules.health(mode));
            assertTrue(DifficultyRules.enemyDamage(mode+1)>DifficultyRules.enemyDamage(mode));
            assertTrue(DifficultyRules.enemySpeed(mode+1)>DifficultyRules.enemySpeed(mode));
            assertTrue(DifficultyRules.enemies(0,1,mode+1)>
                    DifficultyRules.enemies(0,1,mode));
            assertTrue(DifficultyRules.waves(0,mode+1)>DifficultyRules.waves(0,mode));
            assertTrue(DifficultyRules.spawnInterval(0,mode+1)<
                    DifficultyRules.spawnInterval(0,mode));
            assertTrue(pressure(mode+1)>pressure(mode)*1.5f);
        }
        assertTrue("Expert must be massively stronger than Normal even on level 1",
                pressure(DifficultyRules.EXPERT)>pressure(DifficultyRules.NORMAL)*30);
        assertTrue(DifficultyRules.health(3)>=3.5f);
        assertTrue(DifficultyRules.enemyDamage(3)>=2.7f);
        assertTrue(DifficultyRules.waves(0,3)>=9);
        assertTrue(DifficultyRules.enemies(0,1,3)>=16);
        assertEquals(0,DifficultyRules.initialTowers(1,3));
        assertEquals(1,DifficultyRules.initialTowers(1,1));
        assertTrue(DifficultyRules.startingGold(1,3)<
                DifficultyRules.startingGold(1,1));
    }
    @Test public void eliteReinforcementsThreatenNormalMapsNotJustFinalBosses(){
        assertFalse(DifficultyRules.eliteWave(0,2,DifficultyRules.NORMAL));
        assertTrue(DifficultyRules.eliteWave(0,2,DifficultyRules.EXPERT));
        assertTrue(DifficultyRules.eliteWave(0,3,DifficultyRules.HARD));
        assertFalse(DifficultyRules.eliteWave(0,2,DifficultyRules.HARD));
        assertTrue(DifficultyRules.eliteMultiplier(0,2,3)>4f);
        assertTrue(DifficultyRules.worldBoss(Campaign.stage(10,2)));
        assertEquals(DifficultyRules.finalBossMultiplier(Campaign.stage(10,2)),
                DifficultyRules.eliteMultiplier(Campaign.stage(10,2),
                        DifficultyRules.waves(Campaign.stage(10,2),3),3),.001f);
        assertTrue(DifficultyRules.supplyBonus(3,100)>
                DifficultyRules.supplyBonus(2,100));
    }
    @Test public void startingExpertGameHasActualHardResourceAndHeroHandicap()
            throws Exception {
        RiftGameView easy=game(DifficultyRules.EASY);
        float easyHp=value(easy,"heroHp");
        int easyGold=number(easy,"gold");
        RiftGameView normal=game(DifficultyRules.NORMAL);
        float normalHp=value(normal,"heroHp");
        int normalGold=number(normal,"gold");
        RiftGameView expert=game(DifficultyRules.EXPERT);
        assertTrue(value(expert,"heroHp")<normalHp);
        assertTrue(normalHp<easyHp);
        assertTrue(number(expert,"gold")<normalGold);
        assertTrue(normalGold<easyGold);
        assertTrue(number(expert,"remaining")>number(normal,"remaining"));
        Field towers=RiftGameView.class.getDeclaredField("towers");
        towers.setAccessible(true);
        int[] expertTowers=(int[])towers.get(expert);
        assertEquals(0,expertTowers[0]);
        // Even early maps gain a high-health elite when playing Expert.
        set(expert,"wave",2);set(expert,"remaining",1);
        Method spawn=RiftGameView.class.getDeclaredMethod("spawnEnemy");
        spawn.setAccessible(true);spawn.invoke(expert);
        Field enemiesField=RiftGameView.class.getDeclaredField("enemies");
        enemiesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Object> enemies=(List<Object>)enemiesField.get(expert);
        Object elite=enemies.get(enemies.size()-1);
        assertEquals(5,number(elite,"kind"));
        assertTrue(value(elite,"hp")>BattleRules.enemyHealth(0,5)*10);
    }
}
