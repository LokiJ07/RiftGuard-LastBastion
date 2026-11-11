package com.lokij07.riftguard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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
public final class FortressEquipmentVisualTest {
    @Test public void eachOfThreeStoneFortressRanksVisiblyChangesSilhouette() {
        Context ctx=RuntimeEnvironment.getApplication();
        MotionArt artwork=new MotionArt(ctx);
        assertTrue(artwork.fortressReady());
        Bitmap[] pictures=new Bitmap[3];
        for(int rank=3;rank<=5;rank++){
            Bitmap result=Bitmap.createBitmap(700,700,Bitmap.Config.ARGB_8888);
            Canvas canvas=new Canvas(result);
            canvas.drawColor(Color.TRANSPARENT);
            assertTrue(artwork.fortress(canvas,350,405,rank,.7f));
            pictures[rank-3]=result;
        }
        for(int r=0;r<2;r++){
            int changed=0;
            for(int y=85;y<610;y+=4)
                for(int x=100;x<615;x+=4)
                    if(pictures[r].getPixel(x,y)!=pictures[r+1].getPixel(x,y))
                        changed++;
            assertTrue("Fortress ranks must visibly change: "+(r+3),changed>130);
        }
        for(Bitmap b:pictures)b.recycle();
    }
    @Test public void allFourTurretPositionsAreConnectedToRealMasonry() {
        Bitmap bmp=Bitmap.createBitmap(1900,1050,Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(bmp);
        float[] x={810,1092,811,1093},y={370,370,624,624};
        FortressStructures.turretMounts(c,950,510,5,x,y,1f);
        for(int i=0;i<4;i++){
            int color=bmp.getPixel((int)x[i],(int)y[i]+16);
            assertNotEquals("Mount support missing at turret "+i,Color.TRANSPARENT,color);
        }
        bmp.recycle();
    }
    @Test public void everyHeroWeaponUsesItsOwnAnimatedWristSocket() {
        for(int id=0;id<6;id++){
            WeaponRig.Pose idle=WeaponRig.pose(id,-1,-1,0);
            WeaponRig.Pose attack=WeaponRig.pose(id,.5f,-1,.3f);
            assertTrue(Float.isFinite(idle.handX)&&Float.isFinite(idle.handY));
            assertTrue(Float.isFinite(attack.angle));
            assertTrue(Math.abs(idle.handX-attack.handX)>1f ||
                    Math.abs(idle.handY-attack.handY)>1f ||
                    Math.abs(idle.angle-attack.angle)>1f);
            assertTrue(attack.elbowX>=idle.shoulderX-20);
            assertTrue(attack.handY<0);
        }
        assertNotEquals(WeaponRig.pose(1,-1,-1,0).angle,
                WeaponRig.pose(0,-1,-1,0).angle,.01f);
        assertNotEquals(WeaponRig.pose(3,-1,-1,0).handY,
                WeaponRig.pose(1,-1,-1,0).handY,.01f);
    }
    @Test public void equippedWeaponRendersUniqueFramesAndCombatArt() {
        MotionArt art=new MotionArt(RuntimeEnvironment.getApplication());
        assertTrue(art.heroReady());
        assertTrue(art.weaponsReady());
        int[] samples=new int[6];
        for(int id=0;id<6;id++){
            Bitmap bitmap=Bitmap.createBitmap(240,230,Bitmap.Config.ARGB_8888);
            Canvas c=new Canvas(bitmap);
            assertTrue(art.hero(c,115,170,1.3f,-1,false,.45f,-1,false,id));
            // FNV checksum samples the actual rendered output, not weapon metadata.
            int hash=0x811c9dc5;
            for(int y=25;y<198;y+=3)
                for(int x=52;x<181;x+=3)
                    hash=(hash^bitmap.getPixel(x,y))*0x01000193;
            samples[id]=hash;
            bitmap.recycle();
        }
        for(int i=0;i<samples.length;i++)
            for(int j=i+1;j<samples.length;j++)
                assertNotEquals("Weapon "+i+" and "+j+" rendered identically",
                        samples[i],samples[j]);
    }
    @Test public void allCommonViewportAspectRatiosRenderWithoutThrowing() throws Exception {
        Context ctx=RuntimeEnvironment.getApplication();
        ctx.getSharedPreferences("riftguard_campaign_v1",Context.MODE_PRIVATE)
                .edit().clear().putInt("fortress_rank",5).commit();
        RiftGameView view=new RiftGameView(ctx);
        Method start=RiftGameView.class.getDeclaredMethod("start",int.class);
        start.setAccessible(true);start.invoke(view,0);
        int[][] sizes={{1280,720},{1600,900},{1680,720},
                {1780,720},{1920,1080},{1024,768},{1920,1200}};
        for(int[] size:sizes){
            view.layout(0,0,size[0],size[1]);
            Bitmap bmp=Bitmap.createBitmap(size[0],size[1],Bitmap.Config.ARGB_8888);
            view.draw(new Canvas(bmp));
            assertNotEquals(Color.TRANSPARENT,
                    bmp.getPixel(size[0]/2,size[1]/2));
            bmp.recycle();
        }
    }
}
