package com.lokij07.riftguard;

import org.junit.Test;
import static org.junit.Assert.*;

public final class WeaponComboV065Test {
    @Test public void allSixWeaponsExecuteThreeDistinctDamageBeats(){
        for(int weapon=0;weapon<6;weapon++){
            WeaponComboV065 tracker=new WeaponComboV065();
            WeaponComboV065.Hit one=tracker.attack(weapon,10f,false);
            WeaponComboV065.Hit two=tracker.attack(weapon,10.3f,false);
            WeaponComboV065.Hit three=tracker.attack(weapon,10.6f,false);
            assertEquals(1,one.step);
            assertEquals(2,two.step);
            assertEquals(3,three.step);
            assertTrue(two.multiplier>one.multiplier);
            assertTrue(three.multiplier>two.multiplier);
            assertEquals(1,tracker.attack(weapon,10.9f,false).step);
        }
    }
    @Test public void chargedAttacksBeatUnchargedButRequireHeldGesture(){
        WeaponComboV065 c=new WeaponComboV065();
        float normal=c.attack(1,1f,false).multiplier;
        c.reset();
        WeaponComboV065.Hit charged=c.attack(1,1f,true);
        assertTrue(charged.charged);
        assertTrue(charged.multiplier>normal);
        assertEquals(1,charged.step);
        assertTrue(WeaponComboV065.CHARGE_SECONDS>.5f);
    }
    @Test public void missingComboWindowOrWeaponSwitchResetsChain(){
        WeaponComboV065 c=new WeaponComboV065();
        c.attack(1,0f,false);
        assertEquals(2,c.attack(1,.2f,false).step);
        assertEquals(1,c.attack(0,.3f,false).step);
        assertEquals(1,c.attack(0,4f,false).step);
        c.reset();
        assertEquals(1,c.attack(5,5f,false).step);
    }
    @Test public void invalidWeaponsAndNonsensicalClockAreRejected(){
        WeaponComboV065 c=new WeaponComboV065();
        assertThrows(IllegalArgumentException.class,()->c.attack(-1,1f,false));
        assertThrows(IllegalArgumentException.class,()->c.attack(6,1f,false));
        assertThrows(IllegalArgumentException.class,()->c.attack(0,Float.NaN,false));
    }
}
