package com.lokij07.riftguard;

import org.junit.Test;
import static org.junit.Assert.*;

/** Regression coverage for actual boss component health and tactical effects. */
public final class BossAnatomyV065Test {
    @Test public void allTenWorldsHaveDistinctNamedDamageableParts(){
        for(int world=0;world<10;world++){
            BossAnatomyV065 b=new BossAnatomyV065(world,400);
            assertNotEquals(b.name(0),b.name(1));
            assertNotEquals(b.name(1),b.name(2));
            for(int part=0;part<BossAnatomyV065.PARTS;part++){
                assertTrue(b.maxHealth(part)>0);
                assertEquals(1f,b.fraction(part),.001f);
                assertFalse(b.broken(part));
            }
        }
    }
    @Test public void armorBreakChangesIncomingDamageMultiplierExactlyOnce(){
        BossAnatomyV065 b=new BossAnatomyV065(5,500);
        assertEquals(1f,b.incomingMultiplier(),.001f);
        assertFalse(b.damage(BossAnatomyV065.ARMOR,b.maxHealth(0)*.5f));
        assertFalse(b.broken(0));
        assertTrue(b.damage(0,10000));
        assertTrue(b.broken(0));
        assertFalse(b.damage(0,10000));
        assertEquals(1.18f,b.incomingMultiplier(),.001f);
        assertEquals(0f,b.health(0),.001f);
    }
    @Test public void weaponBreakSuppressesFutureBossSiege(){
        BossAnatomyV065 b=new BossAnatomyV065(8,350);
        assertEquals(1f,b.siegeMultiplier(),.001f);
        assertTrue(b.damage(BossAnatomyV065.WEAPON,10000));
        assertEquals(.48f,b.siegeMultiplier(),.001f);
        assertEquals(1,b.nextIntact(BossAnatomyV065.WEAPON));
    }
    @Test public void coreBreakSuppressesReinforcementsAndFocusSkipsDestroyedParts(){
        BossAnatomyV065 b=new BossAnatomyV065(9,1000);
        assertTrue(b.canSummon());
        assertTrue(b.damage(BossAnatomyV065.CORE,10000));
        assertFalse(b.canSummon());
        assertEquals(0,b.nextIntact(2));
        assertTrue(b.damage(0,10000));
        assertTrue(b.damage(1,10000));
        assertEquals(-1,b.nextIntact(0));
    }
    @Test public void invalidInputsAreRejectedAndDamageIsClamped(){
        BossAnatomyV065 b=new BossAnatomyV065(0,150);
        assertFalse(b.damage(0,Float.NaN));
        assertFalse(b.damage(0,-5));
        assertEquals(b.maxHealth(0),b.health(0),.001f);
        assertThrows(IllegalArgumentException.class,()->new BossAnatomyV065(10,300));
        assertThrows(IllegalArgumentException.class,()->new BossAnatomyV065(1,0));
        assertThrows(IllegalArgumentException.class,()->b.name(3));
    }
}
