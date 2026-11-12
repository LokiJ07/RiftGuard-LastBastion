package com.lokij07.riftguard;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import java.util.HashSet;
import java.util.Set;
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
public final class WeaponAnimationRigTest {
    private static Bitmap draw(MotionArt art,int weapon,float progress) {
        Bitmap image=Bitmap.createBitmap(340,325,Bitmap.Config.ARGB_8888);
        boolean drew=art.hero(new Canvas(image),170,257,1.1f,0f,false,
                progress,-1f,false,weapon);
        assertTrue(drew);
        return image;
    }
    private static int changed(Bitmap a,Bitmap b) {
        int count=0;
        for(int y=110;y<260;y+=2)
            for(int x=90;x<250;x+=2)
                if(a.getPixel(x,y)!=b.getPixel(x,y))count++;
        return count;
    }
    @Test public void sixWeaponsHaveDistinctTimelinesAndTrueArmMovement(){
        Set<Integer> frameSignatures=new HashSet<>();
        for(int weapon=0;weapon<6;weapon++){
            float duration=WeaponAnimation.duration(weapon);
            assertTrue(duration>=.12f&&duration<=.4f);
            assertEquals(-1f,WeaponAnimation.progress(weapon,0f),0f);
            assertEquals(.5f,WeaponAnimation.progress(weapon,duration*.5f),.001f);
            WeaponRig.Pose a=WeaponRig.aimedPose(weapon,.12f,-1f,1f,false,0f);
            WeaponRig.Pose b=WeaponRig.aimedPose(weapon,.64f,-1f,1f,false,0f);
            float delta=(float)Math.hypot(b.handX-a.handX,b.handY-a.handY)+
                    Math.abs(a.angle-b.angle);
            assertTrue("Weapon "+weapon+" needs real recoil/wrist movement",delta>2f);
            int signature=(int)(WeaponAnimation.anticipation(weapon,.36f)*100)
                    +(int)(WeaponAnimation.wristForward(weapon,.64f)*1000);
            frameSignatures.add(signature);
        }
        assertEquals("Independent weapon animation profiles",6,frameSignatures.size());
        assertTrue("Heavy sword must wind up slower than sabre",
                WeaponAnimation.duration(1)>WeaponAnimation.duration(2));
        assertTrue("Repeater recoil must be faster than staff channel",
                WeaponAnimation.duration(4)<WeaponAnimation.duration(3));
    }
    @Test public void actualPaintedHeroAndHeldGearChangeForEveryWeaponAttack(){
        MotionArt art=new MotionArt(RuntimeEnvironment.getApplication());
        for(int weapon=0;weapon<6;weapon++){
            Bitmap idle=draw(art,weapon,-1f);
            Bitmap windup=draw(art,weapon,.15f);
            Bitmap contact=draw(art,weapon,.60f);
            assertTrue("All six arm rigs must animate at contact, weapon="+weapon,
                    changed(idle,contact)>55);
            assertTrue("Anticipation and contact need different body/hand positions, weapon="+weapon,
                    changed(windup,contact)>50);
            assertEquals(Color.TRANSPARENT,idle.getPixel(9,9));
            idle.recycle();windup.recycle();contact.recycle();
        }
    }
    @Test public void projectileTipAndAimStaySynchronizedAtSeveralAngles(){
        for(int weapon:new int[]{0,3,4,5})
            for(float face:new float[]{0f,1.2f,(float)Math.PI,-1.0f}){
                WeaponRig.Pose p=WeaponRig.aimedPose(weapon,0f,-1f,2.1f,false,face);
                WeaponRig.Point tip=WeaponRig.muzzle(weapon,0f,-1f,2.1f,false,face);
                boolean left=Math.cos(face)<-.28f;
                float dx=tip.x-(left?-p.handX:p.handX);
                float dy=tip.y-p.handY;
                assertTrue("Real muzzle cannot be at player center",
                        (float)Math.hypot(dx,dy)>39f);
                float direction=(float)(dx*Math.cos(face)+dy*Math.sin(face));
                assertTrue("Muzzle must aim at target",direction>39f);
            }
    }
}
