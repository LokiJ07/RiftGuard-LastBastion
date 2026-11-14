package com.lokij07.riftguard;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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
public class NodeEngravingV070Test {
    private Bitmap weapon(int family,int tier,int mask){
        Bitmap b=Bitmap.createBitmap(176,200,Bitmap.Config.ARGB_8888);
        EvolvedWeaponArt.draw(new Canvas(b),family,tier,mask,88,135,1.45f,1.1f);
        return b;
    }
    private int differences(Bitmap a,Bitmap b){
        int n=0;
        for(int y=12;y<194;y++)for(int x=12;x<164;x++)
            if(a.getPixel(x,y)!=b.getPixel(x,y))n++;
        return n;
    }
    @Test public void eachPurchasedNodeVisiblyChangesAllSixHeldWeapons(){
        for(int weapon=0;weapon<6;weapon++){
            int mask=1;
            for(int id=1;id<11;id++){
                int tier=WeaponMastery.node(weapon,id).tier;
                Bitmap old=weapon(weapon,tier,mask);
                Bitmap newer=weapon(weapon,tier,mask|(1<<id));
                assertTrue("weapon "+weapon+" node "+id,differences(old,newer)>4);
                old.recycle();newer.recycle();mask|=1<<id;
            }
            assertEquals(0x7ff,mask);
        }
    }
    @Test public void ownershipIsBasedOnExistingSaveKeyNotPreviewMask(){
        Context ctx=RuntimeEnvironment.getApplication();
        SharedPreferences p=ctx.getSharedPreferences("riftguard.v070.masktest",Context.MODE_PRIVATE);
        assertTrue(p.edit().clear().putInt("supplies",10000).commit());
        WeaponMastery mastery=new WeaponMastery(p);
        assertEquals(1,mastery.purchasedMask(0));
        int preview=mastery.purchasedMask(0)|(1<<3);
        assertEquals(1,mastery.purchasedMask(0));
        assertTrue(mastery.buy(0,3,3));
        assertEquals(preview,mastery.purchasedMask(0));
        assertEquals(preview,new WeaponMastery(p).purchasedMask(0));
        assertEquals(1,mastery.purchasedMask(1));
    }
    @Test public void idleCacheDoesNotMixBranchesWithEqualNodeCounts(){
        WardenSpriteFrameCache cache=new WardenSpriteFrameCache(new WardenRig());
        Bitmap out=Bitmap.createBitmap(420,300,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(out);
        assertTrue(cache.draw(c,180,225,1f,-1f,false,-1f,-1f,false,
                0,2,1|(1<<1),1,0,-1f));
        int first=cache.frameBuilds();
        assertTrue(cache.draw(c,180,225,1f,-1f,false,-1f,-1f,false,
                0,2,1|(1<<2),1,0,-1f));
        assertEquals(first+1,cache.frameBuilds());
        assertEquals(2,cache.cachedFrames());
        out.recycle();cache.clear();
    }
}
